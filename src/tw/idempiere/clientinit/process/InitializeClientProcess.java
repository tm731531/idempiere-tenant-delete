package tw.idempiere.clientinit.process;

import java.sql.Timestamp;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;

import tw.idempiere.clientinit.cleanup.SequenceReset;
import tw.idempiere.clientinit.cleanup.StatisticsReset;
import tw.idempiere.clientinit.cleanup.TransactionCleanup;
import tw.idempiere.clientinit.data.ForeignKeyAnalyzer;
import tw.idempiere.clientinit.data.ForeignKeyAnalyzer.TableInfo;
import tw.idempiere.clientinit.data.TableDependency;
import tw.idempiere.clientinit.data.TransactionTables;
import tw.idempiere.clientinit.util.CleanupLog;

/**
 * 初始化 Client 交易資料
 *
 * 功能：
 * 1. 按外鍵依賴順序刪除交易資料
 * 2. 支援日期範圍篩選
 * 3. 重置文件序號
 * 4. 重置統計值
 *
 * @author iDempiere Community
 */
public class InitializeClientProcess extends SvrProcess {

    // 參數
    private int p_AD_Client_ID = 0;  // 目標 Client ID
    private Timestamp p_DateFrom = null;
    private Timestamp p_DateTo = null;
    private boolean p_IsResetSequence = true;
    private boolean p_IsResetStatistics = true;
    private boolean p_IsPreviewOnly = false;

    // 清理日誌
    private CleanupLog cleanupLog;

    @Override
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (ProcessInfoParameter p : para) {
            String name = p.getParameterName();
            if ("Target_AD_Client_ID".equals(name)) {
                p_AD_Client_ID = p.getParameterAsInt();
            } else if ("DateFrom".equals(name)) {
                p_DateFrom = p.getParameterAsTimestamp();
            } else if ("DateTo".equals(name)) {
                p_DateTo = p.getParameterAsTimestamp();
            } else if ("IsResetSequence".equals(name)) {
                p_IsResetSequence = p.getParameterAsBoolean();
            } else if ("IsResetStatistics".equals(name)) {
                p_IsResetStatistics = p.getParameterAsBoolean();
            } else if ("IsPreviewOnly".equals(name)) {
                p_IsPreviewOnly = p.getParameterAsBoolean();
            } else {
                log.log(Level.WARNING, "Unknown Parameter: " + name);
            }
        }
    }

    @Override
    protected String doIt() throws Exception {
        // 安全檢查：必須以 System Client 登入
        int loginClientId = getAD_Client_ID();
        if (loginClientId != 0) {
            throw new AdempiereException("必須以 System Client 登入才能執行此 Process");
        }

        // 安全檢查：必須選擇目標 Client
        if (p_AD_Client_ID < 0) {
            throw new AdempiereException("請選擇要初始化的 Tenant (Client)");
        }

        // 安全檢查：禁止初始化 System Client
        if (p_AD_Client_ID == 0) {
            throw new AdempiereException("System Client (ID=0) 不允許被初始化");
        }

        // 使用參數指定的 Client ID
        int clientId = p_AD_Client_ID;
        cleanupLog = new CleanupLog(clientId);

        // 取得 Client 名稱
        String clientName = org.compiere.model.MClient.get(getCtx(), clientId).getName();

        cleanupLog.info("=== 開始初始化 Client: " + clientName + " (ID=" + clientId + ") ===");
        addLog("=== 開始初始化 Client: " + clientName + " (ID=" + clientId + ") ===");

        // 顯示參數
        if (p_DateFrom != null) {
            cleanupLog.info("日期範圍起: " + p_DateFrom);
            addLog("日期範圍起: " + p_DateFrom);
        }
        if (p_DateTo != null) {
            cleanupLog.info("日期範圍迄: " + p_DateTo);
            addLog("日期範圍迄: " + p_DateTo);
        }

        if (p_IsPreviewOnly) {
            return doPreview(clientId);
        }

        return doInitialize(clientId);
    }

    /**
     * 執行預覽模式（使用動態 FK 分析）
     * @param clientId Client ID
     * @return 結果訊息
     */
    private String doPreview(int clientId) {
        cleanupLog.info("--- 預覽模式（動態 FK 分析）---");
        addLog("--- 預覽模式（動態 FK 分析）---");
        addLog("");

        // 使用動態 FK 分析取得所有交易表
        java.util.List<TableInfo> tables = ForeignKeyAnalyzer.getDeleteOrderWithCount(clientId, get_TrxName());

        // 過濾出交易表（排除主資料表）
        java.util.List<TableInfo> transactionTables = new java.util.ArrayList<>();
        for (TableInfo info : tables) {
            if (TransactionTables.isTransactionTable(info.tableName)) {
                transactionTables.add(info);
            }
        }

        int totalRecords = 0;
        int tableCount = 0;

        addLog("--- 交易資料統計 ---");
        for (TableInfo info : transactionTables) {
            // 如果有日期範圍，重新計算
            int count = info.recordCount;
            if (p_DateFrom != null || p_DateTo != null) {
                count = TransactionCleanup.countRecords(info.tableName, clientId, p_DateFrom, p_DateTo, get_TrxName());
            }

            if (count > 0) {
                String msg = "  " + info.tableName + ": " + count + " 筆";
                cleanupLog.info(msg);
                addLog(msg);
                totalRecords += count;
                tableCount++;
            }
        }

        addLog("");
        addLog("--- 摘要 ---");
        addLog("  涉及表數量: " + tableCount);
        addLog("  交易記錄總數: " + totalRecords);

        // 序號預覽
        if (p_IsResetSequence) {
            addLog("");
            cleanupLog.info("--- 序號將被重置 ---");
            addLog("--- 序號將被重置 ---");
            String seqInfo = SequenceReset.getSequenceInfo(clientId, get_TrxName());
            if (!seqInfo.isEmpty()) {
                for (String line : seqInfo.split("\n")) {
                    if (!line.isEmpty()) {
                        addLog("  " + line);
                    }
                }
            }
        }

        // 統計值預覽
        if (p_IsResetStatistics) {
            addLog("");
            cleanupLog.info("--- 統計值將被重置 ---");
            addLog("--- 統計值將被重置 ---");
            String statInfo = StatisticsReset.getStatisticsSummary(clientId, get_TrxName());
            for (String line : statInfo.split("\n")) {
                if (!line.isEmpty()) {
                    addLog("  " + line);
                }
            }
        }

        cleanupLog.setEndTime();
        return "預覽完成：共 " + tableCount + " 個表，" + totalRecords + " 筆記錄將被刪除";
    }

    /**
     * 執行初始化（使用動態 FK 分析）
     * @param clientId Client ID
     * @return 結果訊息
     * @throws Exception 發生錯誤時
     */
    private String doInitialize(int clientId) throws Exception {
        int totalDeleted = 0;
        int tableCount = 0;

        // Phase 0: 清理暫存表
        cleanupLog.logPhaseStart(0, "清理暫存表");
        addLog("--- Phase 0: 清理暫存表 ---");
        int tempDeleted = TransactionCleanup.cleanTempTables(clientId, get_TrxName());
        if (tempDeleted > 0) {
            cleanupLog.info("暫存表清理: " + tempDeleted + " 筆");
            addLog("暫存表清理: " + tempDeleted + " 筆");
        }
        commitEx();

        // 使用動態 FK 分析取得刪除順序
        addLog("");
        addLog("--- 分析外鍵依賴關係 ---");
        java.util.List<String> deleteOrder = ForeignKeyAnalyzer.getDeleteOrder(clientId, get_TrxName());
        addLog("  找到 " + deleteOrder.size() + " 個有資料的表");

        // 過濾出交易表
        java.util.List<String> transactionTables = new java.util.ArrayList<>();
        for (String table : deleteOrder) {
            if (TransactionTables.isTransactionTable(table)) {
                transactionTables.add(table);
            }
        }
        addLog("  其中 " + transactionTables.size() + " 個是交易表");
        addLog("");

        // 按順序刪除交易表
        addLog("--- 刪除交易資料 ---");
        int batchCount = 0;
        for (String table : transactionTables) {
            int deleted = TransactionCleanup.deleteTable(table, clientId, p_DateFrom, p_DateTo, get_TrxName());
            cleanupLog.logTableDelete(table, deleted);
            if (deleted > 0) {
                addLog("  " + table + ": 刪除 " + deleted + " 筆");
                totalDeleted += deleted;
                tableCount++;
            }

            // 每 20 個表 commit 一次
            batchCount++;
            if (batchCount % 20 == 0) {
                commitEx();
            }
        }
        commitEx();

        // 重置序號
        if (p_IsResetSequence) {
            addLog("");
            cleanupLog.logPhaseStart(99, "重置文件序號");
            addLog("--- 重置文件序號 ---");
            int seqCount = SequenceReset.resetDocumentSequences(clientId, get_TrxName());
            cleanupLog.info("重置序號: " + seqCount + " 個");
            addLog("  重置序號: " + seqCount + " 個");
            commitEx();
        }

        // 重置統計值
        if (p_IsResetStatistics) {
            addLog("");
            cleanupLog.logPhaseStart(100, "重置統計值");
            addLog("--- 重置統計值 ---");
            StatisticsReset.resetAll(clientId, get_TrxName());
            cleanupLog.info("統計值已重置");
            addLog("  統計值已重置");
            commitEx();
        }

        cleanupLog.setEndTime();
        addLog("");
        addLog("========================================");
        cleanupLog.info("=== 初始化完成 ===");
        addLog("=== 初始化完成 ===");
        addLog("  已刪除表數量: " + tableCount);
        addLog("  已刪除記錄數: " + totalDeleted);
        addLog("========================================");

        // 輸出完整報告到日誌
        log.info(cleanupLog.generateReport());

        return "初始化完成：共 " + tableCount + " 個表，" + totalDeleted + " 筆記錄已刪除";
    }
}

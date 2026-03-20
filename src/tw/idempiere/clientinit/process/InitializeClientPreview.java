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
import tw.idempiere.clientinit.data.TableDependency;

/**
 * Client 初始化預覽報告
 *
 * 顯示將被刪除的記錄數，不實際執行刪除。
 * 用於在執行初始化前確認影響範圍。
 *
 * @author iDempiere Community
 */
public class InitializeClientPreview extends SvrProcess {

    // 參數
    private int p_AD_Client_ID = 0;  // 目標 Client ID
    private Timestamp p_DateFrom = null;
    private Timestamp p_DateTo = null;

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
            throw new AdempiereException("請選擇要預覽的 Tenant (Client)");
        }

        // 安全檢查：禁止預覽 System Client
        if (p_AD_Client_ID == 0) {
            throw new AdempiereException("System Client (ID=0) 不允許被預覽");
        }

        // 使用參數指定的 Client ID
        int clientId = p_AD_Client_ID;

        // 取得 Client 名稱
        String clientName = org.compiere.model.MClient.get(getCtx(), clientId).getName();

        addLog("========================================");
        addLog("     Client 初始化預覽報告");
        addLog("========================================");
        addLog("");
        addLog("Client: " + clientName + " (ID=" + clientId + ")");

        if (p_DateFrom != null) {
            addLog("日期範圍起: " + p_DateFrom);
        }
        if (p_DateTo != null) {
            addLog("日期範圍迄: " + p_DateTo);
        }

        addLog("");
        addLog("----------------------------------------");
        addLog("交易資料統計:");
        addLog("----------------------------------------");

        int totalRecords = 0;
        int currentPhase = 0;

        // 遍歷所有交易表，統計記錄數
        for (String[] phase : TableDependency.DELETE_ORDER) {
            currentPhase++;
            boolean hasRecords = false;

            for (String table : phase) {
                int count = TransactionCleanup.countRecords(
                    table, clientId, p_DateFrom, p_DateTo, get_TrxName());
                if (count > 0) {
                    if (!hasRecords) {
                        addLog("");
                        addLog("Phase " + currentPhase + ":");
                        hasRecords = true;
                    }
                    addLog("  " + table + ": " + count + " 筆");
                    totalRecords += count;
                }
            }
        }

        addLog("");
        addLog("----------------------------------------");
        addLog("序號資訊:");
        addLog("----------------------------------------");
        String seqInfo = SequenceReset.getSequenceInfo(clientId, get_TrxName());
        if (seqInfo.isEmpty()) {
            addLog("沒有需要重置的序號");
        } else {
            addLog("以下序號將被重置到初始值:");
            for (String line : seqInfo.split("\n")) {
                if (!line.isEmpty()) {
                    addLog("  " + line);
                }
            }
        }

        addLog("");
        addLog("----------------------------------------");
        addLog("統計值資訊:");
        addLog("----------------------------------------");
        String statInfo = StatisticsReset.getStatisticsSummary(clientId, get_TrxName());
        for (String line : statInfo.split("\n")) {
            if (!line.isEmpty()) {
                addLog("  " + line);
            }
        }

        addLog("");
        addLog("========================================");
        addLog("預覽摘要:");
        addLog("  - 交易記錄總數: " + totalRecords + " 筆");
        addLog("  - 涉及 Phase 數: " + currentPhase);
        addLog("========================================");

        if (totalRecords == 0) {
            return "沒有找到需要刪除的交易記錄";
        }

        return "預覽完成：共 " + totalRecords + " 筆記錄將被刪除";
    }
}

package tw.idempiere.clientinit.process;

import java.util.List;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MClient;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import tw.idempiere.clientinit.data.ForeignKeyAnalyzer;
import tw.idempiere.clientinit.data.ForeignKeyAnalyzer.TableInfo;

/**
 * 刪除整個 Tenant (Client)
 *
 * 功能：
 * 1. 按外鍵依賴順序刪除所有資料（包括交易資料和主資料）
 * 2. 最後刪除 Client 本身
 *
 * 警告：此操作不可逆，執行前請務必備份！
 *
 * @author iDempiere Community
 */
public class DeleteClientProcess extends SvrProcess {

    // 參數
    private int p_AD_Client_ID = 0;  // 目標 Client ID
    private boolean p_IsPreviewOnly = true;  // 預設為預覽模式，安全起見
    private boolean p_IsConfirmed = false;   // 二次確認

    @Override
    protected void prepare() {
        ProcessInfoParameter[] para = getParameter();
        for (ProcessInfoParameter p : para) {
            String name = p.getParameterName();
            if ("Target_AD_Client_ID".equals(name)) {
                p_AD_Client_ID = p.getParameterAsInt();
            } else if ("IsPreviewOnly".equals(name)) {
                p_IsPreviewOnly = p.getParameterAsBoolean();
            } else if ("IsConfirmed".equals(name)) {
                p_IsConfirmed = p.getParameterAsBoolean();
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
        if (p_AD_Client_ID <= 0) {
            throw new AdempiereException("請選擇要刪除的 Tenant (Client)");
        }

        // 安全檢查：禁止刪除 System Client
        if (p_AD_Client_ID == 0) {
            throw new AdempiereException("System Client (ID=0) 不允許被刪除");
        }

        // 取得 Client 資訊
        MClient client = MClient.get(getCtx(), p_AD_Client_ID);
        if (client == null || client.getAD_Client_ID() == 0) {
            throw new AdempiereException("找不到指定的 Client (ID=" + p_AD_Client_ID + ")");
        }

        String clientName = client.getName();

        addLog("========================================");
        addLog("     刪除 Tenant 作業");
        addLog("========================================");
        addLog("");
        addLog("目標 Tenant: " + clientName + " (ID=" + p_AD_Client_ID + ")");
        addLog("");

        if (p_IsPreviewOnly) {
            return doPreview(p_AD_Client_ID, clientName);
        }

        // 二次確認
        if (!p_IsConfirmed) {
            throw new AdempiereException("請勾選「確認刪除」以執行此不可逆的操作！");
        }

        return doDelete(p_AD_Client_ID, clientName);
    }

    /**
     * 執行預覽
     */
    private String doPreview(int clientId, String clientName) {
        addLog("【預覽模式】以下是將被刪除的資料統計：");
        addLog("----------------------------------------");

        List<TableInfo> tables = ForeignKeyAnalyzer.getDeleteOrderWithCount(clientId, get_TrxName());

        int totalRecords = 0;
        int tableCount = 0;

        for (TableInfo info : tables) {
            addLog("  " + info.tableName + ": " + info.recordCount + " 筆");
            totalRecords += info.recordCount;
            tableCount++;
        }

        addLog("");
        addLog("----------------------------------------");
        addLog("摘要：");
        addLog("  - 涉及表數量: " + tableCount);
        addLog("  - 總記錄數: " + totalRecords);
        addLog("----------------------------------------");
        addLog("");
        addLog("⚠️ 警告：此操作將永久刪除 Tenant「" + clientName + "」的所有資料！");
        addLog("⚠️ 執行前請務必備份資料庫！");
        addLog("");
        addLog("如要執行刪除，請：");
        addLog("1. 取消勾選「僅預覽」");
        addLog("2. 勾選「確認刪除」");

        return "預覽完成：共 " + tableCount + " 個表，" + totalRecords + " 筆記錄將被刪除";
    }

    /**
     * 執行刪除
     */
    private String doDelete(int clientId, String clientName) throws Exception {
        addLog("【執行刪除】開始刪除 Tenant: " + clientName);
        addLog("----------------------------------------");

        // 取得刪除順序
        List<String> deleteOrder = ForeignKeyAnalyzer.getDeleteOrder(clientId, get_TrxName());

        int totalDeleted = 0;
        int tableCount = 0;
        int errorCount = 0;

        // 按順序刪除每個表
        for (String tableName : deleteOrder) {
            // 先刪除對應的翻譯表（_Trl 後綴）
            String trlTableName = tableName + "_Trl";
            int trlDeleted = deleteTrlTable(trlTableName, tableName, clientId);
            if (trlDeleted > 0) {
                addLog("  " + trlTableName + ": 刪除 " + trlDeleted + " 筆");
                totalDeleted += trlDeleted;
                tableCount++;
            }

            // 刪除主表
            try {
                String sql = "DELETE FROM " + tableName + " WHERE AD_Client_ID = ?";
                int deleted = DB.executeUpdateEx(sql, new Object[]{clientId}, get_TrxName());

                if (deleted > 0) {
                    addLog("  " + tableName + ": 刪除 " + deleted + " 筆");
                    totalDeleted += deleted;
                    tableCount++;
                }
            } catch (Exception e) {
                // 記錄錯誤但繼續處理其他表
                addLog("  ⚠️ " + tableName + ": 刪除失敗 - " + e.getMessage());
                log.log(Level.WARNING, "Failed to delete from " + tableName, e);
                errorCount++;
            }

            // 每處理 10 個表就 commit 一次，避免長事務
            if (tableCount % 10 == 0) {
                commitEx();
            }
        }

        // 強制刪除核心表（確保這些表一定會被處理）
        addLog("");
        addLog("--- 刪除核心資料 ---");
        String[] coreTables = {
            // 用戶相關（按依賴順序）
            "AD_User_Roles",
            "AD_User_OrgAccess",
            "AD_UserBPAccess",
            "AD_UserPreference",
            "AD_User_Substitute",
            "AD_User",
            // 組織相關
            "AD_Role_OrgAccess",
            "AD_OrgInfo",
            "AD_Org",
            // 角色相關
            "AD_Window_Access",
            "AD_Process_Access",
            "AD_Form_Access",
            "AD_Workflow_Access",
            "AD_Task_Access",
            "AD_Document_Action_Access",
            "AD_InfoWindow_Access",
            "AD_Role_Included",
            "AD_Role",
            // 樹節點
            "AD_TreeNodeBP",
            "AD_TreeNodePR",
            "AD_TreeNodeMM",
            "AD_TreeNodeCMC",
            "AD_TreeNodeCMS",
            "AD_TreeNode",
            "AD_Tree",
            // 會計架構
            "C_AcctSchema_GL",
            "C_AcctSchema_Default",
            "C_AcctSchema_Element",
            "C_AcctSchema"
        };

        for (String table : coreTables) {
            try {
                // 先刪除翻譯表
                deleteTrlTable(table + "_Trl", table, clientId);
                // 刪除主表
                String sql = "DELETE FROM " + table + " WHERE AD_Client_ID = ?";
                int deleted = DB.executeUpdateEx(sql, new Object[]{clientId}, get_TrxName());
                if (deleted > 0) {
                    addLog("  " + table + ": 刪除 " + deleted + " 筆");
                    totalDeleted += deleted;
                    tableCount++;
                }
            } catch (Exception e) {
                // 可能已被刪除或不存在，繼續
                log.fine("Core table " + table + ": " + e.getMessage());
            }
        }
        commitEx();

        // 最後刪除 Client 記錄本身
        addLog("");
        addLog("--- 刪除 Client 記錄 ---");

        try {
            // 刪除 AD_ClientInfo
            int deleted = DB.executeUpdateEx(
                "DELETE FROM AD_ClientInfo WHERE AD_Client_ID = ?",
                new Object[]{clientId}, get_TrxName());
            if (deleted > 0) {
                addLog("  AD_ClientInfo: 刪除 " + deleted + " 筆");
            }

            // 刪除 AD_Client
            deleted = DB.executeUpdateEx(
                "DELETE FROM AD_Client WHERE AD_Client_ID = ?",
                new Object[]{clientId}, get_TrxName());
            if (deleted > 0) {
                addLog("  AD_Client: 刪除 " + deleted + " 筆");
            }

            commitEx();
        } catch (Exception e) {
            addLog("  ⚠️ 刪除 Client 記錄失敗: " + e.getMessage());
            log.log(Level.SEVERE, "Failed to delete AD_Client", e);
            throw new AdempiereException("無法刪除 Client 記錄: " + e.getMessage());
        }

        addLog("");
        addLog("========================================");
        addLog("刪除完成！");
        addLog("  - 已刪除表數量: " + tableCount);
        addLog("  - 已刪除記錄數: " + totalDeleted);
        if (errorCount > 0) {
            addLog("  - 錯誤數量: " + errorCount);
        }
        addLog("========================================");

        return "Tenant「" + clientName + "」已刪除，共 " + totalDeleted + " 筆記錄";
    }

    /**
     * 檢查表是否存在
     * @param tableName 表名稱
     * @return 表是否存在
     */
    private boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM AD_Table WHERE TableName = ? AND IsActive = 'Y'";
        int count = DB.getSQLValueEx(get_TrxName(), sql, tableName);
        return count > 0;
    }

    /**
     * 刪除翻譯表記錄
     * 翻譯表可能沒有 AD_Client_ID，需要通過主表的 ID 關聯刪除
     *
     * @param trlTableName 翻譯表名稱
     * @param mainTableName 主表名稱
     * @param clientId Client ID
     * @return 刪除的記錄數
     */
    private int deleteTrlTable(String trlTableName, String mainTableName, int clientId) {
        if (!tableExists(trlTableName)) {
            return 0;
        }

        try {
            // 先嘗試直接用 AD_Client_ID 刪除
            String sql = "DELETE FROM " + trlTableName + " WHERE AD_Client_ID = ?";
            return DB.executeUpdateEx(sql, new Object[]{clientId}, get_TrxName());
        } catch (Exception e1) {
            // 如果沒有 AD_Client_ID，嘗試通過主表 ID 關聯刪除
            try {
                String pkColumn = mainTableName + "_ID";
                String sql = "DELETE FROM " + trlTableName +
                             " WHERE " + pkColumn + " IN (SELECT " + pkColumn +
                             " FROM " + mainTableName + " WHERE AD_Client_ID = ?)";
                return DB.executeUpdateEx(sql, new Object[]{clientId}, get_TrxName());
            } catch (Exception e2) {
                log.fine("Cannot delete Trl table: " + trlTableName + " - " + e2.getMessage());
                return 0;
            }
        }
    }
}

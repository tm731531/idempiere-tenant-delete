package tw.idempiere.clientinit.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 交易表定義
 *
 * 定義哪些表屬於交易表（需要刪除），哪些是主資料表（需要保留）。
 * 使用兩層判斷：
 * 1. 明確列出的交易表
 * 2. 基於表名模式的判斷（如包含 Line, MA, Tax 等後綴）
 *
 * @author iDempiere Community
 */
public class TransactionTables {

    /**
     * 明確的交易表清單
     * 這些表的資料會在初始化時被刪除
     */
    public static final Set<String> TRANSACTION_TABLES = new HashSet<>(Arrays.asList(
        // ========== 會計分錄 ==========
        "Fact_Acct",
        "Fact_Acct_Summary",
        "Fact_Reconciliation",

        // ========== 付款相關 ==========
        "C_PaymentAllocate",
        "C_AllocationLine",
        "C_AllocationHdr",
        "C_PaySelectionCheck",
        "C_PaySelectionLine",
        "C_PaySelection",
        "C_Payment",
        "C_PaymentBatch",
        "C_PaymentTransaction",

        // ========== 銀行對帳 ==========
        "C_BankStatementLine",
        "C_BankStatement",

        // ========== 發票相關 ==========
        "C_InvoiceTax",
        "C_InvoicePaySchedule",
        "C_LandedCostAllocation",
        "C_InvoiceLine",
        "C_InvoiceBatchLine",
        "C_Invoice",
        "C_InvoiceBatch",
        "C_LandedCost",

        // ========== 出貨相關 ==========
        "M_InOutLineMA",
        "M_InOutLineConfirm",
        "M_InOutConfirm",
        "M_PackageLine",
        "M_InOutLine",
        "M_Package",
        "M_PackageMPS",
        "M_InOut",

        // ========== 訂單相關 ==========
        "M_MatchInv",
        "M_MatchPO",
        "C_OrderTax",
        "C_OrderPaySchedule",
        "C_OrderLandedCostAllocation",
        "C_OrderLine",
        "C_OrderLandedCost",
        "C_POSPayment",
        "C_Order",
        "C_POSOrder",

        // ========== 請購單 ==========
        "M_RequisitionLine",
        "M_Requisition",

        // ========== RMA ==========
        "M_RMATax",
        "M_RMALine",
        "M_RMA",

        // ========== 庫存異動 ==========
        "M_TransactionAllocation",
        "M_Transaction",
        "M_StorageOnHand",
        "M_StorageReservation",
        "M_StorageDetail",

        // ========== 盤點 ==========
        "M_InventoryLineMA",
        "M_InventoryLine",
        "M_Inventory",

        // ========== 調撥 ==========
        "M_MovementLineMA",
        "M_MovementLineConfirm",
        "M_MovementConfirm",
        "M_MovementLine",
        "M_Movement",

        // ========== 成本 ==========
        "M_CostQueue",
        "M_CostHistory",
        "M_CostDetail",
        "M_Cost",

        // ========== 生產 ==========
        "M_ProductionLineMA",
        "M_ProductionPlan",
        "M_ProductionLine",
        "M_Production",
        "PP_Order_BOM",
        "PP_Order_BOMLine",
        "PP_Order_BOMLineMA",
        "PP_Order_Cost",
        "PP_Order_Node",
        "PP_Order_NodeAsset",
        "PP_Order_NodeProduct",
        "PP_Order_Workflow",
        "PP_Order",
        "PP_Cost_Collector",
        "PP_Cost_CollectorMA",
        "PP_MRP",

        // ========== 品質管理 ==========
        "M_QualityTestResult",
        "M_QualityTest",

        // ========== 日記帳 ==========
        "GL_JournalLine",
        "GL_Journal",
        "GL_JournalBatch",

        // ========== 專案 ==========
        "C_ProjectIssueMA",
        "C_ProjectIssue",
        "C_ProjectLine",
        "C_ProjectPhase",
        "C_ProjectTask",

        // ========== 佣金 ==========
        "C_CommissionAmt",
        "C_CommissionDetail",
        "C_CommissionRun",
        "C_CommissionLine",

        // ========== 費用報告 ==========
        "S_TimeExpenseLine",
        "S_TimeExpense",

        // ========== 資產 ==========
        "A_Depreciation_Entry",
        "A_Depreciation_Exp",
        "A_Asset_Acct",
        "A_Asset_Addition",
        "A_Asset_Change",
        "A_Asset_Disposed",
        "A_Asset_Info_Fin",
        "A_Asset_Info_Ins",
        "A_Asset_Info_Lic",
        "A_Asset_Info_Oth",
        "A_Asset_Info_Tax",
        "A_Asset_Reval_Entry",
        "A_Asset_Reval_Index",
        "A_Asset_Split",
        "A_Asset_Transfer",
        "A_Asset_Use",

        // ========== 服務相關 ==========
        "S_ResourceAssignment",

        // ========== 請求/CRM ==========
        "R_RequestUpdate",
        "R_RequestAction",
        "R_RequestUpdates",
        "R_Request",
        "C_ContactActivity",
        "C_Opportunity",

        // ========== 週期性 ==========
        "C_Recurring_Run",
        "C_Recurring",

        // ========== 預算 ==========
        "GL_BudgetControl",

        // ========== 稅務 ==========
        "C_TaxDeclarationLine",
        "C_TaxDeclarationAcct",
        "C_TaxDeclaration",

        // ========== 現金日記帳 ==========
        "C_CashLine",
        "C_Cash",

        // ========== 工作流相關 ==========
        "AD_WF_Activity",
        "AD_WF_ActivityResult",
        "AD_WF_EventAudit",
        "AD_WF_Process",

        // ========== Process 執行記錄 ==========
        "AD_PInstance_Para",
        "AD_PInstance_Log",
        "AD_PInstance",

        // ========== 變更記錄 ==========
        "AD_ChangeLog",

        // ========== 附件和筆記 ==========
        "AD_Attachment",
        "AD_AttachmentNote",
        "AD_Note",

        // ========== 其他交易表 ==========
        "C_DunningRunLine",
        "C_DunningRunEntry",
        "C_DunningRun",
        "DD_OrderLine",
        "DD_Order",
        "I_BPartner",
        "I_Product",
        "I_Order",
        "I_Invoice",
        "I_Payment",
        "I_BankStatement",
        "I_GLJournal",
        "I_Inventory",
        "I_Movement",
        "I_InOutLineConfirm",
        "I_FAJournal",
        "I_Conversion_Rate",
        "I_PriceList",
        "I_ProductPlanning",
        "I_ReportLine",
        "I_FixedAsset"
    ));

    /**
     * 暫存表清單
     * 這些表會在刪除交易前先清理
     */
    public static final Set<String> TEMP_TABLES = new HashSet<>(Arrays.asList(
        "T_Selection",
        "T_Selection2",
        "T_Transaction",
        "T_TrialBalance",
        "T_Aging",
        "T_InvoiceGL",
        "T_Replenish",
        "T_Report",
        "T_ReportStatement",
        "T_Spool"
    ));

    /**
     * 明確不是交易表的表（主資料、設定、系統表）
     */
    private static final Set<String> NON_TRANSACTION_TABLES = new HashSet<>(Arrays.asList(
        // 系統設定表
        "AD_Role",
        "AD_User",
        "AD_Org",
        "AD_Tree",
        "AD_TreeNode",
        "AD_TreeNodeBP",
        "AD_TreeNodePR",
        "AD_TreeNodeMM",
        "AD_TreeNodeCMC",
        "AD_TreeNodeCMS",
        "AD_TreeNodeCMM",
        "AD_TreeNodeCMT",
        "AD_TreeNodeU1",
        "AD_TreeNodeU2",
        "AD_TreeNodeU3",
        "AD_TreeNodeU4",
        "AD_Sequence",
        "AD_Preference",
        "AD_UserPreference",
        "AD_PrintForm",
        "AD_PrintFormatItem",
        "AD_OrgInfo",
        "AD_ClientInfo",
        "AD_Session",  // 保留 Session，不刪除

        // 權限設定表
        "AD_Role_OrgAccess",
        "AD_User_Roles",
        "AD_Window_Access",
        "AD_Process_Access",
        "AD_Form_Access",
        "AD_Workflow_Access",
        "AD_InfoWindow_Access",
        "AD_Document_Action_Access",
        "AD_Task_Access",
        "AD_UserBPAccess",

        // 會計設定表（*_Acct 是設定，不是交易）
        "C_AcctSchema_GL",
        "C_AcctSchema_Default",
        "C_AcctSchema_Element",
        "C_BP_Group_Acct",
        "C_BP_Customer_Acct",
        "C_BP_Vendor_Acct",
        "C_BP_Employee_Acct",
        "C_BankAccount_Acct",
        "C_CashBook_Acct",
        "C_Charge_Acct",
        "C_Tax_Acct",
        "C_Withholding_Acct",
        "C_Project_Acct",
        "M_Product_Acct",
        "M_Product_Category_Acct",
        "M_Warehouse_Acct",
        "A_Asset_Acct",

        // 稅務設定表
        "C_Tax",
        "C_TaxCategory",
        "C_TaxPostal",
        "C_TaxProvider",

        // 單據類型設定
        "C_DocType",
        "C_DocTypeCounter",

        // 產品主檔
        "M_Product",
        "M_Product_Category",
        "M_Product_PO",
        "M_ProductPrice",
        "M_PriceList",
        "M_PriceList_Version",
        "M_DiscountSchema",
        "M_DiscountSchemaLine",
        "M_DiscountSchemaBreak",

        // 業務夥伴主檔
        "C_BPartner",
        "C_BPartner_Location",
        "C_BP_Group",
        "C_BP_BankAccount",

        // 銀行設定
        "C_Bank",
        "C_BankAccount",

        // 會計元素
        "C_Element",
        "C_ElementValue",
        "C_ValidCombination",

        // 其他主資料
        "C_Activity",
        "C_Campaign",
        "C_SalesRegion",
        "C_Project",
        "C_CashBook",
        "C_Cycle",
        "C_Period",
        "C_PeriodControl",
        "C_Year",
        "C_Calendar",
        "GL_Category",
        "M_CostElement",
        "M_Locator",
        "M_Warehouse",
        "C_AcctProcessor",
        "R_RequestProcessor",
        "PP_Product_BOM",
        "PP_Product_BOMLine",

        // 翻譯表
        "C_Tax_Trl",
        "C_TaxCategory_Trl",
        "C_DocType_Trl",
        "C_PaymentTerm_Trl",
        "C_Campaign_Trl",
        "C_SalesRegion_Trl",
        "C_ElementValue_Trl",
        "GL_Category_Trl",
        "M_PriceList_Trl",
        "M_PriceList_Version_Trl",
        "PP_Product_BOM_Trl",
        "PP_Product_BOMLine_Trl"
    ));

    /**
     * 檢查表是否為交易表
     * 採用白名單機制：只有明確列出的交易表才會被刪除
     *
     * @param tableName 表名稱
     * @return 是否為交易表
     */
    public static boolean isTransactionTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }

        // 1. 明確列出的非交易表，直接返回 false
        if (NON_TRANSACTION_TABLES.contains(tableName)) {
            return false;
        }

        // 2. 明確列出的交易表
        if (TRANSACTION_TABLES.contains(tableName)) {
            return true;
        }

        // 3. 匯入表（I_ 開頭）視為交易表
        if (tableName.startsWith("I_")) {
            return true;
        }

        // 4. 暫存表（T_ 開頭）視為交易表
        if (tableName.startsWith("T_")) {
            return true;
        }

        // 5. 預設：不在明確清單中的表，不刪除（安全起見）
        return false;
    }

    /**
     * 檢查表是否為暫存表
     * @param tableName 表名稱
     * @return 是否為暫存表
     */
    public static boolean isTempTable(String tableName) {
        if (tableName == null) {
            return false;
        }
        return TEMP_TABLES.contains(tableName) || tableName.toUpperCase().startsWith("T_");
    }

    /**
     * 檢查表是否為主資料表
     * @param tableName 表名稱
     * @return 是否為主資料表
     */
    public static boolean isMasterDataTable(String tableName) {
        return !isTransactionTable(tableName) && !isTempTable(tableName);
    }
}

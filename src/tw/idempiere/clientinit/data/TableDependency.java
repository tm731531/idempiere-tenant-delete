package tw.idempiere.clientinit.data;

/**
 * 表依賴關係定義
 *
 * 定義表格的刪除順序，按照外鍵依賴關係排列。
 * 刪除順序設計原則：
 * 1. 先刪除最底層（被引用最少）的表
 * 2. 再刪除引用它們的表
 * 3. 最後刪除頂層表
 *
 * @author iDempiere Community
 */
public class TableDependency {

    /**
     * 刪除順序
     * 每個陣列元素代表一個 Phase，同一個 Phase 內的表可以平行刪除
     */
    public static final String[][] DELETE_ORDER = {
        // Phase 1: 會計分錄 (最底層，被所有單據引用)
        {"Fact_Acct", "Fact_Acct_Summary", "Fact_Reconciliation"},

        // Phase 2: 付款分攤
        {"C_PaymentAllocate", "C_AllocationLine"},

        // Phase 3: 分攤表頭
        {"C_AllocationHdr"},

        // Phase 4: 付款選擇
        {"C_PaySelectionCheck", "C_PaySelectionLine"},

        // Phase 5: 付款選擇表頭
        {"C_PaySelection"},

        // Phase 6: 付款
        {"C_Payment", "C_PaymentBatch", "C_PaymentTransaction"},

        // Phase 7: 銀行對帳明細
        {"C_BankStatementLine"},

        // Phase 8: 銀行對帳表頭
        {"C_BankStatement"},

        // Phase 9: 發票稅額和付款排程
        {"C_InvoiceTax", "C_InvoicePaySchedule"},

        // Phase 10: 進口費用分攤
        {"C_LandedCostAllocation"},

        // Phase 11: 發票明細
        {"C_InvoiceLine", "C_InvoiceBatchLine"},

        // Phase 12: 發票表頭
        {"C_Invoice", "C_InvoiceBatch", "C_LandedCost"},

        // Phase 13: 出貨明細 MA 和確認
        {"M_InOutLineMA", "M_InOutLineConfirm"},

        // Phase 14: 出貨確認
        {"M_InOutConfirm"},

        // Phase 15: 包裝明細
        {"M_PackageLine"},

        // Phase 16: 出貨明細和包裝
        {"M_InOutLine", "M_Package", "M_PackageMPS"},

        // Phase 17: 出貨表頭
        {"M_InOut"},

        // Phase 18: 發票/訂單配對
        {"M_MatchInv", "M_MatchPO"},

        // Phase 19: 訂單稅額和付款排程
        {"C_OrderTax", "C_OrderPaySchedule"},

        // Phase 20: 訂單進口費用分攤
        {"C_OrderLandedCostAllocation"},

        // Phase 21: 訂單明細
        {"C_OrderLine", "C_OrderLandedCost", "C_POSPayment"},

        // Phase 22: 訂單表頭
        {"C_Order"},

        // Phase 23: 請購單明細
        {"M_RequisitionLine"},

        // Phase 24: 請購單表頭
        {"M_Requisition"},

        // Phase 25: RMA 稅額和明細
        {"M_RMATax", "M_RMALine"},

        // Phase 26: RMA 表頭
        {"M_RMA"},

        // Phase 27: 庫存異動分攤
        {"M_TransactionAllocation"},

        // Phase 28: 庫存異動
        {"M_Transaction"},

        // Phase 29: 庫存現有量和保留
        {"M_StorageOnHand", "M_StorageReservation"},

        // Phase 30: 盤點明細 MA
        {"M_InventoryLineMA"},

        // Phase 31: 盤點明細
        {"M_InventoryLine"},

        // Phase 32: 盤點表頭
        {"M_Inventory"},

        // Phase 33: 調撥明細 MA 和確認
        {"M_MovementLineMA", "M_MovementLineConfirm"},

        // Phase 34: 調撥確認
        {"M_MovementConfirm"},

        // Phase 35: 調撥明細
        {"M_MovementLine"},

        // Phase 36: 調撥表頭
        {"M_Movement"},

        // Phase 37: 成本佇列和歷史
        {"M_CostQueue", "M_CostHistory"},

        // Phase 38: 成本明細
        {"M_CostDetail"},

        // Phase 39: 成本
        {"M_Cost"},

        // Phase 40: 生產明細 MA 和品質測試
        {"M_ProductionLineMA", "M_QualityTestResult"},

        // Phase 41: 生產明細
        {"M_ProductionLine"},

        // Phase 42: 生產表頭
        {"M_Production"},

        // Phase 43: 日記帳明細
        {"GL_JournalLine"},

        // Phase 44: 日記帳
        {"GL_Journal"},

        // Phase 45: 日記帳批次
        {"GL_JournalBatch"},

        // Phase 46: 專案發料和明細
        {"C_ProjectIssueMA", "C_ProjectLine"},

        // Phase 47: 佣金金額和明細
        {"C_CommissionAmt", "C_CommissionDetail"},

        // Phase 48: 佣金執行
        {"C_CommissionRun"},

        // Phase 49: 費用報告明細
        {"S_TimeExpenseLine"},

        // Phase 50: 費用報告表頭
        {"S_TimeExpense"},

        // Phase 51: 請求更新和動作
        {"R_RequestUpdate", "R_RequestAction", "R_RequestUpdates"},

        // Phase 52: 請求
        {"R_Request"},

        // Phase 53: 週期性執行
        {"C_Recurring_Run"},

        // Phase 54: 週期性
        {"C_Recurring"},

        // Phase 55: 聯絡活動
        {"C_ContactActivity"},

        // Phase 56: 商機
        {"C_Opportunity"}
    };

    /**
     * 取得刪除順序的總 Phase 數
     * @return Phase 數量
     */
    public static int getPhaseCount() {
        return DELETE_ORDER.length;
    }

    /**
     * 取得指定 Phase 的表清單
     * @param phaseIndex Phase 索引（從 0 開始）
     * @return 表名稱陣列
     */
    public static String[] getPhase(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex >= DELETE_ORDER.length) {
            return new String[0];
        }
        return DELETE_ORDER[phaseIndex];
    }
}

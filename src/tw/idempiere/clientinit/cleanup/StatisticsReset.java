package tw.idempiere.clientinit.cleanup;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.compiere.util.DB;

/**
 * 統計值重置
 *
 * 負責重置各種累計統計值，例如客戶/供應商的未結金額等。
 *
 * @author iDempiere Community
 */
public class StatisticsReset {

    private static final Logger log = Logger.getLogger(StatisticsReset.class.getName());

    /**
     * 重置所有統計值
     * @param clientId Client ID
     * @param trxName 交易名稱
     */
    public static void resetAll(int clientId, String trxName) {
        resetBPartnerStatistics(clientId, trxName);
        resetProductStatistics(clientId, trxName);
        resetBankAccountBalance(clientId, trxName);
        resetProjectStatistics(clientId, trxName);
    }

    /**
     * 重置業務夥伴統計值
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 更新的筆數
     */
    public static int resetBPartnerStatistics(int clientId, String trxName) {
        int count = 0;

        // 重置 C_BPartner 的統計欄位
        String sql = "UPDATE C_BPartner SET " +
                     "TotalOpenBalance = 0, " +
                     "SO_CreditUsed = 0, " +
                     "ActualLifeTimeValue = 0, " +
                     "Updated = getDate(), " +
                     "UpdatedBy = 0 " +
                     "WHERE AD_Client_ID = ?";

        try {
            count = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
            log.info("Reset BPartner statistics: " + count + " records");
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to reset BPartner statistics", e);
        }

        return count;
    }

    /**
     * 重置產品統計值
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 更新的筆數
     */
    public static int resetProductStatistics(int clientId, String trxName) {
        int count = 0;

        // 重置 M_Product_PO 的統計欄位
        String sql = "UPDATE M_Product_PO SET " +
                     "PriceLastPO = 0, " +
                     "PriceLastInv = 0, " +
                     "Updated = getDate(), " +
                     "UpdatedBy = 0 " +
                     "WHERE AD_Client_ID = ?";

        try {
            count = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
            log.info("Reset Product PO statistics: " + count + " records");
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to reset Product PO statistics", e);
        }

        return count;
    }

    /**
     * 重置銀行帳戶餘額
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 更新的筆數
     */
    public static int resetBankAccountBalance(int clientId, String trxName) {
        int count = 0;

        // 重置 C_BankAccount 的餘額
        String sql = "UPDATE C_BankAccount SET " +
                     "CurrentBalance = 0, " +
                     "Updated = getDate(), " +
                     "UpdatedBy = 0 " +
                     "WHERE AD_Client_ID = ?";

        try {
            count = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
            log.info("Reset Bank Account balance: " + count + " records");
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to reset Bank Account balance", e);
        }

        return count;
    }

    /**
     * 重置專案統計值
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 更新的筆數
     */
    public static int resetProjectStatistics(int clientId, String trxName) {
        int count = 0;

        // 重置 C_Project 的統計欄位
        String sql = "UPDATE C_Project SET " +
                     "InvoicedAmt = 0, " +
                     "ProjectBalanceAmt = 0, " +
                     "CommittedAmt = 0, " +
                     "CommittedQty = 0, " +
                     "Updated = getDate(), " +
                     "UpdatedBy = 0 " +
                     "WHERE AD_Client_ID = ?";

        try {
            count = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
            log.info("Reset Project statistics: " + count + " records");
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to reset Project statistics", e);
        }

        return count;
    }

    /**
     * 取得統計值摘要
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 摘要字串
     */
    public static String getStatisticsSummary(int clientId, String trxName) {
        StringBuilder sb = new StringBuilder();

        // 業務夥伴有未結金額的數量
        String sql = "SELECT COUNT(*) FROM C_BPartner " +
                     "WHERE AD_Client_ID = ? AND TotalOpenBalance <> 0";
        int bpCount = DB.getSQLValueEx(trxName, sql, clientId);
        sb.append("BPartner with open balance: ").append(bpCount).append("\n");

        // 銀行帳戶有餘額的數量
        sql = "SELECT COUNT(*) FROM C_BankAccount " +
              "WHERE AD_Client_ID = ? AND CurrentBalance <> 0";
        int baCount = DB.getSQLValueEx(trxName, sql, clientId);
        sb.append("Bank Account with balance: ").append(baCount).append("\n");

        // 專案有金額的數量
        sql = "SELECT COUNT(*) FROM C_Project " +
              "WHERE AD_Client_ID = ? AND (InvoicedAmt <> 0 OR ProjectBalanceAmt <> 0)";
        int projCount = DB.getSQLValueEx(trxName, sql, clientId);
        sb.append("Project with amounts: ").append(projCount).append("\n");

        return sb.toString();
    }
}

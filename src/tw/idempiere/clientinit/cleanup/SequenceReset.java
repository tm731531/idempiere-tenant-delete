package tw.idempiere.clientinit.cleanup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.compiere.util.DB;

/**
 * 序號重置
 *
 * 負責重置文件序號，讓序號從初始值重新開始。
 *
 * @author iDempiere Community
 */
public class SequenceReset {

    private static final Logger log = Logger.getLogger(SequenceReset.class.getName());

    /**
     * 重置所有文件序號
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 重置的序號數量
     */
    public static int resetDocumentSequences(int clientId, String trxName) {
        int count = 0;

        // 重置 AD_Sequence 的 CurrentNext 到 StartNo
        String sql = "UPDATE AD_Sequence " +
                     "SET CurrentNext = StartNo, " +
                     "    CurrentNextSys = StartNo, " +
                     "    Updated = getDate(), " +
                     "    UpdatedBy = 0 " +
                     "WHERE AD_Client_ID = ? " +
                     "AND IsTableID = 'N' " +
                     "AND IsAutoSequence = 'Y'";

        try {
            count = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
            log.info("Reset " + count + " document sequences for client " + clientId);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to reset document sequences", e);
        }

        return count;
    }

    /**
     * 重置指定表的序號
     * @param tableName 表名稱
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 是否成功
     */
    public static boolean resetTableSequence(String tableName, int clientId, String trxName) {
        String sql = "UPDATE AD_Sequence " +
                     "SET CurrentNext = StartNo, " +
                     "    Updated = getDate(), " +
                     "    UpdatedBy = 0 " +
                     "WHERE AD_Client_ID = ? " +
                     "AND Name = ?";

        try {
            int updated = DB.executeUpdateEx(sql, new Object[]{clientId, tableName}, trxName);
            return updated > 0;
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to reset sequence for table: " + tableName, e);
            return false;
        }
    }

    /**
     * 取得目前的序號資訊
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 序號資訊字串
     */
    public static String getSequenceInfo(int clientId, String trxName) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT Name, CurrentNext, StartNo " +
                     "FROM AD_Sequence " +
                     "WHERE AD_Client_ID = ? " +
                     "AND IsTableID = 'N' " +
                     "AND IsAutoSequence = 'Y' " +
                     "AND CurrentNext > StartNo " +
                     "ORDER BY Name";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = DB.prepareStatement(sql, trxName);
            pstmt.setInt(1, clientId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("Name"))
                  .append(": ")
                  .append(rs.getInt("CurrentNext"))
                  .append(" -> ")
                  .append(rs.getInt("StartNo"))
                  .append("\n");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to get sequence info", e);
        } finally {
            DB.close(rs, pstmt);
        }

        return sb.toString();
    }
}

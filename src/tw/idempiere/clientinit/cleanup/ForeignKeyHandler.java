package tw.idempiere.clientinit.cleanup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.compiere.util.DB;

/**
 * 外鍵處理器
 *
 * 提供外鍵相關的檢查和處理功能。
 * 注意：此 Plugin 設計為不禁用約束，而是按照正確的順序刪除資料。
 *
 * @author iDempiere Community
 */
public class ForeignKeyHandler {

    private static final Logger log = Logger.getLogger(ForeignKeyHandler.class.getName());

    /**
     * 取得引用指定表的所有外鍵
     * @param tableName 被引用的表名稱
     * @return 引用此表的外鍵清單 (格式: table.column)
     */
    public static List<String> getReferencingTables(String tableName) {
        List<String> result = new ArrayList<>();

        // PostgreSQL 查詢外鍵關係
        String sql = "SELECT DISTINCT " +
                     "tc.table_name, kcu.column_name " +
                     "FROM information_schema.table_constraints tc " +
                     "JOIN information_schema.key_column_usage kcu " +
                     "  ON tc.constraint_name = kcu.constraint_name " +
                     "JOIN information_schema.constraint_column_usage ccu " +
                     "  ON ccu.constraint_name = tc.constraint_name " +
                     "WHERE tc.constraint_type = 'FOREIGN KEY' " +
                     "AND UPPER(ccu.table_name) = UPPER(?)";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = DB.prepareStatement(sql, null);
            pstmt.setString(1, tableName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1) + "." + rs.getString(2));
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to get referencing tables for: " + tableName, e);
        } finally {
            DB.close(rs, pstmt);
        }

        return result;
    }

    /**
     * 取得指定表引用的所有外鍵
     * @param tableName 表名稱
     * @return 此表引用的外鍵清單 (格式: referenced_table.column)
     */
    public static List<String> getReferencedTables(String tableName) {
        List<String> result = new ArrayList<>();

        // PostgreSQL 查詢外鍵關係
        String sql = "SELECT DISTINCT " +
                     "ccu.table_name, ccu.column_name " +
                     "FROM information_schema.table_constraints tc " +
                     "JOIN information_schema.key_column_usage kcu " +
                     "  ON tc.constraint_name = kcu.constraint_name " +
                     "JOIN information_schema.constraint_column_usage ccu " +
                     "  ON ccu.constraint_name = tc.constraint_name " +
                     "WHERE tc.constraint_type = 'FOREIGN KEY' " +
                     "AND UPPER(tc.table_name) = UPPER(?)";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = DB.prepareStatement(sql, null);
            pstmt.setString(1, tableName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1) + "." + rs.getString(2));
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to get referenced tables for: " + tableName, e);
        } finally {
            DB.close(rs, pstmt);
        }

        return result;
    }

    /**
     * 檢查是否可以安全刪除表的資料
     * @param tableName 表名稱
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 是否可以安全刪除
     */
    public static boolean canSafelyDelete(String tableName, int clientId, String trxName) {
        List<String> referencingTables = getReferencingTables(tableName);

        for (String ref : referencingTables) {
            String[] parts = ref.split("\\.");
            String refTable = parts[0];

            // 檢查引用表中是否有此 Client 的資料
            String sql = "SELECT COUNT(*) FROM " + refTable + " WHERE AD_Client_ID = ?";
            try {
                int count = DB.getSQLValueEx(trxName, sql, clientId);
                if (count > 0) {
                    log.warning("Cannot delete " + tableName +
                               " - referenced by " + refTable + " (" + count + " records)");
                    return false;
                }
            } catch (Exception e) {
                // 如果查詢失敗，假設不安全
                log.log(Level.WARNING, "Failed to check reference: " + ref, e);
                return false;
            }
        }

        return true;
    }

    /**
     * 取得刪除順序驗證報告
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 驗證報告
     */
    public static String getDeleteOrderValidation(int clientId, String trxName) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 刪除順序驗證報告 ===\n");

        // 使用 TableDependency 的順序進行驗證
        for (String[] phase : tw.idempiere.clientinit.data.TableDependency.DELETE_ORDER) {
            for (String tableName : phase) {
                List<String> refs = getReferencingTables(tableName);
                if (!refs.isEmpty()) {
                    sb.append(tableName).append(" 被引用: ").append(refs).append("\n");
                }
            }
        }

        return sb.toString();
    }
}

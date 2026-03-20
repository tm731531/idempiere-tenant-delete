package tw.idempiere.clientinit.cleanup;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.compiere.util.DB;

import tw.idempiere.clientinit.data.TransactionTables;

/**
 * 交易資料清理
 *
 * 負責清理交易表和暫存表的資料。
 *
 * @author iDempiere Community
 */
public class TransactionCleanup {

    private static final Logger log = Logger.getLogger(TransactionCleanup.class.getName());

    /**
     * 清理暫存表
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 清理的總筆數
     */
    public static int cleanTempTables(int clientId, String trxName) {
        int totalDeleted = 0;

        for (String tableName : TransactionTables.TEMP_TABLES) {
            try {
                // 暫存表可能沒有 AD_Client_ID 欄位，嘗試不同的刪除方式
                String sql;
                int deleted;

                // 先嘗試使用 AD_Client_ID 刪除
                if (hasColumn(tableName, "AD_Client_ID")) {
                    sql = "DELETE FROM " + tableName + " WHERE AD_Client_ID = ?";
                    deleted = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
                } else if (hasColumn(tableName, "AD_PInstance_ID")) {
                    // 使用 AD_PInstance_ID 關聯刪除
                    sql = "DELETE FROM " + tableName +
                          " WHERE AD_PInstance_ID IN (SELECT AD_PInstance_ID FROM AD_PInstance WHERE AD_Client_ID = ?)";
                    deleted = DB.executeUpdateEx(sql, new Object[]{clientId}, trxName);
                } else {
                    // 跳過沒有適當欄位的表
                    log.fine("Skip temp table (no client column): " + tableName);
                    continue;
                }

                if (deleted > 0) {
                    log.info("Cleaned temp table " + tableName + ": " + deleted + " records");
                    totalDeleted += deleted;
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to clean temp table: " + tableName, e);
            }
        }

        return totalDeleted;
    }

    /**
     * 刪除指定表的資料
     * @param tableName 表名稱
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 刪除的筆數
     */
    public static int deleteTable(String tableName, int clientId, String trxName) {
        return deleteTable(tableName, clientId, null, null, trxName);
    }

    /**
     * 刪除指定表的資料（支援日期範圍）
     * @param tableName 表名稱
     * @param clientId Client ID
     * @param dateFrom 開始日期（可為 null）
     * @param dateTo 結束日期（可為 null）
     * @param trxName 交易名稱
     * @return 刪除的筆數
     */
    public static int deleteTable(String tableName, int clientId,
                                   java.sql.Timestamp dateFrom, java.sql.Timestamp dateTo,
                                   String trxName) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName);
        sql.append(" WHERE AD_Client_ID = ?");

        // 加入日期篩選
        if (dateFrom != null) {
            sql.append(" AND Created >= ?");
        }
        if (dateTo != null) {
            sql.append(" AND Created <= ?");
        }

        try {
            Object[] params;
            if (dateFrom != null && dateTo != null) {
                params = new Object[]{clientId, dateFrom, dateTo};
            } else if (dateFrom != null) {
                params = new Object[]{clientId, dateFrom};
            } else if (dateTo != null) {
                params = new Object[]{clientId, dateTo};
            } else {
                params = new Object[]{clientId};
            }

            return DB.executeUpdateEx(sql.toString(), params, trxName);

        } catch (Exception e) {
            log.log(Level.WARNING, "Delete " + tableName + " failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 計算表中的記錄數
     * @param tableName 表名稱
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 記錄數
     */
    public static int countRecords(String tableName, int clientId, String trxName) {
        return countRecords(tableName, clientId, null, null, trxName);
    }

    /**
     * 計算表中的記錄數（支援日期範圍）
     * @param tableName 表名稱
     * @param clientId Client ID
     * @param dateFrom 開始日期（可為 null）
     * @param dateTo 結束日期（可為 null）
     * @param trxName 交易名稱
     * @return 記錄數
     */
    public static int countRecords(String tableName, int clientId,
                                    java.sql.Timestamp dateFrom, java.sql.Timestamp dateTo,
                                    String trxName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(tableName);
        sql.append(" WHERE AD_Client_ID = ?");

        if (dateFrom != null) {
            sql.append(" AND Created >= ?");
        }
        if (dateTo != null) {
            sql.append(" AND Created <= ?");
        }

        try {
            Object[] params;
            if (dateFrom != null && dateTo != null) {
                params = new Object[]{clientId, dateFrom, dateTo};
            } else if (dateFrom != null) {
                params = new Object[]{clientId, dateFrom};
            } else if (dateTo != null) {
                params = new Object[]{clientId, dateTo};
            } else {
                params = new Object[]{clientId};
            }

            return DB.getSQLValueEx(trxName, sql.toString(), params);

        } catch (Exception e) {
            log.log(Level.WARNING, "Count " + tableName + " failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 檢查表是否有指定欄位
     * @param tableName 表名稱
     * @param columnName 欄位名稱
     * @return 是否有該欄位
     */
    private static boolean hasColumn(String tableName, String columnName) {
        String sql = "SELECT COUNT(*) FROM AD_Column c " +
                     "JOIN AD_Table t ON c.AD_Table_ID = t.AD_Table_ID " +
                     "WHERE t.TableName = ? AND c.ColumnName = ?";
        int count = DB.getSQLValueEx(null, sql, tableName, columnName);
        return count > 0;
    }
}

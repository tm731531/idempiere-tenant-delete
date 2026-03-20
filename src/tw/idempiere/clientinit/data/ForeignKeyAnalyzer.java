package tw.idempiere.clientinit.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.compiere.util.DB;

/**
 * 外鍵依賴分析器
 *
 * 動態分析資料庫外鍵關係，計算正確的刪除順序。
 * 使用拓撲排序確保被引用的表最後刪除。
 *
 * @author iDempiere Community
 */
public class ForeignKeyAnalyzer {

    private static final Logger log = Logger.getLogger(ForeignKeyAnalyzer.class.getName());

    /**
     * 取得所有有 AD_Client_ID 欄位的表（排除系統表和視圖）
     * @param trxName 交易名稱
     * @return 表名稱清單
     */
    public static List<String> getAllClientTables(String trxName) {
        List<String> tables = new ArrayList<>();

        String sql = """
            SELECT DISTINCT t.TableName
            FROM AD_Table t
            JOIN AD_Column c ON t.AD_Table_ID = c.AD_Table_ID
            WHERE c.ColumnName = 'AD_Client_ID'
              AND t.IsView = 'N'
              AND t.IsActive = 'Y'
              AND t.TableName NOT LIKE 'AD_%Trl'
              AND t.TableName NOT LIKE '%_v'
              AND t.TableName NOT LIKE '%_vt'
            ORDER BY t.TableName
            """;

        try (PreparedStatement pstmt = DB.prepareStatement(sql, trxName);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tables.add(rs.getString("TableName"));
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to get client tables", e);
        }

        return tables;
    }

    /**
     * 取得指定 Client 有資料的表清單
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 表名稱和記錄數的 Map
     */
    public static Map<String, Integer> getTablesWithData(int clientId, String trxName) {
        Map<String, Integer> result = new LinkedHashMap<>();
        List<String> tables = getAllClientTables(trxName);

        for (String tableName : tables) {
            try {
                String countSql = "SELECT COUNT(*) FROM " + tableName + " WHERE AD_Client_ID = ?";
                int count = DB.getSQLValueEx(trxName, countSql, clientId);
                if (count > 0) {
                    result.put(tableName, count);
                }
            } catch (Exception e) {
                // 忽略不存在的表或權限問題
                log.fine("Skip table " + tableName + ": " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 分析外鍵依賴關係
     * @param tables 要分析的表清單
     * @param trxName 交易名稱
     * @return 依賴關係 Map（表 -> 被它引用的表集合）
     */
    public static Map<String, Set<String>> analyzeForeignKeys(Collection<String> tables, String trxName) {
        Map<String, Set<String>> dependencies = new HashMap<>();
        Set<String> tableSet = new HashSet<>(tables);

        // 初始化所有表
        for (String table : tables) {
            dependencies.put(table, new HashSet<>());
        }

        // 查詢外鍵關係（從 PostgreSQL 系統表）
        String sql = """
            SELECT
                tc.table_name AS from_table,
                ccu.table_name AS to_table
            FROM information_schema.table_constraints tc
            JOIN information_schema.constraint_column_usage ccu
                ON tc.constraint_name = ccu.constraint_name
                AND tc.table_schema = ccu.table_schema
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_schema = 'adempiere'
              AND UPPER(tc.table_name) IN (SELECT UPPER(x) FROM unnest(?::text[]) x)
              AND UPPER(ccu.table_name) IN (SELECT UPPER(x) FROM unnest(?::text[]) x)
              AND UPPER(tc.table_name) != UPPER(ccu.table_name)
            """;

        try {
            String[] tableArray = tables.toArray(new String[0]);
            try (PreparedStatement pstmt = DB.prepareStatement(sql, trxName)) {
                pstmt.setArray(1, pstmt.getConnection().createArrayOf("text", tableArray));
                pstmt.setArray(2, pstmt.getConnection().createArrayOf("text", tableArray));
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String fromTable = rs.getString("from_table").toUpperCase();
                        String toTable = rs.getString("to_table").toUpperCase();

                        // 找到原始大小寫的表名
                        String fromTableOrig = findOriginalCase(tables, fromTable);
                        String toTableOrig = findOriginalCase(tables, toTable);

                        if (fromTableOrig != null && toTableOrig != null) {
                            // fromTable 引用 toTable，所以 fromTable 必須先刪除
                            dependencies.get(fromTableOrig).add(toTableOrig);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to analyze foreign keys via PostgreSQL, trying AD_Column", e);
            // 回退到使用 AD_Column 分析
            analyzeForeignKeysViaADColumn(tables, dependencies, trxName);
        }

        return dependencies;
    }

    /**
     * 透過 AD_Column 分析外鍵關係（作為 PostgreSQL 系統表的備案）
     */
    private static void analyzeForeignKeysViaADColumn(Collection<String> tables,
                                                       Map<String, Set<String>> dependencies,
                                                       String trxName) {
        Set<String> tableSet = new HashSet<>();
        for (String t : tables) {
            tableSet.add(t.toUpperCase());
        }

        String sql = """
            SELECT t1.TableName AS from_table, t2.TableName AS to_table
            FROM AD_Column c
            JOIN AD_Table t1 ON c.AD_Table_ID = t1.AD_Table_ID
            JOIN AD_Table t2 ON c.AD_Reference_Value_ID = t2.AD_Table_ID
               OR (c.ColumnName LIKE '%_ID' AND c.AD_Reference_ID IN (18, 19, 30)
                   AND EXISTS (SELECT 1 FROM AD_Table t3
                              WHERE t3.TableName || '_ID' = c.ColumnName
                              AND t3.AD_Table_ID = t2.AD_Table_ID))
            WHERE t1.IsActive = 'Y'
              AND t2.IsActive = 'Y'
              AND t1.TableName != t2.TableName
            """;

        try (PreparedStatement pstmt = DB.prepareStatement(sql, trxName);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String fromTable = rs.getString("from_table");
                String toTable = rs.getString("to_table");

                if (tableSet.contains(fromTable.toUpperCase()) &&
                    tableSet.contains(toTable.toUpperCase())) {
                    String fromTableOrig = findOriginalCase(tables, fromTable.toUpperCase());
                    String toTableOrig = findOriginalCase(tables, toTable.toUpperCase());
                    if (fromTableOrig != null && toTableOrig != null) {
                        dependencies.computeIfAbsent(fromTableOrig, k -> new HashSet<>()).add(toTableOrig);
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to analyze foreign keys via AD_Column", e);
        }
    }

    /**
     * 找到原始大小寫的表名
     */
    private static String findOriginalCase(Collection<String> tables, String upperCaseName) {
        for (String table : tables) {
            if (table.equalsIgnoreCase(upperCaseName)) {
                return table;
            }
        }
        return null;
    }

    /**
     * 使用拓撲排序計算刪除順序
     * @param dependencies 依賴關係 Map
     * @return 排序後的表清單（先刪除的在前）
     */
    public static List<String> topologicalSort(Map<String, Set<String>> dependencies) {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String table : dependencies.keySet()) {
            if (!visited.contains(table)) {
                topologicalSortDFS(table, dependencies, visited, visiting, result);
            }
        }

        return result;
    }

    private static void topologicalSortDFS(String table,
                                           Map<String, Set<String>> dependencies,
                                           Set<String> visited,
                                           Set<String> visiting,
                                           List<String> result) {
        if (visiting.contains(table)) {
            // 循環依賴，跳過
            log.warning("Circular dependency detected for table: " + table);
            return;
        }
        if (visited.contains(table)) {
            return;
        }

        visiting.add(table);

        Set<String> deps = dependencies.get(table);
        if (deps != null) {
            for (String dep : deps) {
                topologicalSortDFS(dep, dependencies, visited, visiting, result);
            }
        }

        visiting.remove(table);
        visited.add(table);
        // 被依賴的表放在後面，所以用 addFirst
        result.add(0, table);
    }

    /**
     * 取得按 FK 依賴排序的刪除順序
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 排序後的表清單
     */
    public static List<String> getDeleteOrder(int clientId, String trxName) {
        // 取得有資料的表
        Map<String, Integer> tablesWithData = getTablesWithData(clientId, trxName);
        if (tablesWithData.isEmpty()) {
            return new ArrayList<>();
        }

        // 分析 FK 依賴
        Map<String, Set<String>> dependencies = analyzeForeignKeys(tablesWithData.keySet(), trxName);

        // 拓撲排序
        return topologicalSort(dependencies);
    }

    /**
     * 取得按 FK 依賴排序的刪除順序（包含記錄數）
     * @param clientId Client ID
     * @param trxName 交易名稱
     * @return 排序後的表清單和記錄數
     */
    public static List<TableInfo> getDeleteOrderWithCount(int clientId, String trxName) {
        Map<String, Integer> tablesWithData = getTablesWithData(clientId, trxName);
        if (tablesWithData.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Set<String>> dependencies = analyzeForeignKeys(tablesWithData.keySet(), trxName);
        List<String> sortedTables = topologicalSort(dependencies);

        List<TableInfo> result = new ArrayList<>();
        for (String table : sortedTables) {
            Integer count = tablesWithData.get(table);
            if (count != null && count > 0) {
                result.add(new TableInfo(table, count));
            }
        }

        return result;
    }

    /**
     * 表資訊（包含名稱和記錄數）
     */
    public static class TableInfo {
        public final String tableName;
        public final int recordCount;

        public TableInfo(String tableName, int recordCount) {
            this.tableName = tableName;
            this.recordCount = recordCount;
        }

        @Override
        public String toString() {
            return tableName + ": " + recordCount + " 筆";
        }
    }
}

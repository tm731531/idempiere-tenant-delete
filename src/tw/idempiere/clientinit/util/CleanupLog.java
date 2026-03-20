package tw.idempiere.clientinit.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 清理日誌工具
 *
 * 用於記錄清理過程中的各種訊息，並可產生完整的執行報告。
 *
 * @author iDempiere Community
 */
public class CleanupLog {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final List<LogEntry> entries = new ArrayList<>();
    private final int clientId;
    private final Timestamp startTime;
    private Timestamp endTime;
    private int totalDeleted = 0;
    private int totalTables = 0;

    /**
     * 建立新的清理日誌
     * @param clientId Client ID
     */
    public CleanupLog(int clientId) {
        this.clientId = clientId;
        this.startTime = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 新增日誌項目
     * @param level 級別（INFO, WARNING, ERROR）
     * @param message 訊息
     */
    public void log(String level, String message) {
        entries.add(new LogEntry(level, message));
    }

    /**
     * 記錄資訊訊息
     * @param message 訊息
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * 記錄警告訊息
     * @param message 訊息
     */
    public void warning(String message) {
        log("WARNING", message);
    }

    /**
     * 記錄錯誤訊息
     * @param message 訊息
     */
    public void error(String message) {
        log("ERROR", message);
    }

    /**
     * 記錄表刪除結果
     * @param tableName 表名稱
     * @param deletedCount 刪除的筆數
     */
    public void logTableDelete(String tableName, int deletedCount) {
        if (deletedCount > 0) {
            info(tableName + ": 刪除 " + deletedCount + " 筆");
            totalDeleted += deletedCount;
        }
        totalTables++;
    }

    /**
     * 記錄 Phase 開始
     * @param phaseNumber Phase 編號
     * @param description Phase 描述
     */
    public void logPhaseStart(int phaseNumber, String description) {
        info("--- Phase " + phaseNumber + ": " + description + " ---");
    }

    /**
     * 設定結束時間
     */
    public void setEndTime() {
        this.endTime = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 取得執行時間（毫秒）
     * @return 執行時間
     */
    public long getExecutionTime() {
        Timestamp end = (endTime != null) ? endTime : new Timestamp(System.currentTimeMillis());
        return end.getTime() - startTime.getTime();
    }

    /**
     * 取得刪除的總筆數
     * @return 總筆數
     */
    public int getTotalDeleted() {
        return totalDeleted;
    }

    /**
     * 取得處理的表數量
     * @return 表數量
     */
    public int getTotalTables() {
        return totalTables;
    }

    /**
     * 產生完整的執行報告
     * @return 報告字串
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("========================================\n");
        sb.append("     Client 初始化執行報告\n");
        sb.append("========================================\n");
        sb.append("\n");
        sb.append("Client ID: ").append(clientId).append("\n");
        sb.append("開始時間: ").append(dateFormat.format(startTime)).append("\n");
        if (endTime != null) {
            sb.append("結束時間: ").append(dateFormat.format(endTime)).append("\n");
            sb.append("執行時間: ").append(getExecutionTime() / 1000.0).append(" 秒\n");
        }
        sb.append("\n");
        sb.append("----------------------------------------\n");
        sb.append("執行詳情:\n");
        sb.append("----------------------------------------\n");

        for (LogEntry entry : entries) {
            sb.append("[").append(entry.level).append("] ");
            sb.append(entry.message).append("\n");
        }

        sb.append("\n");
        sb.append("----------------------------------------\n");
        sb.append("統計摘要:\n");
        sb.append("----------------------------------------\n");
        sb.append("處理表數量: ").append(totalTables).append("\n");
        sb.append("刪除記錄總數: ").append(totalDeleted).append("\n");
        sb.append("========================================\n");

        return sb.toString();
    }

    /**
     * 取得簡短摘要
     * @return 摘要字串
     */
    public String getSummary() {
        return String.format("初始化完成 - 處理 %d 個表，刪除 %d 筆記錄，耗時 %.1f 秒",
                            totalTables, totalDeleted, getExecutionTime() / 1000.0);
    }

    /**
     * 取得所有日誌項目
     * @return 日誌項目清單
     */
    public List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * 日誌項目類別
     */
    public static class LogEntry {
        public final String level;
        public final String message;
        public final Timestamp timestamp;

        public LogEntry(String level, String message) {
            this.level = level;
            this.message = message;
            this.timestamp = new Timestamp(System.currentTimeMillis());
        }
    }
}

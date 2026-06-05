package model;

import model.enums.LockMechanism;
import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderTransaction extends BaseEntity {
    private String orderId;
    private LockMechanism lockMechanism;
    private int retryCount;
    private long processingTimeMs;
    private boolean success;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public OrderTransaction() {}

    public OrderTransaction(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                            String orderId, LockMechanism lockMechanism,
                            int retryCount, long processingTimeMs, boolean success) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        this.orderId = orderId;
        this.lockMechanism = lockMechanism;
        this.retryCount = retryCount;
        this.processingTimeMs = processingTimeMs;
        this.success = success;
    }

    // Getters & Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LockMechanism getLockMechanism() { return lockMechanism; }
    public void setLockMechanism(LockMechanism lockMechanism) { this.lockMechanism = lockMechanism; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                orderId,
                lockMechanism.name(),
                String.valueOf(retryCount),
                String.valueOf(processingTimeMs),
                String.valueOf(success)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 8) throw new IllegalArgumentException("Invalid OrderTransaction CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.orderId = parts[3].trim();
        this.lockMechanism = LockMechanism.valueOf(parts[4].trim());
        this.retryCount = Integer.parseInt(parts[5].trim());
        this.processingTimeMs = Long.parseLong(parts[6].trim());
        this.success = Boolean.parseBoolean(parts[7].trim());
    }
}

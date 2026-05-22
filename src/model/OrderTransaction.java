package model;

import model.enums.LockMechanism;
import util.CsvUtil;

public class OrderTransaction extends BaseEntity {
    private String orderId;
    private LockMechanism lockMechanism;
    private int retryCount;
    private long processingTimeMs;
    private boolean success;

    public OrderTransaction() {}

    public OrderTransaction(String id, String orderId, LockMechanism lockMechanism, int retryCount, long processingTimeMs, boolean success) {
        this.id = id;
        this.orderId = orderId;
        this.lockMechanism = lockMechanism;
        this.retryCount = retryCount;
        this.processingTimeMs = processingTimeMs;
        this.success = success;
    }

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
                id,
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
        if (parts.length < 6) throw new IllegalArgumentException("Invalid OrderTransaction CSV line");
        this.id = parts[0].trim();
        this.orderId = parts[1].trim();
        this.lockMechanism = LockMechanism.valueOf(parts[2].trim());
        this.retryCount = Integer.parseInt(parts[3].trim());
        this.processingTimeMs = Long.parseLong(parts[4].trim());
        this.success = Boolean.parseBoolean(parts[5].trim());
    }
}

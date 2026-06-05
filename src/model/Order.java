package model;

import model.enums.LockMechanism;
import model.enums.OrderStatus;
import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order extends BaseEntity {
    private String customerId;
    private String eventId;
    private double totalAmount;
    private OrderStatus status;
    private LockMechanism lockMechanism;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Order() {}

    public Order(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                 String customerId, String eventId, double totalAmount,
                 OrderStatus status, LockMechanism lockMechanism) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        this.customerId = customerId;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.lockMechanism = lockMechanism;
    }

    // Getters & Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LockMechanism getLockMechanism() { return lockMechanism; }
    public void setLockMechanism(LockMechanism lockMechanism) { this.lockMechanism = lockMechanism; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                customerId,
                eventId,
                String.valueOf(Math.round(totalAmount)),
                status.name(),
                lockMechanism.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 8) throw new IllegalArgumentException("Invalid Order CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.customerId = parts[3].trim();
        this.eventId = parts[4].trim();
        this.totalAmount = Double.parseDouble(parts[5].trim());
        this.status = OrderStatus.valueOf(parts[6].trim());
        this.lockMechanism = LockMechanism.valueOf(parts[7].trim());
    }
}

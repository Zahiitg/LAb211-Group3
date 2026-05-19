package model;

import model.enums.OrderStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order extends BaseEntity {
    private String customerId;
    private LocalDateTime orderTime;
    private OrderStatus status;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Order() {}

    public Order(String id, String customerId, LocalDateTime orderTime, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.orderTime = orderTime;
        this.status = status;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                customerId,
                orderTime.format(DTF),
                status.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid Order CSV line");
        this.id = parts[0].trim();
        this.customerId = parts[1].trim();
        this.orderTime = LocalDateTime.parse(parts[2].trim(), DTF);
        this.status = OrderStatus.valueOf(parts[3].trim());
    }
}

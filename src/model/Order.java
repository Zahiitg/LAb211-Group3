package model;
import model.enums.OrderStatus;
import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Order extends BaseEntity {
    private String customerId; private LocalDateTime orderTime; private OrderStatus status;
    public Order() {}
    public Order(String id, String customerId, LocalDateTime orderTime, OrderStatus status) {
        setId(id); this.customerId = customerId; this.orderTime = orderTime; this.status = status;
    }
    public String getCustomerId() { return customerId; } public void setCustomerId(String customerId) { this.customerId = customerId; }
    public LocalDateTime getOrderTime() { return orderTime; } public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    public OrderStatus getStatus() { return status; } public void setStatus(OrderStatus status) { this.status = status; }
    @Override public String toCsvLine() { return String.join(",", getId(), customerId, orderTime != null ? orderTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "", status.name()); }
    @Override public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line); setId(parts[0].trim()); this.customerId = parts[1].trim();
        this.orderTime = parts[2].isEmpty() ? null : LocalDateTime.parse(parts[2].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME); this.status = OrderStatus.valueOf(parts[3].trim());
    }
}

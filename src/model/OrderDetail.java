package model;

import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderDetail extends BaseEntity {
    private String orderId;
    private String flashSaleItemId;
    private int quantity;       // 1 hoac 2 (khach chi duoc mua toi da 2)
    private double unitPrice;   // gia tai thoi diem mua (= flashPrice)
    private double subTotal;    // = quantity * unitPrice

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public OrderDetail() {}

    public OrderDetail(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                       String orderId, String flashSaleItemId, int quantity,
                       double unitPrice, double subTotal) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        this.orderId = orderId;
        this.flashSaleItemId = flashSaleItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }

    // Getters & Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getFlashSaleItemId() { return flashSaleItemId; }
    public void setFlashSaleItemId(String flashSaleItemId) { this.flashSaleItemId = flashSaleItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                orderId,
                flashSaleItemId,
                String.valueOf(quantity),
                String.valueOf(Math.round(unitPrice)),
                String.valueOf(Math.round(subTotal))
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 8) throw new IllegalArgumentException("Invalid OrderDetail CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.orderId = parts[3].trim();
        this.flashSaleItemId = parts[4].trim();
        this.quantity = Integer.parseInt(parts[5].trim());
        this.unitPrice = Double.parseDouble(parts[6].trim());
        this.subTotal = Double.parseDouble(parts[7].trim());
    }
}

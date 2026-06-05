package model;

import util.CsvUtil;

public class OrderDetail extends BaseEntity {
    private String orderId;
    private String flashSaleItemId;
    private int quantity;   // 1 hoặc 2 (khách chỉ được mua tối đa 2)

    public OrderDetail() {}

    public OrderDetail(String id, String orderId, String flashSaleItemId, int quantity) {
        setId(id);
        this.orderId = orderId;
        this.flashSaleItemId = flashSaleItemId;
        this.quantity = quantity;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getFlashSaleItemId() { return flashSaleItemId; }
    public void setFlashSaleItemId(String flashSaleItemId) { this.flashSaleItemId = flashSaleItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                orderId,
                flashSaleItemId,
                String.valueOf(quantity)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid OrderDetail CSV line");
        setId(parts[0].trim());
        this.orderId = parts[1].trim();
        this.flashSaleItemId = parts[2].trim();
        this.quantity = Integer.parseInt(parts[3].trim());
    }
}

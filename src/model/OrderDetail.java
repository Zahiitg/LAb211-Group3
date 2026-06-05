package model;
import util.CsvUtil;
public class OrderDetail extends BaseEntity {
    private String orderId; private String flashSaleItemId; private int quantity; private double priceAtPurchase;

    public OrderDetail() {}
    public OrderDetail(String id, String orderId, String flashSaleItemId, int quantity, double priceAtPurchase) {
        setId(id); this.orderId = orderId; this.flashSaleItemId = flashSaleItemId; this.quantity = quantity; this.priceAtPurchase = priceAtPurchase;
    }
    public String getOrderId() { return orderId; } public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getFlashSaleItemId() { return flashSaleItemId; } public void setFlashSaleItemId(String flashSaleItemId) { this.flashSaleItemId = flashSaleItemId; }
    public int getQuantity() { return quantity; } public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPriceAtPurchase() { return priceAtPurchase; } public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
    @Override public String toCsvLine() { 
        return String.join(",", getId(), orderId, flashSaleItemId, String.valueOf(quantity), String.valueOf(priceAtPurchase)); 
    }
    @Override public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line); setId(parts[0].trim()); this.orderId = parts[1].trim(); this.flashSaleItemId = parts[2].trim();
        this.quantity = Integer.parseInt(parts[3].trim()); 
        this.priceAtPurchase = Double.parseDouble(parts[4].trim());
    }
}

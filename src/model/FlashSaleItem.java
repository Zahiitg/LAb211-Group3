package model;
import util.CsvUtil;
public class FlashSaleItem extends BaseEntity {
    private String productId; private String eventId; private int limitedQty; private int soldQty; private int version;
    public FlashSaleItem() {}
    public FlashSaleItem(String id, String productId, String eventId, int limitedQty, int soldQty, int version) {
        setId(id); this.productId = productId; this.eventId = eventId; this.limitedQty = limitedQty; this.soldQty = soldQty; this.version = version;
    }
    public String getProductId() { return productId; } public void setProductId(String productId) { this.productId = productId; }
    public String getEventId() { return eventId; } public void setEventId(String eventId) { this.eventId = eventId; }
    public int getLimitedQty() { return limitedQty; } public void setLimitedQty(int limitedQty) { this.limitedQty = limitedQty; }
    public int getSoldQty() { return soldQty; } public void setSoldQty(int soldQty) { this.soldQty = soldQty; }
    public int getVersion() { return version; } public void setVersion(int version) { this.version = version; }
    public boolean hasStock(int qty) { return (soldQty + qty) <= limitedQty; }
    public void increaseSold(int qty) { if (!hasStock(qty)) throw new IllegalStateException("Out of stock"); this.soldQty += qty; }
    @Override public String toCsvLine() { return String.join(",", getId(), productId, eventId, String.valueOf(limitedQty), String.valueOf(soldQty), String.valueOf(version)); }
    @Override public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line); setId(parts[0].trim()); this.productId = parts[1].trim(); this.eventId = parts[2].trim();
        this.limitedQty = Integer.parseInt(parts[3].trim()); this.soldQty = Integer.parseInt(parts[4].trim()); this.version = Integer.parseInt(parts[5].trim());
    }
}

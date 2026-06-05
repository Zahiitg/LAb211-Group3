package model;

import util.CsvUtil;

public class FlashSaleItem extends BaseEntity {
    private String productId;
    private String eventId;
    private int limitedQty;   // giới hạn flash sale
    private int soldQty;      // đã bán
    private int version;      // cho optimistic lock, khởi tạo = 1

    public FlashSaleItem() {
        this.version = 1;
    }

    public FlashSaleItem(String id, String productId, String eventId, int limitedQty, int soldQty, int version) {
        setId(id);
        this.productId = productId;
        this.eventId = eventId;
        this.limitedQty = limitedQty;
        this.soldQty = soldQty;
        this.version = version;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public int getLimitedQty() { return limitedQty; }
    public void setLimitedQty(int limitedQty) { this.limitedQty = limitedQty; }
    public int getSoldQty() { return soldQty; }
    public void setSoldQty(int soldQty) { this.soldQty = soldQty; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public boolean hasStock(int requestedQty) {
        return (soldQty + requestedQty) <= limitedQty;
    }

    public void increaseSold(int qty) {
        this.soldQty += qty;
        this.version++;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                productId,
                eventId,
                String.valueOf(limitedQty),
                String.valueOf(soldQty),
                String.valueOf(version)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 6) throw new IllegalArgumentException("Invalid FlashSaleItem CSV line");
        setId(parts[0].trim());
        this.productId = parts[1].trim();
        this.eventId = parts[2].trim();
        this.limitedQty = Integer.parseInt(parts[3].trim());
        this.soldQty = Integer.parseInt(parts[4].trim());
        this.version = Integer.parseInt(parts[5].trim());
    }
}

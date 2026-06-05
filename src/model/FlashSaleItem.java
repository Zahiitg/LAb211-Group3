package model;

import model.enums.SaleStatus;
import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlashSaleItem extends BaseEntity {
    private String eventId;
    private String productId;
    private double flashPrice;      // gia sale (da giam)
    private int limitedQty;         // gioi han flash sale
    private int soldQty;            // da ban
    private int discountPercent;    // % giam gia (10-70)
    private int version;            // cho optimistic lock
    private SaleStatus status;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FlashSaleItem() {
        this.version = 1;
    }

    public FlashSaleItem(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                         String eventId, String productId, double flashPrice,
                         int limitedQty, int soldQty, int discountPercent,
                         int version, SaleStatus status) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        this.eventId = eventId;
        this.productId = productId;
        this.flashPrice = flashPrice;
        this.limitedQty = limitedQty;
        this.soldQty = soldQty;
        this.discountPercent = discountPercent;
        this.version = version;
        this.status = status;
    }

    // Getters & Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public double getFlashPrice() { return flashPrice; }
    public void setFlashPrice(double flashPrice) { this.flashPrice = flashPrice; }
    public int getLimitedQty() { return limitedQty; }
    public void setLimitedQty(int limitedQty) { this.limitedQty = limitedQty; }
    public int getSoldQty() { return soldQty; }
    public void setSoldQty(int soldQty) { this.soldQty = soldQty; }
    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public SaleStatus getStatus() { return status; }
    public void setStatus(SaleStatus status) { this.status = status; }

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
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                eventId,
                productId,
                String.valueOf(Math.round(flashPrice)),
                String.valueOf(limitedQty),
                String.valueOf(soldQty),
                String.valueOf(discountPercent),
                String.valueOf(version),
                status.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 11) throw new IllegalArgumentException("Invalid FlashSaleItem CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.eventId = parts[3].trim();
        this.productId = parts[4].trim();
        this.flashPrice = Double.parseDouble(parts[5].trim());
        this.limitedQty = Integer.parseInt(parts[6].trim());
        this.soldQty = Integer.parseInt(parts[7].trim());
        this.discountPercent = Integer.parseInt(parts[8].trim());
        this.version = Integer.parseInt(parts[9].trim());
        this.status = SaleStatus.valueOf(parts[10].trim());
    }
}

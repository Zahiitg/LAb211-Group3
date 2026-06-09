package model;

import util.CsvUtil;

public class FlashSaleItem extends BaseEntity {
    private String productId; 
    private String eventId; 
    private double salePrice; // <-- Bổ sung trường giá sale
    private int limitedQty; 
    private int soldQty; 
    private int version;

    public FlashSaleItem() {}

    // Cập nhật Constructor để nhận thêm salePrice
    public FlashSaleItem(String id, String productId, String eventId, double salePrice, int limitedQty, int soldQty, int version) {
        setId(id); 
        this.productId = productId; 
        this.eventId = eventId; 
        this.salePrice = salePrice; // Khởi tạo giá sale
        this.limitedQty = limitedQty; 
        this.soldQty = soldQty; 
        this.version = version;
    }

    // Getters và Setters cho các trường cũ
    public String getProductId() { return productId; } 
    public void setProductId(String productId) { this.productId = productId; }
    public String getEventId() { return eventId; } 
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    // Thêm Getter và Setter cho salePrice
    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public int getLimitedQty() { return limitedQty; } 
    public void setLimitedQty(int limitedQty) { this.limitedQty = limitedQty; }
    public int getSoldQty() { return soldQty; } 
    public void setSoldQty(int soldQty) { this.soldQty = soldQty; }
    public int getVersion() { return version; } 
    public void setVersion(int version) { this.version = version; }

    // Giữ nguyên logic kiểm tra và tăng kho rất tốt hiện tại của bạn
    public boolean hasStock(int qty) { return (soldQty + qty) <= limitedQty; }
    
    public void increaseSold(int qty) { 
        if (!hasStock(qty)) throw new IllegalStateException("Out of stock"); 
        this.soldQty += qty; 
    }

    @Override 
    public String toCsvLine() { 
        // Thêm String.valueOf(salePrice) vào vị trí thứ 4 (chỉ mục số 3)
        return String.join(",", 
            getId(), 
            productId, 
            eventId, 
            String.valueOf(salePrice), 
            String.valueOf(limitedQty), 
            String.valueOf(soldQty), 
            String.valueOf(version)
        ); 
    }

    @Override 
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line); 
        
        setId(parts[0].trim()); 
        this.productId = parts[1].trim(); 
        this.eventId = parts[2].trim();
        
        // Thêm logic parse salePrice và dịch chuyển chỉ mục các trường phía sau
        this.salePrice = Double.parseDouble(parts[3].trim()); 
        this.limitedQty = Integer.parseInt(parts[4].trim()); 
        this.soldQty = Integer.parseInt(parts[5].trim()); 
        this.version = Integer.parseInt(parts[6].trim());
    }
}
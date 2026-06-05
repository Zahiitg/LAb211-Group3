package model;
import util.CsvUtil;
public class Product extends BaseEntity {
    private String sellerId; private String name; private String category;    private double price; 
    private int stock;

    public Product() {}
    public Product(String id, String sellerId, String name, String category, double price, int stock) {
        setId(id); this.sellerId = sellerId; this.name = name; this.category = category; this.price = price; this.stock = stock;
    }
    public String getSellerId() { return sellerId; } public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; } public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; } public void setStock(int stock) { this.stock = stock; }
    @Override public String toCsvLine() { 
        return String.join(",", getId(), sellerId, CsvUtil.escapeCsv(name), CsvUtil.escapeCsv(category), String.valueOf(price), String.valueOf(stock)); 
    }
    @Override public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line); setId(parts[0].trim()); this.sellerId = parts[1].trim(); this.name = CsvUtil.unescapeCsv(parts[2]).trim();
        this.category = CsvUtil.unescapeCsv(parts[3]).trim(); 
        this.price = Double.parseDouble(parts[4].trim());
        this.stock = Integer.parseInt(parts[5].trim());
    }
}

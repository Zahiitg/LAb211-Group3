package model;

import util.CsvUtil;

public class Product extends BaseEntity {
    private String name;
    private String category;
    private double price;
    private int stock;  // tồn kho vật lý (không dùng cho flash sale, chỉ để tham khảo)

    public Product() {}

    public Product(String id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                CsvUtil.escapeCsv(name),
                CsvUtil.escapeCsv(category),
                String.valueOf(price),
                String.valueOf(stock)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 5) throw new IllegalArgumentException("Invalid Product CSV line");
        this.id = parts[0].trim();
        this.name = CsvUtil.unescapeCsv(parts[1]).trim();
        this.category = CsvUtil.unescapeCsv(parts[2]).trim();
        this.price = Double.parseDouble(parts[3].trim());
        this.stock = Integer.parseInt(parts[4].trim());
    }
}

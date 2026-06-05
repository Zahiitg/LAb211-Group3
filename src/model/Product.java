package model;

import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Product extends BaseEntity {
    private String name;
    private String category;
    private double price;
    private int stock;  // ton kho vat ly (khong dung cho flash sale, chi de tham khao)

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Product() {}

    public Product(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                   String name, String category, double price, int stock) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
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
                getId(),
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                CsvUtil.escapeCsv(name),
                CsvUtil.escapeCsv(category),
                String.valueOf(price),
                String.valueOf(stock)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 7) throw new IllegalArgumentException("Invalid Product CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.name = CsvUtil.unescapeCsv(parts[3]).trim();
        this.category = CsvUtil.unescapeCsv(parts[4]).trim();
        this.price = Double.parseDouble(parts[5].trim());
        this.stock = Integer.parseInt(parts[6].trim());
    }
}

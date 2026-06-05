package model;

import model.enums.CustTier;
import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Customer extends BaseEntity {
    private String fullName;
    private String phone;
    private String email;
    private CustTier tier;
    private double totalSpent;
    private boolean active;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Customer() {}

    public Customer(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String fullName, String phone, String email,
                    CustTier tier, double totalSpent, boolean active) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.tier = tier;
        this.totalSpent = totalSpent;
        this.active = active;
    }

    // Getters & Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public CustTier getTier() { return tier; }
    public void setTier(CustTier tier) { this.tier = tier; }
    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                CsvUtil.escapeCsv(fullName),
                phone,
                email,
                tier.name(),
                String.valueOf(Math.round(totalSpent)),
                String.valueOf(active).toUpperCase()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 9) throw new IllegalArgumentException("Invalid Customer CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.fullName = CsvUtil.unescapeCsv(parts[3]).trim();
        this.phone = parts[4].trim();
        this.email = parts[5].trim();
        this.tier = CustTier.valueOf(parts[6].trim());
        this.totalSpent = Double.parseDouble(parts[7].trim());
        this.active = Boolean.parseBoolean(parts[8].trim());
    }
}

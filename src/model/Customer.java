package model;

import model.enums.CustTier;
import util.CsvUtil;

public class Customer extends BaseEntity {
    private String name;
    private String email;
    private CustTier tier;

    public Customer() {}

    public Customer(String id, String name, String email, CustTier tier) {
        setId(id);
        this.name = name;
        this.email = email;
        this.tier = tier;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public CustTier getTier() { return tier; }
    public void setTier(CustTier tier) { this.tier = tier; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                CsvUtil.escapeCsv(name),
                email,
                tier.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid Customer CSV line");
        setId(parts[0].trim());
        this.name = CsvUtil.unescapeCsv(parts[1]).trim();
        this.email = parts[2].trim();
        this.tier = CustTier.valueOf(parts[3].trim());
    }
}

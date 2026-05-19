package model;

import model.enums.CustTier;

public class Customer extends BaseEntity {
    private String name;
    private String email;
    private CustTier tier;

    public Customer() {}

    public Customer(String id, String name, String email, CustTier tier) {
        this.id = id;
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
                id,
                escapeCsv(name),
                email,
                tier.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid Customer CSV line");
        this.id = parts[0].trim();
        this.name = unescapeCsv(parts[1].trim());
        this.email = parts[2].trim();
        this.tier = CustTier.valueOf(parts[3].trim());
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String unescapeCsv(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }
}

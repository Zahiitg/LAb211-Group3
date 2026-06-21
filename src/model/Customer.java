package model;

import model.enums.AccountStatus;
import model.enums.CustTier;
import util.CsvUtil;

public class Customer extends User {
    private CustTier tier;
    private String address;

    public Customer() {
    }

    public Customer(String id, String name, String email, String password, AccountStatus status, CustTier tier, String address) {
        setId(id);
        setName(name);
        setEmail(email);
        setPassword(password);
        setStatus(status);
        this.tier = tier;
        this.address = address != null ? address : "";
    }

    public CustTier getTier() {
        return tier;
    }

    public void setTier(CustTier tier) {
        this.tier = tier;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address != null ? address : "";
    }

    @Override
    public String toCsvLine() {
        return String.join(",", getId(), CsvUtil.escapeCsv(getName()), getEmail(), getPassword(), getStatus().name(),
                tier.name(), CsvUtil.escapeCsv(address));
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        setId(parts[0].trim());
        setName(CsvUtil.unescapeCsv(parts[1]).trim());
        setEmail(parts[2].trim());
        setPassword(parts[3].trim());
        setStatus(AccountStatus.valueOf(parts[4].trim()));
        this.tier = CustTier.valueOf(parts[5].trim());
        if (parts.length > 6) {
            this.address = CsvUtil.unescapeCsv(parts[6]).trim();
        } else {
            this.address = "";
        }
    }
}

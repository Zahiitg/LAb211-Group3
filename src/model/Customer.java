package model;
import model.enums.AccountStatus;
import model.enums.CustTier;
import util.CsvUtil;
public class Customer extends User {
    private CustTier tier;
    public Customer() {}
    public Customer(String id, String name, String email, String password, AccountStatus status, CustTier tier) {
        setId(id); setName(name); setEmail(email); setPassword(password); setStatus(status); this.tier = tier;
    }
    public CustTier getTier() { return tier; }
    public void setTier(CustTier tier) { this.tier = tier; }
    @Override public String toCsvLine() {
        return String.join(",", getId(), CsvUtil.escapeCsv(getName()), getEmail(), getPassword(), getStatus().name(), tier.name());
    }
    @Override public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        setId(parts[0].trim()); setName(CsvUtil.unescapeCsv(parts[1]).trim()); setEmail(parts[2].trim()); setPassword(parts[3].trim());
        setStatus(AccountStatus.valueOf(parts[4].trim())); this.tier = CustTier.valueOf(parts[5].trim());
    }
}

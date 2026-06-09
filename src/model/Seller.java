package model;

import model.enums.AccountStatus;
import util.CsvUtil;

public class Seller extends User {
    private String storeName;

    public Seller() {
    }

    public Seller(String id, String name, String email, String password, AccountStatus status, String storeName) {
        setId(id);
        setName(name);
        setEmail(email);
        setPassword(password);
        setStatus(status);
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", getId(), CsvUtil.escapeCsv(getName()), getEmail(), getPassword(), getStatus().name(),
                CsvUtil.escapeCsv(storeName));
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        setId(parts[0].trim());
        setName(CsvUtil.unescapeCsv(parts[1]).trim());
        setEmail(parts[2].trim());
        setPassword(parts[3].trim());
        setStatus(AccountStatus.valueOf(parts[4].trim()));
        this.storeName = CsvUtil.unescapeCsv(parts[5]).trim();
    }
}

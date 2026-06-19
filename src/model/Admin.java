package model;

import model.enums.AccountStatus;
import util.CsvUtil;

public class Admin extends User {
    private int roleLevel;

    public Admin() {
    }

    public Admin(String id, String name, String email, String password, AccountStatus status, int roleLevel) {
        setId(id);
        setName(name);
        setEmail(email);
        setPassword(password);
        setStatus(status);
        this.roleLevel = roleLevel;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", getId(), CsvUtil.escapeCsv(getName()), getEmail(), getPassword(), getStatus().name(),
                String.valueOf(roleLevel));
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        setId(parts[0].trim());
        setName(CsvUtil.unescapeCsv(parts[1]).trim());
        setEmail(parts[2].trim());
        setPassword(parts[3].trim());
        setStatus(AccountStatus.valueOf(parts[4].trim()));
        this.roleLevel = Integer.parseInt(parts[5].trim());
    }
}

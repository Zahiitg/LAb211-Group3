package model;

import model.enums.AccountStatus;
import util.CsvUtil;

public class Guest extends User {
    private String sessionId;

    public Guest() {
        setId("");
        setName("Guest");
        setEmail("");
        setPassword("");
        setStatus(AccountStatus.APPROVED);
        this.sessionId = "";
    }

    public Guest(String sessionId) {
        setId("GUEST_" + sessionId);
        setName("Guest");
        setEmail("");
        setPassword("");
        setStatus(AccountStatus.APPROVED);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        setId("GUEST_" + sessionId);
    }

    @Override
    public String toCsvLine() {
        return String.join(",", getId(), sessionId);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        setId(parts[0].trim());
        this.sessionId = parts[1].trim();
        setName("Guest");
        setEmail("");
        setPassword("");
        setStatus(AccountStatus.APPROVED);
    }
}

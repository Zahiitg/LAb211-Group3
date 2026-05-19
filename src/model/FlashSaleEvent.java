package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlashSaleEvent extends BaseEntity {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FlashSaleEvent() {}

    public FlashSaleEvent(String id, String name, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                escapeCsv(name),
                startTime.format(DTF),
                endTime.format(DTF)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid FlashSaleEvent CSV line");
        this.id = parts[0].trim();
        this.name = unescapeCsv(parts[1].trim());
        this.startTime = LocalDateTime.parse(parts[2].trim(), DTF);
        this.endTime = LocalDateTime.parse(parts[3].trim(), DTF);
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

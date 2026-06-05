package model;
import model.enums.SaleStatus;
import util.CsvUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class FlashSaleEvent extends BaseEntity {
    private String name; private LocalDateTime startTime; private LocalDateTime endTime; private SaleStatus status;
    public FlashSaleEvent() {}
    public FlashSaleEvent(String id, String name, LocalDateTime startTime, LocalDateTime endTime, SaleStatus status) {
        setId(id); this.name = name; this.startTime = startTime; this.endTime = endTime; this.status = status;
    }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public SaleStatus getStatus() { return status; } public void setStatus(SaleStatus status) { this.status = status; }
    @Override public String toCsvLine() {
        return String.join(",", getId(), CsvUtil.escapeCsv(name), startTime != null ? startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "", endTime != null ? endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "", status.name());
    }
    @Override public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line); setId(parts[0].trim()); this.name = CsvUtil.unescapeCsv(parts[1]).trim();
        this.startTime = parts[2].isEmpty() ? null : LocalDateTime.parse(parts[2].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.endTime = parts[3].isEmpty() ? null : LocalDateTime.parse(parts[3].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME); this.status = SaleStatus.valueOf(parts[4].trim());
    }
}

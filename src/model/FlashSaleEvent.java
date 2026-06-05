package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import model.enums.SaleStatus;
import util.CsvUtil;

public class FlashSaleEvent extends BaseEntity {
    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SaleStatus status;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FlashSaleEvent() {}

    public FlashSaleEvent(String id, LocalDateTime createdAt, LocalDateTime updatedAt,
                          String eventName, LocalDateTime startTime, LocalDateTime endTime,
                          SaleStatus status) {
        setId(id);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    /**
     * Tinh toan trang thai dua tren thoi gian hien tai (dung khi khong co status luu san).
     * Neu status da duoc set thu cong (vd: DISABLED), phuong thuc nay KHONG ghi de.
     */
    public SaleStatus computeStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return SaleStatus.UPCOMING;
        } else if (now.isAfter(endTime)) {
            return SaleStatus.ENDED;
        } else {
            return SaleStatus.ONGOING;
        }
    }

    // Getters & Setters
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public SaleStatus getStatus() { return status; }
    public void setStatus(SaleStatus status) { this.status = status; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                getCreatedAt().format(DTF),
                getUpdatedAt().format(DTF),
                CsvUtil.escapeCsv(eventName),
                startTime.format(DTF),
                endTime.format(DTF),
                status.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 7) throw new IllegalArgumentException("Invalid FlashSaleEvent CSV line");
        setId(parts[0].trim());
        setCreatedAt(LocalDateTime.parse(parts[1].trim(), DTF));
        setUpdatedAt(LocalDateTime.parse(parts[2].trim(), DTF));
        this.eventName = CsvUtil.unescapeCsv(parts[3]).trim();
        this.startTime = LocalDateTime.parse(parts[4].trim(), DTF);
        this.endTime = LocalDateTime.parse(parts[5].trim(), DTF);
        this.status = SaleStatus.valueOf(parts[6].trim());
    }
}

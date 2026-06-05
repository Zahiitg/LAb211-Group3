package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import model.enums.SaleStatus;
import util.CsvUtil;

public class FlashSaleEvent extends BaseEntity {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FlashSaleEvent() {
    }

    public SaleStatus getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return SaleStatus.UPCOMING;
        } else if (now.isAfter(endTime)) {
            return SaleStatus.ENDED;
        } else {
            return SaleStatus.ONGOING;
        }
    }

    public FlashSaleEvent(String id, String name, LocalDateTime startTime, LocalDateTime endTime) {
        setId(id);
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId(),
                CsvUtil.escapeCsv(name),
                startTime.format(DTF),
                endTime.format(DTF));
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = CsvUtil.splitCsvLine(line);
        if (parts.length < 4)
            throw new IllegalArgumentException("Invalid FlashSaleEvent CSV line");
        setId(parts[0].trim());
        this.name = CsvUtil.unescapeCsv(parts[1]).trim();
        this.startTime = LocalDateTime.parse(parts[2].trim(), DTF);
        this.endTime = LocalDateTime.parse(parts[3].trim(), DTF);
    }
}

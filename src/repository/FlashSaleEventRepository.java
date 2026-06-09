package repository;

import java.time.LocalDateTime;
import java.util.List;
import model.FlashSaleEvent;
import model.enums.SaleStatus;

public class FlashSaleEventRepository extends CsvRepository<FlashSaleEvent> {

    // SỬA LỖI 1: Truyền thêm biến boolean (ví dụ: true) vào super theo đúng yêu cầu lớp cha
    public FlashSaleEventRepository(String filePath) {
        super(filePath, true); 
    }

    // SỬA LỖI 2: Implement bắt buộc hàm createEntity() từ lớp cha CsvRepository
    @Override
    protected FlashSaleEvent createEntity() {
        return new FlashSaleEvent();
    }

    public List<FlashSaleEvent> findActiveEvents(LocalDateTime now) {
        return findBy(event -> {
            if (event.getStatus() != SaleStatus.ONGOING) return false;
            if (event.getStartTime() == null || event.getEndTime() == null) return false;
            return !now.isBefore(event.getStartTime()) && !now.isAfter(event.getEndTime());
        });
    }

    public List<FlashSaleEvent> findUpcomingEvents(LocalDateTime now) {
        return findBy(event -> 
            event.getStatus() == SaleStatus.UPCOMING && 
            event.getStartTime() != null && 
            now.isBefore(event.getStartTime())
        );
    }
}
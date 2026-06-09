package repository;

import model.FlashSaleItem;
import java.util.List;

public class FlashSaleItemRepository extends CsvRepository<FlashSaleItem> {

    // SỬA LỖI 1: Truyền thêm biến boolean (ví dụ: true) vào super
    public FlashSaleItemRepository(String filePath) {
        super(filePath, true);
    }

    // SỬA LỖI 2: Implement bắt buộc hàm createEntity() từ lớp cha CsvRepository
    @Override
    protected FlashSaleItem createEntity() {
        return new FlashSaleItem();
    }

    public List<FlashSaleItem> findItemsByEventId(String eventId) {
        // Thay thế List.of() bằng Collections.emptyList()
        if (eventId == null || eventId.trim().isEmpty()) return java.util.Collections.emptyList();
        return findBy(item -> eventId.equals(item.getEventId()));
    }

    protected boolean deductStock(String itemId, int buyQuantity) {
        FlashSaleItem item = getById(itemId);
        if (item == null) return false;

        int remainingQty = item.getLimitedQty() - item.getSoldQty();
        if (remainingQty >= buyQuantity) {
            item.setSoldQty(item.getSoldQty() + buyQuantity);
            item.setVersion(item.getVersion() + 1); 
            update(item);
            return true;
        }
        return false;
    }

    // ==========================================================================
    // KHU VỰC CỦA THÀNH VIÊN 4
    // ==========================================================================
    public boolean sellWithNoLock(String itemId, int quantity) {
        throw new UnsupportedOperationException("TV4: Implement No Lock o day.");
    }

    public boolean sellWithSynchronized(String itemId, int quantity) {
        throw new UnsupportedOperationException("TV4: Implement Synchronized Lock o day.");
    }

    public boolean sellWithFileLock(String itemId, int quantity) {
        throw new UnsupportedOperationException("TV4: Implement File Lock o day.");
    }

    public boolean sellWithOptimisticLock(String itemId, int quantity) {
        throw new UnsupportedOperationException("TV4: Implement Optimistic Lock o day.");
    }
}
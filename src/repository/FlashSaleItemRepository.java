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

    @Override
    protected String getHeader() {
        return "id,productId,eventId,salePrice,limitedQty,soldQty,version";
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
    // KHU VỰC CỦA THÀNH VIÊN 4 - THUẬT TOÁN KHÓA (LOCKING)
    // ==========================================================================

    /**
     * Ban hang KHONG co co che khoa.
     *
     * Cach hoat dong:
     * 1. Doc item tu Cache.
     * 2. Kiem tra con hang khong.
     * 3. Tru kho va luu file.
     *
     * Nhuoc diem: Khi co 2 thread cung doc-ghi dong thoi,
     * ca 2 deu thay "con hang" va cung tru → BAN LO (Overselling).
     *
     * @param itemId   Ma san pham Flash Sale
     * @param quantity So luong mua
     * @return true neu tru kho thanh cong, false neu het hang
     */
    public boolean sellWithNoLock(String itemId, int quantity) {
        return deductStock(itemId, quantity);
    }

    /**
     * Ban hang voi khoa SYNCHRONIZED (cap JVM).
     *
     * Cach hoat dong:
     * - Tu khoa `synchronized` dam bao tai moi thoi diem chi co DUY NHAT 1 thread
     *   duoc phep thuc thi khoi code ben trong.
     * - Cac thread khac phai xep hang doi den khi thread hien tai hoan tat.
     *
     * Uu diem: Don gian, hieu qua khi chay trong 1 JVM duy nhat.
     * Nhuoc diem: Khong bao ve duoc khi chay nhieu JVM (nhieu process) cung luc.
     *
     * @param itemId   Ma san pham Flash Sale
     * @param quantity So luong mua
     * @return true neu tru kho thanh cong, false neu het hang
     */
    public synchronized boolean sellWithSynchronized(String itemId, int quantity) {
        return deductStock(itemId, quantity);
    }

    /** Doi tuong khoa dung cho FileLock de tranh deadlock */
    private final Object fileLockMonitor = new Object();

    /**
     * Ban hang voi khoa FILE LOCK (cap He dieu hanh).
     *
     * Cach hoat dong:
     * 1. Mo FileChannel tren file CSV.
     * 2. Goi FileChannel.lock() de yeu cau HE DIEU HANH khoa file nay lai.
     *    → Moi tien trinh/JVM khac cung mo file se bi CHAN cho den khi khoa duoc giai phong.
     * 3. Thuc hien tru kho.
     * 4. Giai phong khoa trong khoi finally (BAT BUOC) de tranh deadlock vinh vien.
     *
     * Uu diem: An toan tuyet doi voi da tien trinh (Multi-process).
     * Nhuoc diem: Cham hon Synchronized vi phai giao tiep voi OS.
     *
     * @param itemId   Ma san pham Flash Sale
     * @param quantity So luong mua
     * @return true neu tru kho thanh cong, false neu het hang
     */
    public boolean sellWithFileLock(String itemId, int quantity) {
        synchronized (fileLockMonitor) {
            java.io.File lockFile = new java.io.File(getFilePath() + ".lock");
            java.io.RandomAccessFile raf = null;
            java.nio.channels.FileChannel channel = null;
            java.nio.channels.FileLock lock = null;

            try {
                // Tao file khoa neu chua ton tai
                lockFile.createNewFile();

                // Mo kenh file va yeu cau HE DIEU HANH khoa
                raf = new java.io.RandomAccessFile(lockFile, "rw");
                channel = raf.getChannel();
                lock = channel.lock(); // BLOCKING: Doi cho den khi lay duoc khoa

                // VUNG AN TOAN (Critical Section) - Chi 1 thread/process duoc vao
                boolean result = deductStock(itemId, quantity);
                return result;

            } catch (java.io.IOException e) {
                System.err.println("[FileLock] Loi khi khoa file: " + e.getMessage());
                return false;
            } finally {
                // GIAI PHONG KHOA - BAT BUOC phai nam trong finally
                // De dam bao du co Exception thi khoa van duoc tra lai,
                // tranh tinh trang deadlock vinh vien.
                try {
                    if (lock != null) lock.release();
                } catch (java.io.IOException ignored) {}
                try {
                    if (channel != null) channel.close();
                } catch (java.io.IOException ignored) {}
                try {
                    if (raf != null) raf.close();
                } catch (java.io.IOException ignored) {}
            }
        }
    }

    /** So lan retry toi da cho Optimistic Lock */
    private static final int MAX_RETRY = 5;

    /**
     * Ban hang voi khoa OPTIMISTIC LOCK (kiem tra phien ban - Version Check).
     *
     * Cach hoat dong (Compare-And-Swap):
     * 1. Doc item va ghi nho phien ban hien tai (oldVersion).
     * 2. Kiem tra con hang khong.
     * 3. Tang version len 1 va tru kho.
     * 4. Truoc khi luu, KIEM TRA LAI: Neu version trong Cache van bang oldVersion
     *    → Khong ai chen ngang → Cho phep luu.
     * 5. Neu version da bi thay doi (ai do chen ngang truoc) → HUY BO va THU LAI (retry).
     *
     * Uu diem: Khong khoa cung (No blocking) → Hieu suat cao khi xung dot thap.
     * Nhuoc diem: Khi xung dot cao (nhieu nguoi mua cung luc), so lan retry tang
     *             va co the that bai neu vuot qua MAX_RETRY.
     *
     * @param itemId   Ma san pham Flash Sale
     * @param quantity So luong mua
     * @return true neu tru kho thanh cong, false neu het hang hoac retry het luot
     */
    public boolean sellWithOptimisticLock(String itemId, int quantity) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            FlashSaleItem item = getById(itemId);
            if (item == null) return false;

            // Buoc 1: Ghi nho phien ban hien tai TRUOC khi sua
            int oldVersion = item.getVersion();
            int oldSoldQty = item.getSoldQty();

            // Buoc 2: Kiem tra ton kho
            int remainingQty = item.getLimitedQty() - oldSoldQty;
            if (remainingQty < quantity) {
                return false; // Het hang that su, khong can retry
            }

            // Buoc 3 & 4: Compare & Swap nguyen tu (Atomic)
            // Trong thuc te, Database se dam bao viec nay. 
            // Khi chay tren RAM, ta phai dung synchronized() CHI CHO DOAN NAY 
            // de gia lap hanh vi Atomic cua cau lenh SQL: 
            // UPDATE ... WHERE version = oldVersion
            boolean success = false;
            synchronized (item) {
                if (item.getVersion() == oldVersion) {
                    item.setSoldQty(oldSoldQty + quantity);
                    item.setVersion(oldVersion + 1);
                    success = true;
                }
            }

            if (success) {
                update(item);
                return true;
            } else {
                // Ai do da chen ngang → Retry
                try {
                    Thread.sleep(10 + (long)(Math.random() * 20));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        // Da retry het MAX_RETRY lan ma van bi xung dot → That bai
        System.err.println("[OptimisticLock] That bai sau " + MAX_RETRY
                          + " lan retry cho item: " + itemId);
        return false;
    }

}
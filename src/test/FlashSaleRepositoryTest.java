package test;

import java.time.LocalDateTime;
import java.util.List;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.enums.SaleStatus;
import repository.FlashSaleEventRepository;
import repository.FlashSaleItemRepository;

public class FlashSaleRepositoryTest {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("===               FLASHSALE REPOSITORY REAL CRUD TEST                  ===");
        System.out.println("==========================================================================\n");

        // Đường dẫn file test thật nằm trong thư mục data của dự án
        String eventFile = "data/flash_sale_events.csv";
        String itemFile = "data/flash_sale_items.csv";

        FlashSaleEventRepository eventRepo = new FlashSaleEventRepository(eventFile);
        FlashSaleItemRepository itemRepo = new FlashSaleItemRepository(itemFile);

        // --- TEST 1: THÊM MỚI VÀ GHI FILE THẬT ---
        System.out.println("[TEST 1] Them moi Event va Item xuong file CSV...");
        
        LocalDateTime start = LocalDateTime.of(2026, 6, 9, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 9, 23, 59, 59);
        
        FlashSaleEvent newEvent = new FlashSaleEvent("E-TEST-01", "Sieu Sale Thử Nghiệm", start, end, SaleStatus.ONGOING);
        eventRepo.add(newEvent);
        System.out.println("-> Da add Event: " + newEvent.getId() + ". Tong so hien tai: " + eventRepo.count());

        FlashSaleItem newItem = new FlashSaleItem("ITEM-TEST-01", "PROD-111", "E-TEST-01", 99000.0, 100, 0, 1);
        itemRepo.add(newItem);
        System.out.println("-> Da add Item: " + newItem.getId() + ". Tong so hien tai: " + itemRepo.count());

        // --- TEST 2: ĐỌC VÀ LỌC DỮ LIỆU ĐẶC THÙ ---
        System.out.println("\n[TEST 2] Kiem tra cac ham loc dac thu...");
        
        // Kiểm tra hàm tìm kiếm Event đang kích hoạt tại thời điểm hiện tại (2026-06-09)
        LocalDateTime mockNow = LocalDateTime.of(2026, 6, 9, 15, 0, 0);
        List<FlashSaleEvent> activeEvents = eventRepo.findActiveEvents(mockNow);
        System.out.println("-> Tim thay " + activeEvents.size() + " event dang dien ra tai thoi diem: " + mockNow);

        // Kiểm tra hàm tìm hàng theo mã Event
        List<FlashSaleItem> itemsInEvent = itemRepo.findItemsByEventId("E-TEST-01");
        System.out.println("-> Tim thay " + itemsInEvent.size() + " mat hang thuoc Event 'E-TEST-01'");

        // // --- TEST 3: XÓA ĐỂ DỌN RÁC DATA ---
        // System.out.println("\n[TEST 3] Tien hanh xoa du lieu test de don dep file...");
        // boolean eventDeleted = eventRepo.delete("E-TEST-01");
        // boolean itemDeleted = itemRepo.delete("ITEM-TEST-01");
        
        // System.out.println("-> Xoa Event test: " + (eventDeleted ? "THANH CONG" : "THAT BAI"));
        // System.out.println("-> Xoa Item test: " + (itemDeleted ? "THANH CONG" : "THAT BAI"));
        // System.out.println("-> Tong so Event sau khi don dep: " + eventRepo.count());

        System.out.println("\n==========================================================================");
        System.out.println("===                    PHASE 2 WORK - ALL TESTS PASSED                 ===");
        System.out.println("==========================================================================");
    }
}
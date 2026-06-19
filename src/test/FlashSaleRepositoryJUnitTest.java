package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.enums.SaleStatus;
import repository.FlashSaleEventRepository;
import repository.FlashSaleItemRepository;

/**
 * JUnit Test cho FlashSaleEventRepository va FlashSaleItemRepository.
 * Su dung file tam de khong anh huong du lieu that.
 */
public class FlashSaleRepositoryJUnitTest {

    private static final String EVENT_TEST_FILE = "test_events_junit.csv";
    private static final String ITEM_TEST_FILE = "test_items_junit.csv";
    
    private FlashSaleEventRepository eventRepo;
    private FlashSaleItemRepository itemRepo;

    @Before
    public void setUp() {
        new File(EVENT_TEST_FILE).delete();
        new File(ITEM_TEST_FILE).delete();
        
        eventRepo = new FlashSaleEventRepository(EVENT_TEST_FILE);
        itemRepo = new FlashSaleItemRepository(ITEM_TEST_FILE);
    }

    @After
    public void tearDown() {
        new File(EVENT_TEST_FILE).delete();
        new File(ITEM_TEST_FILE).delete();
    }

    @Test
    public void testAddAndFindActiveEvents() {
        LocalDateTime start1 = LocalDateTime.of(2026, 6, 9, 12, 0, 0);
        LocalDateTime end1 = LocalDateTime.of(2026, 6, 9, 23, 59, 59);
        FlashSaleEvent e1 = new FlashSaleEvent("E01", "Sale 1", start1, end1, SaleStatus.ONGOING);
        eventRepo.add(e1);

        LocalDateTime start2 = LocalDateTime.of(2026, 6, 10, 12, 0, 0);
        LocalDateTime end2 = LocalDateTime.of(2026, 6, 10, 23, 59, 59);
        FlashSaleEvent e2 = new FlashSaleEvent("E02", "Sale 2", start2, end2, SaleStatus.UPCOMING);
        eventRepo.add(e2);

        // Kiem tra event dang hoat dong
        LocalDateTime mockNow = LocalDateTime.of(2026, 6, 9, 15, 0, 0);
        List<FlashSaleEvent> activeEvents = eventRepo.findActiveEvents(mockNow);
        
        assertEquals(1, activeEvents.size());
        assertEquals("E01", activeEvents.get(0).getId());
    }

    @Test
    public void testItemsByEventId() {
        FlashSaleItem i1 = new FlashSaleItem("I01", "P01", "E01", 50000, 10, 0, 1);
        FlashSaleItem i2 = new FlashSaleItem("I02", "P02", "E01", 60000, 20, 0, 2);
        FlashSaleItem i3 = new FlashSaleItem("I03", "P03", "E02", 70000, 30, 0, 3);
        
        itemRepo.add(i1);
        itemRepo.add(i2);
        itemRepo.add(i3);

        List<FlashSaleItem> event1Items = itemRepo.findItemsByEventId("E01");
        assertEquals(2, event1Items.size());
        
        List<FlashSaleItem> event2Items = itemRepo.findItemsByEventId("E02");
        assertEquals(1, event2Items.size());
        assertEquals("I03", event2Items.get(0).getId());
    }
}

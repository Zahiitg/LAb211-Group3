package test;

import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.ControllerResult;
import controller.FlashSaleController;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.enums.SaleStatus;
import repository.FlashSaleEventRepository;
import repository.FlashSaleItemRepository;

/** Unit test cho cac nghiep vu cua FlashSaleController. */
public class FlashSaleControllerJUnitTest {

    private static final String EVENT_FILE = "test_flash_controller_events.csv";
    private static final String ITEM_FILE = "test_flash_controller_items.csv";

    private FlashSaleController controller;

    @Before
    public void setUp() {
        cleanUpFiles();

        FlashSaleEventRepository eventRepo = new FlashSaleEventRepository(EVENT_FILE);
        eventRepo.add(new FlashSaleEvent(
            "E01", "Sale 1",
            LocalDateTime.of(2026, 6, 21, 8, 0),
            LocalDateTime.of(2026, 6, 21, 20, 0),
            SaleStatus.UPCOMING));
        eventRepo.add(new FlashSaleEvent(
            "E02", "Sale 2",
            LocalDateTime.of(2026, 6, 22, 8, 0),
            LocalDateTime.of(2026, 6, 22, 20, 0),
            SaleStatus.ONGOING));

        FlashSaleItemRepository itemRepo = new FlashSaleItemRepository(ITEM_FILE);
        itemRepo.add(new FlashSaleItem("I01", "P01", "E01", 100000, 10, 0, 1));
        itemRepo.add(new FlashSaleItem("I02", "P02", "E01", 200000, 20, 2, 1));
        itemRepo.add(new FlashSaleItem("I03", "P03", "E02", 300000, 30, 3, 1));

        controller = new FlashSaleController(EVENT_FILE, ITEM_FILE);
    }

    @After
    public void tearDown() {
        cleanUpFiles();
    }

    private void cleanUpFiles() {
        new File(EVENT_FILE).delete();
        new File(ITEM_FILE).delete();
    }

    @Test
    public void testListItemsReturnsOnlyItemsOfRequestedEvent() {
        ControllerResult result = controller.listItems(" E01 ");

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        List<FlashSaleItem> items = (List<FlashSaleItem>) result.getData();
        assertEquals(2, items.size());
        assertEquals("E01", items.get(0).getEventId());
        assertEquals("E01", items.get(1).getEventId());
    }

    @Test
    public void testListItemsRejectsUnknownEvent() {
        ControllerResult result = controller.listItems("E99");

        assertFalse(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    public void testStartEventUpdatesCacheAndCsv() {
        ControllerResult result = controller.startEvent("E01");

        assertTrue(result.isSuccess());
        FlashSaleEvent updated = (FlashSaleEvent) result.getData();
        assertEquals(SaleStatus.ONGOING, updated.getStatus());

        FlashSaleEvent persisted = new FlashSaleEventRepository(EVENT_FILE).getById("E01");
        assertEquals(SaleStatus.ONGOING, persisted.getStatus());
    }

    @Test
    public void testEndEventUpdatesCacheAndCsv() {
        ControllerResult result = controller.endEvent("E02");

        assertTrue(result.isSuccess());
        FlashSaleEvent updated = (FlashSaleEvent) result.getData();
        assertEquals(SaleStatus.ENDED, updated.getStatus());

        FlashSaleEvent persisted = new FlashSaleEventRepository(EVENT_FILE).getById("E02");
        assertEquals(SaleStatus.ENDED, persisted.getStatus());
    }

    @Test
    public void testStatusChangeRejectsBlankAndDuplicateRequests() {
        assertFalse(controller.startEvent(" ").isSuccess());
        assertFalse(controller.endEvent(null).isSuccess());

        assertTrue(controller.startEvent("E01").isSuccess());
        assertFalse(controller.startEvent("E01").isSuccess());
    }
}

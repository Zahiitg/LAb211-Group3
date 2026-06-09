package test;

import static org.junit.Assert.*;
import org.junit.Test;

import java.time.LocalDateTime;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.enums.SaleStatus;

/**
 * JUnit Test cho FlashSaleEvent va FlashSaleItem:
 * Kiem tra tinh toan ven du lieu khi Serialize/Deserialize CSV,
 * bao gom truong salePrice moi va xu ly LocalDateTime.
 */
public class FlashSaleEngineJUnitTest {

    // ========================================================================
    // TEST FLASH SALE EVENT
    // ========================================================================

    @Test
    public void testEventCsvRoundTrip_Ongoing() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 9, 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 9, 10, 0, 0);

        FlashSaleEvent original = new FlashSaleEvent("E-001", "Sieu Sale 6/6", start, end, SaleStatus.ONGOING);
        String csv = original.toCsvLine();

        FlashSaleEvent parsed = new FlashSaleEvent();
        parsed.fromCsvLine(csv);

        assertEquals("E-001", parsed.getId());
        assertEquals("Sieu Sale 6/6", parsed.getName());
        assertEquals(start, parsed.getStartTime());
        assertEquals(end, parsed.getEndTime());
        assertEquals(SaleStatus.ONGOING, parsed.getStatus());
    }

    @Test
    public void testEventCsvRoundTrip_Ended() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 1, 12, 0, 0);

        FlashSaleEvent original = new FlashSaleEvent("E-002", "Event Da Ket Thuc", start, end, SaleStatus.ENDED);
        String csv = original.toCsvLine();

        FlashSaleEvent parsed = new FlashSaleEvent();
        parsed.fromCsvLine(csv);

        assertEquals(SaleStatus.ENDED, parsed.getStatus());
        assertEquals("Event Da Ket Thuc", parsed.getName());
    }

    @Test
    public void testEventCsvWithCommaInName() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 9, 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 9, 10, 0, 0);

        FlashSaleEvent original = new FlashSaleEvent("E-003", "Sale 6/6, Giam Gia Soc", start, end, SaleStatus.ONGOING);
        String csv = original.toCsvLine();

        FlashSaleEvent parsed = new FlashSaleEvent();
        parsed.fromCsvLine(csv);

        assertEquals("Sale 6/6, Giam Gia Soc", parsed.getName());
    }

    @Test
    public void testEventAllStatuses() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 9, 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 9, 10, 0, 0);

        for (SaleStatus expectedStatus : SaleStatus.values()) {
            FlashSaleEvent event = new FlashSaleEvent("E-LOOP", "Test", start, end, expectedStatus);
            String csv = event.toCsvLine();

            FlashSaleEvent parsed = new FlashSaleEvent();
            parsed.fromCsvLine(csv);

            assertEquals("Status " + expectedStatus + " phai khop sau parse", expectedStatus, parsed.getStatus());
        }
    }

    // ========================================================================
    // TEST FLASH SALE ITEM (BAO GOM SALEPRICE MOI)
    // ========================================================================

    @Test
    public void testItemCsvRoundTrip() {
        FlashSaleItem original = new FlashSaleItem("FI-001", "PROD-888", "E-999", 199000.0, 150, 45, 3);
        String csv = original.toCsvLine();

        FlashSaleItem parsed = new FlashSaleItem();
        parsed.fromCsvLine(csv);

        assertEquals("FI-001", parsed.getId());
        assertEquals("PROD-888", parsed.getProductId());
        assertEquals("E-999", parsed.getEventId());
        assertEquals(199000.0, parsed.getSalePrice(), 0.001);
        assertEquals(150, parsed.getLimitedQty());
        assertEquals(45, parsed.getSoldQty());
        assertEquals(3, parsed.getVersion());
    }

    @Test
    public void testItemHasStock() {
        FlashSaleItem item = new FlashSaleItem("FI-002", "P1", "E1", 50000.0, 100, 95, 1);
        assertTrue("Con 5 slot, mua 5 phai duoc", item.hasStock(5));
        assertFalse("Con 5 slot, mua 6 phai bi tu choi", item.hasStock(6));
    }

    @Test
    public void testItemIncreaseSold() {
        FlashSaleItem item = new FlashSaleItem("FI-003", "P1", "E1", 50000.0, 100, 90, 1);
        item.increaseSold(5);
        assertEquals(95, item.getSoldQty());
    }

    @Test(expected = IllegalStateException.class)
    public void testItemOutOfStock() {
        FlashSaleItem item = new FlashSaleItem("FI-004", "P1", "E1", 50000.0, 100, 99, 1);
        item.increaseSold(5); // Chi con 1 slot, mua 5 => Out of stock
    }

    @Test
    public void testItemSalePriceZero() {
        FlashSaleItem item = new FlashSaleItem("FI-005", "P1", "E1", 0.0, 10, 0, 1);
        String csv = item.toCsvLine();

        FlashSaleItem parsed = new FlashSaleItem();
        parsed.fromCsvLine(csv);

        assertEquals(0.0, parsed.getSalePrice(), 0.001);
    }
}

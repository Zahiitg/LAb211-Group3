package test;

import java.time.LocalDateTime;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.enums.SaleStatus;

public class FlashSaleEngineTest {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("===                FLASHSALE ENGINE VISUALIZATION TEST                 ===");
        System.out.println("==========================================================================\n");

        // --- TEST SYSTEM 1: FLASH SALE EVENT ---
        System.out.println("--- [TEST 1] FlashSaleEvent: Serialization & Parsing Integrity ---");
        LocalDateTime start = LocalDateTime.of(2026, 6, 9, 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 9, 10, 0, 0);

        int eventCount = 1;
        for (SaleStatus expectedStatus : SaleStatus.values()) {
            System.out.println("\n>> Dang test Case #" + (eventCount++) + " voi Status: [" + expectedStatus + "]");
            
            // 1. Tao du lieu goc
            FlashSaleEvent originalEvent = new FlashSaleEvent("E-999", "Sieu Sale 6/6, Giam Soc Toan San", start, end, expectedStatus);
            System.out.println("   [1. Original] -> ID: " + originalEvent.getId() + " | Name: " + originalEvent.getName());

            // 2. Chuyen doi sang dong CSV
            String csvLine = originalEvent.toCsvLine();
            System.out.println("   [2. To CSV  ] -> \"" + csvLine + "\"");

            // 3. Khoi phuc tu dong CSV
            FlashSaleEvent parsedEvent = new FlashSaleEvent();
            parsedEvent.fromCsvLine(csvLine);
            System.out.println("   [3. Parsed  ] -> ID: " + parsedEvent.getId() + " | Status: " + parsedEvent.getStatus());

            // 4. Xac thuc du lieu truc quan
            try {
                assertEquals(originalEvent.getId(), parsedEvent.getId(), "ID khong khop");
                assertEquals(originalEvent.getName(), parsedEvent.getName(), "Name khong khop");
                assertEquals(originalEvent.getStartTime(), parsedEvent.getStartTime(), "StartTime khong khop");
                assertEquals(originalEvent.getEndTime(), parsedEvent.getEndTime(), "EndTime khong khop");
                assertEquals(expectedStatus, parsedEvent.getStatus(), "SaleStatus khong khop");
                System.out.println("   [ KET QUA   ] ->  SUCCESS ");
            } catch (AssertionError error) {
                System.out.println("   [ KET QUA   ] ->  FAIL: " + error.getMessage());
                System.exit(1);
            }
        }

        // --- TEST SYSTEM 2: FLASH SALE ITEM ---
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("--- [TEST 2] FlashSaleItem: Numerical Values Integrity ---");
        System.out.println("--------------------------------------------------------------------------");
        
        String inputId = "ITEM-001";
        String inputProductId = "PROD-888";
        String inputEventId = "E-999";
        double inputPrice = 199000.0;
        int inputLimitedQty = 150;
        int inputSoldQty = 45;
        int inputVersion = 3;

        // 1. Tao du lieu goc
        FlashSaleItem originalItem = new FlashSaleItem(inputId, inputProductId, inputEventId, inputPrice, inputLimitedQty, inputSoldQty, inputVersion);
        System.out.println("\n[1. Original Item] -> ID: " + originalItem.getId() + " | Price: " + originalItem.getSalePrice() + " | Qty: " + originalItem.getLimitedQty());

        // 2. Chuyen doi sang dong CSV
        String itemCsvLine = originalItem.toCsvLine();
        System.out.println("[2. To Item CSV  ] -> \"" + itemCsvLine + "\"");

        // 3. Khoi phuc tu dong CSV
        FlashSaleItem parsedItem = new FlashSaleItem();
        parsedItem.fromCsvLine(itemCsvLine);
        System.out.println("[3. Parsed Item  ] -> ID: " + parsedItem.getId() + " | Price: " + parsedItem.getSalePrice() + " | Version: " + parsedItem.getVersion());

        // 4. Xac thuc cac gia tri so va chuoi
        try {
            assertEquals(originalItem.getId(), parsedItem.getId(), "ID khong khop");
            assertEquals(originalItem.getProductId(), parsedItem.getProductId(), "ProductId khong khop");
            assertEquals(originalItem.getEventId(), parsedItem.getEventId(), "EventId khong khop");
            assertEquals(originalItem.getSalePrice(), parsedItem.getSalePrice(), "SalePrice khong khop");
            assertEquals(inputLimitedQty, parsedItem.getLimitedQty(), "limitedQty khong khop");
            assertEquals(inputSoldQty, parsedItem.getSoldQty(), "soldQty khong khop");
            assertEquals(inputVersion, parsedItem.getVersion(), "version khong khop");
            System.out.println("\n[ KET QUA CHUNG  ] ->  ALL ITEM VALUES VERIFIED ");
        } catch (AssertionError error) {
            System.out.println("\n[ KET QUA CHUNG  ] ->  VERIFICATION FAILED: " + error.getMessage());
            System.exit(1);
        }

        System.out.println("\n==========================================================================");
        System.out.println("===                    ALL ENGINE TESTS PASSED                         ===");
        System.out.println("==========================================================================");
    }

    // --- Cac ham so sanh tu che ---
    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " [expected=" + expected + " actual=" + actual + "]");
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(message + " [expected=" + expected + " actual=" + actual + "]");
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " [expected=" + expected + " actual=" + actual + "]");
        }
    }
}
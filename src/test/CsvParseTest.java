package test;

import model.*;
import model.enums.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CsvParseTest {
    public static void main(String[] args) {
        System.out.println("Starting CSV Parse Round-Trip Tests...");
        try {
            testProduct();
            testCustomer();
            testFlashSaleEvent();
            testFlashSaleItem();
            testOrder();
            testOrderDetail();
            testOrderTransaction();
            
            System.out.println("==========================================");
            System.out.println("100% test pass. All Entities successfully parsed and serialized.");
            System.out.println("==========================================");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testProduct() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Product p1 = new Product("P001", now, now, "Laptop Dell, Gaming", "Electronics", 1500.50, 10);
        String csv = p1.toCsvLine();
        Product p2 = new Product();
        p2.fromCsvLine(csv);
        
        assertEquals(p1.getId(), p2.getId(), "Product ID mismatch");
        assertEquals(p1.getName(), p2.getName(), "Product Name mismatch");
        assertEquals(p1.getCategory(), p2.getCategory(), "Product Category mismatch");
        assertEquals(p1.getPrice(), p2.getPrice(), "Product Price mismatch");
        assertEquals(p1.getStock(), p2.getStock(), "Product Stock mismatch");
    }

    private static void testCustomer() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Customer c1 = new Customer("C001", now, now, "Nguyen Van A", "0901234567", "a@gmail.com", CustTier.VIP, 150000.0, true);
        String csv = c1.toCsvLine();
        Customer c2 = new Customer();
        c2.fromCsvLine(csv);
        
        assertEquals(c1.getId(), c2.getId(), "Customer ID mismatch");
        assertEquals(c1.getFullName(), c2.getFullName(), "Customer Name mismatch");
        assertEquals(c1.getEmail(), c2.getEmail(), "Customer Email mismatch");
        assertEquals(c1.getTier(), c2.getTier(), "Customer Tier mismatch");
    }

    private static void testFlashSaleEvent() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        FlashSaleEvent e1 = new FlashSaleEvent("E001", now, now, "Black Friday", now, now.plusDays(1), SaleStatus.UPCOMING);
        String csv = e1.toCsvLine();
        FlashSaleEvent e2 = new FlashSaleEvent();
        e2.fromCsvLine(csv);
        
        assertEquals(e1.getId(), e2.getId(), "Event ID mismatch");
        assertEquals(e1.getEventName(), e2.getEventName(), "Event Name mismatch");
        assertEquals(e1.getStartTime().toString(), e2.getStartTime().toString(), "Event StartTime mismatch");
        assertEquals(e1.getEndTime().toString(), e2.getEndTime().toString(), "Event EndTime mismatch");
    }

    private static void testFlashSaleItem() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        FlashSaleItem i1 = new FlashSaleItem("FI001", now, now, "E001", "P001", 1000.0, 100, 5, 20, 2, SaleStatus.ONGOING);
        String csv = i1.toCsvLine();
        FlashSaleItem i2 = new FlashSaleItem();
        i2.fromCsvLine(csv);
        
        assertEquals(i1.getId(), i2.getId(), "Item ID mismatch");
        assertEquals(i1.getProductId(), i2.getProductId(), "Item ProductId mismatch");
        assertEquals(i1.getEventId(), i2.getEventId(), "Item EventId mismatch");
        assertEquals(i1.getLimitedQty(), i2.getLimitedQty(), "Item LimitedQty mismatch");
        assertEquals(i1.getSoldQty(), i2.getSoldQty(), "Item SoldQty mismatch");
        assertEquals(i1.getVersion(), i2.getVersion(), "Item Version mismatch");
    }

    private static void testOrder() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Order o1 = new Order("O001", now, now, "C001", "E001", 500.0, OrderStatus.SUCCESS, LockMechanism.OPTIMISTIC);
        String csv = o1.toCsvLine();
        Order o2 = new Order();
        o2.fromCsvLine(csv);
        
        assertEquals(o1.getId(), o2.getId(), "Order ID mismatch");
        assertEquals(o1.getCustomerId(), o2.getCustomerId(), "Order CustomerId mismatch");
        assertEquals(o1.getStatus(), o2.getStatus(), "Order Status mismatch");
    }

    private static void testOrderDetail() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        OrderDetail d1 = new OrderDetail("OD001", now, now, "O001", "FI001", 2, 250.0, 500.0);
        String csv = d1.toCsvLine();
        OrderDetail d2 = new OrderDetail();
        d2.fromCsvLine(csv);
        
        assertEquals(d1.getId(), d2.getId(), "Detail ID mismatch");
        assertEquals(d1.getOrderId(), d2.getOrderId(), "Detail OrderId mismatch");
        assertEquals(d1.getFlashSaleItemId(), d2.getFlashSaleItemId(), "Detail FlashSaleItemId mismatch");
        assertEquals(d1.getQuantity(), d2.getQuantity(), "Detail Quantity mismatch");
    }

    private static void testOrderTransaction() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        OrderTransaction t1 = new OrderTransaction("T001", now, now, "O001", LockMechanism.OPTIMISTIC, 3, 150L, true);
        String csv = t1.toCsvLine();
        OrderTransaction t2 = new OrderTransaction();
        t2.fromCsvLine(csv);
        
        assertEquals(t1.getId(), t2.getId(), "Transaction ID mismatch");
        assertEquals(t1.getOrderId(), t2.getOrderId(), "Transaction OrderId mismatch");
        assertEquals(t1.getLockMechanism(), t2.getLockMechanism(), "Transaction LockMechanism mismatch");
        assertEquals(t1.getRetryCount(), t2.getRetryCount(), "Transaction RetryCount mismatch");
        assertEquals(t1.getProcessingTimeMs(), t2.getProcessingTimeMs(), "Transaction ProcessingTimeMs mismatch");
        assertEquals(t1.isSuccess(), t2.isSuccess(), "Transaction Success mismatch");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new RuntimeException(message + " - Expected: " + expected + ", Actual: " + actual);
    }
}

package test;

import static org.junit.Assert.*;
import org.junit.Test;

import model.Order;
import model.OrderDetail;
import model.OrderTransaction;
import model.enums.LockMechanism;
import model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Unit Test cho 3 Models cua TV4: Order, OrderDetail, OrderTransaction.
 * 
 * Trong tam kiem tra:
 * - Serialize (toCsvLine): Chuyen doi Object → chuoi CSV dung dinh dang.
 * - Deserialize (fromCsvLine): Doc chuoi CSV → khoi phuc Object voi du lieu chinh xac.
 * - RoundTrip: Object → CSV → Object phai cho ra ket qua giong het ban goc.
 *
 * @author Thanh vien 4 - Orders & Transactions
 */
public class OrderModelJUnitTest {

    // ========================================================================
    // TEST ORDER
    // ========================================================================

    @Test
    public void testOrderCsvRoundTrip() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 19, 14, 30, 0);
        Order order = new Order("ORD00001", "C00001", now, OrderStatus.PENDING);

        // Serialize
        String csv = order.toCsvLine();
        assertNotNull("CSV line khong duoc null", csv);
        assertTrue("CSV phai chua ORD00001", csv.contains("ORD00001"));
        assertTrue("CSV phai chua C00001", csv.contains("C00001"));
        assertTrue("CSV phai chua PENDING", csv.contains("PENDING"));

        // Deserialize
        Order parsed = new Order();
        parsed.fromCsvLine(csv);

        assertEquals("ORD00001", parsed.getId());
        assertEquals("C00001", parsed.getCustomerId());
        assertEquals(OrderStatus.PENDING, parsed.getStatus());
        assertEquals(now, parsed.getOrderTime());
    }

    @Test
    public void testOrderAllStatuses() {
        // Kiem tra tat ca trang thai OrderStatus deu duoc serialize/deserialize dung
        OrderStatus[] allStatuses = { OrderStatus.PENDING, OrderStatus.VERIFIED,
                                       OrderStatus.COMPLETED, OrderStatus.FAILED,
                                       OrderStatus.CANCELLED };

        for (OrderStatus status : allStatuses) {
            Order order = new Order("ORD_TEST", "C00001",
                                     LocalDateTime.of(2026, 1, 1, 0, 0), status);
            String csv = order.toCsvLine();
            Order parsed = new Order();
            parsed.fromCsvLine(csv);

            assertEquals("Trang thai " + status + " phai khop sau RoundTrip",
                         status, parsed.getStatus());
        }
    }

    @Test
    public void testOrderNullOrderTime() {
        Order order = new Order("ORD00002", "C00005", null, OrderStatus.FAILED);
        String csv = order.toCsvLine();

        // Khi orderTime la null, CSV phai co truong rong ""
        Order parsed = new Order();
        parsed.fromCsvLine(csv);

        assertEquals("ORD00002", parsed.getId());
        assertNull("OrderTime phai la null khi truong CSV rong", parsed.getOrderTime());
        assertEquals(OrderStatus.FAILED, parsed.getStatus());
    }

    // ========================================================================
    // TEST ORDER DETAIL
    // ========================================================================

    @Test
    public void testOrderDetailCsvRoundTrip() {
        OrderDetail detail = new OrderDetail("OD00001", "ORD00001", "FSI001", 2, 150000.5);

        // Serialize
        String csv = detail.toCsvLine();
        assertNotNull("CSV line khong duoc null", csv);

        // Deserialize
        OrderDetail parsed = new OrderDetail();
        parsed.fromCsvLine(csv);

        assertEquals("OD00001", parsed.getId());
        assertEquals("ORD00001", parsed.getOrderId());
        assertEquals("FSI001", parsed.getFlashSaleItemId());
        assertEquals(2, parsed.getQuantity());
        assertEquals(150000.5, parsed.getPriceAtPurchase(), 0.001);
    }

    @Test
    public void testOrderDetailLargeValues() {
        // Kiem tra voi so luong lon va gia tien lon (tranh loi tran so)
        OrderDetail detail = new OrderDetail("OD00099", "ORD00050", "FSI999",
                                              999, 99999999.99);
        String csv = detail.toCsvLine();
        OrderDetail parsed = new OrderDetail();
        parsed.fromCsvLine(csv);

        assertEquals(999, parsed.getQuantity());
        assertEquals(99999999.99, parsed.getPriceAtPurchase(), 0.01);
    }

    // ========================================================================
    // TEST ORDER TRANSACTION
    // ========================================================================

    @Test
    public void testOrderTransactionCsvRoundTrip() {
        OrderTransaction tx = new OrderTransaction(
            "TX00001", "ORD00001", LockMechanism.FILE_LOCK, 3, 125L, true
        );

        // Serialize
        String csv = tx.toCsvLine();
        assertNotNull("CSV line khong duoc null", csv);
        assertTrue("CSV phai chua FILE_LOCK", csv.contains("FILE_LOCK"));
        assertTrue("CSV phai chua TRUE", csv.contains("TRUE"));

        // Deserialize
        OrderTransaction parsed = new OrderTransaction();
        parsed.fromCsvLine(csv);

        assertEquals("TX00001", parsed.getId());
        assertEquals("ORD00001", parsed.getOrderId());
        assertEquals(LockMechanism.FILE_LOCK, parsed.getLockMechanism());
        assertEquals(3, parsed.getRetryCount());
        assertEquals(125L, parsed.getProcessingTimeMs());
        assertTrue("Giao dich phai la thanh cong", parsed.isSuccess());
    }

    @Test
    public void testOrderTransactionAllLockMechanisms() {
        // Kiem tra tat ca LockMechanism deu duoc serialize/deserialize dung
        LockMechanism[] allMechanisms = { LockMechanism.NO_LOCK,
                                            LockMechanism.SYNCHRONIZED,
                                            LockMechanism.FILE_LOCK,
                                            LockMechanism.OPTIMISTIC_LOCK };

        for (LockMechanism mechanism : allMechanisms) {
            OrderTransaction tx = new OrderTransaction(
                "TX_TEST", "ORD_TEST", mechanism, 0, 50L, true
            );
            String csv = tx.toCsvLine();
            OrderTransaction parsed = new OrderTransaction();
            parsed.fromCsvLine(csv);

            assertEquals("LockMechanism " + mechanism + " phai khop sau RoundTrip",
                         mechanism, parsed.getLockMechanism());
        }
    }

    @Test
    public void testOrderTransactionFailedCase() {
        // Kiem tra giao dich that bai (success = false)
        OrderTransaction tx = new OrderTransaction(
            "TX00099", "ORD00050", LockMechanism.OPTIMISTIC_LOCK, 5, 500L, false
        );
        String csv = tx.toCsvLine();
        OrderTransaction parsed = new OrderTransaction();
        parsed.fromCsvLine(csv);

        assertFalse("Giao dich phai la THAT BAI", parsed.isSuccess());
        assertEquals(5, parsed.getRetryCount());
        assertEquals(500L, parsed.getProcessingTimeMs());
    }
}

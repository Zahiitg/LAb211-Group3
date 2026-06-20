package test;

import model.OrderTransaction;
import model.enums.LockMechanism;
import org.junit.Test;

import static org.junit.Assert.*;

public class OrderTransactionTest {

    @Test
    public void testConstructorAndGetter() {

        OrderTransaction transaction = new OrderTransaction(
                "T001",
                "O001",
                LockMechanism.SYNCHRONIZED,
                3,
                1000,
                true);

        assertEquals("T001", transaction.getId());
        assertEquals("O001", transaction.getOrderId());
        assertEquals(LockMechanism.SYNCHRONIZED,
                transaction.getLockMechanism());
        assertEquals(3, transaction.getRetryCount());
        assertEquals(1000, transaction.getProcessingTimeMs());
        assertTrue(transaction.isSuccess());
    }

    @Test
    public void testSetter() {

        OrderTransaction transaction = new OrderTransaction();

        transaction.setId("T002");
        transaction.setOrderId("O002");
        transaction.setLockMechanism(
                LockMechanism.OPTIMISTIC_LOCK);
        transaction.setRetryCount(5);
        transaction.setProcessingTimeMs(2000);
        transaction.setSuccess(false);

        assertEquals("T002", transaction.getId());
        assertEquals("O002", transaction.getOrderId());

        assertEquals(
                LockMechanism.OPTIMISTIC_LOCK,
                transaction.getLockMechanism());

        assertEquals(5, transaction.getRetryCount());
        assertEquals(2000, transaction.getProcessingTimeMs());

        assertFalse(transaction.isSuccess());
    }

    @Test
    public void testToCsvLine() {

        OrderTransaction transaction = new OrderTransaction(
                "T001",
                "O001",
                LockMechanism.SYNCHRONIZED,
                3,
                1000,
                true);

        String expected = "T001,O001,SYNCHRONIZED,3,1000,TRUE";

        assertEquals(expected,
                transaction.toCsvLine());
    }

    @Test
    public void testFromCsvLine() {

        OrderTransaction transaction = new OrderTransaction();

        transaction.fromCsvLine(
                "T001,O001,SYNCHRONIZED,3,1000,true");

        assertEquals("T001", transaction.getId());
        assertEquals("O001", transaction.getOrderId());

        assertEquals(
                LockMechanism.SYNCHRONIZED,
                transaction.getLockMechanism());

        assertEquals(3, transaction.getRetryCount());
        assertEquals(1000, transaction.getProcessingTimeMs());

        assertTrue(transaction.isSuccess());
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
                    "TX_TEST", "ORD_TEST", mechanism, 0, 50L, true);
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
                "TX00099", "ORD00050", LockMechanism.OPTIMISTIC_LOCK, 5, 500L, false);
        String csv = tx.toCsvLine();
        OrderTransaction parsed = new OrderTransaction();
        parsed.fromCsvLine(csv);

        assertFalse("Giao dich phai la THAT BAI", parsed.isSuccess());
        assertEquals(5, parsed.getRetryCount());
        assertEquals(500L, parsed.getProcessingTimeMs());
    }
}
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

        String expected = "T001,O001,SYNCHRONIZED,3,1000,true";

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
}
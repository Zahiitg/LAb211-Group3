package test;

import model.Order;
import model.enums.OrderStatus;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class OrderTest {

    @Test
    public void testConstructorAndGetter() {
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);

        Order order = new Order(
                "O001",
                "C001",
                time,
                OrderStatus.PENDING);

        assertEquals("O001", order.getId());
        assertEquals("C001", order.getCustomerId());
        assertEquals(time, order.getOrderTime());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    public void testSetter() {
        Order order = new Order();

        LocalDateTime time = LocalDateTime.now();

        order.setId("O002");
        order.setCustomerId("C002");
        order.setOrderTime(time);
        order.setStatus(OrderStatus.COMPLETED);

        assertEquals("O002", order.getId());
        assertEquals("C002", order.getCustomerId());
        assertEquals(time, order.getOrderTime());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    public void testToCsvLine() {
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);

        Order order = new Order("O001", "C001", time, OrderStatus.PENDING);

        String expected = "O001,C001,2025-01-01T10:00:00,PENDING";

        assertEquals(expected, order.toCsvLine());
    }

    @Test
    public void testFromCsvLine() {
        Order order = new Order();

        order.fromCsvLine(
                "O001,C001,2025-01-01T10:00:00,PENDING");

        assertEquals("O001", order.getId());
        assertEquals("C001", order.getCustomerId());
        assertEquals(
                LocalDateTime.of(2025, 1, 1, 10, 0),
                order.getOrderTime());

        assertEquals(OrderStatus.PENDING,
                order.getStatus());
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
}
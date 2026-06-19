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
}
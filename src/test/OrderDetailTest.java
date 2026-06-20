package test;

import model.OrderDetail;
import org.junit.Test;

import static org.junit.Assert.*;

public class OrderDetailTest {

    @Test
    public void testConstructorAndGetter() {

        OrderDetail detail = new OrderDetail(
                "D001",
                "O001",
                "FS001",
                2,
                99.5);

        assertEquals("D001", detail.getId());
        assertEquals("O001", detail.getOrderId());
        assertEquals("FS001", detail.getFlashSaleItemId());
        assertEquals(2, detail.getQuantity());
        assertEquals(99.5, detail.getPriceAtPurchase(), 0.001);
    }

    @Test
    public void testSetter() {

        OrderDetail detail = new OrderDetail();

        detail.setId("D002");
        detail.setOrderId("O002");
        detail.setFlashSaleItemId("FS002");
        detail.setQuantity(5);
        detail.setPriceAtPurchase(150.0);

        assertEquals("D002", detail.getId());
        assertEquals("O002", detail.getOrderId());
        assertEquals("FS002", detail.getFlashSaleItemId());
        assertEquals(5, detail.getQuantity());
        assertEquals(150.0, detail.getPriceAtPurchase(), 0.001);
    }

    @Test
    public void testToCsvLine() {

        OrderDetail detail = new OrderDetail(
                "D001",
                "O001",
                "FS001",
                2,
                99.5);

        String expected = "D001,O001,FS001,2,99.5";

        assertEquals(expected, detail.toCsvLine());
    }

    @Test
    public void testFromCsvLine() {

        OrderDetail detail = new OrderDetail();

        detail.fromCsvLine(
                "D001,O001,FS001,2,99.5");

        assertEquals("D001", detail.getId());
        assertEquals("O001", detail.getOrderId());
        assertEquals("FS001", detail.getFlashSaleItemId());
        assertEquals(2, detail.getQuantity());
        assertEquals(99.5, detail.getPriceAtPurchase(), 0.001);
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
}
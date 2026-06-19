package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.ControllerResult;
import controller.OrderController;
import model.FlashSaleItem;
import model.Order;
import model.OrderDetail;
import model.OrderTransaction;
import model.enums.LockMechanism;
import model.enums.OrderStatus;

import java.io.File;
import java.util.List;

/**
 * Unit Test cho OrderController - Kiem tra luong dat hang DON LUONG (Single-thread).
 *
 * Muc tieu: Dam bao 1 Customer dat mua 1 san pham Flash Sale thanh cong,
 * va toan bo du lieu (Order, OrderDetail, OrderTransaction, FlashSaleItem)
 * deu duoc cap nhat chinh xac.
 *
 * @author Thanh vien 4 - Orders & Transactions
 */
public class OrderControllerJUnitTest {

    // File test rieng biet, KHONG dung chung file CSV that
    private static final String TEST_ORDERS = "test_orders.csv";
    private static final String TEST_DETAILS = "test_order_details.csv";
    private static final String TEST_TX = "test_order_tx.csv";
    private static final String TEST_ITEMS = "test_flash_items_order.csv";

    private OrderController controller;

    @Before
    public void setUp() {
        // Xoa het file cu de dam bao moi test chay sach
        deleteTestFiles();

        // Tao file flash_items voi 1 san pham: 10 cai, gia 99000, chua ban cai nao
        createTestFlashSaleItem();

        // Khoi tao controller voi file test
        controller = new OrderController(TEST_ORDERS, TEST_DETAILS, TEST_TX, TEST_ITEMS);
    }

    @After
    public void tearDown() {
        deleteTestFiles();
        // Xoa ca file .lock neu co
        new File(TEST_ITEMS + ".lock").delete();
    }

    // =====================================================================
    // HELPER: Tao du lieu Flash Sale Item cho test
    // =====================================================================

    private void createTestFlashSaleItem() {
        // Tao 1 FlashSaleItem truc tiep vao file CSV
        // FlashSaleItemRepository dung hasHeader=true, nen can dong header o dong dau
        // ID: FSI001, ProductId: P01, EventId: EVT01, Gia: 99000, SL gioi han: 10, Da ban: 0, Version: 1
        try (java.io.FileWriter fw = new java.io.FileWriter(TEST_ITEMS)) {
            fw.write("id,productId,eventId,salePrice,limitedQty,soldQty,version\n");
            fw.write("FSI001,P01,EVT01,99000.0,10,0,1\n");
        } catch (java.io.IOException e) {
            fail("Khong tao duoc file test: " + e.getMessage());
        }
    }

    private void deleteTestFiles() {
        new File(TEST_ORDERS).delete();
        new File(TEST_DETAILS).delete();
        new File(TEST_TX).delete();
        new File(TEST_ITEMS).delete();
    }

    // =====================================================================
    // TEST DAT HANG THANH CONG (Single-thread - NoLock)
    // =====================================================================

    @Test
    public void testPlaceOrderSingleThreadSuccess() {
        ControllerResult result = controller.placeOrderSingleThread("C00001", "FSI001", 2);

        // Kiem tra ket qua phai THANH CONG
        assertTrue("Dat hang phai thanh cong", result.isSuccess());
        assertNotNull("Phai co du lieu Order tra ve", result.getData());

        // Kiem tra Order duoc tao dung
        Order order = (Order) result.getData();
        assertEquals("ORD00001", order.getId());
        assertEquals("C00001", order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull("Order phai co thoi gian dat", order.getOrderTime());
    }

    @Test
    public void testPlaceOrderCreatesOrderDetail() {
        controller.placeOrderSingleThread("C00001", "FSI001", 3);

        // Kiem tra OrderDetail da duoc tao
        List<OrderDetail> details = controller.getDetailRepo().getAll();
        assertEquals("Phai co dung 1 OrderDetail", 1, details.size());

        OrderDetail detail = details.get(0);
        assertEquals("OD00001", detail.getId());
        assertEquals("ORD00001", detail.getOrderId());
        assertEquals("FSI001", detail.getFlashSaleItemId());
        assertEquals(3, detail.getQuantity());
        assertEquals(99000.0, detail.getPriceAtPurchase(), 0.01);
    }

    @Test
    public void testPlaceOrderCreatesTransaction() {
        controller.placeOrderSingleThread("C00001", "FSI001", 1);

        // Kiem tra OrderTransaction da duoc ghi nhat ky
        List<OrderTransaction> txList = controller.getTxRepo().getAll();
        assertEquals("Phai co dung 1 Transaction", 1, txList.size());

        OrderTransaction tx = txList.get(0);
        assertEquals("TX00001", tx.getId());
        assertEquals("ORD00001", tx.getOrderId());
        assertEquals(LockMechanism.NO_LOCK, tx.getLockMechanism());
        assertTrue("Giao dich phai thanh cong", tx.isSuccess());
        assertTrue("Thoi gian xu ly phai >= 0ms", tx.getProcessingTimeMs() >= 0);
    }

    @Test
    public void testPlaceOrderDeductsStock() {
        // Ban dau: limitedQty=10, soldQty=0
        controller.placeOrderSingleThread("C00001", "FSI001", 3);

        // Sau khi mua 3 cai: soldQty phai tang len 3
        FlashSaleItem item = controller.getItemRepo().getById("FSI001");
        assertNotNull("Item phai con ton tai", item);
        assertEquals("Da ban phai la 3", 3, item.getSoldQty());
        assertEquals("Gioi han van la 10", 10, item.getLimitedQty());
    }

    // =====================================================================
    // TEST MUA NHIEU LAN LIEN TIEP
    // =====================================================================

    @Test
    public void testPlaceMultipleOrdersSequentially() {
        // Mua 3 lan lien tiep: 3 + 3 + 3 = 9 (con du 1)
        ControllerResult r1 = controller.placeOrderSingleThread("C00001", "FSI001", 3);
        ControllerResult r2 = controller.placeOrderSingleThread("C00002", "FSI001", 3);
        ControllerResult r3 = controller.placeOrderSingleThread("C00003", "FSI001", 3);

        assertTrue("Lan 1 phai thanh cong (3/10)", r1.isSuccess());
        assertTrue("Lan 2 phai thanh cong (6/10)", r2.isSuccess());
        assertTrue("Lan 3 phai thanh cong (9/10)", r3.isSuccess());

        // Lan 4: Mua 2 cai (9+2=11 > 10) → THAT BAI
        ControllerResult r4 = controller.placeOrderSingleThread("C00004", "FSI001", 2);
        assertFalse("Lan 4 phai THAT BAI (vuot gioi han)", r4.isSuccess());

        // Kiem tra kho: da ban = 9
        FlashSaleItem item = controller.getItemRepo().getById("FSI001");
        assertEquals("Da ban phai la 9 (3+3+3)", 9, item.getSoldQty());
    }

    @Test
    public void testPlaceOrderExactlyFullStock() {
        // Mua dung het 10 cai (vua khit)
        ControllerResult r = controller.placeOrderSingleThread("C00001", "FSI001", 10);
        assertTrue("Mua dung 10/10 phai thanh cong", r.isSuccess());

        // Mua them 1 cai nua → THAT BAI
        ControllerResult rFail = controller.placeOrderSingleThread("C00002", "FSI001", 1);
        assertFalse("Mua them khi da het hang phai THAT BAI", rFail.isSuccess());
    }

    // =====================================================================
    // TEST VALIDATION - THAT BAI
    // =====================================================================

    @Test
    public void testPlaceOrderInvalidCustomerId() {
        ControllerResult r1 = controller.placeOrderSingleThread("", "FSI001", 1);
        assertFalse("CustomerId rong phai THAT BAI", r1.isSuccess());

        ControllerResult r2 = controller.placeOrderSingleThread(null, "FSI001", 1);
        assertFalse("CustomerId null phai THAT BAI", r2.isSuccess());
    }

    @Test
    public void testPlaceOrderInvalidItemId() {
        ControllerResult r1 = controller.placeOrderSingleThread("C00001", "", 1);
        assertFalse("ItemId rong phai THAT BAI", r1.isSuccess());

        ControllerResult r2 = controller.placeOrderSingleThread("C00001", "NON_EXISTENT", 1);
        assertFalse("ItemId khong ton tai phai THAT BAI", r2.isSuccess());
    }

    @Test
    public void testPlaceOrderInvalidQuantity() {
        ControllerResult r1 = controller.placeOrderSingleThread("C00001", "FSI001", 0);
        assertFalse("So luong = 0 phai THAT BAI", r1.isSuccess());

        ControllerResult r2 = controller.placeOrderSingleThread("C00001", "FSI001", -5);
        assertFalse("So luong am phai THAT BAI", r2.isSuccess());
    }

    @Test
    public void testPlaceOrderExceedStock() {
        // Kho chi co 10, mua 11 → THAT BAI
        ControllerResult r = controller.placeOrderSingleThread("C00001", "FSI001", 11);
        assertFalse("Mua vuot kho (11 > 10) phai THAT BAI", r.isSuccess());
    }

    // =====================================================================
    // TEST GIAO DICH THAT BAI CUNG GHI NHAT KY
    // =====================================================================

    @Test
    public void testFailedOrderLogsTransaction() {
        // Mua vuot kho → That bai, nhung van phai ghi nhat ky
        controller.placeOrderSingleThread("C00001", "FSI001", 99);

        List<OrderTransaction> txList = controller.getTxRepo().getAll();
        assertEquals("Phai co 1 Transaction (ke ca that bai)", 1, txList.size());

        OrderTransaction tx = txList.get(0);
        assertFalse("Giao dich phai la THAT BAI", tx.isSuccess());
        assertEquals(LockMechanism.NO_LOCK, tx.getLockMechanism());
    }

    // =====================================================================
    // TEST DAT HANG VOI CAC CO CHE LOCK KHAC
    // =====================================================================

    @Test
    public void testPlaceOrderWithSynchronized() {
        ControllerResult r = controller.placeOrderWithSynchronized("C00001", "FSI001", 2);
        assertTrue("Dat hang voi Synchronized phai thanh cong", r.isSuccess());

        OrderTransaction tx = controller.getTxRepo().getAll().get(0);
        assertEquals(LockMechanism.SYNCHRONIZED, tx.getLockMechanism());
    }

    @Test
    public void testPlaceOrderWithFileLock() {
        ControllerResult r = controller.placeOrderWithFileLock("C00001", "FSI001", 2);
        assertTrue("Dat hang voi FileLock phai thanh cong", r.isSuccess());

        OrderTransaction tx = controller.getTxRepo().getAll().get(0);
        assertEquals(LockMechanism.FILE_LOCK, tx.getLockMechanism());
    }

    @Test
    public void testPlaceOrderWithOptimisticLock() {
        ControllerResult r = controller.placeOrderWithOptimisticLock("C00001", "FSI001", 2);
        assertTrue("Dat hang voi OptimisticLock phai thanh cong", r.isSuccess());

        OrderTransaction tx = controller.getTxRepo().getAll().get(0);
        assertEquals(LockMechanism.OPTIMISTIC_LOCK, tx.getLockMechanism());
    }
}

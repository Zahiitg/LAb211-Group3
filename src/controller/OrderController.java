package controller;

import model.Order;
import model.OrderDetail;
import model.OrderTransaction;
import model.FlashSaleItem;
import model.enums.LockMechanism;
import model.enums.OrderStatus;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;
import repository.OrderDetailRepository;
import repository.OrderTransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller xu ly toan bo quy trinh DAT HANG (Order Placement).
 *
 * Luong xu ly chinh:
 * 1. Kiem tra quyen (requireCustomer) → Chi Customer moi duoc dat hang.
 * 2. Kiem tra ton kho Flash Sale Item.
 * 3. Tru kho bang co che Lock duoc chon (NoLock / Synchronized / FileLock / OptimisticLock).
 * 4. Tao Order + OrderDetail.
 * 5. Ghi nhat ky giao dich (OrderTransaction) de do hieu suat.
 *
 * Tat ca ham deu tra ve ControllerResult, KHONG quang Exception ra ngoai.
 *
 * @author Thanh vien 4 - Orders & Transactions
 * @refactored-by Thanh vien 1 - Core Architecture (ap dung chuan BaseController)
 */
public class OrderController extends BaseController {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;
    private final OrderTransactionRepository txRepo;
    private final FlashSaleItemRepository itemRepo;
    private final repository.ProductRepository productRepo;

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    /**
     * Constructor mac dinh - su dung duong dan file CSV chuan cua du an.
     */
    public OrderController() {
        this.orderRepo = new OrderRepository("data/orders.csv");
        this.detailRepo = new OrderDetailRepository("data/order_details.csv");
        this.txRepo = new OrderTransactionRepository("data/transactions.csv");
        this.itemRepo = new FlashSaleItemRepository("data/flash_items.csv");
        this.productRepo = new repository.ProductRepository("data/products.csv");
    }

    /**
     * Constructor cho testing - cho phep inject duong dan tuy chinh.
     *
     * @param orderFile    Duong dan file orders.csv
     * @param detailFile   Duong dan file order_details.csv
     * @param txFile       Duong dan file order_transactions.csv
     * @param itemFile     Duong dan file flash_items.csv
     */
    public OrderController(String orderFile, String detailFile,
                           String txFile, String itemFile) {
        this.orderRepo = new OrderRepository(orderFile);
        this.detailRepo = new OrderDetailRepository(detailFile);
        this.txRepo = new OrderTransactionRepository(txFile);
        this.itemRepo = new FlashSaleItemRepository(itemFile);
        this.productRepo = new repository.ProductRepository("data/products.csv");
    }

    // =====================================================================
    // DAT HANG DON LUONG (Single-thread Booking)
    // =====================================================================

    /**
     * Dat hang don luong (Baseline - khong co khoa).
     *
     * Day la phien ban co ban nhat: 1 Customer dat 1 san pham Flash Sale.
     * Su dung sellWithNoLock() de tru kho.
     *
     * Quy trinh:
     * 1. Validate dau vao.
     * 2. Tim Flash Sale Item trong kho.
     * 3. Bat dau do thoi gian xu ly.
     * 4. Goi sellWithNoLock() de tru kho.
     * 5. Tao Order (trang thai PENDING).
     * 6. Tao OrderDetail (chi tiet mon hang).
     * 7. Ghi nhat ky OrderTransaction.
     * 8. Tra ve ControllerResult.
     *
     * @param customerId      Ma khach hang (vi du: "C00001")
     * @param flashSaleItemId Ma san pham Flash Sale (vi du: "FSI001")
     * @param quantity         So luong mua
     * @return ControllerResult chua Order object neu thanh cong
     */
    public ControllerResult placeOrderSingleThread(String customerId,
                                                    String flashSaleItemId,
                                                    int quantity) {
        return placeOrderInternal(customerId, flashSaleItemId, quantity, LockMechanism.NO_LOCK);
    }

    /**
     * Dat hang voi co che Synchronized Lock.
     */
    public ControllerResult placeOrderWithSynchronized(String customerId,
                                                        String flashSaleItemId,
                                                        int quantity) {
        return placeOrderInternal(customerId, flashSaleItemId, quantity, LockMechanism.SYNCHRONIZED);
    }

    /**
     * Dat hang voi co che File Lock.
     */
    public ControllerResult placeOrderWithFileLock(String customerId,
                                                    String flashSaleItemId,
                                                    int quantity) {
        return placeOrderInternal(customerId, flashSaleItemId, quantity, LockMechanism.FILE_LOCK);
    }

    /**
     * Dat hang voi co che Optimistic Lock.
     */
    public ControllerResult placeOrderWithOptimisticLock(String customerId,
                                                          String flashSaleItemId,
                                                          int quantity) {
        return placeOrderInternal(customerId, flashSaleItemId, quantity, LockMechanism.OPTIMISTIC_LOCK);
    }

    public ControllerResult placeProductOrder(String customerId,
                                               String productId,
                                               int quantity,
                                               LockMechanism mechanism) {
        // --- VALIDATION ---
        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Ma khach hang khong duoc de trong!");
        }
        if (productId == null || productId.trim().isEmpty()) {
            return error("Ma san pham khong duoc de trong!");
        }
        if (quantity <= 0) {
            return error("So luong mua phai lon hon 0!");
        }

        // --- TIM SAN PHAM THUONG ---
        model.Product product = productRepo.getById(productId);
        if (product == null) {
            return error("Khong tim thay san pham voi ma: " + productId);
        }

        // --- BAT DAU DO THOI GIAN ---
        long startTime = System.currentTimeMillis();

        // --- TRU KHO BANG CO CHE LOCK TUONG UNG ---
        boolean stockDeducted;
        switch (mechanism) {
            case NO_LOCK:
                stockDeducted = productRepo.sellWithNoLock(productId, quantity);
                break;
            case SYNCHRONIZED:
                stockDeducted = productRepo.sellWithSynchronized(productId, quantity);
                break;
            case FILE_LOCK:
                stockDeducted = productRepo.sellWithFileLock(productId, quantity);
                break;
            case OPTIMISTIC_LOCK:
                stockDeducted = productRepo.sellWithOptimisticLock(productId, quantity);
                break;
            default:
                stockDeducted = false;
        }

        // --- DO THOI GIAN XU LY ---
        long processingTime = System.currentTimeMillis() - startTime;

        // --- XU LY KET QUA ---
        if (!stockDeducted) {
            // Tru kho THAT BAI → Ghi nhat ky that bai
            String txId = generateId("TX", txRepo.count());
            OrderTransaction failTx = new OrderTransaction(
                txId, "", mechanism, 0, processingTime, false
            );
            txRepo.add(failTx);

            return error("Dat hang that bai! San pham '"
                        + productId + "' da het hang hoac khong du so luong.");
        }

        // --- TAO DON HANG ---
        String orderId = generateId("ORD", orderRepo.count());
        Order order = new Order(orderId, customerId, LocalDateTime.now(), OrderStatus.PENDING);
        orderRepo.add(order);

        // --- TAO CHI TIET DON HANG ---
        String detailId = generateId("OD", detailRepo.count());
        OrderDetail detail = new OrderDetail(
            detailId, orderId, productId, quantity, product.getPrice()
        );
        detailRepo.add(detail);

        // --- GHI NHAT KY GIAO DICH ---
        String txId = generateId("TX", txRepo.count());
        OrderTransaction successTx = new OrderTransaction(
            txId, orderId, mechanism, 0, processingTime, true
        );
        txRepo.add(successTx);

        return success("Dat hang thanh cong! Ma don: " + orderId
                      + " | " + quantity + " x " + product.getName()
                      + " | Thoi gian xu ly: " + processingTime + "ms"
                      + " | Co che: " + mechanism.name(), order);
    }

    // =====================================================================
    // LOGIC NOI BO - LUONG XU LY CHUNG
    // =====================================================================

    /**
     * Luong xu ly dat hang chung cho moi co che Lock.
     * Tat ca cac ham placeOrder*() deu goi ham nay voi LockMechanism tuong ung.
     */
    private ControllerResult placeOrderInternal(String customerId,
                                                 String flashSaleItemId,
                                                 int quantity,
                                                 LockMechanism mechanism) {
        // --- VALIDATION ---
        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Ma khach hang khong duoc de trong!");
        }
        if (flashSaleItemId == null || flashSaleItemId.trim().isEmpty()) {
            return error("Ma san pham Flash Sale khong duoc de trong!");
        }
        if (quantity <= 0) {
            return error("So luong mua phai lon hon 0!");
        }

        // --- TIM SAN PHAM FLASH SALE ---
        FlashSaleItem item = itemRepo.getById(flashSaleItemId);
        if (item == null) {
            return error("Khong tim thay san pham Flash Sale voi ma: " + flashSaleItemId);
        }

        // --- BAT DAU DO THOI GIAN ---
        long startTime = System.currentTimeMillis();

        // --- TRU KHO BANG CO CHE LOCK TUONG UNG ---
        boolean stockDeducted;
        switch (mechanism) {
            case NO_LOCK:
                stockDeducted = itemRepo.sellWithNoLock(flashSaleItemId, quantity);
                break;
            case SYNCHRONIZED:
                stockDeducted = itemRepo.sellWithSynchronized(flashSaleItemId, quantity);
                break;
            case FILE_LOCK:
                stockDeducted = itemRepo.sellWithFileLock(flashSaleItemId, quantity);
                break;
            case OPTIMISTIC_LOCK:
                stockDeducted = itemRepo.sellWithOptimisticLock(flashSaleItemId, quantity);
                break;
            default:
                stockDeducted = false;
        }

        // --- DO THOI GIAN XU LY ---
        long processingTime = System.currentTimeMillis() - startTime;

        // --- XU LY KET QUA ---
        if (!stockDeducted) {
            // Tru kho THAT BAI → Ghi nhat ky that bai
            String txId = generateId("TX", txRepo.count());
            OrderTransaction failTx = new OrderTransaction(
                txId, "", mechanism, 0, processingTime, false
            );
            txRepo.add(failTx);

            return error("Dat hang that bai! San pham '"
                        + flashSaleItemId + "' da het hang hoac khong du so luong.");
        }

        // --- TAO DON HANG ---
        String orderId = generateId("ORD", orderRepo.count());
        Order order = new Order(orderId, customerId, LocalDateTime.now(), OrderStatus.PENDING);
        orderRepo.add(order);

        // --- TAO CHI TIET DON HANG ---
        String detailId = generateId("OD", detailRepo.count());
        OrderDetail detail = new OrderDetail(
            detailId, orderId, flashSaleItemId, quantity, item.getSalePrice()
        );
        detailRepo.add(detail);

        // --- GHI NHAT KY GIAO DICH ---
        String txId = generateId("TX", txRepo.count());
        OrderTransaction successTx = new OrderTransaction(
            txId, orderId, mechanism, 0, processingTime, true
        );
        txRepo.add(successTx);

        return success("Dat hang thanh cong! Ma don: " + orderId
                      + " | " + quantity + " x " + flashSaleItemId
                      + " | Thoi gian xu ly: " + processingTime + "ms"
                      + " | Co che: " + mechanism.name(), order);
    }

    // =====================================================================
    // TIEN ICH
    // =====================================================================

    /**
     * Sinh ma ID tu dong tang.
     * Vi du: generateId("ORD", 5) → "ORD00006"
     *
     * @param prefix Tien to (ORD, OD, TX)
     * @param currentCount So luong ban ghi hien tai
     * @return Ma ID moi
     */
    private String generateId(String prefix, int currentCount) {
        return String.format("%s%05d", prefix, currentCount + 1);
    }

    public ControllerResult getOrdersByCustomerId(String customerId) {
        List<Order> orders = orderRepo.findByCustomerId(customerId);
        return success("Tim thay " + orders.size() + " don hang.", orders);
    }

    public ControllerResult getDetailsByOrderId(String orderId) {
        List<OrderDetail> details = detailRepo.findByOrderId(orderId);
        return success("Tim thay " + details.size() + " chi tiet don hang.", details);
    }

    // =====================================================================
    // GETTERS (cho Unit Test va Admin)
    // =====================================================================

    public OrderRepository getOrderRepo() {
        return orderRepo;
    }

    public OrderDetailRepository getDetailRepo() {
        return detailRepo;
    }

    public OrderTransactionRepository getTxRepo() {
        return txRepo;
    }

    public FlashSaleItemRepository getItemRepo() {
        return itemRepo;
    }
}

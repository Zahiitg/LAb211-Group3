package controller;

import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.Product;
import model.enums.SaleStatus;
import repository.FlashSaleEventRepository;
import repository.FlashSaleItemRepository;
import repository.ProductRepository;
import java.util.List;

/**
 * Controller xu ly cac nghiep vu lien quan den Su kien Flash Sale.
 *
 * Chuc nang hien tai:
 * 1. getAllEvents() → Lay danh sach toan bo su kien Flash Sale
 *
 * Cac chuc nang se bo sung (theo phan cong TV3):
 * - listItems(eventId)   → Lay san pham trong 1 su kien cu the
 * - startEvent(eventId)  → Kich hoat su kien (ONGOING)
 * - endEvent(eventId)    → Ket thuc su kien (ENDED)
 *
 * @author Thanh vien 3 - Flash Sale Logic
 * @refactored-by Thanh vien 1 - Core Architecture (ap dung chuan BaseController)
 */
public class FlashSaleController extends BaseController {

    private final FlashSaleEventRepository eventRepo;
    private final FlashSaleItemRepository itemRepo;
    private final ProductRepository productRepo;

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    /**
     * Constructor mac dinh - su dung duong dan file CSV chuan cua du an.
     */
    public FlashSaleController() {
        this.eventRepo = new FlashSaleEventRepository("data/flash_events.csv");
        this.itemRepo = new FlashSaleItemRepository("data/flash_items.csv");
        this.productRepo = new ProductRepository("data/products.csv");
    }

    /**
     * Constructor cho testing hoac duong dan tuy chinh.
     * @param filePath Duong dan den file CSV flash events
     */
    public FlashSaleController(String filePath) {
        this.eventRepo = new FlashSaleEventRepository(filePath);
        this.itemRepo = new FlashSaleItemRepository("data/flash_items.csv");
        this.productRepo = new ProductRepository("data/products.csv");
    }

    /**
     * Constructor cho testing voi file su kien va mat hang tuy chinh.
     *
     * @param eventFilePath duong dan file CSV flash events
     * @param itemFilePath  duong dan file CSV flash sale items
     */
    public FlashSaleController(String eventFilePath, String itemFilePath) {
        this.eventRepo = new FlashSaleEventRepository(eventFilePath);
        this.itemRepo = new FlashSaleItemRepository(itemFilePath);
        this.productRepo = new ProductRepository("data/products.csv");
    }

    // =====================================================================
    // NGHIEP VU
    // =====================================================================

    /**
     * Lay danh sach TOAN BO su kien Flash Sale trong he thong.
     *
     * @return ControllerResult chua List<FlashSaleEvent> trong data
     *         (co the la list rong neu chua co su kien nao)
     */
    public ControllerResult getAllEvents() {
        List<FlashSaleEvent> events = eventRepo.getAll();

        if (events.isEmpty()) {
            return success("Hien tai chua co su kien Flash Sale nao.", events);
        }

        return success("Tim thay " + events.size() + " su kien Flash Sale.", events);
    }

    /**
     * Lay danh sach mat hang dang tham gia mot su kien Flash Sale.
     *
     * @param eventId ID cua su kien
     * @return ControllerResult chua List&lt;FlashSaleItem&gt;
     */
    public ControllerResult listItems(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            return error("Event ID khong duoc de trong!");
        }

        String normalizedId = eventId.trim();
        if (eventRepo.getById(normalizedId) == null) {
            return error("Khong tim thay Flash Sale Event voi ID: " + normalizedId);
        }

        List<FlashSaleItem> items = itemRepo.findItemsByEventId(normalizedId);
        return success("Tim thay " + items.size()
            + " mat hang trong su kien " + normalizedId + ".", items);
    }

    /**
     * API cu duoc giu lai de khong anh huong cac View dang su dung.
     */
    public ControllerResult getItemsByEventId(String eventId) {
        return listItems(eventId);
    }

    /**
     * Kich hoat mot su kien Flash Sale.
     *
     * @param eventId ID cua su kien
     * @return ControllerResult chua event da cap nhat
     */
    public ControllerResult startEvent(String eventId) {
        return updateEventStatus(eventId, SaleStatus.ONGOING, "kich hoat");
    }

    /**
     * Ket thuc mot su kien Flash Sale.
     *
     * @param eventId ID cua su kien
     * @return ControllerResult chua event da cap nhat
     */
    public ControllerResult endEvent(String eventId) {
        return updateEventStatus(eventId, SaleStatus.ENDED, "ket thuc");
    }

    private ControllerResult updateEventStatus(
            String eventId, SaleStatus targetStatus, String action) {
        if (eventId == null || eventId.trim().isEmpty()) {
            return error("Event ID khong duoc de trong!");
        }

        String normalizedId = eventId.trim();
        FlashSaleEvent event = eventRepo.getById(normalizedId);
        if (event == null) {
            return error("Khong tim thay Flash Sale Event voi ID: " + normalizedId);
        }
        if (event.getStatus() == targetStatus) {
            return error("Su kien " + normalizedId + " da o trang thai "
                + targetStatus + ".");
        }

        event.setStatus(targetStatus);
        eventRepo.update(event);
        return success("Da " + action + " su kien " + normalizedId
            + " (" + targetStatus + ").", event);
    }

    /**
     * Lay san pham theo ID.
     */
    public Product getProductById(String productId) {
        return productRepo.getById(productId);
    }

    /**
     * Lay FlashSaleEventRepository (dung cho Admin hoac Unit Test).
     * @return FlashSaleEventRepository
     */
    public FlashSaleEventRepository getEventRepo() {
        return eventRepo;
    }
}

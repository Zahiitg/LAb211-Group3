package controller;

import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.Product;
import repository.FlashSaleEventRepository;
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
    private final repository.FlashSaleItemRepository itemRepo;
    private final repository.ProductRepository productRepo;

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    /**
     * Constructor mac dinh - su dung duong dan file CSV chuan cua du an.
     */
    public FlashSaleController() {
        this.eventRepo = new FlashSaleEventRepository("data/flash_events.csv");
        this.itemRepo = new repository.FlashSaleItemRepository("data/flash_items.csv");
        this.productRepo = new repository.ProductRepository("data/products.csv");
    }

    /**
     * Constructor cho testing hoac duong dan tuy chinh.
     * @param filePath Duong dan den file CSV flash events
     */
    public FlashSaleController(String filePath) {
        this.eventRepo = new FlashSaleEventRepository(filePath);
        this.itemRepo = new repository.FlashSaleItemRepository("data/flash_items.csv");
        this.productRepo = new repository.ProductRepository("data/products.csv");
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
     * Lay danh sach san pham theo Event ID.
     */
    public ControllerResult getItemsByEventId(String eventId) {
        List<FlashSaleItem> items = itemRepo.findItemsByEventId(eventId);
        return success("Tim thay " + items.size() + " san pham Flash Sale.", items);
    }

    /**
     * Lay san pham theo ID.
     */
    public model.Product getProductById(String productId) {
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

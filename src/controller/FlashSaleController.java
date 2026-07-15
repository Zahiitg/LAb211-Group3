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
 * - listItems(eventId) → Lay san pham trong 1 su kien cu the
 * - startEvent(eventId) → Kich hoat su kien (ONGOING)
 * - endEvent(eventId) → Ket thuc su kien (ENDED)
 *
 * @author Thanh vien 3 - Flash Sale Logic
 * @refactored-by Thanh vien 1 - Core Architecture (ap dung chuan
 *                BaseController)
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
        AuthenticationState authState = AuthenticationState.getInstance();
        this.eventRepo = authState.getFlashSaleEventRepo();
        this.itemRepo = authState.getFlashSaleItemRepo();
        this.productRepo = authState.getProductRepo();
    }

    /**
     * Constructor cho testing hoac duong dan tuy chinh.
     * 
     * @param filePath Duong dan den file CSV flash events
     */
    public FlashSaleController(String filePath) {
        AuthenticationState authState = AuthenticationState.getInstance();
        this.eventRepo = new FlashSaleEventRepository(filePath);
        this.itemRepo = authState.getFlashSaleItemRepo();
        this.productRepo = authState.getProductRepo();
    }

    /**
     * Constructor cho testing voi file su kien va mat hang tuy chinh.
     *
     * @param eventFilePath duong dan file CSV flash events
     * @param itemFilePath  duong dan file CSV flash sale items
     */
    public FlashSaleController(String eventFilePath, String itemFilePath) {
        AuthenticationState authState = AuthenticationState.getInstance();
        this.eventRepo = new FlashSaleEventRepository(eventFilePath);
        this.itemRepo = new FlashSaleItemRepository(itemFilePath);
        this.productRepo = authState.getProductRepo();
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
     * Tao su kien Flash Sale moi. (Goi tu AdminController)
     */
    public ControllerResult createEvent(String name, int durationDays) {
        if (name == null || name.trim().isEmpty()) {
            return error("Ten su kien khong duoc de trong!");
        }
        if (durationDays <= 0) {
            return error("So ngay dien ra phai lon hon 0!");
        }

        int maxNum = 0;
        for (FlashSaleEvent ev : eventRepo.getAll()) {
            if (ev.getId() != null && ev.getId().startsWith("E")) {
                try {
                    int num = Integer.parseInt(ev.getId().substring(1));
                    if (num > maxNum)
                        maxNum = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String newId = String.format("E%05d", maxNum + 1);

        java.time.LocalDateTime start = java.time.LocalDateTime.now()
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        java.time.LocalDateTime end = start.plusDays(durationDays);

        FlashSaleEvent newEvent = new FlashSaleEvent(newId, name.trim(), start, end, SaleStatus.UPCOMING);
        eventRepo.add(newEvent);

        return success("Tao su kien Flash Sale thanh cong: " + name, newEvent);
    }

    /**
     * Nguoi ban dang ky san pham vao Flash Sale.
     */
    public ControllerResult registerItem(String eventId, String productId, double salePrice, int limitedQty,
            String sellerId) {
        FlashSaleEvent ev = eventRepo.getById(eventId);
        if (ev == null)
            return error("Khong tim thay su kien Flash Sale!");
        if (ev.getStatus() != SaleStatus.ONGOING && ev.getStatus() != SaleStatus.UPCOMING)
            return error("Su kien khong trong trang thai ONGOING hoac UPCOMING!");

        Product p = productRepo.getById(productId);
        if (p == null)
            return error("Khong tim thay san pham!");
        if (!p.getSellerId().equals(sellerId))
            return error("Ban chi duoc dang ky san pham cua chinh minh!");

        if (salePrice <= 0 || salePrice >= p.getPrice()) {
            return error("Gia Sale phai lon hon 0 va NHO HON gia goc (" + p.getPrice() + ")!");
        }
        if (limitedQty <= 0 || limitedQty > p.getStock()) {
            return error("So luong gioi han phai tu 1 den " + p.getStock() + "!");
        }

        int maxNum = 0;
        for (FlashSaleItem fi : itemRepo.getAll()) {
            if (fi.getId() != null && fi.getId().startsWith("FI")) {
                try {
                    int num = Integer.parseInt(fi.getId().substring(2));
                    if (num > maxNum)
                        maxNum = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String newId = String.format("FI%05d", maxNum + 1);

        FlashSaleItem item = new FlashSaleItem(newId, productId, eventId, salePrice, limitedQty, 0, 1);
        itemRepo.add(item);

        return success("Da dang ky san pham vao Flash Sale thanh cong!", item);
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
        return updateEventStatus(eventId, SaleStatus.ONGOING, "kich hoat 1234");
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
     * 
     * @return FlashSaleEventRepository
     */
    public FlashSaleEventRepository getEventRepo() {
        return eventRepo;
    }
}

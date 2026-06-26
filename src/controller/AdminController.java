package controller;

import java.util.List;

import model.Customer;
import model.Seller;
import model.Order;
import model.OrderDetail;
import model.OrderTransaction;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.Product;
import model.enums.AccountStatus;
import model.enums.OrderStatus;
import model.enums.SaleStatus;
import repository.CustomerRepository;
import repository.SellerRepository;
import repository.OrderRepository;
import repository.OrderDetailRepository;
import repository.OrderTransactionRepository;
import repository.FlashSaleItemRepository;
import repository.ProductRepository;
import repository.CategoryRepository;
import repository.FlashSaleEventRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Controller dành riêng cho Admin — Quản lý tài khoản người dùng.
 *
 * Chuc nang:
 * - Xem danh sach Customer / Seller
 * - Khoa (Ban) tai khoan Customer / Seller
 * - Duyet (Approve) tai khoan Customer / Seller
 *
 * Moi ham deu goi requireAdmin() o dau → Dam bao CHI ADMIN moi thao tac duoc.
 * Moi ham deu tra ve ControllerResult → Tang View nhan ve format nhat quan.
 *
 * @author Thanh vien 1 - Core Architecture & Admin Controller
 */
public class AdminController extends BaseController {

    private final CustomerRepository customerRepo;
    private final SellerRepository sellerRepo;
    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;
    private final OrderTransactionRepository txRepo;
    private final FlashSaleItemRepository flashSaleItemRepo;
    private final ProductRepository productRepo;
    private final FlashSaleController flashSaleController;
    private final CategoryRepository categoryRepo;
    private final FlashSaleEventRepository eventRepo;
    private final repository.AdminRepository adminRepo;

    public AdminController() {
        AuthenticationState authState = AuthenticationState.getInstance();
        this.adminRepo = authState.getAdminRepo();
        this.customerRepo = authState.getCustomerRepo();
        this.sellerRepo = authState.getSellerRepo();
        this.orderRepo = authState.getOrderRepo();
        this.detailRepo = authState.getDetailRepo();
        this.txRepo = authState.getTxRepo();
        this.flashSaleItemRepo = authState.getFlashSaleItemRepo();
        this.productRepo = authState.getProductRepo();
        this.categoryRepo = authState.getCategoryRepo();
        this.eventRepo = authState.getFlashSaleEventRepo();
        this.flashSaleController = new FlashSaleController();
    }

    // =====================================================================
    // XEM DANH SACH
    // =====================================================================

    /**
     * Lay danh sach TOAN BO Customer.
     * Chi Admin moi duoc phep goi.
     *
     * @return ControllerResult chua List<Customer> trong data
     */
    public ControllerResult listAllCustomers() {
        requireAdmin();
        List<Customer> customers = customerRepo.getAll();
        return success("Tim thay " + customers.size() + " Customer trong he thong.", customers);
    }

    /**
     * Lay danh sach TOAN BO Seller.
     * Chi Admin moi duoc phep goi.
     *
     * @return ControllerResult chua List<Seller> trong data
     */
    public ControllerResult listAllSellers() {
        requireAdmin();
        List<Seller> sellers = sellerRepo.getAll();
        return success("Tim thay " + sellers.size() + " Seller trong he thong.", sellers);
    }

    // =====================================================================
    // KHOA TAI KHOAN (BAN)
    // =====================================================================

    /**
     * Khoa tai khoan Customer (doi trang thai thanh BANNED).
     * Kiem tra:
     * - Customer co ton tai khong
     * - Customer da bi ban tu truoc chua (tranh thao tac thua)
     *
     * @param customerId ID cua Customer can khoa
     * @return ControllerResult chua Customer da bi khoa trong data
     */
    public ControllerResult banCustomer(String customerId) {
        requireAdmin();

        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Customer ID khong duoc de trong!");
        }

        Customer customer = customerRepo.getById(customerId.trim());
        if (customer == null) {
            return error("Khong tim thay Customer voi ID: " + customerId);
        }
        if (customer.getStatus() == AccountStatus.BANNED) {
            return error("Customer '" + customer.getName()
                + "' (" + customerId + ") da bi khoa tu truoc!");
        }

        customer.setStatus(AccountStatus.BANNED);
        customerRepo.update(customer);

        return success("Da khoa tai khoan Customer: "
            + customer.getName() + " (" + customerId + ")", customer);
    }

    /**
     * Khoa tai khoan Seller (doi trang thai thanh BANNED).
     *
     * @param sellerId ID cua Seller can khoa
     * @return ControllerResult chua Seller da bi khoa trong data
     */
    public ControllerResult banSeller(String sellerId) {
        requireAdmin();

        if (sellerId == null || sellerId.trim().isEmpty()) {
            return error("Seller ID khong duoc de trong!");
        }

        Seller seller = sellerRepo.getById(sellerId.trim());
        if (seller == null) {
            return error("Khong tim thay Seller voi ID: " + sellerId);
        }
        if (seller.getStatus() == AccountStatus.BANNED) {
            return error("Seller '" + seller.getName()
                + "' (" + sellerId + ") da bi khoa tu truoc!");
        }

        seller.setStatus(AccountStatus.BANNED);
        sellerRepo.update(seller);

        return success("Da khoa tai khoan Seller: "
            + seller.getName() + " (" + sellerId + ")", seller);
    }

    // =====================================================================
    // DUYET TAI KHOAN (APPROVE)
    // =====================================================================

    /**
     * Duyet tai khoan Customer (doi trang thai thanh APPROVED).
     * Thuong dung de duyet cac tai khoan dang o trang thai PENDING.
     *
     * @param customerId ID cua Customer can duyet
     * @return ControllerResult chua Customer da duoc duyet trong data
     */
    public ControllerResult approveCustomer(String customerId) {
        requireAdmin();

        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Customer ID khong duoc de trong!");
        }

        Customer customer = customerRepo.getById(customerId.trim());
        if (customer == null) {
            return error("Khong tim thay Customer voi ID: " + customerId);
        }
        if (customer.getStatus() == AccountStatus.APPROVED) {
            return error("Customer '" + customer.getName()
                + "' (" + customerId + ") da duoc duyet tu truoc!");
        }

        customer.setStatus(AccountStatus.APPROVED);
        customerRepo.update(customer);

        return success("Da duyet tai khoan Customer: "
            + customer.getName() + " (" + customerId + ")", customer);
    }

    /**
     * Duyet tai khoan Seller (doi trang thai thanh APPROVED).
     *
     * @param sellerId ID cua Seller can duyet
     * @return ControllerResult chua Seller da duoc duyet trong data
     */
    public ControllerResult approveSeller(String sellerId) {
        requireAdmin();

        if (sellerId == null || sellerId.trim().isEmpty()) {
            return error("Seller ID khong duoc de trong!");
        }

        Seller seller = sellerRepo.getById(sellerId.trim());
        if (seller == null) {
            return error("Khong tim thay Seller voi ID: " + sellerId);
        }
        if (seller.getStatus() == AccountStatus.APPROVED) {
            return error("Seller '" + seller.getName()
                + "' (" + sellerId + ") da duoc duyet tu truoc!");
        }

        seller.setStatus(AccountStatus.APPROVED);
        sellerRepo.update(seller);

        return success("Da duyet tai khoan Seller: "
            + seller.getName() + " (" + sellerId + ")", seller);
    }

    // =====================================================================
    // MO KHOA TAI KHOAN (UNBAN)
    // =====================================================================

    /**
     * Mo khoa tai khoan Customer (doi trang thai tu BANNED thanh APPROVED).
     *
     * @param customerId ID cua Customer can mo khoa
     * @return ControllerResult chua Customer da duoc mo khoa trong data
     */
    public ControllerResult unbanCustomer(String customerId) {
        requireAdmin();

        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Customer ID khong duoc de trong!");
        }

        Customer customer = customerRepo.getById(customerId.trim());
        if (customer == null) {
            return error("Khong tim thay Customer voi ID: " + customerId);
        }
        if (customer.getStatus() != AccountStatus.BANNED) {
            return error("Customer '" + customer.getName()
                + "' (" + customerId + ") khong bi khoa!");
        }

        customer.setStatus(AccountStatus.APPROVED);
        customerRepo.update(customer);

        return success("Da mo khoa tai khoan Customer: "
            + customer.getName() + " (" + customerId + ")", customer);
    }

    /**
     * Mo khoa tai khoan Seller (doi trang thai tu BANNED thanh APPROVED).
     *
     * @param sellerId ID cua Seller can mo khoa
     * @return ControllerResult chua Seller da duoc mo khoa trong data
     */
    public ControllerResult unbanSeller(String sellerId) {
        requireAdmin();

        if (sellerId == null || sellerId.trim().isEmpty()) {
            return error("Seller ID khong duoc de trong!");
        }

        Seller seller = sellerRepo.getById(sellerId.trim());
        if (seller == null) {
            return error("Khong tim thay Seller voi ID: " + sellerId);
        }
        if (seller.getStatus() != AccountStatus.BANNED) {
            return error("Seller '" + seller.getName()
                + "' (" + sellerId + ") khong bi khoa!");
        }

        seller.setStatus(AccountStatus.APPROVED);
        sellerRepo.update(seller);

        return success("Da mo khoa tai khoan Seller: "
            + seller.getName() + " (" + sellerId + ")", seller);
    }

    // =====================================================================
    // 1. QUAN LY DON HANG (ORDER MANAGEMENT)
    // =====================================================================

    public ControllerResult listAllOrders() {
        requireAdmin();
        List<Order> orders = orderRepo.getAll();
        return success("Tim thay " + orders.size() + " don hang.", orders);
    }

    public ControllerResult listOrdersByStatus(OrderStatus status) {
        requireAdmin();
        if (status == null) {
            return error("Trang thai khong duoc de trong!");
        }
        List<Order> filtered = orderRepo.getAll().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
        return success("Tim thay " + filtered.size() + " don hang trang thai " + status, filtered);
    }

    public ControllerResult verifyOrder(String orderId) {
        requireAdmin();
        if (orderId == null || orderId.trim().isEmpty()) return error("Order ID khong duoc de trong!");
        
        Order o = orderRepo.getById(orderId.trim());
        if (o == null) return error("Khong tim thay don hang: " + orderId);
        if (o.getStatus() != OrderStatus.PENDING) return error("Chi the xac nhan don hang o trang thai PENDING!");
        
        o.setStatus(OrderStatus.VERIFIED);
        orderRepo.update(o);
        return success("Da xac nhan don hang: " + orderId, o);
    }

    public ControllerResult completeOrder(String orderId) {
        requireAdmin();
        if (orderId == null || orderId.trim().isEmpty()) return error("Order ID khong duoc de trong!");
        
        Order o = orderRepo.getById(orderId.trim());
        if (o == null) return error("Khong tim thay don hang: " + orderId);
        if (o.getStatus() != OrderStatus.VERIFIED) return error("Chi co the hoan thanh don hang dang VERIFIED!");
        
        o.setStatus(OrderStatus.COMPLETED);
        orderRepo.update(o);
        return success("Da hoan thanh don hang: " + orderId, o);
    }

    public ControllerResult cancelOrder(String orderId) {
        requireAdmin();
        if (orderId == null || orderId.trim().isEmpty()) return error("Order ID khong duoc de trong!");
        
        Order o = orderRepo.getById(orderId.trim());
        if (o == null) return error("Khong tim thay don hang: " + orderId);
        if (o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.COMPLETED) {
            return error("Khong the huy don hang da hoan thanh hoac da huy!");
        }
        
        // HOAN KHO
        List<OrderDetail> details = detailRepo.findByOrderId(o.getId());
        for (OrderDetail detail : details) {
            String itemId = detail.getFlashSaleItemId(); // dung cho ca SP thuong & FlashSale
            int qty = detail.getQuantity();
            
            FlashSaleItem fsItem = flashSaleItemRepo.getById(itemId);
            if (fsItem != null) {
                // Hoan kho Flash Sale
                fsItem.setSoldQty(fsItem.getSoldQty() - qty);
                flashSaleItemRepo.update(fsItem);
                
                // Hoan kho Product thuong tuong ung
                Product origP = productRepo.getById(fsItem.getProductId());
                if (origP != null) {
                    origP.setStock(origP.getStock() + qty);
                    productRepo.update(origP);
                }
            } else {
                Product p = productRepo.getById(itemId);
                if (p != null) {
                    // Hoan kho Product thuong
                    p.setStock(p.getStock() + qty);
                    productRepo.update(p);
                }
            }
        }
        
        o.setStatus(OrderStatus.CANCELLED);
        orderRepo.update(o);
        return success("Da huy don hang va hoan kho: " + orderId, o);
    }

    public ControllerResult getDetailsByOrderId(String orderId) {
        requireAdmin();
        Order order = orderRepo.getById(orderId);
        if (order == null) return error("Khong tim thay don hang!");
        List<OrderDetail> details = detailRepo.findByOrderId(orderId);
        return success("Chi tiet don hang", details);
    }

    // =====================================================================
    // PHASE 1: TAO SU KIEN FLASH SALERD
    // =====================================================================

    // =====================================================================
    // 2. THONG KE DASHBOARD
    // =====================================================================

    public ControllerResult getDashboardStats() {
        requireAdmin();
        
        Map<String, Object> stats = new HashMap<>();
        
        List<Customer> customers = customerRepo.getAll();
        stats.put("totalCustomers", customers.size());
        stats.put("approvedCustomers", customers.stream().filter(c -> c.getStatus() == AccountStatus.APPROVED).count());
        stats.put("bannedCustomers", customers.stream().filter(c -> c.getStatus() == AccountStatus.BANNED).count());
        
        stats.put("totalSellers", sellerRepo.getAll().size());
        stats.put("totalProducts", productRepo.getAll().size());
        
        List<Order> orders = orderRepo.getAll();
        stats.put("totalOrders", orders.size());
        stats.put("pendingOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        stats.put("verifiedOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.VERIFIED).count());
        stats.put("completedOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count());
        stats.put("cancelledOrders", orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        
        double totalRevenue = 0.0;
        List<OrderDetail> allDetails = detailRepo.getAll();
        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.COMPLETED) {
                for (OrderDetail d : allDetails) {
                    if (d.getOrderId().equals(o.getId())) {
                        totalRevenue += (d.getPriceAtPurchase() * d.getQuantity());
                    }
                }
            }
        }
        stats.put("totalRevenue", totalRevenue);
        
        List<FlashSaleEvent> events = flashSaleController.getEventRepo().getAll();
        stats.put("totalEvents", events.size());
        stats.put("ongoingEvents", events.stream().filter(e -> e.getStatus() == SaleStatus.ONGOING).count());
        
        return success("Tai thong ke thanh cong", stats);
    }

    // =====================================================================
    // 3. TIM KIEM TAI KHOAN
    // =====================================================================

    public ControllerResult searchCustomers(String keyword) {
        requireAdmin();
        if (keyword == null || keyword.trim().isEmpty()) return error("Tu khoa tim kiem khong duoc trong");
        
        String lowerKeyword = keyword.trim().toLowerCase();
        List<Customer> results = customerRepo.getAll().stream()
                .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(lowerKeyword)) ||
                             (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
        
        return success("Tim thay " + results.size() + " Customer.", results);
    }

    public ControllerResult searchSellers(String keyword) {
        requireAdmin();
        if (keyword == null || keyword.trim().isEmpty()) return error("Tu khoa tim kiem khong duoc trong");
        
        String lowerKeyword = keyword.trim().toLowerCase();
        List<Seller> results = sellerRepo.getAll().stream()
                .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(lowerKeyword)) ||
                             (s.getEmail() != null && s.getEmail().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
        
        return success("Tim thay " + results.size() + " Seller.", results);
    }

    // =====================================================================
    // 4. QUAN LY FLASH SALE
    // =====================================================================

    public ControllerResult listFlashSaleEvents() {
        requireAdmin();
        return flashSaleController.getAllEvents();
    }

    public ControllerResult startFlashSaleEvent(String eventId) {
        requireAdmin();
        return flashSaleController.startEvent(eventId);
    }

    public ControllerResult endFlashSaleEvent(String eventId) {
        requireAdmin();
        return flashSaleController.endEvent(eventId);
    }

    // =====================================================================
    // PHASE 1: TAO SU KIEN FLASH SALE
    // =====================================================================
    public ControllerResult createFlashSaleEvent(String name, int durationDays) {
        requireAdmin();
        return flashSaleController.createEvent(name, durationDays);
    }

    // =====================================================================
    // PHASE 3: QUAN LY DANH MUC
    // =====================================================================
    public ControllerResult listAllCategories() {
        requireAdmin();
        List<model.Category> categories = categoryRepo.getAll();
        
        // Auto-seed from products if empty
        if (categories.isEmpty()) {
            List<String> productCats = productRepo.getAllCategories();
            int idCounter = 1;
            for (String cat : productCats) {
                model.Category c = new model.Category(String.format("CAT%03d", idCounter++), cat);
                categoryRepo.add(c);
                categories.add(c);
            }
        }
        
        if (categories.isEmpty()) return error("Khong co danh muc nao.");
        return success("Danh sach danh muc", categories);
    }

    public ControllerResult addCategory(String name) {
        requireAdmin();
        if (name == null || name.trim().isEmpty()) return error("Ten danh muc khong de trong!");
        int maxNum = 0;
        for (model.Category c : categoryRepo.getAll()) {
            if (c.getId() != null && c.getId().startsWith("CAT")) {
                try {
                    int num = Integer.parseInt(c.getId().substring(3));
                    if (num > maxNum) maxNum = num;
                } catch (Exception e) {}
            }
        }
        model.Category newCat = new model.Category(String.format("CAT%03d", maxNum + 1), name.trim());
        categoryRepo.add(newCat);
        return success("Them danh muc thanh cong: " + newCat.getName(), newCat);
    }

    public ControllerResult deleteCategory(String id) {
        requireAdmin();
        model.Category c = categoryRepo.getById(id);
        if (c == null) return error("Khong tim thay danh muc!");
        categoryRepo.delete(id);
        return success("Da xoa danh muc: " + c.getName(), c);
    }

    // =====================================================================
    // PHASE 4: CAP NHAT PROFILE ADMIN
    // =====================================================================
    public ControllerResult updateProfile(String newName, String newPassword) {
        requireAdmin();
        model.Admin me = (model.Admin) authState.getCurrentUser();
        if (newName != null && !newName.trim().isEmpty()) {
            me.setName(newName.trim());
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.trim().length() < 6) return error("Mat khau moi phai tu 6 ky tu tro len!");
            me.setPassword(newPassword.trim());
        }
        adminRepo.update(me);
        return success("Cap nhat ho so thanh cong!", me);
    }

    // =====================================================================
    // 5. QUAN LY SAN PHAM
    // =====================================================================

    public ControllerResult listAllProducts() {
        requireAdmin();
        List<Product> products = productRepo.getAll();
        return success("Tim thay " + products.size() + " san pham.", products);
    }

    public ControllerResult searchProducts(String keyword) {
        requireAdmin();
        if (keyword == null || keyword.trim().isEmpty()) return error("Tu khoa khong duoc trong");
        List<Product> products = productRepo.findByName(keyword);
        return success("Tim thay " + products.size() + " san pham.", products);
    }

    public ControllerResult getProductsByCategory(String category) {
        requireAdmin();
        if (category == null || category.trim().isEmpty()) return error("Danh muc khong duoc trong");
        List<Product> products = productRepo.findByCategory(category);
        return success("Tim thay " + products.size() + " san pham.", products);
    }

    public ControllerResult deleteProduct(String productId) {
        requireAdmin();
        if (productId == null || productId.trim().isEmpty()) return error("Ma san pham khong duoc trong");
        
        Product p = productRepo.getById(productId.trim());
        if (p == null) return error("Khong tim thay san pham: " + productId);
        
        productRepo.delete(productId.trim());
        return success("Da xoa san pham: " + productId, p);
    }

    // =====================================================================
    // 6. XEM LOG GIAO DICH
    // =====================================================================

    public ControllerResult listAllTransactions() {
        requireAdmin();
        List<OrderTransaction> txList = txRepo.getAll();
        return success("Tim thay " + txList.size() + " log giao dich.", txList);
    }

    public ControllerResult listFailedTransactions() {
        requireAdmin();
        List<OrderTransaction> failedTx = txRepo.getAll().stream()
                .filter(tx -> !tx.isSuccess())
                .collect(Collectors.toList());
        return success("Tim thay " + failedTx.size() + " log giao dich THAT BAI.", failedTx);
    }
}

package controller;

import model.Customer;
import model.enums.AccountStatus;
import model.enums.CustTier;
import repository.CustomerRepository;
import repository.ProductRepository;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;
import repository.OrderDetailRepository;
import model.FlashSaleItem;
import model.Product;
import model.Order;
import model.OrderDetail;
import model.enums.OrderStatus;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller xu ly toan bo nghiep vu lien quan den Khach hang (Customer).
 *
 * Chuc nang:
 * 1. register(name, email, password) → Dang ky tai khoan moi
 * 2. login(email, password)          → Dang nhap va luu phien vao AuthenticationState
 *
 * Tat ca ham deu tra ve ControllerResult de tang View xu ly nhat quan,
 * KHONG BAO GIO quang Exception ra ngoai hoac tra ve null.
 *
 * @author Thanh vien 2 - Customer Logic
 * @refactored-by Thanh vien 1 - Core Architecture (ap dung chuan BaseController)
 */
public class CustomerController extends BaseController {

    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final FlashSaleItemRepository flashSaleItemRepo;
    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;

    // CART STATE
    private static final Map<String, Map<String, Integer>> globalCarts = new HashMap<>();
    private static boolean isCartLoaded = false;
    private static final String CART_FILE = "data/carts.csv";

    private void loadCartsFromFile() {
        if (isCartLoaded) return;
        isCartLoaded = true;
        java.io.File file = new java.io.File(CART_FILE);
        if (!file.exists()) return;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("customerId")) continue;
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String custId = parts[0].trim();
                    String itemId = parts[1].trim();
                    int qty = Integer.parseInt(parts[2].trim());
                    globalCarts.computeIfAbsent(custId, k -> new HashMap<>()).put(itemId, qty);
                }
            }
        } catch (Exception e) {
            System.err.println("Loi khi doc file gio hang: " + e.getMessage());
        }
    }

    private void saveCartsToFile() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(CART_FILE))) {
            pw.println("customerId,itemId,quantity");
            for (Map.Entry<String, Map<String, Integer>> custEntry : globalCarts.entrySet()) {
                String custId = custEntry.getKey();
                for (Map.Entry<String, Integer> itemEntry : custEntry.getValue().entrySet()) {
                    pw.println(custId + "," + itemEntry.getKey() + "," + itemEntry.getValue());
                }
            }
        } catch (Exception e) {
            System.err.println("Loi khi luu file gio hang: " + e.getMessage());
        }
    }

    private Map<String, Integer> getCart() {
        loadCartsFromFile();
        if (!authState.isCustomer()) return new HashMap<>();
        Customer c = (Customer) authState.getCurrentUser();
        return globalCarts.computeIfAbsent(c.getId(), k -> new HashMap<>());
    }

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    /**
     * Constructor mac dinh - su dung duong dan file CSV chuan cua du an.
     */
    public CustomerController() {
        AuthenticationState authState = AuthenticationState.getInstance();
        this.customerRepo = authState.getCustomerRepo();
        this.productRepo = authState.getProductRepo();
        this.flashSaleItemRepo = authState.getFlashSaleItemRepo();
        this.orderRepo = authState.getOrderRepo();
        this.detailRepo = authState.getDetailRepo();
    }

    /**
     * Constructor cho testing hoac duong dan tuy chinh.
     * @param filePath Duong dan den file CSV customers
     */
    public CustomerController(String filePath) {
        AuthenticationState authState = AuthenticationState.getInstance();
        this.customerRepo = new CustomerRepository(filePath);
        this.productRepo = authState.getProductRepo();
        this.flashSaleItemRepo = authState.getFlashSaleItemRepo();
        this.orderRepo = authState.getOrderRepo();
        this.detailRepo = authState.getDetailRepo();
    }

    // =====================================================================
    // DANG KY TAI KHOAN
    // =====================================================================

    /**
     * Dang ky tai khoan Customer moi.
     *
     * Quy trinh:
     * 1. Kiem tra du lieu dau vao (Name, Email, Password khong duoc rong).
     * 2. Kiem tra trung Email trong CSDL.
     * 3. Sinh ma ID tu dong tang (C00001, C00002, ...).
     * 4. Tao Customer moi voi trang thai APPROVED va hang BRONZE.
     * 5. Luu vao file CSV.
     *
     * @param name     Ten khach hang
     * @param email    Email (phai duy nhat)
     * @param password Mat khau
     * @param address  Dia chi giao hang (co the de trong)
     * @return ControllerResult chua Customer object neu thanh cong,
     *         hoac thong bao loi neu that bai
     */
    public ControllerResult register(String name, String email, String password, String address) {
        // --- VALIDATION ---
        if (name == null || name.trim().isEmpty()) {
            return error("Ten khong duoc de trong!");
        }
        if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return error("Email khong hop le! Vui long kiem tra lai dinh dang.");
        }
        if (password == null || password.trim().length() < 6) {
            return error("Mat khau phai tu 6 ky tu tro len!");
        }

        // --- KIEM TRA TRUNG EMAIL ---
        if (customerRepo.getByEmail(email.trim()) != null) {
            return error("Email '" + email.trim() + "' da duoc dang ky truoc do!");
        }

        // --- SINH MA ID TU DONG ---
        int maxNum = 0;
        for (Customer c : customerRepo.getAll()) {
            String id = c.getId();
            if (id != null && id.startsWith("C")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        String newId = String.format("C%05d", maxNum + 1);

        // --- TAO VA LUU CUSTOMER MOI ---
        Customer newCust = new Customer(
            newId,
            name.trim(),
            email.trim(),
            password,
            AccountStatus.APPROVED,
            CustTier.BRONZE,
            address != null ? address.trim() : ""
        );
        customerRepo.add(newCust);

        return success("Dang ky thanh cong! Chao mung " + newCust.getName()
                       + " (Ma KH: " + newId + ")", newCust);
    }

    // =====================================================================
    // DANG NHAP
    // =====================================================================

    /**
     * Xac thuc va dang nhap Customer.
     *
     * Quy trinh:
     * 1. Kiem tra du lieu dau vao.
     * 2. Tim Customer theo email va doi chieu mat khau.
     * 3. Kiem tra trang thai tai khoan (BANNED → tu choi, PENDING → canh bao).
     * 4. Neu hop le → luu phien dang nhap vao AuthenticationState (Singleton).
     *
     * @param email    Email dang nhap
     * @param password Mat khau
     * @return ControllerResult chua Customer object neu thanh cong,
     *         hoac thong bao loi cu the neu that bai
     */
    public ControllerResult login(String email, String password) {
        // --- VALIDATION ---
        if (email == null || email.trim().isEmpty()) {
            return error("Email khong duoc de trong!");
        }
        if (password == null || password.trim().isEmpty()) {
            return error("Mat khau khong duoc de trong!");
        }

        // --- TIM CUSTOMER THEO EMAIL ---
        Customer c = customerRepo.getByEmail(email.trim());
        if (c == null) {
            return error("Email hoac mat khau khong chinh xac!");
        }

        // --- DOI CHIEU MAT KHAU ---
        if (!c.getPassword().equals(password)) {
            return error("Email hoac mat khau khong chinh xac!");
        }

        // --- KIEM TRA TRANG THAI TAI KHOAN ---
        if (c.getStatus() == AccountStatus.BANNED) {
            return error("Tai khoan cua ban da bi khoa (BANNED). "
                        + "Vui long lien he Admin de duoc ho tro!");
        }

        // --- LUU PHIEN DANG NHAP ---
        // Su dung AuthenticationState (Singleton) cua he thong
        // Luu y: authState duoc ke thua tu BaseController
        // Tuy nhien vi AuthenticationState co ham login() rieng,
        // o day ta chi set currentUser thong qua phuong thuc login cua AuthenticationState
        // De tranh goi chong cheo, ta su dung truc tiep:
        AuthenticationState.getInstance().loginDirect(c);

        // --- TRA VE KET QUA ---
        String statusNote = "";
        if (c.getStatus() == AccountStatus.PENDING) {
            statusNote = " (Luu y: Tai khoan dang cho duyet, "
                       + "mot so chuc nang co the bi han che)";
        }

        return success("Dang nhap thanh cong! Xin chao " + c.getName() + statusNote, c);
    }

    // =====================================================================
    // CAP NHAT HO SO
    // =====================================================================

    /**
     * Cap nhat ho so cua Customer.
     */
    public ControllerResult updateProfile(String name, String password, String address) {
        requireCustomer();
        Customer c = (Customer) authState.getCurrentUser();
        
        if (name != null && !name.trim().isEmpty()) c.setName(name);
        if (password != null && !password.trim().isEmpty()) c.setPassword(password);
        if (address != null && !address.trim().isEmpty()) c.setAddress(address);
        
        customerRepo.update(c);
        return success("Cap nhat ho so thanh cong!", c);
    }

    // =====================================================================
    // GIO HANG (CART)
    // =====================================================================

    public ControllerResult addToCart(String itemId, int qty) {
        requireCustomer();
        if (qty <= 0) return error("So luong phai lon hon 0!");
        
        // Kiem tra item co ton tai khong va kiem tra ton kho
        Product p = productRepo.getById(itemId);
        if (p != null) {
            int currentQty = getCart().getOrDefault(itemId, 0);
            if (currentQty + qty > p.getStock()) {
                return error("So luong vuot qua ton kho (Hien co: " + p.getStock() + ", Da co trong gio: " + currentQty + ")!");
            }
        } else {
            FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
            if (fsi == null) {
                return error("Khong tim thay san pham voi ma: " + itemId);
            }
            int currentQty = getCart().getOrDefault(itemId, 0);
            int stockLeft = fsi.getLimitedQty() - fsi.getSoldQty();
            if (currentQty + qty > stockLeft) {
                return error("So luong vuot qua ton kho Flash Sale (Con lai: " + stockLeft + ", Da co trong gio: " + currentQty + ")!");
            }
        }
        
        getCart().put(itemId, getCart().getOrDefault(itemId, 0) + qty);
        saveCartsToFile();
        return success("Da them vao gio hang!", getCart());
    }

    public ControllerResult viewCart() {
        requireCustomer();
        return success("Lay gio hang thanh cong", getCart());
    }

    public ControllerResult clearCart() {
        requireCustomer();
        getCart().clear();
        saveCartsToFile();
        return success("Da xoa sach gio hang", null);
    }

    public ControllerResult checkoutCart() {
        requireCustomer();
        if (getCart().isEmpty()) return error("Gio hang dang trong!");
        
        Customer c = (Customer) authState.getCurrentUser();
        
        // 1. Tao 1 Order cha (Phat sinh ID tu dong tang theo chuan O00000)
        int maxNum = 0;
        for (Order o : orderRepo.getAll()) {
            String id = o.getId();
            if (id != null) {
                String numStr = id.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    try {
                        int num = Integer.parseInt(numStr);
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {} // Bo qua cac ID qua lon (nhu currentTimeMillis)
                }
            }
        }
        String orderId = String.format("O%05d", maxNum + 1);
        Order order = new Order(orderId, c.getId(), LocalDateTime.now(), OrderStatus.PENDING);
        
        double totalRevenue = 0;
        List<OrderDetail> details = new ArrayList<>();
        
        // 2. Duyet tung item de tru kho va tao OrderDetail
        for (Map.Entry<String, Integer> entry : getCart().entrySet()) {
            String itemId = entry.getKey();
            int qty = entry.getValue();
            
            Product p = productRepo.getById(itemId);
            if (p != null) {
                boolean success = productRepo.sellWithOptimisticLock(itemId, qty);
                if (!success) return error("San pham " + p.getName() + " khong du ton kho, vui long thu lai!");
                
                OrderDetail d = new OrderDetail(UUID.randomUUID().toString(), orderId, itemId, qty, p.getPrice());
                details.add(d);
                totalRevenue += p.getPrice() * qty;
            } else {
                FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
                if (fsi != null) {
                    boolean success = flashSaleItemRepo.sellWithOptimisticLock(itemId, qty);
                    if (!success) return error("San pham Flash Sale " + itemId + " khong du ton kho, vui long thu lai!");
                    
                    // Deduct from original product stock
                    boolean prodSuccess = productRepo.sellWithOptimisticLock(fsi.getProductId(), qty);
                    if (!prodSuccess) {
                        fsi.setSoldQty(fsi.getSoldQty() - qty);
                        flashSaleItemRepo.update(fsi);
                        return error("San pham goc cua Flash Sale khong du ton kho!");
                    }
                    
                    OrderDetail d = new OrderDetail(UUID.randomUUID().toString(), orderId, itemId, qty, fsi.getSalePrice());
                    details.add(d);
                    totalRevenue += fsi.getSalePrice() * qty;
                }
            }
        }
        
        // 3. Luu vao DB
        orderRepo.add(order);
        for (OrderDetail d : details) {
            detailRepo.add(d);
        }
        
        // 4. Clear cart
        getCart().clear();
        saveCartsToFile();
        
        return success("Dat hang thanh cong! Ma don hang cua ban la: " + orderId, null);
    }

    // =====================================================================
    // QUAN LY DON HANG VA SAN PHAM
    // =====================================================================

    public ControllerResult searchProducts(String keyword) {
        requireCustomer();
        List<Product> list = productRepo.getAll();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase();
            list = list.stream().filter(p -> p.getName().toLowerCase().contains(kw)).collect(java.util.stream.Collectors.toList());
        }
        return success("Tim thay " + list.size() + " san pham.", list);
    }

    public ControllerResult filterProductsByCategory(String category) {
        requireCustomer();
        List<Product> list = productRepo.getAll();
        if (category != null && !category.trim().isEmpty()) {
            String cat = category.toLowerCase();
            list = list.stream().filter(p -> p.getCategory().toLowerCase().contains(cat)).collect(java.util.stream.Collectors.toList());
        }
        return success("Tim thay " + list.size() + " san pham.", list);
    }

    public ControllerResult cancelOrder(String orderId) {
        requireCustomer();
        Customer c = (Customer) authState.getCurrentUser();
        
        if (orderId == null || orderId.trim().isEmpty()) return error("ID khong duoc de trong");
        
        Order target = orderRepo.getById(orderId);
        if (target == null || !target.getCustomerId().equals(c.getId())) {
            return error("Khong tim thay don hang cua ban.");
        }
        if (target.getStatus() != OrderStatus.PENDING) {
            return error("Chi co the huy don hang dang o trang thai PENDING!");
        }
        
        // Hoan kho
        List<OrderDetail> details = detailRepo.findByOrderId(orderId);
        for (OrderDetail detail : details) {
            String itemId = detail.getFlashSaleItemId();
            int qty = detail.getQuantity();
            
            Product p = productRepo.getById(itemId);
            if (p != null) {
                p.setStock(p.getStock() + qty);
                productRepo.update(p);
            } else {
                FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
                if (fsi != null) {
                    fsi.setSoldQty(fsi.getSoldQty() - qty);
                    flashSaleItemRepo.update(fsi);
                    
                    Product origP = productRepo.getById(fsi.getProductId());
                    if (origP != null) {
                        origP.setStock(origP.getStock() + qty);
                        productRepo.update(origP);
                    }
                }
            }
        }
        
        target.setStatus(OrderStatus.CANCELLED);
        orderRepo.update(target);
        return success("Da huy don hang (CANCELLED) va hoan kho: " + orderId, target);
    }

    /**
     * Tra ve danh sach chi tiet tung item trong gio hang cua Customer hien tai.
     * Moi phan tu la Object[]: {maItem, tenSP, soLuong, donGia}
     * Dung cho View de hien thi bang gio hang day du thong tin.
     */
    public ControllerResult getCartItemDetails() {
        requireCustomer();
        Map<String, Integer> cart = getCart();
        List<Object[]> details = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int qty = entry.getValue();
            String name = "(Khong ro)"; 
            double price = 0.0;

            Product p = productRepo.getById(itemId);
            if (p != null) {
                name  = p.getName();
                price = p.getPrice();
            } else {
                FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
                if (fsi != null) {
                    price = fsi.getSalePrice();
                    Product origP = productRepo.getById(fsi.getProductId());
                    name = (origP != null ? origP.getName() : itemId) + " [Flash Sale]";
                }
            }
            details.add(new Object[]{itemId, name, qty, price});
        }
        return success("Lay chi tiet gio hang thanh cong", details);
    }

    /**
     * Checkout chi cac item duoc chon (theo danh sach ma item).
     * Sau khi checkout thanh cong, xoa cac item da thanh toan khoi gio.
     * Cac item chua chon duoc giu lai trong gio.
     *
     * @param selectedItemIds Danh sach ma item can thanh toan
     */
    public ControllerResult checkoutSelectedItems(List<String> selectedItemIds) {
        requireCustomer();
        if (selectedItemIds == null || selectedItemIds.isEmpty()) {
            return error("Khong co san pham nao duoc chon de thanh toan!");
        }
        Map<String, Integer> cart = getCart();
        if (cart.isEmpty()) return error("Gio hang dang trong!");

        // Kiem tra tat ca item duoc chon ton tai trong gio
        for (String id : selectedItemIds) {
            if (!cart.containsKey(id)) {
                return error("Item '" + id + "' khong co trong gio hang!");
            }
        }

        Customer c = (Customer) authState.getCurrentUser();

        // Tao Order cha
        int maxNum = 0;
        for (Order o : orderRepo.getAll()) {
            String id = o.getId();
            if (id != null) {
                String numStr = id.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    try { int num = Integer.parseInt(numStr); if (num > maxNum) maxNum = num; }
                    catch (NumberFormatException ignored) {}
                }
            }
        }
        String orderId = String.format("O%05d", maxNum + 1);
        Order order = new Order(orderId, c.getId(), LocalDateTime.now(), OrderStatus.PENDING);

        List<OrderDetail> details = new ArrayList<>();

        // Duyet tung item duoc chon
        for (String itemId : selectedItemIds) {
            int qty = cart.get(itemId);
            Product p = productRepo.getById(itemId);
            if (p != null) {
                boolean ok = productRepo.sellWithOptimisticLock(itemId, qty);
                if (!ok) return error("San pham " + p.getName() + " khong du ton kho!");
                details.add(new OrderDetail(UUID.randomUUID().toString(), orderId, itemId, qty, p.getPrice()));
            } else {
                FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
                if (fsi != null) {
                    boolean ok = flashSaleItemRepo.sellWithOptimisticLock(itemId, qty);
                    if (!ok) return error("San pham Flash Sale " + itemId + " khong du ton kho!");
                    boolean prodOk = productRepo.sellWithOptimisticLock(fsi.getProductId(), qty);
                    if (!prodOk) {
                        fsi.setSoldQty(fsi.getSoldQty() - qty);
                        flashSaleItemRepo.update(fsi);
                        return error("San pham goc cua Flash Sale khong du ton kho!");
                    }
                    details.add(new OrderDetail(UUID.randomUUID().toString(), orderId, itemId, qty, fsi.getSalePrice()));
                }
            }
        }

        // Luu Order va OrderDetail
        orderRepo.add(order);
        for (OrderDetail d : details) detailRepo.add(d);

        // Xoa cac item da thanh toan khoi gio, giu lai phan con lai
        for (String itemId : selectedItemIds) cart.remove(itemId);
        saveCartsToFile();

        return success("Dat hang thanh cong! Ma don hang cua ban la: " + orderId, null);
    }

    /**
     * Mua truc tiep 1 san pham (san pham thuong hoac Flash Sale Item)
     * ma khong them vao gio hang.
     * Tao Order ngay lap tuc voi trang thai PENDING.
     *
     * @param itemId Ma san pham (Product ID hoac FlashSaleItem ID)
     * @param qty    So luong mua
     */
    public ControllerResult directPurchase(String itemId, int qty) {
        requireCustomer();
        if (qty <= 0) return error("So luong phai lon hon 0!");

        Customer c = (Customer) authState.getCurrentUser();

        // Tao Order ID tu dong
        int maxNum = 0;
        for (Order o : orderRepo.getAll()) {
            String id = o.getId();
            if (id != null) {
                String numStr = id.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    try { int num = Integer.parseInt(numStr); if (num > maxNum) maxNum = num; }
                    catch (NumberFormatException ignored) {}
                }
            }
        }
        String orderId = String.format("O%05d", maxNum + 1);
        Order order = new Order(orderId, c.getId(), LocalDateTime.now(), OrderStatus.PENDING);

        OrderDetail detail;
        Product p = productRepo.getById(itemId);
        if (p != null) {
            if (qty > p.getStock()) {
                return error("So luong vuot qua ton kho (Hien co: " + p.getStock() + ")!");
            }
            boolean ok = productRepo.sellWithOptimisticLock(itemId, qty);
            if (!ok) return error("San pham " + p.getName() + " khong du ton kho, vui long thu lai!");
            detail = new OrderDetail(UUID.randomUUID().toString(), orderId, itemId, qty, p.getPrice());
        } else {
            FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
            if (fsi == null) return error("Khong tim thay san pham voi ma: " + itemId);
            int stockLeft = fsi.getLimitedQty() - fsi.getSoldQty();
            if (qty > stockLeft) {
                return error("So luong vuot qua ton kho Flash Sale (Con lai: " + stockLeft + ")!");
            }
            boolean ok = flashSaleItemRepo.sellWithOptimisticLock(itemId, qty);
            if (!ok) return error("San pham Flash Sale " + itemId + " khong du ton kho, vui long thu lai!");
            boolean prodOk = productRepo.sellWithOptimisticLock(fsi.getProductId(), qty);
            if (!prodOk) {
                fsi.setSoldQty(fsi.getSoldQty() - qty);
                flashSaleItemRepo.update(fsi);
                return error("San pham goc cua Flash Sale khong du ton kho!");
            }
            detail = new OrderDetail(UUID.randomUUID().toString(), orderId, itemId, qty, fsi.getSalePrice());
        }

        orderRepo.add(order);
        detailRepo.add(detail);

        return success("Mua truc tiep thanh cong! Ma don hang: " + orderId, null);
    }

    // =====================================================================
    // GETTER
    // =====================================================================


    /**
     * Lay CustomerRepository (dung cho Admin hoac Unit Test).
     * @return CustomerRepository
     */
    public CustomerRepository getCustomerRepo() {
        return customerRepo;
    }
}

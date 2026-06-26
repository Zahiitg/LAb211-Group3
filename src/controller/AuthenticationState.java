package controller;

import model.Admin;
import model.Customer;
import model.Guest;
import model.Seller;
import model.User;
import model.enums.AccountStatus;
import repository.AdminRepository;
import repository.CustomerRepository;
import repository.SellerRepository;
import repository.ProductRepository;
import repository.FlashSaleItemRepository;
import repository.FlashSaleEventRepository;
import repository.OrderRepository;
import repository.OrderDetailRepository;
import repository.OrderTransactionRepository;
import repository.CategoryRepository;

/**
 * Singleton quan ly trang thai xac thuc (phien dang nhap) cua toan bo ung dung.
 *
 * Tai sao dung Singleton?
 * - Ung dung Console chi co DUY NHAT 1 nguoi dung tai 1 thoi diem.
 * - Tat ca Controller chi can goi AuthenticationState.getInstance() de biet
 *   ai dang dang nhap ma KHONG can truyen tham so lien tuc qua cac tang.
 *
 * Quy tac dang nhap (da duoc Team Leader xac nhan):
 * - Tai khoan APPROVED : Cho phep dang nhap DAY DU quyen.
 * - Tai khoan PENDING  : CHO PHEP dang nhap (xem duoc thong tin),
 *                        nhung Controller con co the tu han che quyen (vi du: khong cho mua hang).
 * - Tai khoan BANNED   : KHONG cho phep dang nhap.
 *
 * Luu tru Repository dung chung:
 * - AuthenticationState giu cac Repository lien quan den xac thuc (Customer, Seller, Admin).
 * - Cac Controller con truy cap Repository qua authState.getCustomerRepo(), v.v.
 * - Dieu nay dam bao TOAN BO he thong dung CHUNG MOT CACHE, tranh tinh trang
 *   2 Repository cung doc 1 file nhung Cache bi lech nhau.
 *
 * @author Thanh vien 1 - Core Architecture
 */
public class AuthenticationState {

    // =====================================================================
    // SINGLETON PATTERN
    // =====================================================================

    /** Instance duy nhat cua lop */
    private static AuthenticationState instance;

    /** Nguoi dung dang dang nhap (null = chua dang nhap) */
    private User currentUser;

    // =====================================================================
    // REPOSITORIES DUNG CHUNG
    // =====================================================================

    private CustomerRepository customerRepo;
    private SellerRepository sellerRepo;
    private AdminRepository adminRepo;
    private ProductRepository productRepo;
    private FlashSaleItemRepository flashSaleItemRepo;
    private FlashSaleEventRepository flashSaleEventRepo;
    private OrderRepository orderRepo;
    private OrderDetailRepository detailRepo;
    private OrderTransactionRepository txRepo;
    private CategoryRepository categoryRepo;

    // =====================================================================
    // CONSTRUCTOR & SINGLETON ACCESS
    // =====================================================================

    /**
     * Constructor mac dinh - doc du lieu tu duong dan CSV chuan cua du an.
     */
    private AuthenticationState() {
        this.customerRepo = new CustomerRepository("data/customers.csv");
        this.sellerRepo = new SellerRepository("data/sellers.csv");
        this.adminRepo = new AdminRepository("data/admins.csv");
        this.productRepo = new ProductRepository("data/products.csv");
        this.flashSaleItemRepo = new FlashSaleItemRepository("data/flash_items.csv");
        this.flashSaleEventRepo = new FlashSaleEventRepository("data/flash_events.csv");
        this.orderRepo = new OrderRepository("data/orders.csv");
        this.detailRepo = new OrderDetailRepository("data/order_details.csv");
        this.txRepo = new OrderTransactionRepository("data/transactions.csv");
        this.categoryRepo = new CategoryRepository("data/categories.csv");
    }

    /**
     * Lay instance duy nhat cua AuthenticationState.
     * Neu chua co, tu dong tao moi.
     *
     * @return Singleton instance
     */
    public static AuthenticationState getInstance() {
        if (instance == null) {
            instance = new AuthenticationState();
        }
        return instance;
    }

    /**
     * Reset Singleton voi Repository tuy chinh — DANH RIENG CHO TESTING.
     * Cho phep inject du lieu gia (mock data) de test doc lap,
     * khong anh huong den file CSV that.
     *
     * @param customerRepo Repository Customer test
     * @param sellerRepo   Repository Seller test
     * @param adminRepo    Repository Admin test
     */
    public static void resetForTesting(CustomerRepository customerRepo,
                                       SellerRepository sellerRepo,
                                       AdminRepository adminRepo) {
        instance = new AuthenticationState();
        instance.customerRepo = customerRepo;
        instance.sellerRepo = sellerRepo;
        instance.adminRepo = adminRepo;
        instance.currentUser = null;
    }

    // =====================================================================
    // TRUY CAP REPOSITORY DUNG CHUNG
    // =====================================================================

    public ProductRepository getProductRepo() { return productRepo; }
    public FlashSaleItemRepository getFlashSaleItemRepo() { return flashSaleItemRepo; }
    public FlashSaleEventRepository getFlashSaleEventRepo() { return flashSaleEventRepo; }
    public OrderRepository getOrderRepo() { return orderRepo; }
    public OrderDetailRepository getDetailRepo() { return detailRepo; }
    public OrderTransactionRepository getTxRepo() { return txRepo; }
    public CategoryRepository getCategoryRepo() { return categoryRepo; }

    // =====================================================================
    // DANG NHAP
    // =====================================================================

    /**
     * Dang nhap vao he thong.
     *
     * Luan trinh tim kiem: Customer → Seller → Admin (theo thu tu uu tien).
     * Neu tim thay email va dung password → kiem tra trang thai tai khoan:
     *   - BANNED  → Tu choi dang nhap.
     *   - PENDING → Cho phep dang nhap (kem canh bao).
     *   - APPROVED → Cho phep dang nhap binh thuong.
     *
     * @param email    Email dang nhap
     * @param password Mat khau
     * @return ControllerResult chua thong tin thanh cong/that bai va User data
     */
    public ControllerResult login(String email, String password) {
        // Kiem tra da dang nhap chua
        if (currentUser != null) {
            return ControllerResult.error(
                "Da co tai khoan dang dang nhap (" + currentUser.getEmail()
                + ")! Vui long dang xuat truoc.");
        }

        // Validate dau vao
        if (email == null || email.trim().isEmpty()) {
            return ControllerResult.error("Email khong duoc de trong!");
        }
        if (password == null || password.trim().isEmpty()) {
            return ControllerResult.error("Mat khau khong duoc de trong!");
        }

        // Tim trong bang Customer
        Customer customer = customerRepo.getByEmail(email.trim());
        if (customer != null && customer.getPassword().equals(password)) {
            return processLogin(customer, "Customer");
        }

        // Tim trong bang Seller
        Seller seller = sellerRepo.getByEmail(email.trim());
        if (seller != null && seller.getPassword().equals(password)) {
            return processLogin(seller, "Seller");
        }

        // Tim trong bang Admin
        Admin admin = adminRepo.getByEmail(email.trim());
        if (admin != null && admin.getPassword().equals(password)) {
            return processLogin(admin, "Admin");
        }

        // Khong tim thay hoac sai mat khau
        return ControllerResult.error("Email hoac mat khau khong chinh xac!");
    }

    /**
     * Xu ly logic sau khi xac minh dung email + password.
     * Kiem tra trang thai tai khoan truoc khi cap phep dang nhap.
     *
     * @param user Nguoi dung da xac minh thanh cong
     * @param role Ten vai tro (de hien thi thong bao)
     * @return ControllerResult
     */
    private ControllerResult processLogin(User user, String role) {
        // BANNED → Tu choi tuyet doi
        if (user.getStatus() == AccountStatus.BANNED) {
            return ControllerResult.error(
                "Tai khoan cua ban da bi khoa (BANNED). Vui long lien he Admin de duoc ho tro!");
        }

        // Luu trang thai dang nhap
        this.currentUser = user;

        // Tao thong bao phan hoi
        String statusWarning = "";
        if (user.getStatus() == AccountStatus.PENDING) {
            statusWarning = " (Luu y: Tai khoan dang cho duyet, "
                          + "mot so chuc nang co the bi han che)";
        }

        return ControllerResult.success(
            "Dang nhap thanh cong! Xin chao " + role + " " + user.getName() + statusWarning,
            user
        );
    }

    // =====================================================================
    // DANG XUAT
    // =====================================================================

    /**
     * Dang xuat khoi he thong.
     * Xoa trang thai nguoi dung hien tai.
     *
     * @return ControllerResult thong bao ket qua
     */
    public ControllerResult logout() {
        if (currentUser == null) {
            return ControllerResult.error("Hien tai chua co tai khoan nao dang nhap!");
        }

        String name = currentUser.getName();
        this.currentUser = null;

        return ControllerResult.success("Da dang xuat thanh cong. Tam biet " + name + "!");
    }

    // =====================================================================
    // DANG NHAP TRUC TIEP (DANH CHO CONTROLLER CON)
    // =====================================================================

    /**
     * Luu truc tiep User da duoc xac thuc vao phien dang nhap.
     *
     * Phuong thuc nay KHAC voi login(email, password):
     * - login(email, password): Tu dong tim kiem trong DB va validate.
     * - loginDirect(user): Nhan User da duoc Controller con validate san,
     *   chi viec luu vao phien. Tranh viec Controller con phai goi
     *   login(email, password) roi AuthenticationState lai validate lan nua.
     *
     * @param user Doi tuong User da duoc xac thuc thanh cong
     */
    public void loginDirect(User user) {
        this.currentUser = user;
    }

    // =====================================================================
    // KIEM TRA TRANG THAI DANG NHAP
    // =====================================================================

    /**
     * Lay User dang dang nhap.
     * @return User hien tai, hoac null neu chua dang nhap
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiem tra da dang nhap chua.
     * @return true neu da dang nhap
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Kiem tra nguoi dang nhap co phai Admin khong.
     * @return true neu dang nhap va la Admin
     */
    public boolean isAdmin() {
        return currentUser instanceof Admin;
    }

    /**
     * Kiem tra nguoi dang nhap co phai Customer khong.
     * @return true neu dang nhap va la Customer
     */
    public boolean isCustomer() {
        return currentUser instanceof Customer;
    }

    /**
     * Kiem tra nguoi dang nhap co phai Seller khong.
     * @return true neu dang nhap va la Seller
     */
    public boolean isSeller() {
        return currentUser instanceof Seller;
    }

    /**
     * Kiem tra nguoi dang nhap co phai Guest khong.
     * @return true neu dang nhap va la Guest
     */
    public boolean isGuest() {
        return currentUser instanceof Guest;
    }

    /**
     * Dang nhap nhanh voi vai tro Guest (Khach).
     * Tao mot doi tuong Guest va luu vao phien.
     */
    public void loginAsGuest() {
        if (currentUser != null) return; // Neu da dang nhap roi thi bo qua
        Guest guest = new Guest(java.util.UUID.randomUUID().toString().substring(0, 8));
        this.currentUser = guest;
    }

    // =====================================================================
    // TRUY CAP REPOSITORY DUNG CHUNG
    // Cac Controller con goi cac ham nay de thao tac du lieu,
    // dam bao dung chung 1 Cache voi he thong xac thuc.
    // =====================================================================

    /** @return CustomerRepository dung chung */
    public CustomerRepository getCustomerRepo() {
        return customerRepo;
    }

    /** @return SellerRepository dung chung */
    public SellerRepository getSellerRepo() {
        return sellerRepo;
    }

    /** @return AdminRepository dung chung */
    public AdminRepository getAdminRepo() {
        return adminRepo;
    }
}

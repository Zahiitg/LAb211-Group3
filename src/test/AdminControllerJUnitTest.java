package test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import controller.AdminController;
import controller.AuthenticationState;
import controller.ControllerResult;
import model.Customer;
import model.Seller;
import model.enums.AccountStatus;
import repository.AdminRepository;
import repository.CustomerRepository;
import repository.SellerRepository;

/**
 * JUnit Test cho toan bo tang Controller Phase TV1:
 * - AuthenticationState (Login / Logout / Phan quyen)
 * - BaseController (requireLogin, requireAdmin, requireCustomer, requireSeller)
 * - AdminController (listAll, ban, approve)
 *
 * Su dung file CSV tam de khong anh huong du lieu that.
 * Moi test case chay doc lap nho @Before/@After.
 *
 * @author Thanh vien 1 - Core Architecture
 */
public class AdminControllerJUnitTest {

    // =====================================================================
    // FILE CSV TAM DUNG CHO TESTING
    // =====================================================================

    private static final String TEST_CUSTOMERS_FILE = "test_ctrl_customers.csv";
    private static final String TEST_SELLERS_FILE   = "test_ctrl_sellers.csv";
    private static final String TEST_ADMINS_FILE    = "test_ctrl_admins.csv";
    private static final String TEST_ORDERS_FILE    = "test_ctrl_orders.csv";
    private static final String TEST_DETAILS_FILE   = "test_ctrl_details.csv";
    private static final String TEST_PRODUCTS_FILE  = "test_ctrl_products.csv";

    private AuthenticationState authState;
    private AdminController adminController;

    // =====================================================================
    // SETUP & TEARDOWN
    // =====================================================================

    @Before
    public void setUp() throws IOException {
        // Xoa file cu (neu ton tai tu lan chay truoc bi loi)
        cleanUpFiles();

        // Tao file Customer test: 3 tai khoan voi 3 trang thai khac nhau
        // Format: id,name,email,password,status,tier
        try (FileWriter fw = new FileWriter(TEST_CUSTOMERS_FILE)) {
            fw.write("id,name,email,password,status,tier\n");
            fw.write("C001,Nguyen Van A,a@test.com,pass123,APPROVED,SILVER\n");
            fw.write("C002,Tran Van B,b@test.com,pass456,PENDING,BRONZE\n");
            fw.write("C003,Le Van C,c@test.com,pass789,BANNED,GOLD\n");
        }

        // Tao file Seller test: 2 tai khoan
        // Format: id,name,email,password,status,storeName
        try (FileWriter fw = new FileWriter(TEST_SELLERS_FILE)) {
            fw.write("id,name,email,password,status,storeName\n");
            fw.write("S001,Shop Owner 1,shop1@test.com,shoppass1,APPROVED,MyShop\n");
            fw.write("S002,Shop Owner 2,shop2@test.com,shoppass2,PENDING,YourShop\n");
        }

        // Tao file Admin test: 1 tai khoan
        // Format: id,name,email,password,status,roleLevel
        try (FileWriter fw = new FileWriter(TEST_ADMINS_FILE)) {
            fw.write("id,name,email,password,status,roleLevel\n");
            fw.write("A001,Super Admin,admin@test.com,admin123,APPROVED,1\n");
        }

        // Reset Singleton voi Repository test
        CustomerRepository customerRepo = new CustomerRepository(TEST_CUSTOMERS_FILE);
        SellerRepository sellerRepo     = new SellerRepository(TEST_SELLERS_FILE);
        AdminRepository adminRepo       = new AdminRepository(TEST_ADMINS_FILE);

        AuthenticationState.resetForTesting(customerRepo, sellerRepo, adminRepo);
        authState = AuthenticationState.getInstance();
        adminController = new AdminController();
        
        try {
            // Tao file Orders test
            try (FileWriter fw = new FileWriter(TEST_ORDERS_FILE)) {
                fw.write("id,customerId,orderTime,status\n");
                fw.write("O001,C001,2023-10-01T10:00:00,PENDING\n");
                fw.write("O002,C001,2023-10-01T11:00:00,VERIFIED\n");
                fw.write("O003,C001,2023-10-01T12:00:00,COMPLETED\n");
            }
            
            // Tao file OrderDetails test
            try (FileWriter fw = new FileWriter(TEST_DETAILS_FILE)) {
                fw.write("id,orderId,flashSaleItemId,quantity,priceAtPurchase\n");
                fw.write("D001,O003,P001,2,50000.0\n"); // Tinh vao doanh thu cua O003
            }
            
            // Inject Test Repositories via Reflection de khong anh huong data that
            java.lang.reflect.Field orderRepoField = AdminController.class.getDeclaredField("orderRepo");
            orderRepoField.setAccessible(true);
            orderRepoField.set(adminController, new repository.OrderRepository(TEST_ORDERS_FILE));
            
            java.lang.reflect.Field detailRepoField = AdminController.class.getDeclaredField("detailRepo");
            detailRepoField.setAccessible(true);
            detailRepoField.set(adminController, new repository.OrderDetailRepository(TEST_DETAILS_FILE));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        // Dang xuat neu con phien dang nhap
        if (authState != null && authState.isLoggedIn()) {
            authState.logout();
        }
        // Don dep file test
        cleanUpFiles();
    }

    private void cleanUpFiles() {
        new File(TEST_CUSTOMERS_FILE).delete();
        new File(TEST_SELLERS_FILE).delete();
        new File(TEST_ADMINS_FILE).delete();
        new File(TEST_ORDERS_FILE).delete();
        new File(TEST_DETAILS_FILE).delete();
        new File(TEST_PRODUCTS_FILE).delete();
    }

    // =====================================================================
    // NHOM 1: TEST DANG NHAP (LOGIN)
    // =====================================================================

    @Test
    public void testLoginCustomerApproved() {
        ControllerResult result = authState.login("a@test.com", "pass123");

        assertTrue("Customer APPROVED phai dang nhap thanh cong", result.isSuccess());
        assertNotNull("Data phai chua User object", result.getData());
        assertTrue("Phai nhan dien la Customer", authState.isCustomer());
        assertFalse("Khong phai Admin", authState.isAdmin());
        assertFalse("Khong phai Seller", authState.isSeller());
        assertTrue("Phai dang dang nhap", authState.isLoggedIn());
    }

    @Test
    public void testLoginCustomerPendingAllowed() {
        ControllerResult result = authState.login("b@test.com", "pass456");

        assertTrue("Tai khoan PENDING PHAI duoc phep dang nhap", result.isSuccess());
        assertTrue("Message phai chua canh bao ve PENDING",
            result.getMessage().contains("cho duyet"));
        assertNotNull("Data phai co User", result.getData());
    }

    @Test
    public void testLoginCustomerBannedDenied() {
        ControllerResult result = authState.login("c@test.com", "pass789");

        assertFalse("Tai khoan BANNED KHONG duoc dang nhap", result.isSuccess());
        assertTrue("Message phai thong bao bi khoa",
            result.getMessage().contains("BANNED"));
        assertFalse("Khong duoc luu trang thai dang nhap", authState.isLoggedIn());
    }

    @Test
    public void testLoginSellerSuccess() {
        ControllerResult result = authState.login("shop1@test.com", "shoppass1");

        assertTrue("Seller APPROVED phai dang nhap thanh cong", result.isSuccess());
        assertTrue("Phai nhan dien la Seller", authState.isSeller());
    }

    @Test
    public void testLoginAdminSuccess() {
        ControllerResult result = authState.login("admin@test.com", "admin123");

        assertTrue("Admin phai dang nhap thanh cong", result.isSuccess());
        assertTrue("Phai nhan dien la Admin", authState.isAdmin());
        assertNotNull("getCurrentUser phai tra ve Admin", authState.getCurrentUser());
    }

    @Test
    public void testLoginWrongPassword() {
        ControllerResult result = authState.login("a@test.com", "wrong_password");

        assertFalse("Sai mat khau phai that bai", result.isSuccess());
        assertFalse("Khong duoc luu trang thai", authState.isLoggedIn());
    }

    @Test
    public void testLoginWrongEmail() {
        ControllerResult result = authState.login("nonexistent@test.com", "pass123");

        assertFalse("Email khong ton tai phai that bai", result.isSuccess());
    }

    @Test
    public void testLoginEmptyEmail() {
        ControllerResult result = authState.login("", "pass123");

        assertFalse("Email rong phai that bai", result.isSuccess());
        assertTrue("Message phai noi ve email",
            result.getMessage().toLowerCase().contains("email"));
    }

    @Test
    public void testLoginEmptyPassword() {
        ControllerResult result = authState.login("a@test.com", "");

        assertFalse("Mat khau rong phai that bai", result.isSuccess());
    }

    @Test
    public void testLoginDoubleLoginBlocked() {
        authState.login("a@test.com", "pass123");

        ControllerResult result = authState.login("admin@test.com", "admin123");

        assertFalse("Khong duoc dang nhap 2 tai khoan cung luc", result.isSuccess());
        assertTrue("Message phai yeu cau dang xuat truoc",
            result.getMessage().contains("dang xuat"));
    }

    // =====================================================================
    // NHOM 2: TEST DANG XUAT (LOGOUT)
    // =====================================================================

    @Test
    public void testLogoutSuccess() {
        authState.login("a@test.com", "pass123");
        assertTrue("Phai dang nhap truoc", authState.isLoggedIn());

        ControllerResult result = authState.logout();

        assertTrue("Dang xuat phai thanh cong", result.isSuccess());
        assertFalse("Sau dang xuat, isLoggedIn phai false", authState.isLoggedIn());
        assertNull("Sau dang xuat, getCurrentUser phai null", authState.getCurrentUser());
    }

    @Test
    public void testLogoutWithoutLogin() {
        ControllerResult result = authState.logout();

        assertFalse("Dang xuat khi chua dang nhap phai that bai", result.isSuccess());
    }

    @Test
    public void testLoginAfterLogout() {
        authState.login("a@test.com", "pass123");
        authState.logout();

        ControllerResult result = authState.login("admin@test.com", "admin123");

        assertTrue("Phai dang nhap lai duoc sau khi dang xuat", result.isSuccess());
        assertTrue("Phai la Admin", authState.isAdmin());
    }

    // =====================================================================
    // NHOM 3: TEST PHAN QUYEN (AUTHENTICATION GUARDS)
    // =====================================================================

    @Test(expected = IllegalStateException.class)
    public void testAdminFunctionWithoutLogin() {
        // Goi ham Admin khi CHUA dang nhap → Phai nem exception
        adminController.listAllCustomers();
    }

    @Test(expected = IllegalStateException.class)
    public void testCustomerCannotAccessAdminFunction() {
        // Dang nhap bang Customer, roi thu goi ham Admin → Phai nem exception
        authState.login("a@test.com", "pass123");
        adminController.listAllCustomers();
    }

    @Test(expected = IllegalStateException.class)
    public void testSellerCannotAccessAdminFunction() {
        // Dang nhap bang Seller, roi thu goi ham Admin → Phai nem exception
        authState.login("shop1@test.com", "shoppass1");
        adminController.banCustomer("C001");
    }

    // =====================================================================
    // NHOM 4: TEST ADMIN CONTROLLER - XEM DANH SACH
    // =====================================================================

    @Test
    public void testListAllCustomers() {
        authState.login("admin@test.com", "admin123");

        ControllerResult result = adminController.listAllCustomers();

        assertTrue("Phai thanh cong", result.isSuccess());
        assertNotNull("Data phai co", result.getData());

        @SuppressWarnings("unchecked")
        List<Customer> customers = (List<Customer>) result.getData();
        assertEquals("Phai co 3 Customer trong file test", 3, customers.size());
    }

    @Test
    public void testListAllSellers() {
        authState.login("admin@test.com", "admin123");

        ControllerResult result = adminController.listAllSellers();

        assertTrue("Phai thanh cong", result.isSuccess());

        @SuppressWarnings("unchecked")
        List<Seller> sellers = (List<Seller>) result.getData();
        assertEquals("Phai co 2 Seller trong file test", 2, sellers.size());
    }

    // =====================================================================
    // NHOM 5: TEST ADMIN CONTROLLER - KHOA TAI KHOAN (BAN)
    // =====================================================================

    @Test
    public void testBanCustomerSuccess() {
        authState.login("admin@test.com", "admin123");

        ControllerResult result = adminController.banCustomer("C001");

        assertTrue("Ban phai thanh cong", result.isSuccess());

        // Kiem tra du lieu da thay doi thuc su trong Cache + File
        Customer banned = authState.getCustomerRepo().getById("C001");
        assertEquals("Trang thai phai la BANNED", AccountStatus.BANNED, banned.getStatus());
    }

    @Test
    public void testBanCustomerAlreadyBanned() {
        authState.login("admin@test.com", "admin123");

        // C003 da co trang thai BANNED san trong file test
        ControllerResult result = adminController.banCustomer("C003");

        assertFalse("Ban nguoi da bi ban phai tra ve loi", result.isSuccess());
        assertTrue("Message phai noi da bi khoa tu truoc",
            result.getMessage().contains("da bi khoa tu truoc"));
    }

    @Test
    public void testBanCustomerNotFound() {
        authState.login("admin@test.com", "admin123");

        ControllerResult result = adminController.banCustomer("C999");

        assertFalse("Ban ID khong ton tai phai tra ve loi", result.isSuccess());
        assertTrue("Message phai noi khong tim thay",
            result.getMessage().contains("Khong tim thay"));
    }

    @Test
    public void testBanSellerSuccess() {
        authState.login("admin@test.com", "admin123");

        ControllerResult result = adminController.banSeller("S001");

        assertTrue("Ban Seller phai thanh cong", result.isSuccess());

        Seller banned = authState.getSellerRepo().getById("S001");
        assertEquals("Trang thai phai la BANNED", AccountStatus.BANNED, banned.getStatus());
    }

    // =====================================================================
    // NHOM 6: TEST ADMIN CONTROLLER - DUYET TAI KHOAN (APPROVE)
    // =====================================================================

    @Test
    public void testApproveCustomerSuccess() {
        authState.login("admin@test.com", "admin123");

        // C002 dang o trang thai PENDING
        ControllerResult result = adminController.approveCustomer("C002");

        assertTrue("Approve phai thanh cong", result.isSuccess());

        Customer approved = authState.getCustomerRepo().getById("C002");
        assertEquals("Trang thai phai la APPROVED",
            AccountStatus.APPROVED, approved.getStatus());
    }

    @Test
    public void testApproveCustomerAlreadyApproved() {
        authState.login("admin@test.com", "admin123");

        // C001 da co trang thai APPROVED san
        ControllerResult result = adminController.approveCustomer("C001");

        assertFalse("Approve nguoi da duyet phai tra ve loi", result.isSuccess());
    }

    @Test
    public void testApproveSellerSuccess() {
        authState.login("admin@test.com", "admin123");

        // S002 dang o trang thai PENDING
        ControllerResult result = adminController.approveSeller("S002");

        assertTrue("Approve Seller phai thanh cong", result.isSuccess());

        Seller approved = authState.getSellerRepo().getById("S002");
        assertEquals("Trang thai phai la APPROVED",
            AccountStatus.APPROVED, approved.getStatus());
    }

    // =====================================================================
    // NHOM 7: TEST ADMIN CONTROLLER - MO KHOA TAI KHOAN (UNBAN)
    // =====================================================================

    @Test
    public void testUnbanCustomerSuccess() {
        authState.login("admin@test.com", "admin123");

        // C003 dang o trang thai BANNED
        ControllerResult result = adminController.unbanCustomer("C003");

        assertTrue("Unban phai thanh cong", result.isSuccess());

        Customer unbanned = authState.getCustomerRepo().getById("C003");
        assertEquals("Trang thai phai la APPROVED", AccountStatus.APPROVED, unbanned.getStatus());
    }

    @Test
    public void testUnbanCustomerNotBanned() {
        authState.login("admin@test.com", "admin123");

        // C001 da co trang thai APPROVED
        ControllerResult result = adminController.unbanCustomer("C001");

        assertFalse("Unban nguoi khong bi ban phai tra ve loi", result.isSuccess());
        assertTrue("Message phai noi khong bi khoa", result.getMessage().contains("khong bi khoa"));
    }

    @Test
    public void testUnbanSellerSuccess() {
        authState.login("admin@test.com", "admin123");
        // S001 is APPROVED, S002 is PENDING. We need to ban S001 first to test unban.
        adminController.banSeller("S001");

        ControllerResult result = adminController.unbanSeller("S001");

        assertTrue("Unban Seller phai thanh cong", result.isSuccess());

        Seller unbanned = authState.getSellerRepo().getById("S001");
        assertEquals("Trang thai phai la APPROVED", AccountStatus.APPROVED, unbanned.getStatus());
    }

    // =====================================================================
    // NHOM 8: TEST CONTROLLER RESULT (OBJECT PHUC TAP)
    // =====================================================================

    @Test
    public void testControllerResultToString() {
        ControllerResult successResult = ControllerResult.success("Test OK");
        ControllerResult errorResult = ControllerResult.error("Test FAIL");

        assertTrue("toString thanh cong phai bat dau bang [SUCCESS]",
            successResult.toString().startsWith("[SUCCESS]"));
        assertTrue("toString that bai phai bat dau bang [ERROR]",
            errorResult.toString().startsWith("[ERROR]"));
    }

    @Test
    public void testControllerResultWithData() {
        String testData = "Hello Data";
        ControllerResult result = ControllerResult.success("OK", testData);

        assertTrue(result.isSuccess());
        assertEquals("Data phai dung", "Hello Data", result.getData());
    }

    @Test
    public void testControllerResultWithoutData() {
        ControllerResult result = ControllerResult.success("OK");

        assertTrue(result.isSuccess());
        assertNull("Data phai null khi khong truyen", result.getData());
    }

    // =====================================================================
    // NHOM 9: TEST ADMIN CONTROLLER - QUAN LY DON HANG
    // =====================================================================

    @Test
    public void testListAllOrders() {
        authState.login("admin@test.com", "admin123");
        ControllerResult result = adminController.listAllOrders();
        
        assertTrue("Lay danh sach don hang phai thanh cong", result.isSuccess());
        @SuppressWarnings("unchecked")
        List<model.Order> orders = (List<model.Order>) result.getData();
        assertEquals("Phai co 3 don hang trong file test", 3, orders.size());
    }

    @Test
    public void testVerifyOrderSuccess() {
        authState.login("admin@test.com", "admin123");
        // O001 dang la PENDING
        ControllerResult result = adminController.verifyOrder("O001");
        assertTrue("Duyet don hang PENDING phai thanh cong", result.isSuccess());
        
        model.Order updated = (model.Order) result.getData();
        assertEquals("Trang thai phai chuyen sang VERIFIED", model.enums.OrderStatus.VERIFIED, updated.getStatus());
    }

    @Test
    public void testVerifyOrderWrongStatus() {
        authState.login("admin@test.com", "admin123");
        // O002 dang la VERIFIED
        ControllerResult result = adminController.verifyOrder("O002");
        assertFalse("Duyet don hang khong phai PENDING phai that bai", result.isSuccess());
    }

    @Test
    public void testCompleteOrderSuccess() {
        authState.login("admin@test.com", "admin123");
        // O002 dang la VERIFIED
        ControllerResult result = adminController.completeOrder("O002");
        assertTrue("Hoan thanh don hang VERIFIED phai thanh cong", result.isSuccess());
        
        model.Order updated = (model.Order) result.getData();
        assertEquals("Trang thai phai chuyen sang COMPLETED", model.enums.OrderStatus.COMPLETED, updated.getStatus());
    }

    @Test
    public void testCancelOrderSuccess() {
        authState.login("admin@test.com", "admin123");
        // O001 dang la PENDING
        ControllerResult result = adminController.cancelOrder("O001");
        assertTrue("Huy don hang phai thanh cong", result.isSuccess());
        
        model.Order updated = (model.Order) result.getData();
        assertEquals("Trang thai phai chuyen sang CANCELLED", model.enums.OrderStatus.CANCELLED, updated.getStatus());
    }

    // =====================================================================
    // NHOM 10: TEST ADMIN CONTROLLER - THONG KE & TIM KIEM
    // =====================================================================

    @Test
    public void testGetDashboardStats() {
        authState.login("admin@test.com", "admin123");
        ControllerResult result = adminController.getDashboardStats();
        
        assertTrue("Lay thong ke phai thanh cong", result.isSuccess());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> stats = (java.util.Map<String, Object>) result.getData();
        
        assertTrue("Phai chua totalOrders", stats.containsKey("totalOrders"));
        assertTrue("Phai chua totalRevenue", stats.containsKey("totalRevenue"));
        
        // Kiem tra tinh toan doanh thu: O003 COMPLETED, detail co 2 * 50000 = 100000
        double revenue = (Double) stats.get("totalRevenue");
        assertEquals("Doanh thu phai la 100000", 100000.0, revenue, 0.001);
    }

    @Test
    public void testSearchCustomersFound() {
        authState.login("admin@test.com", "admin123");
        // "Nguyen" match "Nguyen Van A"
        ControllerResult result = adminController.searchCustomers("Nguyen");
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<model.Customer> found = (List<model.Customer>) result.getData();
        assertEquals("Phai tim thay 1 Customer", 1, found.size());
        assertEquals("Ten phai la Nguyen Van A", "Nguyen Van A", found.get(0).getName());
    }

    @Test
    public void testSearchCustomersNotFound() {
        authState.login("admin@test.com", "admin123");
        ControllerResult result = adminController.searchCustomers("NonExistentName");
        assertTrue(result.isSuccess()); // Ham search luon tra ve success ke ca khi result = 0
        
        @SuppressWarnings("unchecked")
        List<model.Customer> found = (List<model.Customer>) result.getData();
        assertEquals("Danh sach phai rong", 0, found.size());
    }
}

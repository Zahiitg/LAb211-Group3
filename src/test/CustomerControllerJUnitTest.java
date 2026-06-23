package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.AuthenticationState;
import controller.ControllerResult;
import controller.CustomerController;
import model.Customer;
import model.enums.AccountStatus;
import model.enums.CustTier;

import java.io.File;

/**
 * Unit Test cho CustomerController (phien ban da Refactor).
 *
 * Tat ca test case deu kiem tra qua ControllerResult:
 * - assertTrue(result.isSuccess())  → Mong doi thanh cong
 * - assertFalse(result.isSuccess()) → Mong doi that bai
 * - result.getData()                → Lay doi tuong Customer tra ve
 * - result.getMessage()             → Kiem tra thong bao loi/thanh cong
 *
 * KHONG CON dung @Test(expected = IllegalArgumentException.class)
 * vi Controller khong con throw Exception nua.
 *
 * @author Thanh vien 2 - Customer Logic
 * @refactored-by Thanh vien 1 - Core Architecture
 */
public class CustomerControllerJUnitTest {

    private static final String TEST_FILE = "test_customers_controller.csv";
    private CustomerController controller;

    @Before
    public void setUp() {
        // Xoa file test cu (neu ton tai) de dam bao moi test chay sach
        new File(TEST_FILE).delete();
        controller = new CustomerController(TEST_FILE);
        // Reset AuthenticationState de cac test khong anh huong lan nhau
        AuthenticationState.getInstance().logout();
    }

    @After
    public void tearDown() {
        // Don dep file test va dang xuat sau moi test
        new File(TEST_FILE).delete();
        AuthenticationState.getInstance().logout();
    }

    // =====================================================================
    // TEST DANG KY - THANH CONG
    // =====================================================================

    @Test
    public void testRegisterSuccess() {
        ControllerResult result = controller.register("Nguyen Van A", "nva@gmail.com", "mysecurepassword", "");

        // Kiem tra ket qua phai thanh cong
        assertTrue("Dang ky phai thanh cong", result.isSuccess());
        assertNotNull("Phai co du lieu Customer tra ve", result.getData());

        // Kiem tra du lieu Customer
        Customer c = (Customer) result.getData();
        assertEquals("C00001", c.getId());
        assertEquals("Nguyen Van A", c.getName());
        assertEquals("nva@gmail.com", c.getEmail());
        assertEquals("mysecurepassword", c.getPassword());
        assertEquals(AccountStatus.APPROVED, c.getStatus());
        assertEquals(CustTier.BRONZE, c.getTier());

        // Kiem tra du lieu da duoc luu xuong file (Persistence)
        CustomerController reloadedController = new CustomerController(TEST_FILE);
        Customer fetched = reloadedController.getCustomerRepo().getById("C00001");
        assertNotNull("Customer phai duoc luu xuong file CSV", fetched);
        assertEquals("Nguyen Van A", fetched.getName());
    }

    // =====================================================================
    // TEST DANG KY - THAT BAI (Validation)
    // =====================================================================

    @Test
    public void testRegisterDuplicateEmail() {
        // Dang ky lan dau → thanh cong
        ControllerResult first = controller.register("User One", "dup@test.com", "pass1", "");
        assertTrue("Dang ky lan dau phai thanh cong", first.isSuccess());

        // Dang ky lan hai voi cung email → THAT BAI (khong throw Exception)
        ControllerResult second = controller.register("User Two", "dup@test.com", "pass2", "");
        assertFalse("Dang ky trung email phai THAT BAI", second.isSuccess());
        assertTrue("Thong bao loi phai chua tu 'da duoc dang ky'",
                   second.getMessage().contains("da duoc dang ky"));
    }

    @Test
    public void testRegisterEmptyName() {
        ControllerResult result = controller.register("", "email@test.com", "pass", "");
        assertFalse("Dang ky voi ten rong phai THAT BAI", result.isSuccess());
        assertTrue("Thong bao loi phai de cap den 'Ten'",
                   result.getMessage().toLowerCase().contains("ten"));
    }

    @Test
    public void testRegisterEmptyEmail() {
        ControllerResult result = controller.register("Name", "   ", "pass", "");
        assertFalse("Dang ky voi email rong phai THAT BAI", result.isSuccess());
        assertTrue("Thong bao loi phai de cap den 'Email'",
                   result.getMessage().toLowerCase().contains("email"));
    }

    @Test
    public void testRegisterEmptyPassword() {
        ControllerResult result = controller.register("Name", "email@test.com", "", "");
        assertFalse("Dang ky voi mat khau rong phai THAT BAI", result.isSuccess());
        assertTrue("Thong bao loi phai de cap den 'Mat khau'",
                   result.getMessage().toLowerCase().contains("mat khau"));
    }

    @Test
    public void testRegisterNullInputs() {
        ControllerResult r1 = controller.register(null, "a@b.com", "pass", "");
        assertFalse("Dang ky voi name = null phai THAT BAI", r1.isSuccess());

        ControllerResult r2 = controller.register("Name", null, "pass", "");
        assertFalse("Dang ky voi email = null phai THAT BAI", r2.isSuccess());

        ControllerResult r3 = controller.register("Name", "a@b.com", null, "");
        assertFalse("Dang ky voi password = null phai THAT BAI", r3.isSuccess());
    }

    // =====================================================================
    // TEST DANG NHAP - THANH CONG
    // =====================================================================

    @Test
    public void testLoginSuccess() {
        // Dang ky truoc
        controller.register("Huy", "huy@gmail.com", "pass123", "");

        // Dang nhap
        ControllerResult result = controller.login("huy@gmail.com", "pass123");
        assertTrue("Dang nhap phai thanh cong", result.isSuccess());
        assertNotNull("Phai co du lieu Customer tra ve", result.getData());

        Customer loggedIn = (Customer) result.getData();
        assertEquals("Huy", loggedIn.getName());
        assertEquals("huy@gmail.com", loggedIn.getEmail());
    }

    @Test
    public void testLoginSetsAuthenticationState() {
        // Dang ky truoc
        controller.register("Huy Auth", "huyauth@gmail.com", "pass123", "");

        // Dam bao chua dang nhap
        assertFalse("Truoc khi login, AuthState phai rong",
                    AuthenticationState.getInstance().isLoggedIn());

        // Dang nhap
        ControllerResult result = controller.login("huyauth@gmail.com", "pass123");
        assertTrue("Dang nhap phai thanh cong", result.isSuccess());

        // Kiem tra AuthenticationState da luu phien
        assertTrue("Sau khi login, AuthState phai ghi nhan da dang nhap",
                   AuthenticationState.getInstance().isLoggedIn());
        assertTrue("Nguoi dang nhap phai la Customer",
                   AuthenticationState.getInstance().isCustomer());
        assertEquals("huyauth@gmail.com",
                     AuthenticationState.getInstance().getCurrentUser().getEmail());
    }

    // =====================================================================
    // TEST DANG NHAP - THAT BAI
    // =====================================================================

    @Test
    public void testLoginWrongPassword() {
        controller.register("Huy", "huy@gmail.com", "pass123", "");

        ControllerResult result = controller.login("huy@gmail.com", "wrongpass");
        assertFalse("Dang nhap sai mat khau phai THAT BAI", result.isSuccess());
        assertNull("Khong duoc tra ve du lieu Customer khi that bai", result.getData());
    }

    @Test
    public void testLoginNonExistentEmail() {
        ControllerResult result = controller.login("nonexistent@gmail.com", "pass123");
        assertFalse("Dang nhap voi email khong ton tai phai THAT BAI", result.isSuccess());
    }

    @Test
    public void testLoginBannedUser() {
        // Dang ky user
        ControllerResult regResult = controller.register("Banned User", "banned@test.com", "pass123", "");
        Customer c = (Customer) regResult.getData();

        // Admin khoa tai khoan
        c.setStatus(AccountStatus.BANNED);
        controller.getCustomerRepo().update(c);

        // Thu dang nhap → THAT BAI vi bi BANNED
        ControllerResult loginResult = controller.login("banned@test.com", "pass123");
        assertFalse("User bi BANNED khong duoc phep dang nhap", loginResult.isSuccess());
        assertTrue("Thong bao phai de cap den 'BANNED'",
                   loginResult.getMessage().contains("BANNED"));

        // Dam bao AuthenticationState KHONG luu phien cua user bi ban
        assertFalse("AuthState KHONG duoc ghi nhan user bi BANNED",
                    AuthenticationState.getInstance().isLoggedIn());
    }

    @Test
    public void testLoginEmptyInputs() {
        ControllerResult r1 = controller.login("", "pass");
        assertFalse("Login voi email rong phai THAT BAI", r1.isSuccess());

        ControllerResult r2 = controller.login("a@b.com", "");
        assertFalse("Login voi mat khau rong phai THAT BAI", r2.isSuccess());

        ControllerResult r3 = controller.login(null, "pass");
        assertFalse("Login voi email null phai THAT BAI", r3.isSuccess());
    }
}

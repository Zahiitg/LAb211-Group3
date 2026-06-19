package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import controller.CustomerController;
import model.Customer;
import model.enums.AccountStatus;
import model.enums.CustTier;

import java.io.File;

public class CustomerControllerJUnitTest {
    private static final String TEST_FILE = "test_customers_controller.csv";
    private CustomerController controller;

    @Before
    public void setUp() {
        // Clean up any existing test file
        new File(TEST_FILE).delete();
        controller = new CustomerController(TEST_FILE);
    }

    @After
    public void tearDown() {
        // Clean up test file after test run
        new File(TEST_FILE).delete();
    }

    @Test
    public void testRegisterSuccess() {
        Customer c = controller.register("Nguyen Van A", "nva@gmail.com", "mysecurepassword");
        assertNotNull("Registered customer should not be null", c);
        assertEquals("C00001", c.getId());
        assertEquals("Nguyen Van A", c.getName());
        assertEquals("nva@gmail.com", c.getEmail());
        assertEquals("mysecurepassword", c.getPassword());
        assertEquals(AccountStatus.APPROVED, c.getStatus());
        assertEquals(CustTier.BRONZE, c.getTier());

        // Verify persistence by reloading
        CustomerController reloadedController = new CustomerController(TEST_FILE);
        Customer fetched = reloadedController.getCustomerRepo().getById("C00001");
        assertNotNull("Customer should be persistent", fetched);
        assertEquals("Nguyen Van A", fetched.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterDuplicateEmail() {
        controller.register("User One", "dup@test.com", "pass1");
        // Should throw IllegalArgumentException
        controller.register("User Two", "dup@test.com", "pass2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterEmptyName() {
        controller.register("", "email@test.com", "pass");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterEmptyEmail() {
        controller.register("Name", "   ", "pass");
    }

    @Test
    public void testLoginSuccess() {
        controller.register("Huy", "huy@gmail.com", "pass123");

        Customer loggedIn = controller.login("huy@gmail.com", "pass123");
        assertNotNull("Login should be successful", loggedIn);
        assertEquals("Huy", loggedIn.getName());
    }

    @Test
    public void testLoginWrongPassword() {
        controller.register("Huy", "huy@gmail.com", "pass123");

        Customer loggedIn = controller.login("huy@gmail.com", "wrongpass");
        assertNull("Login with wrong password should fail", loggedIn);
    }

    @Test
    public void testLoginNonExistentEmail() {
        Customer loggedIn = controller.login("nonexistent@gmail.com", "pass123");
        assertNull("Login with non-existent email should fail", loggedIn);
    }

    @Test
    public void testLoginBannedUser() {
        // Register user
        Customer c = controller.register("Banned User", "banned@test.com", "pass123");
        
        // Manually set status to BANNED and update
        c.setStatus(AccountStatus.BANNED);
        controller.getCustomerRepo().update(c);

        Customer loggedIn = controller.login("banned@test.com", "pass123");
        assertNull("Banned user should not be allowed to log in", loggedIn);
    }
}

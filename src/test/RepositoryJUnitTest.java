package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import model.Customer;
import model.enums.AccountStatus;
import model.enums.CustTier;
import repository.AdminRepository;
import repository.CustomerRepository;
import repository.SellerRepository;

import java.util.List;

/**
 * JUnit Test cho CsvRepository: CRUD + Performance Benchmark.
 * Muc tieu: Doc 2000 dong Customer < 1 giay.
 */
public class RepositoryJUnitTest {

    private CustomerRepository customerRepo;
    private AdminRepository adminRepo;
    private SellerRepository sellerRepo;

    @Before
    public void setUp() {
        customerRepo = new CustomerRepository("data/customers.csv");
        adminRepo = new AdminRepository("data/admins.csv");
        sellerRepo = new SellerRepository("data/sellers.csv");
    }

    // ========================================================================
    // PERFORMANCE BENCHMARK
    // ========================================================================

    @Test
    public void testCustomerLoadPerformance() {
        long start = System.currentTimeMillis();
        CustomerRepository repo = new CustomerRepository("data/customers.csv");
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("[BENCHMARK] CustomerRepo: " + repo.count() + " records in " + elapsed + " ms");
        assertTrue("Doc 2000 dong phai duoi 1000ms, thuc te: " + elapsed + "ms", elapsed < 1000);
        assertTrue("Phai co it nhat 1000 Customer", repo.count() >= 1000);
    }

    @Test
    public void testAdminLoadPerformance() {
        long start = System.currentTimeMillis();
        AdminRepository repo = new AdminRepository("data/admins.csv");
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("[BENCHMARK] AdminRepo: " + repo.count() + " records in " + elapsed + " ms");
        assertTrue("Doc Admin phai duoi 1000ms", elapsed < 1000);
    }

    @Test
    public void testSellerLoadPerformance() {
        long start = System.currentTimeMillis();
        SellerRepository repo = new SellerRepository("data/sellers.csv");
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("[BENCHMARK] SellerRepo: " + repo.count() + " records in " + elapsed + " ms");
        assertTrue("Doc Seller phai duoi 1000ms", elapsed < 1000);
    }

    // ========================================================================
    // TEST CRUD - CUSTOMER
    // ========================================================================

    @Test
    public void testGetById() {
        Customer c = customerRepo.getById("C00001");
        assertNotNull("C00001 phai ton tai", c);
        assertEquals("Truong Gia Huy", c.getName());
        assertEquals("truonggiahuy@gmail.com", c.getEmail());
    }

    @Test
    public void testGetByIdNotFound() {
        Customer c = customerRepo.getById("INVALID_ID");
        assertNull("ID khong ton tai phai tra ve null", c);
    }

    @Test
    public void testAuthenticate() {
        Customer login = customerRepo.authenticate("truonggiahuy@gmail.com", "pass123");
        assertNotNull("Dang nhap phai thanh cong", login);
        assertEquals("C00001", login.getId());
    }

    @Test
    public void testAuthenticateWrongPassword() {
        Customer login = customerRepo.authenticate("truonggiahuy@gmail.com", "wrongpass");
        assertNull("Sai mat khau phai tra ve null", login);
    }

    @Test
    public void testAddAndDelete() {
        int beforeCount = customerRepo.count();

        Customer newCust = new Customer("C99999", "Test User JUnit", "junit@test.com", "pass",
                AccountStatus.APPROVED, CustTier.BRONZE, "");
        customerRepo.add(newCust);
        assertEquals("Them 1 record => count tang 1", beforeCount + 1, customerRepo.count());

        Customer fetched = customerRepo.getById("C99999");
        assertNotNull("Phai tim thay Customer vua them", fetched);
        assertEquals("Test User JUnit", fetched.getName());

        boolean deleted = customerRepo.delete("C99999");
        assertTrue("Xoa phai thanh cong", deleted);
        assertEquals("Xoa xong count phai ve nhu ban dau", beforeCount, customerRepo.count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicateId() {
        Customer dup = new Customer("C00001", "Dup", "dup@test.com", "pass",
                AccountStatus.APPROVED, CustTier.BRONZE, "");
        customerRepo.add(dup);
    }

    @Test
    public void testFindByTier() {
        List<Customer> goldCustomers = customerRepo.findByTier(CustTier.GOLD);
        assertNotNull(goldCustomers);
        assertTrue("Phai co it nhat 1 Customer GOLD (C00001)", goldCustomers.size() >= 1);
    }

    @Test
    public void testFindByBanned() {
        List<Customer> bannedList = customerRepo.findBy(c -> c.getStatus() == AccountStatus.BANNED);
        assertNotNull(bannedList);
        System.out.println("[INFO] Tim thay " + bannedList.size() + " Customer BANNED");
    }
}

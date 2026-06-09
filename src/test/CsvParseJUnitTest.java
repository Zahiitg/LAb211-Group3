package test;

import static org.junit.Assert.*;
import org.junit.Test;

import model.Customer;
import model.Product;
import model.Seller;
import model.enums.AccountStatus;
import model.enums.CustTier;

/**
 * JUnit Test cho viec Serialize/Deserialize CSV cua cac Entity.
 * Dam bao du lieu ghi xuong CSV va doc lai phai khop 100% (ke ca truong hop co dau phay).
 */
public class CsvParseJUnitTest {

    // ========================================================================
    // TEST CUSTOMER CSV
    // ========================================================================

    @Test
    public void testCustomerCsvRoundTrip() {
        Customer c = new Customer("C01", "Nguyen Van A", "a@gmail.com", "pass123", AccountStatus.APPROVED, CustTier.GOLD);
        String csv = c.toCsvLine();

        Customer parsed = new Customer();
        parsed.fromCsvLine(csv);

        assertEquals("C01", parsed.getId());
        assertEquals("Nguyen Van A", parsed.getName());
        assertEquals("a@gmail.com", parsed.getEmail());
        assertEquals("pass123", parsed.getPassword());
        assertEquals(AccountStatus.APPROVED, parsed.getStatus());
        assertEquals(CustTier.GOLD, parsed.getTier());
    }

    @Test
    public void testCustomerCsvWithCommaInName() {
        Customer c = new Customer("C02", "Nguyen Van A, VIP", "b@gmail.com", "pass456", AccountStatus.APPROVED, CustTier.SILVER);
        String csv = c.toCsvLine();

        Customer parsed = new Customer();
        parsed.fromCsvLine(csv);

        assertEquals("Nguyen Van A, VIP", parsed.getName());
        assertEquals("b@gmail.com", parsed.getEmail());
        assertEquals(CustTier.SILVER, parsed.getTier());
    }

    @Test
    public void testCustomerBannedStatus() {
        Customer c = new Customer("C03", "Test", "t@gmail.com", "pass", AccountStatus.BANNED, CustTier.BRONZE);
        String csv = c.toCsvLine();

        Customer parsed = new Customer();
        parsed.fromCsvLine(csv);

        assertEquals(AccountStatus.BANNED, parsed.getStatus());
        assertEquals(CustTier.BRONZE, parsed.getTier());
    }

    // ========================================================================
    // TEST SELLER CSV
    // ========================================================================

    @Test
    public void testSellerCsvRoundTrip() {
        Seller s = new Seller("S01", "Tran Thi B", "b@gmail.com", "pass456", AccountStatus.APPROVED, "Shop B");
        String csv = s.toCsvLine();

        Seller parsed = new Seller();
        parsed.fromCsvLine(csv);

        assertEquals("S01", parsed.getId());
        assertEquals("Tran Thi B", parsed.getName());
        assertEquals("Shop B", parsed.getStoreName());
    }

    @Test
    public void testSellerCsvWithCommaInStoreName() {
        Seller s = new Seller("S02", "Le Van C", "c@gmail.com", "pass789", AccountStatus.APPROVED, "Shop B, Dep, Gia Re");
        String csv = s.toCsvLine();

        Seller parsed = new Seller();
        parsed.fromCsvLine(csv);

        assertEquals("Shop B, Dep, Gia Re", parsed.getStoreName());
        assertEquals("Le Van C", parsed.getName());
    }

    // ========================================================================
    // TEST PRODUCT CSV
    // ========================================================================

    @Test
    public void testProductCsvRoundTrip() {
        Product p = new Product("P01", "S01", "iPhone 15", "Electronics", 1500.5, 10);
        String csv = p.toCsvLine();

        Product parsed = new Product();
        parsed.fromCsvLine(csv);

        assertEquals("P01", parsed.getId());
        assertEquals("S01", parsed.getSellerId());
        assertEquals("iPhone 15", parsed.getName());
        assertEquals("Electronics", parsed.getCategory());
        assertEquals(1500.5, parsed.getPrice(), 0.001);
        assertEquals(10, parsed.getStock());
    }

    @Test
    public void testProductCsvWithCommaInNameAndCategory() {
        Product p = new Product("P02", "S01", "Dien thoai iPhone 15, Mau do, 256GB", "Electronics, Phones", 25000000.0, 5);
        String csv = p.toCsvLine();

        Product parsed = new Product();
        parsed.fromCsvLine(csv);

        assertEquals("Dien thoai iPhone 15, Mau do, 256GB", parsed.getName());
        assertEquals("Electronics, Phones", parsed.getCategory());
        assertEquals(25000000.0, parsed.getPrice(), 0.001);
    }
}

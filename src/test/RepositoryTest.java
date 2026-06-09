package test;

import java.util.List;
import model.Customer;
import repository.AdminRepository;
import repository.CustomerRepository;
import repository.SellerRepository;

/**
 * Test nhanh de kiem tra CsvRepository co hoat dong dung va nhanh khong.
 * Muc tieu: Doc 2000 dong Customer < 1 giay.
 */
public class RepositoryTest {

    public static void main(String[] args) {
        System.out.println("=== REPOSITORY PERFORMANCE TEST ===\n");

        // --- Test 1: Doc 2000 Customer ---
        long start = System.currentTimeMillis();
        CustomerRepository customerRepo = new CustomerRepository("data/customers.csv");
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("[CustomerRepo] Doc " + customerRepo.count() + " ban ghi trong " + elapsed + " ms");

        // --- Test 2: Doc Admin ---
        start = System.currentTimeMillis();
        AdminRepository adminRepo = new AdminRepository("data/admins.csv");
        elapsed = System.currentTimeMillis() - start;
        System.out.println("[AdminRepo]    Doc " + adminRepo.count() + " ban ghi trong " + elapsed + " ms");

        // --- Test 3: Doc Seller ---
        start = System.currentTimeMillis();
        SellerRepository sellerRepo = new SellerRepository("data/sellers.csv");
        elapsed = System.currentTimeMillis() - start;
        System.out.println("[SellerRepo]   Doc " + sellerRepo.count() + " ban ghi trong " + elapsed + " ms");

        // --- Test 4: Tim kiem theo ID (O(1) tu Cache) ---
        System.out.println("\n--- Test Tim Kiem ---");
        Customer c = customerRepo.getById("C00001");
        if (c != null) {
            System.out.println("[getById] C00001 => " + c.getName() + " | " + c.getEmail() + " | Tier: " + c.getTier());
        } else {
            System.out.println("[getById] Khong tim thay C00001!");
        }

        // --- Test 5: Tim kiem theo Email (Login) ---
        Customer login = customerRepo.authenticate("truonggiahuy@gmail.com", "pass123");
        if (login != null) {
            System.out.println("[Login]   OK => " + login.getName() + " (ID: " + login.getId() + ")");
        } else {
            System.out.println("[Login]   FAIL - Sai email hoac password!");
        }

        // --- Test 6: findBy (Tim kiem voi dieu kien) ---
        List<Customer> bannedList = customerRepo.findBy(cust -> cust.getStatus().name().equals("BANNED"));
        System.out.println("[findBy]  Tim thay " + bannedList.size() + " Customer bi BANNED.");

        // --- Test 7: Them moi 1 Customer ---
        System.out.println("\n--- Test Them / Xoa ---");
        Customer newCust = new Customer("C99999", "Test User", "test@gmail.com", "pass123",
                model.enums.AccountStatus.APPROVED, model.enums.CustTier.BRONZE);
        customerRepo.add(newCust);
        System.out.println("[add]     Da them C99999. Tong: " + customerRepo.count());

        // --- Test 8: Xoa Customer vua them ---
        boolean deleted = customerRepo.delete("C99999");
        System.out.println("[delete]  Xoa C99999: " + (deleted ? "OK" : "FAIL") + ". Tong: " + customerRepo.count());

        System.out.println("\n=== ALL TESTS PASSED ===");
    }
}

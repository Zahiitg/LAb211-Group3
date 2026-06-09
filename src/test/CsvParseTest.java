package test;

import model.Customer;
import model.Product;
import model.Seller;
import model.enums.AccountStatus;
import model.enums.CustTier;

public class CsvParseTest {
    public static void main(String[] args) {
        System.out.println("--- Bắt đầu test CSV Parse ---");
        testCustomerCsv();
        testSellerCsv();
        testProductCsv();
        System.out.println("--- Kết thúc test CSV Parse ---");
    }

    private static void testCustomerCsv() {
        Customer c = new Customer("C01", "Nguyễn Văn A, VIP", "a@gmail.com", "pass123", AccountStatus.APPROVED, CustTier.GOLD);
        String csvLine = c.toCsvLine();
        System.out.println("Customer CSV: " + csvLine);

        Customer parsed = new Customer();
        parsed.fromCsvLine(csvLine);

        boolean isPassed = parsed.getId().equals("C01") &&
                           parsed.getName().equals("Nguyễn Văn A, VIP") &&
                           parsed.getEmail().equals("a@gmail.com") &&
                           parsed.getPassword().equals("pass123") &&
                           parsed.getStatus() == AccountStatus.APPROVED &&
                           parsed.getTier() == CustTier.GOLD;

        System.out.println("Customer Parse Test: " + (isPassed ? "PASS" : "FAIL"));
    }

    private static void testSellerCsv() {
        Seller s = new Seller("S01", "Trần Thị B", "b@gmail.com", "pass456", AccountStatus.APPROVED, "Shop B, Đẹp, Giá Rẻ");
        String csvLine = s.toCsvLine();
        System.out.println("Seller CSV: " + csvLine);

        Seller parsed = new Seller();
        parsed.fromCsvLine(csvLine);

        boolean isPassed = parsed.getId().equals("S01") &&
                           parsed.getName().equals("Trần Thị B") &&
                           parsed.getEmail().equals("b@gmail.com") &&
                           parsed.getPassword().equals("pass456") &&
                           parsed.getStatus() == AccountStatus.APPROVED &&
                           parsed.getStoreName().equals("Shop B, Đẹp, Giá Rẻ");

        System.out.println("Seller Parse Test: " + (isPassed ? "PASS" : "FAIL"));
    }

    private static void testProductCsv() {
        Product p = new Product("P01", "S01", "Điện thoại iPhone 15, Màu đỏ, 256GB", "Electronics, Phones", 1500.5, 10);
        String csvLine = p.toCsvLine();
        System.out.println("Product CSV: " + csvLine);

        Product parsed = new Product();
        parsed.fromCsvLine(csvLine);

        boolean isPassed = parsed.getId().equals("P01") &&
                           parsed.getSellerId().equals("S01") &&
                           parsed.getName().equals("Điện thoại iPhone 15, Màu đỏ, 256GB") &&
                           parsed.getCategory().equals("Electronics, Phones") &&
                           parsed.getPrice() == 1500.5 &&
                           parsed.getStock() == 10;

        System.out.println("Product Parse Test: " + (isPassed ? "PASS" : "FAIL"));
    }
}

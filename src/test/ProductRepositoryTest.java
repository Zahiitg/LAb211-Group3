package test;

import model.Product;
import repository.ProductRepository;
import java.io.File;
import java.util.List;

public class ProductRepositoryTest {
    public static void main(String[] args) {
        String testFilePath = "test_products.csv";
        System.out.println("--- Bắt đầu test ProductRepository CRUD ---");

        // 1. Khởi tạo repository
        ProductRepository repo = new ProductRepository(testFilePath);

        // 2. Add
        Product p1 = new Product("P01", "S01", "Ban phim co, Blue Switch", "Phu kien", 50.0, 100);
        Product p2 = new Product("P02", "S01", "Chuot khong day", "Phu kien", 20.0, 50);
        repo.add(p1);
        repo.add(p2);
        System.out.println("Đã thêm 2 sản phẩm. Count = " + repo.count());

        // 3. Get và Verify Data từ file
        ProductRepository repoReloaded = new ProductRepository(testFilePath);
        System.out.println("Load lại từ file. Count = " + repoReloaded.count());
        Product p1Reloaded = repoReloaded.getById("P01");
        boolean isAddPassed = p1Reloaded != null && p1Reloaded.getName().equals("Ban phim co, Blue Switch");
        System.out.println("Add & Read Test: " + (isAddPassed ? "PASS" : "FAIL"));

        // 4. Update
        p1Reloaded.setPrice(45.0);
        repoReloaded.update(p1Reloaded);
        ProductRepository repoUpdated = new ProductRepository(testFilePath);
        boolean isUpdatePassed = repoUpdated.getById("P01").getPrice() == 45.0;
        System.out.println("Update Test: " + (isUpdatePassed ? "PASS" : "FAIL"));

        // 5. Custom Search Methods
        List<Product> searchByName = repoUpdated.findByName("ban phim");
        boolean isSearchNamePassed = searchByName.size() == 1 && searchByName.get(0).getId().equals("P01");
        System.out.println("FindByName Test: " + (isSearchNamePassed ? "PASS" : "FAIL"));

        List<Product> searchBySeller = repoUpdated.findBySellerId("S01");
        boolean isSearchSellerPassed = searchBySeller.size() == 2;
        System.out.println("FindBySellerId Test: " + (isSearchSellerPassed ? "PASS" : "FAIL"));

        // 6. Delete
        repoUpdated.delete("P02");
        ProductRepository repoDeleted = new ProductRepository(testFilePath);
        boolean isDeletePassed = repoDeleted.count() == 1 && repoDeleted.getById("P02") == null;
        System.out.println("Delete Test: " + (isDeletePassed ? "PASS" : "FAIL"));

        // Cleanup
        new File(testFilePath).delete();
        System.out.println("--- Kết thúc test ProductRepository CRUD ---");
    }
}

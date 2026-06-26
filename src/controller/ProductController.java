package controller;

import model.Product;
import repository.ProductRepository;
import java.util.List;

/**
 * Controller xử lý nghiệp vụ liên quan đến Sản phẩm thường.
 */
public class ProductController extends BaseController {

    private final ProductRepository productRepo;

    public ProductController() {
        this.productRepo = AuthenticationState.getInstance().getProductRepo();
    }

    public ProductController(String filePath) {
        this.productRepo = new ProductRepository(filePath);
    }

    public ControllerResult searchProducts(String name) {
        if (name == null || name.trim().isEmpty()) {
            return error("Từ khóa tìm kiếm không được để trống!");
        }
        List<Product> list = productRepo.findByName(name.trim());
        return success("Tìm thấy " + list.size() + " sản phẩm.", list);
    }

    public ControllerResult getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return error("Danh mục không được để trống!");
        }
        List<Product> list = productRepo.findByCategory(category.trim());
        return success("Tìm thấy " + list.size() + " sản phẩm trong danh mục " + category, list);
    }

    public ControllerResult getAllCategories() {
        List<String> list = productRepo.getAllCategories();
        return success("Tìm thấy " + list.size() + " danh mục.", list);
    }

    public ControllerResult getProductDetails(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return error("Mã sản phẩm không được để trống!");
        }
        Product p = productRepo.getById(productId.trim());
        if (p == null) {
            return error("Không tìm thấy sản phẩm với mã: " + productId);
        }
        return success("Lấy chi tiết sản phẩm thành công.", p);
    }

    public ProductRepository getProductRepo() {
        return productRepo;
    }
}

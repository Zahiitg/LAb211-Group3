package repository;

import model.Product;
import java.util.List;

/**
 * Repository for managing Product data from products.csv.
 */
public class ProductRepository extends CsvRepository<Product> {

    public ProductRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected Product createEntity() {
        return new Product();
    }

    @Override
    protected String getHeader() {
        return "id,sellerId,name,category,price,stock";
    }

    /**
     * Find products by name (contains, case-insensitive).
     * @param name Name or partial name to search for
     * @return List of matching products
     */
    public List<Product> findByName(String name) {
        String searchStr = name.toLowerCase();
        return findBy(p -> p.getName() != null && p.getName().toLowerCase().contains(searchStr));
    }

    /**
     * Find products by seller ID.
     * @param sellerId Seller ID to filter by
     * @return List of matching products
     */
    public List<Product> findBySellerId(String sellerId) {
        return findBy(p -> p.getSellerId() != null && p.getSellerId().equals(sellerId));
    }
}

package repository;

import java.util.List;
import model.Product;

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
     * Find products by category.
     * @param category Category to filter by
     * @return List of matching products
     */
    public List<Product> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) return java.util.Collections.emptyList();
        return findBy(p -> category.equalsIgnoreCase(p.getCategory()));
    }

    /**
     * Get all unique categories from products.
     * @return List of unique categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new java.util.ArrayList<>();
        for (Product p : getAll()) {
            if (p.getCategory() != null && !p.getCategory().trim().isEmpty()) {
                String cat = p.getCategory().trim();
                boolean exists = false;
                for (String c : categories) {
                    if (c.equalsIgnoreCase(cat)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    categories.add(cat);
                }
            }
        }
        return categories;
    }

    /**
     * Find products by seller ID.
     * @param sellerId Seller ID to filter by
     * @return List of matching products
     */
    public List<Product> findBySellerId(String sellerId) {
        return findBy(p -> p.getSellerId() != null && p.getSellerId().equals(sellerId));
    }

    protected boolean deductStock(String productId, int buyQuantity) {
        Product p = getById(productId);
        if (p == null) return false;

        if (p.getStock() >= buyQuantity) {
            p.setStock(p.getStock() - buyQuantity);
            update(p);
            return true;
        }
        return false;
    }

    public boolean sellWithNoLock(String productId, int quantity) {
        return deductStock(productId, quantity);
    }

    public synchronized boolean sellWithSynchronized(String productId, int quantity) {
        return deductStock(productId, quantity);
    }

    private final Object fileLockMonitor = new Object();

    public boolean sellWithFileLock(String productId, int quantity) {
        synchronized (fileLockMonitor) {
            java.io.File lockFile = new java.io.File(getFilePath() + ".lock");
            java.io.RandomAccessFile raf = null;
            java.nio.channels.FileChannel channel = null;
            java.nio.channels.FileLock lock = null;

            try {
                lockFile.createNewFile();
                raf = new java.io.RandomAccessFile(lockFile, "rw");
                channel = raf.getChannel();
                lock = channel.lock();

                boolean result = deductStock(productId, quantity);
                return result;
            } catch (java.io.IOException e) {
                System.err.println("[FileLock] Loi khi khoa file san pham: " + e.getMessage());
                return false;
            } finally {
                try {
                    if (lock != null) lock.release();
                } catch (java.io.IOException ignored) {}
                try {
                    if (channel != null) channel.close();
                } catch (java.io.IOException ignored) {}
                try {
                    if (raf != null) raf.close();
                } catch (java.io.IOException ignored) {}
            }
        }
    }

    public boolean sellWithOptimisticLock(String productId, int quantity) {
        for (int attempt = 0; attempt < 5; attempt++) {
            Product p = getById(productId);
            if (p == null) return false;

            int oldStock = p.getStock();
            if (oldStock < quantity) {
                return false;
            }

            if (p.getStock() != oldStock) {
                try {
                    Thread.sleep(10 + (long)(Math.random() * 20));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                continue;
            }

            p.setStock(oldStock - quantity);
            update(p);
            return true;
        }
        return false;
    }
}

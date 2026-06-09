package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.Product;
import repository.ProductRepository;

import java.io.File;
import java.util.List;

/**
 * JUnit Test cho ProductRepository: CRUD + Custom Search.
 * Su dung file tam de khong anh huong du lieu that.
 */
public class ProductRepositoryJUnitTest {

    private static final String TEST_FILE = "test_products_junit.csv";
    private ProductRepository repo;

    @Before
    public void setUp() {
        new File(TEST_FILE).delete();
        repo = new ProductRepository(TEST_FILE);
    }

    @After
    public void tearDown() {
        new File(TEST_FILE).delete();
    }

    @Test
    public void testAddAndGetById() {
        Product p = new Product("P01", "S01", "Ban phim co", "Phu kien", 50000.0, 100);
        repo.add(p);

        assertEquals(1, repo.count());

        Product fetched = repo.getById("P01");
        assertNotNull(fetched);
        assertEquals("Ban phim co", fetched.getName());
        assertEquals("S01", fetched.getSellerId());
        assertEquals(50000.0, fetched.getPrice(), 0.001);
        assertEquals(100, fetched.getStock());
    }

    @Test
    public void testPersistenceAfterReload() {
        Product p1 = new Product("P01", "S01", "Ban phim co, Blue Switch", "Phu kien", 50000.0, 100);
        Product p2 = new Product("P02", "S01", "Chuot khong day", "Phu kien", 20000.0, 50);
        repo.add(p1);
        repo.add(p2);

        ProductRepository reloaded = new ProductRepository(TEST_FILE);
        assertEquals("Du lieu phai duoc luu xuong file va doc lai dung", 2, reloaded.count());

        Product p1r = reloaded.getById("P01");
        assertNotNull(p1r);
        assertEquals("Ban phim co, Blue Switch", p1r.getName());
    }

    @Test
    public void testUpdate() {
        Product p = new Product("P01", "S01", "iPhone 15", "Electronics", 25000000.0, 10);
        repo.add(p);

        p.setPrice(23000000.0);
        repo.update(p);

        ProductRepository reloaded = new ProductRepository(TEST_FILE);
        assertEquals(23000000.0, reloaded.getById("P01").getPrice(), 0.001);
    }

    @Test
    public void testDelete() {
        repo.add(new Product("P01", "S01", "A", "Cat", 100, 10));
        repo.add(new Product("P02", "S01", "B", "Cat", 200, 20));
        assertEquals(2, repo.count());

        boolean deleted = repo.delete("P01");
        assertTrue(deleted);
        assertEquals(1, repo.count());
        assertNull(repo.getById("P01"));
        assertNotNull(repo.getById("P02"));
    }

    @Test
    public void testFindByName() {
        repo.add(new Product("P01", "S01", "Ban phim co", "Phu kien", 50000, 10));
        repo.add(new Product("P02", "S01", "Chuot khong day", "Phu kien", 20000, 5));
        repo.add(new Product("P03", "S02", "Ban phim membrane", "Phu kien", 30000, 15));

        List<Product> results = repo.findByName("ban phim");
        assertEquals("Tim kiem 'ban phim' phai tra ve 2 san pham", 2, results.size());
    }

    @Test
    public void testFindBySellerId() {
        repo.add(new Product("P01", "S01", "A", "Cat", 100, 10));
        repo.add(new Product("P02", "S01", "B", "Cat", 200, 20));
        repo.add(new Product("P03", "S02", "C", "Cat", 300, 30));

        List<Product> results = repo.findBySellerId("S01");
        assertEquals("Seller S01 phai co 2 san pham", 2, results.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicateId() {
        repo.add(new Product("P01", "S01", "A", "Cat", 100, 10));
        repo.add(new Product("P01", "S01", "B", "Cat", 200, 20));
    }
}

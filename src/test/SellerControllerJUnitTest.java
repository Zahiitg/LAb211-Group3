package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import controller.AuthenticationState;
import controller.ControllerResult;
import controller.SellerController;
import model.Product;
import model.Seller;
import repository.AdminRepository;
import repository.CustomerRepository;
import repository.SellerRepository;

/**
 * Unit Test cho SellerController.
 * Kiem tra tinh nang Quan ly san pham va ban hang cua Seller.
 */
public class SellerControllerJUnitTest {

    private static final String TEST_CUSTOMERS_FILE = "test_seller_customers.csv";
    private static final String TEST_SELLERS_FILE   = "test_seller_sellers.csv";
    private static final String TEST_ADMINS_FILE    = "test_seller_admins.csv";
    private static final String TEST_PRODUCTS_FILE  = "test_seller_products.csv";

    private AuthenticationState authState;
    private SellerController controller;

    @Before
    public void setUp() throws IOException {
        cleanUpFiles();

        try (FileWriter fw = new FileWriter(TEST_SELLERS_FILE)) {
            fw.write("id,name,email,password,status,storeName\n");
            fw.write("S001,Test Seller 1,s1@test.com,pass1,APPROVED,Store 1\n");
            fw.write("S002,Test Seller 2,s2@test.com,pass2,APPROVED,Store 2\n");
            fw.write("S003,Test Seller 3,s3@test.com,pass3,BANNED,Store 3\n");
        }

        try (FileWriter fw = new FileWriter(TEST_PRODUCTS_FILE)) {
            fw.write("id,sellerId,name,category,price,stock\n");
            fw.write("P001,S001,Macbook,CAT1,30000000.0,10\n");
        }

        try (FileWriter fw = new FileWriter(TEST_ADMINS_FILE)) { fw.write("id,name,email,password,status,roleLevel\n"); }
        try (FileWriter fw = new FileWriter(TEST_CUSTOMERS_FILE)) { fw.write("id,name,email,password,status,tier,address\n"); }

        CustomerRepository customerRepo = new CustomerRepository(TEST_CUSTOMERS_FILE);
        SellerRepository sellerRepo     = new SellerRepository(TEST_SELLERS_FILE);
        AdminRepository adminRepo       = new AdminRepository(TEST_ADMINS_FILE);

        AuthenticationState.resetForTesting(customerRepo, sellerRepo, adminRepo);
        authState = AuthenticationState.getInstance();
        
        try {
            java.lang.reflect.Field productRepoField = AuthenticationState.class.getDeclaredField("productRepo");
            productRepoField.setAccessible(true);
            productRepoField.set(authState, new repository.ProductRepository(TEST_PRODUCTS_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        }

        controller = new SellerController();
    }

    @After
    public void tearDown() {
        if (authState != null && authState.isLoggedIn()) {
            authState.logout();
        }
        cleanUpFiles();
    }

    private void cleanUpFiles() {
        new File(TEST_CUSTOMERS_FILE).delete();
        new File(TEST_SELLERS_FILE).delete();
        new File(TEST_ADMINS_FILE).delete();
        new File(TEST_PRODUCTS_FILE).delete();
    }

    @Test
    public void testLoginBannedSeller() {
        ControllerResult loginRes = authState.login("s3@test.com", "pass3");
        assertFalse("Tai khoan BANNED khong duoc dang nhap", loginRes.isSuccess());
    }

    @Test
    public void testAddProductSuccess() {
        authState.login("s1@test.com", "pass1");
        
        Product p = new Product();
        p.setName("Laptop Dell");
        p.setPrice(15000000);
        p.setStock(10);
        p.setCategory("CAT01");
        
        ControllerResult addRes = controller.addProduct(p);
        assertTrue("Them san pham phai thanh cong", addRes.isSuccess());
        
        ControllerResult listRes = controller.getMyProducts();
        assertTrue(listRes.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) listRes.getData();
        assertEquals("Phai co 2 san pham trong danh sach (1 cu, 1 moi them)", 2, products.size());
    }

    @Test
    public void testUpdateProductPermission() {
        // S2 login and tries to update S1's product (P001)
        authState.login("s2@test.com", "pass2");
        
        Product p = new Product();
        p.setId("P001");
        p.setName("Hacked Macbook");
        p.setPrice(1000);
        p.setStock(100);
        
        ControllerResult updateRes = controller.updateProduct(p);
        
        // Phai that bai vi khong cung Seller ID
        assertFalse("Khong the sua san pham cua nguoi khac", updateRes.isSuccess());
    }
}

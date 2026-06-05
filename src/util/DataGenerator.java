package util;
import model.*;
import model.enums.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DataGenerator {
    private static final String DATA_DIR = "data/";
    private static final Random RANDOM = new Random();

    private static final String[] LAST_NAMES = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Vo", "Dang", "Bui", "Do", "Ho", "Ngo", "Duong", "Ly", "Truong"};
    private static final String[] MIDDLE_NAMES = {"Van", "Thi", "Ngoc", "Gia", "Thanh", "Minh", "Hai", "Tuan", "Duc", "Hoang", "Xuan", "Thu", "Hong", "Mai", "Quynh"};
    private static final String[] FIRST_NAMES = {"Huy", "An", "Anh", "Binh", "Cuong", "Dung", "Duong", "Dat", "Ha", "Hung", "Khanh", "Khoa", "Kien", "Lam", "Linh", "Long", "Nam", "Nghia", "Nhi", "Nhung", "Phuc", "Phuong", "Quan", "Quang", "Quoc", "Tam", "Thao", "Thang", "Thanh", "Thuy", "Trang", "Trung", "Tu", "Tuan", "Uyen", "Van", "Viet", "Vy", "Yen"};
    private static final String[] PROD_PREFIXES = {"Garmin Forerunner", "Lenovo ThinkPad X1", "Razer Blade 16", "Adidas Ultraboost", "Samsung Galaxy Tab S9", "Sony A7 IV", "Samsung Galaxy S25", "Apple iPhone 16", "MacBook Pro M3", "Dell XPS 15", "Asus ROG Zephyrus", "Nike Air Max", "Sony WH-1000XM6"};
    private static final String[] PROD_SUFFIXES = {"2023 Edition", "2024 Edition", "2025 Edition", "2026 Edition", "Pro", "Ultra", "Max", "Plus", "Standard"};

    private static String generateName() {
        return LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)] + " " +
               MIDDLE_NAMES[RANDOM.nextInt(MIDDLE_NAMES.length)] + " " +
               FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
    }

    private static String generateEmail(String name) {
        String noAccent = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().replaceAll(" ", "");
        return noAccent + RANDOM.nextInt(100) + "@gmail.com";
    }

    public static void main(String[] args) {
        System.out.println("🚀 Started generating data... Please wait.");
        try {
            List<Admin> admins = generateAdmins(10);
            List<Seller> sellers = generateSellers(50);
            List<Customer> customers = generateCustomers(2000);
            List<Product> products = generateProducts(5000, sellers);
            List<FlashSaleEvent> events = generateEvents(50);
            List<FlashSaleItem> flashItems = generateFlashItems(1000, products, events);
            List<Order> orders = generateOrders(2500, customers);
            List<OrderDetail> details = generateOrderDetails(4000, orders, flashItems);
            List<OrderTransaction> transactions = generateTransactions(3000, orders);

            saveCsv("admins.csv", "id,name,email,password,status,roleLevel", admins);
            saveCsv("sellers.csv", "id,name,email,password,status,storeName", sellers);
            saveCsv("customers.csv", "id,name,email,password,status,tier", customers);
            saveCsv("products.csv", "id,sellerId,name,category,price,stock", products);
            saveCsv("flash_events.csv", "id,name,startTime,endTime,status", events);
            saveCsv("flash_items.csv", "id,productId,eventId,limitedQty,soldQty,version", flashItems);
            saveCsv("orders.csv", "id,customerId,orderTime,status", orders);
            saveCsv("order_details.csv", "id,orderId,flashSaleItemId,quantity,priceAtPurchase", details);
            saveCsv("transactions.csv", "id,orderId,lockMechanism,retryCount,processingTimeMs,success", transactions);

            System.out.println("✅ Data generation completed!");
            System.out.println("   Admins:       " + admins.size());
            System.out.println("   Sellers:      " + sellers.size());
            System.out.println("   Customers:    " + customers.size());
            System.out.println("   Products:     " + products.size());
            System.out.println("   Events:       " + events.size());
            System.out.println("   FlashItems:   " + flashItems.size());
            System.out.println("   Orders:       " + orders.size());
            System.out.println("   OrderDetails: " + details.size());
            System.out.println("   Transactions: " + transactions.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Admin> generateAdmins(int count) {
        List<Admin> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = generateName();
            list.add(new Admin(String.format("A%05d", i), name, generateEmail(name), "pass123", AccountStatus.APPROVED, 1));
        }
        return list;
    }

    private static List<Seller> generateSellers(int count) {
        List<Seller> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = generateName();
            list.add(new Seller(String.format("S%05d", i), name, generateEmail(name), "pass123", AccountStatus.APPROVED, name + " Store"));
        }
        return list;
    }

    private static List<Customer> generateCustomers(int count) {
        List<Customer> list = new ArrayList<>();
        CustTier[] tiers = CustTier.values();
        list.add(new Customer("C00001", "Truong Gia Huy", "truonggiahuy@gmail.com", "pass123", AccountStatus.APPROVED, CustTier.GOLD));
        for (int i = 2; i <= count; i++) {
            String name = generateName();
            list.add(new Customer(String.format("C%05d", i), name, generateEmail(name), "pass123", RANDOM.nextDouble() > 0.1 ? AccountStatus.APPROVED : AccountStatus.BANNED, tiers[RANDOM.nextInt(tiers.length)]));
        }
        return list;
    }

    private static List<Product> generateProducts(int count, List<Seller> sellers) {
        List<Product> list = new ArrayList<>();
        String[] categories = {"Electronics", "Fashion", "Home", "Beauty", "Sports"};
        for (int i = 1; i <= count; i++) {
            String sellerId = sellers.get(RANDOM.nextInt(sellers.size())).getId();
            double generatedPrice = (RANDOM.nextInt(10000) + 50) * 1000.0; // Từ 50,000 đến 10,049,000 VND
            String prodName = PROD_PREFIXES[RANDOM.nextInt(PROD_PREFIXES.length)] + " " + PROD_SUFFIXES[RANDOM.nextInt(PROD_SUFFIXES.length)];
            list.add(new Product(String.format("P%05d", i), sellerId, prodName, categories[RANDOM.nextInt(categories.length)], generatedPrice, RANDOM.nextInt(500) + 10));
        }
        return list;
    }

    private static List<FlashSaleEvent> generateEvents(int count) {
        List<FlashSaleEvent> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (int i = 1; i <= count; i++) {
            LocalDateTime start = now.minusDays(RANDOM.nextInt(30));
            LocalDateTime end = start.plusHours(RANDOM.nextInt(48) + 1);
            SaleStatus status = end.isBefore(now) ? SaleStatus.ENDED : SaleStatus.ONGOING;
            list.add(new FlashSaleEvent(String.format("E%05d", i), "Event " + i, start, end, status));
        }
        return list;
    }

    private static List<FlashSaleItem> generateFlashItems(int count, List<Product> products, List<FlashSaleEvent> events) {
        List<FlashSaleItem> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String prodId = products.get(RANDOM.nextInt(products.size())).getId();
            String eventId = events.get(RANDOM.nextInt(events.size())).getId();
            int limit = RANDOM.nextInt(100) + 10;
            list.add(new FlashSaleItem(String.format("FI%05d", i), prodId, eventId, limit, RANDOM.nextInt(limit), 1));
        }
        return list;
    }

    private static List<Order> generateOrders(int count, List<Customer> customers) {
        List<Order> list = new ArrayList<>();
        OrderStatus[] statuses = OrderStatus.values();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        for (int i = 1; i <= count; i++) {
            String custId = customers.get(RANDOM.nextInt(customers.size())).getId();
            list.add(new Order(String.format("O%05d", i), custId, now.minusDays(RANDOM.nextInt(30)), statuses[RANDOM.nextInt(statuses.length)]));
        }
        return list;
    }

    private static List<OrderDetail> generateOrderDetails(int count, List<Order> orders, List<FlashSaleItem> items) {
        List<OrderDetail> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String orderId = orders.get(RANDOM.nextInt(orders.size())).getId();
            String itemId = items.get(RANDOM.nextInt(items.size())).getId();
            double purchasePrice = (RANDOM.nextInt(5000) + 50) * 1000.0; // Random giá mua VND
            list.add(new OrderDetail(String.format("OD%05d", i), orderId, itemId, RANDOM.nextInt(5) + 1, purchasePrice));
        }
        return list;
    }

    private static List<OrderTransaction> generateTransactions(int count, List<Order> orders) {
        List<OrderTransaction> list = new ArrayList<>();
        LockMechanism[] locks = LockMechanism.values();
        for (int i = 1; i <= count; i++) {
            String orderId = orders.get(RANDOM.nextInt(orders.size())).getId();
            list.add(new OrderTransaction(String.format("T%05d", i), orderId, locks[RANDOM.nextInt(locks.length)], RANDOM.nextInt(3), RANDOM.nextInt(500) + 10, RANDOM.nextBoolean()));
        }
        return list;
    }

    private static void saveCsv(String filename, String header, List<? extends BaseEntity> data) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + filename))) {
            bw.write(header);
            bw.newLine();
            for (BaseEntity entity : data) {
                bw.write(entity.toCsvLine());
                bw.newLine();
            }
        }
    }
}

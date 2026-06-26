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
    // === TEN SAN PHAM THEO TUNG DANH MUC ===
    private static final Map<String, String[]> CATEGORY_PREFIXES = new LinkedHashMap<String, String[]>() {{
        put("Electronics", new String[]{"Samsung Galaxy S25", "Apple iPhone 16", "MacBook Pro M3", "Dell XPS 15", "Asus ROG Zephyrus", "Lenovo ThinkPad X1", "Razer Blade 16", "Sony WH-1000XM6", "Samsung Galaxy Tab S9", "Sony A7 IV", "Garmin Forerunner", "JBL Charge 5", "Logitech MX Master", "iPad Air M2", "Google Pixel 9"});
        put("Fashion", new String[]{"Ao Thun Cotton", "Quan Jean Slim", "Ao Khoac Bomber", "Dam Maxi Hoa", "Ao So Mi Trang", "Quan Tay Classic", "Ao Hoodie Unisex", "Chan Vay Xep Ly", "Ao Polo Ralph", "Quan Short Kaki", "Ao Vest Nam", "Dam Lien Cong So", "Ao Len Cashmere", "Quan Jogger", "Ao Cardigan"});
        put("Home", new String[]{"Tu Lanh Inverter", "May Giat Cua Truoc", "Noi Chien Khong Dau", "May Hut Bui Robot", "Dieu Hoa 2 Chieu", "Lo Vi Song", "May Loc Nuoc RO", "Quat Tran Panasonic", "Ban La Hoi Nuoc", "May Xay Sinh To", "Noi Com Dien Tu", "Den LED Thong Minh", "May Rua Chen", "Binh Nuoc Nong", "May Say Toc"});
        put("Beauty", new String[]{"Son MAC Ruby Woo", "Kem Chong Nang Anessa", "Serum Vitamin C", "Nuoc Hoa Chanel", "Kem Duong Am", "Phan Phu Innisfree", "Tay Trang Bioderma", "Mat Na Collagen", "Sua Rua Mat CeraVe", "Dau Goi Kerastase", "Mascara Maybelline", "Kem Nen Estee Lauder", "Toner AHA BHA", "Kem Mat SK-II", "Tinh Chat Retinol"});
        put("Sports", new String[]{"Giay Nike Air Max", "Giay Adidas Ultraboost", "Vot Cau Long Yonex", "Bong Da Molten", "Ao Tap Gym", "Giay Chay Asics", "Gang Tay Boxing", "Day Nhay Speed Rope", "Xa Don Treo Tuong", "Tham Yoga TPE", "Kinh Boi Arena", "Mu Bao Hiem Poc", "Dong Ho Garmin", "Binh Nuoc GIGsport", "Ba Lo Leo Nui"});
    }};
    private static final Map<String, String[]> CATEGORY_SUFFIXES = new LinkedHashMap<String, String[]>() {{
        put("Electronics", new String[]{"Pro", "Ultra", "Max", "Plus", "Standard", "2024 Edition", "2025 Edition", "2026 Edition", "Lite", "SE"});
        put("Fashion", new String[]{"Size S", "Size M", "Size L", "Size XL", "Form Rong", "Form Slim", "Unisex", "Limited", "Premium", "Basic"});
        put("Home", new String[]{"9kg", "12kg", "1.5L", "2.0L", "Smart", "Mini", "Gia Dinh", "Cao Cap", "Tiet Kiem Dien", "Inverter"});
        put("Beauty", new String[]{"30ml", "50ml", "100ml", "200ml", "SPF50+", "Travel Size", "Full Size", "Limited Edition", "Set 3 Mon", "Gift Box"});
        put("Sports", new String[]{"Size 39", "Size 41", "Size 43", "Pro", "Elite", "V2", "V3", "Competition", "Training", "Outdoor"});
    }};

    // === TEN SU KIEN FLASH SALE ===
    private static final String[] EVENT_NAMES = {
        "Sieu Sale Ngay Doi", "Xa Kho Don Tet", "Flash Sale 12/12", "Black Friday",
        "Cyber Monday", "Sale Giua Thang", "Sale Luong Ve", "Ngay Hoi Mua Sam",
        "Tuan Le Vang", "Dai Tiec Sale", "Sale Dong Gia", "Clearance Sale",
        "Mid-Year Sale", "Sinh Nhat Tri An", "Deal Soc Nua Dem"
    };

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
        System.out.println("🚀 Started generating data using Streaming I/O... Please wait.");
        try {
            List<String> adminIds = generateAdmins(10);
            List<String> sellerIds = generateSellers(50);
            List<String> customerIds = generateCustomers(2000);
            List<String> productIds = generateProducts(5000, sellerIds);
            List<String> eventIds = generateEvents(50);
            List<String> flashItemIds = generateFlashItems(1000, productIds, eventIds);
            List<String> orderIds = generateOrders(2500, customerIds);
            List<String> detailIds = generateOrderDetails(4000, orderIds, flashItemIds);
            List<String> transactionIds = generateTransactions(3000, orderIds);

            System.out.println("✅ Data generation completed!");
            System.out.println("   Admins:       " + adminIds.size());
            System.out.println("   Sellers:      " + sellerIds.size());
            System.out.println("   Customers:    " + customerIds.size());
            System.out.println("   Products:     " + productIds.size());
            System.out.println("   Events:       " + eventIds.size());
            System.out.println("   FlashItems:   " + flashItemIds.size());
            System.out.println("   Orders:       " + orderIds.size());
            System.out.println("   OrderDetails: " + detailIds.size());
            System.out.println("   Transactions: " + transactionIds.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> generateAdmins(int count) throws IOException {
        List<String> ids = new ArrayList<>();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "admins.csv"))) {
            bw.write("id,name,email,password,status,roleLevel");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String name = generateName();
                Admin admin = new Admin(String.format("A%05d", i), name, generateEmail(name), "pass123", AccountStatus.APPROVED, 1);
                ids.add(admin.getId());
                bw.write(admin.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateSellers(int count) throws IOException {
        List<String> ids = new ArrayList<>();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "sellers.csv"))) {
            bw.write("id,name,email,password,status,storeName");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String name = generateName();
                Seller seller = new Seller(String.format("S%05d", i), name, generateEmail(name), "pass123", AccountStatus.APPROVED, name + " Store");
                ids.add(seller.getId());
                bw.write(seller.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateCustomers(int count) throws IOException {
        List<String> ids = new ArrayList<>();
        CustTier[] tiers = CustTier.values();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "customers.csv"))) {
            bw.write("id,name,email,password,status,tier,address");
            bw.newLine();
            
            Customer mainCust = new Customer("C00001", "Truong Gia Huy", "truonggiahuy@gmail.com", "pass123", AccountStatus.APPROVED, CustTier.GOLD, "123 Duong ABC, Quan 1, TP.HCM");
            ids.add(mainCust.getId());
            bw.write(mainCust.toCsvLine());
            bw.newLine();

            String[] streets = {"Le Loi", "Nguyen Hue", "Tran Hung Dao", "Vo Van Kiet", "Hai Ba Trung", "Dien Bien Phu", "Pasteur", "Ly Tu Trong"};
            String[] districts = {"Quan 1", "Quan 3", "Quan 5", "Quan 10", "Tan Binh", "Phu Nhuan", "Binh Thanh"};

            for (int i = 2; i <= count; i++) {
                String name = generateName();
                String address = "";
                // 80% có địa chỉ, 20% bỏ trống
                if (RANDOM.nextDouble() > 0.2) {
                    address = (RANDOM.nextInt(999) + 1) + " " + streets[RANDOM.nextInt(streets.length)] + ", " + districts[RANDOM.nextInt(districts.length)] + ", TP.HCM";
                }
                
                Customer customer = new Customer(String.format("C%05d", i), name, generateEmail(name), "pass123", RANDOM.nextDouble() > 0.1 ? AccountStatus.APPROVED : AccountStatus.BANNED, tiers[RANDOM.nextInt(tiers.length)], address);
                ids.add(customer.getId());
                bw.write(customer.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateProducts(int count, List<String> sellerIds) throws IOException {
        List<String> ids = new ArrayList<>();
        String[] categories = {"Electronics", "Fashion", "Home", "Beauty", "Sports"};
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "products.csv"))) {
            bw.write("id,sellerId,name,category,price,stock");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String sellerId = sellerIds.get(RANDOM.nextInt(sellerIds.size()));
                // Chon category ngau nhien
                String category = categories[RANDOM.nextInt(categories.length)];
                // Lay ten san pham PHU HOP voi category
                String[] prefixes = CATEGORY_PREFIXES.get(category);
                String[] suffixes = CATEGORY_SUFFIXES.get(category);
                String prodName = prefixes[RANDOM.nextInt(prefixes.length)] + " " + suffixes[RANDOM.nextInt(suffixes.length)];
                double generatedPrice = (RANDOM.nextInt(10000) + 50) * 1000.0;
                Product product = new Product(String.format("P%05d", i), sellerId, prodName, category, generatedPrice, RANDOM.nextInt(500) + 10);
                ids.add(product.getId());
                bw.write(product.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateEvents(int count) throws IOException {
        List<String> ids = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "flash_events.csv"))) {
            bw.write("id,name,startTime,endTime,status");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                LocalDateTime start = now.minusDays(RANDOM.nextInt(30));
                LocalDateTime end = start.plusHours(RANDOM.nextInt(48) + 1);
                SaleStatus status = end.isBefore(now) ? SaleStatus.ENDED : SaleStatus.ONGOING;
                String eventName = EVENT_NAMES[RANDOM.nextInt(EVENT_NAMES.length)] + " " + i;
                FlashSaleEvent event = new FlashSaleEvent(String.format("E%05d", i), eventName, start, end, status);
                ids.add(event.getId());
                bw.write(event.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateFlashItems(int count, List<String> productIds, List<String> eventIds) throws IOException {
        List<String> ids = new ArrayList<>();
        
        // Doc tam products de lay gia goc
        Map<String, Double> productPrices = new HashMap<>();
        try (Scanner sc = new Scanner(new java.io.File(DATA_DIR + "products.csv"))) {
            if (sc.hasNextLine()) sc.nextLine();
            while (sc.hasNextLine()) {
                String[] parts = util.CsvUtil.splitCsvLine(sc.nextLine());
                if (parts.length >= 5) {
                    productPrices.put(parts[0], Double.parseDouble(parts[4]));
                }
            }
        } catch (Exception e) {}

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "flash_items.csv"))) {
            bw.write("id,productId,eventId,salePrice,limitedQty,soldQty,version");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String prodId = productIds.get(RANDOM.nextInt(productIds.size()));
                String eventId = eventIds.get(RANDOM.nextInt(eventIds.size()));
                int limit = RANDOM.nextInt(100) + 10;
                
                double originalPrice = productPrices.getOrDefault(prodId, (RANDOM.nextInt(5000) + 50) * 1000.0);
                double salePrice = originalPrice * (1.0 - (RANDOM.nextInt(50) + 10) / 100.0); // Giam tu 10% den 60%
                
                FlashSaleItem item = new FlashSaleItem(String.format("FI%05d", i), prodId, eventId, salePrice, limit, RANDOM.nextInt(limit), 1);
                ids.add(item.getId());
                bw.write(item.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateOrders(int count, List<String> customerIds) throws IOException {
        List<String> ids = new ArrayList<>();
        OrderStatus[] statuses = OrderStatus.values();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "orders.csv"))) {
            bw.write("id,customerId,orderTime,status");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String custId = customerIds.get(RANDOM.nextInt(customerIds.size()));
                Order order = new Order(String.format("O%05d", i), custId, now.minusDays(RANDOM.nextInt(30)), statuses[RANDOM.nextInt(statuses.length)]);
                ids.add(order.getId());
                bw.write(order.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateOrderDetails(int count, List<String> orderIds, List<String> itemIds) throws IOException {
        List<String> ids = new ArrayList<>();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "order_details.csv"))) {
            bw.write("id,orderId,flashSaleItemId,quantity,priceAtPurchase");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String orderId = orderIds.get(RANDOM.nextInt(orderIds.size()));
                String itemId = itemIds.get(RANDOM.nextInt(itemIds.size()));
                double purchasePrice = (RANDOM.nextInt(5000) + 50) * 1000.0;
                OrderDetail detail = new OrderDetail(String.format("OD%05d", i), orderId, itemId, RANDOM.nextInt(5) + 1, purchasePrice);
                ids.add(detail.getId());
                bw.write(detail.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }

    private static List<String> generateTransactions(int count, List<String> orderIds) throws IOException {
        List<String> ids = new ArrayList<>();
        LockMechanism[] locks = LockMechanism.values();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "transactions.csv"))) {
            bw.write("id,orderId,lockMechanism,retryCount,processingTimeMs,success");
            bw.newLine();
            for (int i = 1; i <= count; i++) {
                String orderId = orderIds.get(RANDOM.nextInt(orderIds.size()));
                OrderTransaction trans = new OrderTransaction(String.format("T%05d", i), orderId, locks[RANDOM.nextInt(locks.length)], RANDOM.nextInt(3), RANDOM.nextInt(500) + 10, RANDOM.nextBoolean());
                ids.add(trans.getId());
                bw.write(trans.toCsvLine());
                bw.newLine();
            }
        }
        return ids;
    }
}

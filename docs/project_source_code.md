### File: model\BaseEntity.java
```java
package model;

public abstract class BaseEntity {
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract String toCsvLine();
    public abstract void fromCsvLine(String line);
}

```

### File: model\Customer.java
```java
package model;

import model.enums.CustTier;

public class Customer extends BaseEntity {
    private String name;
    private String email;
    private CustTier tier;

    public Customer() {}

    public Customer(String id, String name, String email, CustTier tier) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.tier = tier;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public CustTier getTier() { return tier; }
    public void setTier(CustTier tier) { this.tier = tier; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                escapeCsv(name),
                email,
                tier.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid Customer CSV line");
        this.id = parts[0].trim();
        this.name = unescapeCsv(parts[1].trim());
        this.email = parts[2].trim();
        this.tier = CustTier.valueOf(parts[3].trim());
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String unescapeCsv(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }
}

```

### File: model\FlashSaleEvent.java
```java
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlashSaleEvent extends BaseEntity {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public FlashSaleEvent() {}

    public FlashSaleEvent(String id, String name, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                escapeCsv(name),
                startTime.format(DTF),
                endTime.format(DTF)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid FlashSaleEvent CSV line");
        this.id = parts[0].trim();
        this.name = unescapeCsv(parts[1].trim());
        this.startTime = LocalDateTime.parse(parts[2].trim(), DTF);
        this.endTime = LocalDateTime.parse(parts[3].trim(), DTF);
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String unescapeCsv(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }
}

```

### File: model\FlashSaleItem.java
```java
package model;

public class FlashSaleItem extends BaseEntity {
    private String productId;
    private String eventId;
    private int limitedQty;   // giá»›i háº¡n flash sale
    private int soldQty;      // Ä‘Ã£ bÃ¡n
    private int version;      // cho optimistic lock, khá»Ÿi táº¡o = 1

    public FlashSaleItem() {
        this.version = 1;
    }

    public FlashSaleItem(String id, String productId, String eventId, int limitedQty, int soldQty, int version) {
        this.id = id;
        this.productId = productId;
        this.eventId = eventId;
        this.limitedQty = limitedQty;
        this.soldQty = soldQty;
        this.version = version;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public int getLimitedQty() { return limitedQty; }
    public void setLimitedQty(int limitedQty) { this.limitedQty = limitedQty; }
    public int getSoldQty() { return soldQty; }
    public void setSoldQty(int soldQty) { this.soldQty = soldQty; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public boolean hasStock(int requestedQty) {
        return (soldQty + requestedQty) <= limitedQty;
    }

    public void increaseSold(int qty) {
        this.soldQty += qty;
        this.version++;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                productId,
                eventId,
                String.valueOf(limitedQty),
                String.valueOf(soldQty),
                String.valueOf(version)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) throw new IllegalArgumentException("Invalid FlashSaleItem CSV line");
        this.id = parts[0].trim();
        this.productId = parts[1].trim();
        this.eventId = parts[2].trim();
        this.limitedQty = Integer.parseInt(parts[3].trim());
        this.soldQty = Integer.parseInt(parts[4].trim());
        this.version = Integer.parseInt(parts[5].trim());
    }
}

```

### File: model\Order.java
```java
package model;

import model.enums.OrderStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order extends BaseEntity {
    private String customerId;
    private LocalDateTime orderTime;
    private OrderStatus status;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Order() {}

    public Order(String id, String customerId, LocalDateTime orderTime, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.orderTime = orderTime;
        this.status = status;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                customerId,
                orderTime.format(DTF),
                status.name()
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid Order CSV line");
        this.id = parts[0].trim();
        this.customerId = parts[1].trim();
        this.orderTime = LocalDateTime.parse(parts[2].trim(), DTF);
        this.status = OrderStatus.valueOf(parts[3].trim());
    }
}

```

### File: model\OrderDetail.java
```java
package model;

public class OrderDetail extends BaseEntity {
    private String orderId;
    private String flashSaleItemId;
    private int quantity;   // 1 hoáº·c 2 (khÃ¡ch chá»‰ Ä‘Æ°á»£c mua tá»‘i Ä‘a 2)

    public OrderDetail() {}

    public OrderDetail(String id, String orderId, String flashSaleItemId, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.flashSaleItemId = flashSaleItemId;
        this.quantity = quantity;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getFlashSaleItemId() { return flashSaleItemId; }
    public void setFlashSaleItemId(String flashSaleItemId) { this.flashSaleItemId = flashSaleItemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                orderId,
                flashSaleItemId,
                String.valueOf(quantity)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) throw new IllegalArgumentException("Invalid OrderDetail CSV line");
        this.id = parts[0].trim();
        this.orderId = parts[1].trim();
        this.flashSaleItemId = parts[2].trim();
        this.quantity = Integer.parseInt(parts[3].trim());
    }
}

```

### File: model\OrderTransaction.java
```java
package model;

import model.enums.LockMechanism;

public class OrderTransaction extends BaseEntity {
    private String orderId;
    private LockMechanism lockMechanism;
    private int retryCount;
    private long processingTimeMs;
    private boolean success;

    public OrderTransaction() {}

    public OrderTransaction(String id, String orderId, LockMechanism lockMechanism, int retryCount, long processingTimeMs, boolean success) {
        this.id = id;
        this.orderId = orderId;
        this.lockMechanism = lockMechanism;
        this.retryCount = retryCount;
        this.processingTimeMs = processingTimeMs;
        this.success = success;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LockMechanism getLockMechanism() { return lockMechanism; }
    public void setLockMechanism(LockMechanism lockMechanism) { this.lockMechanism = lockMechanism; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                orderId,
                lockMechanism.name(),
                String.valueOf(retryCount),
                String.valueOf(processingTimeMs),
                String.valueOf(success)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) throw new IllegalArgumentException("Invalid OrderTransaction CSV line");
        this.id = parts[0].trim();
        this.orderId = parts[1].trim();
        this.lockMechanism = LockMechanism.valueOf(parts[2].trim());
        this.retryCount = Integer.parseInt(parts[3].trim());
        this.processingTimeMs = Long.parseLong(parts[4].trim());
        this.success = Boolean.parseBoolean(parts[5].trim());
    }
}

```

### File: model\Product.java
```java
package model;

public class Product extends BaseEntity {
    private String name;
    private String category;
    private double price;
    private int stock;  // tá»“n kho váº­t lÃ½ (khÃ´ng dÃ¹ng cho flash sale, chá»‰ Ä‘á»ƒ tham kháº£o)

    public Product() {}

    public Product(String id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toCsvLine() {
        return String.join(",",
                id,
                escapeCsv(name),
                escapeCsv(category),
                String.valueOf(price),
                String.valueOf(stock)
        );
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) throw new IllegalArgumentException("Invalid Product CSV line");
        this.id = parts[0].trim();
        this.name = unescapeCsv(parts[1].trim());
        this.category = unescapeCsv(parts[2].trim());
        this.price = Double.parseDouble(parts[3].trim());
        this.stock = Integer.parseInt(parts[4].trim());
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String unescapeCsv(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\"\"", "\"");
        }
        return s;
    }
}

```

### File: model\enums\CustTier.java
```java
package model.enums;

public enum CustTier {
    NORMAL, VIP, PREMIUM
}

```

### File: model\enums\LockMechanism.java
```java
package model.enums;

public enum LockMechanism {
    NO_LOCK, FILE_LOCK, SYNCHRONIZED, OPTIMISTIC
}

```

### File: model\enums\OrderStatus.java
```java
package model.enums;

public enum OrderStatus {
    PENDING, SUCCESS, FAILED_OUT_OF_STOCK, FAILED_RETRY_EXCEEDED
}

```

### File: test\CsvParseTest.java
```java
package test;

import model.*;
import model.enums.*;
import java.time.LocalDateTime;

public class CsvParseTest {
    public static void main(String[] args) {
        System.out.println("Starting CSV Parse Round-Trip Tests...");
        try {
            testProduct();
            testCustomer();
            testFlashSaleEvent();
            testFlashSaleItem();
            testOrder();
            testOrderDetail();
            testOrderTransaction();
            
            System.out.println("==========================================");
            System.out.println("100% test pass. All Entities successfully parsed and serialized.");
            System.out.println("==========================================");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testProduct() {
        Product p1 = new Product("P001", "Laptop Dell, Gaming", "Electronics", 1500.50, 10);
        String csv = p1.toCsvLine();
        Product p2 = new Product();
        p2.fromCsvLine(csv);
        
        assertEquals(p1.getId(), p2.getId(), "Product ID mismatch");
        assertEquals(p1.getName(), p2.getName(), "Product Name mismatch");
        assertEquals(p1.getCategory(), p2.getCategory(), "Product Category mismatch");
        assertEquals(p1.getPrice(), p2.getPrice(), "Product Price mismatch");
        assertEquals(p1.getStock(), p2.getStock(), "Product Stock mismatch");
    }

    private static void testCustomer() {
        Customer c1 = new Customer("C001", "Nguyen Van A", "a@gmail.com", CustTier.VIP);
        String csv = c1.toCsvLine();
        Customer c2 = new Customer();
        c2.fromCsvLine(csv);
        
        assertEquals(c1.getId(), c2.getId(), "Customer ID mismatch");
        assertEquals(c1.getName(), c2.getName(), "Customer Name mismatch");
        assertEquals(c1.getEmail(), c2.getEmail(), "Customer Email mismatch");
        assertEquals(c1.getTier(), c2.getTier(), "Customer Tier mismatch");
    }

    private static void testFlashSaleEvent() {
        FlashSaleEvent e1 = new FlashSaleEvent("E001", "Black Friday", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        String csv = e1.toCsvLine();
        FlashSaleEvent e2 = new FlashSaleEvent();
        e2.fromCsvLine(csv);
        
        assertEquals(e1.getId(), e2.getId(), "Event ID mismatch");
        assertEquals(e1.getName(), e2.getName(), "Event Name mismatch");
        assertEquals(e1.getStartTime().toString(), e2.getStartTime().toString(), "Event StartTime mismatch");
        assertEquals(e1.getEndTime().toString(), e2.getEndTime().toString(), "Event EndTime mismatch");
    }

    private static void testFlashSaleItem() {
        FlashSaleItem i1 = new FlashSaleItem("FI001", "P001", "E001", 100, 5, 2);
        String csv = i1.toCsvLine();
        FlashSaleItem i2 = new FlashSaleItem();
        i2.fromCsvLine(csv);
        
        assertEquals(i1.getId(), i2.getId(), "Item ID mismatch");
        assertEquals(i1.getProductId(), i2.getProductId(), "Item ProductId mismatch");
        assertEquals(i1.getEventId(), i2.getEventId(), "Item EventId mismatch");
        assertEquals(i1.getLimitedQty(), i2.getLimitedQty(), "Item LimitedQty mismatch");
        assertEquals(i1.getSoldQty(), i2.getSoldQty(), "Item SoldQty mismatch");
        assertEquals(i1.getVersion(), i2.getVersion(), "Item Version mismatch");
    }

    private static void testOrder() {
        Order o1 = new Order("O001", "C001", LocalDateTime.now(), OrderStatus.SUCCESS);
        String csv = o1.toCsvLine();
        Order o2 = new Order();
        o2.fromCsvLine(csv);
        
        assertEquals(o1.getId(), o2.getId(), "Order ID mismatch");
        assertEquals(o1.getCustomerId(), o2.getCustomerId(), "Order CustomerId mismatch");
        assertEquals(o1.getOrderTime().toString(), o2.getOrderTime().toString(), "Order OrderTime mismatch");
        assertEquals(o1.getStatus(), o2.getStatus(), "Order Status mismatch");
    }

    private static void testOrderDetail() {
        OrderDetail d1 = new OrderDetail("OD001", "O001", "FI001", 2);
        String csv = d1.toCsvLine();
        OrderDetail d2 = new OrderDetail();
        d2.fromCsvLine(csv);
        
        assertEquals(d1.getId(), d2.getId(), "Detail ID mismatch");
        assertEquals(d1.getOrderId(), d2.getOrderId(), "Detail OrderId mismatch");
        assertEquals(d1.getFlashSaleItemId(), d2.getFlashSaleItemId(), "Detail FlashSaleItemId mismatch");
        assertEquals(d1.getQuantity(), d2.getQuantity(), "Detail Quantity mismatch");
    }

    private static void testOrderTransaction() {
        OrderTransaction t1 = new OrderTransaction("T001", "O001", LockMechanism.OPTIMISTIC, 3, 150L, true);
        String csv = t1.toCsvLine();
        OrderTransaction t2 = new OrderTransaction();
        t2.fromCsvLine(csv);
        
        assertEquals(t1.getId(), t2.getId(), "Transaction ID mismatch");
        assertEquals(t1.getOrderId(), t2.getOrderId(), "Transaction OrderId mismatch");
        assertEquals(t1.getLockMechanism(), t2.getLockMechanism(), "Transaction LockMechanism mismatch");
        assertEquals(t1.getRetryCount(), t2.getRetryCount(), "Transaction RetryCount mismatch");
        assertEquals(t1.getProcessingTimeMs(), t2.getProcessingTimeMs(), "Transaction ProcessingTimeMs mismatch");
        assertEquals(t1.isSuccess(), t2.isSuccess(), "Transaction Success mismatch");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new RuntimeException(message + " - Expected: " + expected + ", Actual: " + actual);
    }
}

```

### File: util\DataGenerator.java
```java
package util;

import model.*;
import model.enums.CustTier;
import model.enums.OrderStatus;
import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.DecimalFormat;

public class DataGenerator {
    private static final Random random = new Random();
    private static final String DATA_DIR = "data/";
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    // Cáº¥u hÃ¬nh sá»‘ lÆ°á»£ng báº£n ghi
    private static final int PRODUCT_COUNT = 5000;
    private static final int CUSTOMER_COUNT = 2000;
    private static final int FLASH_ITEM_COUNT = 500;
    private static final int ORDER_COUNT = 2500;

    public static void main(String[] args) throws IOException {
        new File(DATA_DIR).mkdirs();

        // 1. Sinh products.csv
        List<Product> products = generateProducts(PRODUCT_COUNT);
        writeCsv(DATA_DIR + "products.csv", products,
                "id,name,category,price,stock",
                Product::toCsvLine);

        // 2. Sinh customers.csv
        List<Customer> customers = generateCustomers(CUSTOMER_COUNT);
        writeCsv(DATA_DIR + "customers.csv", customers,
                "id,name,email,tier",
                Customer::toCsvLine);

        // 3. Sinh flash_events.csv (2 events)
        List<FlashSaleEvent> events = generateEvents(2);
        writeCsv(DATA_DIR + "flash_events.csv", events,
                "id,name,startTime,endTime",
                FlashSaleEvent::toCsvLine);

        // 4. Sinh flash_items.csv (cÃ³ cáº­p nháº­t soldQty sau khi táº¡o order)
        List<FlashSaleItem> flashItems = generateFlashItems(FLASH_ITEM_COUNT, products, events);
        // Ghi táº¡m flash_items.csv (soldQty = 0) â€“ sáº½ ghi láº¡i sau khi cáº­p nháº­t
        writeCsv(DATA_DIR + "flash_items_temp.csv", flashItems,
                "id,productId,eventId,limitedQty,soldQty,version",
                FlashSaleItem::toCsvLine);

        // 5. Sinh orders.csv vÃ  order_details.csv, Ä‘á»“ng thá»i cáº­p nháº­t soldQty thá»±c táº¿
        List<Order> orders = new ArrayList<>();
        List<OrderDetail> orderDetails = new ArrayList<>();
        generateOrdersAndDetails(ORDER_COUNT, customers, flashItems, events, orders, orderDetails);

        // Ghi láº¡i flash_items.csv vá»›i soldQty Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t
        writeCsv(DATA_DIR + "flash_items.csv", flashItems,
                "id,productId,eventId,limitedQty,soldQty,version",
                FlashSaleItem::toCsvLine);
        // XÃ³a file táº¡m
        new File(DATA_DIR + "flash_items_temp.csv").delete();

        writeCsv(DATA_DIR + "orders.csv", orders,
                "id,customerId,orderTime,status",
                Order::toCsvLine);
        writeCsv(DATA_DIR + "order_details.csv", orderDetails,
                "id,orderId,flashSaleItemId,quantity",
                OrderDetail::toCsvLine);

        // 6. Táº¡o transactions.csv rá»—ng (chá»‰ cÃ³ header)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "transactions.csv"))) {
            bw.write("orderId,lockMechanism,retryCount,processingTimeMs,success,timestamp");
            bw.newLine();
        }

        System.out.println("âœ… Data generation completed!");
        System.out.println("Products: " + products.size());
        System.out.println("Customers: " + customers.size());
        System.out.println("FlashItems: " + flashItems.size());
        System.out.println("Orders: " + orders.size());
        System.out.println("OrderDetails: " + orderDetails.size());
    }

    private static List<Product> generateProducts(int count) {
        List<Product> list = new ArrayList<>();
        String[] cats = {"Electronics", "Fashion", "Home", "Books", "Toys"};
        for (int i = 1; i <= count; i++) {
            Product p = new Product();
            p.setId("P" + String.format("%05d", i));
            p.setName("Product_" + i);
            p.setCategory(cats[random.nextInt(cats.length)]);
            double price = 10 + random.nextDouble() * 990;
            p.setPrice(Double.parseDouble(DF.format(price))); // lÃ m trÃ²n 2 chá»¯ sá»‘
            p.setStock(random.nextInt(1000));
            list.add(p);
        }
        return list;
    }

    private static List<Customer> generateCustomers(int count) {
        List<Customer> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Customer c = new Customer();
            c.setId("C" + String.format("%05d", i));
            c.setName("Customer_" + i);
            c.setEmail("user" + i + "@example.com");
            CustTier tier = CustTier.values()[random.nextInt(3)];
            c.setTier(tier);
            list.add(c);
        }
        return list;
    }

    private static List<FlashSaleEvent> generateEvents(int count) {
        List<FlashSaleEvent> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (int i = 1; i <= count; i++) {
            FlashSaleEvent e = new FlashSaleEvent();
            e.setId("E" + i);
            e.setName("Flash Sale " + i);
            // Event 1: báº¯t Ä‘áº§u tá»« hÃ´m qua, kÃ©o dÃ i 2 giá»
            // Event 2: báº¯t Ä‘áº§u tá»« hÃ´m nay, kÃ©o dÃ i 2 giá»
            LocalDateTime start = now.minusDays(2 - i).plusHours(i - 1);
            e.setStartTime(start);
            e.setEndTime(start.plusHours(2));
            list.add(e);
        }
        return list;
    }

    private static List<FlashSaleItem> generateFlashItems(int count, List<Product> products, List<FlashSaleEvent> events) {
        List<FlashSaleItem> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            FlashSaleItem fi = new FlashSaleItem();
            fi.setId("FI" + String.format("%05d", i));
            fi.setProductId(products.get(random.nextInt(products.size())).getId());
            fi.setEventId(events.get(random.nextInt(events.size())).getId());
            fi.setLimitedQty(10 + random.nextInt(91)); // 10..100
            fi.setSoldQty(0);
            fi.setVersion(1);
            list.add(fi);
        }
        return list;
    }

    private static void generateOrdersAndDetails(int orderCount, List<Customer> customers,
                                                  List<FlashSaleItem> flashItems,
                                                  List<FlashSaleEvent> events,
                                                  List<Order> orders, List<OrderDetail> details) {
        // NhÃ³m flash items theo eventId Ä‘á»ƒ dá»… láº¥y item cÃ²n slot
        Map<String, List<FlashSaleItem>> itemsByEvent = new HashMap<>();
        for (FlashSaleItem item : flashItems) {
            itemsByEvent.computeIfAbsent(item.getEventId(), k -> new ArrayList<>()).add(item);
        }

        AtomicInteger detailIdCounter = new AtomicInteger(1);
        int maxAttempts = 100; // trÃ¡nh loop vÃ´ háº¡n náº¿u khÃ´ng cÃ²n slot

        for (int i = 1; i <= orderCount; i++) {
            String orderId = "O" + String.format("%05d", i);
            Customer cust = customers.get(random.nextInt(customers.size()));

            // Chá»n ngáº«u nhiÃªn má»™t event (Ä‘á»ƒ Ä‘Æ¡n hÃ ng náº±m trong khoáº£ng thá»i gian Ä‘Ã³)
            FlashSaleEvent event = events.get(random.nextInt(events.size()));
            // Sinh orderTime trong khoáº£ng [start, end] cá»§a event
            LocalDateTime orderTime = randomTimeBetween(event.getStartTime(), event.getEndTime());

            Order order = new Order();
            order.setId(orderId);
            order.setCustomerId(cust.getId());
            order.setOrderTime(orderTime);
            order.setStatus(OrderStatus.PENDING);
            orders.add(order);

            int itemsInOrder = 1 + random.nextInt(2); // 1 hoáº·c 2 sáº£n pháº©m flash
            for (int j = 0; j < itemsInOrder; j++) {
                // TÃ¬m flash item cá»§a cÃ¹ng event cÃ²n Ä‘á»§ slot
                FlashSaleItem selected = null;
                int attempts = 0;
                List<FlashSaleItem> candidates = itemsByEvent.get(event.getId());
                if (candidates == null || candidates.isEmpty()) continue;

                while (attempts < maxAttempts && selected == null) {
                    FlashSaleItem candidate = candidates.get(random.nextInt(candidates.size()));
                    int qty = 1 + random.nextInt(2); // 1 hoáº·c 2
                    synchronized (candidate) {
                        if (candidate.getSoldQty() + qty <= candidate.getLimitedQty()) {
                            candidate.setSoldQty(candidate.getSoldQty() + qty);
                            selected = candidate;
                            // Táº¡o OrderDetail
                            OrderDetail od = new OrderDetail();
                            od.setId("OD" + String.format("%06d", detailIdCounter.getAndIncrement()));
                            od.setOrderId(orderId);
                            od.setFlashSaleItemId(selected.getId());
                            od.setQuantity(qty);
                            details.add(od);
                        }
                    }
                    attempts++;
                }
                // Náº¿u khÃ´ng tÃ¬m Ä‘Æ°á»£c slot, bá» qua máº·t hÃ ng nÃ y (order váº«n cÃ³ thá»ƒ chá»‰ cÃ³ 1 item)
            }
        }
    }

    private static LocalDateTime randomTimeBetween(LocalDateTime start, LocalDateTime end) {
        long secondsBetween = ChronoUnit.SECONDS.between(start, end);
        if (secondsBetween <= 0) return start;
        long randomSeconds = random.nextInt((int) secondsBetween + 1);
        return start.plusSeconds(randomSeconds);
    }

    private static <T> void writeCsv(String path, List<T> data, String header, CsvWriter<T> writer) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            if (header != null) {
                bw.write(header);
                bw.newLine();
            }
            for (T item : data) {
                bw.write(writer.toCsvLine(item));
                bw.newLine();
            }
        }
    }

    interface CsvWriter<T> {
        String toCsvLine(T item);
    }
}

```


package util;

import model.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataGenerator {
    private static final Random random = new Random();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static void main(String[] args) throws IOException {
        String dataDir = "data/";
        new File(dataDir).mkdirs();

        // 1. Sinh products.csv (≥ 5000 dòng)
        List<Product> products = generateProducts(5000);
        writeCsv(dataDir + "products.csv", products, Product::toCsvLine);

        // 2. Sinh customers.csv (≥ 2000 dòng)
        List<Customer> customers = generateCustomers(2000);
        writeCsv(dataDir + "customers.csv", customers, Customer::toCsvLine);

        // 3. Sinh flash_events.csv (2 events)
        List<FlashSaleEvent> events = generateEvents(2);
        writeCsv(dataDir + "flash_events.csv", events, FlashSaleEvent::toCsvLine);

        // 4. Sinh flash_items.csv (≥ 500 dòng, mỗi dòng là một sản phẩm tham gia event)
        List<FlashSaleItem> flashItems = generateFlashItems(500, products, events);
        writeCsv(dataDir + "flash_items.csv", flashItems, FlashSaleItem::toCsvLine);

        // 5. Sinh orders.csv (≥ 2500) và order_details.csv (tương ứng)
        List<Order> orders = new ArrayList<>();
        List<OrderDetail> orderDetails = new ArrayList<>();
        generateOrdersAndDetails(2500, customers, flashItems, orders, orderDetails);
        writeCsv(dataDir + "orders.csv", orders, Order::toCsvLine);
        writeCsv(dataDir + "order_details.csv", orderDetails, OrderDetail::toCsvLine);

        System.out.println("✅ Data generated successfully. Total lines: " +
                (products.size() + customers.size() + events.size() + flashItems.size() + orders.size() + orderDetails.size()));
    }

    private static List<Product> generateProducts(int count) {
        List<Product> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Product p = new Product();
            p.setId("P" + String.format("%04d", i));
            p.setName("Product_" + i);
            p.setCategory(randomCategory());
            p.setPrice(10 + random.nextDouble() * 990);
            p.setStock(random.nextInt(1000));
            list.add(p);
        }
        return list;
    }

    private static List<Customer> generateCustomers(int count) {
        List<Customer> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Customer c = new Customer();
            c.setId("C" + String.format("%04d", i));
            c.setName("Customer_" + i);
            c.setEmail("user" + i + "@example.com");
            c.setTier(CustTier.values()[random.nextInt(3)]);
            list.add(c);
        }
        return list;
    }

    private static List<FlashSaleEvent> generateEvents(int count) {
        List<FlashSaleEvent> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= count; i++) {
            FlashSaleEvent e = new FlashSaleEvent();
            e.setId("E" + i);
            e.setName("Flash Sale " + i);
            e.setStartTime(now.plusDays(i-1));
            e.setEndTime(now.plusDays(i-1).plusHours(2));
            list.add(e);
        }
        return list;
    }

    private static List<FlashSaleItem> generateFlashItems(int count, List<Product> products, List<FlashSaleEvent> events) {
        List<FlashSaleItem> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            FlashSaleItem item = new FlashSaleItem();
            item.setId("FI" + String.format("%04d", i));
            item.setProductId(products.get(random.nextInt(products.size())).getId());
            item.setEventId(events.get(random.nextInt(events.size())).getId());
            item.setLimitedQty(10 + random.nextInt(91)); // 10..100
            item.setSoldQty(0);
            item.setVersion(1);
            list.add(item);
        }
        return list;
    }

    private static void generateOrdersAndDetails(int orderCount, List<Customer> customers,
                                                 List<FlashSaleItem> flashItems,
                                                 List<Order> orders, List<OrderDetail> details) {
        for (int i = 1; i <= orderCount; i++) {
            String orderId = "O" + String.format("%05d", i);
            Customer cust = customers.get(random.nextInt(customers.size()));
            Order order = new Order();
            order.setId(orderId);
            order.setCustomerId(cust.getId());
            order.setOrderTime(LocalDateTime.now().minusMinutes(random.nextInt(10080)));
            order.setStatus(OrderStatus.SUCCESS); // mặc định, sau này simulator sẽ ghi thực tế
            orders.add(order);

            // Mỗi order có 1-2 sản phẩm flash
            int itemsInOrder = 1 + random.nextInt(2);
            for (int j = 0; j < itemsInOrder; j++) {
                FlashSaleItem fi = flashItems.get(random.nextInt(flashItems.size()));
                OrderDetail od = new OrderDetail();
                od.setId("OD" + String.format("%06d", details.size() + 1));
                od.setOrderId(orderId);
                od.setFlashSaleItemId(fi.getId());
                od.setQuantity(1 + random.nextInt(2)); // max 2
                details.add(od);
            }
        }
    }

    private static String randomCategory() {
        String[] cats = {"Electronics", "Fashion", "Home", "Books", "Toys"};
        return cats[random.nextInt(cats.length)];
    }

    private static <T> void writeCsv(String path, List<T> data, CsvWriter<T> writer) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (T item : data) {
                bw.write(writer.toCsvLine(item));
                bw.newLine();
            }
        }
    }

    interface CsvWriter<T> { String toCsvLine(T item); }
}

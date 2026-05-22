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

    // Cau hinh so luong ban ghi
    private static final int PRODUCT_COUNT = 5000;
    private static final int CUSTOMER_COUNT = 2000;
    private static final int FLASH_ITEM_COUNT = 500;
    private static final int ORDER_COUNT = 2500;

    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Started generating data... Please wait.");
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

        // 4. Sinh flash_items.csv (co cap nhat soldQty sau khi tao order)
        List<FlashSaleItem> flashItems = generateFlashItems(FLASH_ITEM_COUNT, products, events);
        // Ghi tam flash_items.csv (soldQty = 0) ? se ghi lai sau khi cap nhat
        writeCsv(DATA_DIR + "flash_items_temp.csv", flashItems,
                "id,productId,eventId,limitedQty,soldQty,version",
                FlashSaleItem::toCsvLine);

        // 5. Sinh orders.csv va order_details.csv, dong thoi cap nhat soldQty thuc te
        List<Order> orders = new ArrayList<>();
        List<OrderDetail> orderDetails = new ArrayList<>();
        generateOrdersAndDetails(ORDER_COUNT, customers, flashItems, events, orders, orderDetails);

        // Ghi lai flash_items.csv voi soldQty da duoc cap nhat
        writeCsv(DATA_DIR + "flash_items.csv", flashItems,
                "id,productId,eventId,limitedQty,soldQty,version",
                FlashSaleItem::toCsvLine);
        // Xoa file tam
        new File(DATA_DIR + "flash_items_temp.csv").delete();

        writeCsv(DATA_DIR + "orders.csv", orders,
                "id,customerId,orderTime,status",
                Order::toCsvLine);
        writeCsv(DATA_DIR + "order_details.csv", orderDetails,
                "id,orderId,flashSaleItemId,quantity",
                OrderDetail::toCsvLine);

        // 6. Tao transactions.csv rong (chi co header)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "transactions.csv"))) {
            bw.write("id,orderId,lockMechanism,retryCount,processingTimeMs,success");
            bw.newLine();
        }

        System.out.println("? Data generation completed!");
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
            p.setPrice(Math.round(price * 100.0) / 100.0); // lam tron 2 chu so, an toan hon DecimalFormat
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
            // Event 1: bat dau tu hom qua, keo dai 2 gio
            // Event 2: bat dau tu hom nay, keo dai 2 gio
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
        // Nhom flash items theo eventId de de lay item con slot
        Map<String, List<FlashSaleItem>> itemsByEvent = new HashMap<>();
        for (FlashSaleItem item : flashItems) {
            itemsByEvent.computeIfAbsent(item.getEventId(), k -> new ArrayList<>()).add(item);
        }

        AtomicInteger detailIdCounter = new AtomicInteger(1);
        int maxAttempts = 100; // tranh loop vo han neu khong con slot

        for (int i = 1; i <= orderCount; i++) {
            String orderId = "O" + String.format("%05d", i);
            Customer cust = customers.get(random.nextInt(customers.size()));

            // Chon ngau nhien mot event (de don hang nam trong khoang thoi gian do)
            FlashSaleEvent event = events.get(random.nextInt(events.size()));
            // Sinh orderTime trong khoang [start, end] cua event
            LocalDateTime orderTime = randomTimeBetween(event.getStartTime(), event.getEndTime());

            Order order = new Order();
            order.setId(orderId);
            order.setCustomerId(cust.getId());
            order.setOrderTime(orderTime);
            order.setStatus(OrderStatus.PENDING);
            orders.add(order);

            int itemsInOrder = 1 + random.nextInt(2); // 1 hoac 2 san pham flash
            for (int j = 0; j < itemsInOrder; j++) {
                // Tim flash item cua cung event con du slot
                FlashSaleItem selected = null;
                int attempts = 0;
                List<FlashSaleItem> candidates = itemsByEvent.get(event.getId());
                if (candidates == null || candidates.isEmpty()) continue;

                while (attempts < maxAttempts && selected == null) {
                    FlashSaleItem candidate = candidates.get(random.nextInt(candidates.size()));
                    int qty = 1 + random.nextInt(2); // 1 hoac 2
                    synchronized (candidate) {
                        if (candidate.getSoldQty() + qty <= candidate.getLimitedQty()) {
                            candidate.increaseSold(qty);
                            selected = candidate;
                            // Tao OrderDetail
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
                // Neu khong tim duoc slot, bo qua mat hang nay (order van co the chi co 1 item)
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


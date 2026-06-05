package util;

import model.*;
import model.enums.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataGenerator {
    private static final Random random = new Random();
    private static final String DATA_DIR = "data/";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Cau hinh so luong ban ghi
    private static final int PRODUCT_COUNT = 5000;
    private static final int CUSTOMER_COUNT = 2500;
    private static final int EVENT_COUNT = 50;
    private static final int FLASH_ITEM_COUNT = 1000;
    private static final int ORDER_COUNT = 3000;

    // Moc thoi gian de sinh createdAt (6-12 thang truoc)
    private static final LocalDateTime NOW = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDateTime EARLIEST = NOW.minusMonths(12);

    // ======================== MAIN ========================
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Started generating data... Please wait.");
        new File(DATA_DIR).mkdirs();

        // 1. Sinh products.csv
        List<Product> products = generateProducts(PRODUCT_COUNT);
        writeCsv(DATA_DIR + "products.csv", products,
                "id,createdAt,updatedAt,name,category,price,stock",
                Product::toCsvLine);

        // 2. Sinh customers.csv
        List<Customer> customers = generateCustomers(CUSTOMER_COUNT);
        writeCsv(DATA_DIR + "customers.csv", customers,
                "id,createdAt,updatedAt,fullName,phone,email,tier,totalSpent,active",
                Customer::toCsvLine);

        // 3. Sinh flash_events.csv
        List<FlashSaleEvent> events = generateEvents(EVENT_COUNT);
        writeCsv(DATA_DIR + "flash_events.csv", events,
                "id,createdAt,updatedAt,eventName,startTime,endTime,status",
                FlashSaleEvent::toCsvLine);

        // 4. Sinh flash_items.csv (soldQty se duoc cap nhat sau khi tao order)
        List<FlashSaleItem> flashItems = generateFlashItems(FLASH_ITEM_COUNT, products, events);

        // 5. Sinh orders.csv va order_details.csv, dong thoi cap nhat soldQty thuc te
        List<Order> orders = new ArrayList<>();
        List<OrderDetail> orderDetails = new ArrayList<>();
        generateOrdersAndDetails(ORDER_COUNT, customers, flashItems, events, orders, orderDetails);

        // Ghi flash_items.csv voi soldQty da duoc cap nhat
        writeCsv(DATA_DIR + "flash_items.csv", flashItems,
                "id,createdAt,updatedAt,eventId,productId,flashPrice,limitedQty,soldQty,discountPercent,version,status",
                FlashSaleItem::toCsvLine);

        writeCsv(DATA_DIR + "orders.csv", orders,
                "id,createdAt,updatedAt,customerId,eventId,totalAmount,status,lockMechanism",
                Order::toCsvLine);
        writeCsv(DATA_DIR + "order_details.csv", orderDetails,
                "id,createdAt,updatedAt,orderId,flashSaleItemId,quantity,unitPrice,subTotal",
                OrderDetail::toCsvLine);

        // 6. Tao transactions.csv rong (chi co header)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "transactions.csv"))) {
            bw.write("id,createdAt,updatedAt,orderId,lockMechanism,retryCount,processingTimeMs,success");
            bw.newLine();
        }

        System.out.println("✅ Data generation completed!");
        System.out.println("   Products:     " + products.size());
        System.out.println("   Customers:    " + customers.size());
        System.out.println("   Events:       " + events.size());
        System.out.println("   FlashItems:   " + flashItems.size());
        System.out.println("   Orders:       " + orders.size());
        System.out.println("   OrderDetails: " + orderDetails.size());
    }

    // ======================== GENERATORS ========================

    private static List<Product> generateProducts(int count) {
        List<Product> list = new ArrayList<>();
        String[] cats = {"Electronics", "Fashion", "Home", "Books", "Toys"};
        for (int i = 1; i <= count; i++) {
            Product p = new Product();
            p.setId("P" + String.format("%05d", i));
            LocalDateTime created = randomPastTime();
            p.setCreatedAt(created);
            p.setUpdatedAt(randomAfter(created));
            p.setName("Product_" + i);
            p.setCategory(cats[random.nextInt(cats.length)]);
            double price = 10 + random.nextDouble() * 990;
            p.setPrice(Math.round(price * 100.0) / 100.0);
            p.setStock(random.nextInt(1000));
            list.add(p);
        }
        return list;
    }

    private static List<Customer> generateCustomers(int count) {
        List<Customer> list = new ArrayList<>();
        String[] firstNames = {"Hoang", "Nguyen", "Tran", "Le", "Pham", "Vo", "Dang", "Bui", "Do", "Ngo"};
        String[] lastNames = {"Bao Vy", "Anh Nam", "Duc Cuong", "Duc Phong", "Duc Yen",
                              "Minh Hieu", "Thanh Tung", "Quang Huy", "Thi Mai", "Van An"};

        for (int i = 1; i <= count; i++) {
            Customer c = new Customer();
            c.setId("C" + String.format("%05d", i));
            LocalDateTime created = randomPastTime();
            c.setCreatedAt(created);
            c.setUpdatedAt(randomAfter(created));

            String fn = firstNames[random.nextInt(firstNames.length)]
                    + " " + lastNames[random.nextInt(lastNames.length)];
            c.setFullName(fn);

            // Sinh so dien thoai 9 chu so (bat dau 9xx)
            c.setPhone("9" + String.format("%08d", random.nextInt(100000000)));

            c.setEmail(fn.toLowerCase().replace(" ", ".") + i + "@gmail.com");

            CustTier tier = CustTier.values()[random.nextInt(CustTier.values().length)];
            c.setTier(tier);

            // totalSpent phu thuoc tier
            double spent;
            switch (tier) {
                case VIP:     spent = 50000000 + random.nextDouble() * 50000000; break;
                case PREMIUM: spent = 10000000 + random.nextDouble() * 40000000; break;
                default:      spent = random.nextDouble() * 10000000; break;
            }
            c.setTotalSpent(Math.round(spent));

            // 85% active
            c.setActive(random.nextInt(100) < 85);

            list.add(c);
        }
        return list;
    }

    private static List<FlashSaleEvent> generateEvents(int count) {
        List<FlashSaleEvent> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            FlashSaleEvent e = new FlashSaleEvent();
            e.setId("E" + String.format("%03d", i));

            LocalDateTime created = randomPastTime();
            e.setCreatedAt(created);
            e.setUpdatedAt(randomAfter(created));

            e.setEventName("Mega Flash Sale " + i);

            // Phan bo thoi gian: qua khu, hien tai, tuong lai
            LocalDateTime start;
            if (i <= count * 0.6) {
                // 60% events da ket thuc (trong qua khu)
                start = NOW.minusDays(random.nextInt(180) + 1).withHour(11).withMinute(30).withSecond(21);
            } else if (i <= count * 0.8) {
                // 20% events sap toi (trong tuong lai)
                start = NOW.plusDays(random.nextInt(60) + 1).withHour(11).withMinute(30).withSecond(21);
            } else {
                // 20% events dang dien ra (hom nay)
                start = NOW.minusHours(1).withMinute(30).withSecond(21);
            }
            e.setStartTime(start);
            e.setEndTime(start.plusHours(4));

            // Tinh status tu thoi gian
            SaleStatus status = e.computeStatus();
            // 5% bi DISABLED ngau nhien
            if (random.nextInt(100) < 5) {
                status = SaleStatus.DISABLED;
            }
            e.setStatus(status);

            list.add(e);
        }
        return list;
    }

    private static List<FlashSaleItem> generateFlashItems(int count, List<Product> products,
                                                           List<FlashSaleEvent> events) {
        List<FlashSaleItem> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            FlashSaleItem fi = new FlashSaleItem();
            fi.setId("FI" + String.format("%05d", i));

            LocalDateTime created = randomPastTime();
            fi.setCreatedAt(created);
            fi.setUpdatedAt(randomAfter(created));

            Product product = products.get(random.nextInt(products.size()));
            FlashSaleEvent event = events.get(random.nextInt(events.size()));

            fi.setEventId(event.getId());
            fi.setProductId(product.getId());

            // Discount 10-70%
            int discount = 10 + random.nextInt(61);
            fi.setDiscountPercent(discount);
            double flashPrice = product.getPrice() * (100 - discount) / 100.0;
            fi.setFlashPrice(Math.round(flashPrice));

            fi.setLimitedQty(10 + random.nextInt(91)); // 10..100
            fi.setSoldQty(0);
            fi.setVersion(0);

            // Status theo event
            fi.setStatus(event.getStatus());

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

        // Map eventId -> FlashSaleEvent de lay thoi gian
        Map<String, FlashSaleEvent> eventMap = new HashMap<>();
        for (FlashSaleEvent ev : events) {
            eventMap.put(ev.getId(), ev);
        }

        // Map flashSaleItemId -> FlashSaleItem de lay flashPrice
        Map<String, FlashSaleItem> itemMap = new HashMap<>();
        for (FlashSaleItem item : flashItems) {
            itemMap.put(item.getId(), item);
        }

        AtomicInteger detailIdCounter = new AtomicInteger(1);
        LockMechanism[] locks = LockMechanism.values();
        int maxAttempts = 100;

        for (int i = 1; i <= orderCount; i++) {
            String orderId = "O" + String.format("%06d", i);
            Customer cust = customers.get(random.nextInt(customers.size()));

            // Chon ngau nhien mot event
            FlashSaleEvent event = events.get(random.nextInt(events.size()));
            // Sinh orderTime (createdAt) trong khoang [start, end] cua event
            LocalDateTime orderTime = randomTimeBetween(event.getStartTime(), event.getEndTime());
            LocalDateTime updateTime = randomAfter(orderTime);

            // Chon ngau nhien lockMechanism
            LockMechanism lock = locks[random.nextInt(locks.length)];

            // Tam tao order (totalAmount se tinh sau)
            Order order = new Order();
            order.setId(orderId);
            order.setCreatedAt(orderTime);
            order.setUpdatedAt(updateTime);
            order.setCustomerId(cust.getId());
            order.setEventId(event.getId());
            order.setStatus(OrderStatus.PENDING);
            order.setLockMechanism(lock);

            double totalAmount = 0;
            int itemsInOrder = 1 + random.nextInt(2); // 1 hoac 2 san pham flash
            boolean hasDetail = false;

            for (int j = 0; j < itemsInOrder; j++) {
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
                            candidate.setUpdatedAt(orderTime);
                            selected = candidate;

                            double unitPrice = candidate.getFlashPrice();
                            double subTotal = qty * unitPrice;
                            totalAmount += subTotal;

                            OrderDetail od = new OrderDetail();
                            od.setId("OD" + String.format("%06d", detailIdCounter.getAndIncrement()));
                            od.setCreatedAt(orderTime);
                            od.setUpdatedAt(updateTime);
                            od.setOrderId(orderId);
                            od.setFlashSaleItemId(selected.getId());
                            od.setQuantity(qty);
                            od.setUnitPrice(unitPrice);
                            od.setSubTotal(subTotal);
                            details.add(od);
                            hasDetail = true;
                        }
                    }
                    attempts++;
                }
            }

            order.setTotalAmount(totalAmount);
            if (hasDetail) {
                order.setStatus(OrderStatus.SUCCESS);
            }
            orders.add(order);
        }
    }

    // ======================== UTILITIES ========================

    /** Sinh thoi gian ngau nhien trong khoang 6-12 thang truoc */
    private static LocalDateTime randomPastTime() {
        long totalSeconds = ChronoUnit.SECONDS.between(EARLIEST, NOW.minusMonths(1));
        long rndSeconds = (long) (random.nextDouble() * totalSeconds);
        return EARLIEST.plusSeconds(rndSeconds);
    }

    /** Sinh thoi gian ngau nhien SAU mot moc cho truoc (trong vong 30 ngay) */
    private static LocalDateTime randomAfter(LocalDateTime after) {
        long maxOffset = 30L * 24 * 60 * 60; // 30 ngay
        long offset = (long) (random.nextDouble() * maxOffset);
        LocalDateTime result = after.plusSeconds(offset);
        // Dam bao khong vuot qua NOW
        return result.isAfter(NOW) ? NOW : result;
    }

    private static LocalDateTime randomTimeBetween(LocalDateTime start, LocalDateTime end) {
        long secondsBetween = ChronoUnit.SECONDS.between(start, end);
        if (secondsBetween <= 0) return start;
        long randomSeconds = (long) (random.nextDouble() * secondsBetween);
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

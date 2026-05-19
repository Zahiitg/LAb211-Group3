/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication24;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * DataGenerator - A synthetic data generator for an e-commerce database.
 * Generates 6 consistent CSV files: 1. categories.csv (20 rows) 2. products.csv
 * (500 rows) 3. users.csv (1000 rows) 4. orders.csv (3000 rows) 5.
 * order_details.csv (~5000 rows, based on realistic order contents) 6.
 * reviews.csv (1500 rows)
 *
 * Fully compliant with JDK 1.8. Ensures referential integrity and
 * mathematically correct totals.
 */
public class datagenerator {
    

    private static final String DATA_DIR = "data"; // Writes directly to the folder where it is run
    private static final Random random = new Random(42); // Seeded for reproducibility
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Locale US_LOCALE = Locale.US;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   Starting E-commerce CSV Data Generation...     ");
        System.out.println("=================================================");

        // Ensure data directory exists
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Created directory: " + dir.getAbsolutePath());
            }
        }

        try {
            // 1. Generate Categories
            System.out.print("Generating categories.csv... ");
            generateCategories();
            System.out.println("Done! (20 rows)");

            // 2. Generate Products
            System.out.print("Generating products.csv... ");
            double[] productPrices = generateProducts(); // Returns array of prices, indexed by product_id
            System.out.println("Done! (500 rows)");

            // 3. Generate Users
            System.out.print("Generating users.csv... ");
            LocalDateTime[] userCreatedAt = generateUsers(); // Returns registration times, indexed by user_id
            System.out.println("Done! (1000 rows)");

            // 4 & 5. Generate Orders & Order Details (Linked for strict data integrity)
            System.out.print("Generating orders.csv & order_details.csv... ");
            generateOrdersAndDetails(userCreatedAt, productPrices);
            System.out.println("Done! (~3000 orders and ~5000 order details)");

            // 6. Generate Reviews
            System.out.print("Generating reviews.csv... ");
            generateReviews();
            System.out.println("Done! (1500 reviews)");

            System.out.println("=================================================");
            System.out.println("   Data Generation Completed Successfully!       ");
            System.out.println("   Files are saved in: " + dir.getCanonicalPath());
            System.out.println("=================================================");

        } catch (IOException e) {
            System.err.println("\nError generating data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 1. Generates categories.csv (Exactly 20 rows)
     */
    private static void generateCategories() throws IOException {
        String[] categoryNames = {
            "Electronics", "Fashion & Apparel", "Home & Kitchen", "Beauty & Personal Care", "Sports & Outdoors",
            "Books & Stationery", "Toys & Hobbies", "Automotive & Industrial", "Health & Wellness", "Groceries & Pet Supplies",
            "Baby Products", "Jewelry & Accessories", "Computers & Accessories", "Phones & Tablets", "Cameras & Audio",
            "Furniture & Decor", "Garden & Outdoor", "Tools & Improvement", "Musical Instruments", "Office Supplies"
        };

        String[] categoryDescriptions = {
            "Smart gadgets, wireless devices, and cutting-edge consumer electronics.",
            "Trendy clothing, outerwear, footwear, and apparel for men, women, and kids.",
            "Essential kitchen appliances, cookware, dining accessories, and home utility tools.",
            "Premium skincare, hair care, cosmetics, personal hygiene, and beauty products.",
            "High-performance sports gear, outdoor camping equipment, and athletic apparel.",
            "Bestselling novels, educational materials, creative notebooks, and artistic items.",
            "Fun games, educational toys, puzzles, building blocks, and hobby kits for all ages.",
            "Automotive replacement parts, car care accessories, and vehicle maintenance tools.",
            "Dietary supplements, protein powders, fitness trackers, and wellness equipment.",
            "Organic grocery essentials, healthy snacks, pet food, and pet care accessories.",
            "Safe, high-quality baby gear, gentle wipes, nursery bedding, and toys for infants.",
            "Elegant necklaces, minimalist watches, designer sunglasses, and premium bags.",
            "Laptops, mechanical keyboards, gaming mice, storage drives, and laptop bags.",
            "Latest smartphones, tablets, durable cases, and fast charging cables.",
            "Professional cameras, vlogging microphones, studio headphones, and tripod stands.",
            "Modern living room furniture, abstract wall decor, and cozy home styling items.",
            "Vibrant solar garden lights, durable pruning shears, and herb starting kits.",
            "Reliable power drills, comprehensive screwdriver kits, and home improvement gear.",
            "Acoustic guitars, electronic keyboards, kalimbas, and musical accessories.",
            "Paper shredders, desktop whiteboards, filing folders, and ergonomic desk mats."
        };

        File file = new File(DATA_DIR, "categories.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("category_id,category_name,description\n");
            for (int i = 0; i < 20; i++) {
                int categoryId = i + 1;
                bw.write(categoryId + ","
                        + escapeCsv(categoryNames[i]) + ","
                        + escapeCsv(categoryDescriptions[i]) + "\n");
            }
        }
    }

    /**
     * 2. Generates products.csv (Exactly 500 rows, 25 products per category)
     * Returns an array of prices mapped by product_id (1-indexed) for integrity
     * check.
     */
    private static double[] generateProducts() throws IOException {
        double[] productPrices = new double[501]; // 1-indexed

        String[] adjectives = {
            "Premium", "Classic", "Modern", "Ultra", "Portable", "Wireless", "Eco-friendly",
            "Ergonomic", "Professional", "Deluxe", "Essential", "Smart", "Compact", "Heavy-Duty"
        };

        // Specific product roots for each category to make the data highly realistic
        String[][] categoryNouns = {
            // 1. Electronics
            {"Wireless Earbuds", "Bluetooth Speaker", "Smart Watch", "Power Bank", "Noise Cancelling Headphones", "Action Camera", "Digital Voice Recorder", "Wireless Charging Pad"},
            // 2. Fashion & Apparel
            {"Men T-Shirt", "Women Dress", "Denim Jacket", "Leather Belt", "Canvas Sneaker", "Running Shoes", "Athletic Socks", "Sun Hat"},
            // 3. Home & Kitchen
            {"Air Fryer", "Blender Smoothies", "Coffee Maker", "Stainless Knife Set", "Non-Stick Frying Pan", "Electric Kettle", "Vacuum Cleaner", "Storage Containers"},
            // 4. Beauty & Personal Care
            {"Face Moisturizer", "Vitamin C Serum", "Matte Lipstick", "Shampoo & Conditioner", "Hair Dryer", "Electric Toothbrush", "Sunscreen SPF 50", "Makeup Brush Set"},
            // 5. Sports & Outdoors
            {"Yoga Mat", "Dumbbell Set", "Water Bottle 1L", "Camping Tent", "Sleeping Bag", "Hiking Backpack", "Resistance Bands", "Bicycle Helmet"},
            // 6. Books & Stationery
            {"Leather Notebook", "Gel Pen Pack", "Planner 2026", "Desk Organizer", "Drawing Sketchbook", "Fountain Pen", "Sticky Notes Set", "Highlighter Set"},
            // 7. Toys & Hobbies
            {"Building Blocks Set", "Remote Control Car", "Board Game", "Jigsaw Puzzle 1000pcs", "Art Painting Kit", "Action Figure", "Plush Toy", "Model Airplane Kit"},
            // 8. Automotive & Industrial
            {"Car Phone Mount", "Dashboard Camera", "Car Trash Can", "Leather Seat Cover", "Microfiber Cleaning Cloths", "Tire Pressure Gauge", "Wrench Tool Set", "Jumper Cables"},
            // 9. Health & Wellness
            {"Multivitamin Gummies", "Whey Protein 1kg", "Fish Oil Softgels", "Massage Gun", "Essential Oil Diffuser", "Digital Scale", "First Aid Kit", "Knee Support Sleeve"},
            // 10. Groceries & Pet Supplies
            {"Organic Green Tea", "Premium Coffee Beans", "Dog Food 5kg", "Cat Scratching Post", "Pet Grooming Glove", "Healthy Granola Bars", "Extra Virgin Olive Oil", "Gourmet Chocolate Box"},
            // 11. Baby Products
            {"Baby Wipes Sensitive", "Silicon Baby Bibs", "Plush Baby Blanket", "Baby Bottles Set", "Teething Toy", "Stroller Organizer", "Baby Sound Machine", "Pacifier Set"},
            // 12. Jewelry & Accessories
            {"Silver Pendant Necklace", "Minimalist Wristwatch", "Leather Wallet", "Polarized Sunglasses", "Stud Earrings Set", "Charm Bracelet", "Travel Jewelry Case", "Silk Scarf"},
            // 13. Computers & Accessories
            {"Mechanical Keyboard", "Wireless Gaming Mouse", "USB-C Hub", "Laptop Stand", "External SSD 1TB", "Webcam 1080p", "Mouse Pad Extended", "Laptop Sleeve"},
            // 14. Phones & Tablets
            {"Phone Case Clear", "Tempered Glass Screen Protector", "Stylus Pen", "Tablet Stand", "Fast Charger Adapter", "Braided USB-C Cable", "Ring Light Stand", "Waterproof Phone Pouch"},
            // 15. Cameras & Audio
            {"Tripod Stand", "Lavalier Microphone", "Ring Light 10-inch", "Camera Backpack", "Lens Cleaning Kit", "Studio Headphones", "Vinyl Record Player", "Mini Projector"},
            // 16. Furniture & Decor
            {"Ergonomic Office Chair", "Floating Wall Shelves", "LED Desk Lamp", "Throw Pillow Covers", "Scented Candle Set", "Ceramic Flower Vase", "Storage Ottoman", "Full Length Mirror"},
            // 17. Garden & Outdoor
            {"Solar Garden Lights", "Pruning Shears", "Watering Can", "Plant Pots Set", "Garden Hose 50ft", "Hammock Portable", "Bird Feeder", "Herb Seed Starter Kit"},
            // 18. Tools & Improvement
            {"Cordless Drill Set", "Magnetic Screwdriver Kit", "Digital Tape Measure", "Safety Glasses", "Laser Level", "Utility Knife Pack", "LED Flashlight", "Heavy Duty Mounting Tape"},
            // 19. Musical Instruments
            {"Acoustic Guitar", "Ukulele Concert", "Electronic Keyboard", "Kalimba 17 Keys", "Guitar Stand", "Clip-on Tuner", "Drum Sticks Pack", "Harmonica C Key"},
            // 20. Office Supplies
            {"Paper Shredder", "Dry Erase Whiteboard", "File Folder Organizer", "Heavy Duty Stapler", "Dual Monitor Mount", "Laminator Machine", "Desk Pad Mat", "Label Maker Printer"}
        };

        // Realistic price range limits for each category to ensure sensible prices
        double[][] priceRanges = {
            {30.0, 300.0}, // Electronics
            {15.0, 120.0}, // Fashion
            {20.0, 250.0}, // Home & Kitchen
            {10.0, 80.0}, // Beauty
            {10.0, 180.0}, // Sports
            {5.0, 45.0}, // Books
            {12.0, 90.0}, // Toys
            {8.0, 150.0}, // Automotive
            {15.0, 95.0}, // Health
            {4.0, 60.0}, // Groceries
            {8.0, 120.0}, // Baby
            {25.0, 350.0}, // Jewelry
            {35.0, 800.0}, // Computers
            {10.0, 400.0}, // Phones & Tablets
            {20.0, 500.0}, // Cameras & Audio
            {40.0, 600.0}, // Furniture
            {7.0, 110.0}, // Garden
            {15.0, 320.0}, // Tools
            {30.0, 750.0}, // Musical Instruments
            {12.0, 200.0} // Office Supplies
        };

        File file = new File(DATA_DIR, "products.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("product_id,product_name,price,stock_quantity,category_id\n");

            int productId = 1;
            for (int catIdx = 0; catIdx < 20; catIdx++) {
                int categoryId = catIdx + 1;
                String[] nouns = categoryNouns[catIdx];
                double minPrice = priceRanges[catIdx][0];
                double maxPrice = priceRanges[catIdx][1];

                Set<String> generatedNames = new HashSet<>();

                // Generate exactly 25 products per category (25 * 20 = 500 products)
                while (generatedNames.size() < 25) {
                    String adjective = adjectives[random.nextInt(adjectives.length)];
                    String noun = nouns[random.nextInt(nouns.length)];
                    String productName = adjective + " " + noun;

                    if (generatedNames.add(productName)) {
                        double price = minPrice + (random.nextDouble() * (maxPrice - minPrice));
                        price = Math.round(price * 100.0) / 100.0; // Round to 2 decimals
                        int stockQuantity = 10 + random.nextInt(491); // 10 to 500 products in stock

                        productPrices[productId] = price;

                        bw.write(productId + ","
                                + escapeCsv(productName) + ","
                                + String.format(US_LOCALE, "%.2f", price) + ","
                                + stockQuantity + ","
                                + categoryId + "\n");
                        productId++;
                    }
                }
            }
        }
        return productPrices;
    }

    /**
     * 3. Generates users.csv (Exactly 1000 rows) Returns an array of
     * registration times mapped by user_id (1-indexed) for order integrity.
     */
    private static LocalDateTime[] generateUsers() throws IOException {
        LocalDateTime[] userCreatedAt = new LocalDateTime[1001]; // 1-indexed

        String[] lastNames = {"Nguyen", "Tran", "Le", "Pham", "Huynh", "Hoang", "Phan", "Vu", "Vo", "Dang", "Bui", "Do", "Ho", "Ngo", "Duong", "Lam"};
        String[] middleNames = {"Van", "Thi", "Minh", "Hoang", "Duc", "Viet", "Duy", "Cam", "Ngoc", "Khanh"};
        String[] firstNames = {"Anh", "Tuan", "Duy", "Hai", "Hung", "Linh", "Trang", "Lan", "Huong", "Phong", "Quan", "Nam", "Son", "Thao", "Mai", "Quynh", "Yen", "Duc", "Kien", "Phuong", "Huy", "Vy", "Binh"};

        String[] phonePrefixes = {"090", "091", "098", "096", "097", "032", "035", "038", "070", "077", "086", "088", "089"};
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com.vn", "fpt.edu.vn", "hotmail.com"};

        // Setting a time window for registration: Jan 1, 2024 to Dec 31, 2025
        LocalDateTime regStart = LocalDateTime.of(2024, 1, 1, 0, 0);
        long totalSecs = ChronoUnit.SECONDS.between(regStart, LocalDateTime.of(2025, 12, 31, 23, 59));

        File file = new File(DATA_DIR, "users.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("user_id,username,email,phone,created_at\n");

            for (int userId = 1; userId <= 1000; userId++) {
                String lastName = lastNames[random.nextInt(lastNames.length)];
                String middleName = middleNames[random.nextInt(middleNames.length)];
                String firstName = firstNames[random.nextInt(firstNames.length)];

                // Generate usernames like: nguyenvana12, n_v_anh99, or tuan.nguyen88 (Vietnamese, no accent)
                int style = random.nextInt(3);
                String username;
                if (style == 0) {
                    username = (lastName + middleName + firstName).toLowerCase() + (10 + random.nextInt(90));
                } else if (style == 1) {
                    username = (lastName + "_" + middleName.substring(0, 1) + "_" + firstName).toLowerCase() + (10 + random.nextInt(90));
                } else {
                    username = (firstName + "." + lastName).toLowerCase() + (100 + random.nextInt(900));
                }

                String email = username + "@" + domains[random.nextInt(domains.length)];

                // Generate realistic Vietnamese phone number starting with prefix
                String prefix = phonePrefixes[random.nextInt(phonePrefixes.length)];
                String phone = prefix + String.format("%07d", random.nextInt(10000000));

                // Generate random registration date/time
                long randomOffsetSecs = (long) (random.nextDouble() * totalSecs);
                LocalDateTime createdAt = regStart.plusSeconds(randomOffsetSecs);
                userCreatedAt[userId] = createdAt;

                bw.write(userId + ","
                        + escapeCsv(username) + ","
                        + escapeCsv(email) + ","
                        + phone + ","
                        + createdAt.format(DATE_FORMATTER) + "\n");
            }
        }
        return userCreatedAt;
    }

    /**
     * 4 & 5. Generates orders.csv (Exactly 3000 rows) and order_details.csv
     * (~5000 rows) together. Generates order_details as children of orders.
     * Calculates total_amount of each order by summing unit_price * quantity of
     * its details, guaranteeing perfect mathematical matching. Order dates are
     * strictly after the buyer's account creation date.
     */
    private static void generateOrdersAndDetails(LocalDateTime[] userCreatedAt, double[] productPrices) throws IOException {
        String[] statuses = {"Completed", "Completed", "Completed", "Completed", "Pending", "Cancelled"}; // 66.6% Completed, 16.7% Pending, 16.7% Cancelled

        // E-commerce current simulation end time: May 19, 2026
        LocalDateTime nowLimit = LocalDateTime.of(2026, 5, 19, 11, 9);

        File ordersFile = new File(DATA_DIR, "orders.csv");
        File detailsFile = new File(DATA_DIR, "order_details.csv");

        try (BufferedWriter ow = new BufferedWriter(new FileWriter(ordersFile)); BufferedWriter dw = new BufferedWriter(new FileWriter(detailsFile))) {

            ow.write("order_id,user_id,order_date,total_amount,status\n");
            dw.write("order_detail_id,order_id,product_id,quantity,unit_price\n");

            int orderDetailId = 1;

            for (int orderId = 1; orderId <= 3000; orderId++) {
                int userId = 1 + random.nextInt(1000);
                LocalDateTime userReg = userCreatedAt[userId];

                // Generate order date between registration and now
                long secondsBetween = ChronoUnit.SECONDS.between(userReg, nowLimit);
                if (secondsBetween <= 0) {
                    secondsBetween = 3600; // Fallback to 1 hour after if edge-case negative
                }
                long randomOffsetSecs = (long) (random.nextDouble() * secondsBetween);
                LocalDateTime orderDate = userReg.plusSeconds(randomOffsetSecs);

                String status = statuses[random.nextInt(statuses.length)];

                // Determine number of items for this order:
                // Weighted selection to average ~1.67 items per order, making ~5000 order details.
                // 55% -> 1 item, 30% -> 2 items, 10% -> 3 items, 5% -> 4 items.
                double r = random.nextDouble();
                int numItems;
                if (r < 0.55) {
                    numItems = 1;
                } else if (r < 0.85) {
                    numItems = 2;
                } else if (r < 0.95) {
                    numItems = 3;
                } else {
                    numItems = 4;
                }

                double totalAmount = 0.0;
                Set<Integer> uniqueProducts = new HashSet<>();

                for (int item = 0; item < numItems; item++) {
                    int productId;
                    // Ensure unique products in the same order
                    do {
                        productId = 1 + random.nextInt(500);
                    } while (!uniqueProducts.add(productId));

                    // Random quantity (70% -> 1, 20% -> 2, 10% -> 3)
                    double qRand = random.nextDouble();
                    int quantity = 1;
                    if (qRand >= 0.70 && qRand < 0.90) {
                        quantity = 2;
                    } else if (qRand >= 0.90) {
                        quantity = 3;
                    }

                    double unitPrice = productPrices[productId];
                    double itemTotal = unitPrice * quantity;
                    totalAmount += itemTotal;

                    // Write order detail row
                    dw.write(orderDetailId + ","
                            + orderId + ","
                            + productId + ","
                            + quantity + ","
                            + String.format(US_LOCALE, "%.2f", unitPrice) + "\n");
                    orderDetailId++;
                }

                totalAmount = Math.round(totalAmount * 100.0) / 100.0;

                // Write order row
                ow.write(orderId + ","
                        + userId + ","
                        + orderDate.format(DATE_FORMATTER) + ","
                        + String.format(US_LOCALE, "%.2f", totalAmount) + ","
                        + status + "\n");
            }
        }
    }

    /**
     * 6. Generates reviews.csv (Exactly 1500 rows) Reviews are linked to random
     * products and users. Comments are realistic and align with ratings.
     */
    private static void generateReviews() throws IOException {
        String[] comments5 = {
            "Excellent product! Highly recommended.",
            "Very fast shipping and great quality.",
            "Perfect, exactly as described.",
            "Awesome, will buy again.",
            "Exceeded my expectations, works flawlessly.",
            "Great value for money, absolutely love it!",
            "Super fast delivery and superb product.",
            "The quality is amazing for the price.",
            "Very satisfied with this purchase.",
            "Top notch quality and excellent customer service."
        };
        String[] comments4 = {
            "Good quality, minor issues with packaging.",
            "Nice product, works well.",
            "Satisfied with the purchase, is as described.",
            "Value for money, good product.",
            "Pretty decent, does what it is supposed to.",
            "Item arrived on time, works fine.",
            "Quite happy with it, though shipping took a bit longer.",
            "Overall good, would recommend.",
            "Well made product, very useful.",
            "Very good, minor scratches on the outer box but item is intact."
        };
        String[] comments3 = {
            "Average product, matches the price.",
            "Decent but could be better.",
            "It is okay, shipping took too long.",
            "Mediocre quality, but acceptable.",
            "Works fine, but material feels a bit cheap.",
            "Not bad, but not great either. Just average.",
            "Just normal, nothing special.",
            "It does the job, but I expected more features.",
            "Fair quality, could be improved.",
            "Satisfactory, but wouldn't buy it again."
        };
        String[] comments2 = {
            "Disappointed, not as expected.",
            "Poor quality, would not recommend.",
            "Stopped working after a few days.",
            "Bad customer service, item had defects.",
            "Product feels very cheap and flimsy.",
            "Does not work as described in the manual.",
            "Disappointed with the quality, very flimsy.",
            "Not worth the price, unfortunately.",
            "Arrived late and didn't meet expectations.",
            "Defective item, had to return it."
        };
        String[] comments1 = {
            "Terrible! Do not buy.",
            "Total waste of money.",
            "Received damaged product, very upset.",
            "Horrible experience, completely broken.",
            "Absolute garbage, doesn't work at all.",
            "Worst purchase I have ever made.",
            "Item was missing parts and broken.",
            "SCAM! DO NOT BUY!",
            "Extremely poor quality, broke immediately.",
            "Very unhappy, zero stars if I could."
        };

        File file = new File(DATA_DIR, "reviews.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("review_id,product_id,user_id,rating,comment\n");

            for (int reviewId = 1; reviewId <= 1500; reviewId++) {
                int productId = 1 + random.nextInt(500);
                int userId = 1 + random.nextInt(1000);

                // Realistic distribution of ratings (slanted towards positive):
                // 55% -> 5, 25% -> 4, 10% -> 3, 5% -> 2, 5% -> 1
                double r = random.nextDouble();
                int rating;
                String comment;

                if (r < 0.55) {
                    rating = 5;
                    comment = comments5[random.nextInt(comments5.length)];
                } else if (r < 0.80) {
                    rating = 4;
                    comment = comments4[random.nextInt(comments4.length)];
                } else if (r < 0.90) {
                    rating = 3;
                    comment = comments3[random.nextInt(comments3.length)];
                } else if (r < 0.95) {
                    rating = 2;
                    comment = comments2[random.nextInt(comments2.length)];
                } else {
                    rating = 1;
                    comment = comments1[random.nextInt(comments1.length)];
                }

                bw.write(reviewId + ","
                        + productId + ","
                        + userId + ","
                        + rating + ","
                        + escapeCsv(comment) + "\n");
            }
        }
    }

    /**
     * Safely formats and escapes values for standard CSV format. Wraps values
     * containing commas, quotes, or newlines in double quotes, and escapes
     * double quotes by doubling them.
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

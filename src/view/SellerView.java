package view;

import controller.AuthenticationState;
import controller.ControllerResult;
import controller.SellerController;
import model.Order;
import model.Product;
import model.enums.OrderStatus;

import java.util.List;
import java.util.Map;

public class SellerView {

    private final SellerController sellerController;

    public SellerView() {
        this.sellerController = new SellerController();
    }

    public void start() {
        boolean running = true;
        while (running) {
            ConsoleUI.printHeader("SELLER DASHBOARD");
            System.out.println("1. Quan ly san pham cua toi");
            System.out.println("2. Quan ly don hang");
            System.out.println("3. Cap nhat ho so");
            System.out.println("4. Thong ke");
            System.out.println("5. Quan ly Flash Sale");
            System.out.println("0. Dang xuat");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    manageProducts();
                    break;
                case 2:
                    manageOrders();
                    break;
                case 3:
                    updateProfile();
                    break;
                case 4:
                    viewStats();
                    break;
                case 5:
                    manageFlashSale();
                    break;
                case 0:
                    logout();
                    running = false;
                    break;
            }
            if (choice != 0) ConsoleUI.pause();
        }
    }

    private void manageProducts() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY SAN PHAM");
            System.out.println("1. Xem danh sach san pham");
            System.out.println("2. Them san pham moi");
            System.out.println("3. Sua san pham");
            System.out.println("4. Xoa san pham");
            System.out.println("5. Tim kiem san pham");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    displayMyProducts();
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    updateProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 5:
                    searchMyProducts();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void displayMyProducts() {
        ControllerResult res = sellerController.getMyProducts();
        if (res.isSuccess()) {
            @SuppressWarnings("unchecked") List<Product> list = (List<Product>) res.getData();
            System.out.printf("%-10s | %-20s | %-15s | %-10s | %-10s\n", "ID", "Name", "Category", "Price", "Stock");
            System.out.println("-------------------------------------------------------------------------");
            for (Product p : list) {
                System.out.printf("%-10s | %-20s | %-15s | %-10.0f | %-10d\n", p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStock());
            }
            ConsoleUI.printSuccess(res.getMessage());
        } else {
            ConsoleUI.printError(res.getMessage());
        }
    }

    private void addProduct() {
        String name = ConsoleUI.getString("Nhap ten san pham: ");
        
        String cat = "";
        ControllerResult catRes = sellerController.listCategories();
        if (catRes.isSuccess()) {
            @SuppressWarnings("unchecked") List<model.Category> cats = (List<model.Category>) catRes.getData();
            System.out.println("Chon danh muc tu danh sach sau:");
            for (int i = 0; i < cats.size(); i++) {
                System.out.println((i + 1) + ". " + cats.get(i).getName());
            }
            int catChoice = ConsoleUI.getInt("Chon (1-" + cats.size() + "): ", 1, cats.size());
            cat = cats.get(catChoice - 1).getName();
        } else {
            cat = ConsoleUI.getString("Nhap danh muc: ");
        }
        
        double price = 0;
        try { price = Double.parseDouble(ConsoleUI.getString("Nhap gia: ")); } catch(Exception e) {}
        int stock = ConsoleUI.getInt("Nhap ton kho: ", 0, 1000000);

        Product p = new Product();
        p.setName(name);
        p.setCategory(cat);
        p.setPrice(price);
        p.setStock(stock);

        handleResult(sellerController.addProduct(p));
    }

    private void updateProduct() {
        String id = ConsoleUI.getString("Nhap ID san pham can sua: ");
        String name = ConsoleUI.getString("Nhap ten moi: ");
        
        String cat = "";
        ControllerResult catRes = sellerController.listCategories();
        if (catRes.isSuccess()) {
            @SuppressWarnings("unchecked") List<model.Category> cats = (List<model.Category>) catRes.getData();
            System.out.println("Chon danh muc moi tu danh sach sau:");
            for (int i = 0; i < cats.size(); i++) {
                System.out.println((i + 1) + ". " + cats.get(i).getName());
            }
            int catChoice = ConsoleUI.getInt("Chon (1-" + cats.size() + "): ", 1, cats.size());
            cat = cats.get(catChoice - 1).getName();
        } else {
            cat = ConsoleUI.getString("Nhap danh muc moi: ");
        }
        
        double price = 0;
        try { price = Double.parseDouble(ConsoleUI.getString("Nhap gia moi: ")); } catch(Exception e) {}
        int stock = ConsoleUI.getInt("Nhap ton kho moi: ", 0, 1000000);

        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setCategory(cat);
        p.setPrice(price);
        p.setStock(stock);

        handleResult(sellerController.updateProduct(p));
    }

    private void deleteProduct() {
        String id = ConsoleUI.getString("Nhap ID san pham can xoa: ");
        handleResult(sellerController.deleteProduct(id));
    }

    private void manageOrders() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY DON HANG");
            System.out.println("1. Xem danh sach don hang");
            System.out.println("2. Duyet don (PENDING -> VERIFIED)");
            System.out.println("3. Hoan thanh don (VERIFIED -> COMPLETED)");
            System.out.println("4. Loc don hang theo trang thai");
            System.out.println("5. Huy don hang (PENDING/VERIFIED -> CANCELLED)");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    displayMyOrders();
                    break;
                case 2:
                    String id1 = ConsoleUI.getString("Nhap ID don can duyet: ");
                    handleResult(sellerController.verifyOrder(id1));
                    break;
                case 3:
                    String id2 = ConsoleUI.getString("Nhap ID don can hoan thanh: ");
                    handleResult(sellerController.completeOrder(id2));
                    break;
                case 4:
                    filterMyOrders();
                    break;
                case 5:
                    cancelOrder();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void displayMyOrders() {
        ControllerResult res = sellerController.getMyOrders();
        if (res.isSuccess()) {
            @SuppressWarnings("unchecked") List<Order> list = (List<Order>) res.getData();
            System.out.printf("%-10s | %-12s | %-20s | %-15s\n", "ID", "Customer ID", "Order Time", "Status");
            System.out.println("------------------------------------------------------------------");
            for (Order o : list) {
                System.out.printf("%-10s | %-12s | %-20s | %-15s\n", o.getId(), o.getCustomerId(), o.getOrderTime(), o.getStatus());
            }
            ConsoleUI.printSuccess(res.getMessage());
        } else {
            ConsoleUI.printError(res.getMessage());
        }
    }

    private void updateProfile() {
        System.out.println("=== CAP NHAT HO SO ===");
        System.out.println("(Nhan Enter de bo qua neu khong muon doi)");
        String name = ConsoleUI.getString("Nhap ten moi: ");
        String storeName = ConsoleUI.getString("Nhap ten cua hang moi: ");
        String password = ConsoleUI.getString("Nhap mat khau moi: ");
        
        handleResult(sellerController.updateProfile(name, storeName, password));
    }

    private void viewStats() {
        ControllerResult res = sellerController.getDashboardStats();
        if (res.isSuccess()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) res.getData();
            ConsoleUI.printHeader("THONG KE KINH DOANH");
            System.out.printf("Tong doanh thu: %.0f VND\n", (Double) stats.get("totalRevenue"));
            System.out.println("\n--- Tinh hinh don hang ---");
            System.out.println("PENDING: " + stats.get("pending"));
            System.out.println("VERIFIED: " + stats.get("verified"));
            System.out.println("COMPLETED: " + stats.get("completed"));
            System.out.println("CANCELLED: " + stats.get("cancelled"));
            
            System.out.println("\n--- Top 3 san pham ban chay nhat ---");
            @SuppressWarnings("unchecked")
            List<Map.Entry<String, Integer>> topProducts = (List<Map.Entry<String, Integer>>) stats.get("topProducts");
            if (topProducts.isEmpty()) {
                System.out.println("(Chua co san pham nao ban duoc)");
            } else {
                for (Map.Entry<String, Integer> entry : topProducts) {
                    System.out.println("- " + entry.getKey() + " (Da ban: " + entry.getValue() + ")");
                }
            }
            ConsoleUI.printSuccess(res.getMessage());
        } else {
            ConsoleUI.printError(res.getMessage());
        }
    }

    private void searchMyProducts() {
        String kw = ConsoleUI.getString("Nhap tu khoa tim kiem (hoac Enter de xem tat ca): ");
        ControllerResult res = sellerController.searchMyProducts(kw);
        if (res.isSuccess()) {
            @SuppressWarnings("unchecked") List<Product> list = (List<Product>) res.getData();
            System.out.printf("%-10s | %-20s | %-15s | %-10s | %-10s\n", "ID", "Name", "Category", "Price", "Stock");
            System.out.println("-------------------------------------------------------------------------");
            for (Product p : list) {
                System.out.printf("%-10s | %-20s | %-15s | %-10.0f | %-10d\n", p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStock());
            }
            ConsoleUI.printSuccess(res.getMessage());
        } else {
            ConsoleUI.printError(res.getMessage());
        }
    }

    private void filterMyOrders() {
        System.out.println("Chon trang thai can loc:");
        System.out.println("1. PENDING");
        System.out.println("2. VERIFIED");
        System.out.println("3. COMPLETED");
        System.out.println("4. CANCELLED");
        int choice = ConsoleUI.getInt("Chon (1-4): ", 1, 4);
        OrderStatus status = null;
        switch (choice) {
            case 1: status = OrderStatus.PENDING; break;
            case 2: status = OrderStatus.VERIFIED; break;
            case 3: status = OrderStatus.COMPLETED; break;
            case 4: status = OrderStatus.CANCELLED; break;
        }
        
        ControllerResult res = sellerController.filterMyOrders(status);
        if (res.isSuccess()) {
            @SuppressWarnings("unchecked") List<Order> list = (List<Order>) res.getData();
            System.out.printf("%-10s | %-12s | %-20s | %-15s\n", "ID", "Customer ID", "Order Time", "Status");
            System.out.println("------------------------------------------------------------------");
            for (Order o : list) {
                System.out.printf("%-10s | %-12s | %-20s | %-15s\n", o.getId(), o.getCustomerId(), o.getOrderTime(), o.getStatus());
            }
            ConsoleUI.printSuccess(res.getMessage());
        } else {
            ConsoleUI.printError(res.getMessage());
        }
    }

    private void cancelOrder() {
        String id = ConsoleUI.getString("Nhap ID don can huy: ");
        handleResult(sellerController.cancelOrder(id));
    }

    // =====================================================================
    // PHASE 1: QUAN LY FLASH SALE
    // =====================================================================
    private void manageFlashSale() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY FLASH SALE");
            System.out.println("1. Xem cac su kien dang dien ra");
            System.out.println("2. Dang ky san pham vao Flash Sale");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");
            int choice = ConsoleUI.getInt("Chon (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    displayOngoingEvents();
                    break;
                case 2:
                    registerFlashSaleItem();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void displayOngoingEvents() {
        ControllerResult res = sellerController.getOngoingFlashSaleEvents();
        if (!res.isSuccess()) {
            ConsoleUI.printError(res.getMessage());
            return;
        }
        @SuppressWarnings("unchecked")
        List<model.FlashSaleEvent> events = (List<model.FlashSaleEvent>) res.getData();
        if (events.isEmpty()) {
            System.out.println("Khong co su kien nao dang dien ra.");
            return;
        }
        System.out.printf("%-10s | %-25s | %-20s\n", "Event ID", "Name", "End Time");
        System.out.println("------------------------------------------------------------------");
        for (model.FlashSaleEvent ev : events) {
            System.out.printf("%-10s | %-25s | %-20s\n", ev.getId(), ev.getName(), ev.getEndTime());
        }
    }

    private void registerFlashSaleItem() {
        displayOngoingEvents();
        String eventId = ConsoleUI.getString("Nhap ID su kien muon tham gia: ");
        
        ControllerResult resProd = sellerController.getMyProducts();
        if (resProd.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Product> products = (List<Product>) resProd.getData();
            System.out.printf("\n%-10s | %-25s | %-12s | %-10s\n", "ID", "Name", "Price", "Stock");
            System.out.println("------------------------------------------------------------------");
            for (Product p : products) {
                System.out.printf("%-10s | %-25s | %-12.2f | %-10d\n", p.getId(), p.getName(), p.getPrice(), p.getStock());
            }
        }
        
        String productId = ConsoleUI.getString("Nhap ID san pham cua ban: ");
        double salePrice = ConsoleUI.getDouble("Nhap gia Sale: ", 0, Double.MAX_VALUE);
        int qty = ConsoleUI.getInt("Nhap so luong gioi han (limited qty): ", 1, 1000000);
        
        handleResult(sellerController.registerFlashSaleItem(eventId, productId, salePrice, qty));
    }

    private void logout() {
        handleResult(AuthenticationState.getInstance().logout());
    }

    private void handleResult(ControllerResult result) {
        if (result.isSuccess()) ConsoleUI.printSuccess(result.getMessage());
        else ConsoleUI.printError(result.getMessage());
    }
}

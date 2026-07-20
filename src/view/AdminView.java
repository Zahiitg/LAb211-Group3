package view;

import controller.AdminController;
import controller.AuthenticationState;
import controller.ControllerResult;
import model.Customer;
import model.Seller;
import model.Order;
import model.OrderTransaction;
import model.FlashSaleEvent;
import model.Product;
import model.enums.AccountStatus;
import model.enums.OrderStatus;
import java.util.Map;

import java.util.List;

/**
 * Giao dien danh rieng cho Admin.
 * Duoc goi ra sau khi AuthenticationState xac nhan nguoi dang nhap la Admin.
 *
 * @author Thanh vien 1 - View Architecture
 */
public class AdminView {

    private final AdminController adminController;

    public AdminView() {
        this.adminController = new AdminController();
    }

    /**
     * Vong lap Menu chinh cua Admin.
     */
    public void start() {
        boolean running = true;
        while (running) {
            ConsoleUI.printHeader("ADMIN DASHBOARD");
            System.out.println("1. Xem danh sach Customer");
            System.out.println("2. Xem danh sach Seller");
            System.out.println("3. Khoa (Ban) tai khoan Customer");
            System.out.println("4. Mo khoa (Unban) tai khoan Customer");
            System.out.println("5. Khoa (Ban) tai khoan Seller");
            System.out.println("6. Mo khoa (Unban) tai khoan Seller");
            System.out.println("7. Duyet (Approve) tai khoan Customer");
            System.out.println("8. Duyet (Approve) tai khoan Seller");
            System.out.println("9. Quan ly don hang");
            System.out.println("10. Thong ke tong quan");
            System.out.println("11. Tim kiem tai khoan");
            System.out.println("12. Quan ly Flash Sale");
            System.out.println("13. Quan ly danh muc");
            System.out.println("14. Quan ly san pham");
            System.out.println("15. Xem log giao dich");
            System.out.println("16. Cap nhat ho so");
            System.out.println("17. Chay Simulator (Stress Test)");
            System.out.println("0. Dang xuat");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-17): ", 0, 17);

            switch (choice) {
                case 1:
                    listCustomers();
                    break;
                case 2:
                    listSellers();
                    break;
                case 3:
                    banCustomer();
                    break;
                case 4:
                    unbanCustomer();
                    break;
                case 5:
                    banSeller();
                    break;
                case 6:
                    unbanSeller();
                    break;
                case 7:
                    approveCustomer();
                    break;
                case 8:
                    approveSeller();
                    break;
                case 9:
                    manageOrders();
                    break;
                case 10:
                    showDashboard();
                    break;
                case 11:
                    searchAccounts();
                    break;
                case 12:
                    manageFlashSale();
                    break;
                case 13:
                    manageCategories();
                    break;
                case 14:
                    manageProducts();
                    break;
                case 15:
                    viewTransactionLogs();
                    break;
                case 16:
                    updateProfile();
                    break;
                case 17:
                    runSimulator();
                    break;
                case 0:
                    logout();
                    running = false; // Thoat khoi AdminView, quay lai MainMenu
                    break;
            }

            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    // =====================================================================
    // XEM DANH SACH
    // =====================================================================

    private void listCustomers() {
        ControllerResult result = adminController.listAllCustomers();
        if (result.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Customer> list = (List<Customer>) result.getData();
            
            ConsoleUI.printHeader("DANH SACH CUSTOMER");
            System.out.printf("%-10s | %-20s | %-25s | %-10s | %-10s\n",
                    "ID", "Name", "Email", "Status", "Tier");
            System.out.println("-------------------------------------------------------------------------------------");
            
            for (Customer c : list) {
                String statusColor = getStatusColor(c.getStatus());
                System.out.printf("%-10s | %-20s | %-25s | %s%-10s%s | %-10s\n",
                        c.getId(), c.getName(), c.getEmail(),
                        statusColor, c.getStatus(), ConsoleUI.RESET,
                        c.getTier());
            }
            System.out.println("-------------------------------------------------------------------------------------");
            ConsoleUI.printSuccess(result.getMessage());
        } else {
            ConsoleUI.printError(result.getMessage());
        }
    }

    private void listSellers() {
        ControllerResult result = adminController.listAllSellers();
        if (result.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<Seller> list = (List<Seller>) result.getData();
            
            ConsoleUI.printHeader("DANH SACH SELLER");
            System.out.printf("%-10s | %-20s | %-25s | %-10s | %-15s\n",
                    "ID", "Name", "Email", "Status", "Store");
            System.out.println("-----------------------------------------------------------------------------------------");
            
            for (Seller s : list) {
                String statusColor = getStatusColor(s.getStatus());
                System.out.printf("%-10s | %-20s | %-25s | %s%-10s%s | %-15s\n",
                        s.getId(), s.getName(), s.getEmail(),
                        statusColor, s.getStatus(), ConsoleUI.RESET,
                        s.getStoreName());
            }
            System.out.println("-----------------------------------------------------------------------------------------");
            ConsoleUI.printSuccess(result.getMessage());
        } else {
            ConsoleUI.printError(result.getMessage());
        }
    }

    // =====================================================================
    // KHOA / DUYET TAI KHOAN
    // =====================================================================

    private void banCustomer() {
        ConsoleUI.printHeader("KHOA TAI KHOAN CUSTOMER");
        String id = ConsoleUI.getString("Nhap ID Customer can khoa: ");
        ControllerResult result = adminController.banCustomer(id);
        handleResult(result);
    }

    private void banSeller() {
        ConsoleUI.printHeader("KHOA TAI KHOAN SELLER");
        String id = ConsoleUI.getString("Nhap ID Seller can khoa: ");
        ControllerResult result = adminController.banSeller(id);
        handleResult(result);
    }

    private void approveCustomer() {
        ConsoleUI.printHeader("DUYET TAI KHOAN CUSTOMER");
        String id = ConsoleUI.getString("Nhap ID Customer can duyet: ");
        ControllerResult result = adminController.approveCustomer(id);
        handleResult(result);
    }

    private void approveSeller() {
        ConsoleUI.printHeader("DUYET TAI KHOAN SELLER");
        String id = ConsoleUI.getString("Nhap ID Seller can duyet: ");
        ControllerResult result = adminController.approveSeller(id);
        handleResult(result);
    }

    private void unbanCustomer() {
        ConsoleUI.printHeader("MO KHOA TAI KHOAN CUSTOMER");
        String id = ConsoleUI.getString("Nhap ID Customer can mo khoa: ");
        ControllerResult result = adminController.unbanCustomer(id);
        handleResult(result);
    }

    private void unbanSeller() {
        ConsoleUI.printHeader("MO KHOA TAI KHOAN SELLER");
        String id = ConsoleUI.getString("Nhap ID Seller can mo khoa: ");
        ControllerResult result = adminController.unbanSeller(id);
        handleResult(result);
    }

    // =====================================================================
    // 1. QUAN LY DON HANG
    // =====================================================================

    private void manageOrders() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY DON HANG");
            System.out.println("1. Xem tat ca don hang");
            System.out.println("2. Loc don hang theo trang thai");
            System.out.println("3. Huy don hang & hoan kho (Can thiep quyen Admin)");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-3): ", 0, 3);
            switch (choice) {
                case 1:
                    displayOrders(adminController.listAllOrders());
                    break;
                case 2:
                    System.out.println("Chon trang thai: 1-PENDING, 2-VERIFIED, 3-COMPLETED, 4-CANCELLED, 5-FAILED");
                    int sChoice = ConsoleUI.getInt("Chon (1-5): ", 1, 5);
                    OrderStatus status = OrderStatus.PENDING;
                    if (sChoice == 2) status = OrderStatus.VERIFIED;
                    else if (sChoice == 3) status = OrderStatus.COMPLETED;
                    else if (sChoice == 4) status = OrderStatus.CANCELLED;
                    else if (sChoice == 5) status = OrderStatus.FAILED;
                    displayOrders(adminController.listOrdersByStatus(status));
                    break;
                case 3:
                    handleResult(adminController.cancelOrder(ConsoleUI.getString("Nhap ID don hang can huy: ")));
                    break;
                case 0:
                    return;
            }
            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    private void displayOrders(ControllerResult result) {
        if (!result.isSuccess()) {
            ConsoleUI.printError(result.getMessage());
            return;
        }
        @SuppressWarnings("unchecked")
        List<Order> orders = (List<Order>) result.getData();
        if (orders.isEmpty()) {
            System.out.println("Khong co don hang nao.");
            return;
        }
        System.out.printf("%-10s | %-12s | %-20s | %-15s\n", "ID", "Customer ID", "Order Time", "Status");
        System.out.println("------------------------------------------------------------------");
        for (Order o : orders) {
            System.out.printf("%-10s | %-12s | %-20s | %-15s\n", o.getId(), o.getCustomerId(), o.getOrderTime(), o.getStatus());
            ControllerResult detailsRes = adminController.getDetailsByOrderId(o.getId());
            if (detailsRes.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<model.OrderDetail> details = (List<model.OrderDetail>) detailsRes.getData();
                for (model.OrderDetail d : details) {
                    System.out.printf("   + Item ID: %-10s | So luong: %-3d | Don gia: %-10.2f\n",
                            d.getFlashSaleItemId(), d.getQuantity(), d.getPriceAtPurchase());
                }
            }
            System.out.println("------------------------------------------------------------------");
        }
    }

    // =====================================================================
    // 2. THONG KE DASHBOARD
    // =====================================================================

    private void showDashboard() {
        ControllerResult result = adminController.getDashboardStats();
        if (!result.isSuccess()) {
            ConsoleUI.printError(result.getMessage());
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.getData();

        ConsoleUI.printHeader("THONG KE TONG QUAN HETHONG");
        System.out.println("1. Khach hang : " + stats.get("totalCustomers") + " (Approved: " + stats.get("approvedCustomers") + ", Banned: " + stats.get("bannedCustomers") + ")");
        System.out.println("2. Nguoi ban  : " + stats.get("totalSellers"));
        System.out.println("3. San pham   : " + stats.get("totalProducts"));
        System.out.println("4. Don hang   : " + stats.get("totalOrders") + " (Pending: " + stats.get("pendingOrders") + ", Verified: " + stats.get("verifiedOrders") + ", Completed: " + stats.get("completedOrders") + ", Cancelled: " + stats.get("cancelledOrders") + ")");
        System.out.printf( "5. Doanh thu  : %.2f VND\n", (Double) stats.get("totalRevenue"));
        System.out.println("6. Flash Sale : " + stats.get("totalEvents") + " (Ongoing: " + stats.get("ongoingEvents") + ")");
        System.out.println("----------------------------------------");
    }

    // =====================================================================
    // 3. TIM KIEM TAI KHOAN
    // =====================================================================

    private void searchAccounts() {
        ConsoleUI.printHeader("TIM KIEM TAI KHOAN");
        String keyword = ConsoleUI.getString("Nhap ten hoac email can tim: ");
        
        System.out.println(">> Ket qua tren Customer:");
        ControllerResult cRes = adminController.searchCustomers(keyword);
        if (cRes.isSuccess()) {
            @SuppressWarnings("unchecked") List<Customer> cList = (List<Customer>) cRes.getData();
            for (Customer c : cList) System.out.println(" - [" + c.getId() + "] " + c.getName() + " | " + c.getEmail());
        }
        
        System.out.println(">> Ket qua tren Seller:");
        ControllerResult sRes = adminController.searchSellers(keyword);
        if (sRes.isSuccess()) {
            @SuppressWarnings("unchecked") List<Seller> sList = (List<Seller>) sRes.getData();
            for (Seller s : sList) System.out.println(" - [" + s.getId() + "] " + s.getName() + " | " + s.getEmail());
        }
    }

    // =====================================================================
    // 4. QUAN LY FLASH SALE
    // =====================================================================

    private void manageFlashSale() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY FLASH SALE");
            System.out.println("1. Xem tat ca su kien");
            System.out.println("2. Tao su kien Flash Sale moi");
            System.out.println("3. Kich hoat su kien");
            System.out.println("4. Ket thuc su kien");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-4): ", 0, 4);
            switch (choice) {
                case 1:
                    ControllerResult r = adminController.listFlashSaleEvents();
                    if (r.isSuccess()) {
                        @SuppressWarnings("unchecked") List<FlashSaleEvent> evs = (List<FlashSaleEvent>) r.getData();
                        for (FlashSaleEvent e : evs) System.out.println(" - [" + e.getId() + "] " + e.getName() + " | " + e.getStatus());
                    }
                    break;
                case 2:
                    String name = ConsoleUI.getString("Nhap ten su kien: ");
                    int days = ConsoleUI.getInt("Nhap so ngay dien ra: ", 1, 365);
                    handleResult(adminController.createFlashSaleEvent(name, days));
                    break;
                case 3:
                    handleResult(adminController.startFlashSaleEvent(ConsoleUI.getString("Nhap ID su kien can kich hoat: ")));
                    break;
                case 4:
                    handleResult(adminController.endFlashSaleEvent(ConsoleUI.getString("Nhap ID su kien can ket thuc: ")));
                    break;
                case 0:
                    return;
            }
            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    // =====================================================================
    // PHASE 3: QUAN LY DANH MUC
    // =====================================================================

    private void manageCategories() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY DANH MUC");
            System.out.println("1. Xem tat ca danh muc");
            System.out.println("2. Them danh muc moi");
            System.out.println("3. Xoa danh muc");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-3): ", 0, 3);
            switch (choice) {
                case 1:
                    ControllerResult r = adminController.listAllCategories();
                    if (r.isSuccess()) {
                        @SuppressWarnings("unchecked") List<model.Category> cats = (List<model.Category>) r.getData();
                        System.out.printf("%-10s | %-30s\n", "ID", "Name");
                        for (model.Category c : cats) System.out.printf("%-10s | %-30s\n", c.getId(), c.getName());
                    } else {
                        ConsoleUI.printError(r.getMessage());
                    }
                    break;
                case 2:
                    handleResult(adminController.addCategory(ConsoleUI.getString("Nhap ten danh muc moi: ")));
                    break;
                case 3:
                    handleResult(adminController.deleteCategory(ConsoleUI.getString("Nhap ID danh muc can xoa: ")));
                    break;
                case 0:
                    return;
            }
            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    // =====================================================================
    // 5. QUAN LY SAN PHAM
    // =====================================================================

    private void manageProducts() {
        while (true) {
            ConsoleUI.printHeader("QUAN LY SAN PHAM");
            System.out.println("1. Xem tat ca san pham");
            System.out.println("2. Xoa san pham");
            System.out.println("0. Quay lai");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    ControllerResult r = adminController.listAllProducts();
                    if (r.isSuccess()) {
                        @SuppressWarnings("unchecked") List<Product> ps = (List<Product>) r.getData();
                        System.out.printf("%-10s | %-25s | %-10s | %-10s\n", "ID", "Name", "Category", "Stock");
                        for (Product p : ps) System.out.printf("%-10s | %-25s | %-10s | %-10d\n", p.getId(), p.getName(), p.getCategory(), p.getStock());
                    }
                    break;
                case 2:
                    handleResult(adminController.deleteProduct(ConsoleUI.getString("Nhap ID san pham can xoa: ")));
                    break;
                case 0:
                    return;
            }
            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    // =====================================================================
    // 6. XEM LOG GIAO DICH
    // =====================================================================

    private void viewTransactionLogs() {
        ConsoleUI.printHeader("LOG GIAO DICH");
        System.out.println("1. Tat ca giao dich");
        System.out.println("2. Giao dich that bai");
        int choice = ConsoleUI.getInt("Chon (1-2): ", 1, 2);
        
        ControllerResult res = (choice == 1) ? adminController.listAllTransactions() : adminController.listFailedTransactions();
        if (res.isSuccess()) {
            @SuppressWarnings("unchecked") List<OrderTransaction> txs = (List<OrderTransaction>) res.getData();
            System.out.printf("%-10s | %-10s | %-15s | %-10s | %-10s\n", "TX ID", "Order ID", "Lock Mech", "Time(ms)", "Success");
            System.out.println("------------------------------------------------------------------");
            for (OrderTransaction t : txs) {
                System.out.printf("%-10s | %-10s | %-15s | %-10d | %-10s\n", t.getId(), t.getOrderId(), t.getLockMechanism(), t.getProcessingTimeMs(), t.isSuccess());
            }
        }
    }

    // =====================================================================
    // UTILITIES
    // =====================================================================

    private void updateProfile() {
        System.out.println("=== CAP NHAT HO SO ===");
        System.out.println("(Nhan Enter de bo qua neu khong muon doi)");
        String name = ConsoleUI.getString("Nhap ten moi: ");
        String password = ConsoleUI.getString("Nhap mat khau moi: ");
        handleResult(adminController.updateProfile(name, password));
    }

    private void runSimulator() {
        SimulatorView simulatorView = new SimulatorView();
        simulatorView.showSimulatorMenu();
    }

    private void logout() {
        ControllerResult result = AuthenticationState.getInstance().logout();
        handleResult(result);
    }

    private void handleResult(ControllerResult result) {
        if (result.isSuccess()) {
            ConsoleUI.printSuccess(result.getMessage());
        } else {
            ConsoleUI.printError(result.getMessage());
        }
    }

    private String getStatusColor(AccountStatus status) {
        if (status == AccountStatus.APPROVED) return ConsoleUI.GREEN;
        if (status == AccountStatus.PENDING) return ConsoleUI.YELLOW;
        if (status == AccountStatus.BANNED) return ConsoleUI.RED;
        return ConsoleUI.RESET;
    }
}

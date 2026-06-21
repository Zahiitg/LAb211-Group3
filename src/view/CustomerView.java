package view;

import controller.AuthenticationState;
import controller.ControllerResult;
import controller.FlashSaleController;
import controller.OrderController;
import controller.ProductController;
import model.Customer;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.Product;
import model.Order;
import model.OrderDetail;
import model.enums.LockMechanism;

import java.util.List;

/**
 * Giao dien danh rieng cho Customer.
 * Duoc goi sau khi Customer dang nhap thanh cong.
 *
 * @author Thanh vien 2 & 1
 */
public class CustomerView {

    private final FlashSaleController flashSaleController;
    private final OrderController orderController;
    private final ProductController productController;
    private final controller.CustomerController customerController;
    private final AuthenticationState authState;

    public CustomerView() {
        this.flashSaleController = new FlashSaleController();
        this.orderController = new OrderController();
        this.productController = new ProductController();
        this.customerController = new controller.CustomerController();
        this.authState = AuthenticationState.getInstance();
    }

    /**
     * Vong lap Menu chinh cua Customer.
     */
    public void start() {
        boolean running = true;
        while (running) {
            ConsoleUI.printHeader("CUSTOMER DASHBOARD");
            System.out.println("1. Xem thong tin ca nhan");
            System.out.println("2. Tim kiem san pham");
            System.out.println("3. Xem san pham theo danh muc");
            System.out.println("4. Xem danh sach va Dat mua Flash Sale");
            System.out.println("5. Xem lich su don hang");
            System.out.println("0. Dang xuat");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-5): ", 0, 5);

            switch (choice) {
                case 1:
                    showProfile();
                    break;
                case 2:
                    searchProductsFlow();
                    break;
                case 3:
                    browseByCategoryFlow();
                    break;
                case 4:
                    browseAndBuyFlashSale();
                    break;
                case 5:
                    showOrderHistory();
                    break;
                case 0:
                    logout();
                    running = false;
                    break;
            }

            if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    private void showProfile() {
        while (true) {
            ConsoleUI.printHeader("THONG TIN CA NHAN");
            if (authState.isCustomer()) {
                Customer c = (Customer) authState.getCurrentUser();
                System.out.println("Ma Khach Hang : " + c.getId());
                System.out.println("Ho va Ten     : " + c.getName());
                System.out.println("Email         : " + c.getEmail());
                System.out.println("Hang tai khoan: " + c.getTier());
                System.out.println("Trang thai    : " + c.getStatus());
                
                String addr = c.getAddress();
                if (addr == null || addr.isEmpty()) {
                    System.out.println("Dia chi GH    : Chua cap nhat");
                } else {
                    System.out.println("Dia chi GH    : " + addr);
                }
                
                System.out.println("----------------------------------------");
                System.out.println("1. Cap nhat dia chi giao hang");
                System.out.println("0. Quay lai menu chinh");
                
                int choice = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
                if (choice == 1) {
                    String newAddr = ConsoleUI.getString("Nhap dia chi moi (de trong de huy): ");
                    if (!newAddr.isEmpty()) {
                        ControllerResult res = customerController.updateProfile(c, newAddr);
                        if (res.isSuccess()) {
                            ConsoleUI.printSuccess(res.getMessage());
                        } else {
                            ConsoleUI.printError(res.getMessage());
                        }
                    }
                } else {
                    break;
                }
            } else {
                ConsoleUI.printError("Khong tim thay thong tin dang nhap cua Customer!");
                break;
            }
        }
    }

    private void listFlashSaleEvents() {
        ControllerResult result = flashSaleController.getAllEvents();
        if (result.isSuccess()) {
            @SuppressWarnings("unchecked")
            List<FlashSaleEvent> list = (List<FlashSaleEvent>) result.getData();
            
            ConsoleUI.printHeader("DANH SACH SU KIEN FLASH SALE");
            System.out.printf("%-10s | %-25s | %-20s | %-20s | %-10s\n",
                    "ID", "Ten Su Kien", "Bat Dau", "Ket Thuc", "Trang Thai");
            System.out.println("--------------------------------------------------------------------------------------------------");
            
            for (FlashSaleEvent e : list) {
                System.out.printf("%-10s | %-25s | %-20s | %-20s | %-10s\n",
                        e.getId(), e.getName(), e.getStartTime(), e.getEndTime(), e.getStatus());
            }
            System.out.println("--------------------------------------------------------------------------------------------------");
        } else {
            ConsoleUI.printError(result.getMessage());
        }
    }

    private void browseAndBuyFlashSale() {
        // Buoc 1: Hien danh sach su kien de nguoi dung chon
        ControllerResult eventsResult = flashSaleController.getAllEvents();
        if (!eventsResult.isSuccess()) {
            ConsoleUI.printError(eventsResult.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        List<FlashSaleEvent> events = (List<FlashSaleEvent>) eventsResult.getData();
        if (events.isEmpty()) {
            ConsoleUI.printError("Hien tai chua co su kien Flash Sale nao!");
            return;
        }

        ConsoleUI.printHeader("DANH SACH SU KIEN FLASH SALE");
        System.out.printf("%-5s | %-10s | %-25s | %-12s\n", "STT", "ID", "Ten Su Kien", "Trang Thai");
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < events.size(); i++) {
            FlashSaleEvent e = events.get(i);
            System.out.printf("%-5d | %-10s | %-25s | %-12s\n",
                    i + 1, e.getId(), e.getName(), e.getStatus());
        }
        System.out.println("------------------------------------------------------------------");

        // Buoc 2: Nguoi dung chon su kien
        int eventChoice = ConsoleUI.getInt("Chon so thu tu su kien (0 de quay lai): ", 0, events.size());
        if (eventChoice == 0) return;

        FlashSaleEvent chosenEvent = events.get(eventChoice - 1);

        // Buoc 3: Hien danh sach san pham trong su kien
        ControllerResult itemsResult = flashSaleController.getItemsByEventId(chosenEvent.getId());
        if (!itemsResult.isSuccess()) {
            ConsoleUI.printError(itemsResult.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        List<FlashSaleItem> items = (List<FlashSaleItem>) itemsResult.getData();
        if (items.isEmpty()) {
            ConsoleUI.printError("Su kien '" + chosenEvent.getName() + "' hien khong co san pham nao!");
            return;
        }

        ConsoleUI.printHeader("SAN PHAM TRONG SU KIEN: " + chosenEvent.getName());
        System.out.printf("%-5s | %-10s | %-25s | %-12s | %-8s\n",
                "STT", "Item ID", "Ten San Pham", "Gia Sale", "Con Lai");
        System.out.println("----------------------------------------------------------------------");
        for (int i = 0; i < items.size(); i++) {
            FlashSaleItem item = items.get(i);
            Product p = flashSaleController.getProductById(item.getProductId());
            String pName = (p != null) ? p.getName() : "Unknown Product";
            int stockLeft = item.getLimitedQty() - item.getSoldQty();
            System.out.printf("%-5d | %-10s | %-25s | %-12.2f | %-8d\n",
                    i + 1, item.getId(), pName, item.getSalePrice(), stockLeft);
        }
        System.out.println("----------------------------------------------------------------------");

        // Buoc 4: Nguoi dung chon san pham
        int itemChoice = ConsoleUI.getInt("Chon so thu tu san pham muon mua (0 de quay lai): ", 0, items.size());
        if (itemChoice == 0) return;

        FlashSaleItem chosenItem = items.get(itemChoice - 1);
        Product chosenProduct = flashSaleController.getProductById(chosenItem.getProductId());

        // Hien thi chi tiet san pham duoc chon
        ConsoleUI.printHeader("CHI TIET SAN PHAM");
        System.out.println("Ten SP     : " + (chosenProduct != null ? chosenProduct.getName() : "Unknown"));
        System.out.println("Item ID    : " + chosenItem.getId());
        System.out.printf("Gia Flash  : %.2f VND%n", chosenItem.getSalePrice());
        System.out.println("Con lai    : " + (chosenItem.getLimitedQty() - chosenItem.getSoldQty()));
        System.out.println("----------------------------------------");
        System.out.println("1. Mua san pham nay");
        System.out.println("0. Quay lai danh sach");

        int action = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
        if (action == 0) return;

        // Buoc 5: Nhap so luong va co che khoa
        int qty = ConsoleUI.getInt("Nhap so luong muon mua: ", 1, 1000);

        System.out.println("Chon Co che Khoa:");
        System.out.println("1. NO_LOCK       - Khong khoa (co the gay am kho)");
        System.out.println("2. SYNCHRONIZED  - Khoa dong bo trong 1 JVM (an toan, nhanh)");
        System.out.println("3. FILE_LOCK     - Khoa cap OS (an toan da tien trinh, cham)");
        System.out.println("4. OPTIMISTIC_LOCK - Khoa lac quan, thu lai khi xung dot");
        System.out.println("0. Huy dat hang");
        int lockChoice = ConsoleUI.getInt("Chon co che (0-4): ", 0, 4);
        if (lockChoice == 0) {
            System.out.println("Da huy dat hang.");
            return;
        }

        LockMechanism mechanism;
        switch (lockChoice) {
            case 1: mechanism = LockMechanism.NO_LOCK; break;
            case 2: mechanism = LockMechanism.SYNCHRONIZED; break;
            case 3: mechanism = LockMechanism.FILE_LOCK; break;
            case 4: mechanism = LockMechanism.OPTIMISTIC_LOCK; break;
            default: mechanism = LockMechanism.NO_LOCK;
        }

        Customer c = (Customer) authState.getCurrentUser();
        
        // Buoc 6: Xac nhan don hang
        String pName = (chosenProduct != null) ? chosenProduct.getName() : "Unknown Product";
        boolean confirm = showOrderConfirmation(c, pName, qty, chosenItem.getSalePrice());
        if (!confirm) {
            System.out.println("Da huy dat hang.");
            return;
        }

        ControllerResult orderResult;
        switch (mechanism) {
            case NO_LOCK:
                orderResult = orderController.placeOrderSingleThread(c.getId(), chosenItem.getId(), qty);
                break;
            case SYNCHRONIZED:
                orderResult = orderController.placeOrderWithSynchronized(c.getId(), chosenItem.getId(), qty);
                break;
            case FILE_LOCK:
                orderResult = orderController.placeOrderWithFileLock(c.getId(), chosenItem.getId(), qty);
                break;
            case OPTIMISTIC_LOCK:
                orderResult = orderController.placeOrderWithOptimisticLock(c.getId(), chosenItem.getId(), qty);
                break;
            default:
                orderResult = ControllerResult.error("Co che khoa khong hop le!");
        }

        if (orderResult.isSuccess()) {
            ConsoleUI.printSuccess(orderResult.getMessage());
            showPendingAlert();
        } else {
            ConsoleUI.printError(orderResult.getMessage());
        }
    }

    /**
     * Luong tim kiem san pham theo tu khoa.
     * Sau khi hien danh sach, cho phep chon san pham de xem chi tiet & mua hoac quay lai.
     */
    private void searchProductsFlow() {
        ConsoleUI.printHeader("TIM KIEM SAN PHAM");
        String keyword = ConsoleUI.getString("Nhap tu khoa tim kiem (de trong de quay lai): ");
        if (keyword.isEmpty()) return;

        ControllerResult result = productController.searchProducts(keyword);
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) result.getData();

        if (products == null || products.isEmpty()) {
            ConsoleUI.printError("Khong tim thay san pham nao voi tu khoa: '" + keyword + "'");
            return;
        }

        // Hien danh sach va vong lap chon san pham
        selectAndViewProduct(products, "KET QUA TIM KIEM: '" + keyword + "'");
    }

    /**
     * Luong xem san pham theo danh muc.
     * Hien danh sach danh muc → chon → hien san pham → chon de xem chi tiet & mua hoac quay lai.
     */
    private void browseByCategoryFlow() {
        ControllerResult catResult = productController.getAllCategories();
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) catResult.getData();

        if (categories == null || categories.isEmpty()) {
            ConsoleUI.printError("Hien khong co danh muc nao trong he thong!");
            return;
        }

        ConsoleUI.printHeader("DANH SACH DANH MUC");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%-4d. %s%n", i + 1, categories.get(i));
        }
        System.out.println("----------------------------------------");

        int catChoice = ConsoleUI.getInt("Chon danh muc (0 de quay lai): ", 0, categories.size());
        if (catChoice == 0) return;

        String selectedCategory = categories.get(catChoice - 1);
        ControllerResult result = productController.getProductsByCategory(selectedCategory);
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) result.getData();

        if (products == null || products.isEmpty()) {
            ConsoleUI.printError("Danh muc '" + selectedCategory + "' hien khong co san pham nao!");
            return;
        }

        // Hien danh sach va vong lap chon san pham
        selectAndViewProduct(products, "SAN PHAM TRONG DANH MUC: " + selectedCategory.toUpperCase());
    }

    /**
     * Hien thi danh sach san pham theo dang bang.
     */
    private void displayProductList(List<Product> products) {
        System.out.printf("%-5s | %-10s | %-30s | %-12s | %-8s%n",
                "STT", "Ma SP", "Ten San Pham", "Gia", "Ton Kho");
        System.out.println("--------------------------------------------------------------------------");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            System.out.printf("%-5d | %-10s | %-30s | %-12.2f | %-8d%n",
                    i + 1, p.getId(), p.getName(), p.getPrice(), p.getStock());
        }
        System.out.println("--------------------------------------------------------------------------");
    }

    /**
     * Hien thi danh sach san pham, cho nguoi dung chon, xem chi tiet va mua.
     * Sau khi quay lai tu chi tiet san pham, danh sach duoc hien lai de chon tiep.
     * @param products Danh sach san pham hien thi
     * @param title    Tieu de hien thi tren danh sach
     */
    private void selectAndViewProduct(List<Product> products, String title) {
        while (true) {
            ConsoleUI.printHeader(title);
            displayProductList(products);
            int choice = ConsoleUI.getInt("Chon so thu tu san pham de xem chi tiet (0 de quay lai menu): ", 0, products.size());
            if (choice == 0) return;

            Product selected = products.get(choice - 1);
            showProductDetailAndBuy(selected);
        }
    }

    /**
     * Hien thi chi tiet san pham va cho nguoi dung chon mua hoac quay lai.
     */
    private void showProductDetailAndBuy(Product product) {
        ConsoleUI.printHeader("CHI TIET SAN PHAM");
        System.out.println("Ma SP      : " + product.getId());
        System.out.println("Ten SP     : " + product.getName());
        System.out.println("Danh muc   : " + product.getCategory());
        System.out.printf( "Gia        : %.2f VND%n", product.getPrice());
        System.out.println("Ton kho    : " + product.getStock());
        System.out.println("----------------------------------------");
        System.out.println("1. Mua san pham nay");
        System.out.println("0. Quay lai");

        int action = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
        if (action == 0) return;

        if (product.getStock() <= 0) {
            ConsoleUI.printError("San pham nay hien da het hang!");
            return;
        }

        int qty = ConsoleUI.getInt("Nhap so luong muon mua: ", 1, product.getStock());

        System.out.println("Chon Co che Khoa:");
        System.out.println("1. NO_LOCK       - Khong khoa (co the gay am kho)");
        System.out.println("2. SYNCHRONIZED  - Khoa dong bo trong 1 JVM (an toan, nhanh)");
        System.out.println("3. FILE_LOCK     - Khoa cap OS (an toan da tien trinh, cham)");
        System.out.println("4. OPTIMISTIC_LOCK - Khoa lac quan, thu lai khi xung dot");
        System.out.println("0. Huy dat hang");
        int lockChoice = ConsoleUI.getInt("Chon co che (0-4): ", 0, 4);
        if (lockChoice == 0) {
            System.out.println("Da huy dat hang.");
            return;
        }

        LockMechanism mechanism;
        switch (lockChoice) {
            case 1: mechanism = LockMechanism.NO_LOCK; break;
            case 2: mechanism = LockMechanism.SYNCHRONIZED; break;
            case 3: mechanism = LockMechanism.FILE_LOCK; break;
            case 4: mechanism = LockMechanism.OPTIMISTIC_LOCK; break;
            default: mechanism = LockMechanism.NO_LOCK;
        }

        Customer c = (Customer) authState.getCurrentUser();
        
        // Buoc 6: Xac nhan don hang
        boolean confirm = showOrderConfirmation(c, product.getName(), qty, product.getPrice());
        if (!confirm) {
            System.out.println("Da huy dat hang.");
            return;
        }

        ControllerResult orderResult = orderController.placeProductOrder(
                c.getId(), product.getId(), qty, mechanism);

        if (orderResult.isSuccess()) {
            ConsoleUI.printSuccess(orderResult.getMessage());
            showPendingAlert();
        } else {
            ConsoleUI.printError(orderResult.getMessage());
        }
    }

    private void showOrderHistory() {
        ConsoleUI.printHeader("LICH SU MUA HANG");
        Customer c = (Customer) authState.getCurrentUser();
        ControllerResult result = orderController.getOrdersByCustomerId(c.getId());

        if (!result.isSuccess()) {
            ConsoleUI.printError(result.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        List<Order> orders = (List<Order>) result.getData();
        if (orders.isEmpty()) {
            System.out.println("Ban chua co don hang nao trong he thong.");
            return;
        }

        System.out.printf("%-10s | %-20s | %-12s\n", "Ma Don Hang", "Thoi Gian Dat", "Trang Thai");
        System.out.println("------------------------------------------------------------------");
        for (Order o : orders) {
            System.out.printf("%-10s | %-20s | %-12s\n", o.getId(), o.getOrderTime(), o.getStatus());
            
            // Hien thi chi tiet tung don hang
            ControllerResult detailsRes = orderController.getDetailsByOrderId(o.getId());
            if (detailsRes.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<OrderDetail> details = (List<OrderDetail>) detailsRes.getData();
                for (OrderDetail d : details) {
                    System.out.printf("   + Item ID: %-10s | So luong: %-3d | Don gia: %-10.2f\n",
                            d.getFlashSaleItemId(), d.getQuantity(), d.getPriceAtPurchase());
                }
            }
            System.out.println("------------------------------------------------------------------");
        }
    }

    private void logout() {
        ControllerResult result = authState.logout();
        if (result.isSuccess()) {
            ConsoleUI.printSuccess(result.getMessage());
        } else {
            ConsoleUI.printError(result.getMessage());
        }
    }

    // =====================================================================
    // UI HELPERS
    // =====================================================================

    /**
     * Hien thi man hinh xac nhan don hang truoc khi thuc su tao don.
     */
    private boolean showOrderConfirmation(Customer c, String productName, int qty, double price) {
        ConsoleUI.printHeader("XAC NHAN DON HANG");
        System.out.println("Khach Hang : " + c.getName() + " (Ma: " + c.getId() + ")");
        
        String addr = c.getAddress();
        System.out.println("Dia chi GH : " + (addr != null && !addr.isEmpty() ? addr : "Chua cap nhat (Van tiep tuc dat)"));
        System.out.println("----------------------------------------");
        System.out.println("San pham   : " + productName);
        System.out.printf("Don gia    : %.2f VND\n", price);
        System.out.println("So luong   : " + qty);
        System.out.printf("Tong cong  : %.2f VND\n", (price * qty));
        System.out.println("----------------------------------------");
        System.out.println("1. Xac nhan dat hang");
        System.out.println("0. Huy");
        
        int choice = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
        return choice == 1;
    }

    /**
     * Hien thi thong bao don hang o trang thai PENDING sau khi dat thanh cong.
     */
    private void showPendingAlert() {
        System.out.println("\n" + ConsoleUI.YELLOW + "========================================");
        System.out.println("  TRANG THAI DON HANG");
        System.out.println("========================================" + ConsoleUI.RESET);
        System.out.println("Trang thai  : PENDING");
        System.out.println("[!] Don hang cua ban dang cho nguoi ban xac nhan.");
        System.out.println("    Vui long kiem tra lai sau tai muc");
        System.out.println("    \"Xem lich su don hang\".");
        System.out.println(ConsoleUI.YELLOW + "========================================" + ConsoleUI.RESET);
    }
}

package view;

import controller.AuthenticationState;
import controller.ControllerResult;
import controller.FlashSaleController;
import controller.ProductController;
import model.FlashSaleEvent;
import model.FlashSaleItem;
import model.Product;
import model.enums.SaleStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Giao dien danh rieng cho Khach (Guest).
 * Khach duoc phep duyet san pham va xem Flash Sale,
 * nhung KHONG duoc mua hang, xem gio hang, xem lich su, xem thong tin ca nhan.
 *
 * @author Group 3
 */
public class GuestView {

    private final FlashSaleController flashSaleController;
    private final ProductController productController;
    private final AuthenticationState authState;

    public GuestView() {
        this.flashSaleController = new FlashSaleController();
        this.productController = new ProductController();
        this.authState = AuthenticationState.getInstance();
    }

    /**
     * Vong lap Menu chinh cua Guest.
     */
    public void start() {
        boolean running = true;
        while (running) {
            // Kiem tra neu Guest da bi dang xuat (vi promptRegister) thi thoat luon
            if (!authState.isGuest()) {
                break;
            }

            ConsoleUI.printHeader("GUEST DASHBOARD - Dang xem voi tu cach Khach");
            System.out.println("1. Tim kiem san pham");
            System.out.println("2. Xem san pham theo danh muc");
            System.out.println("3. Xem danh sach Flash Sale");
            System.out.println("0. Thoat / Quay lai man hinh chinh");
            System.out.println("----------------------------------------");
            System.out.println("[!] De mua hang, vui long Dang ky tai khoan.");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-3): ", 0, 3);

            switch (choice) {
                case 1:
                    searchProductsFlow();
                    break;
                case 2:
                    browseByCategoryFlow();
                    break;
                case 3:
                    browseFlashSaleFlow();
                    break;
                case 0:
                    logoutGuest();
                    running = false;
                    break;
            }

            // Neu sau khi thuc hien chuc nang, Guest da dang xuat (qua promptRegister) thi
            // thoat
            if (!authState.isGuest()) {
                running = false;
            } else if (choice != 0) {
                ConsoleUI.pause();
            }
        }
    }

    // =====================================================================
    // TIM KIEM SAN PHAM
    // =====================================================================

    private void searchProductsFlow() {
        ConsoleUI.printHeader("TIM KIEM SAN PHAM");
        String keyword = ConsoleUI.getString("Nhap tu khoa tim kiem (de trong de quay lai): ");
        if (keyword.isEmpty())
            return;

        ControllerResult result = productController.searchProducts(keyword);
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) result.getData();

        if (products == null || products.isEmpty()) {
            ConsoleUI.printError("Khong tim thay san pham nao voi tu khoa: '" + keyword + "'");
            return;
        }

        guestSelectAndViewProduct(products, "KET QUA TIM KIEM: '" + keyword + "'");
    }

    // =====================================================================
    // XEM SAN PHAM THEO DANH MUC
    // =====================================================================

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
        if (catChoice == 0)
            return;

        String selectedCategory = categories.get(catChoice - 1);
        ControllerResult result = productController.getProductsByCategory(selectedCategory);
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) result.getData();

        if (products == null || products.isEmpty()) {
            ConsoleUI.printError("Danh muc '" + selectedCategory + "' hien khong co san pham nao!");
            return;
        }

        guestSelectAndViewProduct(products, "SAN PHAM TRONG DANH MUC: " + selectedCategory.toUpperCase());
    }

    // =====================================================================
    // XEM FLASH SALE
    // =====================================================================

    private void browseFlashSaleFlow() {
        ControllerResult eventsResult = flashSaleController.getAllEvents();
        if (!eventsResult.isSuccess()) {
            ConsoleUI.printError(eventsResult.getMessage());
            return;
        }

        @SuppressWarnings("unchecked")
        List<FlashSaleEvent> allEvents = (List<FlashSaleEvent>) eventsResult.getData();
        List<FlashSaleEvent> events = allEvents.stream()
                .filter(e -> e.getStatus() == SaleStatus.ONGOING)
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            ConsoleUI.printError("Hien tai chua co su kien Flash Sale nao dang dien ra!");
            return;
        }

        ConsoleUI.printHeader("DANH SACH SU KIEN FLASH SALE");
        System.out.printf("%-5s | %-10s | %-25s | %-12s%n", "STT", "ID", "Ten Su Kien", "Trang Thai");
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < events.size(); i++) {
            FlashSaleEvent e = events.get(i);
            System.out.printf("%-5d | %-10s | %-25s | %-12s%n",
                    i + 1, e.getId(), e.getName(), e.getStatus());
        }
        System.out.println("------------------------------------------------------------------");

        int eventChoice = ConsoleUI.getInt("Chon so thu tu su kien (0 de quay lai): ", 0, events.size());
        if (eventChoice == 0)
            return;

        FlashSaleEvent chosenEvent = events.get(eventChoice - 1);

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
        System.out.printf("%-5s | %-10s | %-25s | %-10s | %-6s | %-10s | %-8s%n",
                "STT", "Item ID", "Ten San Pham", "Gia Goc", "Giam", "Gia Sale", "Con Lai");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (int i = 0; i < items.size(); i++) {
            FlashSaleItem item = items.get(i);
            Product p = flashSaleController.getProductById(item.getProductId());
            String pName = (p != null) ? p.getName() : "Unknown";
            int stockLeft = item.getLimitedQty() - item.getSoldQty();
            double origPrice = (p != null) ? p.getPrice() : 0.0;
            double salePrice = item.getSalePrice();
            long pctOff = (origPrice > 0) ? Math.round(((origPrice - salePrice) / origPrice) * 100) : 0;

            System.out.printf("%-5d | %-10s | %-25s | %-10.0f | %-6s | %-10.0f | %-8d%n",
                    i + 1, item.getId(), pName, origPrice, "-" + pctOff + "%", salePrice, stockLeft);
        }
        System.out.println("-----------------------------------------------------------------------------------------");

        int itemChoice = ConsoleUI.getInt("Chon san pham de xem chi tiet (0 de quay lai): ", 0, items.size());
        if (itemChoice == 0)
            return;

        FlashSaleItem chosenItem = items.get(itemChoice - 1);
        Product chosenProduct = flashSaleController.getProductById(chosenItem.getProductId());

        double origPrice = chosenProduct != null ? chosenProduct.getPrice() : 0.0;
        double salePrice = chosenItem.getSalePrice();
        long pctOff = (origPrice > 0) ? Math.round(((origPrice - salePrice) / origPrice) * 100) : 0;

        ConsoleUI.printHeader("CHI TIET SAN PHAM");
        System.out.println("Ten SP     : " + (chosenProduct != null ? chosenProduct.getName() : "Unknown"));
        System.out.println("Item ID    : " + chosenItem.getId());
        System.out.printf("Gia Goc    : %.0f VND%n", origPrice);
        System.out.printf("Giam       : -%d%%%n", pctOff);
        System.out.printf("Gia Flash  : %.0f VND%n", salePrice);
        System.out.println("Con lai    : " + (chosenItem.getLimitedQty() - chosenItem.getSoldQty()));
        System.out.println("----------------------------------------");
        System.out.println("1. Them vao gio hang (can dang nhap)");
        System.out.println("0. Quay lai");

        int action = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
        if (action == 1) {
            promptRegister();
        }
    }

    // =====================================================================
    // HELPER: hien thi danh sach san pham va vao chi tiet
    // =====================================================================

    private void guestSelectAndViewProduct(List<Product> products, String title) {
        while (true) {
            ConsoleUI.printHeader(title);
            displayProductList(products);
            int choice = ConsoleUI.getInt("Chon so thu tu san pham de xem chi tiet (0 de quay lai): ", 0,
                    products.size());
            if (choice == 0)
                return;

            Product selected = products.get(choice - 1);
            guestShowProductDetail(selected);
        }
    }

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

    private void guestShowProductDetail(Product product) {
        ConsoleUI.printHeader("CHI TIET SAN PHAM");
        System.out.println("Ma SP      : " + product.getId());
        System.out.println("Ten SP     : " + product.getName());
        System.out.println("Danh muc   : " + product.getCategory());
        System.out.printf("Gia        : %.2f VND%n", product.getPrice());
        System.out.println("Ton kho    : " + product.getStock());
        System.out.println("----------------------------------------");
        System.out.println("1. Them vao gio hang");
        System.out.println("2. Mua ngay");
        System.out.println("0. Quay lai");

        int action = ConsoleUI.getInt("Chon (0-2): ", 0, 2);
        if (action == 1 || action == 2) {
            promptRegister();
        }
    }

    // =====================================================================
    // THONG BAO YEU CAU DANG KY
    // =====================================================================

    /**
     * Hien thi thong bao yeu cau dang ky khi Guest co gang thuc hien hanh dong bi
     * han che.
     * Hoi xem co muon quay ve man hinh chinh de dang ky khong.
     * Neu nguoi dung chon quay ve, se dang xuat Guest va ket thuc GuestView.
     */
    private void promptRegister() {
        System.out.println();
        System.out.println(ConsoleUI.YELLOW + "========================================" + ConsoleUI.RESET);
        System.out.println("  YEU CAU DANG KY TAI KHOAN");
        System.out.println(ConsoleUI.YELLOW + "========================================" + ConsoleUI.RESET);
        System.out.println("Vui long Dang ky hoac Dang nhap de tiep tuc mua hang.");
        System.out.println("----------------------------------------");
        System.out.println("1. Quay ve man hinh chinh de Dang ky / Dang nhap");
        System.out.println("0. Tiep tuc xem voi tu cach Khach");

        int choice = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
        if (choice == 1) {
            // Dang xuat Guest, GuestView.start() se nhan dieu nay va thoat
            authState.logout();
        }
    }

    // =====================================================================
    // DANG XUAT GUEST
    // =====================================================================

    private void logoutGuest() {
        authState.logout();
        System.out.println("Da thoat khoi che do Khach. Quay ve man hinh chinh.");
    }
}

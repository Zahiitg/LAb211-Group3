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
import model.enums.SaleStatus;
import java.util.stream.Collectors;

import java.util.ArrayList;
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
            System.out.println("6. Gio hang cua toi");
            System.out.println("0. Dang xuat");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-6): ", 0, 6);

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
                case 6:
                    manageCart();
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
                System.out.println("1. Cap nhat ho so (Ten, Mat khau, Dia chi)");
                System.out.println("0. Quay lai menu chinh");
                
                int choice = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
                if (choice == 1) {
                    String newName = ConsoleUI.getString("Nhap Ten hien thi moi (Enter de bo qua): ");
                    String newPass = ConsoleUI.getString("Nhap Mat khau moi (Enter de bo qua): ");
                    String newAddr = ConsoleUI.getString("Nhap Dia chi GH moi (Enter de bo qua): ");
                    
                    ControllerResult res = customerController.updateProfile(newName, newPass, newAddr);
                    if (res.isSuccess()) {
                        ConsoleUI.printSuccess(res.getMessage());
                    } else {
                        ConsoleUI.printError(res.getMessage());
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
        List<FlashSaleEvent> allEvents = (List<FlashSaleEvent>) eventsResult.getData();
        List<FlashSaleEvent> events = allEvents.stream()
                .filter(e -> e.getStatus() == SaleStatus.ONGOING)
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            ConsoleUI.printError("Hien tai chua co su kien Flash Sale nao dang dien ra!");
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
        System.out.printf("%-5s | %-10s | %-25s | %-10s | %-6s | %-10s | %-8s\n",
                "STT", "Item ID", "Ten San Pham", "Gia Goc", "Giam", "Gia Sale", "Con Lai");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (int i = 0; i < items.size(); i++) {
            FlashSaleItem item = items.get(i);
            Product p = flashSaleController.getProductById(item.getProductId());
            String pName = (p != null) ? p.getName() : "Unknown Product";
            int stockLeft = item.getLimitedQty() - item.getSoldQty();
            
            double originalPrice = (p != null) ? p.getPrice() : 0.0;
            double salePrice = item.getSalePrice();
            long percentOff = (originalPrice > 0) ? Math.round(((originalPrice - salePrice) / originalPrice) * 100) : 0;
            String discountStr = "-" + percentOff + "%";
            
            System.out.printf("%-5d | %-10s | %-25s | %-10.0f | %-6s | %-10.0f | %-8d\n",
                    i + 1, item.getId(), pName, originalPrice, discountStr, salePrice, stockLeft);
        }
        System.out.println("-----------------------------------------------------------------------------------------");

        // Buoc 4: Nguoi dung chon san pham
        int itemChoice = ConsoleUI.getInt("Chon so thu tu san pham muon mua (0 de quay lai): ", 0, items.size());
        if (itemChoice == 0) return;

        FlashSaleItem chosenItem = items.get(itemChoice - 1);
        Product chosenProduct = flashSaleController.getProductById(chosenItem.getProductId());

        double originalPrice = chosenProduct != null ? chosenProduct.getPrice() : 0.0;
        double salePrice = chosenItem.getSalePrice();
        long percentOff = (originalPrice > 0) ? Math.round(((originalPrice - salePrice) / originalPrice) * 100) : 0;

        // Hien thi chi tiet san pham duoc chon
        ConsoleUI.printHeader("CHI TIET SAN PHAM");
        System.out.println("Ten SP     : " + (chosenProduct != null ? chosenProduct.getName() : "Unknown"));
        System.out.println("Item ID    : " + chosenItem.getId());
        System.out.printf("Gia Goc    : %.0f VND\n", originalPrice);
        System.out.printf("Giam       : -%d%%\n", percentOff);
        System.out.printf("Gia Flash  : %.0f VND\n", salePrice);
        System.out.println("Con lai    : " + (chosenItem.getLimitedQty() - chosenItem.getSoldQty()));
        System.out.println("----------------------------------------");
        System.out.println("1. Them vao gio hang");
        System.out.println("2. Mua truc tiep");
        System.out.println("0. Quay lai danh sach");

        int action = ConsoleUI.getInt("Chon (0-2): ", 0, 2);
        if (action == 0) return;

        // Buoc 5: Nhap so luong
        int stockLeft = chosenItem.getLimitedQty() - chosenItem.getSoldQty();
        int qty = ConsoleUI.getInt("Nhap so luong muon mua: ", 1, Math.max(1, stockLeft));

        if (action == 1) {
            ControllerResult cartResult = customerController.addToCart(chosenItem.getId(), qty);
            if (cartResult.isSuccess()) ConsoleUI.printSuccess(cartResult.getMessage());
            else ConsoleUI.printError(cartResult.getMessage());
        } else {
            // Mua truc tiep — hien xac nhan truoc
            Customer c = (Customer) authState.getCurrentUser();
            String spName = (chosenProduct != null ? chosenProduct.getName() : chosenItem.getId()) + " [Flash Sale]";
            List<String[]> confirmItems = new ArrayList<>();
            confirmItems.add(new String[]{spName, chosenItem.getId(), String.valueOf(qty), String.valueOf(salePrice)});
            if (showOrderConfirmation(c, confirmItems)) {
                ControllerResult res = customerController.directPurchase(chosenItem.getId(), qty);
                if (res.isSuccess()) { ConsoleUI.printSuccess(res.getMessage()); showPendingAlert(); }
                else ConsoleUI.printError(res.getMessage());
            } else {
                ConsoleUI.printError("Da huy mua hang.");
            }
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
        System.out.println("1. Them vao gio hang");
        System.out.println("2. Mua truc tiep");
        System.out.println("0. Quay lai");

        int action = ConsoleUI.getInt("Chon (0-2): ", 0, 2);
        if (action == 0) return;

        if (product.getStock() <= 0) {
            ConsoleUI.printError("San pham nay hien da het hang!");
            return;
        }

        int qty = ConsoleUI.getInt("Nhap so luong muon mua: ", 1, product.getStock());

        if (action == 1) {
            ControllerResult cartResult = customerController.addToCart(product.getId(), qty);
            if (cartResult.isSuccess()) ConsoleUI.printSuccess(cartResult.getMessage());
            else ConsoleUI.printError(cartResult.getMessage());
        } else {
            // Mua truc tiep — hien xac nhan truoc
            Customer c = (Customer) authState.getCurrentUser();
            List<String[]> confirmItems = new ArrayList<>();
            confirmItems.add(new String[]{product.getName(), product.getId(), String.valueOf(qty), String.valueOf(product.getPrice())});
            if (showOrderConfirmation(c, confirmItems)) {
                ControllerResult res = customerController.directPurchase(product.getId(), qty);
                if (res.isSuccess()) { ConsoleUI.printSuccess(res.getMessage()); showPendingAlert(); }
                else ConsoleUI.printError(res.getMessage());
            } else {
                ConsoleUI.printError("Da huy mua hang.");
            }
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
        
        System.out.println("1. Huy don hang");
        System.out.println("0. Quay lai");
        int choice = ConsoleUI.getInt("Chon (0-1): ", 0, 1);
        if (choice == 1) {
            String orderId = ConsoleUI.getString("Nhap Ma Don Hang muon huy: ");
            ControllerResult cancelRes = customerController.cancelOrder(orderId);
            if (cancelRes.isSuccess()) {
                ConsoleUI.printSuccess(cancelRes.getMessage());
            } else {
                ConsoleUI.printError(cancelRes.getMessage());
            }
        }
    }

    private void manageCart() {
        while (true) {
            ConsoleUI.printHeader("GIO HANG CUA TOI");

            // Lay chi tiet gio hang (co ten san pham, don gia)
            @SuppressWarnings("unchecked")
            ControllerResult detailRes = customerController.getCartItemDetails();
            @SuppressWarnings("unchecked")
            List<Object[]> cartDetails = (List<Object[]>) detailRes.getData();

            if (cartDetails == null || cartDetails.isEmpty()) {
                System.out.println("Gio hang dang trong!");
                System.out.println("----------------------------------------");
                System.out.println("0. Quay lai");
                ConsoleUI.getInt("Chon (0): ", 0, 0);
                return;
            }

            // Hien thi bang gio hang day du thong tin
            System.out.printf("%-4s | %-12s | %-30s | %-9s | %-14s | %-14s%n",
                    "STT", "Ma SP", "Ten San Pham", "So Luong", "Don Gia (VND)", "Thanh Tien (VND)");
            System.out.println("--------------------------------------------------------------------------------------------");
            double tongTien = 0;
            // Dung index-based loop de STT on dinh
            for (int i = 0; i < cartDetails.size(); i++) {
                Object[] row = cartDetails.get(i);
                String maItem  = (String)  row[0];
                String tenSP   = (String)  row[1];
                int    soLuong = (Integer) row[2];
                double donGia  = (Double)  row[3];
                double thanhTien = donGia * soLuong;
                tongTien += thanhTien;
                // Rut gon ten SP neu qua dai
                String tenHienThi = tenSP.length() > 28 ? tenSP.substring(0, 27) + "." : tenSP;
                System.out.printf("%-4d | %-12s | %-30s | %-9d | %,14.0f | %,14.0f%n",
                        i + 1, maItem, tenHienThi, soLuong, donGia, thanhTien);
            }
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.printf("%-60s %,14.0f VND%n", "TONG CONG:", tongTien);
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.println("1. Thanh toan tat ca");
            System.out.println("2. Chon san pham de thanh toan");
            System.out.println("3. Xoa sach gio hang");
            System.out.println("0. Quay lai");

            int choice = ConsoleUI.getInt("Chon (0-3): ", 0, 3);
            switch (choice) {
                case 1: {
                    // Thanh toan tat ca — hien xac nhan truoc
                    Customer c = (Customer) authState.getCurrentUser();
                    List<String[]> confirmList = buildConfirmList(cartDetails);
                    if (showOrderConfirmation(c, confirmList)) {
                        ControllerResult checkoutRes = customerController.checkoutCart();
                        if (checkoutRes.isSuccess()) {
                            ConsoleUI.printSuccess(checkoutRes.getMessage());
                            showPendingAlert();
                            return;
                        } else {
                            ConsoleUI.printError(checkoutRes.getMessage());
                        }
                    } else {
                        ConsoleUI.printError("Da huy thanh toan.");
                    }
                    break;
                }
                case 2: {
                    // Chon san pham de thanh toan
                    System.out.println("Nhap so thu tu (STT) cac san pham muon thanh toan,");
                    System.out.println("cach nhau bang khoang trang hoac dau phay (vi du: 1 3 hoac 1,3):");
                    String input = ConsoleUI.getString("STT can thanh toan: ").replaceAll(",", " ");
                    String[] parts = input.trim().split("\\s+");
                    List<String> selectedIds = new ArrayList<>();
                    boolean inputValid = true;
                    for (String part : parts) {
                        try {
                            int idx = Integer.parseInt(part.trim()) - 1;
                            if (idx < 0 || idx >= cartDetails.size()) {
                                ConsoleUI.printError("So thu tu " + (idx+1) + " khong hop le!");
                                inputValid = false;
                                break;
                            }
                            String itemId = (String) cartDetails.get(idx)[0];
                            if (!selectedIds.contains(itemId)) selectedIds.add(itemId);
                        } catch (NumberFormatException e) {
                            ConsoleUI.printError("Gia tri '" + part + "' khong phai so nguyen!");
                            inputValid = false;
                            break;
                        }
                    }
                    if (!inputValid || selectedIds.isEmpty()) break;

                    // Loc ra chi cac item duoc chon de hien xac nhan
                    List<Object[]> selectedDetails = new ArrayList<>();
                    for (String sid : selectedIds) {
                        for (Object[] row : cartDetails) {
                            if (row[0].equals(sid)) { selectedDetails.add(row); break; }
                        }
                    }
                    Customer c = (Customer) authState.getCurrentUser();
                    List<String[]> confirmList = buildConfirmList(selectedDetails);
                    if (showOrderConfirmation(c, confirmList)) {
                        ControllerResult checkoutRes = customerController.checkoutSelectedItems(selectedIds);
                        if (checkoutRes.isSuccess()) {
                            ConsoleUI.printSuccess(checkoutRes.getMessage());
                            showPendingAlert();
                            // Neu gio trong thi thoat, con lai thi tiep tuc vong lap
                        } else {
                            ConsoleUI.printError(checkoutRes.getMessage());
                        }
                    } else {
                        ConsoleUI.printError("Da huy thanh toan.");
                    }
                    break;
                }
                case 3:
                    customerController.clearCart();
                    ConsoleUI.printSuccess("Da xoa sach gio hang!");
                    break;
                case 0:
                    return;
            }
        }
    }

    /**
     * Chuyen doi danh sach Object[] tu getCartItemDetails thanh List<String[]>
     * phu hop voi ham showOrderConfirmation.
     * String[]: {tenSP, maSP, soLuong, donGia}
     */
    private List<String[]> buildConfirmList(List<Object[]> details) {
        List<String[]> list = new ArrayList<>();
        for (Object[] row : details) {
            list.add(new String[]{
                (String) row[1],          // ten SP
                (String) row[0],          // ma SP
                String.valueOf(row[2]),   // so luong
                String.valueOf(row[3])    // don gia
            });
        }
        return list;
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
    /**
     * Hien thi man hinh xac nhan don hang truoc khi thuc su tao don.
     * Ho tro nhieu san pham trong mot don.
     *
     * @param c     Khach hang hien tai
     * @param items Danh sach san pham: moi phan tu la String[]{tenSP, maSP, soLuong, donGia}
     * @return true neu nguoi dung xac nhan, false neu huy
     */
    private boolean showOrderConfirmation(Customer c, List<String[]> items) {
        ConsoleUI.printHeader("XAC NHAN DON HANG");
        System.out.println("Khach Hang : " + c.getName() + " (Ma: " + c.getId() + ")");
        String addr = c.getAddress();
        System.out.println("Dia chi GH : " + (addr != null && !addr.isEmpty() ? addr : "Chua cap nhat (Van tiep tuc dat)"));
        System.out.println("----------------------------------------");
        System.out.printf("%-4s | %-30s | %-9s | %-14s | %-14s%n",
                "STT", "Ten San Pham", "So Luong", "Don Gia (VND)", "Thanh Tien (VND)");
        System.out.println("--------------------------------------------------------------------------");
        double tongCong = 0;
        for (int i = 0; i < items.size(); i++) {
            String[] item = items.get(i);
            String tenSP   = item[0];
            int    soLuong = Integer.parseInt(item[2]);
            double donGia  = Double.parseDouble(item[3]);
            double thanhTien = donGia * soLuong;
            tongCong += thanhTien;
            String tenHienThi = tenSP.length() > 28 ? tenSP.substring(0, 27) + "." : tenSP;
            System.out.printf("%-4d | %-30s | %-9d | %,14.0f | %,14.0f%n",
                    i + 1, tenHienThi, soLuong, donGia, thanhTien);
        }
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-58s %,14.0f VND%n", "TONG CONG:", tongCong);
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

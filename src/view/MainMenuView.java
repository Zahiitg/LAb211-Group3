package view;

import controller.AuthenticationState;
import controller.ControllerResult;

/**
 * Giao dien khoi dau cua toan bo ung dung.
 * Xu ly viec dang nhap, dang ky (sau nay), hoac thoat chuong trinh.
 * Sau khi dang nhap thanh cong, se tu dong dieu huong sang View phu hop (AdminView, CustomerView...).
 *
 * @author Thanh vien 1 - View Architecture
 */
public class MainMenuView {

    private final AuthenticationState authState;

    public MainMenuView() {
        this.authState = AuthenticationState.getInstance();
    }

    /**
     * Vong lap chinh cua he thong. Day la noi giu chuong trinh chay lien tuc.
     */
    public void start() {
        boolean running = true;
        
        while (running) {
            ConsoleUI.printHeader("FLASH SALE MANAGEMENT SYSTEM");
            System.out.println("1. Dang nhap");
            System.out.println("2. Dang ky");
            System.out.println("3. Tiep tuc voi vai tro Khach (Guest)");
            System.out.println("0. Thoat chuong trinh");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-3): ", 0, 3);

            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 3:
                    handleGuestMode();
                    break;
                case 0:
                    ConsoleUI.printSuccess("Cam on ban da su dung phan mem. Tam biet!");
                    running = false;
                    break;
            }
        }
    }

    /**
     * Xu ly form dang nhap.
     */
    private void handleLogin() {
        ConsoleUI.printHeader("DANG NHAP HE THONG");
        String email = ConsoleUI.getString("Nhap Email: ");
        String password = ConsoleUI.getString("Nhap Password: ");

        ControllerResult result = authState.login(email, password);

        if (result.isSuccess()) {
            ConsoleUI.printSuccess(result.getMessage());
            ConsoleUI.pause();

            // Dieu huong (Routing) dua tren Role cua User
            if (authState.isAdmin()) {
                AdminView adminView = new AdminView();
                adminView.start(); // Chuyen quyen dieu khien sang AdminView
            } else if (authState.isCustomer()) {
                CustomerView customerView = new CustomerView();
                customerView.start(); // Chuyen quyen dieu khien sang CustomerView
            } else if (authState.isSeller()) {
                SellerView sellerView = new SellerView();
                sellerView.start(); // Chuyen quyen dieu khien sang SellerView
            }
        } else {
            ConsoleUI.printError(result.getMessage());
            ConsoleUI.pause();
        }
    }

    /**
     * Xu ly form dang ky Customer.
     */
    private void handleRegister() {
        ConsoleUI.printHeader("DANG KY TAI KHOAN CUSTOMER");
        String name = ConsoleUI.getString("Nhap Ho va Ten: ");
        if (name.isEmpty()) return;
        
        String email = ConsoleUI.getString("Nhap Email: ");
        if (email.isEmpty()) return;
        
        String password = ConsoleUI.getString("Nhap Password: ");
        if (password.isEmpty()) return;
        
        String address = ConsoleUI.getString("Nhap Dia chi giao hang (co the de trong): ");

        controller.CustomerController customerController = new controller.CustomerController();
        ControllerResult result = customerController.register(name, email, password, address);

        if (result.isSuccess()) {
            ConsoleUI.printSuccess(result.getMessage());
        } else {
            ConsoleUI.printError(result.getMessage());
        }
        ConsoleUI.pause();
    }

    /**
     * Xu ly dang nhap voi vai tro Khach (Guest).
     * Khong can dang nhap, chi can bam Enter de tien hanh.
     */
    private void handleGuestMode() {
        ConsoleUI.printHeader("SHOPEE XIN CHAO!");
        System.out.println("Ban dang vao he thong voi vai tro KHACH (Guest).");
        System.out.println("- De mua hang, vui long dang ky tai khoan.");
        System.out.println("----------------------------------------");

        authState.loginAsGuest();
        ConsoleUI.printSuccess("Chao mung ban! Ban dang xem voi tu cach Khach.");
        ConsoleUI.pause();

        GuestView guestView = new GuestView();
        guestView.start();
    }
}

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
            System.out.println("2. Dang ky (Chua ho tro)");
            System.out.println("0. Thoat chuong trinh");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-2): ", 0, 2);

            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    ConsoleUI.printError("Chuc nang dang ky do TV2 dam nhiem, dang duoc xay dung.");
                    ConsoleUI.pause();
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
                ConsoleUI.printError("CustomerView do TV2 dam nhiem, chua duoc xay dung.");
                authState.logout(); // Dang xuat tam thoi de tranh loi khet
                ConsoleUI.pause();
            } else if (authState.isSeller()) {
                ConsoleUI.printError("SellerView do TV2 dam nhiem, chua duoc xay dung.");
                authState.logout(); // Dang xuat tam thoi de tranh loi khet
                ConsoleUI.pause();
            }
        } else {
            ConsoleUI.printError(result.getMessage());
            ConsoleUI.pause();
        }
    }
}

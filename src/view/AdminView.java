package view;

import controller.AdminController;
import controller.AuthenticationState;
import controller.ControllerResult;
import model.Customer;
import model.Seller;
import model.enums.AccountStatus;

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
            System.out.println("0. Dang xuat");
            System.out.println("----------------------------------------");

            int choice = ConsoleUI.getInt("Chon chuc nang (0-8): ", 0, 8);

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
    // UTILITIES
    // =====================================================================

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

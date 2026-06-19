package view;

import java.util.Scanner;

/**
 * Lop tien ich xu ly toan bo giao tiep giua he thong va nguoi dung tren Console.
 * Muc dich:
 * - Dong goi Scanner de tranh loi ranh gioi (InputMismatchException).
 * - Cung cap cac ham nhap lieu (chuoi, so) an toan.
 * - Format mau sac, tieu de cho dep mat.
 *
 * TOAN BO cac View khac phai dung class nay de nhap xuat, KHONG tu tao Scanner.
 *
 * @author Thanh vien 1 - View Architecture
 */
public class ConsoleUI {

    // =====================================================================
    // ANSI COLORS CHO CONSOLE (Ho tro tren hau het Terminal hien dai)
    // =====================================================================
    public static final String RESET  = "\u001B[0m";
    public static final String RED    = "\u001B[31m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN   = "\u001B[36m";

    private static final Scanner scanner = new Scanner(System.in);

    // =====================================================================
    // NHAP LIEU AN TOAN (BULLET-PROOF INPUT)
    // =====================================================================

    /**
     * Nhan input la mot chuoi ky tu (String).
     *
     * @param prompt Cau hoi in ra man hinh
     * @return Chuoi nguoi dung nhap
     */
    public static String getString(String prompt) {
        System.out.print(CYAN + prompt + RESET);
        return scanner.nextLine().trim();
    }

    /**
     * Nhan input la mot so nguyen (int).
     * Cho phep lap lai loi nhap neu nguoi dung nhap sai kieu du lieu.
     *
     * @param prompt Cau hoi in ra man hinh
     * @return So nguyen hop le
     */
    public static int getInt(String prompt) {
        while (true) {
            try {
                System.out.print(CYAN + prompt + RESET);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("Loi: Vui long nhap mot so nguyen hop le!");
            }
        }
    }

    /**
     * Nhan input la mot so nguyen nam trong khoang [min, max].
     *
     * @param prompt Cau hoi in ra man hinh
     * @param min Gia tri nho nhat cho phep
     * @param max Gia tri lon nhat cho phep
     * @return So nguyen hop le
     */
    public static int getInt(String prompt, int min, int max) {
        while (true) {
            int value = getInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            printError("Loi: Vui long nhap so trong khoang tu " + min + " den " + max + "!");
        }
    }

    // =====================================================================
    // HIEN THI THONG BAO (OUTPUT FORMATTING)
    // =====================================================================

    /**
     * In tieu de cua mot Menu.
     */
    public static void printHeader(String title) {
        System.out.println("\n" + YELLOW + "========================================");
        System.out.println("  " + title.toUpperCase());
        System.out.println("========================================" + RESET);
    }

    /**
     * In thong bao loi (Mau do).
     */
    public static void printError(String message) {
        System.out.println(RED + "[-] " + message + RESET);
    }

    /**
     * In thong bao thanh cong (Mau xanh la).
     */
    public static void printSuccess(String message) {
        System.out.println(GREEN + "[+] " + message + RESET);
    }

    /**
     * Dung man hinh cho nguoi dung an Enter roi tiep tuc.
     */
    public static void pause() {
        System.out.print(YELLOW + "Nhan [ENTER] de tiep tuc..." + RESET);
        scanner.nextLine();
    }
}

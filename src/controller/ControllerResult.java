package controller;

/**
 * Lop ket qua chuan hoa cho TOAN BO tang Controller.
 * Moi ham xu ly nghiep vu trong Controller deu tra ve doi tuong nay
 * de tang View co the xu ly phan hoi mot cach nhat quan.
 *
 * Vi du su dung:
 *   ControllerResult result = adminController.banCustomer("C001");
 *   if (result.isSuccess()) {
 *       Customer banned = (Customer) result.getData();
 *       System.out.println(result.getMessage());
 *   }
 *
 * @author Thanh vien 1 - Core Architecture
 */
public class ControllerResult {

    /** Trang thai thanh cong (true) hay that bai (false) */
    private final boolean success;

    /** Thong diep mo ta ket qua (human-readable) */
    private final String message;

    /** Du lieu dinh kem (co the la User, List, hoac null) */
    private final Object data;

    // =====================================================================
    // CONSTRUCTOR (Private - chi duoc tao qua Factory Methods)
    // =====================================================================

    private ControllerResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // =====================================================================
    // FACTORY METHODS - Cach duy nhat de tao ControllerResult
    // =====================================================================

    /**
     * Tao ket qua THANH CONG (khong kem du lieu).
     * @param message Thong diep thanh cong
     * @return ControllerResult voi success = true
     */
    public static ControllerResult success(String message) {
        return new ControllerResult(true, message, null);
    }

    /**
     * Tao ket qua THANH CONG (co kem du lieu dinh kem).
     * @param message Thong diep thanh cong
     * @param data    Du lieu dinh kem (User, List, v.v.)
     * @return ControllerResult voi success = true va data
     */
    public static ControllerResult success(String message, Object data) {
        return new ControllerResult(true, message, data);
    }

    /**
     * Tao ket qua THAT BAI.
     * @param message Thong diep loi
     * @return ControllerResult voi success = false
     */
    public static ControllerResult error(String message) {
        return new ControllerResult(false, message, null);
    }

    // =====================================================================
    // GETTERS
    // =====================================================================

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    // =====================================================================
    // HIEN THI
    // =====================================================================

    /**
     * Chuyen doi thanh chuoi de in ra Console.
     * Vi du: "[SUCCESS] Da ban tai khoan C001"
     *        "[ERROR] Email hoac mat khau khong chinh xac!"
     */
    @Override
    public String toString() {
        return (success ? "[SUCCESS] " : "[ERROR] ") + message;
    }
}

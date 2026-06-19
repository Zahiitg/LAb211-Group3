package controller;

/**
 * Lop co so TRUU TUONG cho toan bo tang Controller.
 *
 * Cung cap 2 nhom chuc nang chinh:
 * 1. AUTHENTICATION GUARDS: Kiem tra quyen truy cap truoc khi thuc hien nghiep vu
 *    - requireLogin()    → Bat buoc phai dang nhap
 *    - requireAdmin()    → Bat buoc phai la Admin
 *    - requireCustomer() → Bat buoc phai la Customer
 *    - requireSeller()   → Bat buoc phai la Seller
 *
 * 2. RESPONSE FACTORY: Tao phan hoi chuan hoa (ControllerResult)
 *    - success(message)       → Ket qua thanh cong
 *    - success(message, data) → Ket qua thanh cong kem du lieu
 *    - error(message)         → Ket qua that bai
 *
 * Tat ca Controller con (CustomerController, FlashSaleController, OrderController...)
 * deu PHAI extends lop nay de dam bao tinh nhat quan trong toan bo he thong.
 *
 * Vi du trong Controller con:
 *   public ControllerResult doSomething() {
 *       requireLogin();  // Kiem tra quyen
 *       // ... logic xu ly ...
 *       return success("Thao tac thanh cong!", resultData);
 *   }
 *
 * @author Thanh vien 1 - Core Architecture
 */
public abstract class BaseController {

    /** Tham chieu den Singleton quan ly trang thai dang nhap */
    protected final AuthenticationState authState = AuthenticationState.getInstance();

    // =====================================================================
    // AUTHENTICATION GUARDS - Kiem tra quyen truy cap
    // =====================================================================

    /**
     * Kiem tra nguoi dung DA DANG NHAP chua.
     * Goi ham nay o DAU moi ham nghiep vu can xac thuc.
     *
     * @throws IllegalStateException neu chua dang nhap
     */
    protected void requireLogin() {
        if (!authState.isLoggedIn()) {
            throw new IllegalStateException("Ban chua dang nhap! Vui long dang nhap truoc.");
        }
    }

    /**
     * Kiem tra nguoi dung da dang nhap VA phai la Admin.
     *
     * @throws IllegalStateException neu chua dang nhap hoac khong phai Admin
     */
    protected void requireAdmin() {
        requireLogin();
        if (!authState.isAdmin()) {
            throw new IllegalStateException(
                "Ban khong co quyen Admin de thuc hien thao tac nay!");
        }
    }

    /**
     * Kiem tra nguoi dung da dang nhap VA phai la Customer.
     *
     * @throws IllegalStateException neu chua dang nhap hoac khong phai Customer
     */
    protected void requireCustomer() {
        requireLogin();
        if (!authState.isCustomer()) {
            throw new IllegalStateException(
                "Chi Customer moi co quyen thuc hien thao tac nay!");
        }
    }

    /**
     * Kiem tra nguoi dung da dang nhap VA phai la Seller.
     *
     * @throws IllegalStateException neu chua dang nhap hoac khong phai Seller
     */
    protected void requireSeller() {
        requireLogin();
        if (!authState.isSeller()) {
            throw new IllegalStateException(
                "Chi Seller moi co quyen thuc hien thao tac nay!");
        }
    }

    // =====================================================================
    // RESPONSE FACTORY - Tao phan hoi chuan hoa
    // =====================================================================

    /**
     * Tao ket qua THANH CONG (khong kem du lieu).
     */
    protected ControllerResult success(String message) {
        return ControllerResult.success(message);
    }

    /**
     * Tao ket qua THANH CONG (co kem du lieu dinh kem).
     */
    protected ControllerResult success(String message, Object data) {
        return ControllerResult.success(message, data);
    }

    /**
     * Tao ket qua THAT BAI.
     */
    protected ControllerResult error(String message) {
        return ControllerResult.error(message);
    }
}

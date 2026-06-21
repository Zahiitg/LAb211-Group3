package controller;

import model.Customer;
import model.enums.AccountStatus;
import model.enums.CustTier;
import repository.CustomerRepository;

/**
 * Controller xu ly toan bo nghiep vu lien quan den Khach hang (Customer).
 *
 * Chuc nang:
 * 1. register(name, email, password) → Dang ky tai khoan moi
 * 2. login(email, password)          → Dang nhap va luu phien vao AuthenticationState
 *
 * Tat ca ham deu tra ve ControllerResult de tang View xu ly nhat quan,
 * KHONG BAO GIO quang Exception ra ngoai hoac tra ve null.
 *
 * @author Thanh vien 2 - Customer Logic
 * @refactored-by Thanh vien 1 - Core Architecture (ap dung chuan BaseController)
 */
public class CustomerController extends BaseController {

    private final CustomerRepository customerRepo;

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    /**
     * Constructor mac dinh - su dung duong dan file CSV chuan cua du an.
     */
    public CustomerController() {
        this.customerRepo = new CustomerRepository("data/customers.csv");
    }

    /**
     * Constructor cho testing hoac duong dan tuy chinh.
     * @param filePath Duong dan den file CSV customers
     */
    public CustomerController(String filePath) {
        this.customerRepo = new CustomerRepository(filePath);
    }

    // =====================================================================
    // DANG KY TAI KHOAN
    // =====================================================================

    /**
     * Dang ky tai khoan Customer moi.
     *
     * Quy trinh:
     * 1. Kiem tra du lieu dau vao (Name, Email, Password khong duoc rong).
     * 2. Kiem tra trung Email trong CSDL.
     * 3. Sinh ma ID tu dong tang (C00001, C00002, ...).
     * 4. Tao Customer moi voi trang thai APPROVED va hang BRONZE.
     * 5. Luu vao file CSV.
     *
     * @param name     Ten khach hang
     * @param email    Email (phai duy nhat)
     * @param password Mat khau
     * @param address  Dia chi giao hang (co the de trong)
     * @return ControllerResult chua Customer object neu thanh cong,
     *         hoac thong bao loi neu that bai
     */
    public ControllerResult register(String name, String email, String password, String address) {
        // --- VALIDATION ---
        if (name == null || name.trim().isEmpty()) {
            return error("Ten khong duoc de trong!");
        }
        if (email == null || email.trim().isEmpty()) {
            return error("Email khong duoc de trong!");
        }
        if (password == null || password.trim().isEmpty()) {
            return error("Mat khau khong duoc de trong!");
        }

        // --- KIEM TRA TRUNG EMAIL ---
        if (customerRepo.getByEmail(email.trim()) != null) {
            return error("Email '" + email.trim() + "' da duoc dang ky truoc do!");
        }

        // --- SINH MA ID TU DONG ---
        int maxNum = 0;
        for (Customer c : customerRepo.getAll()) {
            String id = c.getId();
            if (id != null && id.startsWith("C")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        String newId = String.format("C%05d", maxNum + 1);

        // --- TAO VA LUU CUSTOMER MOI ---
        Customer newCust = new Customer(
            newId,
            name.trim(),
            email.trim(),
            password,
            AccountStatus.APPROVED,
            CustTier.BRONZE,
            address != null ? address.trim() : ""
        );
        customerRepo.add(newCust);

        return success("Dang ky thanh cong! Chao mung " + newCust.getName()
                       + " (Ma KH: " + newId + ")", newCust);
    }

    // =====================================================================
    // DANG NHAP
    // =====================================================================

    /**
     * Xac thuc va dang nhap Customer.
     *
     * Quy trinh:
     * 1. Kiem tra du lieu dau vao.
     * 2. Tim Customer theo email va doi chieu mat khau.
     * 3. Kiem tra trang thai tai khoan (BANNED → tu choi, PENDING → canh bao).
     * 4. Neu hop le → luu phien dang nhap vao AuthenticationState (Singleton).
     *
     * @param email    Email dang nhap
     * @param password Mat khau
     * @return ControllerResult chua Customer object neu thanh cong,
     *         hoac thong bao loi cu the neu that bai
     */
    public ControllerResult login(String email, String password) {
        // --- VALIDATION ---
        if (email == null || email.trim().isEmpty()) {
            return error("Email khong duoc de trong!");
        }
        if (password == null || password.trim().isEmpty()) {
            return error("Mat khau khong duoc de trong!");
        }

        // --- TIM CUSTOMER THEO EMAIL ---
        Customer c = customerRepo.getByEmail(email.trim());
        if (c == null) {
            return error("Email hoac mat khau khong chinh xac!");
        }

        // --- DOI CHIEU MAT KHAU ---
        if (!c.getPassword().equals(password)) {
            return error("Email hoac mat khau khong chinh xac!");
        }

        // --- KIEM TRA TRANG THAI TAI KHOAN ---
        if (c.getStatus() == AccountStatus.BANNED) {
            return error("Tai khoan cua ban da bi khoa (BANNED). "
                        + "Vui long lien he Admin de duoc ho tro!");
        }

        // --- LUU PHIEN DANG NHAP ---
        // Su dung AuthenticationState (Singleton) cua he thong
        // Luu y: authState duoc ke thua tu BaseController
        // Tuy nhien vi AuthenticationState co ham login() rieng,
        // o day ta chi set currentUser thong qua phuong thuc login cua AuthenticationState
        // De tranh goi chong cheo, ta su dung truc tiep:
        AuthenticationState.getInstance().loginDirect(c);

        // --- TRA VE KET QUA ---
        String statusNote = "";
        if (c.getStatus() == AccountStatus.PENDING) {
            statusNote = " (Luu y: Tai khoan dang cho duyet, "
                       + "mot so chuc nang co the bi han che)";
        }

        return success("Dang nhap thanh cong! Xin chao " + c.getName() + statusNote, c);
    }

    // =====================================================================
    // CAP NHAT HO SO
    // =====================================================================

    /**
     * Cap nhat ho so cua Customer (hien tai chi ho tro dia chi).
     */
    public ControllerResult updateProfile(Customer c, String newAddress) {
        if (c == null) return error("Khong tim thay thong tin khach hang.");
        c.setAddress(newAddress);
        customerRepo.update(c);
        return success("Cap nhat ho so thanh cong!", c);
    }

    // =====================================================================
    // GETTER
    // =====================================================================

    /**
     * Lay CustomerRepository (dung cho Admin hoac Unit Test).
     * @return CustomerRepository
     */
    public CustomerRepository getCustomerRepo() {
        return customerRepo;
    }
}

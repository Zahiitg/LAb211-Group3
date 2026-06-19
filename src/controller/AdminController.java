package controller;

import java.util.List;

import model.Customer;
import model.Seller;
import model.enums.AccountStatus;
import repository.CustomerRepository;
import repository.SellerRepository;

/**
 * Controller dành riêng cho Admin — Quản lý tài khoản người dùng.
 *
 * Chuc nang:
 * - Xem danh sach Customer / Seller
 * - Khoa (Ban) tai khoan Customer / Seller
 * - Duyet (Approve) tai khoan Customer / Seller
 *
 * Moi ham deu goi requireAdmin() o dau → Dam bao CHI ADMIN moi thao tac duoc.
 * Moi ham deu tra ve ControllerResult → Tang View nhan ve format nhat quan.
 *
 * @author Thanh vien 1 - Core Architecture & Admin Controller
 */
public class AdminController extends BaseController {

    private final CustomerRepository customerRepo;
    private final SellerRepository sellerRepo;

    public AdminController() {
        this.customerRepo = authState.getCustomerRepo();
        this.sellerRepo = authState.getSellerRepo();
    }

    // =====================================================================
    // XEM DANH SACH
    // =====================================================================

    /**
     * Lay danh sach TOAN BO Customer.
     * Chi Admin moi duoc phep goi.
     *
     * @return ControllerResult chua List<Customer> trong data
     */
    public ControllerResult listAllCustomers() {
        requireAdmin();
        List<Customer> customers = customerRepo.getAll();
        return success("Tim thay " + customers.size() + " Customer trong he thong.", customers);
    }

    /**
     * Lay danh sach TOAN BO Seller.
     * Chi Admin moi duoc phep goi.
     *
     * @return ControllerResult chua List<Seller> trong data
     */
    public ControllerResult listAllSellers() {
        requireAdmin();
        List<Seller> sellers = sellerRepo.getAll();
        return success("Tim thay " + sellers.size() + " Seller trong he thong.", sellers);
    }

    // =====================================================================
    // KHOA TAI KHOAN (BAN)
    // =====================================================================

    /**
     * Khoa tai khoan Customer (doi trang thai thanh BANNED).
     * Kiem tra:
     * - Customer co ton tai khong
     * - Customer da bi ban tu truoc chua (tranh thao tac thua)
     *
     * @param customerId ID cua Customer can khoa
     * @return ControllerResult chua Customer da bi khoa trong data
     */
    public ControllerResult banCustomer(String customerId) {
        requireAdmin();

        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Customer ID khong duoc de trong!");
        }

        Customer customer = customerRepo.getById(customerId.trim());
        if (customer == null) {
            return error("Khong tim thay Customer voi ID: " + customerId);
        }
        if (customer.getStatus() == AccountStatus.BANNED) {
            return error("Customer '" + customer.getName()
                + "' (" + customerId + ") da bi khoa tu truoc!");
        }

        customer.setStatus(AccountStatus.BANNED);
        customerRepo.update(customer);

        return success("Da khoa tai khoan Customer: "
            + customer.getName() + " (" + customerId + ")", customer);
    }

    /**
     * Khoa tai khoan Seller (doi trang thai thanh BANNED).
     *
     * @param sellerId ID cua Seller can khoa
     * @return ControllerResult chua Seller da bi khoa trong data
     */
    public ControllerResult banSeller(String sellerId) {
        requireAdmin();

        if (sellerId == null || sellerId.trim().isEmpty()) {
            return error("Seller ID khong duoc de trong!");
        }

        Seller seller = sellerRepo.getById(sellerId.trim());
        if (seller == null) {
            return error("Khong tim thay Seller voi ID: " + sellerId);
        }
        if (seller.getStatus() == AccountStatus.BANNED) {
            return error("Seller '" + seller.getName()
                + "' (" + sellerId + ") da bi khoa tu truoc!");
        }

        seller.setStatus(AccountStatus.BANNED);
        sellerRepo.update(seller);

        return success("Da khoa tai khoan Seller: "
            + seller.getName() + " (" + sellerId + ")", seller);
    }

    // =====================================================================
    // DUYET TAI KHOAN (APPROVE)
    // =====================================================================

    /**
     * Duyet tai khoan Customer (doi trang thai thanh APPROVED).
     * Thuong dung de duyet cac tai khoan dang o trang thai PENDING.
     *
     * @param customerId ID cua Customer can duyet
     * @return ControllerResult chua Customer da duoc duyet trong data
     */
    public ControllerResult approveCustomer(String customerId) {
        requireAdmin();

        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Customer ID khong duoc de trong!");
        }

        Customer customer = customerRepo.getById(customerId.trim());
        if (customer == null) {
            return error("Khong tim thay Customer voi ID: " + customerId);
        }
        if (customer.getStatus() == AccountStatus.APPROVED) {
            return error("Customer '" + customer.getName()
                + "' (" + customerId + ") da duoc duyet tu truoc!");
        }

        customer.setStatus(AccountStatus.APPROVED);
        customerRepo.update(customer);

        return success("Da duyet tai khoan Customer: "
            + customer.getName() + " (" + customerId + ")", customer);
    }

    /**
     * Duyet tai khoan Seller (doi trang thai thanh APPROVED).
     *
     * @param sellerId ID cua Seller can duyet
     * @return ControllerResult chua Seller da duoc duyet trong data
     */
    public ControllerResult approveSeller(String sellerId) {
        requireAdmin();

        if (sellerId == null || sellerId.trim().isEmpty()) {
            return error("Seller ID khong duoc de trong!");
        }

        Seller seller = sellerRepo.getById(sellerId.trim());
        if (seller == null) {
            return error("Khong tim thay Seller voi ID: " + sellerId);
        }
        if (seller.getStatus() == AccountStatus.APPROVED) {
            return error("Seller '" + seller.getName()
                + "' (" + sellerId + ") da duoc duyet tu truoc!");
        }

        seller.setStatus(AccountStatus.APPROVED);
        sellerRepo.update(seller);

        return success("Da duyet tai khoan Seller: "
            + seller.getName() + " (" + sellerId + ")", seller);
    }

    // =====================================================================
    // MO KHOA TAI KHOAN (UNBAN)
    // =====================================================================

    /**
     * Mo khoa tai khoan Customer (doi trang thai tu BANNED thanh APPROVED).
     *
     * @param customerId ID cua Customer can mo khoa
     * @return ControllerResult chua Customer da duoc mo khoa trong data
     */
    public ControllerResult unbanCustomer(String customerId) {
        requireAdmin();

        if (customerId == null || customerId.trim().isEmpty()) {
            return error("Customer ID khong duoc de trong!");
        }

        Customer customer = customerRepo.getById(customerId.trim());
        if (customer == null) {
            return error("Khong tim thay Customer voi ID: " + customerId);
        }
        if (customer.getStatus() != AccountStatus.BANNED) {
            return error("Customer '" + customer.getName()
                + "' (" + customerId + ") khong bi khoa!");
        }

        customer.setStatus(AccountStatus.APPROVED);
        customerRepo.update(customer);

        return success("Da mo khoa tai khoan Customer: "
            + customer.getName() + " (" + customerId + ")", customer);
    }

    /**
     * Mo khoa tai khoan Seller (doi trang thai tu BANNED thanh APPROVED).
     *
     * @param sellerId ID cua Seller can mo khoa
     * @return ControllerResult chua Seller da duoc mo khoa trong data
     */
    public ControllerResult unbanSeller(String sellerId) {
        requireAdmin();

        if (sellerId == null || sellerId.trim().isEmpty()) {
            return error("Seller ID khong duoc de trong!");
        }

        Seller seller = sellerRepo.getById(sellerId.trim());
        if (seller == null) {
            return error("Khong tim thay Seller voi ID: " + sellerId);
        }
        if (seller.getStatus() != AccountStatus.BANNED) {
            return error("Seller '" + seller.getName()
                + "' (" + sellerId + ") khong bi khoa!");
        }

        seller.setStatus(AccountStatus.APPROVED);
        sellerRepo.update(seller);

        return success("Da mo khoa tai khoan Seller: "
            + seller.getName() + " (" + sellerId + ")", seller);
    }
}

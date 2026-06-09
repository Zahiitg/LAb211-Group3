package repository;

import java.util.List;

import model.Customer;
import model.enums.CustTier;

/**
 * Repository quan ly du lieu Customer tu file customers.csv.
 * Ke thua toan bo logic Cache + CRUD tu CsvRepository.
 */
public class CustomerRepository extends CsvRepository<Customer> {

    public CustomerRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected Customer createEntity() {
        return new Customer();
    }

    @Override
    protected String getHeader() {
        return "id,name,email,password,status,tier";
    }

    // ========================================================================
    // CAC HAM TIM KIEM MO RONG DANH RIENG CHO CUSTOMER
    // ========================================================================

    /**
     * Tim Customer theo email (dung cho chuc nang Login).
     * @param email Email can tim
     * @return Customer neu tim thay, null neu khong co
     */
    public Customer getByEmail(String email) {
        for (Customer c : getAll()) {
            if (c.getEmail() != null && c.getEmail().equalsIgnoreCase(email)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Xac thuc dang nhap Customer.
     * @param email Email dang nhap
     * @param password Mat khau
     * @return Customer neu dang nhap thanh cong, null neu sai thong tin
     */
    public Customer authenticate(String email, String password) {
        Customer c = getByEmail(email);
        if (c != null && c.getPassword() != null && c.getPassword().equals(password)) {
            return c;
        }
        return null;
    }

    /**
     * Tim danh sach Customer theo hang (Tier).
     * @param tier Hang khach hang can tim
     * @return Danh sach Customer thuoc hang nay
     */
    public List<Customer> findByTier(CustTier tier) {
        return findBy(c -> c.getTier() == tier);
    }
}

package repository;

import model.Admin;

/**
 * Repository quan ly du lieu Admin tu file admins.csv.
 * Ke thua toan bo logic Cache + CRUD tu CsvRepository.
 */
public class AdminRepository extends CsvRepository<Admin> {

    public AdminRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected Admin createEntity() {
        return new Admin();
    }

    @Override
    protected String getHeader() {
        return "id,name,email,password,status,roleLevel";
    }

    /**
     * Tim Admin theo email (dung cho chuc nang Login).
     * @param email Email can tim
     * @return Admin neu tim thay, null neu khong co
     */
    public Admin getByEmail(String email) {
        for (Admin a : getAll()) {
            if (a.getEmail() != null && a.getEmail().equalsIgnoreCase(email)) {
                return a;
            }
        }
        return null;
    }
}

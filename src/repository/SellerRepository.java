package repository;

import model.Seller;

/**
 * Repository quan ly du lieu Seller tu file sellers.csv.
 * Ke thua toan bo logic Cache + CRUD tu CsvRepository.
 */
public class SellerRepository extends CsvRepository<Seller> {

    public SellerRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected Seller createEntity() {
        return new Seller();
    }

    @Override
    protected String getHeader() {
        return "id,name,email,password,status,storeName";
    }

    /**
     * Tim Seller theo email.
     * @param email Email can tim
     * @return Seller neu tim thay, null neu khong co
     */
    public Seller getByEmail(String email) {
        for (Seller s : getAll()) {
            if (s.getEmail() != null && s.getEmail().equalsIgnoreCase(email)) {
                return s;
            }
        }
        return null;
    }
}

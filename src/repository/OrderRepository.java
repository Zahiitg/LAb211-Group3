package repository;

import model.Order;
import java.util.List;

/**
 * Repository luu tru va truy van Don hang (Order) xuong file CSV.
 *
 * File CSV: data/orders.csv
 * Cac truong: id, customerId, orderTime, status
 *
 * @author Thanh vien 4 - Orders & Transactions
 */
public class OrderRepository extends CsvRepository<Order> {

    public OrderRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected Order createEntity() {
        return new Order();
    }

    /**
     * Tim tat ca don hang cua 1 Customer.
     * @param customerId Ma khach hang (vi du: "C00001")
     * @return Danh sach Order cua customer do
     */
    public List<Order> findByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return findBy(order -> customerId.equals(order.getCustomerId()));
    }

    @Override
    protected String getHeader() {
        return "id,customerId,orderTime,status";
    }
}

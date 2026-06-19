package repository;

import model.OrderDetail;
import java.util.List;

/**
 * Repository luu tru chi tiet don hang (OrderDetail) xuong file CSV.
 *
 * File CSV: data/order_details.csv
 * Cac truong: id, orderId, flashSaleItemId, quantity, priceAtPurchase
 *
 * @author Thanh vien 4 - Orders & Transactions
 */
public class OrderDetailRepository extends CsvRepository<OrderDetail> {

    public OrderDetailRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected OrderDetail createEntity() {
        return new OrderDetail();
    }

    /**
     * Tim tat ca chi tiet cua 1 don hang.
     * @param orderId Ma don hang (vi du: "ORD00001")
     * @return Danh sach OrderDetail cua don hang do
     */
    public List<OrderDetail> findByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return findBy(detail -> orderId.equals(detail.getOrderId()));
    }

    @Override
    protected String getHeader() {
        return "id,orderId,flashSaleItemId,quantity,priceAtPurchase";
    }
}

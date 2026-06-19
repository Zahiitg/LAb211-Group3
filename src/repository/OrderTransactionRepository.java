package repository;

import model.OrderTransaction;
import model.enums.LockMechanism;
import java.util.List;

/**
 * Repository luu tru nhat ky giao dich (OrderTransaction) xuong file CSV.
 *
 * OrderTransaction ghi lai:
 * - Moi lan dat hang da dung co che khoa (Lock) nao
 * - So lan retry (neu dung Optimistic Lock)
 * - Thoi gian xu ly (ms) de do hieu suat
 * - Thanh cong hay that bai
 *
 * File CSV: data/order_transactions.csv
 *
 * @author Thanh vien 4 - Orders & Transactions
 */
public class OrderTransactionRepository extends CsvRepository<OrderTransaction> {

    public OrderTransactionRepository(String filePath) {
        super(filePath, true);
    }

    @Override
    protected OrderTransaction createEntity() {
        return new OrderTransaction();
    }

    /**
     * Tim tat ca giao dich cua 1 don hang.
     * @param orderId Ma don hang
     * @return Danh sach OrderTransaction
     */
    public List<OrderTransaction> findByOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return findBy(tx -> orderId.equals(tx.getOrderId()));
    }

    /**
     * Tim tat ca giao dich theo co che khoa cu the.
     * Huu ich khi phan tich hieu suat cua tung co che Lock.
     * @param mechanism Co che khoa (NO_LOCK, SYNCHRONIZED, FILE_LOCK, OPTIMISTIC_LOCK)
     * @return Danh sach OrderTransaction
     */
    public List<OrderTransaction> findByLockMechanism(LockMechanism mechanism) {
        return findBy(tx -> mechanism.equals(tx.getLockMechanism()));
    }

    @Override
    protected String getHeader() {
        return "id,orderId,lockMechanism,retryCount,processingTimeMs,success";
    }
}

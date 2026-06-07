package repository;

import model.BaseEntity;
import java.util.List;

/**
 * Generic Repository Interface - Cung cap cac thao tac CRUD co ban.
 * Moi Repository cu the (Customer, Product, Order...) deu phai implement interface nay.
 *
 * @param <T> Kieu Entity ke thua tu BaseEntity
 */
public interface IRepository<T extends BaseEntity> {

    /**
     * Lay tat ca ban ghi tu file CSV (doc tu Cache trong RAM).
     * @return Danh sach tat ca entity
     */
    List<T> getAll();

    /**
     * Tim kiem 1 entity theo ID.
     * @param id ID can tim
     * @return Entity neu tim thay, null neu khong co
     */
    T getById(String id);

    /**
     * Them moi 1 entity vao Cache va ghi xuong file CSV.
     * @param entity Entity can them
     */
    void add(T entity);

    /**
     * Cap nhat 1 entity da ton tai trong Cache va ghi xuong file CSV.
     * @param entity Entity da duoc cap nhat (ID phai trung voi ban ghi cu)
     */
    void update(T entity);

    /**
     * Xoa 1 entity khoi Cache va ghi lai file CSV.
     * @param id ID cua entity can xoa
     * @return true neu xoa thanh cong, false neu khong tim thay
     */
    boolean delete(String id);

    /**
     * Dem so luong ban ghi hien co.
     * @return So luong entity
     */
    int count();
}

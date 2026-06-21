package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.BaseEntity;

/**
 * Abstract Generic CSV Repository - Trai tim cua toan bo tang Data Access.
 *
 * Cach hoat dong:
 * 1. Khi khoi tao (Constructor), doc TOAN BO file CSV len RAM (Cache) bang
 * BufferedReader.
 * 2. Moi thao tac Tim kiem (getById) deu truy xuat tu HashMap => Toc do O(1),
 * cuc nhanh.
 * 3. Moi thao tac Ghi (add/update/delete) se cap nhat Cache TRUOC, roi ghi de
 * lai TOAN BO file CSV.
 *
 * Uu diem: Doc 10.000 dong < 1 giay. Thiet ke Generic de moi Entity deu dung
 * lai duoc.
 * Nhuoc diem: Khong an toan voi Da luong (Multi-thread) => Dev B se bo sung
 * Lock vao lop con.
 *
 * @param <T> Kieu Entity ke thua tu BaseEntity
 */
public abstract class CsvRepository<T extends BaseEntity> implements IRepository<T> {

    /** Cache luu tru Entity trong RAM. Key = ID, Value = Entity object. */
    protected final Map<String, T> cache = new LinkedHashMap<>();

    /** Duong dan tuyet doi toi file CSV. */
    private final String filePath;

    /** Co header (dong dau tien) trong file CSV hay khong. */
    private final boolean hasHeader;

    /**
     * Constructor - Doc toan bo file CSV len Cache.
     * 
     * @param filePath  Duong dan toi file CSV (vi du: "data/customers.csv")
     * @param hasHeader true neu file CSV co dong tieu de o dong dau tien
     */
    public CsvRepository(String filePath, boolean hasHeader) {
        this.filePath = filePath;
        this.hasHeader = hasHeader;
        loadFromFile();
    }

    // ========================================================================
    // CAC PHUONG THUC TRUU TUONG - Lop con BAT BUOC phai implement
    // ========================================================================

    /**
     * Tao 1 instance Entity rong (dung de goi fromCsvLine()).
     * Vi du: return new Customer();
     * 
     * @return Entity rong chua co du lieu
     */
    protected abstract T createEntity();

    // ========================================================================
    // IMPLEMENT CRUD TU INTERFACE IRepository
    // ========================================================================

    @Override
    public List<T> getAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public T getById(String id) {
        return cache.get(id);
    }

    @Override
    public void add(T entity) {
        if (entity.getId() == null || entity.getId().isEmpty()) {
            throw new IllegalArgumentException("Entity ID khong duoc de trong!");
        }
        if (cache.containsKey(entity.getId())) {
            throw new IllegalArgumentException("Entity voi ID '" + entity.getId() + "' da ton tai!");
        }
        cache.put(entity.getId(), entity);
        saveToFile();
    }

    @Override
    public void update(T entity) {
        if (!cache.containsKey(entity.getId())) {
            throw new IllegalArgumentException("Khong tim thay Entity voi ID '" + entity.getId() + "' de cap nhat!");
        }
        cache.put(entity.getId(), entity);
        saveToFile();
    }

    @Override
    public boolean delete(String id) {
        if (cache.remove(id) != null) {
            saveToFile();
            return true;
        }
        return false;
    }

    @Override
    public int count() {
        return cache.size();
    }

    // ========================================================================
    // PHUONG THUC HO TRO TIM KIEM MO RONG
    // ========================================================================

    /**
     * Tim kiem Entity theo dieu kien bat ky (dung Lambda/Predicate).
     * Vi du: findBy(c -> c.getName().contains("Huy"))
     * 
     * @param predicate Dieu kien loc
     * @return Danh sach Entity thoa man dieu kien
     */
    public List<T> findBy(java.util.function.Predicate<T> predicate) {
        List<T> results = new ArrayList<>();
        for (T entity : cache.values()) {
            if (predicate.test(entity)) {
                results.add(entity);
            }
        }
        return results;
    }

    // ========================================================================
    // DOC / GHI FILE CSV VOI BUFFERED I/O (SIEU TOC)
    // ========================================================================

    /**
     * Doc toan bo file CSV len Cache (RAM).
     * Su dung BufferedReader de doc tung dong, parse thanh Entity roi dua vao
     * HashMap.
     * Dong header (neu co) se duoc bo qua.
     */
    private void loadFromFile() {
        cache.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[CsvRepository] File khong ton tai, tao Cache rong: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Bo qua dong header
                if (isFirstLine && hasHeader) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                // Bo qua dong trong
                if (line.trim().isEmpty()) {
                    continue;
                }

                T entity = createEntity();
                entity.fromCsvLine(line);
                cache.put(entity.getId(), entity);
            }

            //System.out.println("[CsvRepository] Da doc " + cache.size() + " ban ghi tu: " + filePath);

        } catch (IOException e) {
            System.err.println("[CsvRepository] Loi doc file: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * Ghi toan bo Cache xuong file CSV (ghi de toan bo file).
     * Su dung BufferedWriter de tang toc do ghi.
     * Dong header se duoc ghi lai (neu co).
     */
    protected void saveToFile() {
        File file = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Ghi dong header
            if (hasHeader) {
                writer.write(getHeader());
                writer.newLine();
            }

            // Ghi tung dong du lieu tu Cache
            for (T entity : cache.values()) {
                writer.write(entity.toCsvLine());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("[CsvRepository] Loi ghi file: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * Tra ve dong Header cua file CSV.
     * Lop con co the override de tuy chinh. Mac dinh tra ve chuoi rong.
     * 
     * @return Chuoi header
     */
    protected String getHeader() {
        return "";
    }

    /**
     * Tai lai du lieu tu file CSV vao Cache.
     * Huu ich khi can dong bo lai voi file sau khi bi thay doi tu ben ngoai.
     */
    public void reload() {
        loadFromFile();
    }

    /**
     * Lay duong dan file CSV.
     * 
     * @return duong dan file
     */
    public String getFilePath() {
        return filePath;
    }
}

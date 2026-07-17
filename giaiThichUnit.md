# Giải thích chi tiết các hàm kiểm thử (Unit Test)

Tài liệu này giải thích từng dòng code của 3 phương thức quan trọng trong `RepositoryTest.java`: `testProductRepository`, `testCustomerRepository` và `testPerformance`.

## 1. Phương thức `testProductRepository`

Phương thức này kiểm tra các tính năng CRUD và tìm kiếm của Kho chứa Sản phẩm (ProductRepository).

```java
public void testProductRepository() {
    // Khởi tạo đối tượng ProductRepository và nạp dữ liệu từ file products.csv
    ProductRepository repo = new ProductRepository("data/products.csv");

    // Lấy toàn bộ danh sách sản phẩm
    List<Product> all = repo.findAll();
    // Khẳng định danh sách trả về không được trống
    assertFalse(all.isEmpty(), "findAll() phải đọc được dữ liệu");
    // Khẳng định số lượng sản phẩm đọc được phải đúng 5000 dòng
    assertEquals(5000, all.size(), "findAll() đọc đúng 5000 dòng");

    // Tìm kiếm sản phẩm có mã ID là "PRD-00001"
    Optional<Product> first = repo.findById("PRD-00001");
    // Khẳng định chắc chắn phải tìm thấy sản phẩm này
    assertTrue(first.isPresent(), "findById('PRD-00001') tìm thấy");
    
    // Nếu tìm thấy, thực hiện chuỗi kiểm tra các thuộc tính của sản phẩm đó
    first.ifPresent(p -> {
        // Khẳng định mã sản phẩm phải chính xác là "PRD-00001"
        assertEquals("PRD-00001", p.getProductId(), "productId đúng");
        // Khẳng định sản phẩm phải có tên (không được null)
        assertNotNull(p.getName(), "có tên sản phẩm");
        // Khẳng định tên sản phẩm không được là chuỗi rỗng
        assertFalse(p.getName().isEmpty(), "có tên sản phẩm");
        // Khẳng định giá bán nguyên thủy của sản phẩm phải lớn hơn 0
        assertTrue(p.getOriginalPrice() > 0, "giá > 0");
    });

    // Lọc danh sách các sản phẩm thuộc danh mục Điện tử (DIEN_TU)
    List<Product> dienTu = repo.findByCategory(ProductCategory.DIEN_TU);
    // Khẳng định danh sách trả về không bị trống
    assertFalse(dienTu.isEmpty(), "findByCategory(DIEN_TU) có kết quả");
    // Quét toàn bộ danh sách, khẳng định mọi sản phẩm đều có Category là DIEN_TU
    assertTrue(dienTu.stream().allMatch(p -> p.getCategory() == ProductCategory.DIEN_TU), "tất cả đều DIEN_TU");

    // Lọc danh sách các sản phẩm có mức giá từ 100.000 đến 500.000
    List<Product> priceRange = repo.findByPriceRange(100_000, 500_000);
    // Khẳng định có sản phẩm trong mức giá này
    assertFalse(priceRange.isEmpty(), "findByPriceRange có kết quả");
    // Quét toàn bộ danh sách, khẳng định mọi sản phẩm đều có giá nằm trong đoạn [100.000, 500.000]
    assertTrue(priceRange.stream().allMatch(p -> p.getOriginalPrice() >= 100_000 && p.getOriginalPrice() <= 500_000), "trong khoảng giá");

    // Lọc danh sách sản phẩm có chứa từ khóa "Tai nghe" trong tên
    List<Product> searchName = repo.findByName("Tai nghe");
    // Khẳng định tìm kiếm có trả về kết quả
    assertFalse(searchName.isEmpty(), "findByName có kết quả");

    // Lọc danh sách các sản phẩm còn hàng tồn kho (stock > 0)
    List<Product> inStock = repo.findInStock();
    // Khẳng định danh sách trả về không rỗng
    assertFalse(inStock.isEmpty(), "findInStock có kết quả");
}
```

## 2. Phương thức `testCustomerRepository`

Phương thức này kiểm tra các tính năng CRUD và tìm kiếm của Kho chứa Khách hàng (CustomerRepository).

```java
public void testCustomerRepository() {
    // Khởi tạo đối tượng CustomerRepository và nạp dữ liệu từ file customers.csv
    CustomerRepository repo = new CustomerRepository("data/customers.csv");

    // Lấy toàn bộ danh sách khách hàng
    List<Customer> all = repo.findAll();
    // Khẳng định danh sách trả về không được trống
    assertFalse(all.isEmpty());
    // Khẳng định số lượng khách hàng đọc được phải đúng 2000 dòng
    assertEquals(2000, all.size());

    // Tìm kiếm khách hàng có mã ID là "CUS-00001"
    Optional<Customer> first = repo.findById("CUS-00001");
    // Khẳng định chắc chắn phải tìm thấy khách hàng này
    assertTrue(first.isPresent());

    // Lọc danh sách các khách hàng có hạng là VIP
    List<Customer> vips = repo.findByTier(CustomerTier.VIP);
    // Khẳng định có khách hàng VIP trong dữ liệu
    assertFalse(vips.isEmpty());
    // Khẳng định tất cả các khách hàng trong danh sách trả về đều đúng là hạng VIP
    assertTrue(vips.stream().allMatch(c -> c.getTier() == CustomerTier.VIP));

    // Nếu danh sách khách hàng ban đầu không trống
    if (!all.isEmpty()) {
        // Lấy địa chỉ email của khách hàng đầu tiên trong danh sách
        String email = all.get(0).getEmail();
        // Dùng địa chỉ email đó để gọi hàm tìm kiếm và khẳng định hàm tìm kiếm hoạt động thành công
        assertTrue(repo.findByEmail(email).isPresent());
    }
}
```

## 3. Phương thức `testPerformance`

Đây là bài kiểm tra Hiệu năng siêu quan trọng nhằm chứng minh phần mềm xử lý dữ liệu cực nhanh.

```java
public void testPerformance() {
    // Đánh dấu mốc thời gian bắt đầu cho CẢ QUÁ TRÌNH
    long start = System.currentTimeMillis();
    
    // Đọc liên tục 3 kho dữ liệu từ ổ cứng lên RAM
    List<Product> products = new ProductRepository("data/products.csv").findAll(); // Đọc 5000 dòng
    List<Customer> customers = new CustomerRepository("data/customers.csv").findAll(); // Đọc 2000 dòng
    List<Order> orders = new OrderRepository("data/orders.csv").findAll(); // Đọc 2500 dòng

    // Tính TỔNG thời gian đã trôi qua
    long totalElapsed = System.currentTimeMillis() - start;

    // Tính tổng số lượng dòng đã đọc
    int totalLines = products.size() + customers.size() + orders.size();

    // Khẳng định tổng số lượng bản ghi phần mềm đọc được đạt trên 9500 dòng (xấp xỉ 10K)
    assertTrue(totalLines >= 9500, "Tổng đọc >= 9500 dòng");
    
    // CHUẨN NHẤT: Khẳng định TỔNG thời gian của cả 3 thao tác phải < 1000ms (1 giây)
    // Nếu tổng thời gian > 1s, test case sẽ đánh Fail
    assertTrue(totalElapsed < 1000, "Tổng thời gian đọc " + totalLines + " dòng là " + totalElapsed + "ms (Phải < 1000ms)");
}
```


@echo off
chcp 65001 > nul
set "PROJECT_DIR=%~dp0"

echo Dang bien dich ma nguon...
dir /s /B "%PROJECT_DIR%src\main\java\*.java" > "%PROJECT_DIR%sources.txt"
javac -encoding UTF-8 -d "%PROJECT_DIR%target\classes" @"%PROJECT_DIR%sources.txt"
del "%PROJECT_DIR%sources.txt"

echo Dang khoi chay ung dung...
java -Dfile.encoding=UTF-8 -cp "%PROJECT_DIR%target\classes" view.MainView
pause

===== HE THONG FLASH SALE =====
1. Nguoi mua
2. Nguoi ban
3. Admin
0. Thoat
Chon role: 3
Admin email: admin@gmail.com
Admin password: 

Sai tai khoan Admin.

===== HE THONG FLASH SALE =====
1. Nguoi mua
2. Nguoi ban
3. Admin
0. Thoat
Chon role: 3              
Admin email: admin@gmail.com
Admin password: 


===== QUAN TRI VIEN =====
1. Xem Flash Sale cho phe duyet
2. Phe duyet Flash Sale
3. Tu choi Flash Sale
4. Xem tat ca Flash Sale
5. Bat dau Flash Sale da duyet
6. Ket thuc Flash Sale
7. Chay Simulator 4 co che lock
0. Dang xuat Admin
Chon: 7
Nhap flashItemId can simulate: PRD-00007
So customer co the dung de test: 2011
Nhap so thread (1-2011, moi thread = 1 customer): 1000
Nhap so luong moi thread mua: 30
Thao tac that bai: Khong tim thay flashItemId: PRD-00007

===== QUAN TRI VIEN =====
1. Xem Flash Sale cho phe duyet
2. Phe duyet Flash Sale
3. Tu choi Flash Sale
4. Xem tat ca Flash Sale
5. Bat dau Flash Sale da duyet
6. Ket thuc Flash Sale
7. Chay Simulator 4 co che lock
0. Dang xuat Admin
Chon: 7
Nhap flashItemId can simulate: FSI-00006
So customer co the dung de test: 2011
Nhap so thread (1-2011, moi thread = 1 customer): 1000
Nhap so luong moi thread mua: 100

=== SIMULATOR RESULT ===
Mechanism       Threads       OK     Fail       Sold LostUpdate   Oversold       TPS  vs Baseline   Violation%   Muc tieu
NO_LOCK            1000        0     1000    0/22             0          0      0.00     Baseline        0.00%       PASS
FILE_LOCK          1000        0     1000    0/22             0          0      0.00      -100.0%        0.00%       FAIL
SYNCHRONIZED       1000        0     1000    0/22             0          0      0.00      -100.0%        0.00%       FAIL
OPTIMISTIC         1000        0     1000    0/22             0          0      0.00      -100.0%        0.00%       FAIL
Da ghi log vao data/transactions.csv
Moi transaction co customerId de truy vet khach hang.
Da ghi tong hop vao data/simulation_results.csv

# Sửa cơ chế Lock & Thêm chức năng Giả lập Multi-thread

## Bối cảnh

Dự án Flash Sale có 4 cơ chế lock (`NO_LOCK`, `SYNCHRONIZED`, `FILE_LOCK`, `OPTIMISTIC_LOCK`) trong `FlashSaleItemRepository` và `ProductRepository`. Hiện tại có **2 lỗi logic nghiêm trọng** cần sửa trước khi xây dựng phần giả lập. Sau đó cần implement `SimulatorController` + `SimulatorView` để chạy experiment so sánh hiệu suất 4 cơ chế lock.

## Proposed Changes

### 1. Sửa lỗi Lock Mechanisms

#### [MODIFY] [FlashSaleItemRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/FlashSaleItemRepository.java)

**Lỗi 1 - `fileLockMonitor` là instance variable (dòng 82):**
```diff
- private final Object fileLockMonitor = new Object();
+ private static final Object fileLockMonitor = new Object();
```
→ Sửa thành `static` để tất cả instance dùng chung 1 monitor, tránh `OverlappingFileLockException`.

**Lỗi 2 - Optimistic Lock không thực sự kiểm tra version (dòng 163-203):**
- Hiện tại: Đọc `item`, gán `oldVersion`, rồi so `item.getVersion() != oldVersion` → Luôn bằng nhau vì cùng 1 object reference.
- Sửa: Gọi `reload()` rồi `getById()` lần nữa trước khi compare để lấy dữ liệu thực tế từ file.

```diff
- // Buoc 3: Compare
- if (item.getVersion() != oldVersion) {
+ // Buoc 3: Compare - Doc lai tu file de lay phien ban thuc te
+ reload();
+ FlashSaleItem freshItem = getById(itemId);
+ if (freshItem == null || freshItem.getVersion() != oldVersion) {
```

---

#### [MODIFY] [ProductRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/ProductRepository.java)

Sửa tương tự 2 lỗi như `FlashSaleItemRepository`:
- `fileLockMonitor` → `static`
- `sellWithOptimisticLock`: Thêm `reload()` + `getById()` trước khi compare stock

---

### 2. Implement SimulatorController

#### [NEW] [SimulatorController.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/controller/SimulatorController.java)

Controller chuyên giả lập multi-thread mua hàng. Thiết kế:

- **`runSimulation(LockMechanism mechanism, int threadCount, String itemId, int qtyPerThread)`** → Chạy 1 lần giả lập với cơ chế lock chỉ định
  - Dùng `ExecutorService` (thread pool) + `CountDownLatch` để đồng bộ tất cả thread bắt đầu cùng lúc
  - Mỗi thread gọi `FlashSaleItemRepository.sellWith*()` tương ứng
  - Thu thập kết quả: successCount, failCount, elapsed time, TPS
  - Ghi từng transaction vào `OrderTransactionRepository`
  - Trả về `SimulationResult` (inner class hoặc model)

- **`runFullExperiment(int threadCount, int repeatCount, String itemId, int qtyPerThread)`** → Chạy full experiment: `threadCount × 4 mechanisms × repeatCount lần lặp`
  - Reset stock trước mỗi lần chạy
  - Lấy trung bình qua các lần lặp
  - Trả về `List<ExperimentResult>` (4 dòng, mỗi dòng là 1 mechanism)

**Inner classes:**
```java
public static class SimulationResult {
    LockMechanism mechanism;
    int threadCount;
    int successCount;
    int failCount;
    long elapsedMs;
    double tps;           // TPS = successCount / (elapsedMs / 1000.0)
    int expectedStock;    // stock mong doi (stock ban dau - success * qty)
    int actualStock;      // stock thuc te sau khi chay
    boolean dataConsistent; // expectedStock == actualStock
}

public static class ExperimentResult {
    LockMechanism mechanism;
    double avgSuccess;
    double avgFail;
    double avgElapsedMs;
    double avgTps;
    boolean allConsistent; // Tat ca cac lan chay deu consistent
}
```

**Luồng giả lập chi tiết:**
```
1. Lưu stock ban đầu (originalStock)
2. Reset stock về originalStock
3. Tạo ExecutorService(threadCount)
4. Tạo CountDownLatch startGate(1) + CountDownLatch doneLatch(threadCount)
5. Submit threadCount tasks:
   - Mỗi task: startGate.await() → gọi sellWith*() → ghi kết quả → doneLatch.countDown()
6. startGate.countDown() ← Tất cả thread bắt đầu ĐỒNG THỜI
7. doneLatch.await() ← Đợi tất cả hoàn thành
8. Tính TPS = successCount / (elapsed / 1000.0)
9. So sánh actualStock vs expectedStock
```

---

### 3. Implement SimulatorView

#### [NEW] [SimulatorView.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/view/SimulatorView.java)

**Khi nhấn vào chức năng "Gia lap", màn hình sẽ hiện ra:**

```
========================================
  GIA LAP MUA HANG DA LUONG
========================================
1. Chay gia lap nhanh (Quick Simulation)
2. Chay thi nghiem day du (Full Experiment)
0. Quay lai
----------------------------------------
Chon chuc nang (0-2): 
```

**Option 1 - Quick Simulation:**
```
--- CAU HINH GIA LAP ---
Nhap so luong thread (10-5000): 100
Nhap so luong mua moi thread (1-10): 1
Chon co che Lock:
  1. NO_LOCK (Baseline - khong khoa)
  2. SYNCHRONIZED (Khoa cap JVM)
  3. FILE_LOCK (Khoa cap OS)
  4. OPTIMISTIC_LOCK (Khoa lac quan)
  5. TAT CA (chay lan luot 4 co che)
Chon (1-5): 5

[Dang chay gia lap 100 threads x NO_LOCK...]
[Dang chay gia lap 100 threads x SYNCHRONIZED...]
...
```

**Option 2 - Full Experiment:**
```
--- CAU HINH THI NGHIEM ---
Nhap so luong thread (10-5000): 1000
Nhap so lan lap (1-10): 3
Nhap so luong mua moi thread (1-10): 1

[Dang chay: NO_LOCK lan 1/3...]
[Dang chay: NO_LOCK lan 2/3...]
...
```

**Bảng kết quả ASCII (sau khi chạy xong):**

```
╔══════════════════╦══════════╦══════════╦═══════════╦══════════╦═══════════════╦════════════╦═══════════╗
║ Co che Lock      ║ Threads  ║ Success  ║ Fail      ║ TPS      ║ Time (ms)     ║ vs Baseline║ Muc tieu  ║
╠══════════════════╬══════════╬══════════╬═══════════╬══════════╬═══════════════╬════════════╬═══════════╣
║ NO_LOCK          ║ 1000     ║ 1000     ║ 0         ║ 5000.0   ║ 200           ║ ---        ║ OVERSELL! ║
║ SYNCHRONIZED     ║ 1000     ║ 500      ║ 500       ║ 2500.0   ║ 200           ║ -50.0% TPS ║ OK        ║
║ FILE_LOCK        ║ 1000     ║ 500      ║ 500       ║ 1200.0   ║ 416           ║ -76.0% TPS ║ OK        ║
║ OPTIMISTIC_LOCK  ║ 1000     ║ 498      ║ 502       ║ 2000.0   ║ 249           ║ -60.0% TPS ║ OK        ║
╚══════════════════╩══════════╩══════════╩═══════════╩══════════╩═══════════════╩════════════╩═══════════╝

KIEM TRA TINH NHAT QUAN DU LIEU:
+------------------+----------+----------+-----------+
| Co che Lock      | Stock DK | Stock TT | Ket qua   |
+------------------+----------+----------+-----------+
| NO_LOCK          | 500      | 0        | SAI (Oversell!) |
| SYNCHRONIZED     | 500      | 500      | DUNG      |
| FILE_LOCK        | 500      | 500      | DUNG      |
| OPTIMISTIC_LOCK  | 500      | 502      | DUNG      |
+------------------+----------+----------+-----------+
```

**Giải thích cột:**
- `vs Baseline`: So sánh TPS với NO_LOCK (phần trăm chênh lệch)
- `Muc tieu`: `OK` nếu data consistent, `OVERSELL!` nếu tồn kho bị âm/sai

---

### 4. Tích hợp vào AdminView

#### [MODIFY] [AdminView.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/view/AdminView.java)

Thêm menu item **17. Gia lap mua hang da luong** vào Admin Dashboard:
- Cập nhật menu từ `(0-16)` → `(0-17)`
- Thêm `case 17: runSimulator();`
- Method `runSimulator()` tạo `SimulatorView` và gọi `start()`

---

### 5. Hỗ trợ reset stock trong FlashSaleItemRepository

#### [MODIFY] [FlashSaleItemRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/FlashSaleItemRepository.java)

Thêm method `resetStock(String itemId, int soldQty, int version)` để reset lại stock trước mỗi lần giả lập:
```java
public void resetStock(String itemId, int soldQty, int version) {
    FlashSaleItem item = getById(itemId);
    if (item != null) {
        item.setSoldQty(soldQty);
        item.setVersion(version);
        update(item);
    }
}
```

---

## Open Questions

> [!IMPORTANT]
> **Câu hỏi 1:** Chức năng giả lập chỉ dành cho Admin hay cho tất cả role? Hiện tại tôi đặt trong AdminView (option 17). Nếu bạn muốn nó là menu riêng (ví dụ option 4 trên MainMenuView), hãy cho tôi biết.

> [!IMPORTANT]
> **Câu hỏi 2:** Khi chạy giả lập, bạn muốn sử dụng FlashSaleItem hay Product? Hiện tại FlashSaleItem đã có field `version` sẵn (cần cho Optimistic Lock), còn Product thì chưa. Tôi dự kiến dùng **FlashSaleItem** cho giả lập. Đồng ý không?

## Verification Plan
# Sửa cơ chế Lock & Thêm chức năng Giả lập Multi-thread

## Bối cảnh

Dự án Flash Sale có 4 cơ chế lock (`NO_LOCK`, `SYNCHRONIZED`, `FILE_LOCK`, `OPTIMISTIC_LOCK`) trong `FlashSaleItemRepository` và `ProductRepository`. Hiện tại có **2 lỗi logic nghiêm trọng** cần sửa trước khi xây dựng phần giả lập. Sau đó cần implement `SimulatorController` + `SimulatorView` để chạy experiment so sánh hiệu suất 4 cơ chế lock.

## Proposed Changes

### 1. Sửa lỗi Lock Mechanisms

#### [MODIFY] [FlashSaleItemRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/FlashSaleItemRepository.java)

**Lỗi 1 - `fileLockMonitor` là instance variable (dòng 82):**
```diff
- private final Object fileLockMonitor = new Object();
+ private static final Object fileLockMonitor = new Object();
```
→ Sửa thành `static` để tất cả instance dùng chung 1 monitor, tránh `OverlappingFileLockException`.

**Lỗi 2 - Optimistic Lock không thực sự kiểm tra version (dòng 163-203):**
- Hiện tại: Đọc `item`, gán `oldVersion`, rồi so `item.getVersion() != oldVersion` → Luôn bằng nhau vì cùng 1 object reference.
- Sửa: Gọi `reload()` rồi `getById()` lần nữa trước khi compare để lấy dữ liệu thực tế từ file.

```diff
- // Buoc 3: Compare
- if (item.getVersion() != oldVersion) {
+ // Buoc 3: Compare - Doc lai tu file de lay phien ban thuc te
+ reload();
+ FlashSaleItem freshItem = getById(itemId);
+ if (freshItem == null || freshItem.getVersion() != oldVersion) {
```

---

#### [MODIFY] [ProductRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/ProductRepository.java)

Sửa tương tự 2 lỗi như `FlashSaleItemRepository`:
- `fileLockMonitor` → `static`
- `sellWithOptimisticLock`: Thêm `reload()` + `getById()` trước khi compare stock

---

### 2. Implement SimulatorController

#### [NEW] [SimulatorController.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/controller/SimulatorController.java)

Controller chuyên giả lập multi-thread mua hàng. Thiết kế:

- **`runSimulation(LockMechanism mechanism, int threadCount, String itemId, int qtyPerThread)`** → Chạy 1 lần giả lập với cơ chế lock chỉ định
  - Dùng `ExecutorService` (thread pool) + `CountDownLatch` để đồng bộ tất cả thread bắt đầu cùng lúc
  - Mỗi thread gọi `FlashSaleItemRepository.sellWith*()` tương ứng
  - Thu thập kết quả: successCount, failCount, elapsed time, TPS
  - Ghi từng transaction vào `OrderTransactionRepository`
  - Trả về `SimulationResult` (inner class hoặc model)

- **`runFullExperiment(int threadCount, int repeatCount, String itemId, int qtyPerThread)`** → Chạy full experiment: `threadCount × 4 mechanisms × repeatCount lần lặp`
  - Reset stock trước mỗi lần chạy
  - Lấy trung bình qua các lần lặp
  - Trả về `List<ExperimentResult>` (4 dòng, mỗi dòng là 1 mechanism)

**Inner classes:**
```java
public static class SimulationResult {
    LockMechanism mechanism;
    int threadCount;
    int successCount;
    int failCount;
    long elapsedMs;
    double tps;           // TPS = successCount / (elapsedMs / 1000.0)
    int expectedStock;    // stock mong doi (stock ban dau - success * qty)
    int actualStock;      // stock thuc te sau khi chay
    boolean dataConsistent; // expectedStock == actualStock
}

public static class ExperimentResult {
    LockMechanism mechanism;
    double avgSuccess;
    double avgFail;
    double avgElapsedMs;
    double avgTps;
    boolean allConsistent; // Tat ca cac lan chay deu consistent
}
```

**Luồng giả lập chi tiết:**
```
1. Lưu stock ban đầu (originalStock)
2. Reset stock về originalStock
3. Tạo ExecutorService(threadCount)
4. Tạo CountDownLatch startGate(1) + CountDownLatch doneLatch(threadCount)
5. Submit threadCount tasks:
   - Mỗi task: startGate.await() → gọi sellWith*() → ghi kết quả → doneLatch.countDown()
6. startGate.countDown() ← Tất cả thread bắt đầu ĐỒNG THỜI
7. doneLatch.await() ← Đợi tất cả hoàn thành
8. Tính TPS = successCount / (elapsed / 1000.0)
9. So sánh actualStock vs expectedStock
```

---

### 3. Implement SimulatorView

#### [NEW] [SimulatorView.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/view/SimulatorView.java)

**Khi nhấn vào chức năng "Gia lap", màn hình sẽ hiện ra:**

```
========================================
  GIA LAP MUA HANG DA LUONG
========================================
1. Chay gia lap nhanh (Quick Simulation)
2. Chay thi nghiem day du (Full Experiment)
0. Quay lai
----------------------------------------
Chon chuc nang (0-2): 
```

**Option 1 - Quick Simulation:**
```
--- CAU HINH GIA LAP ---
Nhap so luong thread (10-5000): 100
Nhap so luong mua moi thread (1-10): 1
Chon co che Lock:
  1. NO_LOCK (Baseline - khong khoa)
  2. SYNCHRONIZED (Khoa cap JVM)
  3. FILE_LOCK (Khoa cap OS)
  4. OPTIMISTIC_LOCK (Khoa lac quan)
  5. TAT CA (chay lan luot 4 co che)
Chon (1-5): 5

[Dang chay gia lap 100 threads x NO_LOCK...]
[Dang chay gia lap 100 threads x SYNCHRONIZED...]
...
```

**Option 2 - Full Experiment:**
```
--- CAU HINH THI NGHIEM ---
Nhap so luong thread (10-5000): 1000
Nhap so lan lap (1-10): 3
Nhap so luong mua moi thread (1-10): 1

[Dang chay: NO_LOCK lan 1/3...]
[Dang chay: NO_LOCK lan 2/3...]
...
```

**Bảng kết quả ASCII (sau khi chạy xong):**

```
╔══════════════════╦══════════╦══════════╦═══════════╦══════════╦═══════════════╦════════════╦═══════════╗
║ Co che Lock      ║ Threads  ║ Success  ║ Fail      ║ TPS      ║ Time (ms)     ║ vs Baseline║ Muc tieu  ║
╠══════════════════╬══════════╬══════════╬═══════════╬══════════╬═══════════════╬════════════╬═══════════╣
║ NO_LOCK          ║ 1000     ║ 1000     ║ 0         ║ 5000.0   ║ 200           ║ ---        ║ OVERSELL! ║
║ SYNCHRONIZED     ║ 1000     ║ 500      ║ 500       ║ 2500.0   ║ 200           ║ -50.0% TPS ║ OK        ║
║ FILE_LOCK        ║ 1000     ║ 500      ║ 500       ║ 1200.0   ║ 416           ║ -76.0% TPS ║ OK        ║
║ OPTIMISTIC_LOCK  ║ 1000     ║ 498      ║ 502       ║ 2000.0   ║ 249           ║ -60.0% TPS ║ OK        ║
╚══════════════════╩══════════╩══════════╩═══════════╩══════════╩═══════════════╩════════════╩═══════════╝

KIEM TRA TINH NHAT QUAN DU LIEU:
+------------------+----------+----------+-----------+
| Co che Lock      | Stock DK | Stock TT | Ket qua   |
+------------------+----------+----------+-----------+
| NO_LOCK          | 500      | 0        | SAI (Oversell!) |
| SYNCHRONIZED     | 500      | 500      | DUNG      |
| FILE_LOCK        | 500      | 500      | DUNG      |
| OPTIMISTIC_LOCK  | 500      | 502      | DUNG      |
+------------------+----------+----------+-----------+
```

**Giải thích cột:**
- `vs Baseline`: So sánh TPS với NO_LOCK (phần trăm chênh lệch)
- `Muc tieu`: `OK` nếu data consistent, `OVERSELL!` nếu tồn kho bị âm/sai

---

### 4. Tích hợp vào AdminView

#### [MODIFY] [AdminView.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/view/AdminView.java)

Thêm menu item **17. Gia lap mua hang da luong** vào Admin Dashboard:
- Cập nhật menu từ `(0-16)` → `(0-17)`
- Thêm `case 17: runSimulator();`
- Method `runSimulator()` tạo `SimulatorView` và gọi `start()`

---

### 5. Hỗ trợ reset stock trong FlashSaleItemRepository

#### [MODIFY] [FlashSaleItemRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/FlashSaleItemRepository.java)

Thêm method `resetStock(String itemId, int soldQty, int version)` để reset lại stock trước mỗi lần giả lập:
```java
public void resetStock(String itemId, int soldQty, int version) {
    FlashSaleItem item = getById(itemId);
    if (item != null) {
        item.setSoldQty(soldQty);
        item.setVersion(version);
        update(item);
    }
}
```

---

## Open Questions

> [!IMPORTANT]
> **Câu hỏi 1:** Chức năng giả lập chỉ dành cho Admin hay cho tất cả role? Hiện tại tôi đặt trong AdminView (option 17). Nếu bạn muốn nó là menu riêng (ví dụ option 4 trên MainMenuView), hãy cho tôi biết.

> [!IMPORTANT]
> **Câu hỏi 2:** Khi chạy giả lập, bạn muốn sử dụng FlashSaleItem hay Product? Hiện tại FlashSaleItem đã có field `version` sẵn (cần cho Optimistic Lock), còn Product thì chưa. Tôi dự kiến dùng **FlashSaleItem** cho giả lập. Đồng ý không?

## Verification Plan

### Manual Verification
1. Chạy Quick Simulation với 100 threads → Kiểm tra bảng kết quả hiện đúng
2. Chạy Full Experiment 1000 threads × 4 mechanisms × 3 lần → Kiểm tra dữ liệu trung bình
3. Kiểm tra file `data/transactions.csv` có ghi đúng các transaction
4. Kiểm tra `NO_LOCK` gây overselling, các lock khác giữ data consistent
5. Kiểm tra cột `vs Baseline` tính đúng phần trăm so với NO_LOCK

### Manual Verification
1. Chạy Quick Simulation với 100 threads → Kiểm tra bảng kết quả hiện đúng
2. Chạy Full Experiment 1000 threads × 4 mechanisms × 3 lần → Kiểm tra dữ liệu trung bình
3. Kiểm tra file `data/transactions.csv` có ghi đúng các transaction
4. Kiểm tra `NO_LOCK` gây overselling, các lock khác giữ data consistent
5. Kiểm tra cột `vs Baseline` tính đúng phần trăm so với NO_LOCK
# Sửa cơ chế Lock & Thêm chức năng Giả lập Multi-thread

## Bối cảnh

Dự án Flash Sale có 4 cơ chế lock (`NO_LOCK`, `SYNCHRONIZED`, `FILE_LOCK`, `OPTIMISTIC_LOCK`) trong `FlashSaleItemRepository` và `ProductRepository`. Hiện tại có **2 lỗi logic nghiêm trọng** cần sửa trước khi xây dựng phần giả lập. Sau đó cần implement `SimulatorController` + `SimulatorView` để chạy experiment so sánh hiệu suất 4 cơ chế lock.

## Proposed Changes

### 1. Sửa lỗi Lock Mechanisms

#### [MODIFY] [FlashSaleItemRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/FlashSaleItemRepository.java)

**Lỗi 1 - `fileLockMonitor` là instance variable (dòng 82):**
```diff
- private final Object fileLockMonitor = new Object();
+ private static final Object fileLockMonitor = new Object();
```
→ Sửa thành `static` để tất cả instance dùng chung 1 monitor, tránh `OverlappingFileLockException`.

**Lỗi 2 - Optimistic Lock không thực sự kiểm tra version (dòng 163-203):**
- Hiện tại: Đọc `item`, gán `oldVersion`, rồi so `item.getVersion() != oldVersion` → Luôn bằng nhau vì cùng 1 object reference.
- Sửa: Gọi `reload()` rồi `getById()` lần nữa trước khi compare để lấy dữ liệu thực tế từ file.

```diff
- // Buoc 3: Compare
- if (item.getVersion() != oldVersion) {
+ // Buoc 3: Compare - Doc lai tu file de lay phien ban thuc te
+ reload();
+ FlashSaleItem freshItem = getById(itemId);
+ if (freshItem == null || freshItem.getVersion() != oldVersion) {
```

---

#### [MODIFY] [ProductRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/ProductRepository.java)

Sửa tương tự 2 lỗi như `FlashSaleItemRepository`:
- `fileLockMonitor` → `static`
- `sellWithOptimisticLock`: Thêm `reload()` + `getById()` trước khi compare stock

---

### 2. Implement SimulatorController

#### [NEW] [SimulatorController.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/controller/SimulatorController.java)

Controller chuyên giả lập multi-thread mua hàng. Thiết kế:

- **`runSimulation(LockMechanism mechanism, int threadCount, String itemId, int qtyPerThread)`** → Chạy 1 lần giả lập với cơ chế lock chỉ định
  - Dùng `ExecutorService` (thread pool) + `CountDownLatch` để đồng bộ tất cả thread bắt đầu cùng lúc
  - Mỗi thread gọi `FlashSaleItemRepository.sellWith*()` tương ứng
  - Thu thập kết quả: successCount, failCount, elapsed time, TPS
  - Ghi từng transaction vào `OrderTransactionRepository`
  - Trả về `SimulationResult` (inner class hoặc model)

- **`runFullExperiment(int threadCount, int repeatCount, String itemId, int qtyPerThread)`** → Chạy full experiment: `threadCount × 4 mechanisms × repeatCount lần lặp`
  - Reset stock trước mỗi lần chạy
  - Lấy trung bình qua các lần lặp
  - Trả về `List<ExperimentResult>` (4 dòng, mỗi dòng là 1 mechanism)

**Inner classes:**
```java
public static class SimulationResult {
    LockMechanism mechanism;
    int threadCount;
    int successCount;
    int failCount;
    long elapsedMs;
    double tps;           // TPS = successCount / (elapsedMs / 1000.0)
    int expectedStock;    // stock mong doi (stock ban dau - success * qty)
    int actualStock;      // stock thuc te sau khi chay
    boolean dataConsistent; // expectedStock == actualStock
}

public static class ExperimentResult {
    LockMechanism mechanism;
    double avgSuccess;
    double avgFail;
    double avgElapsedMs;
    double avgTps;
    boolean allConsistent; // Tat ca cac lan chay deu consistent
}
```

**Luồng giả lập chi tiết:**
```
1. Lưu stock ban đầu (originalStock)
2. Reset stock về originalStock
3. Tạo ExecutorService(threadCount)
4. Tạo CountDownLatch startGate(1) + CountDownLatch doneLatch(threadCount)
5. Submit threadCount tasks:
   - Mỗi task: startGate.await() → gọi sellWith*() → ghi kết quả → doneLatch.countDown()
6. startGate.countDown() ← Tất cả thread bắt đầu ĐỒNG THỜI
7. doneLatch.await() ← Đợi tất cả hoàn thành
8. Tính TPS = successCount / (elapsed / 1000.0)
9. So sánh actualStock vs expectedStock
```

---

### 3. Implement SimulatorView

#### [NEW] [SimulatorView.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/view/SimulatorView.java)

**Khi nhấn vào chức năng "Gia lap", màn hình sẽ hiện ra:**

```
========================================
  GIA LAP MUA HANG DA LUONG
========================================
1. Chay gia lap nhanh (Quick Simulation)
2. Chay thi nghiem day du (Full Experiment)
0. Quay lai
----------------------------------------
Chon chuc nang (0-2): 
```

**Option 1 - Quick Simulation:**
```
--- CAU HINH GIA LAP ---
Nhap so luong thread (10-5000): 100
Nhap so luong mua moi thread (1-10): 1
Chon co che Lock:
  1. NO_LOCK (Baseline - khong khoa)
  2. SYNCHRONIZED (Khoa cap JVM)
  3. FILE_LOCK (Khoa cap OS)
  4. OPTIMISTIC_LOCK (Khoa lac quan)
  5. TAT CA (chay lan luot 4 co che)
Chon (1-5): 5

[Dang chay gia lap 100 threads x NO_LOCK...]
[Dang chay gia lap 100 threads x SYNCHRONIZED...]
...
```

**Option 2 - Full Experiment:**
```
--- CAU HINH THI NGHIEM ---
Nhap so luong thread (10-5000): 1000
Nhap so lan lap (1-10): 3
Nhap so luong mua moi thread (1-10): 1

[Dang chay: NO_LOCK lan 1/3...]
[Dang chay: NO_LOCK lan 2/3...]
...
```

**Bảng kết quả ASCII (sau khi chạy xong):**

```
╔══════════════════╦══════════╦══════════╦═══════════╦══════════╦═══════════════╦════════════╦═══════════╗
║ Co che Lock      ║ Threads  ║ Success  ║ Fail      ║ TPS      ║ Time (ms)     ║ vs Baseline║ Muc tieu  ║
╠══════════════════╬══════════╬══════════╬═══════════╬══════════╬═══════════════╬════════════╬═══════════╣
║ NO_LOCK          ║ 1000     ║ 1000     ║ 0         ║ 5000.0   ║ 200           ║ ---        ║ OVERSELL! ║
║ SYNCHRONIZED     ║ 1000     ║ 500      ║ 500       ║ 2500.0   ║ 200           ║ -50.0% TPS ║ OK        ║
║ FILE_LOCK        ║ 1000     ║ 500      ║ 500       ║ 1200.0   ║ 416           ║ -76.0% TPS ║ OK        ║
║ OPTIMISTIC_LOCK  ║ 1000     ║ 498      ║ 502       ║ 2000.0   ║ 249           ║ -60.0% TPS ║ OK        ║
╚══════════════════╩══════════╩══════════╩═══════════╩══════════╩═══════════════╩════════════╩═══════════╝

KIEM TRA TINH NHAT QUAN DU LIEU:
+------------------+----------+----------+-----------+
| Co che Lock      | Stock DK | Stock TT | Ket qua   |
+------------------+----------+----------+-----------+
| NO_LOCK          | 500      | 0        | SAI (Oversell!) |
| SYNCHRONIZED     | 500      | 500      | DUNG      |
| FILE_LOCK        | 500      | 500      | DUNG      |
| OPTIMISTIC_LOCK  | 500      | 502      | DUNG      |
+------------------+----------+----------+-----------+
```

**Giải thích cột:**
- `vs Baseline`: So sánh TPS với NO_LOCK (phần trăm chênh lệch)
- `Muc tieu`: `OK` nếu data consistent, `OVERSELL!` nếu tồn kho bị âm/sai

---

### 4. Tích hợp vào AdminView

#### [MODIFY] [AdminView.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/view/AdminView.java)

Thêm menu item **17. Gia lap mua hang da luong** vào Admin Dashboard:
- Cập nhật menu từ `(0-16)` → `(0-17)`
- Thêm `case 17: runSimulator();`
- Method `runSimulator()` tạo `SimulatorView` và gọi `start()`

---

### 5. Hỗ trợ reset stock trong FlashSaleItemRepository

#### [MODIFY] [FlashSaleItemRepository.java](file:///d:/SUM2026/LAB211/LAb211-Group3/src/repository/FlashSaleItemRepository.java)

Thêm method `resetStock(String itemId, int soldQty, int version)` để reset lại stock trước mỗi lần giả lập:
```java
public void resetStock(String itemId, int soldQty, int version) {
    FlashSaleItem item = getById(itemId);
    if (item != null) {
        item.setSoldQty(soldQty);
        item.setVersion(version);
        update(item);
    }
}
```

---

## Open Questions

> [!IMPORTANT]
> **Câu hỏi 1:** Chức năng giả lập chỉ dành cho Admin hay cho tất cả role? Hiện tại tôi đặt trong AdminView (option 17). Nếu bạn muốn nó là menu riêng (ví dụ option 4 trên MainMenuView), hãy cho tôi biết.

> [!IMPORTANT]
> **Câu hỏi 2:** Khi chạy giả lập, bạn muốn sử dụng FlashSaleItem hay Product? Hiện tại FlashSaleItem đã có field `version` sẵn (cần cho Optimistic Lock), còn Product thì chưa. Tôi dự kiến dùng **FlashSaleItem** cho giả lập. Đồng ý không?

## Verification Plan

### Manual Verification
1. Chạy Quick Simulation với 100 threads → Kiểm tra bảng kết quả hiện đúng
2. Chạy Full Experiment 1000 threads × 4 mechanisms × 3 lần → Kiểm tra dữ liệu trung bình
3. Kiểm tra file `data/transactions.csv` có ghi đúng các transaction
4. Kiểm tra `NO_LOCK` gây overselling, các lock khác giữ data consistent
5. Kiểm tra cột `vs Baseline` tính đúng phần trăm so với NO_LOCK

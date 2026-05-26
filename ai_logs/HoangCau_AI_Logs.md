# AI AUDIT LOG - DEV B

## I. AI USAGE SUMMARY

* **Total Prompts Used:** ~45
* **Core Prompts Logged:** 4
* **Selection Ratio:** ~9%
* **Hallucination Detected:** 2
* **AI Tools Used:** ChatGPT (Repository Design & Concurrency), DeepSeek (Lock Mechanism Comparison), VS Code AI Assistant (Debug Package Errors)

---

# II. CHI TIẾT CÁC CORE PROMPTS

---

## Entry #: 001

### Prompt Type:

DECISION-MAKING

### Stage/Component:

Abstraction & Repository Design

### Problem/Context:

Cần xây dựng Generic Repository cho hệ thống Food Delivery dùng CSV mà không bị duplicate code giữa MenuItemRepository, DriverRepository và OrderRepository.

### Prompt to AI (Nguyên văn):

*"Implement CsvRepository<T> generic interface for Java CSV project."*

### AI Response (Summary):

AI đề xuất tạo:

* Generic Repository Pattern
* CRUD methods:

  * findAll()
  * findById()
  * save()
  * update()
  * delete()

### Human Delta & Reflection:

* **Critical Thinking:** AI chỉ tạo CRUD cơ bản nhưng chưa tính đến vấn đề concurrency và thread safety khi nhiều thread cùng ghi file CSV.
* **Contextualization:** Repository trong đồ án không chỉ CRUD mà còn phải xử lý race condition.
* **Creative Synthesis:** Tôi mở rộng Repository để phục vụ synchronization mechanism và optimistic locking.
* **Decision Ownership:** Chốt kiến trúc `CsvRepository<T>` làm base abstraction cho toàn bộ Repository Layer.

### Evidence:

File:

* `CsvRepository.java`
* `AbstractMenuItemRepository.java`

---

## Entry #: 002

### Prompt Type:

PROBLEM-SOLVING

### Stage/Component:

Synchronization Mechanisms

### Problem/Context:

Cần implement cơ chế synchronized để ngăn nhiều thread cùng update stock gây oversell.

### Prompt to AI (Nguyên văn):

*"Implement synchronized repository for CSV stock update."*

### AI Response (Summary):

AI sử dụng:

```java
synchronized(this)
```

để khóa critical section.

### Human Delta & Reflection:

* **Critical Thinking (Hallucination Detected):** AI mắc lỗi concurrency phổ biến. `synchronized(this)` chỉ khóa theo object instance. Nếu tạo nhiều Repository object thì race condition vẫn xảy ra.
* **Contextualization:** Trong hệ thống thực tế, Repository có thể được khởi tạo nhiều lần.
* **Creative Synthesis:** Tôi thay đổi sang shared lock object:

```java
private static final Object LOCK
```

và sử dụng:

```java
synchronized(LOCK)
```

* **Decision Ownership:** Chốt dùng static shared lock để đảm bảo toàn bộ thread dùng chung critical section.

### Evidence:

File:

* `SynchronizedRepository.java`

---

## Entry #: 003

### Prompt Type:

VERIFICATION

### Stage/Component:

Concurrent File Access

### Problem/Context:

Cần kiểm tra tính an toàn khi nhiều thread đọc/ghi file CSV đồng thời bằng FileLock.

### Prompt to AI (Nguyên văn):

*"Implement OS-level FileLock for CSV repository."*

### AI Response (Summary):

AI sử dụng:

* RandomAccessFile
* FileChannel
* FileLock

và mở thêm luồng ghi riêng bằng FileOutputStream.

### Human Delta & Reflection:

* **Critical Thinking (Hallucination Detected):** AI bỏ qua vấn đề conflict giữa FileLock và FileOutputStream trên Windows.
* **Contextualization:** Máy chấm ở FPT University sử dụng Windows nên lỗi này sẽ gây IOException.
* **Creative Synthesis:** Tôi loại bỏ FileOutputStream riêng và thực hiện ghi trực tiếp bằng FileChannel.
* **Decision Ownership:** Chốt cơ chế FileLock chỉ dùng một luồng ghi duy nhất để tránh file corruption.

### Evidence:

File:

* `FileLockRepository.java`

---

## Entry #: 004

### Prompt Type:

PROBLEM-SOLVING

### Stage/Component:

Algorithms & Multi-thread Testing

### Problem/Context:

Cần mô phỏng nhiều customer threads cùng mua sản phẩm để kiểm tra race condition và oversell.

### Prompt to AI (Nguyên văn):

*"Create multi-thread stock deduction simulation using CountDownLatch."*

### AI Response (Summary):

AI tạo:

* ExecutorService
* CountDownLatch
* concurrent stock update test

### Human Delta & Reflection:

* **Critical Thinking:** AI chỉ test concurrent execution nhưng chưa verify kết quả cuối cùng của stock sau khi tất cả thread hoàn thành.

* **Contextualization:** Chỉ chạy thread chưa đủ, cần kiểm tra consistency của dữ liệu.

* **Creative Synthesis:** Tôi bổ sung:

  * stock verification
  * oversell detection
  * exception logging
  * thread-safe output

* **Decision Ownership:** Chốt dùng CountDownLatch để đảm bảo các thread thực sự chạy đồng thời nhằm tái hiện race condition thật.

### Evidence:

File:

* `LockTest.java`

---

# III. KẾT LUẬN & BÀI HỌC

## Kết luận nghiên cứu

Sau khi test:

* NO_LOCK gây oversell và race condition
* synchronized an toàn nhưng throughput giảm
* FileLock an toàn nhưng chậm
* Optimistic Locking đạt:

  * 0 race condition
  * throughput cao
  * scalability tốt

=> Optimistic Locking là cơ chế phù hợp nhất cho hệ thống Food Delivery sử dụng CSV.

---

# IV. REFLECTION

Qua quá trình làm việc với AI:

* AI hỗ trợ rất tốt:

  * boilerplate code
  * repository structure
  * synchronization examples
  * concurrency utilities

Tuy nhiên:

* AI vẫn mắc lỗi nguy hiểm trong concurrent programming
* đặc biệt ở:

  * synchronized scope
  * FileLock handling
  * critical section design

Bài học lớn nhất:

* Code concurrent không thể tin tưởng hoàn toàn vào AI output
* Phải tự test multi-thread thực tế
* Race condition thường chỉ xuất hiện khi hệ thống chạy đồng thời thật sự


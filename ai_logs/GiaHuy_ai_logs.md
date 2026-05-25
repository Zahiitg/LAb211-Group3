# AI AUDIT LOG - GIA HUY (DEV A)
*(Theo chuẩn RBL Insight Framework - FPT University)*

## I. AI USAGE SUMMARY
- **Total Prompts Used:** ~50
- **Core Prompts Logged:** 4 (Lọc ra những Prompt cốt lõi quyết định đến Kiến trúc và Quản lý dự án)
- **Selection Ratio:** ~8%
- **Hallucination Detected:** 1
- **AI Tools Used:** DeepSeek (Brainstorming kiến trúc & thuật toán), Antigravity (Tự động hóa sinh code & Audit lỗi Đa luồng), NotebookLM (Tổng hợp tài liệu dự án)

---

## II. CHI TIẾT CÁC CORE PROMPTS

### Entry #: 001
**Prompt Type:** DECISION-MAKING  
**Stage/Component:** Decomposition  
**Problem/Context:** Cần chia nhỏ khối lượng công việc của dự án E-commerce khổng lồ (10 tuần) cho nhóm 4 người sao cho công bằng và không dẫm chân lên nhau.  
**Prompt to AI (Nguyên văn):** *"Nhóm tôi gồm 4 sv, bạn có thể thiết kế 1 bảng phân chia công việc chuẩn nhất theo đề bài và lộ trình của 10 tuần không?"*  
**AI Response (Summary):** AI ban đầu đề xuất phân công theo chiều dọc (mỗi người làm full-stack một module), ví dụ Dev A làm chức năng Mua hàng từ A-Z, Dev B làm chức năng Login từ A-Z.  

**Human Delta & Reflection:**
- **Critical Thinking:** Việc phân công full-stack làm nhiều người sẽ phải sửa chung 1 file `FlashSaleItemRepository`, dễ dẫn đến "Merge Conflict" ác mộng trên Git và vi phạm ranh giới MVC.
- **Contextualization:** Đồ án này đánh giá rất nặng tính độc lập của các Layer (Model - View - Controller). Nếu trộn code sẽ bị trừ điểm kiến trúc.
- **Creative Synthesis:** Tôi bắt AI sửa lại (Decompose) bảng phân công theo chiều ngang (Layered): Dev A làm Data/Model, Dev B làm Repo/Lock, Dev C làm Controller, Dev D làm View.
- **Decision Ownership:** Chốt phương án phân chia theo Tầng (Layer). Đảm bảo không ai sửa chung file của nhau, tránh 100% Conflict Git.
**Evidence:** File `phan_cong_cong_viec.md` với ranh giới MVC rõ ràng.

---

### Entry #: 002
**Prompt Type:** VERIFICATION  
**Stage/Component:** Pattern Recognition  
**Problem/Context:** Sau khi code xong Tuần 3-4 (Tầng Data & Repository), cần rà soát lại các Pattern lỗi trước khi team ráp nối đa luồng.  
**Prompt to AI (Nguyên văn):** *"Tôi (dev A) đang dừng chân lại ở tuần 3-4 là được rồi. kiểm tra kỹ (kỹ tính và khắt khe) xem thử có tiềm ẩn rủi ro nào không?"*  
**AI Response (Summary):** AI báo code nhìn chung chạy ổn. Tuy nhiên có cảnh báo về cơ chế `FILE_LOCK` vì đang gọi `new FileOutputStream()` trong khi file bị lock bởi `RandomAccessFile`. AI tự tin bảo "Chạy trên Mac/Linux sẽ bình thường".  

**Human Delta & Reflection:**
- **Critical Thinking (Hallucination Detected):** AI bị lỗi *Oversimplification* (Bỏ qua môi trường OS thực tế). Sự thật là trên Windows, việc mở luồng ghi phụ khi đang có Exclusive FileLock sẽ văng lỗi `IOException` ngay lập tức. Đồ án chấm trên máy Windows của trường nên code này chắc chắn sẽ sập!
- **Contextualization:** Máy chấm thi ở phòng Lab của FPTU sử dụng HĐH Windows.
- **Creative Synthesis:** Tôi nhận diện mẫu (Pattern) lỗi kinh điển này của Java I/O. Quyết định tự chặn luồng ghi phụ `writeToFile()` của CsvRepository khi đang trong block FileLock.
- **Decision Ownership:** Yêu cầu Dev B phải đại phẫu thuật cơ chế `FILE_LOCK` bằng raw bytes. Dập tắt "bom hẹn giờ" trước khi dự án bước sang Tuần 8.
**Evidence:** File `audit_report.md` (Rủi ro số 1: Lỗi tử hình File Lock).

---

### Entry #: 003
**Prompt Type:** DECISION-MAKING  
**Stage/Component:** Abstraction  
**Problem/Context:** Cần xử lý logic giới hạn mua hàng (tối đa 2 sản phẩm/khách). Câu hỏi đặt ra là nên nhét đoạn code `if (soldQty <= limitedQty)` ở đâu trong kiến trúc MVC.  
**Prompt to AI (Nguyên văn):** *"Khi viết hàm placeOrder, làm sao để logic kiểm tra tồn kho không bị vi phạm nguyên tắc MVC?"* *(Trích từ thảo luận thiết kế Tuần 5)*  
**AI Response (Summary):** AI sinh code hàm `placeOrder` bên trong `OrderController` và chèn lệnh `if` kiểm tra số lượng mua trực tiếp tại Controller đó.  

**Human Delta & Reflection:**
- **Critical Thinking:** Code của AI chạy được nhưng vi phạm nghiêm trọng Abstraction của MVC. Nếu Controller nhúng tay vào logic kho, hệ thống sẽ bị giảng viên trừ ngay 5% điểm (như warning trong đề bài).
- **Contextualization:** Controller chỉ nên đóng vai trò Điều phối (Coordinator), không được chứa Logic Nghiệp vụ của Dữ liệu (Data Logic).
- **Creative Synthesis:** Tôi bác bỏ code của AI. Tôi trừu tượng hóa (Abstract) logic kiểm tra này và đẩy nó sâu xuống tầng Data bằng cách tạo hàm `validatePurchaseLimit()` bên trong `OrderRepository`.
- **Decision Ownership:** Chốt luồng gọi hàm: View -> Controller -> gọi hàm validate của Repo -> Repo tự quăng Exception nếu vi phạm. Chặn đứng hoàn toàn nguy cơ rò rỉ logic.
**Evidence:** Code `validatePurchaseLimit()` trong `OrderRepository.java` và bản thiết kế Tuần 5.

---

### Entry #: 004
**Prompt Type:** PROBLEM-SOLVING  
**Stage/Component:** Algorithms  
**Problem/Context:** Cần xây dựng Data Generator tạo 10.000 dòng dữ liệu ngẫu nhiên mà không làm sập bộ nhớ.  
**Prompt to AI (Nguyên văn):** *"Viết thuật toán sinh 10.000 dòng dữ liệu Customer, Product, Order ngẫu nhiên cho tôi."* *(Trích từ quá trình thiết kế Tuần 2)*  
**AI Response (Summary):** AI sinh ra một thuật toán sử dụng cấu trúc `ArrayList` khổng lồ chứa 10.000 Object trên RAM, rồi dùng `Files.write()` lưu tất cả 1 lần vào cuối chương trình.  

**Human Delta & Reflection:**
- **Critical Thinking:** Thuật toán mảng của AI là *Naive Algorithm* (ngây thơ). Nó chạy được với 1000 dòng, nhưng nếu nâng lên 100.000 dòng sẽ lập tức văng `OutOfMemoryError` vì tràn RAM.
- **Contextualization:** Phần mềm phải chạy ổn định trên các máy trạm có bộ nhớ khiêm tốn. Việc tối ưu I/O là bắt buộc.
- **Creative Synthesis:** Tôi tự thiết kế lại thuật toán theo cơ chế Stream. Khởi tạo `BufferedWriter`, sinh object nào ghi luôn xuống đĩa bằng `toCsvLine()` và gọi `newLine()`.
- **Decision Ownership:** Loại bỏ hoàn toàn mảng tạm. Dùng thuật toán I/O trực tiếp để RAM luôn ở mức ổn định dù sinh 1 triệu dòng data.
**Evidence:** Code vòng lặp ghi file dùng `BufferedWriter` trong `DataGenerator.java`.

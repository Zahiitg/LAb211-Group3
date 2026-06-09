# 🛒 E-Commerce Flash Sale Simulation – Hệ thống Ngăn chặn Âm kho

**Đồ án môn học LAB211 (OOP with Java) — Nhóm 3**

## 🎯 1. RESEARCH QUESTION (Câu hỏi Nghiên cứu)
> *"Khi nhiều luồng xử lý đơn hàng đồng thời trên cùng một file CSV kho hàng — race condition nào xảy ra, và kỹ thuật đồng bộ hóa nào ngăn được tình trạng âm kho mà không làm throughput (tốc độ xử lý) giảm quá 30%?"*

Dự án này không nhằm mục đích xây dựng một website bán hàng thông thường, mà đi sâu vào việc giải quyết bài toán cốt lõi của các hệ thống Thương mại điện tử lớn (Shopee, Lazada): **Race Condition gây Âm kho (Overselling)** trong đợt Flash Sale.

## 🚀 2. TÍNH NĂNG NỔI BẬT & CƠ CHẾ ĐỒNG BỘ
Để trả lời Research Question, hệ thống tích hợp sẵn một **Simulator Tool** (sử dụng `CountDownLatch` và `ExecutorService`) để giả lập hàng trăm luồng (threads) tranh cướp mua hàng cùng lúc. Kết quả sẽ được so sánh trên 4 cơ chế:

1. `NO_LOCK`: Không sử dụng cơ chế đồng bộ (Mô phỏng lỗi Âm kho làm Baseline).
2. `SYNCHRONIZED`: Khóa đồng bộ ở cấp độ luồng (Java Thread-level Lock).
3. `FILE_LOCK`: Khóa bi quan ở cấp độ Hệ điều hành (Pessimistic Lock dùng `FileChannel.lock()`).
4. `OPTIMISTIC`: Khóa lạc quan dựa trên đối chiếu phiên bản thông qua biến `version` (Compare-And-Swap).

Hệ thống tự động đo lường và xuất báo cáo thống kê về **Tỷ lệ Âm kho (%)** và **Tốc độ xử lý (TPS - Transactions Per Second)** để tìm ra điểm cân bằng tốt nhất giữa Sự an toàn và Hiệu năng.

## ⚙️ 3. KIẾN TRÚC & CÔNG NGHỆ
- **Kiến trúc:** 100% chuẩn MVC (Model - View - Controller). Logic trừ kho và validate hoàn toàn bị cô lập tại tầng `Repository` & `Model`, tuyệt đối không xử lý ở `View` hay `Controller`.
- **Cấu trúc Dữ liệu:** Lưu trữ thuần bằng File Text (CSV) cho 7 Entities: `Customer`, `Product`, `FlashSaleEvent`, `FlashSaleItem`, `Order`, `OrderDetail`, `OrderTransaction`. (Đáp ứng yêu cầu I/O khắc nghiệt).
- **Ngôn ngữ & Thư viện:** Java Core (JDK 8+), không sử dụng bất kỳ thư viện bên thứ ba nào (Pure Java) để chứng minh năng lực hiểu sâu thuật toán lõi.

## 👨‍💻 4. THÀNH VIÊN NHÓM (GROUP 3)
| STT | Họ và Tên | MSSV |
|-----|-----------|------|
| 1   | Truong Gia Huy | QE190139 |
| 2   | Lê Hoàng Cầu | QE200098 |
| 3   | Mai Hoàng  Đăng | QE190050 |
| 4   | Nguyễn Thành Danh | QE200138 |

---

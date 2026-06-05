# Database Schema

Dựa trên Class Diagram và bản cập nhật Auditable Entity mới nhất, dưới đây là thiết kế Schema chi tiết cho 7 file CSV, bao gồm các cột, kiểu dữ liệu, và Khóa ngoại (Foreign Keys - FK) để liên kết dữ liệu giữa các file:

> **Lưu ý quan trọng**: Tất cả các file CSV (trừ file chứa log/transaction nếu có thiết kế riêng) đều kế thừa từ `BaseEntity` nên bắt buộc có 3 cột đầu tiên là: `id`, `createdAt`, `updatedAt`.

---

## 1. File `customers.csv`
Lưu trữ thông tin chi tiết của khách hàng.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian tạo tài khoản |
| `updatedAt` | String (ISO) | Thời gian cập nhật tài khoản gần nhất |
| `fullName` | String | Họ và tên đầy đủ |
| `phone` | String | Số điện thoại liên lạc |
| `email` | String | Địa chỉ Email |
| `tier` | String (Enum) | Hạng thành viên (NORMAL, VIP, PREMIUM) |
| `totalSpent` | Double | Tổng tiền đã chi tiêu |
| `active` | Boolean | Trạng thái hoạt động (true/false) |

## 2. File `products.csv`
Lưu trữ thông tin cơ bản của tất cả sản phẩm trong hệ thống (Kho tổng).
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian tạo |
| `updatedAt` | String (ISO) | Thời gian cập nhật gần nhất |
| `name` | String | Tên sản phẩm |
| `category` | String | Danh mục sản phẩm |
| `price` | Double | Giá gốc của sản phẩm |
| `stock` | Int | Số lượng tồn kho vật lý hiện tại |

## 3. File `flash_events.csv`
Lưu trữ thông tin về các đợt sự kiện Flash Sale.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian tạo sự kiện |
| `updatedAt` | String (ISO) | Thời gian cập nhật gần nhất |
| `eventName` | String | Tên đợt sale (VD: "Siêu Sale 11/11") |
| `startTime` | String (ISO) | Thời gian bắt đầu mở bán |
| `endTime` | String (ISO) | Thời gian kết thúc |
| `status` | String (Enum) | Trạng thái (UPCOMING, ONGOING, ENDED, DISABLED) |

## 4. File `flash_items.csv`
Lưu trữ các sản phẩm cụ thể được bán trong một đợt Flash Sale.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian đưa vào Flash Sale |
| `updatedAt` | String (ISO) | Thời gian cập nhật (thường là lúc có người mua) |
| `eventId` | String | **FK** -> Trỏ tới `id` trong `flash_events.csv` |
| `productId` | String | **FK** -> Trỏ tới `id` trong `products.csv` |
| `flashPrice` | Double | Giá bán trong đợt Sale (sau khi giảm) |
| `limitedQty` | Int | Số lượng giới hạn bán trong đợt Sale này |
| `soldQty` | Int | Số lượng đã bán được |
| `discountPercent`| Int | Phần trăm giảm giá (VD: 10, 20...) |
| `version` | Int | Phiên bản (Optimistic Lock) |
| `status` | String (Enum) | Trạng thái của mặt hàng sale này |

## 5. File `orders.csv`
Lưu trữ thông tin tổng quan của các đơn hàng.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian đặt hàng |
| `updatedAt` | String (ISO) | Thời gian cập nhật trạng thái đơn |
| `customerId` | String | **FK** -> Trỏ tới `id` trong `customers.csv` |
| `eventId` | String | **FK** -> Trỏ tới `id` trong `flash_events.csv` |
| `totalAmount` | Double | Tổng tiền của cả đơn hàng |
| `status` | String (Enum) | Trạng thái (PENDING, SUCCESS, CANCELLED...) |
| `lockMechanism`| String (Enum) | Cơ chế lock đã dùng cho đơn này |

## 6. File `order_details.csv`
Lưu trữ chi tiết các mặt hàng được mua trong một đơn hàng.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian tạo chi tiết |
| `updatedAt` | String (ISO) | Thời gian cập nhật |
| `orderId` | String | **FK** -> Trỏ tới `id` trong `orders.csv` |
| `flashSaleItemId`| String | **FK** -> Trỏ tới `id` trong `flash_items.csv` |
| `quantity` | Int | Số lượng mua |
| `unitPrice` | Double | Đơn giá lúc mua (= flashPrice) |
| `subTotal` | Double | Thành tiền (= quantity * unitPrice) |

## 7. File `transactions.csv`
Lưu vết các giao dịch xử lý đơn hàng và cơ chế đồng bộ.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK**, kế thừa từ BaseEntity |
| `createdAt` | String (ISO) | Thời gian tạo log |
| `updatedAt` | String (ISO) | Thời gian cập nhật log |
| `orderId` | String | **FK** -> Trỏ tới `id` trong `orders.csv` |
| `lockMechanism`| String (Enum) | Cơ chế Lock |
| `retryCount` | Int | Số lần thử lại |
| `processingTimeMs`| Long | Thời gian xử lý (ms) |
| `success` | Boolean | Thành công hay thất bại |

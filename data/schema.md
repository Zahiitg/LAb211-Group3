# Database Schema

Dựa trên Class Diagram, dưới đây là thiết kế Schema chi tiết cho 6 file CSV, bao gồm các cột, kiểu dữ liệu, và Khóa ngoại (Foreign Keys - FK) để liên kết dữ liệu giữa các file:

## 1. File `customers.csv`
Lưu trữ thông tin khách hàng.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK** (Khóa chính), kế thừa từ BaseEntity |
| `name` | String | Tên khách hàng |
| `email` | String | Email (nên là duy nhất - Unique) |
| `tier` | String (Enum) | Hạng thành viên (VD: BRONZE, SILVER, GOLD...) |

## 2. File `products.csv`
Lưu trữ thông tin cơ bản của tất cả sản phẩm trong hệ thống.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK** (Khóa chính), kế thừa từ BaseEntity |
| `name` | String | Tên sản phẩm |
| `category` | String | Danh mục sản phẩm |
| `price` | Double | Giá gốc của sản phẩm |
| `stock` | Int | Số lượng tồn kho hiện tại |

## 3. File `flash_sale_events.csv`
Lưu trữ thông tin về các đợt sự kiện Flash Sale.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK** (Khóa chính), kế thừa từ BaseEntity |
| `name` | String | Tên sự kiện (VD: "Siêu Sale 11/11") |
| `start_time` | String (ISO 8601)| Thời gian bắt đầu (VD: `2026-11-11T00:00:00`) |
| `end_time` | String (ISO 8601)| Thời gian kết thúc |
| `status` | String (Enum) | Trạng thái sự kiện (VD: UPCOMING, ACTIVE, ENDED) |

## 4. File `flash_sale_items.csv`
Lưu trữ các sản phẩm cụ thể được bán trong một đợt Flash Sale. File này đóng vai trò như bảng trung gian nhiều-nhiều giữa Product và Event.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK** (Khóa chính), kế thừa từ BaseEntity |
| `product_id` | String | **FK** -> Trỏ tới `id` trong `products.csv` |
| `event_id` | String | **FK** -> Trỏ tới `id` trong `flash_sale_events.csv` |
| `limited_qty` | Int | Số lượng giới hạn bán trong đợt Sale này |
| `sold_qty` | Int | Số lượng đã bán được (dùng để check hết hàng) |
| `version` | Int | Phiên bản (dùng cho cơ chế Optimistic Lock chống race-condition) |

## 5. File `orders.csv`
Lưu trữ thông tin tổng quan của các đơn hàng.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK** (Khóa chính), kế thừa từ BaseEntity |
| `customer_id` | String | **FK** -> Trỏ tới `id` trong `customers.csv` |
| `order_time` | String (ISO 8601)| Thời gian đặt hàng |
| `status` | String (Enum) | Trạng thái đơn (VD: PENDING, SUCCESS, CANCELLED) |

## 6. File `order_details.csv`
Lưu trữ chi tiết các mặt hàng được mua trong một đơn hàng.
| Column Name | Data Type | Constraint/Description |
| :--- | :--- | :--- |
| `id` | String | **PK** (Khóa chính), kế thừa từ BaseEntity |
| `order_id` | String | **FK** -> Trỏ tới `id` trong `orders.csv` |
| `flash_sale_item_id` | String | **FK** -> Trỏ tới `id` trong `flash_sale_items.csv` |
| `quantity` | Int | Số lượng mua của mặt hàng này |

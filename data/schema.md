# Cấu trúc cơ sở dữ liệu (Database Schema)

## customers.csv
- `id` (Chuỗi): Mã định danh duy nhất của khách hàng.
- `name` (Chuỗi): Tên khách hàng.
- `email` (Chuỗi): Địa chỉ email của khách hàng.
- `tier` (Chuỗi): Cấp độ thành viên của khách hàng (ví dụ: NORMAL, PREMIUM, VIP).

## flash_events.csv
- `id` (Chuỗi): Mã định danh duy nhất của sự kiện flash sale.
- `name` (Chuỗi): Tên của sự kiện flash sale.
- `startTime` (Ngày giờ): Thời gian bắt đầu sự kiện.
- `endTime` (Ngày giờ): Thời gian kết thúc sự kiện.

## flash_items.csv
- `id` (Chuỗi): Mã định danh duy nhất của sản phẩm trong flash sale.
- `productId` (Chuỗi): Tham chiếu đến mã sản phẩm ID (`products.csv`).
- `eventId` (Chuỗi): Tham chiếu đến mã sự kiện flash sale ID (`flash_events.csv`).
- `limitedQty` (Số nguyên): Tổng số lượng giới hạn dành cho flash sale.
- `soldQty` (Số nguyên): Số lượng đã bán.
- `version` (Số nguyên): Phiên bản dùng cho khóa lạc quan (Optimistic locking).

## order_details.csv
- `id` (Chuỗi): Mã định danh duy nhất của chi tiết đơn hàng.
- `orderId` (Chuỗi): Tham chiếu đến mã đơn hàng ID (`orders.csv`).
- `flashSaleItemId` (Chuỗi): Tham chiếu đến mã sản phẩm flash sale ID (`flash_items.csv`).
- `quantity` (Số nguyên): Số lượng đặt mua.

## orders.csv
- `id` (Chuỗi): Mã định danh duy nhất của đơn hàng.
- `customerId` (Chuỗi): Tham chiếu đến mã khách hàng ID (`customers.csv`).
- `orderTime` (Ngày giờ): Dấu thời gian khi đơn hàng được đặt.
- `status` (Chuỗi): Trạng thái hiện tại của đơn hàng (ví dụ: PENDING).

## products.csv
- `id` (Chuỗi): Mã định danh duy nhất của sản phẩm.
- `name` (Chuỗi): Tên sản phẩm.
- `category` (Chuỗi): Danh mục của sản phẩm.
- `price` (Số thực): Giá của sản phẩm.
- `stock` (Số nguyên): Số lượng tồn kho hiện tại.

## transactions.csv
- `id` (Chuỗi): Mã định danh duy nhất của giao dịch.
- `orderId` (Chuỗi): Tham chiếu đến mã đơn hàng ID.
- `lockMechanism` (Chuỗi): Loại cơ chế khóa được sử dụng.
- `retryCount` (Số nguyên): Số lần thử lại.
- `processingTimeMs` (Số nguyên): Thời gian xử lý tính bằng mili-giây.
- `success` (Đúng/Sai): Giao dịch có thành công hay không.

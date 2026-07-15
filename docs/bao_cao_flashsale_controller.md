# Bao cao chi tiet FlashSaleController

Ngay lap: 2026-06-26

Pham vi bao cao: `src/controller/FlashSaleController.java` va cac lop lien quan truc tiep gom:

- `src/model/FlashSaleEvent.java`
- `src/model/FlashSaleItem.java`
- `src/model/enums/SaleStatus.java`
- `src/repository/FlashSaleEventRepository.java`
- `src/repository/FlashSaleItemRepository.java`
- `src/repository/ProductRepository.java`
- Cac view/controller goi vao Flash Sale: `AdminView`, `SellerView`, `CustomerView`, `GuestView`, `AdminController`, `SellerController`

## 1. Vai tro cua FlashSaleController

`FlashSaleController` nam trong package `controller` va `extends BaseController`.

Nhiem vu chinh:

- Quan ly su kien Flash Sale: xem danh sach, tao su kien, kich hoat, ket thuc.
- Quan ly san pham tham gia Flash Sale: seller dang ky san pham vao mot su kien.
- Cung cap danh sach item theo event cho Customer/Guest xem va cho Customer them vao gio hang.
- Lam cau noi giua tang View/Controller khac voi cac repository CSV: `flash_events.csv`, `flash_items.csv`, `products.csv`.

Controller nay khong xu ly viec dat hang truc tiep. Viec tru kho va tao don hang thuc hien o `CustomerController.checkoutCart()` hoac `OrderController`, thong qua `FlashSaleItemRepository` va `ProductRepository`.

## 2. Vi tri trong kien truc MVC

Luồng tong quat:

```text
View
  -> AdminController/SellerController hoac goi FlashSaleController truc tiep
  -> FlashSaleController
  -> Repository
  -> CSV file
```

Vai tro theo tang:

- View: hien menu, nhan input, in ket qua.
- Controller: validate nghiep vu va dieu phoi repository.
- Repository: doc/ghi CSV, tim kiem, cap nhat cache.
- Model: bieu dien `FlashSaleEvent`, `FlashSaleItem`, parse/generate CSV.

## 3. Thanh phan phu thuoc

Trong `FlashSaleController` co 3 repository:

```java
private final FlashSaleEventRepository eventRepo;
private final FlashSaleItemRepository itemRepo;
private final ProductRepository productRepo;
```

Y nghia:

- `eventRepo`: doc/ghi `data/flash_events.csv`, quan ly `FlashSaleEvent`.
- `itemRepo`: doc/ghi `data/flash_items.csv`, quan ly `FlashSaleItem`.
- `productRepo`: doc `data/products.csv`, kiem tra san pham goc, gia goc, seller so huu.

Constructor mac dinh lay repository tu `AuthenticationState.getInstance()`, nen app console dung chung cache/repository voi cac controller khac.

## 4. Constructor

### 4.1. `FlashSaleController()`

Dung trong ung dung that.

```java
public FlashSaleController() {
    AuthenticationState authState = AuthenticationState.getInstance();
    this.eventRepo = authState.getFlashSaleEventRepo();
    this.itemRepo = authState.getFlashSaleItemRepo();
    this.productRepo = authState.getProductRepo();
}
```

Y nghia:

- Dung chung repository singleton cua he thong.
- Dam bao Admin, Seller, Customer, Guest cung nhin vao cung mot nguon du lieu.
- Du lieu mac dinh nam trong:
  - `data/flash_events.csv`
  - `data/flash_items.csv`
  - `data/products.csv`

### 4.2. `FlashSaleController(String filePath)`

Dung cho testing hoac can file event rieng.

- `eventRepo` duoc tao moi tu `filePath`.
- `itemRepo` va `productRepo` van lay tu `AuthenticationState`.

### 4.3. `FlashSaleController(String eventFilePath, String itemFilePath)`

Dung cho unit test doc lap ca event va item.

- `eventRepo` doc tu file test rieng.
- `itemRepo` doc tu file test rieng.
- `productRepo` van dung repository chung tu `AuthenticationState`.

## 5. Phan tich tung method

### 5.1. `getAllEvents()`

Chuc nang:

- Lay toan bo danh sach Flash Sale Event.
- Tra ve `ControllerResult.success`.
- Neu khong co event, van tra success voi list rong.

Luong xu ly:

```text
eventRepo.getAll()
  -> neu list rong: success("Hien tai chua co su kien...", events)
  -> neu co data: success("Tim thay N su kien...", events)
```

Dau ra:

- `success = true`
- `data = List<FlashSaleEvent>`

Noi goi:

- `AdminController.listFlashSaleEvents()`
- `CustomerView.listFlashSaleEvents()`
- `CustomerView.browseAndBuyFlashSale()`
- `GuestView.browseFlashSaleFlow()`
- `SellerController.getOngoingFlashSaleEvents()`

Nhan xet:

- Method nay khong yeu cau dang nhap, hop ly vi Guest/Customer cung can xem su kien.
- View tu loc event `ONGOING` khi can hien Flash Sale dang dien ra.

### 5.2. `createEvent(String name, int durationDays)`

Chuc nang:

- Tao mot su kien Flash Sale moi.
- Duoc Admin goi thong qua `AdminController.createFlashSaleEvent()`.

Dieu kien validate:

- `name` khong duoc null/rong.
- `durationDays > 0`.

Luong tao ID:

```text
maxNum = 0
duyet tat ca event
  neu id bat dau bang "E"
    lay phan so sau "E"
    cap nhat maxNum
newId = E%05d(maxNum + 1)
```

Vi du:

- Da co `E00001`, `E00002`
- Event moi se la `E00003`

Luong tao thoi gian:

```java
start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
end = start.plusDays(durationDays);
status = SaleStatus.UPCOMING;
```

Dau ra khi thanh cong:

- Tao `FlashSaleEvent(newId, name, start, end, UPCOMING)`
- Ghi vao `eventRepo.add(newEvent)`
- Tra `ControllerResult.success(..., newEvent)`

Nhan xet nghiep vu:

- Event moi luon bat dau tu thoi diem tao, nhung status lai la `UPCOMING`.
- Khong co input chon ngay bat dau/ket thuc.
- Khong kiem tra trung ten su kien.
- Khong kiem tra su kien trung thoi gian voi event khac.

Rui ro code hien tai:

- `FlashSaleController` khong goi `requireAdmin()` truc tiep. Viec phan quyen dang nam o `AdminController`.
- Neu class khac goi truc tiep `createEvent()`, no co the tao event ma khong can Admin.

### 5.3. `registerItem(String eventId, String productId, double salePrice, int limitedQty, String sellerId)`

Chuc nang:

- Cho Seller dang ky san pham cua minh vao mot su kien Flash Sale.
- Duoc goi qua `SellerController.registerFlashSaleItem()`.

Luong validate:

```text
1. Tim event theo eventId
   - khong thay: loi "Khong tim thay su kien Flash Sale!"

2. Kiem tra status event
   - chi chap nhan ONGOING hoac UPCOMING
   - ENDED/DISABLED bi tu choi

3. Tim product theo productId
   - khong thay: loi "Khong tim thay san pham!"

4. Kiem tra quyen so huu
   - p.getSellerId() phai bang sellerId
   - sai seller: loi "Ban chi duoc dang ky san pham cua chinh minh!"

5. Kiem tra gia sale
   - salePrice > 0
   - salePrice < gia goc

6. Kiem tra so luong gioi han
   - limitedQty > 0
   - limitedQty <= ton kho san pham goc
```

Luong tao FlashSaleItem:

```text
maxNum = so lon nhat trong cac id bat dau bang "FI"
newId = FI%05d(maxNum + 1)
item = new FlashSaleItem(newId, productId, eventId, salePrice, limitedQty, 0, 1)
itemRepo.add(item)
```

Gia tri khoi tao:

- `soldQty = 0`
- `version = 1`

Dau ra thanh cong:

- `ControllerResult.success("Da dang ky san pham vao Flash Sale thanh cong!", item)`

Nhan xet nghiep vu:

- Method nay dam bao Seller chi dang ky san pham cua minh.
- Gia Flash Sale bat buoc nho hon gia goc.
- So luong gioi han khong duoc vuot ton kho vat ly cua product.
- Chua tru/reserve ton kho san pham goc tai thoi diem dang ky. Ton kho chi bi tru khi Customer checkout.

Rui ro code hien tai:

- Khong trim `eventId`, `productId`, `sellerId`.
- Khong chan mot product dang ky trung vao cung event.
- Khong co rule gioi han moi seller duoc dang ky bao nhieu item.
- Khong kiem tra `event.startTime/endTime` theo thoi gian thuc, chi kiem tra `status`.
- Khong goi `requireSeller()` truc tiep trong controller nay, ma dua vao `SellerController`.

### 5.4. `listItems(String eventId)`

Chuc nang:

- Lay danh sach cac mat hang Flash Sale thuoc mot event.

Luong validate:

```text
eventId null/rong -> error
trim eventId -> normalizedId
eventRepo.getById(normalizedId) null -> error
itemRepo.findItemsByEventId(normalizedId) -> success voi List<FlashSaleItem>
```

Dau ra:

- Thanh cong: `data = List<FlashSaleItem>`
- That bai: `data = null`

Noi goi:

- `getItemsByEventId(eventId)`
- `CustomerView.browseAndBuyFlashSale()`
- `GuestView.browseFlashSaleFlow()`
- Unit test `FlashSaleControllerJUnitTest`

Nhan xet:

- Viec kiem tra event ton tai giup tranh lay item cua event khong hop le.
- Method khong loc theo status event. Neu caller truyen event `ENDED`, method van tra item neu event ton tai.
- Customer/Guest View hien tai da loc event `ONGOING` truoc khi goi, nen UI binh thuong khong hien item cua event da ket thuc.

### 5.5. `getItemsByEventId(String eventId)`

Chuc nang:

- API alias giu tuong thich nguoc.
- Goi thang `listItems(eventId)`.

Y nghia:

- Giu cho cac View cu khong bi loi khi ten method thay doi.
- Khong co logic rieng.

### 5.6. `startEvent(String eventId)`

Chuc nang:

- Chuyen event sang `SaleStatus.ONGOING`.
- Duoc Admin goi thong qua `AdminController.startFlashSaleEvent()`.

Luong xu ly:

```text
startEvent(eventId)
  -> updateEventStatus(eventId, ONGOING, "kich hoat")
```

Ket qua:

- Neu hop le: update event status va ghi CSV.
- Neu event da `ONGOING`: bao loi.

Rui ro:

- Co the start event tu trang thai `ENDED` hoac `DISABLED`, vi code chi chan khi status da bang target.
- Khong kiem tra `startTime/endTime` voi thoi gian hien tai.

### 5.7. `endEvent(String eventId)`

Chuc nang:

- Chuyen event sang `SaleStatus.ENDED`.
- Duoc Admin goi thong qua `AdminController.endFlashSaleEvent()`.

Luong xu ly:

```text
endEvent(eventId)
  -> updateEventStatus(eventId, ENDED, "ket thuc")
```

Ket qua:

- Neu hop le: update event status va ghi CSV.
- Neu event da `ENDED`: bao loi.

Rui ro:

- Co the end event tu `UPCOMING` ma chua tung `ONGOING`.
- Khong co trang thai `DISABLED` trong flow UI/controller, du enum co khai bao.

### 5.8. `updateEventStatus(String eventId, SaleStatus targetStatus, String action)`

Day la private helper dung chung cho `startEvent()` va `endEvent()`.

Luong validate:

```text
eventId null/rong -> error
normalizedId = eventId.trim()
eventRepo.getById(normalizedId) null -> error
event.status == targetStatus -> error
event.setStatus(targetStatus)
eventRepo.update(event)
success
```

Diem tot:

- Gom logic update status vao mot noi, tranh lap code.
- Co trim `eventId`.
- Co chan thao tac trung trang thai.

Diem thieu:

- Chua co state machine chat che.
- Chua co rule hop le nhu:
  - `UPCOMING -> ONGOING`
  - `ONGOING -> ENDED`
  - khong cho `ENDED -> ONGOING`
  - khong cho `DISABLED -> ONGOING`

### 5.9. `getProductById(String productId)`

Chuc nang:

- Lay product goc theo ID.
- Dung de View hien ten san pham, gia goc, tinh phan tram giam gia.

Noi goi:

- `CustomerView.browseAndBuyFlashSale()`
- `GuestView.browseFlashSaleFlow()`

Nhan xet:

- Method tra ve `Product` hoac null.
- Khong validate null/rong, goi truc tiep `productRepo.getById(productId)`.

### 5.10. `getEventRepo()`

Chuc nang:

- Tra ve `FlashSaleEventRepository`.
- Dung trong `AdminController.getDashboardStats()` va unit test.

Nhan xet:

- Tien cho test/thong ke.
- Nhung viec expose repository truc tiep lam tang coupling, class khac co the doc/ghi event bo qua controller.

## 6. Luong hoat dong theo vai tro

### 6.1. Admin quan ly Flash Sale

Menu:

```text
AdminView.manageFlashSale()
  1. Xem tat ca su kien
  2. Tao su kien Flash Sale moi
  3. Kich hoat su kien
  4. Ket thuc su kien
```

Luong:

```text
AdminView
  -> AdminController
  -> FlashSaleController
  -> FlashSaleEventRepository
  -> data/flash_events.csv
```

Method lien quan:

- `listFlashSaleEvents()` -> `getAllEvents()`
- `createFlashSaleEvent()` -> `createEvent()`
- `startFlashSaleEvent()` -> `startEvent()`
- `endFlashSaleEvent()` -> `endEvent()`

Phan quyen:

- `AdminController` co goi `requireAdmin()`.
- `FlashSaleController` khong tu kiem tra Admin.

### 6.2. Seller dang ky san pham vao Flash Sale

Menu:

```text
SellerView.manageFlashSale()
  1. Xem cac su kien dang dien ra
  2. Dang ky san pham vao Flash Sale
```

Luong xem event dang dien ra:

```text
SellerView.displayOngoingEvents()
  -> SellerController.getOngoingFlashSaleEvents()
  -> FlashSaleController.getAllEvents()
  -> loc SaleStatus.ONGOING tai SellerController
```

Luong dang ky item:

```text
SellerView.registerFlashSaleItem()
  -> SellerController.registerFlashSaleItem(eventId, productId, salePrice, qty)
  -> FlashSaleController.registerItem(eventId, productId, salePrice, limitedQty, sellerId)
  -> FlashSaleItemRepository.add()
  -> data/flash_items.csv
```

Phan quyen:

- `SellerController` co goi `requireSeller()`.
- `FlashSaleController.registerItem()` khong tu goi `requireSeller()`.

### 6.3. Customer xem va mua Flash Sale

Menu:

```text
CustomerView
  4. Xem danh sach va Dat mua Flash Sale
```

Luong:

```text
CustomerView.browseAndBuyFlashSale()
  -> flashSaleController.getAllEvents()
  -> loc event status ONGOING
  -> flashSaleController.getItemsByEventId(eventId)
  -> flashSaleController.getProductById(productId)
  -> customerController.addToCart(itemId, qty)
```

Luu y:

- FlashSaleController chi cung cap event/item/product.
- Them gio hang va checkout nam trong `CustomerController`.
- Khi checkout, ton kho flash sale va product goc moi bi tru bang optimistic lock.

### 6.4. Guest xem Flash Sale

Menu:

```text
GuestView
  3. Xem danh sach Flash Sale
```

Luong:

```text
GuestView.browseFlashSaleFlow()
  -> flashSaleController.getAllEvents()
  -> loc ONGOING
  -> flashSaleController.getItemsByEventId(eventId)
  -> flashSaleController.getProductById(productId)
```

Guest chi duoc xem. Neu chon them gio hang/mua ngay, View hien thong bao yeu cau dang ky/dang nhap.

## 7. Model va CSV lien quan

### 7.1. `FlashSaleEvent`

Thuoc tinh:

- `id`
- `name`
- `startTime`
- `endTime`
- `status`

CSV thuc te:

```text
id,name,startTime,endTime,status
```

Status enum:

```text
UPCOMING, ONGOING, ENDED, DISABLED
```

### 7.2. `FlashSaleItem`

Thuoc tinh:

- `id`
- `productId`
- `eventId`
- `salePrice`
- `limitedQty`
- `soldQty`
- `version`

CSV thuc te:

```text
id,productId,eventId,salePrice,limitedQty,soldQty,version
```

Y nghia:

- `limitedQty`: so luong gioi han ban trong event.
- `soldQty`: so luong da ban trong event.
- `version`: dung cho optimistic lock khi tru kho.

## 8. Lien quan den co che chong overselling

`FlashSaleController` khong truc tiep trien khai lock. Lock nam trong `FlashSaleItemRepository`:

- `sellWithNoLock()`
- `sellWithSynchronized()`
- `sellWithFileLock()`
- `sellWithOptimisticLock()`

Trong luong mua hang hien tai cua Customer:

```text
CustomerController.checkoutCart()
  -> flashSaleItemRepo.sellWithOptimisticLock(itemId, qty)
  -> productRepo.sellWithOptimisticLock(productId, qty)
  -> tao Order va OrderDetail
```

Nghia la FlashSaleController phu trach setup Flash Sale, con viec tranh overselling khi dat hang nam o repository/order flow.

## 9. Test coverage hien co

File test lien quan truc tiep:

- `src/test/FlashSaleControllerJUnitTest.java`
- `src/test/FlashSaleRepositoryJUnitTest.java`
- `src/test/FlashSaleEngineJUnitTest.java`

Trong `AllTests`, `FlashSaleControllerJUnitTest` da duoc dua vao suite.

Nhom test dang bao phu:

- Lay item theo event.
- Event khong ton tai thi tra loi.
- Start event chuyen sang `ONGOING`.
- End event chuyen sang `ENDED`.
- Input rong/null khi start/end.
- Goi start lap lai thi bi loi.

Ket qua chay trong phien lam viec:

```text
javac -encoding UTF-8 -cp ".;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" -d build\classes ...
java -cp "build\classes;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore test.AllTests test.SellerControllerJUnitTest

OK (128 tests)
```

Luu y khi compile tren Windows:

- Can them `-encoding UTF-8`.
- Neu khong them, `javac` co the loi `unmappable character` do comment tieng Viet trong source.

## 10. Diem manh

- Controller gon, dung dung vai tro dieu phoi nghiep vu Flash Sale.
- Tra ve `ControllerResult` thong nhat voi toan he thong.
- Co constructor rieng cho test, giup unit test de tach file CSV.
- Co validate can thiet khi Seller dang ky item: event ton tai, product ton tai, dung chu so huu, gia sale hop le, so luong gioi han hop le.
- Co alias `getItemsByEventId()` de giu tuong thich voi View.
- Chia status update qua helper `updateEventStatus()`, tranh lap code.

## 11. Van de va rui ro can luu y

### 11.1. Phan quyen nam o controller bao ngoai

`FlashSaleController` khong goi:

- `requireAdmin()` trong `createEvent/startEvent/endEvent`
- `requireSeller()` trong `registerItem`

Hien tai UI di qua `AdminController` va `SellerController` nen van co guard. Nhung neu class khac goi truc tiep `FlashSaleController`, co the bo qua phan quyen.

Khuyen nghi:

- Them guard truc tiep vao method quan tri.
- Hoac bien `FlashSaleController` thanh service noi bo chi duoc goi tu controller co guard, va dat ten/phan tang ro hon.

### 11.2. Chua co state machine chat che cho event status

Hien tai:

- `startEvent()` co the chuyen `ENDED -> ONGOING`.
- `endEvent()` co the chuyen `UPCOMING -> ENDED`.
- `DISABLED` co trong enum nhung chua co luong su dung.

Khuyen nghi:

```text
UPCOMING -> ONGOING -> ENDED
UPCOMING/ONGOING -> DISABLED neu admin huy
ENDED khong duoc quay lai ONGOING
DISABLED khong duoc start lai neu khong co rule rieng
```

### 11.3. Tao event chua linh hoat ve thoi gian

`createEvent()` tu dat:

- `startTime = now`
- `endTime = now + durationDays`
- `status = UPCOMING`

Dieu nay co the gay mau thuan nho: thoi gian bat dau la hien tai nhung status la `UPCOMING`.

Khuyen nghi:

- Cho Admin nhap start/end time.
- Hoac neu start la hien tai thi status nen co the la `ONGOING`.
- Hoac scheduler/logic rieng tu dong chuyen status theo thoi gian.

### 11.4. Chua chan dang ky trung san pham trong cung event

Seller co the dang ky cung `productId` vao cung `eventId` nhieu lan, moi lan tao `FIxxxxx` moi.

Khuyen nghi:

- Truoc khi add item, kiem tra:

```text
exists item where item.eventId == eventId && item.productId == productId
```

Neu ton tai thi tra loi.

### 11.5. Khong reserve stock khi dang ky Flash Sale

`registerItem()` chi tao flash item, khong tru/reserve stock product goc.

He qua:

- Mot product co the vua ban thuong, vua ban flash sale.
- Luc checkout moi phat hien product goc khong du stock.

Khuyen nghi:

- Neu nghiep vu yeu cau chac chan co hang cho Flash Sale, can reserve stock tai luc dang ky.
- Neu khong reserve, UI nen thong bao limitedQty chi la han muc ban, khong phai kho da giu rieng.

### 11.6. Repository Flash Sale chua override header CSV

`FlashSaleEventRepository` va `FlashSaleItemRepository` ke thua `CsvRepository` voi `hasHeader = true`, nhung khong override `getHeader()`.

He qua:

- Khi `eventRepo.add/update()` hoac `itemRepo.add/update()` ghi file, dong header co the bi ghi thanh dong rong.
- App van co the doc tiep vi `CsvRepository` bo qua dong dau tien, nhung file CSV mat header, kho doc va sai voi schema.

Khuyen nghi them:

```java
@Override
protected String getHeader() {
    return "id,name,startTime,endTime,status";
}
```

cho `FlashSaleEventRepository`.

Va:

```java
@Override
protected String getHeader() {
    return "id,productId,eventId,salePrice,limitedQty,soldQty,version";
}
```

cho `FlashSaleItemRepository`.

### 11.7. Expose repository qua `getEventRepo()`

`getEventRepo()` cho class khac lay repository truc tiep.

Uu diem:

- Tien cho test va dashboard.

Rui ro:

- Tang coupling.
- Class khac co the thao tac truc tiep bo qua validate cua controller.

Khuyen nghi:

- Neu chi can thong ke, them method rieng trong controller nhu `countEvents()`, `countOngoingEvents()`.
- Giu `getEventRepo()` package-private hoac chi dung trong test neu co the.

## 12. De xuat nang cap FlashSaleController

Uu tien cao:

1. Them `getHeader()` cho `FlashSaleEventRepository` va `FlashSaleItemRepository`.
2. Them guard phan quyen truc tiep hoac chuan hoa lai tang service/controller.
3. Chan dang ky trung product trong cung event.
4. Bo sung state transition hop le cho `startEvent/endEvent`.

Uu tien trung binh:

1. Cho Admin nhap `startTime/endTime`.
2. Them `disableEvent()` tuong ung `SaleStatus.DISABLED`.
3. Them validate theo thoi gian: khong start event da qua `endTime`.
4. Them API lay chi tiet event kem danh sach item va product name de View khong phai goi nhieu lan.

Uu tien thap:

1. Chuan hoa message tieng Viet co dau trong source.
2. Trim tat ca ID dau vao trong `registerItem()`.
3. Them DTO cho View thay vi tra model/repository object truc tiep.

## 13. Tom tat ket luan

`FlashSaleController` la controller trung tam cho phan setup va hien thi Flash Sale. Class nay dang lam tot viec tao event, cap nhat status, dang ky item va lay item theo event. Luong tich hop voi Admin/Seller/Customer/Guest da ro rang va phu hop MVC.

Diem can sua quan trong nhat khong nam o logic mua hang, ma nam o tinh chat quan tri va bao tri du lieu: phan quyen dang phu thuoc controller bao ngoai, state transition con long, chua chan duplicate item, va hai repository Flash Sale chua ghi header CSV dung khi save file.

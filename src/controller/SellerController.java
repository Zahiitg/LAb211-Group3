package controller;

import model.Order;
import model.OrderDetail;
import model.Product;
import model.Seller;
import model.FlashSaleItem;
import model.enums.OrderStatus;
import repository.OrderRepository;
import repository.OrderDetailRepository;
import repository.ProductRepository;
import repository.FlashSaleItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import repository.SellerRepository;

public class SellerController extends BaseController {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;
    private final FlashSaleItemRepository flashSaleItemRepo;
    private final SellerRepository sellerRepo;
    private final FlashSaleController flashSaleController;
    private final repository.CategoryRepository categoryRepo;

    public SellerController() {
        AuthenticationState authState = AuthenticationState.getInstance();
        this.productRepo = authState.getProductRepo();
        this.orderRepo = authState.getOrderRepo();
        this.detailRepo = authState.getDetailRepo();
        this.flashSaleItemRepo = authState.getFlashSaleItemRepo();
        this.sellerRepo = authState.getSellerRepo();
        this.categoryRepo = authState.getCategoryRepo();
        this.flashSaleController = new FlashSaleController();
    }

    // =====================================================================
    // QUAN LY SAN PHAM
    // =====================================================================

    public ControllerResult getMyProducts() {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        List<Product> myProducts = productRepo.findBySellerId(me.getId());
        return success("Lay danh sach san pham thanh cong.", myProducts);
    }

    public ControllerResult listCategories() {
        List<model.Category> categories = categoryRepo.getAll();
        if (categories.isEmpty()) return error("Khong co danh muc nao.");
        return success("Lay danh muc", categories);
    }

    public ControllerResult addProduct(Product p) {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        
        if (p.getName() == null || p.getName().trim().isEmpty()) return error("Ten san pham khong duoc de trong!");
        if (p.getPrice() <= 0) return error("Gia phai lon hon 0!");
        if (p.getStock() < 0) return error("Ton kho khong duoc am!");
        
        p.setSellerId(me.getId());
        
        // --- SINH MA ID TU DONG ---
        int maxNum = 0;
        for (Product prod : productRepo.getAll()) {
            if (prod.getId() != null && prod.getId().startsWith("P")) {
                try {
                    int num = Integer.parseInt(prod.getId().substring(1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        p.setId(String.format("P%05d", maxNum + 1));

        productRepo.add(p);
        return success("Da them san pham: " + p.getName(), p);
    }

    public ControllerResult updateProduct(Product p) {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        
        Product existing = productRepo.getById(p.getId());
        if (existing == null) return error("San pham khong ton tai!");
        if (!existing.getSellerId().equals(me.getId())) return error("Ban chi co the sua san pham cua minh!");
        
        if (p.getName() == null || p.getName().trim().isEmpty()) return error("Ten san pham khong duoc de trong!");
        if (p.getPrice() <= 0) return error("Gia phai lon hon 0!");
        if (p.getStock() < 0) return error("Ton kho khong duoc am!");
        
        // Preserve original sellerId just in case
        p.setSellerId(me.getId());
        productRepo.update(p);
        return success("Da cap nhat san pham: " + p.getId(), p);
    }

    public ControllerResult deleteProduct(String productId) {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        
        Product p = productRepo.getById(productId);
        if (p == null) return error("San pham khong ton tai!");
        if (!p.getSellerId().equals(me.getId())) return error("Ban chi duoc quyen xoa san pham cua minh!");
        
        productRepo.delete(productId);
        return success("Da xoa san pham: " + productId, p);
    }

    public ControllerResult searchMyProducts(String keyword) {
        requireSeller();
        ControllerResult res = getMyProducts();
        @SuppressWarnings("unchecked")
        List<Product> list = (List<Product>) res.getData();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase();
            list = list.stream().filter(p -> p.getName().toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        return success("Tim thay " + list.size() + " san pham.", list);
    }

    // =====================================================================
    // QUAN LY DON HANG
    // =====================================================================

    public ControllerResult getMyOrders() {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        String sellerId = me.getId();

        List<Order> allOrders = orderRepo.getAll();
        List<Order> myOrders = new ArrayList<>();
        
        List<OrderDetail> allDetails = detailRepo.getAll();
        Map<String, Product> pMap = new HashMap<>();
        for (Product p : productRepo.getAll()) pMap.put(p.getId(), p);
        Map<String, FlashSaleItem> fMap = new HashMap<>();
        for (FlashSaleItem f : flashSaleItemRepo.getAll()) fMap.put(f.getId(), f);

        for (Order o : allOrders) {
            List<OrderDetail> details = new ArrayList<>();
            for (OrderDetail d : allDetails) {
                if (d.getOrderId().equals(o.getId())) details.add(d);
            }
            
            boolean hasMyProduct = false;
            
            for (OrderDetail d : details) {
                String itemId = d.getFlashSaleItemId();
                // 1. Thu tim trong Product
                Product p = pMap.get(itemId);
                if (p != null) {
                    if (sellerId.equals(p.getSellerId())) {
                        hasMyProduct = true;
                        break;
                    }
                } else {
                    // 2. Thu tim trong FlashSaleItem
                    FlashSaleItem fsi = fMap.get(itemId);
                    if (fsi != null) {
                        Product pFlash = pMap.get(fsi.getProductId());
                        if (pFlash != null && sellerId.equals(pFlash.getSellerId())) {
                            hasMyProduct = true;
                            break;
                        }
                    }
                }
            }
            
            if (hasMyProduct) {
                myOrders.add(o);
            }
        }
        return success("Tim thay " + myOrders.size() + " don hang.", myOrders);
    }

    public ControllerResult filterMyOrders(OrderStatus status) {
        requireSeller();
        ControllerResult res = getMyOrders();
        @SuppressWarnings("unchecked")
        List<Order> list = (List<Order>) res.getData();
        if (status != null) {
            list = list.stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
        }
        return success("Tim thay " + list.size() + " don hang trang thai " + status, list);
    }

    public ControllerResult verifyOrder(String orderId) {
        requireSeller();
        if (orderId == null || orderId.trim().isEmpty()) return error("ID khong duoc de trong");
        
        ControllerResult myOrdersResult = getMyOrders();
        @SuppressWarnings("unchecked")
        List<Order> myOrders = (List<Order>) myOrdersResult.getData();
        
        Order target = null;
        for (Order o : myOrders) {
            if (o.getId().equals(orderId)) {
                target = o;
                break;
            }
        }
        
        if (target == null) return error("Khong tim thay don hang hoac don hang khong chua san pham cua ban.");
        if (target.getStatus() != OrderStatus.PENDING) return error("Chi the xac nhan don hang o trang thai PENDING!");
        
        target.setStatus(OrderStatus.VERIFIED);
        orderRepo.update(target);
        return success("Da duyet (VERIFIED) don hang: " + orderId, target);
    }

    public ControllerResult completeOrder(String orderId) {
        requireSeller();
        if (orderId == null || orderId.trim().isEmpty()) return error("ID khong duoc de trong");
        
        ControllerResult myOrdersResult = getMyOrders();
        @SuppressWarnings("unchecked")
        List<Order> myOrders = (List<Order>) myOrdersResult.getData();
        
        Order target = null;
        for (Order o : myOrders) {
            if (o.getId().equals(orderId)) {
                target = o;
                break;
            }
        }
        
        if (target == null) return error("Khong tim thay don hang hoac don hang khong chua san pham cua ban.");
        if (target.getStatus() != OrderStatus.VERIFIED) return error("Chi the hoan thanh don hang dang o trang thai VERIFIED!");
        
        target.setStatus(OrderStatus.COMPLETED);
        orderRepo.update(target);
        return success("Da hoan thanh (COMPLETED) don hang: " + orderId, target);
    }

    public ControllerResult cancelOrder(String orderId) {
        requireSeller();
        if (orderId == null || orderId.trim().isEmpty()) return error("ID khong duoc de trong");
        
        ControllerResult myOrdersResult = getMyOrders();
        @SuppressWarnings("unchecked")
        List<Order> myOrders = (List<Order>) myOrdersResult.getData();
        
        Order target = null;
        for (Order o : myOrders) {
            if (o.getId().equals(orderId)) {
                target = o;
                break;
            }
        }
        
        if (target == null) return error("Khong tim thay don hang hoac don hang khong chua san pham cua ban.");
        if (target.getStatus() != OrderStatus.PENDING && target.getStatus() != OrderStatus.VERIFIED) {
            return error("Chi the huy don hang dang o trang thai PENDING hoac VERIFIED!");
        }
        
        // Hoan kho
        Seller me = (Seller) authState.getCurrentUser();
        List<OrderDetail> details = detailRepo.findByOrderId(target.getId());
        for (OrderDetail detail : details) {
            String itemId = detail.getFlashSaleItemId();
            int qty = detail.getQuantity();
            
            Product p = productRepo.getById(itemId);
            if (p != null && p.getSellerId().equals(me.getId())) {
                p.setStock(p.getStock() + qty);
                productRepo.update(p);
            } else {
                FlashSaleItem fsi = flashSaleItemRepo.getById(itemId);
                if (fsi != null) {
                    Product pFlash = productRepo.getById(fsi.getProductId());
                    if (pFlash != null && pFlash.getSellerId().equals(me.getId())) {
                        fsi.setSoldQty(fsi.getSoldQty() - qty);
                        flashSaleItemRepo.update(fsi);
                        
                        pFlash.setStock(pFlash.getStock() + qty);
                        productRepo.update(pFlash);
                    }
                }
            }
        }
        
        target.setStatus(OrderStatus.CANCELLED);
        orderRepo.update(target);
        return success("Da huy don hang (CANCELLED) va hoan kho: " + orderId, target);
    }

    // =====================================================================
    // THONG KE & HO SO
    // =====================================================================

    public ControllerResult getDashboardStats() {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        
        ControllerResult myOrdersRes = getMyOrders();
        @SuppressWarnings("unchecked")
        List<Order> myOrders = (List<Order>) myOrdersRes.getData();
        
        long pending = 0, verified = 0, completed = 0, cancelled = 0;
        double totalRevenue = 0;
        Map<String, Integer> productSales = new HashMap<>();
        List<OrderDetail> allDetails = detailRepo.getAll();
        
        Map<String, Product> pMap = new HashMap<>();
        for (Product p : productRepo.getAll()) pMap.put(p.getId(), p);
        Map<String, FlashSaleItem> fMap = new HashMap<>();
        for (FlashSaleItem f : flashSaleItemRepo.getAll()) fMap.put(f.getId(), f);
        
        for (Order o : myOrders) {
            switch (o.getStatus()) {
                case PENDING: pending++; break;
                case VERIFIED: verified++; break;
                case COMPLETED: completed++; break;
                case CANCELLED: cancelled++; break;
            }
            
            if (o.getStatus() == OrderStatus.COMPLETED) {
                for (OrderDetail d : allDetails) {
                    if (d.getOrderId().equals(o.getId())) {
                        String itemId = d.getFlashSaleItemId();
                        boolean isMine = false;
                        String productName = "Unknown";
                        
                        Product p = pMap.get(itemId);
                        if (p != null && me.getId().equals(p.getSellerId())) {
                            isMine = true;
                            productName = p.getName();
                        } else {
                            FlashSaleItem fsi = fMap.get(itemId);
                            if (fsi != null) {
                                Product pFlash = pMap.get(fsi.getProductId());
                                if (pFlash != null && me.getId().equals(pFlash.getSellerId())) {
                                    isMine = true;
                                    productName = pFlash.getName();
                                }
                            }
                        }
                        
                        if (isMine) {
                            totalRevenue += (d.getPriceAtPurchase() * d.getQuantity());
                            productSales.put(productName, productSales.getOrDefault(productName, 0) + d.getQuantity());
                        }
                    }
                }
            }
        }
        
        List<Map.Entry<String, Integer>> topProducts = new ArrayList<>(productSales.entrySet());
        topProducts.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        if (topProducts.size() > 3) topProducts = topProducts.subList(0, 3);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("pending", pending);
        stats.put("verified", verified);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("topProducts", topProducts);
        
        return success("Lay thong ke thanh cong", stats);
    }

    public ControllerResult updateProfile(String name, String storeName, String password) {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        
        if (name != null && !name.trim().isEmpty()) me.setName(name);
        if (storeName != null && !storeName.trim().isEmpty()) me.setStoreName(storeName);
        if (password != null && !password.trim().isEmpty()) me.setPassword(password);
        
        sellerRepo.update(me);
        return success("Cap nhat ho so thanh cong!", me);
    }

    // =====================================================================
    // PHASE 1: DANG KY FLASH SALE
    // =====================================================================
    public ControllerResult getOngoingFlashSaleEvents() {
        requireSeller();
        ControllerResult res = flashSaleController.getAllEvents();
        if (!res.isSuccess()) return res;
        @SuppressWarnings("unchecked")
        List<model.FlashSaleEvent> events = (List<model.FlashSaleEvent>) res.getData();
        List<model.FlashSaleEvent> ongoing = events.stream()
            .filter(e -> e.getStatus() == model.enums.SaleStatus.ONGOING)
            .collect(Collectors.toList());
        return success("Danh sach su kien dang dien ra.", ongoing);
    }

    public ControllerResult registerFlashSaleItem(String eventId, String productId, double salePrice, int limitedQty) {
        requireSeller();
        Seller me = (Seller) authState.getCurrentUser();
        return flashSaleController.registerItem(eventId, productId, salePrice, limitedQty, me.getId());
    }
}

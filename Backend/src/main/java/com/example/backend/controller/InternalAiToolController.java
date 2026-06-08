package com.example.backend.controller;

import com.example.backend.service.ProductService;
import com.example.backend.service.StockService;
import com.example.backend.service.DashboardService;
import com.example.backend.service.SystemSettingService;
import com.example.backend.service.CustomerService;
import com.example.backend.service.SupplierService;
import com.example.backend.service.PurchaseOrderService;
import com.example.backend.service.SaleOrderService;
import com.example.backend.service.WarehouseService;
import com.example.backend.service.OperationLogService;
import com.example.backend.service.UserService;
import com.example.backend.vo.ProductVO;
import com.example.backend.vo.StockVO;
import com.example.backend.vo.CategoryStatsVO;
import com.example.backend.vo.CustomerVO;
import com.example.backend.vo.SupplierVO;
import com.example.backend.vo.PurchaseOrderVO;
import com.example.backend.vo.SaleOrderVO;
import com.example.backend.vo.WarehouseVO;
import com.example.backend.entity.OperationLog;
import com.example.backend.entity.User;
import com.example.backend.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 2026-06-08: 拓展后的商品、库存、客户、供应商、仓库、订单、日志及用户安全回调接口，专门供 Python AI 微服务调用。
 */
@RestController
@RequestMapping("/internal/ai/tools")
public class InternalAiToolController {

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserService userService;

    @GetMapping("/stock-snapshot")
    public List<StockVO> getStockSnapshot(@RequestParam(value = "warehouseId", required = false) Long warehouseId) {
        return stockService.getStockSnapshot(warehouseId);
    }

    @GetMapping("/low-stock")
    public List<StockVO> getLowStockProducts() {
        int threshold = systemSettingService.getIntValue("low_stock_threshold", 10);
        List<StockVO> snapshot = stockService.getStockSnapshot(null);
        return snapshot.stream()
                .filter(stock -> stock.getCurrentStock() < threshold)
                .collect(Collectors.toList());
    }

    @GetMapping("/category-stock-stats")
    public List<CategoryStatsVO> getCategoryStockStats() {
        return dashboardService.getCategoryStats("STOCK");
    }

    @GetMapping("/product-detail")
    public ProductVO getProductDetail(@RequestParam("productId") Long productId) {
        return productService.getProductById(productId);
    }

    @GetMapping("/all-products")
    public List<ProductVO> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/customers")
    public List<CustomerVO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/customer-detail")
    public CustomerVO getCustomerDetail(@RequestParam("customerId") Long customerId) {
        return customerService.getCustomerById(customerId);
    }

    @GetMapping("/suppliers")
    public List<SupplierVO> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/supplier-detail")
    public SupplierVO getSupplierDetail(@RequestParam("supplierId") Long supplierId) {
        return supplierService.getSupplierById(supplierId);
    }

    @GetMapping("/purchase-orders")
    public List<PurchaseOrderVO> getAllPurchaseOrders() {
        return purchaseOrderService.getAllOrders();
    }

    @GetMapping("/purchase-order-detail")
    public PurchaseOrderVO getPurchaseOrderDetail(@RequestParam("orderId") Long orderId) {
        return purchaseOrderService.getOrderById(orderId);
    }

    @GetMapping("/sales-orders")
    public List<SaleOrderVO> getAllSalesOrders() {
        return saleOrderService.getAllOrders();
    }

    @GetMapping("/sales-order-detail")
    public SaleOrderVO getSalesOrderDetail(@RequestParam("orderId") Long orderId) {
        return saleOrderService.getOrderById(orderId);
    }

    @GetMapping("/warehouses")
    public List<WarehouseVO> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    @GetMapping("/recent-logs")
    public List<OperationLog> getRecentLogs(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        return operationLogService.findRecent(limit);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users != null) {
            for (User u : users) {
                u.setPassword(null); // 绝对禁止密码泄露给 AI
            }
        }
        return users;
    }

    @GetMapping("/settings")
    public Map<String, String> getSettings() {
        Map<String, String> settings = new HashMap<>();
        
        // 解密后的 API Key 传给 Python，避免跨语言重复实现加解密逻辑
        String encryptedKey = systemSettingService.getValue("ai_api_key");
        String apiKey = "";
        if (encryptedKey != null && !encryptedKey.trim().isEmpty()) {
            try {
                apiKey = EncryptionUtils.decrypt(encryptedKey);
            } catch (Exception e) {
                apiKey = encryptedKey; // 降级处理
            }
        }
        settings.put("ai_api_key", apiKey);
        
        settings.put("ai_base_url", systemSettingService.getValue("ai_base_url", "https://api.openai.com/v1"));
        settings.put("ai_model_name", systemSettingService.getValue("ai_model_name", "gpt-4o"));
        settings.put("ai_max_rpm", systemSettingService.getValue("ai_max_rpm", ""));
        settings.put("ai_max_tpm", systemSettingService.getValue("ai_max_tpm", ""));
        settings.put("ai_max_rpd", systemSettingService.getValue("ai_max_rpd", ""));
        
        return settings;
    }
}

package com.example.backend.controller;

import com.example.backend.service.ProductService;
import com.example.backend.service.StockService;
import com.example.backend.service.DashboardService;
import com.example.backend.service.SystemSettingService;
import com.example.backend.vo.ProductVO;
import com.example.backend.vo.StockVO;
import com.example.backend.vo.CategoryStatsVO;
import com.example.backend.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
2026-06-06: 专为 Python AI 微服务调用的安全/内部数据回调端点
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

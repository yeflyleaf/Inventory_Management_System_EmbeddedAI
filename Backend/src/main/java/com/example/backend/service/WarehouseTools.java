package com.example.backend.service;

import com.example.backend.service.ProductService;
import com.example.backend.service.StockService;
import com.example.backend.service.DashboardService;
import com.example.backend.service.SystemSettingService;
import com.example.backend.vo.ProductVO;
import com.example.backend.vo.StockVO;
import com.example.backend.vo.CategoryStatsVO;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓储管理系统的 AI 助手可用工具集 (Function Calling)
 */
@Component
public class WarehouseTools {

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Tool("获取指定仓库或所有仓库的当前库存列表快照（包含当前库存数量、商品名称、SKU、分类、最后变动时间）")
    public List<StockVO> getStockSnapshot(Long warehouseId) {
        return stockService.getStockSnapshot(warehouseId);
    }

    @Tool("查询库存处于紧张状态（低于低库存预警阈值）的商品列表")
    public List<StockVO> getLowStockProducts() {
        int threshold = systemSettingService.getIntValue("low_stock_threshold", 10);
        List<StockVO> snapshot = stockService.getStockSnapshot(null);
        return snapshot.stream()
                .filter(stock -> stock.getCurrentStock() < threshold)
                .collect(Collectors.toList());
    }

    @Tool("统计各个商品分类的库存总数列表（返回各分类的名称和库存数值）")
    public List<CategoryStatsVO> getCategoryStockStats() {
        return dashboardService.getCategoryStats("STOCK");
    }

    @Tool("根据商品ID获取商品的详细规格及价格信息")
    public ProductVO getProductDetail(Long productId) {
        return productService.getProductById(productId);
    }

    @Tool("获取所有商品的基础信息列表（包含商品ID、名称、SKU编码、分类、销售单价）")
    public List<ProductVO> getAllProducts() {
        return productService.getAllProducts();
    }
}

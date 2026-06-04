package com.example.backend.service.impl;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.backend.dao.SaleOrderMapper;
import com.example.backend.dao.StockFlowMapper;
import com.example.backend.entity.StockFlow;
import com.example.backend.service.DashboardService;
import com.example.backend.service.StockService;
import com.example.backend.vo.DashboardVO;
import com.example.backend.vo.StockVO;

/**
 * 仪表盘服务实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private SaleOrderMapper saleOrderMapper;

    @Autowired
    private com.example.backend.dao.PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockFlowMapper stockFlowMapper;

    @Autowired
    private com.example.backend.service.SystemSettingService systemSettingService;

    /**
     * 获取仪表盘综合统计数据
     * 包含：今日销售额、库存概况（总库存、低库存预警）、最近活动记录、库存趋势图、待处理事项（待审核订单）、积压库存统计
     * 结果会被缓存，缓存key由参数组合决定
     *
     * @param itemId      指定商品ID进行过滤 (可选)
     * @param days        趋势图统计天数 (默认30天)
     * @param warehouseId 指定仓库ID进行过滤 (可选)
     * @param category    指定分类进行过滤 (可选)
     * @param itemName    指定商品名称模糊搜索 (可选)
     * @return 仪表盘数据VO对象
     */
    @Override
    @Cacheable(value = "dashboardStats", key = "{#itemId, #days, #warehouseId, #category, #itemName}", unless = "#result == null")
    public DashboardVO getStats(Long itemId, Integer days, Long warehouseId, String category, String itemName) {
        DashboardVO vo = new DashboardVO();

        // 1. Today's Sales (Global for now, as per requirement focus on Stock)
        BigDecimal todaySales = saleOrderMapper.sumTodaySales();
        vo.setTodaySales(todaySales != null ? todaySales : BigDecimal.ZERO);

        // 2. Stock Stats
        int totalStock = 0;
        int lowStockCount = 0;
        // Get low stock threshold from settings
        int LOW_STOCK_THRESHOLD = systemSettingService.getIntValue("low_stock_threshold", 10);

        if (itemId != null) {
            // Specific Item
            Integer currentStock = stockFlowMapper.sumStockByItemIdAndWarehouseId(itemId, null);
            if (currentStock == null)
                currentStock = 0;
            totalStock = currentStock;
            if (totalStock < LOW_STOCK_THRESHOLD) {
                lowStockCount = 1;
            }
        } else {
            // All Items
            List<StockVO> stocks = stockService.getStockSnapshot(null);
            for (StockVO stock : stocks) {
                totalStock += stock.getCurrentStock();
                if (stock.getCurrentStock() < LOW_STOCK_THRESHOLD) {
                    lowStockCount++;
                }
            }
        }

        vo.setTotalStock(totalStock);
        vo.setLowStockCount(lowStockCount);

        // 3. Recent Activity
        List<StockFlow> recentFlows = stockFlowMapper.selectRecent(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<com.example.backend.vo.DashboardActivityVO> activities = recentFlows.stream().map(flow -> {
            com.example.backend.vo.DashboardActivityVO activity = new com.example.backend.vo.DashboardActivityVO();
            activity.setTime(flow.getCreatedAt().format(formatter));

            String type = flow.getChangeType();
            String amount = (flow.getChangeAmount() > 0 ? "+" : "") + flow.getChangeAmount();
            activity.setDescription(type + " " + amount + " (Item #" + flow.getItemId() + ")");
            return activity;
        }).collect(Collectors.toList());
        vo.setRecentActivity(activities);

        // 4. Stock Trend (Filtered)
        int daysToQuery = days != null ? days : 30;
        List<com.example.backend.vo.StockFlowDailyStatsVO> trend = stockFlowMapper.selectDailyStats(daysToQuery,
                warehouseId, category, itemName);
        vo.setStockTrend(trend);

        // 5. Pending Tasks
        vo.setPendingPurchaseOrders(purchaseOrderMapper.countByStatus("CREATED"));
        vo.setPendingSalesOrders(saleOrderMapper.countByStatus("CREATED"));
        vo.setAbnormalOrders(0); // Placeholder for now

        // 6. Overstock (Items with stock > 0 but no movement in last X days)
        // Get backlog days from settings
        int backlogDays = systemSettingService.getIntValue("inventory_backlog_days", 60);
        
        if (itemId == null) {
            List<StockVO> allStocks = stockService.getStockSnapshot(null);
            List<Long> activeItemIds = stockFlowMapper.selectDistinctItemIdsRecent(backlogDays);

            long overstock = allStocks.stream()
                    .filter(s -> s.getCurrentStock() > 0)
                    .filter(s -> !activeItemIds.contains(s.getItemId()))
                    .count();
            vo.setOverstockCount((int) overstock);
        } else {
            vo.setOverstockCount(0);
        }

        return vo;
    }

    @Autowired
    private com.example.backend.dao.SaleOrderItemMapper saleOrderItemMapper;

    /**
     * 获取分类统计数据
     * 支持三种统计类型：
     * 1. SALES_QTY: 按分类统计销售数量
     * 2. SALES_AMOUNT: 按分类统计销售金额
     * 3. STOCK: 按分类统计当前库存数量 (默认)
     * 结果会被缓存，key为统计类型
     *
     * @param type 统计类型字符串
     * @return 分类统计VO列表，包含分类名称和对应数值
     */
    @Override
    @Cacheable(value = "dashboardCategoryStats", key = "#type")
    public List<com.example.backend.vo.CategoryStatsVO> getCategoryStats(String type) {
        if ("SALES_QTY".equals(type)) {
            return saleOrderItemMapper.selectSalesQtyByCategory();
        } else if ("SALES_AMOUNT".equals(type)) {
            return saleOrderItemMapper.selectSalesAmountByCategory();
        } else {
            // Default to STOCK (calculated from stock snapshot)
            // Note: Ideally we should do this in SQL for performance, but reusing
            // getStockSnapshot is easier for now
            List<StockVO> stocks = stockService.getStockSnapshot(null);
            java.util.Map<String, Integer> categoryMap = new java.util.HashMap<>();
            for (StockVO stock : stocks) {
                String cat = stock.getCategory() != null ? stock.getCategory() : "Uncategorized";
                categoryMap.put(cat, categoryMap.getOrDefault(cat, 0) + stock.getCurrentStock());
            }

            return categoryMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(5)
                    .map(e -> {
                        com.example.backend.vo.CategoryStatsVO stat = new com.example.backend.vo.CategoryStatsVO();
                        stat.setCategory(e.getKey());
                        stat.setValue(new BigDecimal(e.getValue()));
                        return stat;
                    })
                    .collect(Collectors.toList());
        }
    }
}

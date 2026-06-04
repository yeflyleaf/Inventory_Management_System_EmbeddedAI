package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘综合数据VO
 * 包含销售、库存、活动、趋势等多维度数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    /** 今日销售总额 */
    private BigDecimal todaySales;
    /** 当前总库存数量 */
    private Integer totalStock;
    /** 低库存商品数量 (触发预警) */
    private Integer lowStockCount;
    /** 近期活动列表 */
    private List<DashboardActivityVO> recentActivity;
    /** 库存/销售趋势数据 */
    private List<StockFlowDailyStatsVO> stockTrend;
    
    /** 待处理采购订单数 */
    private Integer pendingPurchaseOrders;
    /** 待处理销售订单数 */
    private Integer pendingSalesOrders;
    /** 异常订单数 (暂留字段) */
    private Integer abnormalOrders;
    
    /** 积压库存商品数 (长期无动销) */
    private Integer overstockCount;

}

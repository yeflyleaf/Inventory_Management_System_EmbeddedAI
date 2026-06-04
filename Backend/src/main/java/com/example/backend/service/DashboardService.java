package com.example.backend.service;

import com.example.backend.vo.DashboardVO;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {
    /**
     * 获取仪表盘统计数据
     * @param itemId 商品ID (可选)
     * @param days 统计天数 (可选)
     * @param warehouseId 仓库ID (可选)
     * @param category 分类 (可选)
     * @param itemName 商品名称 (可选)
     * @return 仪表盘VO
     */
    DashboardVO getStats(Long itemId, Integer days, Long warehouseId, String category, String itemName);

    /**
     * 获取分类统计数据
     * @param type 统计类型 (SALES_QTY, SALES_AMOUNT, STOCK)
     * @return 分类统计VO列表
     */
    java.util.List<com.example.backend.vo.CategoryStatsVO> getCategoryStats(String type);
}

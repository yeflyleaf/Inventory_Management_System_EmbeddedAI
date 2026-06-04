package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.DashboardService;
import com.example.backend.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 * 提供仪表盘统计数据接口
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * @param itemId 商品ID(可选)
     * @param days 天数(可选)
     * @param warehouseId 仓库ID(可选)
     * @param category 分类(可选)
     * @param itemName 商品名称(可选)
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardVO> getStats(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long itemId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer days,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long warehouseId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String category,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String itemName
    ) {
        DashboardVO stats = dashboardService.getStats(itemId, days, warehouseId, category, itemName);
        return Result.success(stats, "获取仪表盘数据成功");
    }

    /**
     * 获取分类统计数据
     * @param type 统计类型(默认为STOCK)
     * @return 分类统计列表
     */
    @GetMapping("/category-stats")
    public Result<java.util.List<com.example.backend.vo.CategoryStatsVO>> getCategoryStats(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "STOCK") String type) {
        return Result.success(dashboardService.getCategoryStats(type), "Success");
    }
}

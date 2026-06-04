package com.example.backend.vo;

import lombok.Data;

/**
 * 管理员仪表盘统计数据VO
 * 用于向前端展示管理员视角的系统概览数据
 */
@Data
public class AdminDashboardVO {
    /** 总用户数 */
    private int totalUsers;
    /** 活跃用户数 (近期有登录) */
    private int activeUsers;
    /** 今日新增用户数 */
    private int newUsersToday;
    
    /** 今日总操作次数 */
    private int totalOperationsToday;
    /** 今日总登录次数 */
    private int totalLoginToday;
    
    /** 商品总数 */
    private int totalProducts;
    /** 低库存商品数 (低于预警阈值) */
    private int lowStockProducts;
    /** 缺货商品数 (库存为0) */
    private int outOfStockProducts;
    
    /** 待处理采购订单数 (状态为 CREATED) */
    private int pendingPurchaseOrders;
    /** 待发货销售订单数 (状态为 CREATED) */
    private int pendingSalesOrders;
    
    /** 系统版本号 */
    private String systemVersion;
    /** 最后备份时间 */
    private String lastBackupTime;
}

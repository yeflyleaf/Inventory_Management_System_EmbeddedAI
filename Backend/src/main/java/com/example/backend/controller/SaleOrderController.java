package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.dto.SaleOrderDTO;
import com.example.backend.service.SaleOrderService;
import com.example.backend.vo.SaleOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 销售订单管理控制器
 * 提供销售订单的增删改查及发货功能
 */
@RestController
@RequestMapping("/sale-orders")
@CrossOrigin
public class SaleOrderController {

    @Autowired
    private SaleOrderService saleOrderService;

    /**
     * 获取所有销售订单
     * @return 销售订单列表
     */
    @GetMapping
    public Result<List<SaleOrderVO>> getAll() {
        List<SaleOrderVO> orders = saleOrderService.getAllOrders();
        return Result.success(orders, "获取销售订单列表成功");
    }

    /**
     * 获取销售订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<SaleOrderVO> getById(@PathVariable Long id) {
        SaleOrderVO order = saleOrderService.getOrderById(id);
        return Result.success(order, "获取销售订单详情成功");
    }

    /**
     * 创建销售订单
     * @param orderDTO 订单信息
     * @param request HTTP请求
     * @return 成功信息
     */
    @PostMapping
    @Log(module = "销售管理", action = "创建订单", description = "创建销售订单")
    public Result<Void> create(@RequestBody SaleOrderDTO orderDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        saleOrderService.createOrder(orderDTO, userId);
        return Result.success(null, "创建销售订单成功");
    }

    /**
     * 销售订单发货(出库)
     * @param id 订单ID
     * @return 成功信息
     */
    @PostMapping("/{id}/ship")
    @Log(module = "销售管理", action = "订单发货", description = "销售订单发货(出库)")
    public Result<Void> ship(@PathVariable Long id) {
        saleOrderService.shipOrder(id);
        return Result.success(null, "发货成功");
    }

    /**
     * 清空所有销售订单
     * @return 成功信息
     */
    @DeleteMapping
    @Log(module = "销售管理", action = "清空订单", description = "清空所有销售订单")
    public Result<Void> deleteAll() {
        saleOrderService.deleteAllOrders();
        return Result.success(null, "清空销售订单成功");
    }

    /**
     * 删除销售订单
     * @param id 订单ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Log(module = "销售管理", action = "删除订单", description = "删除销售订单")
    public Result<Void> delete(@PathVariable Long id) {
        saleOrderService.deleteOrder(id);
        return Result.success(null, "删除销售订单成功");
    }
}

package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.dto.PurchaseOrderDTO;
import com.example.backend.service.PurchaseOrderService;
import com.example.backend.vo.PurchaseOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 采购订单管理控制器
 * 提供采购订单的增删改查及状态流转功能
 */
@RestController
@RequestMapping("/purchase-orders")
@CrossOrigin
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
     * 获取所有采购订单
     * @return 采购订单列表
     */
    @GetMapping
    public Result<List<PurchaseOrderVO>> getAll() {
        List<PurchaseOrderVO> orders = purchaseOrderService.getAllOrders();
        return Result.success(orders, "获取采购订单列表成功");
    }

    /**
     * 获取采购订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> getById(@PathVariable Long id) {
        PurchaseOrderVO order = purchaseOrderService.getOrderById(id);
        return Result.success(order, "获取采购订单详情成功");
    }

    /**
     * 创建采购订单
     * @param orderDTO 订单信息
     * @param request HTTP请求
     * @return 成功信息
     */
    @PostMapping
    @Log(module = "采购管理", action = "创建订单", description = "创建采购订单")
    public Result<Void> create(@RequestBody PurchaseOrderDTO orderDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        purchaseOrderService.createOrder(orderDTO, userId);
        return Result.success(null, "创建采购订单成功");
    }

    /**
     * 完成采购订单(入库)
     * @param id 订单ID
     * @return 成功信息
     */
    @PostMapping("/{id}/finish")
    @Log(module = "采购管理", action = "完成订单", description = "完成采购订单(入库)")
    public Result<Void> finish(@PathVariable Long id) {
        purchaseOrderService.finishOrder(id);
        return Result.success(null, "完成采购订单成功");
    }

    /**
     * 清空所有采购订单
     * @return 成功信息
     */
    @DeleteMapping
    @Log(module = "采购管理", action = "清空订单", description = "清空所有采购订单")
    public Result<Void> deleteAll() {
        purchaseOrderService.deleteAllOrders();
        return Result.success(null, "清空采购订单成功");
    }

    /**
     * 删除采购订单
     * @param id 订单ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Log(module = "采购管理", action = "删除订单", description = "删除采购订单")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
        return Result.success(null, "删除采购订单成功");
    }
}

package com.example.backend.service;

import com.example.backend.dto.PurchaseOrderDTO;
import com.example.backend.vo.PurchaseOrderVO;
import java.util.List;

/**
 * 采购订单服务接口
 */
public interface PurchaseOrderService {
    /**
     * 创建采购订单
     * @param orderDTO 采购订单DTO
     * @param userId 创建人ID
     */
    void createOrder(PurchaseOrderDTO orderDTO, Long userId);

    /**
     * 完成采购订单 (入库)
     * @param orderId 订单ID
     */
    void finishOrder(Long orderId); // Inbound

    /**
     * 获取所有采购订单
     * @return 采购订单VO列表
     */
    List<PurchaseOrderVO> getAllOrders();

    /**
     * 根据ID获取采购订单
     * @param id 订单ID
     * @return 采购订单VO
     */
    PurchaseOrderVO getOrderById(Long id);

    /**
     * 删除所有采购订单
     */
    void deleteAllOrders();

    /**
     * 删除指定采购订单
     * @param id 订单ID
     */
    void deleteOrder(Long id);
}

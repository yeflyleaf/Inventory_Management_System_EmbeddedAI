package com.example.backend.service;

import com.example.backend.dto.SaleOrderDTO;
import com.example.backend.vo.SaleOrderVO;
import java.util.List;

/**
 * 销售订单服务接口
 */
public interface SaleOrderService {
    /**
     * 创建销售订单
     * @param orderDTO 销售订单DTO
     * @param userId 创建人ID
     */
    void createOrder(SaleOrderDTO orderDTO, Long userId);

    /**
     * 发货 (出库)
     * @param orderId 订单ID
     */
    void shipOrder(Long orderId); // Outbound

    /**
     * 获取所有销售订单
     * @return 销售订单VO列表
     */
    List<SaleOrderVO> getAllOrders();

    /**
     * 根据ID获取销售订单
     * @param id 订单ID
     * @return 销售订单VO
     */
    SaleOrderVO getOrderById(Long id);

    /**
     * 删除所有销售订单
     */
    void deleteAllOrders();

    /**
     * 删除指定销售订单
     * @param id 订单ID
     */
    void deleteOrder(Long id);
}

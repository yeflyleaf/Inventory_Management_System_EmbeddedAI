package com.example.backend.dao;

import com.example.backend.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 采购订单项数据访问接口
 */
@Mapper
public interface PurchaseOrderItemMapper {
    /**
     * 插入订单项
     */
    int insert(PurchaseOrderItem item);

    /**
     * 根据订单ID删除订单项
     */
    int deleteByOrderId(Long orderId);

    /**
     * 根据订单ID查询订单项
     */
    List<PurchaseOrderItem> selectByOrderId(Long orderId);

    /**
     * 删除所有订单项
     */
    int deleteAll();
}

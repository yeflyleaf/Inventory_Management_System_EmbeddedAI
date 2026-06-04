package com.example.backend.dao;

import com.example.backend.entity.SaleOrderItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 销售订单项数据访问接口
 */
@Mapper
public interface SaleOrderItemMapper {
    /**
     * 插入订单项
     */
    int insert(SaleOrderItem item);

    /**
     * 根据订单ID删除订单项
     */
    int deleteByOrderId(Long orderId);

    /**
     * 根据订单ID查询订单项
     */
    List<SaleOrderItem> selectByOrderId(Long orderId);

    /**
     * 删除所有订单项
     */
    int deleteAll();
    
    /**
     * 按分类统计销售数量
     */
    List<com.example.backend.vo.CategoryStatsVO> selectSalesQtyByCategory();

    /**
     * 按分类统计销售金额
     */
    List<com.example.backend.vo.CategoryStatsVO> selectSalesAmountByCategory();
}

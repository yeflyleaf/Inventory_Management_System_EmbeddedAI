package com.example.backend.dao;

import com.example.backend.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


/**
 * 销售订单数据访问接口
 */
@Mapper
public interface SaleOrderMapper {
    /**
     * 插入销售订单
     */
    int insert(SaleOrder saleOrder);

    /**
     * 更新销售订单
     */
    int update(SaleOrder saleOrder);

    /**
     * 根据ID删除销售订单
     */
    int deleteById(Long id);

    /**
     * 根据ID查询销售订单
     */
    SaleOrder selectById(Long id);

    /**
     * 查询所有销售订单
     */
    List<SaleOrder> selectAll();

    /**
     * 统计今日销售额
     */
    java.math.BigDecimal sumTodaySales();

    /**
     * 删除所有销售订单
     */
    int deleteAll();

    /**
     * 根据状态统计订单数量
     */
    int countByStatus(String status);
}

package com.example.backend.dao;

import com.example.backend.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 采购订单数据访问接口
 */
@Mapper
public interface PurchaseOrderMapper {
    /**
     * 插入采购订单
     */
    int insert(PurchaseOrder purchaseOrder);

    /**
     * 更新采购订单
     */
    int update(PurchaseOrder purchaseOrder);

    /**
     * 根据ID删除采购订单
     */
    int deleteById(Long id);

    /**
     * 根据ID查询采购订单
     */
    PurchaseOrder selectById(Long id);

    /**
     * 查询所有采购订单
     */
    List<PurchaseOrder> selectAll();

    /**
     * 删除所有采购订单
     */
    int deleteAll();

    /**
     * 根据状态统计订单数量
     */
    int countByStatus(String status);
}

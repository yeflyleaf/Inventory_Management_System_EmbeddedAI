package com.example.backend.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 采购订单项实体类
 */
@Data
public class PurchaseOrderItem {
    /**
     * 订单项ID
     */
    private Long id;

    /**
     * 采购订单ID
     */
    private Long purchaseOrderId;

    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 采购单价
     */
    private BigDecimal costPrice;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}

package com.example.backend.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 销售订单项实体类
 */
@Data
public class SaleOrderItem {
    /**
     * 订单项ID
     */
    private Long id;

    /**
     * 销售订单ID
     */
    private Long salesOrderId;

    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 销售单价
     */
    private BigDecimal salePrice;

    /**
     * 小计金额
     */
    private BigDecimal subtotal;
}

package com.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 销售订单项DTO
 */
@Data
public class SaleOrderItemDTO {
    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 销售数量
     */
    private Integer qty;

    /**
     * 销售单价
     */
    private BigDecimal salePrice;
}

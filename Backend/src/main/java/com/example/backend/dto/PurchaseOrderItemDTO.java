package com.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 采购订单项DTO
 */
@Data
public class PurchaseOrderItemDTO {
    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 采购数量
     */
    private Integer qty;

    /**
     * 采购单价
     */
    private BigDecimal costPrice;
}

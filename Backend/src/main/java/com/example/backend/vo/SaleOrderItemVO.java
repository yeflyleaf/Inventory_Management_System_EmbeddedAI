package com.example.backend.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 销售订单明细视图对象
 * 包含商品信息和销售数量、价格
 */
@Data
public class SaleOrderItemVO {
    /** 明细ID */
    private Long id;
    /** 商品ID */
    private Long itemId;
    /** 商品名称 */
    private String itemName;
    /** 商品SKU */
    private String itemSku;
    /** 销售数量 */
    private Integer qty;
    /** 销售单价 */
    private BigDecimal salePrice;
    /** 小计金额 (单价 * 数量) */
    private BigDecimal subtotal;
}

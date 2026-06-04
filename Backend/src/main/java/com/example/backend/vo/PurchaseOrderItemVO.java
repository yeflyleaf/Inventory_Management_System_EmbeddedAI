package com.example.backend.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 采购订单明细视图对象
 * 包含商品信息和采购数量、价格
 */
@Data
public class PurchaseOrderItemVO {
    /** 明细ID */
    private Long id;
    /** 商品ID */
    private Long itemId;
    /** 商品名称 */
    private String itemName;
    /** 商品SKU */
    private String itemSku;
    /** 采购数量 */
    private Integer qty;
    /** 采购单价 */
    private BigDecimal costPrice;
    /** 小计金额 (单价 * 数量) */
    private BigDecimal subtotal;
}

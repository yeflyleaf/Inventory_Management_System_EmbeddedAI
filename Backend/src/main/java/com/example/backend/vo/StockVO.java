package com.example.backend.vo;

import lombok.Data;

/**
 * 库存快照视图对象
 * 展示商品在特定仓库的当前库存状态
 */
@Data
public class StockVO {
    /** 商品ID */
    private Long itemId;
    /** 商品名称 */
    private String itemName;
    /** 商品SKU */
    private String itemSku;
    /** 商品分类 */
    private String category;
    /** 仓库ID */
    private Long warehouseId;
    /** 仓库名称 */
    private String warehouseName;
    /** 当前库存数量 */
    private Integer currentStock;
    /** 最后变动时间 */
    private java.time.LocalDateTime lastMovementDate;
}

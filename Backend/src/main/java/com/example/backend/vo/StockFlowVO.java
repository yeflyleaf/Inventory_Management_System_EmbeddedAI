package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存流水视图对象
 * 记录每一次库存变动的详细信息
 */
@Data
public class StockFlowVO {
    /** 流水ID */
    private Long id;
    /** 商品ID */
    private Long itemId;
    /** 商品名称 */
    private String itemName;
    /** 变动数量 (正数增加，负数减少) */
    private Integer changeAmount;
    /** 变动类型 (如：采购入库、销售出库) */
    private String changeType;
    /** 关联单据类型 (如：PO, SO) */
    private String refType;
    /** 关联单据编号 */
    private String refId;
    /** 仓库ID */
    private Long warehouseId;
    /** 仓库名称 */
    private String warehouseName;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

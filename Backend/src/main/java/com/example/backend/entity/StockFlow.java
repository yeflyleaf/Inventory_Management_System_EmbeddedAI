package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存流水实体类
 */
@Data
public class StockFlow {
    /**
     * 流水ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 变动数量
     */
    private Integer changeAmount;

    /**
     * 变动类型
     */
    private String changeType;

    /**
     * 关联类型 (如：采购订单、销售订单)
     */
    private String refType;

    /**
     * 关联ID
     */
    private String refId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

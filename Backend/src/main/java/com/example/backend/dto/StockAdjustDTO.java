package com.example.backend.dto;

import lombok.Data;

/**
 * 库存调整DTO
 */
@Data
public class StockAdjustDTO {
    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 变动数量 (正数增加，负数减少)
     */
    private Integer changeAmount;

    /**
     * 变动类型 (如：盘点、损耗、领用等)
     */
    private String changeType;

    /**
     * 备注
     */
    private String remark;
}

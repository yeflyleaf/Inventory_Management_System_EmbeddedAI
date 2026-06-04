package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存流水每日统计VO
 * 用于图表展示每日的出入库数量趋势
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockFlowDailyStatsVO {
    /** 日期 (yyyy-MM-dd) */
    private String date;
    /** 入库数量 */
    private Integer inQuantity;
    /** 出库数量 */
    private Integer outQuantity;
}

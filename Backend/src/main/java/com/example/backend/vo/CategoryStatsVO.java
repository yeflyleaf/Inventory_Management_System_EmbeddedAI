package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 分类统计数据VO
 * 用于图表展示各类别的统计数值 (如库存量、销售额等)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsVO {
    /** 分类名称 */
    private String category;
    /** 统计数值 */
    private BigDecimal value;
}

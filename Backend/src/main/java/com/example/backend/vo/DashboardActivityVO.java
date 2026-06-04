package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘近期活动VO
 * 用于展示最近的操作记录或库存变动
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityVO {
    /** 活动时间 (格式化后的字符串) */
    private String time;
    /** 活动描述 */
    private String description;
}

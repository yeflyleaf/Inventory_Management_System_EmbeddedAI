package com.example.backend.dto;

import lombok.Data;

/**
 * 操作日志查询DTO
 */
@Data
public class LogQueryDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 开始时间 (yyyy-MM-dd HH:mm:ss)
     */
    private String startTime;

    /**
     * 结束时间 (yyyy-MM-dd HH:mm:ss)
     */
    private String endTime;

    /**
     * 页码 (默认1)
     */
    private Integer page = 1;

    /**
     * 每页大小 (默认20)
     */
    private Integer size = 20;
}

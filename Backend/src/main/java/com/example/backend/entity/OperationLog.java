package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
public class OperationLog {
    /**
     * 日志ID
     */
    private Long id;

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
     * 操作对象
     */
    private String target;

    /**
     * 对象ID
     */
    private String targetId;

    /**
     * 描述
     */
    private String description;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 执行时间(ms)
     */
    private Integer executionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

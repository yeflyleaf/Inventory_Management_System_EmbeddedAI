package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仓库实体类
 */
@Data
public class Warehouse {
    /**
     * 仓库ID
     */
    private Long id;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 仓库地址
     */
    private String address;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

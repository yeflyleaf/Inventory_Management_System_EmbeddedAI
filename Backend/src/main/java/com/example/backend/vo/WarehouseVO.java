package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仓库信息视图对象
 * 用于前端展示仓库详情
 */
@Data
public class WarehouseVO {
    /** 仓库ID */
    private Long id;
    /** 仓库名称 */
    private String name;
    /** 仓库地址 */
    private String address;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

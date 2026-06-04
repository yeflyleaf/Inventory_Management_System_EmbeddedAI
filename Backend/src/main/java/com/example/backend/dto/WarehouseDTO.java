package com.example.backend.dto;

import lombok.Data;

/**
 * 仓库信息DTO
 */
@Data
public class WarehouseDTO {
    /**
     * 仓库ID (更新时必填)
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
}

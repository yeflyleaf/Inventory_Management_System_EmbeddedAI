package com.example.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商实体类
 */
@Data
public class Supplier {
    /**
     * 供应商ID
     */
    private Long id;

    /**
     * 供应商名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 备用电话
     */
    private String phone2;

    /**
     * 地址
     */
    private String address;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

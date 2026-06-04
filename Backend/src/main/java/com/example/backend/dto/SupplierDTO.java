package com.example.backend.dto;

import lombok.Data;

/**
 * 供应商信息DTO
 */
@Data
public class SupplierDTO {
    /**
     * 供应商ID (更新时必填)
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
}

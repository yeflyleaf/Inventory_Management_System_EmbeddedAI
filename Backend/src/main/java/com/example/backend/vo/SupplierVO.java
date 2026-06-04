package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商信息视图对象
 * 用于前端展示供应商详情
 */
@Data
public class SupplierVO {
    /** 供应商ID */
    private Long id;
    /** 供应商名称 */
    private String name;
    /** 联系人姓名 */
    private String contactPerson;
    /** 主要联系电话 */
    private String phone;
    /** 备用联系电话 */
    private String phone2;
    /** 联系地址 */
    private String address;
    /** 创建时间 */
    private LocalDateTime createdAt;
}

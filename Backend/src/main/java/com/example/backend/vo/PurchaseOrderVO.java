package com.example.backend.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单视图对象
 * 包含订单主表信息和明细列表
 */
@Data
public class PurchaseOrderVO {
    /** 订单ID */
    private Long id;
    /** 订单编号 */
    private String orderNo;
    /** 供应商ID */
    private Long supplierId;
    /** 供应商名称 */
    private String supplierName;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 订单状态 (CREATED, COMPLETED, CANCELLED) */
    private String status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 创建人ID */
    private Long createdBy;
    /** 创建人姓名 */
    private String createdByName;
    /** 订单明细列表 */
    private List<PurchaseOrderItemVO> items;
}

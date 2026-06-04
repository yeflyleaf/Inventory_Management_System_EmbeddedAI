package com.example.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 采购订单DTO
 */
@Data
public class PurchaseOrderDTO {
    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 采购商品列表
     */
    private List<PurchaseOrderItemDTO> items;
}

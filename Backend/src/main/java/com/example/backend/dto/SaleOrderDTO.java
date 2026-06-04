package com.example.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * 销售订单DTO
 */
@Data
public class SaleOrderDTO {
    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 销售商品列表
     */
    private List<SaleOrderItemDTO> items;
}

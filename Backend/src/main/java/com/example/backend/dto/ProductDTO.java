package com.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品信息DTO
 */
@Data
public class ProductDTO {
    /**
     * 商品ID (更新时必填)
     */
    private Long id;

    /**
     * 商品编码 (SKU)
     */
    private String sku;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 分类
     */
    private String category;

    /**
     * 单位
     */
    private String unit;

    /**
     * 条形码
     */
    private String barcode;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 图片URL列表 (支持多张图片，最多5张)
     */
    private List<String> imageUrls;
}

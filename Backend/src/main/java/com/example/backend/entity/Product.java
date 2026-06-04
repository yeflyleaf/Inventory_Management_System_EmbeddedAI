package com.example.backend.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 */
@Data
public class Product {
    /**
     * 商品ID
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
     * 图片URL列表
     */
    private List<String> imageUrls;  // 支持多张图片，最多5张

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

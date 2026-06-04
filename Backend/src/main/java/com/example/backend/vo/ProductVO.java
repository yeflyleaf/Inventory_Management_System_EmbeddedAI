package com.example.backend.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品信息视图对象
 * 用于前端展示商品详情
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) 
public class ProductVO {
    /** 商品ID */
    private Long id;
    /** 商品SKU */
    private String sku;
    /** 商品名称 */
    private String name;
    /** 商品分类 */
    private String category;
    /** 计量单位 */
    private String unit;
    /** 条形码 */
    private String barcode;
    /** 销售价格 */
    private BigDecimal salePrice;
    /** 商品图片URL列表 (最多5张) */
    private List<String> imageUrls;  // 支持多张图片，最多5张
    /** 创建时间 */
    private LocalDateTime createdAt;
}

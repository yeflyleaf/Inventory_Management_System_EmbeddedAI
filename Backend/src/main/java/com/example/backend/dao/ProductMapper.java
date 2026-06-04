package com.example.backend.dao;

import com.example.backend.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 商品数据访问接口
 */
@Mapper
public interface ProductMapper {
    /**
     * 插入商品
     */
    int insert(Product product);

    /**
     * 更新商品
     */
    int update(Product product);

    /**
     * 根据ID删除商品
     */
    int deleteById(Long id);

    /**
     * 根据ID查询商品
     */
    Product selectById(Long id);

    /**
     * 查询所有商品
     */
    List<Product> selectAll();

    /**
     * 根据名称统计商品数量(用于查重)
     */
    int countByName(@org.apache.ibatis.annotations.Param("name") String name, @org.apache.ibatis.annotations.Param("excludeId") Long excludeId);

    /**
     * 根据SKU统计商品数量(用于查重)
     */
    int countBySku(@org.apache.ibatis.annotations.Param("sku") String sku, @org.apache.ibatis.annotations.Param("excludeId") Long excludeId);
}

package com.example.backend.service;

import com.example.backend.entity.Product;

/**
 * 商品向量化嵌入服务接口
 */
public interface ProductEmbeddingService {

    /**
     * 更新或新增商品的向量数据
     * @param product 商品实体
     */
    void updateProductEmbedding(Product product);

    /**
     * 删除商品的向量数据
     * @param productId 商品ID
     */
    void deleteProductEmbedding(Long productId);

    /**
     * 全量重新同步所有商品数据到向量库
     */
    void reindexAllProducts();
}

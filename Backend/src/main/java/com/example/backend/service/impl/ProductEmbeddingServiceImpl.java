package com.example.backend.service.impl;

import com.example.backend.dao.ProductMapper;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductEmbeddingService;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductEmbeddingServiceImpl implements ProductEmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Async
    public void updateProductEmbedding(Product product) {
        if (product == null || product.getId() == null) return;

        // 1. 先删除可能存在的旧向量（避免重复）
        deleteProductEmbedding(product.getId());

        // 2. 构建结构化描述文本以便计算嵌入向量
        String text = String.format("商品名称: %s, SKU编码: %s, 商品分类: %s, 计量单位: %s, 条码: %s, 销售价格: %s元",
                product.getName(),
                product.getSku(),
                product.getCategory() != null ? product.getCategory() : "无",
                product.getUnit() != null ? product.getUnit() : "个",
                product.getBarcode() != null ? product.getBarcode() : "无",
                product.getSalePrice() != null ? product.getSalePrice().toString() : "0.00"
        );

        Metadata metadata = new Metadata();
        metadata.add("productId", product.getId().toString());
        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();

        // 3. 写入向量库
        embeddingStore.add(embedding, segment);
    }

    @Override
    @Async
    public void deleteProductEmbedding(Long productId) {
        if (productId == null) return;
        try {
            // 使用 metadata 中的 productId 作为 Filter 来精确删除
            dev.langchain4j.store.embedding.filter.Filter filter = 
                    dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey("productId")
                            .isEqualTo(productId.toString());
            embeddingStore.removeAll(filter);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to delete product embedding for ID " + productId + ": " + e.getMessage());
        }
    }

    @Override
    public void reindexAllProducts() {
        List<Product> products = productMapper.selectAll();
        for (Product product : products) {
            updateProductEmbedding(product);
        }
    }
}

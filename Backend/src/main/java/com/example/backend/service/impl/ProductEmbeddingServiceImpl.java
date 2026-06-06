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

        // 构建结构化描述文本以便计算嵌入向量
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

        // 使用 productId 作为显式 ID 存入向量库
        // 这样后续可以通过 embeddingStore.remove(id) 精确删除
        String embeddingId = "product-" + product.getId();
        embeddingStore.add(embeddingId, embedding, segment);
    }

    @Override
    @Async
    public void deleteProductEmbedding(Long productId) {
        if (productId == null) return;
        // 使用与 add 时一致的 ID 来删除向量
        String embeddingId = "product-" + productId;
        try {
            embeddingStore.remove(embeddingId);
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

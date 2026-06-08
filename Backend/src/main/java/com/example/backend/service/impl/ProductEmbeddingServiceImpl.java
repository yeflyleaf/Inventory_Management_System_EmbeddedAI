package com.example.backend.service.impl;

import com.example.backend.entity.Product;
import com.example.backend.service.ProductEmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 2026-06-06: 改造后的商品向量同步实现类
 * 将原本本地计算和存储向量的逻辑，改为通过 HTTP 异步调用 Python FastAPI 微服务向量端点
 */
@Service
public class ProductEmbeddingServiceImpl implements ProductEmbeddingService {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Async
    public void updateProductEmbedding(Product product) {
        if (product == null || product.getId() == null) return;
        try {
            String url = aiServiceUrl + "/ai/embedding/update";
            restTemplate.postForEntity(url, product, Void.class);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to sync product embedding update to AI Service: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void deleteProductEmbedding(Long productId) {
        if (productId == null) return;
        try {
            String url = aiServiceUrl + "/ai/embedding/delete?productId=" + productId;
            restTemplate.delete(url);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to sync product embedding delete to AI Service: " + e.getMessage());
        }
    }

    @Override
    public void reindexAllProducts() {
        try {
            String url = aiServiceUrl + "/ai/reindex";
            restTemplate.postForEntity(url, null, Void.class);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to trigger AI Service reindex: " + e.getMessage());
        }
    }
}

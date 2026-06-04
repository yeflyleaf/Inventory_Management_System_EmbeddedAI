package com.example.backend.aspect;

import com.example.backend.entity.Product;
import com.example.backend.service.ProductEmbeddingService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品向量库同步切面
 * 用于在商品新增、更新、删除成功后，透明且异步地同步向量库
 */
@Aspect
@Component
public class ProductEmbeddingAspect {

    @Autowired
    private ProductEmbeddingService productEmbeddingService;

    /**
     * 拦截新增商品，成功返回后异步更新向量
     *
     * @param product 成功保存的商品实体
     */
    @AfterReturning(pointcut = "execution(* com.example.backend.service.ProductService.addProduct(..))", returning = "product")
    public void afterAddProduct(Product product) {
        if (product != null) {
            productEmbeddingService.updateProductEmbedding(product);
        }
    }

    /**
     * 拦截更新商品，成功返回后异步更新向量
     *
     * @param product 成功修改的商品实体
     */
    @AfterReturning(pointcut = "execution(* com.example.backend.service.ProductService.updateProduct(..))", returning = "product")
    public void afterUpdateProduct(Product product) {
        if (product != null) {
            productEmbeddingService.updateProductEmbedding(product);
        }
    }

    /**
     * 拦截删除商品，成功执行后异步清理向量
     *
     * @param productId 删除商品的ID
     */
    @AfterReturning(pointcut = "execution(* com.example.backend.service.ProductService.deleteProduct(..)) && args(productId)")
    public void afterDeleteProduct(Long productId) {
        if (productId != null) {
            productEmbeddingService.deleteProductEmbedding(productId);
        }
    }
}

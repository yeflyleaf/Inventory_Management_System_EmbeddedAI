package com.example.backend.service;

import com.example.backend.dto.ProductDTO;
import com.example.backend.vo.ProductVO;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {
    /**
     * 新增商品
     * @param productDTO 商品信息
     */
    void addProduct(ProductDTO productDTO);

    /**
     * 更新商品
     * @param productDTO 商品信息
     */
    void updateProduct(ProductDTO productDTO);

    /**
     * 删除商品
     * @param id 商品ID
     */
    void deleteProduct(Long id);

    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getProductById(Long id);

    /**
     * 获取所有商品
     * @return 商品列表
     */
    List<ProductVO> getAllProducts();
}

package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.backend.dao.ProductMapper;
import com.example.backend.dto.ProductDTO;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;
import com.example.backend.service.ProductEmbeddingService;
import com.example.backend.utils.FileUtils;
import com.example.backend.vo.ProductVO;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private ProductEmbeddingService productEmbeddingService;

    /**
     * 添加新商品
     * 1. 检查商品名称是否重复
     * 2. 检查商品编码(SKU)是否重复
     * 3. 处理商品图片：将临时目录的图片移动到正式目录
     * 4. 保存商品信息到数据库
     * 5. 清空 'products' 缓存
     *
     * @param productDTO 商品信息传输对象
     * @throws RuntimeException 如果名称或SKU已存在
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(ProductDTO productDTO) {
        if (productMapper.countByName(productDTO.getName(), null) > 0) {
            throw new RuntimeException("商品名称已存在");
        }
        if (productMapper.countBySku(productDTO.getSku(), null) > 0) {
            throw new RuntimeException("商品编码已存在");
        }
        Product product = new Product();
        product.setSku(productDTO.getSku());
        product.setName(productDTO.getName());
        product.setCategory(productDTO.getCategory());
        product.setUnit(productDTO.getUnit());
        product.setBarcode(productDTO.getBarcode());
        product.setSalePrice(productDTO.getSalePrice());
        
        // 处理图片：如果是临时图片，移动到正式目录（支持多图片）
        List<String> finalImageUrls = fileUtils.moveFilesFromTempToProduct(productDTO.getImageUrls());
        product.setImageUrls(finalImageUrls);
        
        product.setCreatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return product;
    }

    /**
     * 更新商品信息
     * 1. 检查名称和SKU是否与其他商品冲突
     * 2. 处理图片更新：删除被移除的旧图片，移动新上传的图片
     * 3. 更新数据库记录
     * 4. 清空 'products' 缓存
     *
     * @param productDTO 商品信息传输对象，必须包含ID
     * @throws RuntimeException 如果名称或SKU与其他商品冲突
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(ProductDTO productDTO) {
        if (productMapper.countByName(productDTO.getName(), productDTO.getId()) > 0) {
            throw new RuntimeException("商品名称已存在");
        }
        if (productMapper.countBySku(productDTO.getSku(), productDTO.getId()) > 0) {
            throw new RuntimeException("商品编码已存在");
        }
        Product product = productMapper.selectById(productDTO.getId());
        if (product != null) {
            // 检查是否更换了图片，删除被移除的旧图片
            List<String> oldImageUrls = product.getImageUrls();
            List<String> newImageUrls = productDTO.getImageUrls();
            
            // 删除不在新列表中的旧图片
            fileUtils.deleteRemovedImages(oldImageUrls, newImageUrls);

            product.setSku(productDTO.getSku());
            product.setName(productDTO.getName());
            product.setCategory(productDTO.getCategory());
            product.setUnit(productDTO.getUnit());
            product.setBarcode(productDTO.getBarcode());
            product.setSalePrice(productDTO.getSalePrice());
            
            // 处理图片：如果是临时图片，移动到正式目录（支持多图片）
            List<String> finalImageUrls = fileUtils.moveFilesFromTempToProduct(newImageUrls);
            product.setImageUrls(finalImageUrls);
            
            productMapper.update(product);
        }
        return product;
    }

    /**
     * 删除商品
     * 1. 删除该商品关联的所有物理图片文件
     * 2. 删除数据库中的商品记录
     * 3. 清空 'products' 缓存
     *
     * @param id 商品ID
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        // 先获取商品信息，检查是否有图片需要删除
        Product product = productMapper.selectById(id);
        if (product != null && product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            // 删除所有本地图片文件
            fileUtils.deleteImageFiles(product.getImageUrls());
        }

        // 删除数据库记录
        productMapper.deleteById(id);
    }

    /**
     * 根据ID获取商品详情
     * 优先从缓存 'products' 中获取，key为 'product:' + id
     *
     * @param id 商品ID
     * @return 商品VO对象，如果不存在则返回null
     */
    @Override
    @Cacheable(value = "products", key = "'product:' + #id")
    public ProductVO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        return convertToVO(product);
    }

    /**
     * 获取所有商品列表
     * 优先从缓存 'products' 中获取，key为 'all'
     *
     * @return 所有商品的VO列表
     */
    @Override
    @Cacheable(value = "products", key = "'all'")
    public List<ProductVO> getAllProducts() {
        return productMapper.selectAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 Product 实体转换为 ProductVO 对象
     *
     * @param product Product实体对象
     * @return ProductVO对象，如果输入为null则返回null
     */
    private ProductVO convertToVO(Product product) {
        if (product == null)
            return null;
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setSku(product.getSku());
        vo.setName(product.getName());
        vo.setCategory(product.getCategory());
        vo.setUnit(product.getUnit());
        vo.setBarcode(product.getBarcode());
        vo.setSalePrice(product.getSalePrice());
        vo.setImageUrls(product.getImageUrls());
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }
}


package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.dto.ProductDTO;
import com.example.backend.service.ProductService;
import com.example.backend.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 商品管理控制器
 * 提供商品的增删改查功能
 */
@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取所有商品列表
     * @return 商品列表
     */
    @GetMapping
    public Result<List<ProductVO>> getAll() {
        List<ProductVO> products = productService.getAllProducts();
        return Result.success(products, "获取产品列表成功");
    }

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        ProductVO product = productService.getProductById(id);
        return Result.success(product, "获取产品详情成功");
    }

    /**
     * 新增商品
     * @param productDTO 商品信息
     * @return 成功信息
     */
    @PostMapping
    @Log(module = "商品管理", action = "新增商品", description = "新增商品")
    public Result<Void> add(@RequestBody ProductDTO productDTO) {
        productService.addProduct(productDTO);
        return Result.success(null, "添加产品成功");
    }

//    @PutMapping("/{id}")
//    public Result<Void> update(@PathVariable Long id,@RequestBody ProductDTO productDTO) {
//        productDTO.setId(id);
//        productService.updateProduct(productDTO);
//        return Result.success(null, "更新产品成功");
//    }
    /**
     * 更新商品信息
     * @param productDTO 商品信息
     * @return 成功信息
     */
    @PutMapping("/{id}")
    @Log(module = "商品管理", action = "更新商品", description = "更新商品信息")
    public Result<Void> update(@RequestBody ProductDTO productDTO) {
        productService.updateProduct(productDTO);
        return Result.success(null, "更新产品成功");
    }

    /**
     * 删除商品
     * @param id 商品ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Log(module = "商品管理", action = "删除商品", description = "删除商品")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success(null, "删除产品成功");
    }
}

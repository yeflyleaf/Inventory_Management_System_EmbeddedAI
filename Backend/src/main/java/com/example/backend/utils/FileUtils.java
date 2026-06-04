package com.example.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件操作工具类
 * 提供文件的删除、移动、清理等功能
 * 主要用于处理图片上传和管理
 */
@Component
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 根据图片URL删除本地图片文件
     *
     * @param imageUrl 图片URL，如 "/uploads/products/xxx.png"
     * @return true: 删除成功或文件不存在; false: 删除失败
     */
    public boolean deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            logger.info("图片URL为空，无需删除文件");
            return true;
        }

        try {
            // 从URL中提取相对路径，如 "/uploads/products/xxx.png" -> "products/xxx.png"
            String relativePath = imageUrl;
            if (imageUrl.startsWith("/uploads/")) {
                relativePath = imageUrl.substring("/uploads/".length());
            }

            // 构建完整的文件路径
            Path filePath = Paths.get(uploadPath, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("成功删除图片文件: {}", filePath);
                return true;
            } else {
                logger.warn("图片文件不存在: {}", filePath);
                return true; // 文件不存在也算成功
            }
        } catch (IOException e) {
            logger.error("删除图片文件失败: {}, 错误: {}", imageUrl, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据文件名和目录删除本地文件
     *
     * @param directory 目录名，如 "products"
     * @param filename  文件名，如 "xxx.png"
     * @return true: 删除成功或文件不存在; false: 删除失败
     */
    public boolean deleteFile(String directory, String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            logger.info("文件名为空，无需删除文件");
            return true;
        }

        try {
            Path filePath = Paths.get(uploadPath, directory, filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("成功删除文件: {}", filePath);
                return true;
            } else {
                logger.warn("文件不存在: {}", filePath);
                return true; // 文件不存在也算成功
            }
        } catch (IOException e) {
            logger.error("删除文件失败: {}/{}, 错误: {}", directory, filename, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将临时目录的图片移动到商品图片目录
     * 如果图片URL不包含 "/temp/"，则认为是已有正式图片，直接返回原URL
     *
     * @param tempImageUrl 临时图片URL
     * @return 移动后的正式图片URL，如果不是临时图片则返回原URL
     */
    public String moveFileFromTempToProduct(String tempImageUrl) {
        if (tempImageUrl == null || !tempImageUrl.contains("/temp/")) {
            return tempImageUrl;
        }

        try {
            // 提取文件名
            String filename = tempImageUrl.substring(tempImageUrl.lastIndexOf("/") + 1);
            
            // 源文件路径 (temp)
            Path sourcePath = Paths.get(uploadPath, "temp", filename);
            
            // 目标目录 (products)
            Path targetDir = Paths.get(uploadPath, "products");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            
            // 目标文件路径
            Path targetPath = targetDir.resolve(filename);
            
            // 移动文件
            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("成功将图片从临时目录移动到正式目录: {}", filename);
                return "/uploads/products/" + filename;
            } else {
                logger.warn("临时文件不存在: {}", sourcePath);
                return tempImageUrl;
            }
        } catch (IOException e) {
            logger.error("移动图片文件失败: {}, 错误: {}", tempImageUrl, e.getMessage(), e);
            return tempImageUrl;
        }
    }

    /**
     * 批量将临时目录的图片移动到商品图片目录
     *
     * @param tempImageUrls 临时图片URL列表
     * @return 移动后的正式图片URL列表
     */
    public java.util.List<String> moveFilesFromTempToProduct(java.util.List<String> tempImageUrls) {
        if (tempImageUrls == null || tempImageUrls.isEmpty()) {
            return tempImageUrls;
        }
        
        java.util.List<String> finalUrls = new java.util.ArrayList<>();
        for (String url : tempImageUrls) {
            finalUrls.add(moveFileFromTempToProduct(url));
        }
        return finalUrls;
    }

    /**
     * 批量删除图片文件
     *
     * @param imageUrls 图片URL列表
     * @return true: 全部删除成功; false: 存在删除失败的情况
     */
    public boolean deleteImageFiles(java.util.List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return true;
        }
        
        boolean allSuccess = true;
        for (String url : imageUrls) {
            if (!deleteImageFile(url)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    /**
     * 查找需要删除的旧图片（存在于旧列表但不在新列表中）
     * 用于在更新操作时清理不再使用的图片
     *
     * @param oldUrls 旧图片URL列表
     * @param newUrls 新图片URL列表
     */
    public void deleteRemovedImages(java.util.List<String> oldUrls, java.util.List<String> newUrls) {
        if (oldUrls == null || oldUrls.isEmpty()) {
            return;
        }
        
        java.util.Set<String> newUrlSet = newUrls != null ? 
            new java.util.HashSet<>(newUrls) : new java.util.HashSet<>();
        
        for (String oldUrl : oldUrls) {
            if (oldUrl != null && !oldUrl.trim().isEmpty() && !newUrlSet.contains(oldUrl)) {
                deleteImageFile(oldUrl);
                logger.info("删除被替换的旧图片: {}", oldUrl);
            }
        }
    }
}

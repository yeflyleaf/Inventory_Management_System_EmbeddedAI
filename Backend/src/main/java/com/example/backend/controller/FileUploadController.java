package com.example.backend.controller;

import com.example.backend.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 处理文件上传请求
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传商品图片
     * @param file 图片文件
     * @return 上传结果(包含URL)
     */
    @PostMapping("/product-image")
    public Result<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 检查文件大小 (最大20MB)
        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error("图片大小不能超过20MB");
        }

        try {
            // 创建上传目录 (改为临时目录)
            String tempPath = uploadPath + "/temp";
            Path uploadDir = Paths.get(tempPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 返回访问URL (临时路径)
            String imageUrl = "/uploads/temp/" + newFilename;
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);

            return Result.success(data, "图片上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品图片 (兼容正式目录和临时目录)
     * @param filename 文件名
     * @return 成功信息
     */
    @DeleteMapping("/product-image")
    public Result<Void> deleteProductImage(@RequestParam("filename") String filename) {
        try {
            // 尝试在 products 目录查找
            Path productFilePath = Paths.get(uploadPath, "products", filename);
            // 尝试在 temp 目录查找
            Path tempFilePath = Paths.get(uploadPath, "temp", filename);
            
            boolean deleted = false;
            
            if (Files.exists(tempFilePath)) {
                Files.delete(tempFilePath);
                deleted = true;
            } else if (Files.exists(productFilePath)) {
                Files.delete(productFilePath);
                deleted = true;
            }
            
            if (deleted) {
                return Result.success(null, "图片删除成功");
            } else {
                return Result.error("图片不存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("图片删除失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     * @param file 头像文件
     * @return 上传结果(包含URL)
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的头像");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        // 检查文件大小 (最大20MB)
        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error("头像大小不能超过20MB");
        }

        try {
            // 创建头像目录
            String avatarPath = uploadPath + "/avatars";
            Path uploadDir = Paths.get(avatarPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 返回访问URL
            String imageUrl = "/uploads/avatars/" + newFilename;
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);

            return Result.success(data, "头像上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}

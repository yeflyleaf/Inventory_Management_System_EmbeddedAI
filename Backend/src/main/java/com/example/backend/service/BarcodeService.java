package com.example.backend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 条形码识别服务接口
 */
public interface BarcodeService {
    
    /**
     * 从上传的图片中识别条形码
     * @param file 包含条形码的图片文件
     * @return 识别到的条形码内容，如果未识别到则返回null
     */
    String recognizeBarcodeFromImage(MultipartFile file);
}

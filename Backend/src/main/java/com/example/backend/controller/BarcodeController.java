package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.BarcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 条形码识别控制器
 */
@RestController
@RequestMapping("/barcode")
@CrossOrigin
public class BarcodeController {

    @Autowired
    private BarcodeService barcodeService;

    /**
     * 从上传的图片中识别条形码
     * @param file 包含条形码的图片文件
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public Result<Map<String, Object>> recognizeBarcode(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请上传图片文件");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "请上传有效的图片文件");
        }
        
        String barcode = barcodeService.recognizeBarcodeFromImage(file);
        
        Map<String, Object> data = new HashMap<>();
        if (barcode != null && !barcode.isEmpty()) {
            data.put("success", true);
            data.put("barcode", barcode);
            return Result.success(data, "条形码识别成功");
        } else {
            data.put("success", false);
            data.put("barcode", null);
            return Result.success(data, "未能识别条形码，请确保图片清晰且包含有效的条形码");
        }
    }
}

package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

/**
 * 系统公共控制器
 * 提供无需特定权限即可访问的系统级别接口
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * 获取系统公共设置
     * 如公司名称、系统配置等
     */
    @GetMapping("/settings")
    public Result<Map<String, String>> getSettings() {
        Map<String, String> settings = new HashMap<>(systemSettingService.getAllAsMap());
        settings.remove("ai_api_key"); // 移除AI API Key，避免非管理员泄露
        return Result.success(settings);
    }
}

package com.example.backend.service;

import com.example.backend.entity.SystemSetting;

import java.util.List;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemSettingService {
    
    /**
     * 获取所有设置
     */
    List<SystemSetting> findAll();
    
    /**
     * 获取单个设置值
     */
    String getValue(String key);
    
    /**
     * 获取设置值（带默认值）
     */
    String getValue(String key, String defaultValue);
    
    /**
     * 获取整数设置值
     */
    Integer getIntValue(String key, Integer defaultValue);
    
    /**
     * 获取布尔设置值
     */
    Boolean getBooleanValue(String key, Boolean defaultValue);
    
    /**
     * 更新设置
     */
    void updateValue(String key, String value);
    
    /**
     * 批量更新设置
     */
    void batchUpdate(Map<String, String> settings);
    
    /**
     * 获取所有设置（转为Map）
     */
    Map<String, String> getAllAsMap();
}

package com.example.backend.service.impl;

import com.example.backend.dao.SystemSettingMapper;
import com.example.backend.entity.SystemSetting;
import com.example.backend.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现类
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;
    
    /**
     * 获取所有系统设置
     * 优先从缓存 'systemSettings' 中获取，key为 'all'
     *
     * @return 所有系统设置列表
     */
    @Override
    @Cacheable(value = "systemSettings", key = "'all'")
    public List<SystemSetting> findAll() {
        return systemSettingMapper.findAll();
    }
    
    /**
     * 根据键获取设置值
     * 优先从缓存 'systemSettings' 中获取，key为参数key
     *
     * @param key 设置键
     * @return 设置值，如果不存在则返回null
     */
    @Override
    @Cacheable(value = "systemSettings", key = "#key")
    public String getValue(String key) {
        SystemSetting setting = systemSettingMapper.findByKey(key);
        return setting != null ? setting.getSettingValue() : null;
    }
    
    /**
     * 根据键获取设置值，如果不存在则返回默认值
     *
     * @param key          设置键
     * @param defaultValue 默认值
     * @return 设置值或默认值
     */
    @Override
    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取整数类型的设置值
     * 如果解析失败或不存在，则返回默认值
     *
     * @param key          设置键
     * @param defaultValue 默认整数值
     * @return 整数设置值或默认值
     */
    @Override
    public Integer getIntValue(String key, Integer defaultValue) {
        String value = getValue(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 获取布尔类型的设置值
     * 支持 "true" (忽略大小写) 或 "1" 为真
     *
     * @param key          设置键
     * @param defaultValue 默认布尔值
     * @return 布尔设置值或默认值
     */
    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getValue(key);
        if (value != null) {
            return "true".equalsIgnoreCase(value) || "1".equals(value);
        }
        return defaultValue;
    }
    
    /**
     * 更新或创建设置值
     * 如果设置不存在，则自动创建并设置默认描述
     * 更新后清空 'systemSettings' 缓存
     *
     * @param key   设置键
     * @param value 设置值
     */
    @Override
    @CacheEvict(value = "systemSettings", allEntries = true)
    public void updateValue(String key, String value) {
        if ("ai_api_key".equals(key)) {
            if ("******".equals(value)) {
                return; // 掩码占位符，说明未修改，直接返回
            }
            if (value != null && !value.trim().isEmpty()) {
                value = com.example.backend.utils.EncryptionUtils.encrypt(value.trim());
            }
        }
        SystemSetting existing = systemSettingMapper.findByKey(key);
        if (existing == null) {
            SystemSetting newSetting = new SystemSetting();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(value);
            newSetting.setSettingType("string"); // Default type
            if ("ai_api_key".equals(key)) {
                newSetting.setDescription("AI API密钥");
            } else {
                newSetting.setDescription("Auto created setting");
            }
            systemSettingMapper.insert(newSetting);
        } else {
            systemSettingMapper.updateValue(key, value);
        }
    }
    
    /**
     * 批量更新设置
     * 遍历Map并逐个调用 updateValue
     * 更新后清空 'systemSettings' 缓存
     *
     * @param settings 包含键值对的Map
     */
    @Override
    @CacheEvict(value = "systemSettings", allEntries = true)
    public void batchUpdate(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateValue(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 获取所有设置并转换为Map格式
     * 方便前端一次性获取所有配置
     * 优先从缓存 'systemSettings' 中获取，key为 'map'
     *
     * @return 包含所有设置键值对的Map
     */
    @Override
    @Cacheable(value = "systemSettings", key = "'map'")
    public Map<String, String> getAllAsMap() {
        List<SystemSetting> settings = findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemSetting setting : settings) {
            map.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return map;
    }
}

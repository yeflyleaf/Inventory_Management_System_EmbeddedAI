package com.example.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 * 使用Redis存储和管理用户token
 */
@Component
public class JwtUtils {
    
    @Autowired
    private RedisUtils redisUtils;
    
    // Token在Redis中的前缀
    private static final String TOKEN_PREFIX = "token:";
    
    // Token过期时间：7天（秒）
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;

    /**
     * 生成Token并存储到Redis
     * 默认角色为 "USER"
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 生成的Token字符串
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "USER");
    }
    
    /**
     * 生成Token并存储到Redis（带角色）
     * Token作为Key，用户信息作为Value存储在Redis中
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return 生成的Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        // 生成唯一的token
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 构建用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        userInfo.put("role", role);
        userInfo.put("createTime", System.currentTimeMillis());
        
        // 存储到Redis，设置7天过期
        String redisKey = TOKEN_PREFIX + token;
        redisUtils.set(redisKey, userInfo, TOKEN_EXPIRE_TIME);
        
        return token;
    }

    /**
     * 验证Token是否有效
     * 通过检查Redis中是否存在该Token对应的Key来判断
     *
     * @param token Token字符串
     * @return true: 有效; false: 无效或已过期
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String redisKey = TOKEN_PREFIX + token;
        return redisUtils.hasKey(redisKey);
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token Token字符串
     * @return 用户ID，如果Token无效或解析失败则返回null
     */
    public Long getUserIdFromToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            Object userInfoObj = redisUtils.get(redisKey);
            if (userInfoObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                Object userIdObj = userInfo.get("userId");
                if (userIdObj instanceof Number) {
                    return ((Number) userIdObj).longValue();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 从Token中获取用户名
     *
     * @param token Token字符串
     * @return 用户名，如果Token无效或解析失败则返回null
     */
    public String getUsernameFromToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            Object userInfoObj = redisUtils.get(redisKey);
            if (userInfoObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                return (String) userInfo.get("username");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 刷新Token过期时间
     * 如果Token有效，则重置其在Redis中的过期时间
     *
     * @param token Token字符串
     */
    public void refreshToken(String token) {
        if (validateToken(token)) {
            String redisKey = TOKEN_PREFIX + token;
            redisUtils.expire(redisKey, TOKEN_EXPIRE_TIME);
        }
    }
    
    /**
     * 删除Token（用户登出）
     * 从Redis中移除该Token
     *
     * @param token Token字符串
     */
    public void removeToken(String token) {
        if (token != null && !token.isEmpty()) {
            String redisKey = TOKEN_PREFIX + token;
            redisUtils.del(redisKey);
        }
    }
    
    /**
     * 从Token中获取用户角色
     *
     * @param token Token字符串
     * @return 用户角色，如果Token无效或解析失败则返回null
     */
    public String getRoleFromToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            Object userInfoObj = redisUtils.get(redisKey);
            if (userInfoObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                return (String) userInfo.get("role");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查Token对应的用户是否为管理员
     *
     * @param token Token字符串
     * @return true: 是管理员; false: 不是管理员
     */
    public boolean isAdmin(String token) {
        String role = getRoleFromToken(token);
        return "ADMIN".equalsIgnoreCase(role);
    }
}


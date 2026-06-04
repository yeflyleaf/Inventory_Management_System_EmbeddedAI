package com.example.backend.utils;

import java.util.UUID;

/**
 * ID生成工具类
 * 用于生成各种业务单据的唯一编号
 */
public class IdGenerator {
    /**
     * 生成订单编号
     * 格式：前缀 + 当前时间戳 + 4位随机UUID片段
     *
     * @param prefix 订单前缀 (如 "PO", "SO")
     * @return 生成的唯一订单编号
     */
    public static String generateOrderNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

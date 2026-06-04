package com.example.backend.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供日期时间的格式化和解析功能
 */
public class DateUtils {
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将 LocalDateTime 格式化为字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     *
     * @param date LocalDateTime对象
     * @return 格式化后的日期字符串，如果输入为null则返回null
     */
    public static String format(LocalDateTime date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern(STANDARD_FORMAT));
    }

    /**
     * 将字符串解析为 LocalDateTime
     * 格式：yyyy-MM-dd HH:mm:ss
     *
     * @param dateStr 日期字符串
     * @return LocalDateTime对象，如果输入为null则返回null
     */
    public static LocalDateTime parse(String dateStr) {
        if (dateStr == null) return null;
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(STANDARD_FORMAT));
    }
}

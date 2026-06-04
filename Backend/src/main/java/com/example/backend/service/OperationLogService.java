package com.example.backend.service;

import com.example.backend.entity.OperationLog;
import com.example.backend.vo.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {
    
    /**
     * 记录操作日志
     */
    void log(OperationLog log);
    
    /**
     * 分页查询操作日志
     */
    PageResult<OperationLog> findByPage(Long userId, String username, String module, 
                                         String action, LocalDateTime startTime, 
                                         LocalDateTime endTime, int page, int size);
    
    /**
     * 获取所有模块名称
     */
    List<String> findAllModules();
    
    /**
     * 清理旧日志（保留指定天数）
     */
    int cleanOldLogs(int keepDays);
    
    /**
     * 统计今日操作数
     */
    int countToday();
    
    /**
     * 获取最近的操作日志
     */
    List<OperationLog> findRecent(int limit);
}

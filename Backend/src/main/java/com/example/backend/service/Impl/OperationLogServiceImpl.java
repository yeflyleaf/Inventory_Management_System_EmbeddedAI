package com.example.backend.service.impl;

import com.example.backend.dao.OperationLogMapper;
import com.example.backend.entity.OperationLog;
import com.example.backend.service.OperationLogService;
import com.example.backend.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    /**
     * 异步记录操作日志
     * 如果日志时间为空，则自动设置为当前时间
     * 使用 @Async 注解实现异步处理，不阻塞主线程
     *
     * @param log 操作日志实体对象
     */
    @Override
    @Async
    public void log(OperationLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        operationLogMapper.insert(log);
    }
    
    /**
     * 分页查询操作日志
     * 支持多条件组合查询：用户ID、用户名、模块、操作类型、时间范围
     *
     * @param userId    用户ID (可选)
     * @param username  用户名 (可选)
     * @param module    模块名称 (可选)
     * @param action    操作类型 (可选)
     * @param startTime 开始时间 (可选)
     * @param endTime   结束时间 (可选)
     * @param page      页码 (从1开始)
     * @param size      每页大小
     * @return 包含日志列表和总记录数的分页结果对象
     */
    @Override
    public PageResult<OperationLog> findByPage(Long userId, String username, String module,
                                                String action, LocalDateTime startTime,
                                                LocalDateTime endTime, int page, int size) {
        int offset = (page - 1) * size;
        List<OperationLog> logs = operationLogMapper.findByCondition(userId, username, module, 
                                                                      action, startTime, endTime, 
                                                                      offset, size);
        int total = operationLogMapper.countByCondition(userId, username, module, 
                                                         action, startTime, endTime);
        return new PageResult<>(logs, total, page, size);
    }
    
    /**
     * 获取所有已记录的模块名称列表
     * 用于前端筛选下拉框
     *
     * @return 模块名称字符串列表
     */
    @Override
    public List<String> findAllModules() {
        return operationLogMapper.findAllModules();
    }
    
    /**
     * 清理指定天数之前的旧日志
     *
     * @param keepDays 保留最近的天数
     * @return 被删除的日志条数
     */
    @Override
    public int cleanOldLogs(int keepDays) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(keepDays);
        return operationLogMapper.deleteOlderThan(beforeDate);
    }
    
    /**
     * 统计今日产生的操作日志数量
     *
     * @return 今日日志总数
     */
    @Override
    public int countToday() {
        return operationLogMapper.countToday();
    }
    
    /**
     * 获取最近的几条操作日志
     * 通常用于仪表盘显示
     *
     * @param limit 获取条数限制
     * @return 最近的操作日志列表
     */
    @Override
    public List<OperationLog> findRecent(int limit) {
        return operationLogMapper.findRecent(limit);
    }
}

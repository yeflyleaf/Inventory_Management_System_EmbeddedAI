package com.example.backend.dao;

import com.example.backend.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志数据访问接口
 */
@Mapper
public interface OperationLogMapper {
    
    /**
     * 插入操作日志
     */
    int insert(OperationLog log);
    
    /**
     * 分页查询操作日志
     */
    List<OperationLog> findByCondition(@Param("userId") Long userId,
                                        @Param("username") String username,
                                        @Param("module") String module,
                                        @Param("action") String action,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("offset") int offset,
                                        @Param("size") int size);
    
    /**
     * 统计总数
     */
    int countByCondition(@Param("userId") Long userId,
                         @Param("username") String username,
                         @Param("module") String module,
                         @Param("action") String action,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取所有模块名称
     */
    List<String> findAllModules();
    
    /**
     * 清理旧日志
     */
    int deleteOlderThan(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * 统计今日操作数
     */
    int countToday();
    
    /**
     * 获取最近的操作日志
     */
    List<OperationLog> findRecent(@Param("limit") int limit);
}

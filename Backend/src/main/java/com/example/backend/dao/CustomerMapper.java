package com.example.backend.dao;

import com.example.backend.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 客户数据访问接口
 */
@Mapper
public interface CustomerMapper {
    /**
     * 插入客户
     */
    int insert(Customer customer);

    /**
     * 更新客户
     */
    int update(Customer customer);

    /**
     * 根据ID删除客户
     */
    int deleteById(Long id);

    /**
     * 根据ID查询客户
     */
    Customer selectById(Long id);

    /**
     * 查询所有客户
     */
    List<Customer> selectAll();

    /**
     * 根据名称统计客户数量(用于查重)
     */
    int countByName(@org.apache.ibatis.annotations.Param("name") String name, @org.apache.ibatis.annotations.Param("excludeId") Long excludeId);
}

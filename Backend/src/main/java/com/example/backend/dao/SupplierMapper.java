package com.example.backend.dao;

import com.example.backend.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 供应商数据访问接口
 */
@Mapper
public interface SupplierMapper {
    /**
     * 插入供应商
     */
    int insert(Supplier supplier);

    /**
     * 更新供应商
     */
    int update(Supplier supplier);

    /**
     * 根据ID删除供应商
     */
    int deleteById(Long id);

    /**
     * 根据ID查询供应商
     */
    Supplier selectById(Long id);

    /**
     * 查询所有供应商
     */
    List<Supplier> selectAll();

    /**
     * 根据名称统计供应商数量(用于查重)
     */
    int countByName(@org.apache.ibatis.annotations.Param("name") String name, @org.apache.ibatis.annotations.Param("excludeId") Long excludeId);
}

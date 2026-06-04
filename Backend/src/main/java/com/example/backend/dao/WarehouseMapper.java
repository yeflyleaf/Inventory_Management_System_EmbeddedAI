package com.example.backend.dao;

import com.example.backend.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 仓库数据访问接口
 */
@Mapper
public interface WarehouseMapper {
    /**
     * 插入仓库
     */
    int insert(Warehouse warehouse);

    /**
     * 更新仓库
     */
    int update(Warehouse warehouse);

    /**
     * 根据ID删除仓库
     */
    int deleteById(Long id);

    /**
     * 根据ID查询仓库
     */
    Warehouse selectById(Long id);

    /**
     * 查询所有仓库
     */
    List<Warehouse> selectAll();
}

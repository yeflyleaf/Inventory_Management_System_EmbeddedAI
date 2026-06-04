package com.example.backend.service;

import com.example.backend.dto.WarehouseDTO;
import com.example.backend.vo.WarehouseVO;
import java.util.List;

/**
 * 仓库服务接口
 */
public interface WarehouseService {
    /**
     * 添加仓库
     * @param warehouseDTO 仓库信息DTO
     */
    void addWarehouse(WarehouseDTO warehouseDTO);

    /**
     * 更新仓库信息
     * @param warehouseDTO 仓库信息DTO
     */
    void updateWarehouse(WarehouseDTO warehouseDTO);

    /**
     * 删除仓库
     * @param id 仓库ID
     */
    void deleteWarehouse(Long id);

    /**
     * 根据ID获取仓库信息
     * @param id 仓库ID
     * @return 仓库VO
     */
    WarehouseVO getWarehouseById(Long id);

    /**
     * 获取所有仓库列表
     * @return 仓库VO列表
     */
    List<WarehouseVO> getAllWarehouses();
}

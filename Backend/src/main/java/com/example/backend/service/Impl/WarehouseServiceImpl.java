package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.dao.WarehouseMapper;
import com.example.backend.dto.WarehouseDTO;
import com.example.backend.entity.Warehouse;
import com.example.backend.service.WarehouseService;
import com.example.backend.vo.WarehouseVO;

/**
 * 仓库服务实现类
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    /**
     * 添加新仓库
     *
     * @param warehouseDTO 仓库信息DTO
     */
    @Override
    public void addWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseDTO.getName());
        warehouse.setAddress(warehouseDTO.getAddress());
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouseMapper.insert(warehouse);
    }

    /**
     * 更新仓库信息
     *
     * @param warehouseDTO 仓库信息DTO，必须包含ID
     */
    @Override
    public void updateWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = warehouseMapper.selectById(warehouseDTO.getId());
        if (warehouse != null) {
            warehouse.setName(warehouseDTO.getName());
            warehouse.setAddress(warehouseDTO.getAddress());
            warehouseMapper.update(warehouse);
        }
    }

    /**
     * 根据ID删除仓库
     *
     * @param id 仓库ID
     */
    @Override
    public void deleteWarehouse(Long id) {
        warehouseMapper.deleteById(id);
    }

    /**
     * 根据ID获取仓库详情
     *
     * @param id 仓库ID
     * @return 仓库VO对象
     */
    @Override
    public WarehouseVO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseMapper.selectById(id);
        return convertToVO(warehouse);
    }

    /**
     * 获取所有仓库列表
     *
     * @return 所有仓库VO列表
     */
    @Override
    public List<WarehouseVO> getAllWarehouses() {
        return warehouseMapper.selectAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 Warehouse 实体转换为 WarehouseVO 对象
     *
     * @param warehouse Warehouse实体对象
     * @return WarehouseVO对象
     */
    private WarehouseVO convertToVO(Warehouse warehouse) {
        if (warehouse == null)
            return null;
        WarehouseVO vo = new WarehouseVO();
        vo.setId(warehouse.getId());
        vo.setName(warehouse.getName());
        vo.setAddress(warehouse.getAddress());
        vo.setCreatedAt(warehouse.getCreatedAt());
        return vo;
    }
}

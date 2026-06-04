package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.WarehouseDTO;
import com.example.backend.service.WarehouseService;
import com.example.backend.vo.WarehouseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 仓库管理控制器
 * 提供仓库的增删改查功能
 */
@RestController
@RequestMapping("/warehouses")
@CrossOrigin
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 获取所有仓库列表
     * @return 仓库列表
     */
    @GetMapping
    public Result<List<WarehouseVO>> getAll() {
        List<WarehouseVO> warehouses = warehouseService.getAllWarehouses();
        return Result.success(warehouses, "获取仓库列表成功");
    }

    /**
     * 新增仓库
     * @param warehouseDTO 仓库信息
     * @return 成功信息
     */
    @PostMapping
    public Result<Void> add(@RequestBody WarehouseDTO warehouseDTO) {
        warehouseService.addWarehouse(warehouseDTO);
        return Result.success(null, "添加仓库成功");
    }

    /**
     * 更新仓库信息
     * @param warehouseDTO 仓库信息
     * @return 成功信息
     */
    @PutMapping
    public Result<Void> update(@RequestBody WarehouseDTO warehouseDTO) {
        warehouseService.updateWarehouse(warehouseDTO);
        return Result.success(null, "更新仓库成功");
    }

    /**
     * 删除仓库
     * @param id 仓库ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success(null, "删除仓库成功");
    }
}

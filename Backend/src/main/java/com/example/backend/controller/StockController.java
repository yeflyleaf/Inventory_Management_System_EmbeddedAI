package com.example.backend.controller;

import com.example.backend.annotation.Log;
import com.example.backend.common.Result;
import com.example.backend.service.StockService;
import com.example.backend.vo.StockFlowVO;
import com.example.backend.vo.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 库存管理控制器
 * 提供库存查询、流水查询及手动调整库存功能
 */
@RestController
@RequestMapping("/stock")
@CrossOrigin
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 获取库存快照
     * @param warehouseId 仓库ID(可选)
     * @return 库存列表
     */
    @GetMapping
    public Result<List<StockVO>> getStockSnapshot(@RequestParam(required = false) Long warehouseId) {
        List<StockVO> stocks = stockService.getStockSnapshot(warehouseId);
        return Result.success(stocks, "获取库存快照成功");
    }

    /**
     * 获取所有库存流水
     * @return 库存流水列表
     */
    @GetMapping("/flow")
    public Result<List<StockFlowVO>> getAllStockFlows() {
        List<StockFlowVO> flows = stockService.getAllStockFlows();
        return Result.success(flows, "获取所有库存流水成功");
    }

    /**
     * 获取指定商品库存流水
     * @param itemId 商品ID
     * @return 库存流水列表
     */
    @GetMapping("/flow/{itemId}")
    public Result<List<StockFlowVO>> getStockFlows(@PathVariable Long itemId) {
        List<StockFlowVO> flows = stockService.getStockFlows(itemId);
        return Result.success(flows, "获取库存流水成功");
    }

    /**
     * 手动调整库存
     * @param adjustDTO 调整信息
     * @return 成功信息
     */
    @PostMapping("/adjust")
    @Log(module = "库存管理", action = "调整库存", description = "手动调整库存")
    public Result<Void> adjustStock(@RequestBody com.example.backend.dto.StockAdjustDTO adjustDTO) {
        if (adjustDTO.getChangeAmount() == null || adjustDTO.getChangeAmount() == 0) {
            return Result.error("调整数量不能为0");
        }
        // Default warehouseId to 1 for now
        Long warehouseId = 1L;
        stockService.adjustStock(adjustDTO.getItemId(), adjustDTO.getChangeAmount(), adjustDTO.getChangeType(), warehouseId, "库存调整", null);
        return Result.success(null, "库存调整成功");
    }

    /**
     * 删除库存记录
     * @param itemId 商品ID
     * @return 成功信息
     */
    @DeleteMapping("/{itemId}")
    @Log(module = "库存管理", action = "删除库存", description = "删除库存记录")
    public Result<Void> deleteStock(@PathVariable Long itemId) {
        stockService.deleteStock(itemId);
        return Result.success(null, "库存记录删除成功");
    }
}

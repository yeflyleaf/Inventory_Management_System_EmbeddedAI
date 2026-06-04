package com.example.backend.service;

import com.example.backend.vo.StockVO;
import com.example.backend.vo.StockFlowVO;
import java.util.List;

/**
 * 库存服务接口
 */
public interface StockService {
    /**
     * 获取库存快照
     * @param warehouseId 仓库ID (可选)
     * @return 库存VO列表
     */
    List<StockVO> getStockSnapshot(Long warehouseId);

    /**
     * 获取指定商品的库存流水
     * @param itemId 商品ID
     * @return 库存流水VO列表
     */
    List<StockFlowVO> getStockFlows(Long itemId);

    /**
     * 获取所有库存流水
     * @return 库存流水VO列表
     */
    List<StockFlowVO> getAllStockFlows();

    /**
     * 调整库存
     * @param itemId 商品ID
     * @param changeAmount 变动数量 (正数增加，负数减少)
     * @param changeType 变动类型
     * @param warehouseId 仓库ID
     * @param refType 关联类型
     * @param refId 关联ID
     */
    void adjustStock(Long itemId, Integer changeAmount, String changeType, Long warehouseId, String refType, String refId);
    
    /**
     * 删除指定商品的库存记录
     * @param itemId 商品ID
     */
    void deleteStock(Long itemId);
}

package com.example.backend.dao;

import com.example.backend.entity.StockFlow;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 库存流水数据访问接口
 */
@Mapper
public interface StockFlowMapper {
    /**
     * 插入库存流水
     */
    int insert(StockFlow stockFlow);

    /**
     * 查询所有库存流水
     */
    List<StockFlow> selectAll();

    /**
     * 根据商品ID查询库存流水
     */
    List<StockFlow> selectByItemId(Long itemId);

    /**
     * 统计指定仓库中指定商品的当前库存
     */
    Integer sumStockByItemIdAndWarehouseId(Long itemId, Long warehouseId);

    /**
     * 查询最近的库存流水
     */
    List<StockFlow> selectRecent(int limit);
    
    /**
     * 根据商品ID删除库存流水
     */
    int deleteByItemId(Long itemId);
    
    /**
     * 获取有库存流水记录的所有商品ID（去重）
     */
    List<Long> selectDistinctItemIds();

    /**
     * 获取过去N天的每日统计数据
     */
    List<com.example.backend.vo.StockFlowDailyStatsVO> selectDailyStats(int days, Long warehouseId, String category, String itemName);

    /**
     * 获取最近N天有变动的商品ID
     */
    List<Long> selectDistinctItemIdsRecent(int days);

    /**
     * 获取所有商品的最后变动时间
     */
    @org.apache.ibatis.annotations.MapKey("itemId")
    java.util.Map<Long, java.util.Map<String, Object>> selectLastMovements();
}

package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dao.ProductMapper;
import com.example.backend.dao.StockFlowMapper;
import com.example.backend.dao.WarehouseMapper;
import com.example.backend.entity.Product;
import com.example.backend.entity.StockFlow;
import com.example.backend.entity.Warehouse;
import com.example.backend.service.StockService;
import com.example.backend.service.SystemSettingService;
import com.example.backend.vo.StockFlowVO;
import com.example.backend.vo.StockVO;

/**
 * 库存服务实现类
 */
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockFlowMapper stockFlowMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * 获取库存快照
     * 计算指定仓库或所有仓库的当前库存状态
     * 包含：当前库存数量、最后变动时间、仓库名称等信息
     * 结果会被缓存，key为 'warehouse:' + warehouseId
     *
     * @param warehouseId 仓库ID (可选，为null则统计所有仓库)
     * @return 库存快照VO列表
     */
    @Override
    @Cacheable(value = "stock_snapshot", key = "'warehouse:' + (#warehouseId == null ? 'all' : #warehouseId)")
    public List<StockVO> getStockSnapshot(Long warehouseId) {
        // 获取所有商品，确保与管理员后台统计逻辑一致
        List<Product> allProducts = productMapper.selectAll();
        
        // Batch fetch last movement dates
        java.util.Map<Long, java.util.Map<String, Object>> lastMovements = stockFlowMapper.selectLastMovements();
        
        List<StockVO> stockVOs = new ArrayList<>();

        for (Product product : allProducts) {
            Integer currentStock = stockFlowMapper.sumStockByItemIdAndWarehouseId(product.getId(), warehouseId);
            if (currentStock == null)
                currentStock = 0;

            StockVO vo = new StockVO();
            vo.setItemId(product.getId());
            vo.setItemName(product.getName());
            vo.setItemSku(product.getSku());
            vo.setCategory(product.getCategory());
            vo.setWarehouseId(warehouseId);
            vo.setCurrentStock(currentStock);
            
            // Set last movement date
            if (lastMovements != null && lastMovements.containsKey(product.getId())) {
                java.util.Map<String, Object> movement = lastMovements.get(product.getId());
                Object dateObj = movement.get("lastDate");
                if (dateObj instanceof java.sql.Timestamp) {
                    vo.setLastMovementDate(((java.sql.Timestamp) dateObj).toLocalDateTime());
                } else if (dateObj instanceof java.time.LocalDateTime) {
                    vo.setLastMovementDate((java.time.LocalDateTime) dateObj);
                }
            }

            if (warehouseId != null) {
                Warehouse warehouse = warehouseMapper.selectById(warehouseId);
                if (warehouse != null) {
                    vo.setWarehouseName(warehouse.getName());
                }
            }

            stockVOs.add(vo);
        }
        return stockVOs;
    }

    /**
     * 获取指定商品的库存流水记录
     * 包含每一次库存变动的详细信息
     *
     * @param itemId 商品ID
     * @return 库存流水VO列表
     */
    @Override
    public List<StockFlowVO> getStockFlows(Long itemId) {
        List<StockFlow> flows = stockFlowMapper.selectByItemId(itemId);
        return flows.stream().map(this::convertToFlowVO).collect(Collectors.toList());
    }

    /**
     * 获取所有库存流水记录
     *
     * @return 所有库存流水VO列表
     */
    @Override
    public List<StockFlowVO> getAllStockFlows() {
        List<StockFlow> flows = stockFlowMapper.selectAll();
        return flows.stream().map(this::convertToFlowVO).collect(Collectors.toList());
    }

    /**
     * 调整库存
     * 记录一条库存流水，并更新库存状态
     * 根据系统设置 allow_negative_stock 决定是否允许负库存
     * 操作完成后清空 'stock_snapshot' 缓存
     *
     * @param itemId       商品ID
     * @param changeAmount 变动数量 (正数增加，负数减少)
     * @param changeType   变动类型 (如：采购入库、销售出库、盘点调整)
     * @param warehouseId  仓库ID
     * @param refType      关联单据类型
     * @param refId        关联单据ID/编号
     * @throws RuntimeException 如果库存不足且不允许负库存
     */
    @Override
    @Transactional
    @CacheEvict(value = "stock_snapshot", allEntries = true)
    public void adjustStock(Long itemId, Integer changeAmount, String changeType, Long warehouseId, String refType,
            String refId) {
        // 读取系统设置：是否允许负库存
        boolean allowNegativeStock = systemSettingService.getBooleanValue("allow_negative_stock", false);
        
        // 出库操作时校验库存是否充足
        if (!allowNegativeStock && changeAmount < 0) {
            Integer currentStock = stockFlowMapper.sumStockByItemIdAndWarehouseId(itemId, warehouseId);
            if (currentStock == null)
                currentStock = 0;
            if (currentStock + changeAmount < 0) {
                throw new RuntimeException("库存不足，无法执行出库操作。当前库存: " + currentStock + ", 变动: " + changeAmount);
            }
        }

        StockFlow flow = new StockFlow();
        flow.setItemId(itemId);
        flow.setChangeAmount(changeAmount);
        flow.setChangeType(changeType);
        flow.setRefType(refType);
        flow.setRefId(refId);
        flow.setWarehouseId(warehouseId);
        flow.setCreatedAt(LocalDateTime.now());
        stockFlowMapper.insert(flow);
    }

    /**
     * 删除指定商品的所有库存记录
     * 通常在删除商品时调用
     * 操作完成后清空 'stock_snapshot' 缓存
     *
     * @param itemId 商品ID
     */
    @Override
    @Transactional
    @CacheEvict(value = "stock_snapshot", allEntries = true)
    public void deleteStock(Long itemId) {
        // 删除该商品的所有库存流水记录
        stockFlowMapper.deleteByItemId(itemId);
    }

    /**
     * 将 StockFlow 实体转换为 StockFlowVO
     * 关联查询商品名和仓库名
     *
     * @param flow StockFlow实体
     * @return StockFlowVO视图对象
     */
    private StockFlowVO convertToFlowVO(StockFlow flow) {
        StockFlowVO vo = new StockFlowVO();
        vo.setId(flow.getId());
        vo.setItemId(flow.getItemId());
        vo.setChangeAmount(flow.getChangeAmount());
        vo.setChangeType(flow.getChangeType());
        vo.setRefType(flow.getRefType());
        vo.setRefId(flow.getRefId());
        vo.setWarehouseId(flow.getWarehouseId());
        vo.setCreatedAt(flow.getCreatedAt());

        Product product = productMapper.selectById(flow.getItemId());
        if (product != null) {
            vo.setItemName(product.getName());
        }

        Warehouse warehouse = warehouseMapper.selectById(flow.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getName());
        }

        return vo;
    }
}

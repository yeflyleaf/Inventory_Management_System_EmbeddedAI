package com.example.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dao.ProductMapper;
import com.example.backend.dao.PurchaseOrderItemMapper;
import com.example.backend.dao.PurchaseOrderMapper;
import com.example.backend.dao.SupplierMapper;
import com.example.backend.dto.PurchaseOrderDTO;
import com.example.backend.dto.PurchaseOrderItemDTO;
import com.example.backend.entity.Product;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderItem;
import com.example.backend.entity.Supplier;
import com.example.backend.service.PurchaseOrderService;
import com.example.backend.service.StockService;
import com.example.backend.vo.PurchaseOrderItemVO;
import com.example.backend.vo.PurchaseOrderVO;

/**
 * 采购订单服务实现类
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private com.example.backend.service.SystemSettingService systemSettingService;

    /**
     * 创建采购订单
     * 1. 生成订单号 (前缀 + 时间戳)
     * 2. 计算订单总金额
     * 3. 保存订单主表信息
     * 4. 保存订单明细项
     * 5. 清空 'purchase_orders' 缓存
     *
     * @param orderDTO 采购订单数据传输对象
     * @param userId   创建人ID
     * @throws RuntimeException 如果商品数量不合法
     */
    @Override
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void createOrder(PurchaseOrderDTO orderDTO, Long userId) {
        PurchaseOrder order = new PurchaseOrder();
        String prefix = systemSettingService.getValue("order_prefix_purchase", "PO");
        order.setOrderNo(prefix + System.currentTimeMillis()); // Simple ID generation
        order.setSupplierId(orderDTO.getSupplierId());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy(userId);

        // Calculate total amount
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO itemDTO : orderDTO.getItems()) {
            if (itemDTO.getQty() == null || itemDTO.getQty() <= 0) {
                throw new RuntimeException("商品数量必须为正整数");
            }
            BigDecimal subtotal = itemDTO.getCostPrice().multiply(BigDecimal.valueOf(itemDTO.getQty()));
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);

        purchaseOrderMapper.insert(order);

        for (PurchaseOrderItemDTO itemDTO : orderDTO.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(order.getId());
            item.setItemId(itemDTO.getItemId());
            item.setQty(itemDTO.getQty());
            item.setCostPrice(itemDTO.getCostPrice());
            item.setSubtotal(itemDTO.getCostPrice().multiply(BigDecimal.valueOf(itemDTO.getQty())));
            purchaseOrderItemMapper.insert(item);
        }
    }

    /**
     * 完成采购订单 (入库操作)
     * 1. 检查订单状态是否为 'CREATED'
     * 2. 更新订单状态为 'FINISHED'
     * 3. 遍历订单明细，调用库存服务增加库存 (采购入库)
     * 4. 清空 'purchase_orders' 缓存
     *
     * @param orderId 采购订单ID
     * @throws RuntimeException 如果订单不存在或状态不正确
     */
    @Override
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void finishOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order not found or already finished");
        }

        order.setStatus("FINISHED");
        purchaseOrderMapper.update(order);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectByOrderId(orderId);
        for (PurchaseOrderItem item : items) {
            // Inbound: Positive quantity
            stockService.adjustStock(item.getItemId(), item.getQty(), "采购入库", 1L, "采购订单", order.getOrderNo());
        }
    }

    /**
     * 获取所有采购订单列表
     * 优先从缓存 'purchase_orders' 中获取，key为 'all'
     *
     * @return 采购订单VO列表
     */
    @Override
    @Cacheable(value = "purchase_orders", key = "'all'")
    public List<PurchaseOrderVO> getAllOrders() {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectAll();
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据ID获取采购订单详情
     * 优先从缓存 'purchase_orders' 中获取，key为 'order:' + id
     *
     * @param id 采购订单ID
     * @return 采购订单VO对象
     */
    @Override
    @Cacheable(value = "purchase_orders", key = "'order:' + #id")
    public PurchaseOrderVO getOrderById(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        return convertToVO(order);
    }

    /**
     * 将 PurchaseOrder 实体转换为 PurchaseOrderVO 对象
     * 同时会查询并填充供应商名称和订单明细项
     *
     * @param order PurchaseOrder实体对象
     * @return PurchaseOrderVO对象
     */
    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        if (order == null)
            return null;
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCreatedBy(order.getCreatedBy());

        Supplier supplier = supplierMapper.selectById(order.getSupplierId());
        if (supplier != null) {
            vo.setSupplierName(supplier.getName());
        }

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectByOrderId(order.getId());
        vo.setItems(items.stream().map(item -> {
            PurchaseOrderItemVO itemVO = new PurchaseOrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setItemId(item.getItemId());
            // 查询商品信息并设置名称和SKU
            Product product = productMapper.selectById(item.getItemId());
            if (product != null) {
                itemVO.setItemName(product.getName());
                itemVO.setItemSku(product.getSku());
            }
            itemVO.setQty(item.getQty());
            itemVO.setCostPrice(item.getCostPrice());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 删除所有采购订单
     * 1. 删除所有订单明细
     * 2. 删除所有订单主表记录
     * 3. 清空 'purchase_orders' 缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void deleteAllOrders() {
        purchaseOrderItemMapper.deleteAll();
        purchaseOrderMapper.deleteAll();
    }

    /**
     * 删除指定采购订单
     * 1. 删除该订单的所有明细
     * 2. 删除该订单主表记录
     * 3. 清空 'purchase_orders' 缓存
     *
     * @param id 采购订单ID
     */
    @Override
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void deleteOrder(Long id) {
        purchaseOrderItemMapper.deleteByOrderId(id);
        purchaseOrderMapper.deleteById(id);
    }
}

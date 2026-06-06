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

import com.example.backend.dao.CustomerMapper;
import com.example.backend.dao.ProductMapper;
import com.example.backend.dao.SaleOrderItemMapper;
import com.example.backend.dao.SaleOrderMapper;
import com.example.backend.dto.SaleOrderDTO;
import com.example.backend.dto.SaleOrderItemDTO;
import com.example.backend.entity.Customer;
import com.example.backend.entity.Product;
import com.example.backend.entity.SaleOrder;
import com.example.backend.entity.SaleOrderItem;
import com.example.backend.service.SaleOrderService;
import com.example.backend.service.StockService;
import com.example.backend.vo.SaleOrderItemVO;
import com.example.backend.vo.SaleOrderVO;

/**
 * 销售订单服务实现类
 */
@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Autowired
    private SaleOrderMapper saleOrderMapper;

    @Autowired
    private SaleOrderItemMapper saleOrderItemMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private com.example.backend.service.SystemSettingService systemSettingService;

    /**
     * 创建销售订单
     * 1. 生成订单号 (前缀 + 时间戳)
     * 2. 计算订单总金额
     * 3. 保存订单主表信息
     * 4. 保存订单明细项
     * 5. 清空 'sale_orders' 缓存
     *
     * @param orderDTO 销售订单数据传输对象
     * @param userId   创建人ID
     * @throws RuntimeException 如果商品数量不合法
     */
    @Override
    @Transactional
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void createOrder(SaleOrderDTO orderDTO, Long userId) {
        SaleOrder order = new SaleOrder();
        String prefix = systemSettingService.getValue("order_prefix_sales", "SO");
        order.setOrderNo(prefix + System.currentTimeMillis());
        order.setCustomerId(orderDTO.getCustomerId());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy(userId);

        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderItemDTO itemDTO : orderDTO.getItems()) {
            if (itemDTO.getQty() == null || itemDTO.getQty() <= 0) {
                throw new RuntimeException("商品数量必须为正整数");
            }
            BigDecimal subtotal = itemDTO.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQty()));
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);

        saleOrderMapper.insert(order);

        for (SaleOrderItemDTO itemDTO : orderDTO.getItems()) {
            SaleOrderItem item = new SaleOrderItem();
            item.setSalesOrderId(order.getId());
            item.setItemId(itemDTO.getItemId());
            item.setQty(itemDTO.getQty());
            item.setSalePrice(itemDTO.getSalePrice());
            item.setSubtotal(itemDTO.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQty())));
            saleOrderItemMapper.insert(item);
        }
    }

    /**
     * 销售订单发货 (出库操作)
     * 1. 检查订单状态是否为 'CREATED'
     * 2. 更新订单状态为 'SHIPPED'
     * 3. 遍历订单明细，调用库存服务减少库存 (销售出库)
     * 4. 清空 'sale_orders' 缓存
     *
     * @param orderId 销售订单ID
     * @throws RuntimeException 如果订单不存在或已发货
     */
    @Override
    @Transactional
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void shipOrder(Long orderId) {
        SaleOrder order = saleOrderMapper.selectById(orderId);
        if (order == null || !"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order not found or already shipped");
        }

        order.setStatus("SHIPPED");
        saleOrderMapper.update(order);

        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(orderId);
        for (SaleOrderItem item : items) {
            // Outbound: Negative quantity
            stockService.adjustStock(item.getItemId(), -item.getQty(), "销售出库", 1L, "销售订单", order.getOrderNo());
        }
    }

    /**
     * 获取所有销售订单列表
     * 优先从缓存 'sale_orders' 中获取，key为 'all'
     *
     * @return 销售订单VO列表
     */
    @Override
    @Cacheable(value = "sale_orders", key = "'all'")
    public List<SaleOrderVO> getAllOrders() {
        List<SaleOrder> orders = saleOrderMapper.selectAll();
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据ID获取销售订单详情
     * 优先从缓存 'sale_orders' 中获取，key为 'order:' + id
     *
     * @param id 销售订单ID
     * @return 销售订单VO对象
     */
    @Override
    @Cacheable(value = "sale_orders", key = "'order:' + #id")
    public SaleOrderVO getOrderById(Long id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        return convertToVO(order);
    }

    /**
     * 将 SaleOrder 实体转换为 SaleOrderVO 对象
     * 同时会查询并填充客户名称和订单明细项
     *
     * @param order SaleOrder实体对象
     * @return SaleOrderVO对象
     */
    private SaleOrderVO convertToVO(SaleOrder order) {
        if (order == null)
            return null;
        SaleOrderVO vo = new SaleOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCreatedBy(order.getCreatedBy());

        Customer customer = customerMapper.selectById(order.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getName());
        }

        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(order.getId());
        vo.setItems(items.stream().map(item -> {
            SaleOrderItemVO itemVO = new SaleOrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setItemId(item.getItemId());
            // 查询商品信息并设置名称和SKU
            Product product = productMapper.selectById(item.getItemId());
            if (product != null) {
                itemVO.setItemName(product.getName());
                itemVO.setItemSku(product.getSku());
            }
            itemVO.setQty(item.getQty());
            itemVO.setSalePrice(item.getSalePrice());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 删除所有销售订单
     * 1. 删除所有订单明细
     * 2. 删除所有订单主表记录
     * 3. 清空 'sale_orders' 缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void deleteAllOrders() {
        saleOrderItemMapper.deleteAll();
        saleOrderMapper.deleteAll();
    }

    /**
     * 删除指定销售订单
     * 1. 删除该订单的所有明细
     * 2. 删除该订单主表记录
     * 3. 清空 'sale_orders' 缓存
     *
     * @param id 销售订单ID
     */
    @Override
    @Transactional
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void deleteOrder(Long id) {
        saleOrderItemMapper.deleteByOrderId(id);
        saleOrderMapper.deleteById(id);
    }
}

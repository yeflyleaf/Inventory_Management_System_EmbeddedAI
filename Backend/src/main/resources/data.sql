-- ============================================================
-- 数据库数据初始化脚本
-- ============================================================

-- 1. 初始化系统设置
-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO system_setting (setting_key, setting_value, setting_type, description) VALUES
('company_name', '库存管理系统', 'string', '公司名称'),
('company_address', '', 'string', '公司地址'),
('company_phone', '', 'string', '公司电话'),
('company_email', '', 'string', '公司邮箱'),
('low_stock_threshold', '10', 'number', '低库存预警阈值'),
('order_prefix_purchase', 'PO', 'string', '采购单编号前缀'),
('order_prefix_sales', 'SO', 'string', '销售单编号前缀'),
('allow_negative_stock', 'false', 'boolean', '是否允许负库存'),
('inventory_backlog_days', '60', 'number', '库存积压预警天数'),
('ai_api_key', '', 'string', 'AI API密钥'),
('ai_base_url', 'https://api.openai.com/v1', 'string', 'AI API基础路径'),
('ai_model_name', 'gpt-5', 'string', 'AI模型名称'),
('ai_max_rpm', '500', 'number', 'AI最高RPM限制'),
('ai_max_tpm', '2000000', 'number', 'AI最高TPM限制(输入)'),
('ai_max_rpd', '10000', 'number', 'AI最高RPD限制');

-- 删除可能已存在的 ai_temperature 设置项
DELETE FROM system_setting WHERE setting_key = 'ai_temperature';

-- 2. 添加默认系统账号
-- 密码为 admin123abc 的加密形式 (BCrypt)
-- 如果对应账号不存在则插入
INSERT INTO user (username, password, nickname, role, status, created_at) 
SELECT 'admin', 'admin123abc', '系统管理员', 'ADMIN', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

INSERT INTO user (username, password, nickname, role, status, created_at) 
SELECT 'manager', 'admin123abc', '仓储经理', 'MANAGER', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'manager');

INSERT INTO user (username, password, nickname, role, status, created_at) 
SELECT 'clerk', 'admin123abc', '库存专员', 'EMPLOYEE', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'clerk');

-- 3. 初始化仓库数据
INSERT INTO warehouse (id, name, address, created_at)
SELECT 1, '北京总仓', '北京市朝阳区大屯路10号', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM warehouse WHERE id = 1);
INSERT INTO warehouse (id, name, address, created_at)
SELECT 2, '广州分仓', '广州市天河区科韵路20号', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM warehouse WHERE id = 2);
INSERT INTO warehouse (id, name, address, created_at)
SELECT 3, '上海分仓', '上海市浦东新区张江高科30号', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM warehouse WHERE id = 3);

-- 4. 初始化供应商数据
INSERT INTO supplier (id, name, contact_person, phone, phone2, address, created_at)
SELECT 1, '科创电子配件有限公司', '张经理', '13800138000', '010-62345678', '深圳市南山区科技园', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE id = 1);
INSERT INTO supplier (id, name, contact_person, phone, phone2, address, created_at)
SELECT 2, '优办公品供应商', '李女士', '13900139000', '020-81234567', '广州市越秀区北京路', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE id = 2);
INSERT INTO supplier (id, name, contact_person, phone, phone2, address, created_at)
SELECT 3, '智能五金制造厂', '王厂长', '13700137000', '021-51234567', '上海市闵行区工业园', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE id = 3);

-- 5. 初始化客户数据
INSERT INTO customer (id, name, contact_person, phone, phone2, address, created_at)
SELECT 1, '地平线科技有限公司', '赵总', '15000150000', '010-88888888', '北京市海淀区中关村', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 1);
INSERT INTO customer (id, name, contact_person, phone, phone2, address, created_at)
SELECT 2, '朝阳百货商贸公司', '钱经理', '15100151000', '0755-22222222', '深圳市罗湖区东门', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 2);
INSERT INTO customer (id, name, contact_person, phone, phone2, address, created_at)
SELECT 3, '个体工商户李明', '李明', '15200152000', '', '南京市玄武区中山路', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 3);

-- 6. 初始化商品数据
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 1, 'PROD-001', 'iPhone 15 Pro', '电子产品', '台', '6901234567890', 7999.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 1);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 2, 'PROD-002', 'MacBook Pro 16', '电子产品', '台', '6901234567891', 19999.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 2);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 3, 'PROD-003', '人体工学电脑椅', '办公家具', '把', '6901234567892', 899.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 3);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 4, 'PROD-004', '升降办公桌', '办公家具', '张', '6901234567893', 1599.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 4);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 5, 'PROD-005', '得力签字笔 12支/盒', '办公用品', '盒', '6901234567894', 15.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 5);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 6, 'PROD-006', 'A4复印纸 500张/包', '办公用品', '包', '6901234567895', 25.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 6);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 7, 'PROD-007', '智能加湿器', '智能生活', '台', '6901234567896', 199.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 7);
INSERT INTO item (id, sku, name, category, unit, barcode, sale_price, image_url, created_at)
SELECT 8, 'PROD-008', '蓝牙降噪耳机', '智能生活', '个', '6901234567897', 999.00, '', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM item WHERE id = 8);

-- 7. 初始化采购订单
INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, created_at, created_by)
SELECT 1, 'PO20260601001', 1, 159985.00, 'FINISHED', '2026-06-01 10:00:00', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order WHERE id = 1);
INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, created_at, created_by)
SELECT 2, 'PO20260602001', 2, 1400.00, 'FINISHED', '2026-06-02 14:00:00', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order WHERE id = 2);
INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, created_at, created_by)
SELECT 3, 'PO20260603001', 3, 15490.00, 'CREATED', '2026-06-03 16:30:00', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order WHERE id = 3);

-- 8. 初始化采购订单明细
INSERT INTO purchase_order_item (id, purchase_order_id, item_id, qty, cost_price, subtotal)
SELECT 1, 1, 1, 10, 6999.00, 69990.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order_item WHERE id = 1);
INSERT INTO purchase_order_item (id, purchase_order_id, item_id, qty, cost_price, subtotal)
SELECT 2, 1, 2, 5, 17999.00, 89995.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order_item WHERE id = 2);
INSERT INTO purchase_order_item (id, purchase_order_id, item_id, qty, cost_price, subtotal)
SELECT 3, 2, 5, 40, 10.00, 400.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order_item WHERE id = 3);
INSERT INTO purchase_order_item (id, purchase_order_id, item_id, qty, cost_price, subtotal)
SELECT 4, 2, 6, 50, 20.00, 1000.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order_item WHERE id = 4);
INSERT INTO purchase_order_item (id, purchase_order_id, item_id, qty, cost_price, subtotal)
SELECT 5, 3, 7, 50, 150.00, 7500.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order_item WHERE id = 5);
INSERT INTO purchase_order_item (id, purchase_order_id, item_id, qty, cost_price, subtotal)
SELECT 6, 3, 8, 10, 799.00, 7990.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM purchase_order_item WHERE id = 6);

-- 9. 初始化销售订单
INSERT INTO sales_order (id, order_no, customer_id, total_amount, status, created_at, created_by)
SELECT 1, 'SO20260604001', 1, 39995.00, 'SHIPPED', '2026-06-04 09:00:00', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sales_order WHERE id = 1);
INSERT INTO sales_order (id, order_no, customer_id, total_amount, status, created_at, created_by)
SELECT 2, 'SO20260605001', 2, 300.00, 'CREATED', '2026-06-05 15:00:00', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sales_order WHERE id = 2);

-- 10. 初始化销售订单明细
INSERT INTO sales_order_item (id, sales_order_id, item_id, qty, sale_price, subtotal)
SELECT 1, 1, 1, 5, 7999.00, 39995.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sales_order_item WHERE id = 1);
INSERT INTO sales_order_item (id, sales_order_id, item_id, qty, sale_price, subtotal)
SELECT 2, 2, 5, 10, 15.00, 150.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sales_order_item WHERE id = 2);
INSERT INTO sales_order_item (id, sales_order_id, item_id, qty, sale_price, subtotal)
SELECT 3, 2, 6, 6, 25.00, 150.00 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sales_order_item WHERE id = 3);

-- 11. 初始化库存流水
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 1, 1, 10, '采购入库', '采购订单', 'PO20260601001', 1, '2026-06-01 10:10:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 1);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 2, 1, -5, '销售出库', '销售订单', 'SO20260604001', 1, '2026-06-04 09:15:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 2);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 3, 1, 20, '初始化入库', '盘点单', 'SYS001', 2, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 3);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 4, 2, 5, '采购入库', '采购订单', 'PO20260601001', 1, '2026-06-01 10:10:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 4);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 5, 2, 10, '初始化入库', '盘点单', 'SYS002', 3, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 5);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 6, 3, 30, '初始化入库', '盘点单', 'SYS003', 1, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 6);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 7, 3, 15, '初始化入库', '盘点单', 'SYS004', 2, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 7);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 8, 4, 20, '初始化入库', '盘点单', 'SYS005', 1, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 8);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 9, 5, 40, '采购入库', '采购订单', 'PO20260602001', 2, '2026-06-02 14:15:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 9);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 10, 5, 200, '初始化入库', '盘点单', 'SYS006', 2, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 10);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 11, 6, 50, '采购入库', '采购订单', 'PO20260602001', 2, '2026-06-02 14:15:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 11);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 12, 6, 100, '初始化入库', '盘点单', 'SYS007', 2, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 12);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 13, 7, 4, '初始化入库', '盘点单', 'SYS008', 3, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 13);
INSERT INTO stock_flow (id, item_id, change_amount, change_type, ref_type, ref_id, warehouse_id, created_at)
SELECT 14, 8, 3, '初始化入库', '盘点单', 'SYS009', 3, '2026-06-01 00:00:00' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM stock_flow WHERE id = 14);


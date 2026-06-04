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
('ai_api_key', '', 'string', 'AI API密钥');

-- 2. 添加默认管理员账号
-- 密码为 admin123abc 的加密形式 (BCrypt)
-- 如果用户表为空或没有admin用户，则插入
INSERT INTO user (username, password, nickname, role, status, created_at) 
SELECT 'admin', 'admin123abc', '系统管理员', 'ADMIN', 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

-- ============================================================
-- 数据库架构初始化脚本 (替代 Flyway)
-- ============================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(50) COMMENT '姓名/昵称',
    role VARCHAR(20) COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 2. 商品表 (item)
CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sku VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) COMMENT '商品分类',
    unit VARCHAR(20) COMMENT '计量单位',
    barcode VARCHAR(50) COMMENT '条码',
    sale_price DECIMAL(10, 2) COMMENT '销售价',
    image_url VARCHAR(2000) COMMENT '商品图片URL列表（逗号分隔，最多5张）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 3. 客户表
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '联系电话',
    phone2 VARCHAR(20) COMMENT '备用电话',
    address VARCHAR(255) COMMENT '联系地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 4. 供应商表
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '联系电话',
    phone2 VARCHAR(20) COMMENT '备用电话',
    address VARCHAR(255) COMMENT '联系地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 5. 仓库表
CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    address VARCHAR(255) COMMENT '仓库地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- 6. 采购订单
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '采购单编号',
    supplier_id BIGINT COMMENT '供应商ID',
    total_amount DECIMAL(12, 2) COMMENT '采购总金额',
    status VARCHAR(20) COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人'
);

-- 7. 采购明细
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    purchase_order_id BIGINT COMMENT '采购单ID',
    item_id BIGINT COMMENT '商品ID',
    qty INT COMMENT '采购数量',
    cost_price DECIMAL(10, 2) COMMENT '采购单价',
    subtotal DECIMAL(12, 2) COMMENT '小计金额'
);

-- 8. 销售订单
CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '销售单编号',
    customer_id BIGINT COMMENT '客户ID',
    total_amount DECIMAL(12, 2) COMMENT '销售总金额',
    status VARCHAR(20) COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人'
);

-- 9. 销售明细
CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sales_order_id BIGINT COMMENT '销售单ID',
    item_id BIGINT COMMENT '商品ID',
    qty INT COMMENT '销售数量',
    sale_price DECIMAL(10, 2) COMMENT '销售单价',
    subtotal DECIMAL(12, 2) COMMENT '小计金额'
);

-- 10. 库存流水表
CREATE TABLE IF NOT EXISTS stock_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    item_id BIGINT COMMENT '商品ID',
    change_amount INT COMMENT '库存变动数量',
    change_type VARCHAR(20) COMMENT '变动类型',
    ref_type VARCHAR(20) COMMENT '来源单据类型',
    ref_id VARCHAR(50) COMMENT '来源单据ID',
    warehouse_id BIGINT COMMENT '仓库ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
);

-- 11. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    module VARCHAR(50) COMMENT '操作模块',
    action VARCHAR(50) COMMENT '操作类型',
    target VARCHAR(100) COMMENT '操作对象',
    target_id VARCHAR(50) COMMENT '操作对象ID',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_status INT COMMENT '响应状态码',
    execution_time INT COMMENT '执行时间(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
);

-- 12. 系统设置表
CREATE TABLE IF NOT EXISTS system_setting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    setting_key VARCHAR(50) NOT NULL UNIQUE COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    setting_type VARCHAR(20) COMMENT '类型: string/number/boolean/json',
    description VARCHAR(200) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- ============================================================
-- 索引
-- ============================================================

-- 库存流水索引
-- 使用 DROP IF EXISTS 避免重复创建错误
-- 库存流水索引
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stock_flow' AND INDEX_NAME = 'idx_item_change');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_item_change already exists''', 'CREATE INDEX idx_item_change ON stock_flow(item_id, change_amount)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 操作日志索引
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND INDEX_NAME = 'idx_log_user_id');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_log_user_id already exists''', 'CREATE INDEX idx_log_user_id ON operation_log(user_id)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND INDEX_NAME = 'idx_log_created_at');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_log_created_at already exists''', 'CREATE INDEX idx_log_created_at ON operation_log(created_at)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_log' AND INDEX_NAME = 'idx_log_module');
SET @sqlstmt := IF(@exist > 0, 'SELECT ''Index idx_log_module already exists''', 'CREATE INDEX idx_log_module ON operation_log(module)');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 数据库结构变更维护指南
-- ============================================================
-- 当您需要修改现有表结构（例如新增字段）时，请不要直接修改上面的 CREATE TABLE 语句，
-- 因为它们对已经存在的表无效。
--
-- 您应该在文件末尾添加"幂等"的变更脚本。
-- "幂等"意味着脚本可以重复运行而不会报错（例如：先检查字段是否存在，不存在才添加）。
--
-- 示例：向 user 表安全添加 new_column 字段
-- ------------------------------------------------------------
-- SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'new_column');
-- SET @sqlstmt := IF(@exist = 0, 'ALTER TABLE user ADD COLUMN new_column VARCHAR(50) DEFAULT NULL COMMENT ''新字段''', 'SELECT ''Column already exists''');
-- PREPARE stmt FROM @sqlstmt;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;
-- ------------------------------------------------------------


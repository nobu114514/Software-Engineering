-- 二级分类功能优化版数据库脚本

-- 1. 创建一级分类表（优化版）
SET @table_exists = (SELECT COUNT(*) FROM information_schema.TABLES 
                    WHERE TABLE_SCHEMA = DATABASE() 
                    AND TABLE_NAME = 'categories');

SET @sql = IF(@table_exists = 0, 
             'CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT ''分类名称'',
    description VARCHAR(500) DEFAULT NULL COMMENT ''分类描述'',
    active BOOLEAN DEFAULT TRUE COMMENT ''是否激活'',
    sort_order INT DEFAULT 0 COMMENT ''排序权重'',
    icon VARCHAR(255) DEFAULT NULL COMMENT ''分类图标'',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间"
)', 
             'SELECT ''Table already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 如果icon字段不存在则添加
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'categories' 
                     AND COLUMN_NAME = 'icon');

SET @sql = IF(@column_exists = 0, 
             'ALTER TABLE categories ADD COLUMN icon VARCHAR(255) DEFAULT NULL COMMENT ''分类图标''', 
             'SELECT ''Column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 创建二级分类表（优化版）
SET @table_exists = (SELECT COUNT(*) FROM information_schema.TABLES 
                    WHERE TABLE_SCHEMA = DATABASE() 
                    AND TABLE_NAME = 'sub_categories');

SET @sql = IF(@table_exists = 0, 
             'CREATE TABLE sub_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT ''子分类名称'',
    description VARCHAR(500) DEFAULT NULL COMMENT ''子分类描述'',
    active BOOLEAN DEFAULT TRUE COMMENT ''是否激活'',
    sort_order INT DEFAULT 0 COMMENT ''排序权重'',
    category_id BIGINT NOT NULL COMMENT ''所属一级分类ID'',
    icon VARCHAR(255) DEFAULT NULL COMMENT ''子分类图标'',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY uk_sub_category_name (category_id, name) COMMENT ''同一分类下子分类名称唯一"
)', 
             'SELECT ''Table already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 如果icon字段不存在则添加
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'sub_categories' 
                     AND COLUMN_NAME = 'icon');

SET @sql = IF(@column_exists = 0, 
             'ALTER TABLE sub_categories ADD COLUMN icon VARCHAR(255) DEFAULT NULL COMMENT ''子分类图标''', 
             'SELECT ''Column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 为产品表添加分类关联字段（如果不存在）
-- 检查并添加category_id字段
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'products' 
                     AND COLUMN_NAME = 'category_id');

SET @sql = IF(@column_exists = 0, 
             'ALTER TABLE products ADD COLUMN category_id BIGINT COMMENT ''所属一级分类ID''', 
             'SELECT ''Column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加sub_category_id字段
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'products' 
                     AND COLUMN_NAME = 'sub_category_id');

SET @sql = IF(@column_exists = 0, 
             'ALTER TABLE products ADD COLUMN sub_category_id BIGINT COMMENT ''所属二级分类ID''', 
             'SELECT ''Column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 检查并添加外键约束
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
                         WHERE TABLE_SCHEMA = DATABASE() 
                         AND TABLE_NAME = 'products' 
                         AND CONSTRAINT_NAME = 'fk_product_category');

SET @sql = IF(@constraint_exists = 0, 
             'ALTER TABLE products ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL', 
             'SELECT ''Constraint already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
                         WHERE TABLE_SCHEMA = DATABASE() 
                         AND TABLE_NAME = 'products' 
                         AND CONSTRAINT_NAME = 'fk_product_sub_category');

SET @sql = IF(@constraint_exists = 0, 
             'ALTER TABLE products ADD CONSTRAINT fk_product_sub_category FOREIGN KEY (sub_category_id) REFERENCES sub_categories(id) ON DELETE SET NULL', 
             'SELECT ''Constraint already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 创建优化后的索引
-- categories表索引
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'categories' 
                   AND INDEX_NAME = 'idx_categories_active_sort');

SET @sql = IF(@index_exists = 0, 
             'CREATE INDEX idx_categories_active_sort ON categories(active, sort_order)', 
             'SELECT ''Index already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- sub_categories表索引
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'sub_categories' 
                   AND INDEX_NAME = 'idx_sub_categories_category_active');

SET @sql = IF(@index_exists = 0, 
             'CREATE INDEX idx_sub_categories_category_active ON sub_categories(category_id, active, sort_order)', 
             'SELECT ''Index already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- products表索引
SET @index_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'products' 
                   AND INDEX_NAME = 'idx_products_categories');

SET @sql = IF(@index_exists = 0, 
             'CREATE INDEX idx_products_categories ON products(category_id, sub_category_id)', 
             'SELECT ''Index already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 8. 插入优化后的默认分类数据
INSERT INTO categories (name, description, active, sort_order, icon) VALUES
('数码产品', '各类数码电子设备', TRUE, 1, '/icons/digital.png'),
('服装鞋帽', '时尚服装和鞋类', TRUE, 2, '/icons/clothing.png'),
('家居用品', '居家生活必备品', TRUE, 3, '/icons/home.png'),
('食品饮料', '各类食品和饮品', TRUE, 4, '/icons/food.png'),
('美妆个护', '美容和个人护理产品', TRUE, 5, '/icons/beauty.png')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    active = VALUES(active),
    sort_order = VALUES(sort_order),
    icon = VALUES(icon);

-- 为数码产品分类插入子分类
INSERT INTO sub_categories (name, description, active, sort_order, category_id, icon) VALUES
('手机', '智能手机和平板', TRUE, 1, (SELECT id FROM categories WHERE name = '数码产品'), '/icons/phone.png'),
('电脑', '笔记本和台式电脑', TRUE, 2, (SELECT id FROM categories WHERE name = '数码产品'), '/icons/computer.png'),
('相机', '数码相机和配件', TRUE, 3, (SELECT id FROM categories WHERE name = '数码产品'), '/icons/camera.png')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    active = VALUES(active),
    sort_order = VALUES(sort_order),
    icon = VALUES(icon);

-- 为服装鞋帽分类插入子分类
INSERT INTO sub_categories (name, description, active, sort_order, category_id, icon) VALUES
('男装', '男士服装', TRUE, 1, (SELECT id FROM categories WHERE name = '服装鞋帽'), '/icons/mens.png'),
('女装', '女士服装', TRUE, 2, (SELECT id FROM categories WHERE name = '服装鞋帽'), '/icons/womens.png'),
('鞋类', '各类鞋子', TRUE, 3, (SELECT id FROM categories WHERE name = '服装鞋帽'), '/icons/shoes.png')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    active = VALUES(active),
    sort_order = VALUES(sort_order),
    icon = VALUES(icon);

-- 为家居用品分类插入子分类
INSERT INTO sub_categories (name, description, active, sort_order, category_id, icon) VALUES
('厨具', '厨房用具', TRUE, 1, (SELECT id FROM categories WHERE name = '家居用品'), '/icons/kitchen.png'),
('卧室', '卧室用品', TRUE, 2, (SELECT id FROM categories WHERE name = '家居用品'), '/icons/bedroom.png'),
('客厅', '客厅用品', TRUE, 3, (SELECT id FROM categories WHERE name = '家居用品'), '/icons/livingroom.png')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    active = VALUES(active),
    sort_order = VALUES(sort_order),
    icon = VALUES(icon);
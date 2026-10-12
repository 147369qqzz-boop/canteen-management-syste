-- 校园食堂系统 - 新功能数据库迁移脚本
-- 执行前请先备份数据库

-- 1. 创建食堂表 (canteen)
CREATE TABLE IF NOT EXISTS `canteen` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '食堂名称',
    `location` VARCHAR(200) COMMENT '位置描述',
    `floors` VARCHAR(50) COMMENT '楼层信息，如 1,2,3',
    `create_time` DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食堂';

-- 2. 扩展 business 表：增加食堂关联、楼层、营业状态、排队信息
ALTER TABLE `business`
    ADD COLUMN `canteen_id` INT DEFAULT NULL COMMENT '所属食堂ID',
    ADD COLUMN `floor` VARCHAR(20) DEFAULT NULL COMMENT '所在楼层',
    ADD COLUMN `operate_status` VARCHAR(10) DEFAULT '营业中' COMMENT '营业状态：营业中/休息中',
    ADD COLUMN `estimated_cooking_time` INT DEFAULT 10 COMMENT '预计出餐时间(分钟)',
    ADD COLUMN `queue_count` INT DEFAULT 0 COMMENT '当前排队人数';

-- 3. 扩展 goods 表：增加营养成分字段
ALTER TABLE `goods`
    ADD COLUMN `calories` DECIMAL(10,2) DEFAULT 0 COMMENT '热量(千卡)',
    ADD COLUMN `protein` DECIMAL(10,2) DEFAULT 0 COMMENT '蛋白质(g)',
    ADD COLUMN `carbs` DECIMAL(10,2) DEFAULT 0 COMMENT '碳水化合物(g)',
    ADD COLUMN `fat` DECIMAL(10,2) DEFAULT 0 COMMENT '脂肪(g)';

-- 4. 创建饮食记录表 (diet_record)
CREATE TABLE IF NOT EXISTS `diet_record` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `goods_id` INT COMMENT '菜品ID',
    `goods_name` VARCHAR(100) COMMENT '菜品名称',
    `meal_type` VARCHAR(10) NOT NULL COMMENT '餐次：早餐/午餐/晚餐/加餐',
    `calories` DECIMAL(10,2) DEFAULT 0 COMMENT '热量(千卡)',
    `protein` DECIMAL(10,2) DEFAULT 0 COMMENT '蛋白质(g)',
    `carbs` DECIMAL(10,2) DEFAULT 0 COMMENT '碳水化合物(g)',
    `fat` DECIMAL(10,2) DEFAULT 0 COMMENT '脂肪(g)',
    `record_time` DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录';

-- 5. 创建饮食目标表 (diet_goal)
CREATE TABLE IF NOT EXISTS `diet_goal` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL UNIQUE COMMENT '用户ID',
    `goal_type` VARCHAR(20) DEFAULT '保持' COMMENT '目标类型：减脂/增肌/保持',
    `daily_calories` DECIMAL(10,2) DEFAULT 2000 COMMENT '每日热量目标(千卡)',
    `daily_protein` DECIMAL(10,2) DEFAULT 60 COMMENT '每日蛋白质目标(g)',
    `daily_carbs` DECIMAL(10,2) DEFAULT 250 COMMENT '每日碳水目标(g)',
    `daily_fat` DECIMAL(10,2) DEFAULT 65 COMMENT '每日脂肪目标(g)',
    `create_time` DATETIME DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食目标';

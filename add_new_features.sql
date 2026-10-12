-- 新增功能所需数据库表
-- 在 code2026 数据库中执行

-- 1. 系统公告表
CREATE TABLE IF NOT EXISTS `notice` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT COMMENT '公告内容',
    `status` INT DEFAULT 1 COMMENT '状态：1=发布 0=草稿',
    `time` VARCHAR(50) COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 2. 意见反馈表
CREATE TABLE IF NOT EXISTS `feedback` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` INT COMMENT '反馈用户ID',
    `user_name` VARCHAR(50) COMMENT '反馈用户名',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `reply` TEXT COMMENT '管理员回复',
    `status` INT DEFAULT 0 COMMENT '状态：0=待回复 1=已回复',
    `time` VARCHAR(50) COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈表';

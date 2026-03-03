-- 为用户表添加关怀账号（虚拟账号）标识字段

ALTER TABLE `user_info`
ADD COLUMN `is_dummy` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为关怀账号 0-否 1-是'
AFTER `status`;

-- 为关怀账号添加索引
ALTER TABLE `user_info`
ADD INDEX `idx_is_dummy` (`is_dummy`);

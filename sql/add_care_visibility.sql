-- 为file_info表添加关怀用户可见性字段
-- 0-对关怀用户不可见，1-对关怀用户可见

ALTER TABLE `file_info` 
ADD COLUMN `visible_to_care` TINYINT NOT NULL DEFAULT 0 
COMMENT '对关怀用户可见性：0-不可见，1-可见' 
AFTER `belonging_home`;

-- 为字段添加索引以提高查询性能
ALTER TABLE `file_info` 
ADD INDEX `idx_visible_to_care` (`visible_to_care` ASC);

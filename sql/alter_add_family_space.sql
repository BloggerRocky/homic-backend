-- 家庭空间功能 - 数据库增量更新脚本
-- 适用于已有数据库升级，新增家庭空间文件管理功能所需字段

-- ----------------------------
-- 1. 文件信息表(file_info)添加所属家庭字段
-- ----------------------------
-- 用途：区分个人文件和家庭文件，NULL表示个人文件，非NULL值表示所属家庭ID
ALTER TABLE `file_info` 
ADD COLUMN `belonging_home` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属家庭ID：NULL表示个人文件，非NULL表示家庭文件' 
AFTER `del_flag`;

-- 为belonging_home添加索引，优化家庭文件查询性能
ALTER TABLE `file_info` 
ADD INDEX `idx_belonging_home`(`belonging_home` ASC) USING BTREE;

-- ----------------------------
-- 2. 家庭信息表(family)添加空间管理字段
-- ----------------------------
-- 用途：管理家庭空间容量，与个人空间独立计算

-- 已用空间（单位：字节），默认0
ALTER TABLE `family` 
ADD COLUMN `use_space` BIGINT NOT NULL DEFAULT 0 COMMENT '已用空间（单位Byte）' 
AFTER `creator_id`;

-- 总空间（单位：字节），默认1GB = 1073741824字节
ALTER TABLE `family` 
ADD COLUMN `total_space` BIGINT NOT NULL DEFAULT 1073741824 COMMENT '总空间（单位Byte，默认1GB）' 
AFTER `use_space`;

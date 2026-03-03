-- 为家庭成员表添加备注字段
-- 该备注仅在对应家庭内生效，用于标识成员在该家庭中的昵称或备注信息

ALTER TABLE `family_member` 
ADD COLUMN `remark` VARCHAR(50) DEFAULT NULL COMMENT '成员备注（仅在该家庭内生效）' 
AFTER `role`;

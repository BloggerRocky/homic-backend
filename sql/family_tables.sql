-- 家庭信息表
CREATE TABLE IF NOT EXISTS `family` (
    `family_id` VARCHAR(10) NOT NULL COMMENT '家庭ID',
    `family_name` VARCHAR(50) NOT NULL COMMENT '家庭名称',
    `family_desc` VARCHAR(200) DEFAULT NULL COMMENT '家庭描述',
    `family_avatar` VARCHAR(150) DEFAULT NULL COMMENT '家庭头像',
    `family_code` VARCHAR(10) NOT NULL COMMENT '家庭码',
    `creator_id` VARCHAR(15) NOT NULL COMMENT '创建者ID',
    `use_space` BIGINT NOT NULL DEFAULT 0 COMMENT '已用空间（单位Byte）',
    `total_space` BIGINT NOT NULL DEFAULT 1073741824 COMMENT '总空间（单位Byte，默认1GB）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`family_id`),
    UNIQUE KEY `uk_family_code` (`family_code`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭信息表';

-- 家庭成员表
CREATE TABLE IF NOT EXISTS `family_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `family_id` VARCHAR(10) NOT NULL COMMENT '家庭ID',
    `user_id` VARCHAR(15) NOT NULL COMMENT '用户ID',
    `role` TINYINT NOT NULL DEFAULT 2 COMMENT '角色 0-创建者 1-管理员 2-成员',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '成员备注（仅在该家庭内生效）',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_family_user` (`family_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 家庭邀请表
CREATE TABLE IF NOT EXISTS `family_invite` (
    `invite_id` VARCHAR(15) NOT NULL COMMENT '邀请ID',
    `family_id` VARCHAR(10) NOT NULL COMMENT '家庭ID',
    `from_user_id` VARCHAR(15) NOT NULL COMMENT '邀请人ID',
    `to_user_id` VARCHAR(15) NOT NULL COMMENT '被邀请人ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待处理 1-已接受 2-已拒绝',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`invite_id`),
    KEY `idx_to_user_status` (`to_user_id`, `status`),
    KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭邀请表';

-- 家庭申请表
CREATE TABLE IF NOT EXISTS `family_apply` (
    `apply_id` VARCHAR(15) NOT NULL COMMENT '申请ID',
    `family_id` VARCHAR(10) NOT NULL COMMENT '家庭ID',
    `user_id` VARCHAR(15) NOT NULL COMMENT '申请人ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待处理 1-已同意 2-已拒绝',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`apply_id`),
    KEY `idx_family_status` (`family_id`, `status`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭申请表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(15) NOT NULL COMMENT '用户ID',
    `permission_key` VARCHAR(50) NOT NULL COMMENT '权限键，如 FAMILY-UPLOAD, FAMILY-MODIFY, FAMILY-DELETE',
    `permission_value` TINYINT NOT NULL DEFAULT 0 COMMENT '权限值，0-无权限，1-有权限',
    `object_id` VARCHAR(10) NOT NULL COMMENT '对象ID，如家庭ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission_object` (`user_id`, `permission_key`, `object_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

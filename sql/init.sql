SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户信息表
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户邮箱地址',
  `qq_open_id` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'QQ号',
  `user_avatar` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'default_avatar.jpg' COMMENT '头像',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始密码的md5值（长度固定32位）',
  `join_time` datetime NULL DEFAULT NULL COMMENT '账号注册的日期',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '用户上一次登录的时间',
  `status` int NOT NULL DEFAULT 0 COMMENT '0代表用户封禁 1代表用户正常',
  `is_dummy` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为关怀账号 0-否 1-是',
  `use_space` bigint NOT NULL COMMENT '已使用空间（单位Byte）',
  `total_space` bigint NOT NULL COMMENT '总空间（单位Byte）',
  `admin` tinyint(1) NULL DEFAULT 0 COMMENT '0:非管理员 1：管理员',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `key_nick_name`(`nick_name` ASC) USING BTREE,
  UNIQUE INDEX `key_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `key_qq_open_id`(`qq_open_id` ASC) USING BTREE,
  INDEX `idx_is_dummy` (`is_dummy` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用于存放用户信息的表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 文件信息表
-- ----------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info`  (
  `file_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件ID:主键',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件md5值:用于校验文件是否相同',
  `file_pid` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '文件父文件夹ID',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名',
  `file_cover` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件封面路径',
  `file_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件路径',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后一次修改时间',
  `folder_type` int NOT NULL DEFAULT 0 COMMENT '0:文件 1：文件夹目录',
  `file_category` int NULL DEFAULT NULL COMMENT '文件粗分类：1：视频 ，2：音频 ，3：图片 ，4：文档，5：其他',
  `file_type` int NULL DEFAULT NULL COMMENT '文件细分类：1：视频，2：音频，3：图片，4：pdf，5：doc，6：xlsx，7：txt，8：代码，9：压缩包，10：其他',
  `status` int NULL DEFAULT 2 COMMENT '文件状态：0：转码中，1：转码失败 ，2：转码成功',
  `recovery_time` datetime NULL DEFAULT NULL COMMENT '文件进入回收站的时间',
  `del_flag` int NOT NULL COMMENT '删除标记：0：删除，1：回收站，2：正常',
  `belonging_home` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属家庭ID：NULL表示个人文件，非NULL表示家庭文件',
  `visible_to_care` tinyint NOT NULL DEFAULT 0 COMMENT '对关怀用户可见性：0-不可见，1-可见',
  PRIMARY KEY (`file_id`, `user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_file_pid`(`file_pid` ASC) USING BTREE,
  INDEX `idx_md5`(`file_md5` ASC) USING BTREE,
  INDEX `idx_recovery_time`(`recovery_time` ASC) USING BTREE,
  INDEX `idx_belonging_home`(`belonging_home` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用于存放文件信息的表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 文件分享表
-- ----------------------------
DROP TABLE IF EXISTS `file_share`;
CREATE TABLE `file_share`  (
  `share_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享链接id',
  `file_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对应文件id',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享者id',
  `valid_type` int NULL DEFAULT NULL COMMENT '分享文件时间：0：一天；  1：7天；  2：30天；  3：长期有效',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件提取码',
  `show_count` int UNSIGNED NULL DEFAULT 0 COMMENT '浏览次数',
  `share_time` datetime NULL DEFAULT NULL COMMENT '分享时间',
  `file_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分享文件名',
  `folder_type` int NULL DEFAULT NULL COMMENT '文件夹类型0：文件1：文件夹',
  `file_Category` int NULL DEFAULT NULL COMMENT '文件粗分类',
  `file_type` int NULL DEFAULT NULL COMMENT '文件细分类',
  `file_cover` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件封面路径',
  PRIMARY KEY (`share_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分享文件信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 家庭信息表
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family` (
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

-- ----------------------------
-- 家庭成员表
-- ----------------------------
DROP TABLE IF EXISTS `family_member`;
CREATE TABLE `family_member` (
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

-- ----------------------------
-- 家庭邀请表
-- ----------------------------
DROP TABLE IF EXISTS `family_invite`;
CREATE TABLE `family_invite` (
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

-- ----------------------------
-- 家庭申请表
-- ----------------------------
DROP TABLE IF EXISTS `family_apply`;
CREATE TABLE `family_apply` (
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

-- ----------------------------
-- 权限表
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
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

-- ----------------------------
-- 好友关系表
-- ----------------------------
DROP TABLE IF EXISTS `friend_relation`;
CREATE TABLE `friend_relation`  (
  `relation_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关系ID',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起者用户ID',
  `friend_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '好友用户ID',
  `status` int NOT NULL DEFAULT 1 COMMENT '关系状态：0-已删除，1-已接受，2-已拒绝',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '好友备注名',
  `group_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '好友分组ID',
  `is_special` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否特别关注：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE INDEX `idx_user_friend` (`user_id` ASC, `friend_id` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_friend_id` (`friend_id` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE,
  INDEX `idx_special` (`user_id` ASC, `is_special` ASC) USING BTREE,
  CONSTRAINT `fk_friend_relation_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_friend_relation_friend` FOREIGN KEY (`friend_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 好友申请记录表
-- ----------------------------
DROP TABLE IF EXISTS `friend_request`;
CREATE TABLE `friend_request`  (
  `request_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请ID',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请者用户ID',
  `friend_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '被申请者用户ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '申请状态：0-待审核，1-已接受，2-已拒绝',
  `message` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请消息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`request_id`) USING BTREE,
  INDEX `idx_user_friend_request` (`user_id` ASC, `friend_id` ASC) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_friend_id` (`friend_id` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE,
  INDEX `idx_create_time` (`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_friend_request_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_friend_request_friend` FOREIGN KEY (`friend_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友申请记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 好友文件分享表
-- ----------------------------
DROP TABLE IF EXISTS `friend_file_share`;
CREATE TABLE `friend_file_share`  (
  `share_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分享ID',
  `from_user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分享者用户ID',
  `to_user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收者用户ID',
  `file_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件ID',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名',
  `folder_type` tinyint NOT NULL DEFAULT 0 COMMENT '文件类型：0-文件，1-文件夹',
  `file_type` tinyint NULL DEFAULT NULL COMMENT '文件类型：0-其他，1-视频，2-音频，3-图片，4-文档，5-压缩包',
  `file_category` tinyint NULL DEFAULT NULL COMMENT '文件分类：1-视频，2-音频，3-图片，4-文档，5-其他',
  `file_cover` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件封面',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `valid_type` tinyint NOT NULL DEFAULT 0 COMMENT '有效期类型：0-永久，1-1天，2-7天，3-30天',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `share_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分享时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已取消，1-有效，2-已过期',
  PRIMARY KEY (`share_id`) USING BTREE,
  INDEX `idx_from_user` (`from_user_id` ASC) USING BTREE,
  INDEX `idx_to_user` (`to_user_id` ASC) USING BTREE,
  INDEX `idx_share_time` (`share_time` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE,
  CONSTRAINT `fk_friend_share_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_friend_share_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友文件分享表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 关怀账号登录码表
-- ----------------------------
DROP TABLE IF EXISTS `care_account_login_code`;
CREATE TABLE `care_account_login_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(15) NOT NULL COMMENT '关怀账号用户ID',
    `login_code` VARCHAR(20) NOT NULL COMMENT '登录码',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_login_code` (`login_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关怀账号登录码表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------

-- 用户初始数据//超管初始密码为Aa2990945
INSERT INTO `user_info` VALUES
('sj7dSkDf85ug9So', 'Rocky23318', '3169632223@qq.com', NULL, 'sj7dSkDf85ug9So.jpg', '356318609b397963f434cae075b6c2c0', '2024-07-13 13:50:21', '2024-07-20 02:38:26', 1, 0, 5802483, 104857600, 1),
('VS32nY4jrNGycc5', 'LuoShuang', '3378199813@qq.com', NULL, 'VS32nY4jrNGycc5.jpg', '1c117de4467fb3221c9f3f5ea7d726e9', '2024-07-14 09:04:24', '2024-07-15 15:53:06', 1, 0, 0, 1073741824, 0);

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 初始化完成
-- ----------------------------
-- 数据库已成功创建，包含以下功能模块：
-- 1. 用户系统（支持关怀账号）
-- 2. 文件管理系统（支持个人和家庭空间）
-- 3. 家庭空间系统（完整的权限管理）
-- 4. 好友系统（好友关系和申请）
-- 5. 分享系统（公开分享和好友分享）
-- 6. 关怀账号登录码系统
--
-- 所有表已包含必要的索引和外键约束，确保数据一致性和查询性能
-- ----------------------------

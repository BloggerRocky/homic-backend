-- 家庭功能相关表

-- ----------------------------
-- Table structure for family
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family`  (
  `family_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家庭ID',
  `family_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家庭名称',
  `owner_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家庭所有者ID（创建者）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '家庭描述',
  `total_space` bigint NOT NULL DEFAULT 1099511627776 COMMENT '家庭总空间（单位Byte，默认1TB）',
  `use_space` bigint NOT NULL DEFAULT 0 COMMENT '家庭已用空间（单位Byte）',
  `member_count` int NOT NULL DEFAULT 1 COMMENT '家庭成员数',
  `status` int NOT NULL DEFAULT 1 COMMENT '家庭状态：0-禁用，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`family_id`) USING BTREE,
  UNIQUE INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '家庭信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for family_member
-- ----------------------------
DROP TABLE IF EXISTS `family_member`;
CREATE TABLE `family_member`  (
  `member_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '成员ID',
  `family_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家庭ID',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `role_id` int NOT NULL COMMENT '角色ID：1-主人(Owner)，2-家长(Guardian)，3-儿童(Children)',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在家庭中的昵称',
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入家庭的时间',
  `status` int NOT NULL DEFAULT 1 COMMENT '成员状态：0-已移除，1-正常，2-待审核',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`member_id`) USING BTREE,
  UNIQUE INDEX `idx_family_user`(`family_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_family_member_family` FOREIGN KEY (`family_id`) REFERENCES `family` (`family_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_family_member_user` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '家庭成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 修改user_info表，添加family_id字段
-- ----------------------------
ALTER TABLE `user_info` ADD COLUMN `family_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属家庭ID' AFTER `admin`;
ALTER TABLE `user_info` ADD INDEX `idx_family_id`(`family_id` ASC);

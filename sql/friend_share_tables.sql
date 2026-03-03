-- 好友分享功能相关表

-- ----------------------------
-- Table structure for friend_file_share
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

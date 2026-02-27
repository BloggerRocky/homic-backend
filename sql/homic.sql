/*
 Navicat Premium Dump SQL

 Source Server         : 主机
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : homic

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 20/07/2024 11:53:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for file_info
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
  PRIMARY KEY (`file_id`, `user_id`) USING BTREE,
  UNIQUE INDEX `idx_main_key`(`file_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_file_pid`(`file_pid` ASC) USING BTREE,
  INDEX `idx_md5`(`file_md5` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_recovery_time`(`recovery_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用于存放文件信息的表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_info
-- ----------------------------
INSERT INTO `file_info` VALUES ('2LYyhSfXixsj0N6', 'sj7dSkDf85ug9So', NULL, '0', NULL, '555', 'file/0000-00-00/system/default_folder_icon.jpg', NULL, '2024-07-20 01:25:38', '2024-07-20 01:25:38', 1, NULL, NULL, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('2Mm05N47SvX4jta', 'sj7dSkDf85ug9So', 'efc2d35059c20192f279fa91877430e3', '0', 2023563, 'bandicam 2023-11-24 22-39-13-879.mp4', 'file/2024-07-19/2Mm05N47SvX4jta/cover.jpg', 'file/2024-07-19/2Mm05N47SvX4jta/bandicam 2023-11-24 22-39-13-879.mp4', '2024-07-19 22:16:43', '2024-07-19 22:16:43', 0, 1, 1, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('EGSgvlH7zN7TMyi', 'sj7dSkDf85ug9So', 'edff3fcf2866fd162a09f835ed8b7cea', '0', 3778920, '556785937-1-16.mp4', 'file/2024-07-19/EGSgvlH7zN7TMyi/cover.jpg', 'file/2024-07-19/EGSgvlH7zN7TMyi/556785937-1-16.mp4', '2024-07-19 22:53:54', '2024-07-19 22:53:54', 0, 1, 1, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('tx3nECiybNak7IL', 'sj7dSkDf85ug9So', 'edff3fcf2866fd162a09f835ed8b7cea', '2LYyhSfXixsj0N6', 3778920, '556785937-1-16.mp4', 'file/2024-07-19/EGSgvlH7zN7TMyi/cover.jpg', 'file/2024-07-19/EGSgvlH7zN7TMyi/556785937-1-16.mp4', '2024-07-20 02:42:36', '2024-07-19 22:53:54', 0, 1, 1, 2, NULL, 2);

-- ----------------------------
-- Table structure for file_share
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分享文件信息\r\n' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_share
-- ----------------------------
INSERT INTO `file_share` VALUES ('etOlfdhBXP5nZ0Kt0Yw4', 'EGSgvlH7zN7TMyi', 'sj7dSkDf85ug9So', 0, '2024-07-20 23:16:40', '61443', 0, '2024-07-19 23:16:40', '556785937-1-16.mp4', 0, 1, 1, 'file/2024-07-19/EGSgvlH7zN7TMyi/cover.jpg');
INSERT INTO `file_share` VALUES ('IEgrkvf3EwEJ85osofWs', '2LYyhSfXixsj0N6', 'sj7dSkDf85ug9So', 0, '2024-07-21 01:25:47', '11682', 0, '2024-07-20 01:25:47', '555', 1, NULL, NULL, 'file/0000-00-00/system/default_folder_icon.jpg');

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户邮箱地址',
  `qq_open_id` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'QQ号',
  `user_avatar` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'default_avatar.jpg' COMMENT '头像',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始密码的md5值（长度固定32位)',
  `join_time` datetime NULL DEFAULT NULL COMMENT '账号注册的日期',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '用户上一次登录的时间',
  `status` int NOT NULL DEFAULT 0 COMMENT '0代表用户封禁 1代表用户正常',
  `use_space` bigint NOT NULL COMMENT '可用空间（单位Byte）',
  `total_space` bigint NOT NULL COMMENT '总使用空间（单位Byte）',
  `admin` tinyint(1) NULL DEFAULT 0 COMMENT '0:非管理员 1：管理员',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `key_nick_name`(`nick_name` ASC) USING BTREE,
  UNIQUE INDEX `key_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `key_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `key_qq_open_id`(`qq_open_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用于存放用户信息的表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES ('sj7dSkDf85ug9So', 'Rocky23318', '3169632223@qq.com', NULL, 'sj7dSkDf85ug9So.jpg', '356318609b397963f434cae075b6c2c0', '2024-07-13 13:50:21', '2024-07-20 02:38:26', 1, 5802483, 104857600, 1);
INSERT INTO `user_info` VALUES ('VS32nY4jrNGycc5', 'LuoShuang', '3378199813@qq.com', NULL, 'VS32nY4jrNGycc5.jpg', '1c117de4467fb3221c9f3f5ea7d726e9', '2024-07-14 09:04:24', '2024-07-15 15:53:06', 1, 0, 1073741824, 0);

SET FOREIGN_KEY_CHECKS = 1;

-- 关怀账号登录码表
CREATE TABLE IF NOT EXISTS `care_account_login_code` (
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

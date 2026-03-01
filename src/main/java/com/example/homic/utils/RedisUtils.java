package com.example.homic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具类（静态方法）
 * 作者：Rocky23318
 * 时间：2024
 * 项目名：homic
 */
public class RedisUtils {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 生成 Redis key 前缀
     */
    public static String buildKey(String prefix, String... params) {
        StringBuilder key = new StringBuilder(prefix);
        for (String param : params) {
            key.append(":").append(param);
        }
        return key.toString();
    }

    /**
     * 生成用户空间 key
     */
    public static String getUserSpaceKey(String userId) {
        return buildKey("user:space", userId);
    }

    /**
     * 生成邮箱验证码 key
     */
    public static String getEmailCodeKey(String prefix, String email) {
        return buildKey(prefix, email);
    }

    /**
     * 生成临时文件大小 key
     */
    public static String getTempSizeKey(String userId, String fileId) {
        return buildKey("temp:size", userId, fileId);
    }

    /**
     * 生成下载码 key
     */
    public static String getDownloadCodeKey(String code) {
        return buildKey("download:code", code);
    }

    /**
     * 生成系统设置 key
     */
    public static String getSystemSettingKey() {
        return "system:setting";
    }

    /**
     * 生成好友码 key
     */
    public static String getFriendCodeKey(String friendCode) {
        return buildKey("friend:code", friendCode);
    }

    /**
     * 生成用户好友码映射 key
     */
    public static String getUserFriendCodeKey(String userId) {
        return buildKey("user:friend:code", userId);
    }

    /**
     * 生成好友码生成冷却 key
     */
    public static String getFriendCodeCooldownKey(String userId) {
        return buildKey("friend:code:cooldown", userId);
    }
}


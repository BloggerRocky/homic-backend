package com.example.homic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis 管理器，处理序列化和反序列化
 * 作者：Rocky23318
 * 时间：2024
 * 项目名：homic
 */
@Component
public class RedisManager {
    private static final Logger logger = LoggerFactory.getLogger(RedisManager.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取值并反序列化为指定类型
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return clazz.cast(value);
        } catch (Exception e) {
            logger.error("获取Redis键值失败: {}", key, e);
            return null;
        }
    }

    /**
     * 设置键值对
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置Redis键值对失败 - key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 设置键值对并指定过期时间（秒）
     */
    public boolean setex(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置Redis有效时长键值对失败 - key: {}, time: {}s", key, time, e);
            return false;
        }
    }

    /**
     * 删除键
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            logger.error("删除Redis键失败: {}", key, e);
            return false;
        }
    }

    /**
     * 检查键是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("检查Redis键存在性失败: {}", key, e);
            return false;
        }
    }
}

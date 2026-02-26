package com.example.homic.utils;

import org.apache.ibatis.javassist.bytecode.stackmap.BasicBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.12:14
 * 项目名：homic
 */
//Redis操作工具类
@Component("redisUtils")//<V>是用于处理包装类的泛型，在映入RedisUtils要进行声明
public class RedisUtils<V> {
    @Resource
    private RedisTemplate<String,V> redisTemplate;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(RedisUtils.class);
    //获取一个值
    public V get(String key)
    {
        return key == null?null:redisTemplate.opsForValue().get(key);
    }
    //设置一个KV键值对
    public boolean set(String key,V value)
    {
        try {
            redisTemplate.opsForValue().set(key, value);
        }catch(Exception e)
        {
            logger.error("设置Redis键值对{}：{}失败",key,value);
            return false;
        }
        return true;
    }
    //设置一个含过期时间的KV键值对
    public boolean setex(String key,V value,long time)
    {
        try {
            if (time > 0)
                redisTemplate.opsForValue().set(key,value,time, TimeUnit.SECONDS);
            else
                set(key,value);
        }catch(Exception e)
        {
            logger.error("设置Redis有效时长为{}s的键值对{}：{}失败,",time,key,value);
            return false;
        }
        return true;
    }
    public  boolean delete(String key)
    {
        return redisTemplate.delete(key);
    }
}

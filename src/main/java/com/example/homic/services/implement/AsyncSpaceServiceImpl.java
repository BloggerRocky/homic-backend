package com.example.homic.services.implement;

import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.mapper.FamilyMapper;
import com.example.homic.model.UserInfo;
import com.example.homic.model.Family;
import com.example.homic.services.AsyncFamilySpaceService;
import com.example.homic.config.RedisManager;
import com.example.homic.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.homic.constants.NormalConstants.REDIS_DEFAULT_EXPIRE_TIME;

/**
 * 异步家庭空间服务实现
 * 串行化执行家庭空间大小更新任务，确保数据一致性
 */
@Service
public class AsyncSpaceServiceImpl implements AsyncFamilySpaceService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncSpaceServiceImpl.class);

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private FamilyMapper familyMapper;

    // 使用ConcurrentHashMap来存储每个家庭的锁，实现串行化
    private final ConcurrentHashMap<String, ReentrantLock> familyLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    @Override
    @Async("familySpaceTaskExecutor")
    public void asyncUpdateFamilySpaceUsage(String familyId) {
        if (familyId == null || familyId.trim().isEmpty()) {
            logger.warn("家庭ID为空，跳过空间大小更新");
            return;
        }

        // 获取或创建该家庭的锁
        ReentrantLock lock = familyLocks.computeIfAbsent(familyId, k -> new ReentrantLock());

        try {
            // 获取锁，确保串行化执行
            lock.lock();

            logger.info("开始异步更新家庭空间使用大小，家庭ID: {}", familyId);

            // 从数据库计算家庭空间使用大小
            Long useSpace = fileInfoMapper.getSizeByFamilyId(familyId);
            useSpace = useSpace == null ? 0L : useSpace;

            // 更新数据库中的家庭空间使用大小
            Family family = new Family();
            family.setFamilyId(familyId);
            family.setUseSpace(useSpace);
            familyMapper.updateById(family);
            logger.info("已更新数据库家庭空间使用大小，家庭ID: {}, 使用空间: {} bytes", familyId, useSpace);

            // 更新Redis缓存
            String redisKey = RedisUtils.getFamilySpaceKey(familyId);
            RedisUseSpaceDTO spaceDTO = redisManager.get(redisKey, RedisUseSpaceDTO.class);

            if (spaceDTO != null) {
                spaceDTO.setUseSpace(useSpace);
                redisManager.setex(redisKey, spaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
                logger.info("家庭空间使用大小更新完成，家庭ID: {}, 使用空间: {} bytes", familyId, useSpace);
            } else {
                // 如果缓存中没有，创建新的缓存对象
                spaceDTO = new RedisUseSpaceDTO();
                spaceDTO.setUseSpace(useSpace);
                redisManager.setex(redisKey, spaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
                logger.info("创建家庭空间缓存，家庭ID: {}, 使用空间: {} bytes", familyId, useSpace);
            }

        } catch (Exception e) {
            logger.error("异步更新家庭空间使用大小失败，家庭ID: {}", familyId, e);
        } finally {
            // 释放锁
            lock.unlock();

            // 清理不再需要的锁（可选，避免内存泄漏）
            if (!lock.hasQueuedThreads()) {
                familyLocks.remove(familyId, lock);
            }
        }
    }

    @Override
    @Async("familySpaceTaskExecutor")
    public void asyncUpdatePersonalSpaceUsage(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("用户ID为空，跳过空间大小更新");
            return;
        }

        // 获取或创建该用户的锁
        ReentrantLock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());

        try {
            // 获取锁，确保串行化执行
            lock.lock();

            logger.info("开始异步更新个人空间使用大小，用户ID: {}", userId);

            // 从数据库计算个人空间使用大小
            Long useSpace = fileInfoMapper.getSizeByUserId(userId);
            useSpace = useSpace == null ? 0L : useSpace;

            // 更新数据库中的用户空间使用大小
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setUseSpace(useSpace);
            userInfoMapper.updateById(userInfo);
            logger.info("已更新数据库个人空间使用大小，用户ID: {}, 使用空间: {} bytes", userId, useSpace);

            // 更新Redis缓存
            String redisKey = RedisUtils.getUserSpaceKey(userId);
            RedisUseSpaceDTO spaceDTO = redisManager.get(redisKey, RedisUseSpaceDTO.class);

            if (spaceDTO != null) {
                spaceDTO.setUseSpace(useSpace);
                redisManager.setex(redisKey, spaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
                logger.info("个人空间使用大小更新完成，用户ID: {}, 使用空间: {} bytes", userId, useSpace);
            } else {
                // 如果缓存中没有，创建新的缓存对象
                spaceDTO = new RedisUseSpaceDTO();
                spaceDTO.setUseSpace(useSpace);
                redisManager.setex(redisKey, spaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
                logger.info("创建个人空间缓存，用户ID: {}, 使用空间: {} bytes", userId, useSpace);
            }

        } catch (Exception e) {
            logger.error("异步更新个人空间使用大小失败，用户ID: {}", userId, e);
        } finally {
            // 释放锁
            lock.unlock();

            // 清理不再需要的锁（可选，避免内存泄漏）
            if (!lock.hasQueuedThreads()) {
                userLocks.remove(userId, lock);
            }
        }
    }
}

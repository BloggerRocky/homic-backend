package com.example.homic.services.implement;

import com.example.homic.config.RedisManager;
import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.mapper.FileInfoMapper;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.model.UserInfo;
import com.example.homic.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.example.homic.constants.NormalConstants.REDIS_DEFAULT_EXPIRE_TIME;
import static com.example.homic.constants.NormalConstants.REDIS_USER_SPACE_PREFIX;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/18.22:33
 * 项目名：homic
 */
public class CommonServiceImpl {
    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    RedisManager redisManager;
    @Autowired
    UserInfoMapper userInfoMapper;
    //刷新内存信息
    void refreshUseSpace(String userId) {
        //计算已用内存总量
        Long useSpace = fileInfoMapper.getSizeByUserId(userId);
        useSpace = useSpace==null?0:useSpace;
        //更新redis缓存
        RedisUseSpaceDTO redisUseSpaceDTO = redisManager.get(RedisUtils.getUserSpaceKey(userId), RedisUseSpaceDTO.class);
        redisUseSpaceDTO.setUseSpace(useSpace);
        redisManager.setex(RedisUtils.getUserSpaceKey(userId), redisUseSpaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
        //更新数据库
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUseSpace(useSpace);
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }
}

package com.example.homic.services.implement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.homic.constants.NormalConstants;
import com.example.homic.dto.redis.RedisSettingDTO;
import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.UserInfoMapper;
import com.example.homic.model.UserInfo;
import com.example.homic.services.AdminService;
import com.example.homic.utils.RedisUtils;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.UserInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.homic.constants.CodeConstants.FAIL_RES_CODE;
import static com.example.homic.constants.NormalConstants.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.15:22
 * 项目名：homic
 */
@Service
public class AdminServiceImpl extends CommonServiceImpl implements AdminService   {
    @Autowired
    RedisUtils<RedisSettingDTO> redisUtilsForSetting;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    LambdaQueryWrapper<UserInfo> userInfoLqw;
    private static Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    /**
     * 获取系统设置信息
     * @return
     */
    @Override
    public RedisSettingDTO getSysSettings() {
        RedisSettingDTO redisSettingDTO = redisUtilsForSetting.get(REDIS_SYSTEM_SETTING_KEY);
        redisSettingDTO = redisSettingDTO == null ? DEFAULT_SETTING_INFO: redisSettingDTO;
        return redisSettingDTO;
    }

    /**
     * 保存系统信息
     * @param settingInfo
     * @return
     */
    @Override
    public boolean saveSysSettings(RedisSettingDTO settingInfo) {
        if(settingInfo.getUserInitUseSpace()<=0)
            return false;
        if(!settingInfo.getRegisterEmailContent().contains("%s"))
            return false;
        return redisUtilsForSetting.set(REDIS_SYSTEM_SETTING_KEY,settingInfo);
    }

    /**
     * 加载用户列表
     * @param pageNoStr
     * @param pageSizeStr
     * @param nickNameFuzzy
     * @param status
     * @return
     * @throws MyException
     */
    @Override
    public PageResultVO<UserInfoVO> loadUserList(String pageNoStr, String pageSizeStr, String nickNameFuzzy, String status) throws MyException {
        userInfoLqw.clear();
        if(nickNameFuzzy != null && !nickNameFuzzy.equals(""))
            userInfoLqw.like(UserInfo::getNickName,nickNameFuzzy);
        if(status != null && !status.equals(""))
            userInfoLqw.eq(UserInfo::getStatus,status);
        userInfoLqw.eq(UserInfo::getAdmin,false);
        try {
            Integer pageNo = pageNoStr.equals("")?1:Integer.parseInt(pageNoStr);
            Integer pageSize = pageSizeStr.equals("")? NormalConstants.DEFAULT_PAGE_SIZE:Integer.parseInt(pageSizeStr);
            IPage page = new Page<>(pageNo,pageSize);
            page = userInfoMapper.selectPage(page,userInfoLqw);
            PageResultVO<UserInfoVO> pageResult = new PageResultVO<>(page,UserInfoVO.class);
            return pageResult;
        } catch (Exception e) {
            throw new MyException("加载用户列表失败",FAIL_RES_CODE);
        }
    }

    /**
     * 修改用户状态
     * @param userId
     * @param status
     * @return
     */
    @Override
    public boolean updateUserStatus(String userId, String status) {
        userInfoLqw.clear();
        userInfoLqw.eq(UserInfo::getUserId,userId);
        UserInfo userInfo = new UserInfo();
        try {
            userInfo.setStatus(Integer.parseInt(status));
            userInfoMapper.update(userInfo,userInfoLqw);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateUserSpace(String userId, Integer space) {
        try {
            userInfoLqw.clear();
            userInfoLqw.eq(UserInfo::getUserId,userId);
            UserInfo userInfo = userInfoMapper.selectOne(userInfoLqw);
            Long oldUseSpace = userInfo.getUseSpace();
            Long newSpace = space*1024*1024L;
            System.out.println(oldUseSpace);
            System.out.println(newSpace);
            if(oldUseSpace>newSpace)
                return false;
            userInfo.setTotalSpace(newSpace);
            //修改数据库信息，修改空间使用状态
            userInfoMapper.update(userInfo,userInfoLqw);
            RedisUseSpaceDTO redisUseSpaceDTO = new RedisUseSpaceDTO();
            //将信息传入redis
            redisUseSpaceDTO.setUseSpace(oldUseSpace);
            redisUseSpaceDTO.setTotalSpace(newSpace);
            redisUtilsForUserSpace.setex(REDIS_USER_SPACE_PREFIX+userId,redisUseSpaceDTO,REDIS_DEFAULT_EXPIRE_TIME);
            return true;
        } catch (Exception e) {
            logger.error("修改用户空间失败",e);
            return false;
        }
    }
}

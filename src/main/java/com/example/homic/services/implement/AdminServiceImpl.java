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
import com.example.homic.config.RedisManager;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.RedisUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UserInfoVO;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

import static com.example.homic.constants.CodeConstants.FAIL_RES_CODE;
import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.NormalConstants.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.15:22
 * 项目名：homic
 */
@Service
public class AdminServiceImpl extends CommonServiceImpl implements AdminService   {
    @Autowired
    RedisManager redisManager;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    MinioUtils minioUtils;

    private static Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private static final String[] PICTURE_TYPE_ARRAY = new String[]{".png", ".PNG", ".jpg", ".JPG", ".jpeg", ".JPEG", ".gif", ".GIF", ".bmp", ".BMP"};
    /**
     * 获取系统设置信息
     * @return
     */
    @Override
    public RedisSettingDTO getSysSettings() {
        RedisSettingDTO redisSettingDTO = redisManager.get(RedisUtils.getSystemSettingKey(), RedisSettingDTO.class);
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
        return redisManager.set(RedisUtils.getSystemSettingKey(), settingInfo);
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
        LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
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
        LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
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
            LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
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
            redisManager.setex(RedisUtils.getUserSpaceKey(userId), redisUseSpaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
            return true;
        } catch (Exception e) {
            logger.error("修改用户空间失败",e);
            return false;
        }
    }

    @Override
    public ResponseVO createUser(String email, String nickName, String password, MultipartFile avatar) throws Exception {
        // 验证邮箱格式
        if (email == null || !email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return new ResponseVO(FAIL_RES_STATUS, "邮箱格式不正确");
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<UserInfo> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserInfo::getEmail, email);
        if (userInfoMapper.selectCount(checkWrapper) > 0) {
            return new ResponseVO(FAIL_RES_STATUS, "该邮箱已被注册");
        }

        // 验证昵称
        if (nickName == null || nickName.trim().isEmpty()) {
            nickName = email.split("@")[0]; // 如果昵称为空，使用邮箱前缀
        }

        // 验证密码
        if (password == null || password.length() < 6) {
            return new ResponseVO(FAIL_RES_STATUS, "密码长度至少为6位");
        }

        // 生成用户ID
        String userId = StringUtils.getSerialNumber(10);

        // 处理头像上传
        String avatarName = null;
        if (avatar != null && !avatar.isEmpty()) {
            // 验证文件类型
            String fileType = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."));
            if (!ArrayUtils.contains(PICTURE_TYPE_ARRAY, fileType)) {
                return new ResponseVO(FAIL_RES_STATUS, "头像格式不支持");
            }

            // 生成头像文件名
            avatarName = userId + fileType;

            try {
                minioUtils.saveMultipartFile(FILE_AVATAR_PATH + avatarName, avatar);
            } catch (MyException e) {
                logger.error("头像上传失败", e);
                return new ResponseVO(FAIL_RES_STATUS, "头像上传失败");
            }
        }

        // 获取系统设置
        RedisSettingDTO systemSettings = getSysSettings();
        Long totalSpace = systemSettings.getUserInitUseSpace() * 1024 * 1024L;

        // 创建用户
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringUtils.getMD5(password));
        userInfo.setJoinTime(new Date());
        userInfo.setLastLoginTime(new Date());
        userInfo.setStatus(1); // 默认启用
        userInfo.setUseSpace(0L);
        userInfo.setTotalSpace(totalSpace);
        userInfo.setAdmin(false);

        if (avatarName != null) {
            userInfo.setUserAvatar(avatarName);
        }

        try {
            userInfoMapper.insert(userInfo);
            return new ResponseVO(SUCCESS_RES_STATUS, "用户创建成功");
        } catch (Exception e) {
            logger.error("创建用户失败", e);
            return new ResponseVO(FAIL_RES_STATUS, "创建用户失败");
        }
    }
}

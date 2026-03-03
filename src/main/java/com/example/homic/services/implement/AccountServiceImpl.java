package com.example.homic.services.implement;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.config.RedisManager;
import com.example.homic.config.properties.AppProperties;
import com.example.homic.dto.redis.RedisEmailCodeDTO;
import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.UserInfoMapper;

import com.example.homic.model.UserInfo;
import com.example.homic.services.AccountService;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.utils.RedisUtils;
import com.example.homic.vo.ResponseVO;
import com.example.homic.dto.redis.RedisSettingDTO;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.units.qual.min;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.example.homic.constants.CodeConstants.*;
import static com.example.homic.constants.NormalConstants.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/11.21:40
 * 项目名：homic
 */
@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Autowired
    UserInfoMapper userInfoMapper;
    @Resource
    JavaMailSender javaMailSender;
    @Autowired
    AppProperties appProperties;
    @Autowired
    RedisManager redisManager;
    @Autowired
    MinioUtils minioUtils;
    @Autowired
    ModelMapper modelMapper;

    /**
     * 发送邮箱验证码(包含注册和重置密码)
     * @param email
     * @param type
     * @return
     */
    public ResponseVO sendEmailCode(String email,Integer type) throws MyException {
        String text = null;
        String title = null;
        String prefix = null;
        String code = StringUtils.getRandomNumber(EMAIL_CODE_LENGTH);
        // 如果为0，表示发送注册验证码
        if(type == 0)
        {
            RedisSettingDTO redisSettingDTO = redisManager.get(RedisUtils.getSystemSettingKey(), RedisSettingDTO.class);
            redisSettingDTO = redisSettingDTO == null ? DEFAULT_SETTING_INFO : redisSettingDTO;
            title = redisSettingDTO.getRegisterEmailTitle();
            text = String.format(redisSettingDTO.getRegisterEmailContent(),code);
            prefix = REDIS_REGIS_CODE_PREFIX;
        }
        // 如果为1，表示发送重置密码验证码
        else if(type == 1)
        {
            title = RESETTING_TITLE;
            text = String.format(RESETTING_CONTEXT,code);
            prefix = REDIS_RESETTING_CODE_PREFIX;
        }
        else {
            throw new MyException("邮件类型错误",FAIL_RES_CODE);
        }
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true);
            helper.setFrom(appProperties.getUsername());//设置发送人
            helper.setTo(email);//设置收件人
            helper.setSubject(title);//设置邮件标题
            helper.setText(text);//设置邮件内容,用code填充 %s 占位符
            helper.setSentDate(new Date());
            javaMailSender.send(message);//发送邮件
        }catch(Exception e)
        {
            logger.error("发送到{}的邮件发送失败",email);
            throw new MyException("邮箱验证码发送失败",FAIL_RES_CODE);
        }
        RedisEmailCodeDTO redisEmailCodeDTO = new RedisEmailCodeDTO(appProperties.getUsername(), email, code);
        redisManager.setex(RedisUtils.getEmailCodeKey(prefix, email), redisEmailCodeDTO, 900L);
        return new ResponseVO(SUCCESS_RES_STATUS,"邮件发送成功，请注意查收");
    }


    /**
     * 用户注册
     * @param email
     * @param nickName
     * @param password
     * @param emailCode
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO register(String email, String nickName, String password, String emailCode) {
        RedisEmailCodeDTO codeInfo = redisManager.get(RedisUtils.getEmailCodeKey(REDIS_REGIS_CODE_PREFIX, email), RedisEmailCodeDTO.class);
        if(codeInfo == null || !codeInfo.getCode().equals(emailCode))
            return new ResponseVO(FAIL_RES_STATUS,"邮箱验证码错误或已过期");
        else {
                LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
                userInfoLqw.eq(UserInfo::getEmail,email).or()
                        .eq(UserInfo::getNickName,nickName);
                if(userInfoMapper.selectOne(userInfoLqw)!=null)
                    return new ResponseVO(FAIL_RES_STATUS,"邮箱或昵称已存在");
                else{
                    UserInfo userInfo = new UserInfo();
                    userInfo.setEmail(email);
                    userInfo.setNickName(nickName);
                    userInfo.setUserAvatar(DEFAULT_AVATAR_FILE_NAME);
                    userInfo.setJoinTime(new Date());
                    userInfo.setStatus(USER_STATUS_ENABLE);
                    userInfo.setAdmin(false);
                    //对传入密码进行md5加密
                    password = StringUtils.getMD5(password);
                    userInfo.setPassword(password);
                    //获取一个长度为15的UserId
                    String randomCode = StringUtils.getSerialNumber(USER_ID_LENGTH);
                    userInfo.setUserId(randomCode);
                    //初始化使用空间
                    userInfo.setUseSpace(USER_DEFAULT_USE_SPACE);
                    RedisSettingDTO redisSettingDTO = redisManager.get(RedisUtils.getSystemSettingKey(), RedisSettingDTO.class);
                    redisSettingDTO = redisSettingDTO == null ? DEFAULT_SETTING_INFO : redisSettingDTO;
                    userInfo.setTotalSpace(redisSettingDTO.getUserInitUseSpace()*1024*1024L);
                    try {
                        //插入数据
                        userInfoMapper.insert(userInfo);
                        //删除Redis验证码缓存
                        redisManager.delete(RedisUtils.getEmailCodeKey(REDIS_REGIS_CODE_PREFIX, email));
                    }catch (Exception e)
                    {
                        logger.error("数据库操作异常",e);
                        return new ResponseVO(FAIL_RES_STATUS,"服务器异常");
                    }
                }



        }
        return new ResponseVO(SUCCESS_RES_STATUS,"注册成功");
    }


    /**
     * 用户登录
     * @param email
     * @param password//已经经过前端md5加密
     * @return
     * @throws MyException
     */
    public SessionWebUserDTO login(String email,String password) throws MyException {
        LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
        userInfoLqw.eq(UserInfo::getEmail,email);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoLqw);
        if(userInfo == null || !userInfo.getPassword().equals(password))
            throw new MyException("账号或密码错误",FAIL_RES_CODE);
        else
        {
            if(userInfo.getStatus() == USER_STATUS_DISABLE)
                throw new MyException("账号已被封禁",FAIL_RES_CODE);
            //更新最后一次登录时间
            userInfo.setLastLoginTime(new Date());
            userInfoMapper.updateByPrimaryKeySelective(userInfo);
            //将用户的空间使用信息缓存到Redis
            saveUseSpaceToRedis(userInfo);
            //装填用户信息
            SessionWebUserDTO sessionWebUserDTO = modelMapper.map(userInfo,SessionWebUserDTO.class);
            // 手动设置isDummy字段（Integer转Boolean）
            sessionWebUserDTO.setIsDummy(userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1);
            return sessionWebUserDTO;
        }
    }
    public  RedisUseSpaceDTO saveUseSpaceToRedis(UserInfo userInfo)
    {
        RedisUseSpaceDTO redisUseSpaceDTO = new RedisUseSpaceDTO();
        redisUseSpaceDTO.setTotalSpace(userInfo.getTotalSpace());
        redisUseSpaceDTO.setUseSpace(userInfo.getUseSpace());
        redisManager.setex(RedisUtils.getUserSpaceKey(userInfo.getUserId()), redisUseSpaceDTO, REDIS_DEFAULT_EXPIRE_TIME);
        return redisUseSpaceDTO;
    }

    /**
     * 重置密码
     * @param email
     * @param password
     * @param emailCode
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO resetPwd(String email, String password, String emailCode) {
        RedisEmailCodeDTO codeInfo = redisManager.get(RedisUtils.getEmailCodeKey(REDIS_RESETTING_CODE_PREFIX, email), RedisEmailCodeDTO.class);
        if(codeInfo == null || !codeInfo.getCode().equals(emailCode))
            return new ResponseVO(FAIL_RES_STATUS,"邮箱验证码错误或已过期");
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(StringUtils.getMD5(password));//对原始密码进行加密
        LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
        userInfoLqw.eq(UserInfo::getEmail,email);
        userInfoMapper.update(userInfo,userInfoLqw);
        return new ResponseVO(SUCCESS_RES_STATUS,"密码重置成功");
    }

    /**
     * 获取用户头像
     * @param response
     * @param userId
     * @return
     */
    @Override
    public void getAvatar(HttpServletResponse response,String userId) throws Exception {
        //查询用户头像图片文件名称
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        String avatarName = userInfo.getUserAvatar();
        //拼接文件夹位置
        if(StrUtil.isBlank(avatarName)){
            avatarName = DEFAULT_AVATAR_FILE_NAME;
        }
        String filePath = FILE_AVATAR_PATH+avatarName;
        if(!minioUtils.checkExist(filePath)){
            minioUtils.getFile(USER_DEFAULT_AVATAR_PATH, response);
        }else{
            minioUtils.getFile(filePath,response);
        }
    }

    /**
     * 获取用户空间使用信息
     * @param userId
     * @return
     */
    @Override
    public RedisUseSpaceDTO getUseSpace(String userId) {
        RedisUseSpaceDTO redisUseSpaceDTO = redisManager.get(RedisUtils.getUserSpaceKey(userId), RedisUseSpaceDTO.class);
        if(redisUseSpaceDTO == null)
        {
            UserInfo userInfo = getUserInfo(userId);
             redisUseSpaceDTO = saveUseSpaceToRedis(userInfo);
        }
        return redisUseSpaceDTO;
    }

    /**
     * 修改头像
     * @param userId
     * @param avatar
     * @return
     * @throws IOException
     */
    @Override
    public ResponseVO updateUserAvatar(String userId, MultipartFile avatar) throws Exception {
        String fileType = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."));
        if(!ArrayUtils.contains(PICTURE_TYPE_ARRAY,fileType))
            throw new MyException("图片格式不支持",FAIL_RES_CODE);
        String avatarName = userId+fileType;
        try {
            minioUtils.saveMultipartFile(FILE_AVATAR_PATH+avatarName,avatar);
        } catch (MyException e) {
            logger.error(e.msg);
            throw new MyException("头像上传失败",FAIL_RES_CODE);
        }
        //修改成功，更新头像地址
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setUserAvatar(avatarName);
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
            //设置返回信息
            return new ResponseVO(SUCCESS_RES_STATUS,"头像上传成功");


    }

    /**
     * 修改用户密码
     * @param password
     * @param userId
     * @return
     */
    @Override
    public ResponseVO updatePassword(String password, String userId) {
        LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
        userInfoLqw.eq(UserInfo::getUserId,userId);
        UserInfo userInfo = new UserInfo();
        password = StringUtils.getMD5(password);
        userInfo.setPassword(password);
        userInfoMapper.update(userInfo,userInfoLqw);
        return new ResponseVO(SUCCESS_RES_STATUS,"密码修改成功");
    }

    //通过userId获取用户信息
    public UserInfo getUserInfo(String userId)
    {
        LambdaQueryWrapper<UserInfo> userInfoLqw = new LambdaQueryWrapper<>();
        userInfoLqw.eq(UserInfo::getUserId,userId);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoLqw);
        return userInfo;
    }
}

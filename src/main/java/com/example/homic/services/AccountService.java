package com.example.homic.services;

import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.vo.ResponseVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/11.21:41
 * 项目名：homic
 */

public interface AccountService {
    /**
     * 发送邮箱验证码
     * @param email
     * @param type 0为注册，1为重置密码
     * @return
     * @throws MyException
     */
    ResponseVO sendEmailCode(String email,Integer type) throws MyException;

    /**
     * 注册用户
     * @param email
     * @param nickName
     * @param password
     * @param emailCode
     * @return
     */
    ResponseVO register(String email,String nickName,String password,String emailCode);

    /**
     * 用户登录
     * @param email
     * @param password
     * @return
     * @throws MyException
     */
    SessionWebUserDTO login (String email,String password) throws MyException;

    /**
     * 重置密码
     * @param email
     * @param password
     * @param emailCode
     * @return
     */
    ResponseVO resetPwd(String email, String password, String emailCode);

    /**
     * 获取用户头像
     * @param response
     * @param userId
     * @throws Exception
     */

    void getAvatar(HttpServletResponse response,String userId) throws Exception;

    /**
     * 获取用户使用空间
     * @param userId
     * @return
     */

    RedisUseSpaceDTO getUseSpace(String userId);

    /**更新用户头像
     *
     * @param userId
     * @param avaterFile
     * @return
     * @throws Exception
     */

    ResponseVO updateUserAvatar(String userId, MultipartFile avaterFile) throws Exception;

    /**
     * 修改密码
     * @param password
     * @param userId
     * @return
     */

    ResponseVO updatePassword(String password, String userId);
}

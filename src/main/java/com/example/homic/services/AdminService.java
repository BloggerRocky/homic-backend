package com.example.homic.services;

import com.example.homic.dto.redis.RedisSettingDTO;
import com.example.homic.exception.MyException;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UserInfoVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.15:23
 * 项目名：homic
 */
@Service
public interface AdminService {
    /**
     * 获取系统设定
     * @return
     */
    RedisSettingDTO getSysSettings();

    /**
     * 修改保存系统设定
     * @param settingInfo
     * @return
     */

    boolean saveSysSettings(RedisSettingDTO settingInfo);

    /**
     * 加载用户列表
     * @param pageNo
     * @param pageSize
     * @param nickNameFuzzy
     * @param status
     * @return
     * @throws MyException
     */

    PageResultVO<UserInfoVO> loadUserList(String pageNo, String pageSize, String nickNameFuzzy, String status) throws MyException;

    /**
     * 更改用户状态
     * @param userId
     * @param status
     * @return
     */

    boolean updateUserStatus(String userId, String status);

    /**
     * 更改用户最大空间
     * @param userId
     * @param space
     * @return
     */

    boolean updateUserSpace(String userId, Integer space);

    /**
     * 快速创建用户
     * @param email
     * @param nickName
     * @param password
     * @param avatar
     * @return
     */
    ResponseVO createUser(String email, String nickName, String password, MultipartFile avatar) throws Exception;
}

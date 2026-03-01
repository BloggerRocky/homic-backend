package com.example.homic.services;

import com.example.homic.exception.MyException;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UserSimpleInfoVO;

/**
 * 好友服务接口
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
public interface FriendService {

    /**
     * 生成好友码
     * @return 好友码
     * @throws MyException
     */
    ResponseVO generateFriendCode() throws MyException;

    /**
     * 通过好友码查询用户信息
     * @param friendCode 好友码
     * @return 用户简化信息（仅包含id、昵称、头像）
     * @throws MyException
     */
    UserSimpleInfoVO getUserByFriendCode(String friendCode) throws MyException;

    /**
     * 查询自身好友码
     * @return 用户的好友码
     * @throws MyException
     */
    ResponseVO getMyFriendCode() throws MyException;
}

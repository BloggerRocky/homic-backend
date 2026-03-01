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

    /**
     * 发送好友申请
     * @param userId 当前用户ID
     * @param friendId 被申请者的用户ID
     * @return 响应
     * @throws MyException
     */
    ResponseVO sendFriendRequest(String userId, String friendId) throws MyException;

    /**
     * 查询好友申请状态
     * @param userId 当前用户ID
     * @param friendId 被申请者的用户ID
     * @return 申请状态 (0-未申请, 1-已申请, 2-已接受, 3-已拒绝)
     * @throws MyException
     */
    Integer getFriendRequestStatus(String userId, String friendId) throws MyException;
}

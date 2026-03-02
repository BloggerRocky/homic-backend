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

    /**
     * 获取我发出的好友申请列表
     * @param userId 当前用户ID
     * @return 好友申请列表
     * @throws MyException
     */
    ResponseVO getSentRequests(String userId) throws MyException;

    /**
     * 获取我收到的好友申请列表
     * @param userId 当前用户ID
     * @return 好友申请列表
     * @throws MyException
     */
    ResponseVO getReceivedRequests(String userId) throws MyException;

    /**
     * 处理好友申请
     * @param userId 当前用户ID
     * @param requestId 申请ID
     * @param action 操作 (1-接受, 2-拒绝)
     * @return 响应
     * @throws MyException
     */
    ResponseVO handleFriendRequest(String userId, Long requestId, Integer action) throws MyException;

    /**
     * 获取我的好友列表
     * @param userId 当前用户ID
     * @return 好友列表
     * @throws MyException
     */
    ResponseVO getMyFriends(String userId) throws MyException;

    /**
     * 获取特别关注的好友列表
     * @param userId 当前用户ID
     * @return 好友列表
     * @throws MyException
     */
    ResponseVO getSpecialFriends(String userId) throws MyException;

    /**
     * 更新好友备注
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @param remark 备注名
     * @return 响应
     * @throws MyException
     */
    ResponseVO updateFriendRemark(String userId, String friendId, String remark) throws MyException;

    /**
     * 切换好友特别关注状态
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @param isSpecial 是否特别关注
     * @return 响应
     * @throws MyException
     */
    ResponseVO toggleSpecialAttention(String userId, String friendId, Boolean isSpecial) throws MyException;

    /**
     * 删除好友
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 响应
     * @throws MyException
     */
    ResponseVO deleteFriend(String userId, String friendId) throws MyException;
}

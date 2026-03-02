package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.services.FriendService;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UserSimpleInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.NormalConstants.SESSION_USER_INFO_KEY;

/**
 * 好友控制器
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
@RestController("friendController")
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 生成好友码
     *
     * @param session HTTP会话
     * @return 包含好友码的响应
     * @throws MyException
     */
    @RequestMapping("/generateFriendCode")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO generateFriendCode(HttpSession session) throws MyException {
        return friendService.generateFriendCode();
    }

    /**
     * 通过好友码查询用户信息
     *
     * @param friendCode 好友码
     * @return 用户简化信息（仅包含id、昵称、头像）
     * @throws MyException
     */
    @RequestMapping("/getUserByFriendCode")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO getUserByFriendCode(
            @VerifyParam(required = true)
            String friendCode) throws MyException {
        UserSimpleInfoVO userInfo = friendService.getUserByFriendCode(friendCode);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "查询成功");
        responseVO.setData(userInfo);
        return responseVO;
    }

    /**
     * 查询自身好友码
     *
     * @param session HTTP会话
     * @return 包含好友码的响应
     * @throws MyException
     */
    @RequestMapping("/getMyFriendCode")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getMyFriendCode(HttpSession session) throws MyException {
        return friendService.getMyFriendCode();
    }

    /**
     * 发送好友申请
     *
     * @param session HTTP会话
     * @param friendId 被申请者的用户ID
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/sendFriendRequest")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO sendFriendRequest(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.sendFriendRequest(userInfo.getUserId(), friendId);
    }

    /**
     * 查询好友申请状态
     *
     * @param session HTTP会话
     * @param friendId 被申请者的用户ID
     * @return 申请状态 (0-未申请, 1-已申请, 2-已接受, 3-已拒绝)
     * @throws MyException
     */
    @RequestMapping("/getFriendRequestStatus")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO getFriendRequestStatus(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        Integer status = friendService.getFriendRequestStatus(userInfo.getUserId(), friendId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "查询成功");
        responseVO.setData(status);
        return responseVO;
    }

    /**
     * 获取我发出的好友申请列表
     *
     * @param session HTTP会话
     * @return 好友申请列表
     * @throws MyException
     */
    @RequestMapping("/getSentRequests")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getSentRequests(HttpSession session) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.getSentRequests(userInfo.getUserId());
    }

    /**
     * 获取我收到的好友申请列表
     *
     * @param session HTTP会话
     * @return 好友申请列表
     * @throws MyException
     */
    @RequestMapping("/getReceivedRequests")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getReceivedRequests(HttpSession session) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.getReceivedRequests(userInfo.getUserId());
    }

    /**
     * 处理好友申请
     *
     * @param session HTTP会话
     * @param requestId 申请ID
     * @param action 操作 (1-接受, 2-拒绝)
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/handleFriendRequest")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO handleFriendRequest(
            HttpSession session,
            @VerifyParam(required = true)
            Long requestId,
            @VerifyParam(required = true)
            Integer action) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.handleFriendRequest(userInfo.getUserId(), requestId, action);
    }

    /**
     * 获取我的好友列表
     *
     * @param session HTTP会话
     * @return 好友列表
     * @throws MyException
     */
    @RequestMapping("/getMyFriends")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getMyFriends(HttpSession session) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.getMyFriends(userInfo.getUserId());
    }

    /**
     * 获取特别关注的好友列表
     *
     * @param session HTTP会话
     * @return 好友列表
     * @throws MyException
     */
    @RequestMapping("/getSpecialFriends")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getSpecialFriends(HttpSession session) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.getSpecialFriends(userInfo.getUserId());
    }

    /**
     * 更新好友备注
     *
     * @param session HTTP会话
     * @param friendId 好友ID
     * @param remark 备注名
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/updateFriendRemark")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO updateFriendRemark(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId,
            @VerifyParam(required = true)
            String remark) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.updateFriendRemark(userInfo.getUserId(), friendId, remark);
    }

    /**
     * 切换好友特别关注状态
     *
     * @param session HTTP会话
     * @param friendId 好友ID
     * @param isSpecial 是否特别关注
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/toggleSpecialAttention")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO toggleSpecialAttention(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId,
            @VerifyParam(required = true)
            Boolean isSpecial) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.toggleSpecialAttention(userInfo.getUserId(), friendId, isSpecial);
    }

    /**
     * 删除好友
     *
     * @param session HTTP会话
     * @param friendId 好友ID
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/deleteFriend")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO deleteFriend(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendService.deleteFriend(userInfo.getUserId(), friendId);
    }
}

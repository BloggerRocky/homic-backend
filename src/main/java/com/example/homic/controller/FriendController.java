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
     * @param session HTTP会话
     * @return 包含好友码的响应
     * @throws MyException
     */
    @RequestMapping("/getMyFriendCode")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getMyFriendCode(HttpSession session) throws MyException {
        return friendService.getMyFriendCode();
    }
}

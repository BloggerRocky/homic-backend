package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.constants.enums.VerifyRegexEnum;
import com.example.homic.dto.redis.RedisUseSpaceDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.services.AccountService;
import com.example.homic.vo.ResponseVO;
import com.wf.captcha.utils.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.NormalConstants.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/11.20:28
 * 项目名：homic
 */
@RestController("accountController")
public class AccountController {
    @Autowired
    AccountService accountService;
    //发送图片验证码
    @RequestMapping ("/checkCode")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CaptchaUtil.out(request, response);
    }
    //发送邮箱验证码
    @RequestMapping("/sendEmailCode")
    @GlobalInteceptor(checkParams = true)//需要进行参数校验
    public ResponseVO sendEmailCode(
            HttpServletRequest request,
            @VerifyParam(regex = VerifyRegexEnum.EMAIL,required = true)
            String email,
            @VerifyParam(length = CHECK_CODE_LENGTH,required = true)
            String checkCode,
            Integer type) throws MyException {
        if (!CaptchaUtil.ver(checkCode, request))
        {
            CaptchaUtil.clear(request);
            return new ResponseVO(FAIL_RES_STATUS, "验证码错误");
        }
        return accountService.sendEmailCode(email,type);
    }
    //注册用户
    @RequestMapping("/register")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO register(
            HttpServletRequest request,
            @VerifyParam(regex = VerifyRegexEnum.EMAIL,max = 150,required = true)
            String email,
            @VerifyParam(max = 20,required = true)
            String nickName,
            @VerifyParam(required = true)
            String password,
            @VerifyParam(length = CHECK_CODE_LENGTH,required = true)
            String checkCode,
            @VerifyParam(length = EMAIL_CODE_LENGTH,required = true)
            String emailCode)
    {
        if(!CaptchaUtil.ver(checkCode, request)){
            CaptchaUtil.clear(request);
            return new ResponseVO(FAIL_RES_STATUS,"验证码错误");
        }
        return accountService.register(email,nickName,password,emailCode);
    }
    //登录用户
    @RequestMapping("/login")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO login(
            HttpServletRequest request,
            @VerifyParam(regex = VerifyRegexEnum.EMAIL,required = true)
            String email,
            @VerifyParam(required = true)
            String password,
            @VerifyParam(length = CHECK_CODE_LENGTH,required = true)
            String checkCode) throws MyException {
        if(!CaptchaUtil.ver(checkCode, request)){
            CaptchaUtil.clear(request);
            return new ResponseVO(FAIL_RES_STATUS,"验证码错误");
        }
        //获取用户信息，将用户信息装填到session和responseVO中
        SessionWebUserDTO sessionWebUserDTO = accountService.login(email, password);
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_USER_INFO_KEY,sessionWebUserDTO);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "登录成功");
        responseVO.setData(sessionWebUserDTO);
        return responseVO;
    }
    //重置密码
    @RequestMapping("/resetPwd")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO resetPwd(
            HttpServletRequest request,
            @VerifyParam(regex = VerifyRegexEnum.EMAIL,required = true)
            String email,
            @VerifyParam(required = true)
            String password,
            @VerifyParam(length = EMAIL_CODE_LENGTH,required = true)
            String emailCode,
            @VerifyParam(length = CHECK_CODE_LENGTH,required = true)
            String checkCode) throws MyException {
        if(!CaptchaUtil.ver(checkCode, request)){
            CaptchaUtil.clear(request);
            return new ResponseVO(FAIL_RES_STATUS,"验证码错误");
        }
        return accountService.resetPwd(email,password,emailCode);
    }
    //获取用户头像
    @RequestMapping("/getAvatar/{userId}")
    public void getAvatar(
            HttpServletResponse response,
            @PathVariable("userId")//绑定路径参数
            String userId
            ) throws Exception {
         accountService.getAvatar(response,userId);
    }
    @RequestMapping("/getUserInfo")
    @GlobalInteceptor(checkLogin = true )
    public ResponseVO getAvatar( HttpSession session ){
        SessionWebUserDTO userInfo = (SessionWebUserDTO)session.getAttribute(SESSION_USER_INFO_KEY);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取用户信息成功");
        responseVO.setData(userInfo);
        return responseVO;
    }
    @RequestMapping("/getUseSpace")
    @GlobalInteceptor(checkLogin = true )
    public ResponseVO getUseSpace( HttpSession session){
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        RedisUseSpaceDTO redisUseSpaceDTO = accountService.getUseSpace(userInfo.getUserId());
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取空间信息成功");
        responseVO.setData(redisUseSpaceDTO);
        return responseVO;
    }
    @RequestMapping("/logout")
    public ResponseVO logout( HttpSession session){
        //无效化session
        session.invalidate();
        return new ResponseVO("success","登出成功！");
    }
    @RequestMapping("/updateUserAvatar")
    @GlobalInteceptor(checkLogin = true )
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) throws Exception {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return  accountService.updateUserAvatar(userInfo.getUserId(),avatar);
    }
    @RequestMapping("/updatePassword")
    @GlobalInteceptor(checkParams = true,checkLogin = true )
    public ResponseVO updatePassword(
            HttpSession session,
            @VerifyParam(required = true)
            String password
    )
    {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return accountService.updatePassword(password,userInfo.getUserId());
    }
}

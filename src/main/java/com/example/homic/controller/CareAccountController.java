package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.services.CareAccountService;
import com.example.homic.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import static com.example.homic.constants.NormalConstants.SESSION_USER_INFO_KEY;

/**
 * 关怀账号控制器
 */
@RestController
@RequestMapping("/careAccount")
public class CareAccountController extends CommonController {
    
    @Resource
    private CareAccountService careAccountService;
    
    /**
     * 创建关怀账号
     */
    @RequestMapping("/create")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO createCareAccount(
            HttpSession session,
            @VerifyParam(required = true, max = 50) String nickName,
            MultipartFile avatar) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return careAccountService.createCareAccount(userDTO.getUserId(), nickName, avatar);
    }
    
    /**
     * 获取关怀账号列表
     */
    @RequestMapping("/list")
    @GlobalInteceptor
    public ResponseVO getCareAccountList(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return careAccountService.getCareAccountList(userDTO.getUserId());
    }
    
    /**
     * 生成登录码
     */
    @RequestMapping("/generateLoginCode")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO generateLoginCode(
            HttpSession session,
            @VerifyParam(required = true) String careAccountId,
            @VerifyParam(required = true) Integer validType) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return careAccountService.generateLoginCode(userDTO.getUserId(), careAccountId, validType);
    }
    
    /**
     * 通过登录码登录
     */
    @RequestMapping("/loginByCode")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO loginByCode(
            HttpSession session,
            @VerifyParam(required = true) String loginCode) {
        ResponseVO result = careAccountService.loginByCode(loginCode);
        if (result.getCode() == 200 && result.getData() != null) {
            session.setAttribute(SESSION_USER_INFO_KEY, result.getData());
        }
        return result;
    }
    
    /**
     * 删除关怀账号
     */
    @RequestMapping("/delete")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO deleteCareAccount(
            HttpSession session,
            @VerifyParam(required = true) String careAccountId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return careAccountService.deleteCareAccount(userDTO.getUserId(), careAccountId);
    }
    
    /**
     * 禁用/启用关怀账号
     */
    @RequestMapping("/toggleStatus")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO toggleCareAccountStatus(
            HttpSession session,
            @VerifyParam(required = true) String careAccountId,
            @VerifyParam(required = true) Integer status) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return careAccountService.toggleCareAccountStatus(userDTO.getUserId(), careAccountId, status);
    }
}

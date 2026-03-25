package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.services.FamilyService;
import com.example.homic.vo.ResponseVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/family")
public class FamilyController extends CommonController {

    @Resource
    private FamilyService familyService;

    /**
     * 检查用户是否已加入家庭
     */
    @RequestMapping("/checkFamily")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO checkFamily(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.checkFamily(userDTO.getUserId());
    }

    /**
     * 创建家庭
     */
    @RequestMapping("/createFamily")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO createFamily(
            HttpSession session,
            @VerifyParam(required = true, max = 50) String familyName,
            @VerifyParam(max = 200) String familyDesc) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.createFamily(userDTO.getUserId(), familyName, familyDesc);
    }

    /**
     * 获取家庭信息
     */
    @RequestMapping("/getFamilyInfo")
    @GlobalInteceptor
    public ResponseVO getFamilyInfo(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.getFamilyInfo(userDTO.getUserId());
    }

    /**
     * 获取家庭成员列表
     */
    @RequestMapping("/getFamilyMembers")
    @GlobalInteceptor
    public ResponseVO getFamilyMembers(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.getFamilyMembers(userDTO.getUserId());
    }

    /**
     * 通过家庭码搜索家庭
     */
    @RequestMapping("/searchFamily")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO searchFamily(
            @VerifyParam(required = true) String familyCode) {
        return familyService.searchFamily(familyCode);
    }

    /**
     * 申请加入家庭
     */
    @RequestMapping("/applyJoinFamily")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO applyJoinFamily(
            HttpSession session,
            @VerifyParam(required = true) String familyId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.applyJoinFamily(userDTO.getUserId(), familyId);
    }

    /**
     * 获取家庭邀请列表
     */
    @RequestMapping("/getInviteList")
    @GlobalInteceptor
    public ResponseVO getInviteList(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.getInviteList(userDTO.getUserId());
    }

    /**
     * 接受家庭邀请
     */
    @RequestMapping("/acceptInvite")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO acceptInvite(
            HttpSession session,
            @VerifyParam(required = true) String inviteId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.acceptInvite(userDTO.getUserId(), inviteId);
    }

    /**
     * 拒绝家庭邀请
     */
    @RequestMapping("/rejectInvite")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO rejectInvite(
            HttpSession session,
            @VerifyParam(required = true) String inviteId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.rejectInvite(userDTO.getUserId(), inviteId);
    }

    /**
     * 生成家庭码
     */
    @RequestMapping("/generateCode")
    @GlobalInteceptor
    public ResponseVO generateCode(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.generateCode(userDTO.getUserId());
    }

    /**
     * 邀请好友加入家庭
     */
    @RequestMapping("/inviteFriends")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO inviteFriends(
            HttpSession session,
            @VerifyParam(required = true) String friendIds) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.inviteFriends(userDTO.getUserId(), friendIds);
    }

    /**
     * 离开家庭
     */
    @RequestMapping("/leaveFamily")
    @GlobalInteceptor
    public ResponseVO leaveFamily(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.leaveFamily(userDTO.getUserId());
    }

    /**
     * 更新家庭头像
     */
    @RequestMapping("/updateAvatar")
    @GlobalInteceptor
    public ResponseVO updateAvatar(
            HttpSession session,
            MultipartFile familyAvatar) throws Exception {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.updateAvatar(userDTO.getUserId(), familyAvatar);
    }

    /**
     * 获取家庭头像
     */
    @RequestMapping("/getAvatar/{familyId}")
    public void getAvatar(
            HttpServletResponse response,
            @PathVariable String familyId) throws Exception {
        familyService.getAvatar(response, familyId);
    }

    /**
     * 获取加入申请列表（仅创建者）
     */
    @RequestMapping("/getApplyList")
    @GlobalInteceptor
    public ResponseVO getApplyList(HttpSession session) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.getApplyList(userDTO.getUserId());
    }

    /**
     * 同意加入申请
     */
    @RequestMapping("/acceptApply")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO acceptApply(
            HttpSession session,
            @VerifyParam(required = true) String applyId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.acceptApply(userDTO.getUserId(), applyId);
    }

    /**
     * 拒绝加入申请
     */
    @RequestMapping("/rejectApply")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO rejectApply(
            HttpSession session,
            @VerifyParam(required = true) String applyId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.rejectApply(userDTO.getUserId(), applyId);
    }

    /**
     * 更新家庭信息
     */
    @RequestMapping("/updateFamilyInfo")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO updateFamilyInfo(
            HttpSession session,
            @VerifyParam(required = true, max = 50) String familyName,
            @VerifyParam(max = 200) String familyDesc) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.updateFamilyInfo(userDTO.getUserId(), familyName, familyDesc);
    }


    /**
     * 更新成员备注
     */
    @RequestMapping("/updateRemark")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO updateRemark(
            HttpSession session,
            @VerifyParam(max = 50) String remark) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.updateRemark(userDTO.getUserId(), remark);
    }

    /**
     * 更新成员角色
     */
    @RequestMapping("/updateMemberRole")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO updateMemberRole(
            HttpSession session,
            @VerifyParam(required = true) String userId,
            @VerifyParam(required = true) Integer role) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.updateMemberRole(userDTO.getUserId(), userId, role);
    }

    /**
     * 更新成员备注
     */
    @RequestMapping("/updateMemberRemark")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO updateMemberRemark(
            HttpSession session,
            @VerifyParam(required = true) String userId,
            @VerifyParam(max = 50) String remark) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.updateMemberRemark(userDTO.getUserId(), userId, remark);
    }

    /**
     * 移出成员
     */
    @RequestMapping("/removeMember")
    @GlobalInteceptor(checkParams = true)
    public ResponseVO removeMember(
            HttpSession session,
            @VerifyParam(required = true) String userId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.removeMember(userDTO.getUserId(), userId);
    }

    /**
     * 获取用户权限
     */
    @RequestMapping("/getUserPermissions")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getUserPermissions(
            HttpSession session,
            @VerifyParam(required = true) String familyId) {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familyService.getUserPermissions(userDTO.getUserId(), familyId);
    }
}

package com.example.homic.services;

import com.example.homic.vo.ResponseVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface FamilyService {

    /**
     * 检查用户是否已加入家庭
     */
    ResponseVO checkFamily(String userId);

    /**
     * 创建家庭
     */
    ResponseVO createFamily(String userId, String familyName, String familyDesc);

    /**
     * 获取家庭信息
     */
    ResponseVO getFamilyInfo(String userId);

    /**
     * 获取家庭成员列表
     */
    ResponseVO getFamilyMembers(String userId);

    /**
     * 通过家庭码搜索家庭
     */
    ResponseVO searchFamily(String familyCode);

    /**
     * 申请加入家庭
     */
    ResponseVO applyJoinFamily(String userId, String familyId);

    /**
     * 获取家庭邀请列表
     */
    ResponseVO getInviteList(String userId);

    /**
     * 接受家庭邀请
     */
    ResponseVO acceptInvite(String userId, String inviteId);

    /**
     * 拒绝家庭邀请
     */
    ResponseVO rejectInvite(String userId, String inviteId);

    /**
     * 生成家庭码
     */
    ResponseVO generateCode(String userId);

    /**
     * 邀请好友加入家庭
     */
    ResponseVO inviteFriends(String userId, String friendIds);

    /**
     * 离开家庭
     */
    ResponseVO leaveFamily(String userId);

    /**
     * 更新家庭头像
     */
    ResponseVO updateAvatar(String userId, MultipartFile familyAvatar) throws Exception;

    /**
     * 获取家庭头像
     */
    void getAvatar(HttpServletResponse response, String familyId) throws Exception;

    /**
     * 获取加入申请列表
     */
    ResponseVO getApplyList(String userId);

    /**
     * 同意加入申请
     */
    ResponseVO acceptApply(String userId, String applyId);

    /**
     * 拒绝加入申请
     */
    ResponseVO rejectApply(String userId, String applyId);


    /**
     * 更新家庭信息
     */
    ResponseVO updateFamilyInfo(String userId, String familyName, String familyDesc);

    /**
     * 更新成员备注
     */
    ResponseVO updateRemark(String userId, String remark);

    /**
     * 更新成员角色
     */
    ResponseVO updateMemberRole(String operatorId, String userId, Integer role);

    /**
     * 更新成员备注
     */
    ResponseVO updateMemberRemark(String operatorId, String userId, String remark);

    /**
     * 移出成员
     */
    ResponseVO removeMember(String operatorId, String userId);

    /**
     * 获取用户权限
     */
    ResponseVO getUserPermissions(String userId, String familyId);
}

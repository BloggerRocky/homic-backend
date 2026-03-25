package com.example.homic.services.implement;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.*;
import com.example.homic.model.*;
import com.example.homic.services.FamilyService;
import com.example.homic.services.PermissionService;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.homic.constants.CodeConstants.*;
import static com.example.homic.constants.NormalConstants.FAMILY_AVATAR_PATH;

@Service
public class FamilyServiceImpl implements FamilyService {

    private static final Logger logger = LoggerFactory.getLogger(FamilyServiceImpl.class);
    private static final String[] PICTURE_TYPE_ARRAY = new String[]{".png", ".PNG", ".jpg", ".JPG", ".jpeg", ".JPEG", ".gif", ".GIF", ".bmp", ".BMP"};
    private static final String FAMILY_DEFAULT_AVATAR_PATH = "file/0000-00-00/system/default_family_avatar.png";

    @Resource
    private FamilyMapper familyMapper;

    @Resource
    private FamilyMemberMapper familyMemberMapper;

    @Resource
    private FamilyInviteMapper familyInviteMapper;

    @Resource
    private FamilyApplyMapper familyApplyMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private FriendRelationMapper friendRelationMapper;

    @Resource
    private MinioUtils minioUtils;

    @Resource
    private PermissionService permissionService;

    @Override
    public ResponseVO checkFamily(String userId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("hasFamily", member != null);
        if (member != null) {
            result.put("familyId", member.getFamilyId());
            result.put("role", member.getRole());
        }

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "查询成功");
        responseVO.setData(result);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO createFamily(String userId, String familyName, String familyDesc) {
        // 检查用户是否已加入家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        if (familyMemberMapper.selectCount(memberWrapper) > 0) {
            return new ResponseVO(FAIL_RES_STATUS, "您已加入家庭，无法创建新家庭");
        }

        // 生成家庭ID和家庭码
        String familyId = StringUtils.getSerialNumber(10);
        String familyCode = generateUniqueFamilyCode();

        // 创建家庭
        Family family = new Family();
        family.setFamilyId(familyId);
        family.setFamilyName(familyName);
        family.setFamilyDesc(familyDesc);
        family.setFamilyCode(familyCode);
        family.setCreatorId(userId);
        family.setCreateTime(new Date());
        family.setUpdateTime(new Date());
        familyMapper.insert(family);

        // 创建者自动加入家庭
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        member.setRole(0);  // 0-创建者
        member.setJoinTime(new Date());
        familyMemberMapper.insert(member);

        // 初始化创建者权限（创建者拥有所有权限）
        permissionService.initDefaultPermissions(userId, familyId, 0);

        return new ResponseVO(SUCCESS_RES_STATUS, "创建家庭成功");
    }

    @Override
    public ResponseVO getFamilyInfo(String userId) {
        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 获取家庭信息
        Family family = familyMapper.selectById(member.getFamilyId());
        if (family == null) {
            return new ResponseVO(FAIL_RES_STATUS, "家庭不存在");
        }

        // 获取成员数量
        LambdaQueryWrapper<FamilyMember> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FamilyMember::getFamilyId, family.getFamilyId());
        Integer memberCount = familyMemberMapper.selectCount(countWrapper);

        FamilyVO familyVO = new FamilyVO();
        BeanUtils.copyProperties(family, familyVO);
        familyVO.setMemberCount(memberCount.intValue());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取成功");
        responseVO.setData(familyVO);
        return responseVO;
    }

    @Override
    public ResponseVO getFamilyMembers(String userId) {
        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember userMember = familyMemberMapper.selectOne(memberWrapper);

        if (userMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 获取所有成员
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, userMember.getFamilyId());
        wrapper.orderByAsc(FamilyMember::getRole);
        List<FamilyMember> members = familyMemberMapper.selectList(wrapper);

        // 组装成员信息
        List<FamilyMemberVO> memberVOList = members.stream().map(member -> {
            FamilyMemberVO vo = new FamilyMemberVO();
            vo.setUserId(member.getUserId());
            vo.setRole(member.getRole());
            vo.setRemark(member.getRemark());
            vo.setJoinTime(member.getJoinTime());

            // 获取用户信息
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(member.getUserId());
            if (userInfo != null) {
                vo.setNickName(userInfo.getNickName());
                vo.setAvatar(userInfo.getUserAvatar());
                vo.setIsDummy(userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1);
            }

            // 检查是否为好友（排除自己）
            if (!member.getUserId().equals(userId)) {
                FriendRelation relation = friendRelationMapper.selectRelation(userId, member.getUserId());
                vo.setIsFriend(relation != null && relation.getStatus() == 1);
            } else {
                vo.setIsFriend(true); // 自己默认为true
            }

            return vo;
        }).collect(Collectors.toList());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取成功");
        responseVO.setData(memberVOList);
        return responseVO;
    }

    @Override
    public ResponseVO searchFamily(String familyCode) {
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Family::getFamilyCode, familyCode);
        Family family = familyMapper.selectOne(wrapper);

        if (family == null) {
            return new ResponseVO(FAIL_RES_STATUS, "家庭码不存在");
        }

        // 获取成员数量
        LambdaQueryWrapper<FamilyMember> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FamilyMember::getFamilyId, family.getFamilyId());
        Integer memberCount = familyMemberMapper.selectCount(countWrapper);

        FamilyVO familyVO = new FamilyVO();
        BeanUtils.copyProperties(family, familyVO);
        familyVO.setMemberCount(memberCount.intValue());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "查询成功");
        responseVO.setData(familyVO);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO applyJoinFamily(String userId, String familyId) {
        // 检查用户是否已加入家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        if (familyMemberMapper.selectCount(memberWrapper) > 0) {
            return new ResponseVO(FAIL_RES_STATUS, "您已加入家庭，无法申请加入其他家庭");
        }

        // 检查是否已有待处理的申请
        LambdaQueryWrapper<FamilyApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(FamilyApply::getUserId, userId);
        applyWrapper.eq(FamilyApply::getFamilyId, familyId);
        applyWrapper.eq(FamilyApply::getStatus, 0);
        if (familyApplyMapper.selectCount(applyWrapper) > 0) {
            return new ResponseVO(FAIL_RES_STATUS, "您已提交过申请，请等待审核");
        }

        // 创建申请
        FamilyApply apply = new FamilyApply();
        apply.setApplyId(StringUtils.getSerialNumber(15));
        apply.setFamilyId(familyId);
        apply.setUserId(userId);
        apply.setStatus(0);
        apply.setCreateTime(new Date());
        apply.setUpdateTime(new Date());
        familyApplyMapper.insert(apply);

        return new ResponseVO(SUCCESS_RES_STATUS, "申请已提交");
    }

    @Override
    public ResponseVO getInviteList(String userId) {
        LambdaQueryWrapper<FamilyInvite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyInvite::getToUserId, userId);
        wrapper.eq(FamilyInvite::getStatus, 0);
        wrapper.orderByDesc(FamilyInvite::getCreateTime);
        List<FamilyInvite> invites = familyInviteMapper.selectList(wrapper);

        List<FamilyInviteVO> inviteVOList = invites.stream().map(invite -> {
            FamilyInviteVO vo = new FamilyInviteVO();
            vo.setInviteId(invite.getInviteId());
            vo.setFamilyId(invite.getFamilyId());
            vo.setFromUserId(invite.getFromUserId());
            vo.setStatus(invite.getStatus());
            vo.setCreateTime(invite.getCreateTime());

            // 获取家庭信息
            Family family = familyMapper.selectById(invite.getFamilyId());
            if (family != null) {
                vo.setFamilyName(family.getFamilyName());
            }

            // 获取邀请人信息
            UserInfo fromUser = userInfoMapper.selectByPrimaryKey(invite.getFromUserId());
            if (fromUser != null) {
                vo.setFromUserName(fromUser.getNickName());
                vo.setFromUserAvatar(fromUser.getUserAvatar());
            }

            return vo;
        }).collect(Collectors.toList());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取成功");
        responseVO.setData(inviteVOList);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO acceptInvite(String userId, String inviteId) {
        // 获取邀请信息
        FamilyInvite invite = familyInviteMapper.selectById(inviteId);
        if (invite == null || !invite.getToUserId().equals(userId)) {
            return new ResponseVO(FAIL_RES_STATUS, "邀请不存在");
        }

        if (invite.getStatus() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "邀请已处理");
        }

        // 检查用户是否已加入家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        if (familyMemberMapper.selectCount(memberWrapper) > 0) {
            return new ResponseVO(FAIL_RES_STATUS, "您已加入家庭");
        }

        // 加入家庭
        FamilyMember member = new FamilyMember();
        member.setFamilyId(invite.getFamilyId());
        member.setUserId(userId);
        member.setRole(2);  // 2-成员
        member.setJoinTime(new Date());
        familyMemberMapper.insert(member);

        // 初始化成员权限（普通成员默认无权限）
        permissionService.initDefaultPermissions(userId, invite.getFamilyId(), 2);

        // 更新邀请状态
        invite.setStatus(1);
        invite.setUpdateTime(new Date());
        familyInviteMapper.updateById(invite);

        return new ResponseVO(SUCCESS_RES_STATUS, "已加入家庭");
    }

    @Override
    @Transactional
    public ResponseVO rejectInvite(String userId, String inviteId) {
        FamilyInvite invite = familyInviteMapper.selectById(inviteId);
        if (invite == null || !invite.getToUserId().equals(userId)) {
            return new ResponseVO(FAIL_RES_STATUS, "邀请不存在");
        }

        if (invite.getStatus() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "邀请已处理");
        }

        invite.setStatus(2);
        invite.setUpdateTime(new Date());
        familyInviteMapper.updateById(invite);

        return new ResponseVO(SUCCESS_RES_STATUS, "已拒绝邀请");
    }

    @Override
    public ResponseVO generateCode(String userId) {
        // 检查是否为关怀账号
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo != null && userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不支持此操作");
        }

        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        Family family = familyMapper.selectById(member.getFamilyId());
        if (family == null) {
            return new ResponseVO(FAIL_RES_STATUS, "家庭不存在");
        }

        Map<String, String> result = new HashMap<>();
        result.put("familyCode", family.getFamilyCode());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取成功");
        responseVO.setData(result);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO inviteFriends(String userId, String friendIds) {
        // 检查是否为关怀账号
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo != null && userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不支持此操作");
        }

        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        String[] friendIdArray = friendIds.split(",");
        for (String friendId : friendIdArray) {
            // 检查好友是否已加入家庭
            LambdaQueryWrapper<FamilyMember> friendMemberWrapper = new LambdaQueryWrapper<>();
            friendMemberWrapper.eq(FamilyMember::getUserId, friendId);
            if (familyMemberMapper.selectCount(friendMemberWrapper) > 0) {
                continue;  // 已加入家庭，跳过
            }

            // 检查是否已有待处理的邀请
            LambdaQueryWrapper<FamilyInvite> inviteWrapper = new LambdaQueryWrapper<>();
            inviteWrapper.eq(FamilyInvite::getFamilyId, member.getFamilyId());
            inviteWrapper.eq(FamilyInvite::getToUserId, friendId);
            inviteWrapper.eq(FamilyInvite::getStatus, 0);
            if (familyInviteMapper.selectCount(inviteWrapper) > 0) {
                continue;  // 已有待处理邀请，跳过
            }

            // 创建邀请
            FamilyInvite invite = new FamilyInvite();
            invite.setInviteId(StringUtils.getSerialNumber(15));
            invite.setFamilyId(member.getFamilyId());
            invite.setFromUserId(userId);
            invite.setToUserId(friendId);
            invite.setStatus(0);
            invite.setCreateTime(new Date());
            invite.setUpdateTime(new Date());
            familyInviteMapper.insert(invite);
        }

        return new ResponseVO(SUCCESS_RES_STATUS, "邀请已发送");
    }

    @Override
    @Transactional
    public ResponseVO leaveFamily(String userId) {
        // 检查是否为关怀账号
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo != null && userInfo.getIsDummy() != null && userInfo.getIsDummy() == 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不支持此操作");
        }

        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 如果是创建者，需要特殊处理（这里简单处理，后续可以添加转让或解散逻辑）
        if (member.getRole() == 0) {
            // 检查是否还有其他成员
            LambdaQueryWrapper<FamilyMember> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(FamilyMember::getFamilyId, member.getFamilyId());
            Integer memberCount = familyMemberMapper.selectCount(countWrapper);

            if (memberCount > 1) {
                return new ResponseVO(FAIL_RES_STATUS, "创建者离开前需要转让家庭或解散家庭");
            }

            // 如果只有创建者一人，删除家庭
            familyMapper.deleteById(member.getFamilyId());
        }

        // 删除成员记录
        familyMemberMapper.deleteById(member.getId());

        return new ResponseVO(SUCCESS_RES_STATUS, "已离开家庭");
    }

    @Override
    public ResponseVO updateAvatar(String userId, MultipartFile familyAvatar) throws Exception {
        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 检查权限（只有创建者和管理员可以修改）
        if (member.getRole() > 1) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限修改家庭头像");
        }

        // 获取家庭信息
        Family family = familyMapper.selectById(member.getFamilyId());
        if (family == null) {
            return new ResponseVO(FAIL_RES_STATUS, "家庭不存在");
        }

        // 验证文件类型
        String fileType = familyAvatar.getOriginalFilename().substring(familyAvatar.getOriginalFilename().lastIndexOf("."));
        if (!ArrayUtils.contains(PICTURE_TYPE_ARRAY, fileType)) {
            throw new MyException("图片格式不支持", FAIL_RES_CODE);
        }

        // 生成头像文件名：family + familyId + 文件扩展名
        String avatarName = family.getFamilyId() + fileType;

        try {
            minioUtils.saveMultipartFile(FAMILY_AVATAR_PATH + avatarName, familyAvatar);
        } catch (MyException e) {
            logger.error(e.msg);
            throw new MyException("头像上传失败", FAIL_RES_CODE);
        }

        // 更新家庭头像
        family.setFamilyAvatar(avatarName);
        family.setUpdateTime(new Date());
        familyMapper.updateById(family);

        return new ResponseVO(SUCCESS_RES_STATUS, "头像更新成功");
    }

    @Override
    public void getAvatar(HttpServletResponse response, String familyId) throws Exception {

        // 查询家庭头像文件名称
        Family family = familyMapper.selectById(familyId);
        String avatarName = null;
        if (family != null) {
            avatarName = family.getFamilyAvatar();
        }

        // 拼接文件路径
        if (StrUtil.isBlank(avatarName)) {
            // 使用默认家庭头像
            minioUtils.getFile(FAMILY_DEFAULT_AVATAR_PATH, response);
        } else {
            String filePath = FAMILY_AVATAR_PATH + avatarName;
            if (!minioUtils.checkExist(filePath)) {
                // 文件不存在，使用默认头像
                minioUtils.getFile(FAMILY_DEFAULT_AVATAR_PATH, response);
            } else {
                minioUtils.getFile(filePath, response);
            }
        }
    }

    @Override
    public ResponseVO getApplyList(String userId) {
        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 检查权限（只有创建者可以查看）
        if (member.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限查看申请列表");
        }

        // 获取所有申请（包括已处理的）
        LambdaQueryWrapper<FamilyApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(FamilyApply::getFamilyId, member.getFamilyId());
        // 移除状态过滤，返回所有申请
        applyWrapper.orderByDesc(FamilyApply::getCreateTime);
        List<FamilyApply> applies = familyApplyMapper.selectList(applyWrapper);

        // 组装申请信息
        List<FamilyApplyVO> applyVOList = applies.stream().map(apply -> {
            FamilyApplyVO vo = new FamilyApplyVO();
            vo.setApplyId(apply.getApplyId());
            vo.setFamilyId(apply.getFamilyId());
            vo.setUserId(apply.getUserId());
            vo.setStatus(apply.getStatus());
            vo.setCreateTime(apply.getCreateTime());

            // 获取申请人信息
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(apply.getUserId());
            if (userInfo != null) {
                vo.setNickName(userInfo.getNickName());
                vo.setAvatar(userInfo.getUserAvatar());
            }

            return vo;
        }).collect(Collectors.toList());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取成功");
        responseVO.setData(applyVOList);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO acceptApply(String userId, String applyId) {
        // 获取申请信息
        FamilyApply apply = familyApplyMapper.selectById(applyId);
        if (apply == null) {
            return new ResponseVO(FAIL_RES_STATUS, "申请不存在");
        }

        if (apply.getStatus() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "申请已处理");
        }

        // 验证权限
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        memberWrapper.eq(FamilyMember::getFamilyId, apply.getFamilyId());
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null || member.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限处理申请");
        }

        // 检查申请人是否已加入家庭
        LambdaQueryWrapper<FamilyMember> applicantWrapper = new LambdaQueryWrapper<>();
        applicantWrapper.eq(FamilyMember::getUserId, apply.getUserId());
        if (familyMemberMapper.selectCount(applicantWrapper) > 0) {
            return new ResponseVO(FAIL_RES_STATUS, "该用户已加入其他家庭");
        }

        // 添加成员
        FamilyMember newMember = new FamilyMember();
        newMember.setFamilyId(apply.getFamilyId());
        newMember.setUserId(apply.getUserId());
        newMember.setRole(2);  // 2-成员
        newMember.setJoinTime(new Date());
        familyMemberMapper.insert(newMember);

        // 初始化成员权限（普通成员默认无权限）
        permissionService.initDefaultPermissions(apply.getUserId(), apply.getFamilyId(), 2);

        // 更新申请状态
        apply.setStatus(1);
        apply.setUpdateTime(new Date());
        familyApplyMapper.updateById(apply);

        return new ResponseVO(SUCCESS_RES_STATUS, "已同意申请");
    }

    @Override
    @Transactional
    public ResponseVO rejectApply(String userId, String applyId) {
        // 获取申请信息
        FamilyApply apply = familyApplyMapper.selectById(applyId);
        if (apply == null) {
            return new ResponseVO(FAIL_RES_STATUS, "申请不存在");
        }

        if (apply.getStatus() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "申请已处理");
        }

        // 验证权限
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        memberWrapper.eq(FamilyMember::getFamilyId, apply.getFamilyId());
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null || member.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限处理申请");
        }

        // 更新申请状态
        apply.setStatus(2);
        apply.setUpdateTime(new Date());
        familyApplyMapper.updateById(apply);

        return new ResponseVO(SUCCESS_RES_STATUS, "已拒绝申请");
    }

    /**
     * 生成唯一的家庭码
     */
    private String generateUniqueFamilyCode() {
        String code;
        LambdaQueryWrapper<Family> wrapper = new LambdaQueryWrapper<>();
        do {
            code = StringUtils.getSerialNumber(10);
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Family::getFamilyCode, code);
        } while (familyMapper.selectCount(wrapper) > 0);
        return code;
    }


    @Override
    @Transactional
    public ResponseVO updateFamilyInfo(String userId, String familyName, String familyDesc) {
        // 获取用户所在家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 检查权限（只有创建者和管理员可以修改）
        if (member.getRole() > 1) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限修改家庭信息");
        }

        // 获取家庭信息
        Family family = familyMapper.selectById(member.getFamilyId());
        if (family == null) {
            return new ResponseVO(FAIL_RES_STATUS, "家庭不存在");
        }

        // 验证家庭名称
        if (StrUtil.isBlank(familyName)) {
            return new ResponseVO(FAIL_RES_STATUS, "家庭名称不能为空");
        }

        // 更新家庭信息
        family.setFamilyName(familyName);
        family.setFamilyDesc(familyDesc);
        family.setUpdateTime(new Date());
        familyMapper.updateById(family);

        return new ResponseVO(SUCCESS_RES_STATUS, "家庭信息更新成功");
    }

    @Override
    @Transactional
    public ResponseVO updateRemark(String userId, String remark) {
        // 获取用户所在家庭成员记录
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 更新备注
        member.setRemark(remark);
        familyMemberMapper.updateById(member);

        return new ResponseVO(SUCCESS_RES_STATUS, "昵称更新成功");
    }


    @Override
    @Transactional
    public ResponseVO updateMemberRole(String operatorId, String userId, Integer role) {
        // 获取操作者所在家庭
        LambdaQueryWrapper<FamilyMember> operatorWrapper = new LambdaQueryWrapper<>();
        operatorWrapper.eq(FamilyMember::getUserId, operatorId);
        FamilyMember operator = familyMemberMapper.selectOne(operatorWrapper);

        if (operator == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 获取目标成员
        LambdaQueryWrapper<FamilyMember> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(FamilyMember::getUserId, userId);
        targetWrapper.eq(FamilyMember::getFamilyId, operator.getFamilyId());
        FamilyMember targetMember = familyMemberMapper.selectOne(targetWrapper);

        if (targetMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "目标成员不存在");
        }

        // 检查目标成员是否为关怀账号
        UserInfo targetUserInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (targetUserInfo != null && targetUserInfo.getIsDummy() != null && targetUserInfo.getIsDummy() == 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不能被设置为管理员");
        }

        // 不能修改自己的权限
        if (operatorId.equals(userId)) {
            return new ResponseVO(FAIL_RES_STATUS, "不能修改自己的权限");
        }

        // 不能修改创建者的权限
        if (targetMember.getRole() == 0) {
            return new ResponseVO(FAIL_RES_STATUS, "不能修改创建者的权限");
        }

        // 检查权限
        if (operator.getRole() == 0) {
            // 创建者可以修改所有人的权限（除了自己）
        } else if (operator.getRole() == 1) {
            // 管理员只能修改普通成员的权限
            if (targetMember.getRole() != 2) {
                return new ResponseVO(FAIL_RES_STATUS, "无权限修改该成员的角色");
            }
        } else {
            return new ResponseVO(FAIL_RES_STATUS, "无权限修改成员角色");
        }

        // 验证角色值
        if (role < 1 || role > 2) {
            return new ResponseVO(FAIL_RES_STATUS, "角色值无效");
        }

        // 更新角色
        targetMember.setRole(role);
        familyMemberMapper.updateById(targetMember);

        return new ResponseVO(SUCCESS_RES_STATUS, "权限更新成功");
    }

    @Override
    @Transactional
    public ResponseVO updateMemberRemark(String operatorId, String userId, String remark) {
        // 获取操作者所在家庭
        LambdaQueryWrapper<FamilyMember> operatorWrapper = new LambdaQueryWrapper<>();
        operatorWrapper.eq(FamilyMember::getUserId, operatorId);
        FamilyMember operator = familyMemberMapper.selectOne(operatorWrapper);

        if (operator == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 检查权限（只有创建者和管理员可以修改成员备注）
        if (operator.getRole() > 1) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限修改成员备注");
        }

        // 获取目标成员
        LambdaQueryWrapper<FamilyMember> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(FamilyMember::getUserId, userId);
        targetWrapper.eq(FamilyMember::getFamilyId, operator.getFamilyId());
        FamilyMember targetMember = familyMemberMapper.selectOne(targetWrapper);

        if (targetMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "目标成员不存在");
        }

        // 管理员只能修改普通成员的备注
        if (operator.getRole() == 1 && targetMember.getRole() != 2) {
            return new ResponseVO(FAIL_RES_STATUS, "无权限修改该成员的备注");
        }

        // 更新备注
        targetMember.setRemark(remark);
        familyMemberMapper.updateById(targetMember);

        return new ResponseVO(SUCCESS_RES_STATUS, "备注更新成功");
    }

    @Override
    @Transactional
    public ResponseVO removeMember(String operatorId, String userId) {
        // 获取操作者所在家庭
        LambdaQueryWrapper<FamilyMember> operatorWrapper = new LambdaQueryWrapper<>();
        operatorWrapper.eq(FamilyMember::getUserId, operatorId);
        FamilyMember operator = familyMemberMapper.selectOne(operatorWrapper);

        if (operator == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        // 获取目标成员
        LambdaQueryWrapper<FamilyMember> targetWrapper = new LambdaQueryWrapper<>();
        targetWrapper.eq(FamilyMember::getUserId, userId);
        targetWrapper.eq(FamilyMember::getFamilyId, operator.getFamilyId());
        FamilyMember targetMember = familyMemberMapper.selectOne(targetWrapper);

        if (targetMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "目标成员不存在");
        }

        // 不能移出自己
        if (operatorId.equals(userId)) {
            return new ResponseVO(FAIL_RES_STATUS, "不能移出自己");
        }

        // 不能移出创建者
        if (targetMember.getRole() == 0) {
            return new ResponseVO(FAIL_RES_STATUS, "不能移出创建者");
        }

        // 检查权限
        if (operator.getRole() == 0) {
            // 创建者可以移出所有人（除了自己）
        } else if (operator.getRole() == 1) {
            // 管理员只能移出普通成员
            if (targetMember.getRole() != 2) {
                return new ResponseVO(FAIL_RES_STATUS, "无权限移出该成员");
            }
        } else {
            return new ResponseVO(FAIL_RES_STATUS, "无权限移出成员");
        }

        // 移出成员
        familyMemberMapper.deleteById(targetMember.getId());

        return new ResponseVO(SUCCESS_RES_STATUS, "已移出成员");
    }

    @Override
    public ResponseVO getUserPermissions(String userId, String familyId) {
        try {
            // 校验用户是否属于该家庭
            validateFamilyMember(userId, familyId);

            // 获取用户在家庭中的角色
            FamilyMember member = validateFamilyMember(userId, familyId);
            Integer userRole = member.getRole();

            // 获取家庭信息，检查是否为创建者
            LambdaQueryWrapper<Family> familyLqw = new LambdaQueryWrapper<>();
            familyLqw.eq(Family::getFamilyId, familyId);
            Family family = familyMapper.selectOne(familyLqw);
            boolean isCreator = family != null && family.getCreatorId().equals(userId);

            // 构建权限对象
            java.util.Map<String, Object> permissions = new java.util.HashMap<>();

            // 关怀用户可见性管理权限
            if (isCreator) {
                // 创建者硬编码拥有权限
                permissions.put("CARE_VISIBILITY_MANAGE", 1);
            } else if (userRole == 1) {
                // 管理员需要检查权限
                boolean hasPermission = permissionService.hasPermission(userId, permissionService.PERMISSION_CARE_VISIBILITY_MANAGE, familyId);
                permissions.put("CARE_VISIBILITY_MANAGE", hasPermission ? 1 : 0);
            } else {
                // 普通用户无权限
                permissions.put("CARE_VISIBILITY_MANAGE", 0);
            }

            ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取权限成功");
            responseVO.setData(permissions);
            return responseVO;

        } catch (Exception e) {
            logger.error("获取用户权限失败", e);
            return new ResponseVO(FAIL_RES_STATUS, "获取权限失败");
        }
    }
    private FamilyMember validateFamilyMember(String userId, String familyId) throws MyException {
        if (familyId == null || familyId.isEmpty()) {
            throw new MyException("家庭ID不能为空", FAIL_RES_CODE);
        }
        LambdaQueryWrapper<FamilyMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FamilyMember::getUserId, userId);
        lqw.eq(FamilyMember::getFamilyId, familyId);
        FamilyMember member = familyMemberMapper.selectOne(lqw);
        if (member == null) {
            throw new MyException("您不属于该家庭", FAIL_RES_CODE);
        }
        return member;
    }
}

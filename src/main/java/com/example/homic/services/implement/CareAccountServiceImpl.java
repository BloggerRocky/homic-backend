package com.example.homic.services.implement;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.mapper.*;
import com.example.homic.model.*;
import com.example.homic.services.CareAccountService;
import com.example.homic.utils.MinioUtils;
import com.example.homic.utils.StringUtils;
import com.example.homic.vo.CareAccountVO;
import com.example.homic.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.NormalConstants.FILE_AVATAR_PATH;
import static com.example.homic.constants.NormalConstants.PICTURE_TYPE_ARRAY;

/**
 * 关怀账号服务实现
 */
@Slf4j
@Service
public class CareAccountServiceImpl implements CareAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private FamilyMapper familyMapper;

    @Resource
    private FamilyMemberMapper familyMemberMapper;

    @Resource
    private CareAccountLoginCodeMapper loginCodeMapper;

    @Resource
    private MinioUtils minioUtils;

    private static Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    // 关怀账号空间限制：100MB
    private static final Long CARE_ACCOUNT_SPACE = 100 * 1024 * 1024L;

    // 每个家庭最多5个关怀账号
    private static final int MAX_CARE_ACCOUNTS = 5;

    @Override
    @Transactional
    public ResponseVO createCareAccount(String creatorId, String nickName, MultipartFile avatar) {
        // 验证创建者是否是家庭创建者
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, creatorId);
        FamilyMember creatorMember = familyMemberMapper.selectOne(memberWrapper);

        if (creatorMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        if (creatorMember.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "只有家庭创建者可以创建关怀账号");
        }

        // 检查该家庭已有的关怀账号数量
        LambdaQueryWrapper<FamilyMember> careAccountWrapper = new LambdaQueryWrapper<>();
        careAccountWrapper.eq(FamilyMember::getFamilyId, creatorMember.getFamilyId());
        List<FamilyMember> allMembers = familyMemberMapper.selectList(careAccountWrapper);

        long careAccountCount = allMembers.stream()
            .map(FamilyMember::getUserId)
            .map(userInfoMapper::selectByPrimaryKey)
            .filter(user -> user != null && user.getIsDummy() == 1)
            .count();

        if (careAccountCount >= MAX_CARE_ACCOUNTS) {
            return new ResponseVO(FAIL_RES_STATUS, "每个家庭最多只能创建" + MAX_CARE_ACCOUNTS + "个关怀账号");
        }

        // 验证昵称
        if (StrUtil.isBlank(nickName)) {
            return new ResponseVO(FAIL_RES_STATUS, "昵称不能为空");
        }

        // 生成用户ID
        String userId = StringUtils.getRandomNumber(10);
        while (userInfoMapper.selectByPrimaryKey(userId) != null) {
            userId = StringUtils.getRandomNumber(10);
        }

        // 创建关怀账号
        UserInfo careAccount = new UserInfo();
        careAccount.setUserId(userId);
        careAccount.setNickName(nickName);
        // 生成随机邮箱字符串以规避唯一主键问题
        String randomEmail = "care_" + userId + "_" + RandomUtil.randomString(8) + "@dummy.local";
        careAccount.setEmail(randomEmail);
        careAccount.setPassword(""); // 关怀账号无密码
        careAccount.setJoinTime(new Date());
        careAccount.setLastLoginTime(new Date());
        careAccount.setStatus(1); // 启用状态
        careAccount.setUseSpace(0L);
        careAccount.setTotalSpace(CARE_ACCOUNT_SPACE); // 100MB
        careAccount.setIsDummy(1); // 标记为关怀账号

        // 处理头像上传
        String avatarName = null;
        if (avatar != null && !avatar.isEmpty()) {
            // 验证文件类型
            String fileType = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf("."));
            if (!ArrayUtils.contains(PICTURE_TYPE_ARRAY, fileType)) {
                return new ResponseVO(FAIL_RES_STATUS, "头像格式不支持");
            }

            // 生成头像文件名
            avatarName = userId + fileType;
            try {
                minioUtils.saveMultipartFile(FILE_AVATAR_PATH + avatarName, avatar);
                careAccount.setUserAvatar(avatarName);
            } catch (MyException e) {
                logger.error("头像上传失败", e);
                return new ResponseVO(FAIL_RES_STATUS, "头像上传失败");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        userInfoMapper.insert(careAccount);

        // 自动加入家庭
        FamilyMember newMember = new FamilyMember();
        newMember.setFamilyId(creatorMember.getFamilyId());
        newMember.setUserId(userId);
        newMember.setRole(2); // 普通成员
        newMember.setJoinTime(new Date());
        familyMemberMapper.insert(newMember);

        return new ResponseVO(SUCCESS_RES_STATUS, "关怀账号创建成功");
    }

    @Override
    public ResponseVO getCareAccountList(String userId) {
        // 验证用户是否是家庭创建者
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        FamilyMember member = familyMemberMapper.selectOne(memberWrapper);

        if (member == null) {
            return new ResponseVO(FAIL_RES_STATUS, "您还未加入家庭");
        }

        if (member.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "只有家庭创建者可以查看关怀账号");
        }

        // 获取家庭所有成员
        LambdaQueryWrapper<FamilyMember> allMembersWrapper = new LambdaQueryWrapper<>();
        allMembersWrapper.eq(FamilyMember::getFamilyId, member.getFamilyId());
        List<FamilyMember> allMembers = familyMemberMapper.selectList(allMembersWrapper);

        // 筛选出关怀账号并组装VO
        List<CareAccountVO> careAccounts = allMembers.stream()
            .map(fm -> {
                UserInfo user = userInfoMapper.selectByPrimaryKey(fm.getUserId());
                if (user != null && user.getIsDummy() == 1) {
                    CareAccountVO vo = new CareAccountVO();
                    vo.setUserId(user.getUserId());
                    vo.setNickName(user.getNickName());
                    vo.setAvatar(user.getUserAvatar());
                    vo.setUseSpace(user.getUseSpace());
                    vo.setTotalSpace(user.getTotalSpace());
                    vo.setCreateTime(user.getJoinTime());
                    vo.setStatus(user.getStatus());

                    // 查询是否有有效的登录码
                    CareAccountLoginCode loginCode = loginCodeMapper.selectValidByUserId(user.getUserId());
                    if (loginCode != null) {
                        vo.setLoginCode(loginCode.getLoginCode());
                        vo.setLoginCodeExpireTime(loginCode.getExpireTime());
                    }

                    return vo;
                }
                return null;
            })
            .filter(vo -> vo != null)
            .collect(Collectors.toList());

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取成功");
        responseVO.setData(careAccounts);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO generateLoginCode(String creatorId, String careAccountId, Integer validType) {
        // 验证创建者权限
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, creatorId);
        FamilyMember creatorMember = familyMemberMapper.selectOne(memberWrapper);

        if (creatorMember == null || creatorMember.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "只有家庭创建者可以生成登录码");
        }

        // 验证关怀账号
        UserInfo careAccount = userInfoMapper.selectByPrimaryKey(careAccountId);
        if (careAccount == null || careAccount.getIsDummy() != 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不存在");
        }

        // 验证关怀账号是否在同一家庭
        LambdaQueryWrapper<FamilyMember> careAccountMemberWrapper = new LambdaQueryWrapper<>();
        careAccountMemberWrapper.eq(FamilyMember::getUserId, careAccountId);
        careAccountMemberWrapper.eq(FamilyMember::getFamilyId, creatorMember.getFamilyId());
        FamilyMember careAccountMember = familyMemberMapper.selectOne(careAccountMemberWrapper);

        if (careAccountMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "该关怀账号不在您的家庭中");
        }

        // 生成8位登录码
        String loginCode = RandomUtil.randomString(8).toUpperCase();

        // 确保登录码唯一
        while (loginCodeMapper.selectByLoginCode(loginCode) != null) {
            loginCode = RandomUtil.randomString(8).toUpperCase();
        }

        // 根据validType设置过期时间
        // 0: 8小时, 1: 16小时, 2: 1天, 3: 3天, 4: 7天
        Calendar calendar = Calendar.getInstance();
        switch (validType) {
            case 0:
                calendar.add(Calendar.HOUR_OF_DAY, 8);
                break;
            case 1:
                calendar.add(Calendar.HOUR_OF_DAY, 16);
                break;
            case 2:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case 3:
                calendar.add(Calendar.DAY_OF_MONTH, 3);
                break;
            case 4:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            default:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
        }
        Date expireTime = calendar.getTime();

        // 保存登录码
        CareAccountLoginCode loginCodeEntity = new CareAccountLoginCode();
        loginCodeEntity.setUserId(careAccountId);
        loginCodeEntity.setLoginCode(loginCode);
        loginCodeEntity.setExpireTime(expireTime);
        loginCodeEntity.setCreateTime(new Date());
        loginCodeMapper.insert(loginCodeEntity);

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "登录码生成成功");
        responseVO.setData(loginCode);
        return responseVO;
    }

    @Override
    public ResponseVO loginByCode(String loginCode) {
        if (StrUtil.isBlank(loginCode)) {
            return new ResponseVO(FAIL_RES_STATUS, "登录码不能为空");
        }

        // 查询登录码
        CareAccountLoginCode codeEntity = loginCodeMapper.selectByLoginCode(loginCode);
        if (codeEntity == null) {
            return new ResponseVO(FAIL_RES_STATUS, "登录码无效或已过期");
        }

        // 获取用户信息
        UserInfo user = userInfoMapper.selectByPrimaryKey(codeEntity.getUserId());
        if (user == null || user.getIsDummy() != 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不存在");
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            return new ResponseVO(FAIL_RES_STATUS, "该关怀账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(new Date());
        userInfoMapper.updateById(user);

        // 构建session信息
        SessionWebUserDTO sessionUser = new SessionWebUserDTO();
        sessionUser.setUserId(user.getUserId());
        sessionUser.setNickName(user.getNickName());
        sessionUser.setUserAvatar(user.getUserAvatar());
        sessionUser.setAdmin(false);
        sessionUser.setIsDummy(true); // 标记为关怀账号

        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "登录成功");
        responseVO.setData(sessionUser);
        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO deleteCareAccount(String creatorId, String careAccountId) {
        // 验证创建者权限
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, creatorId);
        FamilyMember creatorMember = familyMemberMapper.selectOne(memberWrapper);

        if (creatorMember == null || creatorMember.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "只有家庭创建者可以删除关怀账号");
        }

        // 验证关怀账号
        UserInfo careAccount = userInfoMapper.selectByPrimaryKey(careAccountId);
        if (careAccount == null || careAccount.getIsDummy() != 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不存在");
        }

        // 删除用户（会级联删除相关数据）
        userInfoMapper.deleteById(careAccountId);

        // 删除家庭成员记录
        LambdaQueryWrapper<FamilyMember> deleteMemberWrapper = new LambdaQueryWrapper<>();
        deleteMemberWrapper.eq(FamilyMember::getUserId, careAccountId);
        familyMemberMapper.delete(deleteMemberWrapper);

        // 删除所有登录码
        LambdaQueryWrapper<CareAccountLoginCode> deleteCodeWrapper = new LambdaQueryWrapper<>();
        deleteCodeWrapper.eq(CareAccountLoginCode::getUserId, careAccountId);
        loginCodeMapper.delete(deleteCodeWrapper);

        return new ResponseVO(SUCCESS_RES_STATUS, "关怀账号删除成功");
    }

    @Override
    @Transactional
    public ResponseVO toggleCareAccountStatus(String creatorId, String careAccountId, Integer status) {
        // 验证创建者权限
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, creatorId);
        FamilyMember creatorMember = familyMemberMapper.selectOne(memberWrapper);

        if (creatorMember == null || creatorMember.getRole() != 0) {
            return new ResponseVO(FAIL_RES_STATUS, "只有家庭创建者可以禁用/启用关怀账号");
        }

        // 验证关怀账号
        UserInfo careAccount = userInfoMapper.selectByPrimaryKey(careAccountId);
        if (careAccount == null || careAccount.getIsDummy() != 1) {
            return new ResponseVO(FAIL_RES_STATUS, "关怀账号不存在");
        }

        // 验证关怀账号是否在同一家庭
        LambdaQueryWrapper<FamilyMember> careAccountMemberWrapper = new LambdaQueryWrapper<>();
        careAccountMemberWrapper.eq(FamilyMember::getUserId, careAccountId);
        careAccountMemberWrapper.eq(FamilyMember::getFamilyId, creatorMember.getFamilyId());
        FamilyMember careAccountMember = familyMemberMapper.selectOne(careAccountMemberWrapper);

        if (careAccountMember == null) {
            return new ResponseVO(FAIL_RES_STATUS, "该关怀账号不在您的家庭中");
        }

        // 验证状态值
        if (status != 0 && status != 1) {
            return new ResponseVO(FAIL_RES_STATUS, "状态值无效");
        }

        // 更新状态
        careAccount.setStatus(status);
        userInfoMapper.updateById(careAccount);

        String message = status == 1 ? "关怀账号已启用" : "关怀账号已禁用";
        return new ResponseVO(SUCCESS_RES_STATUS, message);
    }
}

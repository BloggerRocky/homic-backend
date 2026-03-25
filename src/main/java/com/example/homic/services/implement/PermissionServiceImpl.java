package com.example.homic.services.implement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.mapper.FamilyMapper;
import com.example.homic.mapper.FamilyMemberMapper;
import com.example.homic.mapper.PermissionMapper;
import com.example.homic.model.Permission;
import com.example.homic.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public boolean hasPermission(String userId, String permissionKey, String objectId) {
        // 如果是创建者，强制拥有所有权限
        Permission permission = permissionMapper.selectByUserKeyAndObject(userId, permissionKey, objectId);
        if (permission == null) {
            return false;
        }
        return permission.getPermissionValue() == 1;
    }

    @Override
    public List<Permission> getUserPermissions(String userId, String objectId) {
        return permissionMapper.selectByUserIdAndObjectId(userId, objectId);
    }

    @Override
    @Transactional
    public boolean setPermission(String userId, String permissionKey, int permissionValue, String objectId) {
        // 先查询是否已存在
        Permission existing = permissionMapper.selectByUserKeyAndObject(userId, permissionKey, objectId);

        if (existing != null) {
            // 更新
            existing.setPermissionValue(permissionValue);
            existing.setUpdateTime(new Date());
            permissionMapper.updateById(existing);
        } else {
            // 插入
            Permission permission = new Permission();
            permission.setUserId(userId);
            permission.setPermissionKey(permissionKey);
            permission.setPermissionValue(permissionValue);
            permission.setObjectId(objectId);
            permission.setCreateTime(new Date());
            permission.setUpdateTime(new Date());
            permissionMapper.insert(permission);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchSetPermissions(String userId, Map<String, Integer> permissions, String objectId) {
        for (Map.Entry<String, Integer> entry : permissions.entrySet()) {
            setPermission(userId, entry.getKey(), entry.getValue(), objectId);
        }
        return true;
    }

    @Override
    @Transactional
    public void initDefaultPermissions(String userId, String objectId, int role) {
        Date now = new Date();

        // 创建者强制拥有所有权限
        if (role == 0) {
            setPermission(userId, PERMISSION_UPLOAD, 1, objectId);
            setPermission(userId, PERMISSION_MODIFY, 1, objectId);
            setPermission(userId, PERMISSION_DELETE, 1, objectId);
        } else if (role == 1) {
            // 管理员默认拥有三种权限
            setPermission(userId, PERMISSION_UPLOAD, 1, objectId);
            setPermission(userId, PERMISSION_MODIFY, 1, objectId);
            setPermission(userId, PERMISSION_DELETE, 1, objectId);
        } else {
            // 普通成员默认无权限
            setPermission(userId, PERMISSION_UPLOAD, 0, objectId);
            setPermission(userId, PERMISSION_MODIFY, 0, objectId);
            setPermission(userId, PERMISSION_DELETE, 0, objectId);
        }
    }

    @Override
    public boolean canView(String userId, String familyId) {
        // 所有家庭成员都可以查看，通过FamilyMemberMapper验证
        LambdaQueryWrapper<com.example.homic.model.FamilyMember> memberLqw = new LambdaQueryWrapper<>();
        memberLqw.eq(com.example.homic.model.FamilyMember::getFamilyId, familyId);
        memberLqw.eq(com.example.homic.model.FamilyMember::getUserId, userId);
        return familyMemberMapper.selectCount(memberLqw) > 0;
    }

    @Override
    public boolean canUpload(String userId, String familyId) {
        // 创建者硬编码拥有权限
        if (isCreator(userId, familyId)) {
            return true;
        }
        return hasPermission(userId, PERMISSION_UPLOAD, familyId);
    }

    @Override
    public boolean canModify(String userId, String familyId) {
        // 创建者硬编码拥有权限
        if (isCreator(userId, familyId)) {
            return true;
        }
        return hasPermission(userId, PERMISSION_MODIFY, familyId);
    }

    @Override
    public boolean canDelete(String userId, String familyId) {
        // 创建者硬编码拥有权限
        if (isCreator(userId, familyId)) {
            return true;
        }
        return hasPermission(userId, PERMISSION_DELETE, familyId);
    }

    /**
     * 检查用户是否为家庭创建者
     */
    private boolean isCreator(String userId, String familyId) {
        LambdaQueryWrapper<com.example.homic.model.Family> familyLqw = new LambdaQueryWrapper<>();
        familyLqw.eq(com.example.homic.model.Family::getFamilyId, familyId);
        com.example.homic.model.Family family = familyMapper.selectOne(familyLqw);
        return family != null && family.getCreatorId().equals(userId);
    }
}

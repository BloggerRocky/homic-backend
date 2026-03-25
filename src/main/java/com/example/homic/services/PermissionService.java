package com.example.homic.services;

import com.example.homic.exception.MyException;
import com.example.homic.model.Permission;
import com.example.homic.vo.ResponseVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 权限常量定义
     */
    String PERMISSION_UPLOAD = "FAMILY-UPLOAD";
    String PERMISSION_MODIFY = "FAMILY-MODIFY";
    String PERMISSION_DELETE = "FAMILY-DELETE";

    /**
     * 检查用户是否有特定权限
     * @param userId 用户ID
     * @param permissionKey 权限键
     * @param objectId 对象ID（如家庭ID）
     * @return true-有权限，false-无权限
     */
    boolean hasPermission(String userId, String permissionKey, String objectId);

    /**
     * 获取用户在某个对象下的所有权限
     * @param userId 用户ID
     * @param objectId 对象ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(String userId, String objectId);

    /**
     * 设置用户权限
     * @param userId 用户ID
     * @param permissionKey 权限键
     * @param permissionValue 权限值（0-无权限，1-有权限）
     * @param objectId 对象ID
     * @return 是否设置成功
     */
    boolean setPermission(String userId, String permissionKey, int permissionValue, String objectId);

    /**
     * 批量设置用户权限
     * @param userId 用户ID
     * @param permissions 权限键值对
     * @param objectId 对象ID
     * @return 是否设置成功
     */
    boolean batchSetPermissions(String userId, java.util.Map<String, Integer> permissions, String objectId);

    /**
     * 初始化用户默认权限（用于管理员创建时）
     * @param userId 用户ID
     * @param objectId 对象ID
     * @param role 角色（0-创建者，1-管理员，2-成员）
     */
    void initDefaultPermissions(String userId, String objectId, int role);

    /**
     * 检查用户是否有查看权限（所有家庭成员都可以查看）
     */
    boolean canView(String userId, String familyId);

    /**
     * 检查用户是否有上传权限
     */
    boolean canUpload(String userId, String familyId);

    /**
     * 检查用户是否有修改权限（移动和重命名）
     */
    boolean canModify(String userId, String familyId);

    /**
     * 检查用户是否有删除权限
     */
    boolean canDelete(String userId, String familyId);
}

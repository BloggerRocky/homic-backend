package com.example.homic.constants.enums;

/**
 * 家庭角色枚举
 */
public enum FamilyRoleEnum {
    
    /**
     * 主人 - 家庭创建者，拥有最高权限
     */
    OWNER(1, "主人", "Owner", "家庭创建者，拥有最高权限"),
    
    /**
     * 家长 - 家庭管理员，拥有管理权限
     */
    GUARDIAN(2, "家长", "Guardian", "家庭管理员，拥有管理权限"),
    
    /**
     * 儿童 - 儿童成员，受限制的访问权限
     */
    CHILDREN(3, "儿童", "Children", "儿童成员，受限制的访问权限");
    
    /**
     * 角色ID
     */
    private final Integer roleId;
    
    /**
     * 中文名称
     */
    private final String roleName;
    
    /**
     * 英文代码
     */
    private final String roleCode;
    
    /**
     * 角色描述
     */
    private final String description;
    
    FamilyRoleEnum(Integer roleId, String roleName, String roleCode, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleCode = roleCode;
        this.description = description;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public String getRoleCode() {
        return roleCode;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据roleId获取枚举
     */
    public static FamilyRoleEnum getByRoleId(Integer roleId) {
        if (roleId == null) {
            return null;
        }
        for (FamilyRoleEnum role : FamilyRoleEnum.values()) {
            if (role.roleId.equals(roleId)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * 根据roleCode获取枚举
     */
    public static FamilyRoleEnum getByRoleCode(String roleCode) {
        if (roleCode == null) {
            return null;
        }
        for (FamilyRoleEnum role : FamilyRoleEnum.values()) {
            if (role.roleCode.equalsIgnoreCase(roleCode)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为主人
     */
    public boolean isOwner() {
        return this == OWNER;
    }
    
    /**
     * 判断是否为家长
     */
    public boolean isGuardian() {
        return this == GUARDIAN;
    }
    
    /**
     * 判断是否为儿童
     */
    public boolean isChildren() {
        return this == CHILDREN;
    }
    
    /**
     * 判断是否有管理权限（主人和家长）
     */
    public boolean hasManagementPermission() {
        return this == OWNER || this == GUARDIAN;
    }
}

package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 关怀账号VO
 */
@Data
public class CareAccountVO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 已使用空间（字节）
     */
    private Long useSpace;
    
    /**
     * 总空间（字节）
     */
    private Long totalSpace;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 当前登录码（如果有）
     */
    private String loginCode;
    
    /**
     * 登录码过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date loginCodeExpireTime;
    
    /**
     * 账号状态 0-禁用 1-启用
     */
    private Integer status;
}

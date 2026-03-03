package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 关怀账号登录码
 */
@Data
@TableName("care_account_login_code")
public class CareAccountLoginCode implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private String userId;
    
    @TableField("login_code")
    private String loginCode;
    
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    private static final long serialVersionUID = 1L;
}

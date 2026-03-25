package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("permission")
public class Permission implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String permissionKey;  // 权限键，如 FAMILY-UPLOAD, FAMILY-MODIFY, FAMILY-DELETE
    private Integer permissionValue;  // 权限值，0-无权限，1-有权限
    private String objectId;  // 对象ID，如家庭ID
    private Date createTime;
    private Date updateTime;
}

package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("family")
public class Family implements Serializable {
    @TableId
    private String familyId;
    private String familyName;
    private String familyDesc;
    private String familyAvatar;
    private String familyCode;
    private String creatorId;
    private Long useSpace;
    private Long totalSpace;
    private Date createTime;
    private Date updateTime;
}

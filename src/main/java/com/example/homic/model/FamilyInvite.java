package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("family_invite")
public class FamilyInvite implements Serializable {
    @TableId
    private String inviteId;
    private String familyId;
    private String fromUserId;
    private String toUserId;
    private Integer status;  // 0-待处理 1-已接受 2-已拒绝
    private Date createTime;
    private Date updateTime;
}

package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class FamilyInviteVO {
    private String inviteId;
    private String familyId;
    private String familyName;
    private String fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class FamilyMemberVO {
    private String userId;
    private String nickName;
    private String avatar;
    private Integer role;  // 0-创建者 1-管理员 2-成员
    private String remark;  // 成员备注（仅在该家庭内生效）
    private Boolean isFriend;  // 是否为好友
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinTime;
}

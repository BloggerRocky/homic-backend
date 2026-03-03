package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class FamilyApplyVO {
    private String applyId;
    private String familyId;
    private String userId;
    private String nickName;
    private String avatar;
    private Integer status;  // 0-待处理 1-已同意 2-已拒绝
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

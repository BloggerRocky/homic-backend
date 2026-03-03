package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("family_apply")
public class FamilyApply implements Serializable {
    @TableId
    private String applyId;
    private String familyId;
    private String userId;
    private Integer status;  // 0-待处理 1-已同意 2-已拒绝
    private Date createTime;
    private Date updateTime;
}

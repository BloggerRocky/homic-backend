package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("family_member")
public class FamilyMember implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String familyId;
    private String userId;
    private Integer role;  // 0-创建者 1-管理员 2-成员
    private String remark;  // 成员备注（仅在该家庭内生效）
    private Date joinTime;
}

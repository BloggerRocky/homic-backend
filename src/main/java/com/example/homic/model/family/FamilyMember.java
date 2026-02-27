package com.example.homic.model.family;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 家庭成员表
 */
@Data
public class FamilyMember implements Serializable {

    @TableId(value = "member_id", type = IdType.INPUT)
    private String memberId;

    /**
     * 家庭ID
     */
    private String familyId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 在家庭中的昵称
     */
    private String nickName;

    /**
     * 加入家庭的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date joinTime;

    /**
     * 成员状态：0-已移除，1-正常，2-待审核
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}

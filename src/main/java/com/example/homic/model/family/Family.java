package com.example.homic.model.family;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 家庭信息表
 */
@Data
public class Family implements Serializable {

    @TableId(value = "family_id", type = IdType.INPUT)
    private String familyId;

    /**
     * 家庭名称
     */
    private String familyName;

    /**
     * 家庭所有者ID（创建者）
     */
    private String ownerId;

    /**
     * 家庭描述
     */
    private String description;

    /**
     * 家庭总空间（单位Byte，默认1TB）
     */
    private Long totalSpace;

    /**
     * 家庭已用空间（单位Byte）
     */
    private Long useSpace;

    /**
     * 家庭成员数
     */
    private Integer memberCount;

    /**
     * 家庭状态：0-禁用，1-正常
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

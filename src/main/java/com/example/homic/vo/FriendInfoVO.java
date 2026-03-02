package com.example.homic.vo;

import lombok.Data;

/**
 * 好友信息VO
 * 用于返回好友列表，包含好友的个人信息和关系信息
 */
@Data
public class FriendInfoVO {

    /**
     * 好友用户ID
     */
    private String friendId;

    /**
     * 好友昵称
     */
    private String nickName;

    /**
     * 好友头像
     */
    private String avatar;

    /**
     * 好友备注
     */
    private String remark;

    /**
     * 是否特别关注
     * 0-否，1-是
     */
    private Integer isSpecial;

    /**
     * 关系ID
     */
    private String relationId;
}

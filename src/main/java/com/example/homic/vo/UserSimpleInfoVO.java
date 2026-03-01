package com.example.homic.vo;

import lombok.Data;

/**
 * 用户简化信息VO
 * 仅包含用户ID、昵称和头像
 * 作者：Rocky23318
 * 时间：2024
 * 项目名：homic
 */
@Data
public class UserSimpleInfoVO {
    private String userId;
    private String nickName;
    private String userAvatar;

    public UserSimpleInfoVO() {
    }

    public UserSimpleInfoVO(String userId, String nickName, String userAvatar) {
        this.userId = userId;
        this.nickName = nickName;
        this.userAvatar = userAvatar;
    }
}

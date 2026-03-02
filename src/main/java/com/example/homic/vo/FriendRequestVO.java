package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 好友申请VO
 * 用于返回好友申请列表，包含对方的个人信息
 */
@Data
public class FriendRequestVO {

    /**
     * 申请ID
     */
    private String id;

    /**
     * 发送者用户ID
     */
    private String senderId;

    /**
     * 发送者昵称
     */
    private String senderNickName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者用户ID
     */
    private String receiverId;

    /**
     * 接收者昵称
     */
    private String receiverNickName;

    /**
     * 接收者头像
     */
    private String receiverAvatar;

    /**
     * 申请状态
     * 0-待处理，1-已接受，2-已拒绝
     */
    private Integer status;

    /**
     * 申请消息
     */
    private String message;

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
}

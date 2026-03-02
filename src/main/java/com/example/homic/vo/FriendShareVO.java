package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 好友分享信息VO
 */
@Data
public class FriendShareVO {
    
    /**
     * 分享ID
     */
    private Long shareId;
    
    /**
     * 分享者用户ID
     */
    private String fromUserId;
    
    /**
     * 分享者昵称
     */
    private String fromUserNickName;
    
    /**
     * 接收者用户ID
     */
    private String toUserId;
    
    /**
     * 接收者昵称
     */
    private String toUserNickName;
    
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件类型：0-文件，1-文件夹
     */
    private Integer folderType;
    
    /**
     * 文件类型
     */
    private Integer fileType;
    
    /**
     * 文件分类
     */
    private Integer fileCategory;
    
    /**
     * 文件封面
     */
    private String fileCover;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 有效期类型：0-永久，1-1天，2-7天，3-30天
     */
    private Integer validType;
    
    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    
    /**
     * 分享时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;
    
    /**
     * 状态：0-已取消，1-有效，2-已过期
     */
    private Integer status;
    
    /**
     * 是否是发送方（true-我发送的，false-我收到的）
     */
    private Boolean isSender;
}

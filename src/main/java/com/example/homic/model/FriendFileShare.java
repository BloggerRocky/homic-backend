package com.example.homic.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友文件分享表
 */
@Data
@TableName("friend_file_share")
public class FriendFileShare implements Serializable {

    /**
     * 分享ID
     */
    @TableId(value = "share_id", type = IdType.AUTO)
    private Long shareId;

    /**
     * 分享者用户ID
     */
    private String fromUserId;

    /**
     * 接收者用户ID
     */
    private String toUserId;

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
     * 文件类型：0-其他，1-视频，2-音频，3-图片，4-文档，5-压缩包
     */
    private Integer fileType;

    /**
     * 文件分类：1-视频，2-音频，3-图片，4-文档，5-其他
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

    private static final long serialVersionUID = 1L;
}

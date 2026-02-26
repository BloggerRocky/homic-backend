package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.23:22
 * 项目名：homic
 */
@Data
public class ShareInfoVO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    private String shareId;
    private String fileId;
    private String nickName;//分享人名称
    private String fileName;
    private String avatar;//分享人头像
    private String userId;//分享人id
    private Boolean currentUser;
}

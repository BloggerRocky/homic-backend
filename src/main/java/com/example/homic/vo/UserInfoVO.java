package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.15:55
 * 项目名：homic
 */
@Data
public class UserInfoVO {
    String userId;
    String nickName;
    String email;
    String qqAvatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date joinTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date lastLoginTime;
    Boolean status;
    Long useSpace;
    Long totalSpace;
}

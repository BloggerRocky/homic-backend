package com.example.homic.dto.session;

import lombok.Data;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/13.16:13
 * 项目名：homic
 */
//用于装填登陆后返回给前端的用户数据和存储到服务器本地session中的包装实体类
    @Data
public class SessionWebUserDTO {
    private String nickName;
    private String userId;
    private String userAvatar;
    private Boolean admin;
    private Boolean isDummy;  // 是否为关怀账号

}

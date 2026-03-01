package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2026.2026/3/1.22:46
 * 项目名：back-end
 */
public enum FriendRequestEnum {
    PENDING(0,"pending","已申请"),
    ACCEPTED(1,"accepted","已通过"),
    REJECTED(2,"rejected","已拒绝");

    private Integer id;
    private String code;
    private String msg;
    FriendRequestEnum(Integer id,String code,String msg){
        this.code = code;
        this.msg = msg;
    }
}

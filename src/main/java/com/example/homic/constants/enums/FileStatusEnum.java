package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/15.12:35
 * 项目名：homic
 */
public enum FileStatusEnum {
    TRANSFERRING(0,"转码中"),
    TRANS_FAILED (1,"转码失败"),
    TRANS_SUCCEED(2,"转码成功") ;;

    private Integer status;
    private String desc;

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    FileStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;

    }
}

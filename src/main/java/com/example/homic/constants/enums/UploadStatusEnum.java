package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/15.12:42
 * 项目名：homic
 */
//上传回应状态枚举类
public enum UploadStatusEnum {
    UPLOAD_SECONDS ("upload_seconds", "秒传"),
    UPLOADING("uploading","上传中"),
    UPLOAD_FINISH("upload_finish","上传完成");
    private String status;
    private String desc;

    UploadStatusEnum(String code, String desc) {
        this.status = code;
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}

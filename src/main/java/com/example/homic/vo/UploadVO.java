package com.example.homic.vo;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.22:05
 * 项目名：homic
 */
public class UploadVO {
    private String fileId;//上传文件的ID
    private String status;//当前文件上传状态

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

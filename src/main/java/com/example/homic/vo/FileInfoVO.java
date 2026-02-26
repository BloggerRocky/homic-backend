package com.example.homic.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.18:45
 * 项目名：homic
 */
@Data
public class FileInfoVO {
    private String fileId;
    private String filePid;
    private Integer fileSize;
    private String userId;
    private String fileName;
    private String fileCover;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;
    private Integer folderType;
    private Integer fileCategory;
    private Integer fileType;
    private Integer status;
    private String nickName;
}

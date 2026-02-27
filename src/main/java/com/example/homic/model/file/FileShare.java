package com.example.homic.model.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class FileShare implements Serializable {
    private String shareId;

    private String fileId;

    private String userId;

    private Integer validType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;
    private String code;

    private Integer showCount;

    private String fileName;

    private Integer folderType;

    private Integer fileCategory;

    private Integer fileType;

    private String fileCover;

    private static final long serialVersionUID = 1L;

}

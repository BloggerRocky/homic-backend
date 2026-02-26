package com.example.homic.dto.session;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/20.0:08
 * 项目名：homic
 */
@Data
public class SessionShareInfoDTO   {
    private String shareUserId;
    private String shareId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    private String fileId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shareTime;
    private String FileName;
}

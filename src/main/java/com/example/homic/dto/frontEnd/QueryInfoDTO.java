package com.example.homic.dto.frontEnd;

import lombok.Data;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.18:26
 * 项目名：homic
 */
//用于接受前端查询参数的包装实体类
@Data
public class QueryInfoDTO {
    private String userId;
    private String fileId;
    private Integer pageNo;
    private Integer pageSize;
    private String fileNameFuzzy;//模糊查询域
    private String category;//粗分类
    private String filePid;//父文件夹ID
    private Integer delFlag = 2;//查询状态默认只查询正常文件
    private boolean queryNickName = false;
}

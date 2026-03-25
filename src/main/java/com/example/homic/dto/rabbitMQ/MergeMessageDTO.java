package com.example.homic.dto.rabbitMQ;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.example.homic.dto.frontEnd.UploadDTO;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/16.19:09
 * 项目名：homic
 */
//用于存放mergeFile参数的消息队列实体类（可序列化）
public class MergeMessageDTO implements  Serializable{
    private UploadDTO uploadDTO;
    private String filePath;
    private String userId;
    private String familyId; // 家庭ID，null表示个人空间文件
    @JSONField(serialize =false)
    public static final String EXCHANGE_NAME = "merge_exchange";//交换机名称
    @JSONField(serialize =false)
    public static final String ROUTING_KEY = "merge";//路由键

    public UploadDTO getUploadDTO() {
        return uploadDTO;
    }

    public void setUploadDTO(UploadDTO uploadDTO) {
        this.uploadDTO = uploadDTO;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public MergeMessageDTO(UploadDTO uploadDTO, String filePath, String userId) {
        this.uploadDTO = uploadDTO;
        this.filePath = filePath;
        this.userId = userId;
        this.familyId = null; // 默认为个人空间
    }

    public MergeMessageDTO(UploadDTO uploadDTO, String filePath, String userId, String familyId) {
        this.uploadDTO = uploadDTO;
        this.filePath = filePath;
        this.userId = userId;
        this.familyId = familyId;
    }

    public MergeMessageDTO() {
    }
}

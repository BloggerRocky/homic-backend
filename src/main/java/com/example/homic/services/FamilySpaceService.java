package com.example.homic.services;

import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.frontEnd.UploadDTO;
import com.example.homic.exception.MyException;
import com.example.homic.vo.FileInfoVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UploadVO;

/**
 * 家庭空间文件服务接口
 * 与个人文件服务 FileService 完全分离，拥有独立的权限校验和空间管理逻辑
 */
public interface FamilySpaceService {

    /**
     * 加载家庭文件列表
     * @param queryInfoDTO 查询参数
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 文件列表
     * @throws MyException
     */
    ResponseVO loadDataList(QueryInfoDTO queryInfoDTO, String userId, String familyId) throws MyException;

    /**
     * 上传文件到家庭空间
     * 包含权限校验（关怀账号和普通成员不能上传）和家庭空间容量校验
     * @param uploadDTO 上传参数
     * @param userId 当前用户ID
     * @param isDummy 是否为关怀账号
     * @param familyId 家庭ID
     * @return 上传结果
     * @throws Exception
     */
    UploadVO uploadFile(UploadDTO uploadDTO, String userId, boolean isDummy, String familyId) throws Exception;

    /**
     * 在家庭空间新建文件夹
     * 包含权限校验（关怀账号和普通成员不能新建）
     * @param filePid 父文件夹ID
     * @param fileName 文件夹名称
     * @param userId 当前用户ID
     * @param isDummy 是否为关怀账号
     * @param familyId 家庭ID
     * @return 文件夹信息
     * @throws MyException
     */
    FileInfoVO newFolder(String filePid, String fileName, String userId, boolean isDummy, String familyId) throws MyException;

    /**
     * 刷新家庭空间使用量（更新DB和Redis缓存）
     * @param familyId 家庭ID
     */
    void refreshFamilySpace(String familyId);
}

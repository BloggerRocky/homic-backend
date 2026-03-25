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
     * 包含家庭空间容量校验
     * @param uploadDTO 上传参数
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 上传结果
     * @throws Exception
     */
    UploadVO uploadFile(UploadDTO uploadDTO, String userId, String familyId) throws Exception;

    /**
     * 在家庭空间新建文件夹
     * @param filePid 父文件夹ID
     * @param fileName 文件夹名称
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 文件夹信息
     * @throws MyException
     */
    FileInfoVO newFolder(String filePid, String fileName, String userId, String familyId) throws MyException;

    /**
     * 刷新家庭空间使用量（更新DB和Redis缓存）
     * @param familyId 家庭ID
     */
    void refreshFamilySpace(String familyId);

    /**
     * 获取家庭空间使用情况
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 空间使用情况（useSpace, totalSpace）
     * @throws MyException
     */
    ResponseVO getFamilySpaceUsage(String userId, String familyId) throws MyException;

    /**
     * 加载家庭空间所有文件夹（用于移动功能）
     * @param filePid 父文件夹ID
     * @param currentFileIds 当前文件ID（需要排除的）
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 文件夹列表
     * @throws MyException
     */
    ResponseVO loadAllFolder(String filePid, String currentFileIds, String userId, String familyId) throws MyException;

    /**
     * 删除家庭空间文件
     * @param fileIds 文件ID列表（逗号分隔）
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 删除结果
     * @throws MyException
     */
    ResponseVO deleteFamilyFiles(String fileIds, String userId, String familyId) throws MyException;

    /**
     * 重命名家庭空间文件
     * @param fileId 文件ID
     * @param fileName 新文件名
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 重命名结果
     * @throws MyException
     */
    ResponseVO renameFile(String fileId, String fileName, String userId, String familyId) throws MyException;

    /**
     * 移动家庭空间文件
     * @param fileIds 文件ID列表（逗号分隔）
     * @param filePid 目标父文件夹ID
     * @param userId 当前用户ID
     * @param familyId 家庭ID
     * @return 移动结果
     * @throws MyException
     */
    ResponseVO changeFileFolder(String fileIds, String filePid, String userId, String familyId) throws MyException;
}

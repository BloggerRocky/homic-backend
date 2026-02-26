package com.example.homic.services;

import com.example.homic.exception.MyException;
import com.example.homic.vo.FileInfoVO;
import com.example.homic.vo.FolderInfoVO;
import com.example.homic.vo.PageResultVO;

import java.util.List;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/18.13:59
 * 项目名：homic
 */
public interface ManageService {
    /**
     * 创建文件夹
     * @param filePid
     * @param fileName
     * @param userId
     * @return
     * @throws MyException
     */

    FileInfoVO newFolder(String filePid, String fileName, String userId) throws MyException;

    /**
     * 重命名文件
     * @param fileId
     * @param fileName
     * @param userId
     * @return
     */

    FileInfoVO reNameFile(String fileId, String fileName, String userId);

    /**
     * 获取文件夹信息
     * @param path
     * @param shareId
     * @param userId
     * @return
     */

    List<FolderInfoVO> getFolderInfo(String path, String shareId, String userId);

    /**
     * 获取当前文件夹路径上的所有文件信息
     * @param filePid
     * @param currentFileIds
     * @param userId
     * @return
     * @throws MyException
     */

    List<FileInfoVO> loadAllFolder(String filePid, String currentFileIds, String userId) throws MyException;

    /**
     * （批量）移动文件
     * @param fileIds
     * @param filePid
     * @param userId
     * @return
     * @throws MyException
     */
    List<FileInfoVO> changeFileFolder(String fileIds, String filePid, String userId) throws MyException;

    /**
     * 批量 删除文件
     * @param fileIds
     * @param userId
     * @return
     * @throws MyException
     */

    boolean deleteFile(String fileIds, String userId) throws MyException;

    /**
     * 加载回收站文件列表
     * @param pageNo
     * @param pageSize
     * @param userId
     * @return
     */

    PageResultVO loadRecycleList(String pageNo, String pageSize, String userId);

    /**
     * （批量）恢复文件
     * @param fileIds
     * @param userId
     * @return
     * @throws MyException
     */

    boolean recoverFile(String fileIds, String userId) throws MyException;

    /**
     * 彻底删除（粉碎）文件
     * @param fileIds
     * @param userId
     * @return
     * @throws Exception
     */
    boolean smashFile(List<String> fileIds, String userId) throws Exception;
}

package com.example.homic.services;

import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.frontEnd.UploadDTO;
import com.example.homic.dto.redis.RedisSettingDTO;
import com.example.homic.exception.MyException;
import com.example.homic.vo.FolderInfoVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UploadVO;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.18:53
 * 项目名：homic
 */
@Service
public interface FileService {
    /**
     * 加载用户文件
     * @param queryInfoDTO
     * @return
     */
     ResponseVO loadDataList(QueryInfoDTO queryInfoDTO);

    /**
     * 上传文件（分为秒传，分片上传）
     * @param uploadDTO
     * @param userId
     * @return
     * @throws Exception
     */

    UploadVO uploadFile(UploadDTO uploadDTO, String userId) throws Exception;

    /**
     * 合并文件
     * @param uploadDTO 上传参数
     * @param filePath 文件路径
     * @param userId 用户ID
     * @throws Exception
     */
    void mergeFile(UploadDTO uploadDTO, String filePath, String userId) throws Exception;

    /**
     * 获取文件缩略图
     * @param response
     * @param coverPath
     * @throws Exception
     */
    void getImage(HttpServletResponse response, String coverPath) throws Exception;

    /**
     * 加载视频文件（预览视频文件）
     * @param response
     * @param fileId
     * @param userId
     * @param session
     * @throws Exception
     */

    void getVideoInfo(HttpServletResponse response, String fileId, String userId, HttpSession session) throws Exception;

    /**
     * 下载文件（仅用于音乐的在线预览）
     * @param response
     * @param fileId
     * @param userId
     * @param session
     * @throws MyException
     */

    void getFile(HttpServletResponse response, String fileId, String userId, HttpSession session) throws MyException;

    /**
     * 创建下载链接（前端自动调用）
     * @param fileId
     * @param userId
     * @return
     * @throws MyException
     */

    String getDownloadUrl(String fileId, String userId) throws MyException;

    /**
     * 下载文件
     * @param code
     * @param response
     * @throws Exception
     */

    void downloadFile(String code, HttpServletResponse response) throws Exception;

}

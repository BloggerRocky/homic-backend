package com.example.homic.services;

import com.example.homic.exception.MyException;
import com.example.homic.model.file.FileShare;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ShareInfoVO;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.12:24
 * 项目名：homic
 */
@Service
public interface ShareService {
    /**
     * 加载用户个人创建的分享链接
     * @param pageNo
     * @param pageSize
     * @param userId
     * @return
     * @throws MyException
     */
    PageResultVO loadShareList(String pageNo, String pageSize, String userId) throws MyException;

    /**
     * 创建分享链接
     * @param fileId
     * @param validType
     * @param codeType
     * @param code
     * @param userId
     * @return
     * @throws MyException
     */

    FileShare shareFile(String fileId, String validType, String codeType, String code, String userId) throws MyException;

    /**
     * 取消分享链接
     * @param shareIds
     * @param userId
     * @return
     * @throws MyException
     */

    boolean cancelShare(String shareIds, String userId) throws MyException;

    /**
     * 获取分享文件的信息（仅仅是获取文件的发布时间，到期时间，发布人等文件索引归属信息）
     * @param shareId
     * @param userId
     * @return
     */

    ShareInfoVO getShareInfo(String shareId, String userId);

    /**
     * 校验提取码
     * @param shareId
     * @param code
     * @param session
     * @return
     */

    ShareInfoVO checkShareCode(String shareId, String code, HttpSession session);

    /**
     * 保存文件到自己目录
     * @param shareFileIdArray
     * @param userId
     * @param myfolderId
     * @return
     */

    boolean saveShareFile(String[] shareFileIdArray, String userId, String myfolderId);
}

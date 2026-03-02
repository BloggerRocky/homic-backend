package com.example.homic.services;

import com.example.homic.exception.MyException;
import com.example.homic.vo.ResponseVO;

/**
 * 好友文件分享服务接口
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
public interface FriendFileShareService {

    /**
     * 分享文件给好友
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @param fileIds 文件ID列表（逗号分隔）
     * @param validType 有效期类型：0-永久，1-1天，2-7天，3-30天
     * @return 响应
     * @throws MyException
     */
    ResponseVO shareFilesToFriend(String userId, String friendId, String fileIds, Integer validType) throws MyException;

    /**
     * 查询与好友的分享历史记录
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 分享记录列表
     * @throws MyException
     */
    ResponseVO getShareHistory(String userId, String friendId) throws MyException;

    /**
     * 取消分享
     * @param userId 当前用户ID
     * @param shareIds 分享ID列表（逗号分隔）
     * @return 响应
     * @throws MyException
     */
    ResponseVO cancelFriendShare(String userId, String shareIds) throws MyException;

    /**
     * 保存好友分享的文件到我的网盘
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @param targetFolderId 目标文件夹ID
     * @return 响应
     * @throws MyException
     */
    ResponseVO saveFriendSharedFile(String userId, Long shareId, String targetFolderId) throws MyException;

    /**
     * 下载好友分享的文件
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @return 下载URL
     * @throws MyException
     */
    ResponseVO downloadFriendSharedFile(String userId, Long shareId) throws MyException;

    /**
     * 预览好友分享的文件
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @return 文件信息
     * @throws MyException
     */
    ResponseVO previewFriendSharedFile(String userId, Long shareId) throws MyException;


    /**
     * 获取好友分享文件的内容（用于预览）
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @param fileId 文件ID
     * @param response HTTP响应
     * @throws Exception
     */
    void getFriendSharedFile(String userId, Long shareId, String fileId, javax.servlet.http.HttpServletResponse response) throws Exception;

    /**
     * 获取好友分享视频的流信息（用于视频预览）
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @param msg 消息（文件ID或ts文件名）
     * @param response HTTP响应
     * @param session HTTP会话
     * @throws Exception
     */
    void getFriendSharedVideoInfo(String userId, Long shareId, String msg, javax.servlet.http.HttpServletResponse response, javax.servlet.http.HttpSession session) throws Exception;

    /**
     * 获取好友分享文件的封面图片
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @param coverPath 封面路径
     * @param response HTTP响应
     * @throws Exception
     */
    void getFriendSharedImage(String userId, Long shareId, String coverPath, javax.servlet.http.HttpServletResponse response) throws Exception;

    /**
     * 创建好友分享文件的下载链接（用于预览下载）
     * @param userId 当前用户ID
     * @param shareId 分享ID
     * @param fileId 文件ID
     * @return 下载码
     * @throws MyException
     */
    ResponseVO createFriendSharedDownloadUrl(String userId, Long shareId, String fileId) throws MyException;

}

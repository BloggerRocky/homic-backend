package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.services.FileService;
import com.example.homic.services.FriendFileShareService;
import com.example.homic.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.example.homic.constants.NormalConstants.SESSION_USER_INFO_KEY;

/**
 * 好友文件分享控制器
 * 作者：Rocky23318
 * 时间：2026
 * 项目名：homic
 */
@RestController("friendFileShareController")
@RequestMapping("/friendShare")
public class FriendFileShareController {

    @Autowired
    private FriendFileShareService friendFileShareService;

    @Autowired
    private FileService fileService;

    /**
     * 分享文件给好友
     *
     * @param session   HTTP会话
     * @param friendId  好友ID
     * @param fileIds   文件ID列表（逗号分隔）
     * @param validType 有效期类型：0-永久，1-1天，2-7天，3-30天
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/shareFiles")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO shareFilesToFriend(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId,
            @VerifyParam(required = true)
            String fileIds,
            Integer validType) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.shareFilesToFriend(userInfo.getUserId(), friendId, fileIds, validType);
    }

    /**
     * 查询与好友的分享历史记录
     *
     * @param session  HTTP会话
     * @param friendId 好友ID
     * @return 分享记录列表
     * @throws MyException
     */
    @RequestMapping("/getHistory")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO getShareHistory(
            HttpSession session,
            @VerifyParam(required = true)
            String friendId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.getShareHistory(userInfo.getUserId(), friendId);
    }

    /**
     * 取消分享
     *
     * @param session  HTTP会话
     * @param shareIds 分享ID列表（逗号分隔）
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/cancel")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO cancelFriendShare(
            HttpSession session,
            @VerifyParam(required = true)
            String shareIds) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.cancelFriendShare(userInfo.getUserId(), shareIds);
    }

    /**
     * 保存好友分享的文件
     *
     * @param session        HTTP会话
     * @param shareId        分享ID
     * @param targetFolderId 目标文件夹ID
     * @return 响应
     * @throws MyException
     */
    @RequestMapping("/save")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO saveFriendSharedFile(
            HttpSession session,
            @VerifyParam(required = true)
            Long shareId,
            String targetFolderId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.saveFriendSharedFile(userInfo.getUserId(), shareId, targetFolderId);
    }

    /**
     * 下载好友分享的文件
     *
     * @param session HTTP会话
     * @param shareId 分享ID
     * @return 下载URL
     * @throws MyException
     */
    @RequestMapping("/getDownloadUrl")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO downloadFriendSharedFile(
            HttpSession session,
            @VerifyParam(required = true)
            Long shareId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.downloadFriendSharedFile(userInfo.getUserId(), shareId);
    }

    /**
     * 预览好友分享的文件
     *
     * @param session HTTP会话
     * @param shareId 分享ID
     * @return 文件信息
     * @throws MyException
     */
    @RequestMapping("/preview")
    @GlobalInteceptor(checkLogin = true, checkParams = true)
    public ResponseVO previewFriendSharedFile(
            HttpSession session,
            @VerifyParam(required = true)
            Long shareId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.previewFriendSharedFile(userInfo.getUserId(), shareId);
    }

    /**
     * 下载好友分享的文件（实际下载）
     *
     * @param response HTTP响应
     * @param code     下载码
     * @throws Exception
     */
    @RequestMapping("/download/{code}")
    public void downloadFriendFile(
            HttpServletResponse response,
            @PathVariable("code")
            String code) throws Exception {
        fileService.downloadFile(code, response);
    }


    /**
     * 获取好友分享文件的内容（用于预览）
     *
     * @param session  HTTP会话
     * @param response HTTP响应
     * @param shareId  分享ID
     * @param fileId   文件ID
     * @throws Exception
     */
    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalInteceptor(checkLogin = true)
    public void getFriendSharedFile(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("shareId")
            Long shareId,
            @PathVariable("fileId")
            String fileId) throws Exception {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        friendFileShareService.getFriendSharedFile(userInfo.getUserId(), shareId, fileId, response);
    }

    /**
     * 获取好友分享视频的流信息（用于视频预览）
     *
     * @param session  HTTP会话
     * @param response HTTP响应
     * @param shareId  分享ID
     * @param msg      消息（文件ID或ts文件名）
     * @throws Exception
     */
    @RequestMapping("/ts/getVideoInfo/{shareId}/{msg}")
    @GlobalInteceptor(checkLogin = true)
    public void getFriendSharedVideoInfo(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("shareId")
            Long shareId,
            @PathVariable("msg")
            String msg) throws Exception {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        friendFileShareService.getFriendSharedVideoInfo(userInfo.getUserId(), shareId, msg, response, session);
    }

    /**
     * 获取好友分享文件的封面图片
     *
     * @param session  HTTP会话
     * @param response HTTP响应
     * @param shareId  分享ID
     * @param rootPath 根路径
     * @param date     日期
     * @param id       ID
     * @param fileName 文件名
     * @throws Exception
     */
    @RequestMapping("/getImage/{shareId}/{rootPath}/{date}/{id}/{fileName}")
    @GlobalInteceptor(checkLogin = true)
    public void getFriendSharedImage(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("shareId")
            Long shareId,
            @PathVariable("rootPath")
            String rootPath,
            @PathVariable("date")
            String date,
            @PathVariable("id")
            String id,
            @PathVariable("fileName")
            String fileName) throws Exception {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        String coverPath = rootPath + "/" + date + "/" + id + "/" + fileName;
        friendFileShareService.getFriendSharedImage(userInfo.getUserId(), shareId, coverPath, response);
    }

    /**
     * 创建好友分享文件的下载链接（用于预览下载）
     *
     * @param session HTTP会话
     * @param shareId 分享ID
     * @param fileId  文件ID
     * @return 下载码
     * @throws MyException
     */
    @RequestMapping("/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO createFriendSharedDownloadUrl(
            HttpSession session,
            @PathVariable("shareId")
            Long shareId,
            @PathVariable("fileId")
            String fileId) throws MyException {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        return friendFileShareService.createFriendSharedDownloadUrl(userInfo.getUserId(), shareId, fileId);
    }

}

package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.session.SessionShareInfoDTO;
import com.example.homic.exception.MyException;
import com.example.homic.model.file.FileShare;
import com.example.homic.services.FileService;
import com.example.homic.services.ShareService;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.ShareInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.example.homic.constants.CodeConstants.*;
import static com.example.homic.constants.NormalConstants.SESSION_SHARE_KEY_PREFIX;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.12:22
 * 项目名：homic
 */
@RestController("shareController")
public class ShareController extends CommonController{
    @Autowired
    ShareService shareService;
    @Autowired
    FileService fileService;
    @RequestMapping("/share/loadShareList")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadShareList(
            HttpSession session,
            String pageNo,
            String pageSize
    ) throws MyException {
        String userId = getUserIdBySession(session);
        PageResultVO pageResultVO = shareService.loadShareList(pageNo, pageSize, userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS,"查询分享列表成功");
        responseVO.setData(pageResultVO);
        return responseVO;
    }
    @RequestMapping("/share/shareFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO shareFile(
            HttpSession session,
            String fileId,
            String validType,
            String codeType,
            String code
    ) throws MyException {
        String userId = getUserIdBySession(session);
        FileShare result =  shareService.shareFile(fileId, validType,codeType,code, userId);
        ResponseVO responseVO = result != null ? new ResponseVO(SUCCESS_RES_STATUS,"文件分享成功") : new ResponseVO(FAIL_RES_STATUS,"文件分享失败");
        responseVO.setData(result);
        return responseVO;
    }
    @RequestMapping("/share/cancelShare")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO recoverFile(
            HttpSession session,
            String shareIds
    ) throws MyException {
        String userId = getUserIdBySession(session);
        boolean is_success = shareService.cancelShare(shareIds,userId);
        ResponseVO responseVO = is_success ? new ResponseVO(SUCCESS_RES_STATUS,"取消分享成功") : new ResponseVO(FAIL_RES_STATUS,"取消分享失败");
        return  responseVO;
    }
    @RequestMapping("/showShare/getShareInfo")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getShareInfo(
            HttpSession session,
            String shareId
    ) throws MyException {
        String userId = getUserIdBySession(session);
        ShareInfoVO shareInfoVO = shareService.getShareInfo(shareId,userId);
        ResponseVO responseVO;
        if (shareInfoVO == null) {
            responseVO = new ResponseVO(FAIL_RES_STATUS, "分享链接已取消或过期");
            responseVO.setCode(EXPIRE_RES_CODE);
        } else {
            responseVO = new ResponseVO(SUCCESS_RES_STATUS, "查询分享信息成功");
            responseVO.setData(shareInfoVO);
        }
        return responseVO;

    }
    @RequestMapping("/showShare/getShareLoginInfo")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getShareLoginInfo(
            HttpSession session,
            String shareId
    ) throws MyException {
        SessionShareInfoDTO sessionShareInfoDTO = (SessionShareInfoDTO) session.getAttribute(SESSION_SHARE_KEY_PREFIX+shareId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(sessionShareInfoDTO);
        return responseVO;

    }
    @RequestMapping("/showShare/checkShareCode")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO checkShareCode(
            HttpSession session,
            String shareId,
            String code
    ) throws MyException {
        ShareInfoVO  shareInfo = shareService.checkShareCode(shareId,code,session);
        ResponseVO responseVO =  shareInfo != null? new ResponseVO(SUCCESS_RES_STATUS,"提取码正确") : new ResponseVO("提取码错误");
        responseVO.setData(shareInfo);
        return  responseVO;
    }
    @RequestMapping("/showShare/loadFileList")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadFileList(
            HttpSession session,
            Integer pageNo,
            Integer pageSize,
            String shareId,
            String filePid
    ) throws MyException {
        SessionShareInfoDTO shareInfo = (SessionShareInfoDTO) session.getAttribute(SESSION_SHARE_KEY_PREFIX+shareId);
        if(shareInfo == null)
            return new ResponseVO(FAIL_RES_STATUS,"非法访问");
        String fileId = shareInfo.getFileId();
        QueryInfoDTO queryInfoDTO = new QueryInfoDTO();
        queryInfoDTO.setFilePid(filePid);
        queryInfoDTO.setFileId(fileId);
        queryInfoDTO.setPageNo(pageNo);
        queryInfoDTO.setPageSize(pageSize);
        ResponseVO responseVO = fileService.loadDataList(queryInfoDTO);
        return  responseVO;
    }
    @RequestMapping("/showShare/ts/getVideoInfo/{shareId}/{msg}")
    @GlobalInteceptor(checkLogin = true)
    public void getVideoInfo(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("shareId")//绑定路径参数
            String shareId,
            @PathVariable("msg")//绑定路径参数
            String msg
    ) throws Exception {
        SessionShareInfoDTO sessionShareInfoDTO = (SessionShareInfoDTO) session.getAttribute(SESSION_SHARE_KEY_PREFIX+shareId);
        if(sessionShareInfoDTO == null)
            return;
        String userId = sessionShareInfoDTO.getShareUserId();
        fileService.getVideoInfo(response,msg,userId,session);
    }

    @RequestMapping("/showShare/createDownloadUrl/{shareId}/{fileId}")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO createDownloadUrl(
            HttpSession session,
            @PathVariable("fileId")//绑定路径参数
            String fileId,
            @PathVariable("shareId")//绑定路径参数
            String shareId
    ) throws MyException {
        SessionShareInfoDTO sessionShareInfoDTO = (SessionShareInfoDTO) session.getAttribute(SESSION_SHARE_KEY_PREFIX+shareId);
        if(sessionShareInfoDTO == null)
            return new ResponseVO(FAIL_RES_STATUS,"非法访问");
        String userId = sessionShareInfoDTO.getShareUserId();
        String downloadUrl = fileService.getDownloadUrl(fileId,userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "创建下载链接成功");
        responseVO.setData(downloadUrl);
        return responseVO;
    }
    @RequestMapping("/showShare/download/{code}")
    public void downloadFile(
            HttpServletResponse response,
            @PathVariable("code")//绑定路径参数
            String code
    ) throws Exception {
        fileService.downloadFile(code,response);
    }
    @RequestMapping("/showShare/saveShare")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO saveShareFile(
            HttpSession session,
            String shareId,
            String shareFileIds,
            String myFolderId
    ) throws Exception {
        SessionShareInfoDTO sessionShareInfoDTO = (SessionShareInfoDTO) session.getAttribute(SESSION_SHARE_KEY_PREFIX+shareId);
        String userId = getUserIdBySession(session);
        if(sessionShareInfoDTO == null)
            return new ResponseVO(FAIL_RES_STATUS,"非法访问");
        String[] shareFileIdArray = shareFileIds.split(",");
        boolean is_success = shareService.saveShareFile(shareFileIdArray, userId,myFolderId);
        return is_success? new ResponseVO(SUCCESS_RES_STATUS,"保存成功"): new ResponseVO(FAIL_RES_STATUS,"保存失败");
    }
}

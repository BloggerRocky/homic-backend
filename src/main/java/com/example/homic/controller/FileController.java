package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.dto.frontEnd.UploadDTO;
import com.example.homic.exception.MyException;
import com.example.homic.model.UserInfo;
import com.example.homic.services.FileService;
import com.example.homic.vo.FolderInfoVO;
import com.example.homic.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.enums.FileFlagEnum.NORMAL;
import static com.example.homic.constants.NormalConstants.SESSION_USER_INFO_KEY;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.17:56
 * 项目名：homic
 */

@RestController("fileController")
@RequestMapping("/file")
public class FileController extends CommonController{
    @Autowired
    FileService fileService;
    @RequestMapping("/loadDataList")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadDataList(
            HttpSession session,
            QueryInfoDTO queryInfo)
    {
        SessionWebUserDTO userInfo = (SessionWebUserDTO)session.getAttribute(SESSION_USER_INFO_KEY);
        queryInfo.setUserId(userInfo.getUserId());
        queryInfo.setDelFlag(NORMAL.getFlag());
        ResponseVO responseVO = fileService.loadDataList(queryInfo);
        return responseVO;
    }
    @RequestMapping("/uploadFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO uploadFile(
            HttpSession session,
            UploadDTO uploadDTO) throws Exception {
        //获取用户信息
        String userId = getUserIdBySession(session);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(fileService.uploadFile(uploadDTO,userId));
        return responseVO;
    }
    @RequestMapping("/getImage/{rootPath}/{date}/{id}/{fileName}")
    @GlobalInteceptor(checkLogin = true)
    public void getImage(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("rootPath")//绑定路径参数
            String rootPath,
            @PathVariable("date")//绑定路径参数
            String date,
            @PathVariable("id")//绑定路径参数
            String id,
            @PathVariable("fileName")//绑定路径参数
            String fileName
    ) throws Exception {
        String coverPath = rootPath+"/"+date+"/"+id+"/"+fileName;
        fileService.getImage(response,coverPath);
    }
    @RequestMapping("/ts/getVideoInfo/{msg}")
    @GlobalInteceptor(checkLogin = true)
    public void getVideoInfo(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("msg")//绑定路径参数
            String msg
    ) throws Exception {
        String userId = super.getUserIdBySession(session);
        fileService.getVideoInfo(response,msg,userId,session);
    }
    @RequestMapping("/getFile/{fileId}")
    @GlobalInteceptor(checkLogin = true)
    public void getFile(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("fileId")//绑定路径参数
            String fileId
    ) throws Exception {
        String userId = super.getUserIdBySession(session);
        fileService.getFile(response,fileId,userId,session);
    }
    @RequestMapping("/createDownloadUrl/{fileId}")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO createDownloadUrl(
            HttpSession session,
            @PathVariable("fileId")//绑定路径参数
            String fileId
    ) throws MyException {
        String userId = super.getUserIdBySession(session);
        String downloadUrl = fileService.getDownloadUrl(fileId,userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "创建下载链接成功");
        responseVO.setData(downloadUrl);
        return responseVO;
    }
    @RequestMapping("/download/{code}")
    public void downloadFile(
            HttpServletResponse response,
            @PathVariable("code")//绑定路径参数
            String code
    ) throws Exception {
        fileService.downloadFile(code,response);
    }

}

package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.exception.MyException;
import com.example.homic.services.ManageService;
import com.example.homic.vo.FileInfoVO;
import com.example.homic.vo.FolderInfoVO;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.List;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/18.13:58
 * 项目名：homic
 */
@RestController("manageController")
public class ManageController extends CommonController{
    @Autowired
    ManageService manageService;
    @RequestMapping("/file/newFoloder")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO newFolder(
            HttpSession session,
            String filePid,
            String fileName
    ) throws MyException {
        String userId = getUserIdBySession(session);
        FileInfoVO folderInfo = manageService.newFolder(filePid, fileName, userId);
        ResponseVO responseVO = folderInfo != null ? new ResponseVO(SUCCESS_RES_STATUS,"创建成功") : new ResponseVO(FAIL_RES_STATUS,"包含同名文件夹");
        responseVO.setData(folderInfo);
        return  responseVO;
    }
    @RequestMapping("/file/getFolderInfo")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getFolderInfo(
            HttpSession session,
            String path,
            String shareId
    ) {
        String userId = getUserIdBySession(session);
        List<FolderInfoVO> folderInfos = manageService.getFolderInfo(path, shareId, userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(folderInfos);
        return  responseVO;
    }
    @RequestMapping("/file/rename")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO reNameFile(
            HttpSession session,
            String fileId,
            String fileName
    )  {
        String userId = getUserIdBySession(session);
        FileInfoVO resFileInfoVO = manageService.reNameFile(fileId,fileName,userId);
        ResponseVO responseVO = resFileInfoVO != null ? new ResponseVO(SUCCESS_RES_STATUS,"重命名成功") : new ResponseVO(FAIL_RES_STATUS,"重命名失败");
        responseVO.setData(resFileInfoVO);
        return  responseVO;
    }
    @RequestMapping("/file/loadAllFolder")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadAllFolder(
            HttpSession session,
            String filePid,
            String currentFileIds
    ) throws MyException {
        String userId = getUserIdBySession(session);
        List<FileInfoVO> fileInfos = manageService.loadAllFolder(filePid,currentFileIds,userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(fileInfos);
        return  responseVO;
    }
    @RequestMapping("/file/changeFileFolder")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO changeFileFolder(
            HttpSession session,
            String fileIds,//需要移动的文件id集合
            String filePid //目标父文件id
    ) throws MyException {
        String userId = getUserIdBySession(session);
        List<FileInfoVO> fileInfos = manageService.changeFileFolder(fileIds,filePid,userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(fileInfos);
        return  responseVO;
    }
    @RequestMapping("/file/delFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO changeFileFolder(
            HttpSession session,
            String fileIds//需要预删除的文件id集合
    ) throws MyException {
        String userId = getUserIdBySession(session);
        boolean is_success = manageService.deleteFile(fileIds,userId);
        ResponseVO responseVO = is_success ? new ResponseVO(SUCCESS_RES_STATUS,"文件删除成功") : new ResponseVO(FAIL_RES_STATUS,"文件删除失败");
        return  responseVO;
    }
    @RequestMapping("recycle/loadRecycleList")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadRecycleList(
            HttpSession session,
            String pageNo,
            String pageSize
    ) throws MyException {
        String userId = getUserIdBySession(session);
        PageResultVO pageResult = manageService.loadRecycleList(pageNo,pageSize,userId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(pageResult);
        return  responseVO;
    }
    @RequestMapping("recycle/recoverFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO recoverFile(
            HttpSession session,
            String fileIds
    ) throws MyException {
        String userId = getUserIdBySession(session);
        boolean is_success = manageService.recoverFile(fileIds,userId);
        ResponseVO responseVO = is_success ? new ResponseVO(SUCCESS_RES_STATUS,"文件恢复成功") : new ResponseVO(FAIL_RES_STATUS,"文件路径错误");
        return  responseVO;
    }
    @RequestMapping("/recycle/delFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO smashFile(
            HttpSession session,
            String fileIds
    ) throws Exception {
        List<String> fileIdsList = Arrays.asList(fileIds.split(","));
        String userId = getUserIdBySession(session);
        manageService.smashFile(fileIdsList,userId);
        return  new ResponseVO(SUCCESS_RES_STATUS,"彻底删除文件成功");
    }
}

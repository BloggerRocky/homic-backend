package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.frontEnd.UploadDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.services.FamilySpaceService;
import com.example.homic.vo.FileInfoVO;
import com.example.homic.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static com.example.homic.constants.CodeConstants.*;

/**
 * 家庭空间文件控制器
 * 仅负责请求转发，所有业务逻辑在 FamilySpaceService 中处理
 */
@RestController
@RequestMapping("/familySpace")
public class FamilySpaceController extends CommonController {

    @Autowired
    private FamilySpaceService familySpaceService;

    /**
     * 加载家庭文件列表
     */
    @RequestMapping("/loadDataList")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadDataList(
            HttpSession session,
            QueryInfoDTO queryInfo,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.loadDataList(queryInfo, userDTO.getUserId(), familyId);
    }

    /**
     * 上传文件到家庭空间
     */
    @RequestMapping("/uploadFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO uploadFile(
            HttpSession session,
            UploadDTO uploadDTO,
            String familyId) throws Exception {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(familySpaceService.uploadFile(uploadDTO, userDTO.getUserId(), familyId));
        return responseVO;
    }

    /**
     * 获取家庭空间使用情况
     */
    @RequestMapping("/getFamilySpaceUsage")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getFamilySpaceUsage(
            HttpSession session,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.getFamilySpaceUsage(userDTO.getUserId(), familyId);
    }

    /**
     * 加载家庭空间所有文件夹（用于移动功能）
     */
    @RequestMapping("/loadAllFolder")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO loadAllFolder(
            HttpSession session,
            String filePid,
            String currentFileIds,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.loadAllFolder(filePid, currentFileIds, userDTO.getUserId(), familyId);
    }

    /**
     * 在家庭空间新建文件夹
     */
    @RequestMapping("/newFolder")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO newFolder(
            HttpSession session,
            String filePid,
            String fileName,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        FileInfoVO folderInfo = familySpaceService.newFolder(filePid, fileName, userDTO.getUserId(), familyId);
        ResponseVO responseVO = folderInfo != null
                ? new ResponseVO(SUCCESS_RES_STATUS, "创建成功")
                : new ResponseVO(FAIL_RES_STATUS, "包含同名文件夹");
        responseVO.setData(folderInfo);
        return responseVO;
    }

    /**
     * 删除家庭空间文件
     */
    @RequestMapping("/delFile")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO delFile(
            HttpSession session,
            String fileIds,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.deleteFamilyFiles(fileIds, userDTO.getUserId(), familyId);
    }

    /**
     * 重命名家庭空间文件
     */
    @RequestMapping("/rename")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO rename(
            HttpSession session,
            String fileId,
            String fileName,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.renameFile(fileId, fileName, userDTO.getUserId(), familyId);
    }

    /**
     * 移动家庭空间文件
     */
    @RequestMapping("/changeFileFolder")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO changeFileFolder(
            HttpSession session,
            String fileIds,
            String filePid,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.changeFileFolder(fileIds, filePid, userDTO.getUserId(), familyId);
    }

    /**
     * 更新文件对关怀用户的可见性
     */
    @RequestMapping("/updateFileVisibleToCare")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO updateFileVisibleToCare(
            HttpSession session,
            String fileId,
            Integer visibleToCare,
            String familyId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        return familySpaceService.updateFileVisibleToCare(fileId, visibleToCare, userDTO.getUserId(), familyId);
    }
}

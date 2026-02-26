package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.annotation.VerifyParam;
import com.example.homic.constants.NormalConstants;
import com.example.homic.dto.frontEnd.QueryInfoDTO;
import com.example.homic.dto.redis.RedisSettingDTO;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.model.UserInfo;
import com.example.homic.services.AdminService;
import com.example.homic.services.FileService;
import com.example.homic.services.ManageService;
import com.example.homic.utils.RedisUtils;
import com.example.homic.vo.FileInfoVO;
import com.example.homic.vo.PageResultVO;
import com.example.homic.vo.ResponseVO;
import com.example.homic.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.Session;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;
import static com.example.homic.constants.NormalConstants.DEFAULT_PAGE_SIZE;
import static com.example.homic.constants.NormalConstants.SESSION_USER_INFO_KEY;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/18.13:58
 * 项目名：homic
 */
@RestController("adminController")
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    FileService fileService;
    @Autowired
    AdminService adminService;
    @Autowired
    ManageService manageService;
    @RequestMapping("/getSysSettings")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO getSysSettings(
            HttpSession session
    )
    {
        RedisSettingDTO redisSettingDTO = adminService.getSysSettings();
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(redisSettingDTO);
        return responseVO;
    }
    @RequestMapping("/saveSysSettings")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO saveSysSettings(
            HttpSession session,
            RedisSettingDTO settingInfo
    )
    {
        boolean is_success = adminService.saveSysSettings(settingInfo);
        ResponseVO responseVO = is_success ? new ResponseVO(SUCCESS_RES_STATUS,"修改系统设置成功") : new ResponseVO(FAIL_RES_STATUS,"修改系统设置失败");
        return responseVO;
    }
    @RequestMapping("/loadUserList")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO loadUserList(
            HttpSession session,
            String pageNo,
            String pageSize,
            String nickNameFuzzy,
            String status
    ) throws MyException {
        PageResultVO<UserInfoVO> userInfos = adminService.loadUserList(pageNo,pageSize,nickNameFuzzy,status);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS);
        responseVO.setData(userInfos);
        return responseVO;
    }
    @RequestMapping("/updateUserStatus")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO updateUserStatus(
            HttpSession session,
            String userId,
            String status
    )
    {
        boolean is_success = adminService.updateUserStatus(userId,status);
        ResponseVO responseVO = is_success ? new ResponseVO(SUCCESS_RES_STATUS,"修改用户状态成功") : new ResponseVO(FAIL_RES_STATUS,"修改用户状态失败");
        return responseVO;
    }
    @RequestMapping("/updateUserSpace")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO updateUserSpace(
            HttpSession session,
            String userId,
            Integer changeSpace
    )
    {
        boolean is_success = adminService.updateUserSpace(userId,changeSpace);
        ResponseVO responseVO = is_success ? new ResponseVO(SUCCESS_RES_STATUS,"修改用户空间成功") : new ResponseVO(FAIL_RES_STATUS,"修改用户空间失败");
        return responseVO;
    }
    @RequestMapping("/loadFileList")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO loadFileList(
            HttpSession session,
            Integer pageNo,
            Integer pageSize,
            String fileNameFuzzy,
            String filePid
    ) throws MyException {
        QueryInfoDTO queryInfoDTO = new QueryInfoDTO();
        queryInfoDTO.setFilePid(filePid);
        queryInfoDTO.setFileNameFuzzy(fileNameFuzzy);
        queryInfoDTO.setPageNo(pageNo);
        queryInfoDTO.setPageSize(pageSize);
        queryInfoDTO.setDelFlag(-1);//表示查询所有状态的文件
        queryInfoDTO.setQueryNickName(true);//表示结果中要返回发布人信息
        ResponseVO responseVO = fileService.loadDataList(queryInfoDTO);
        return responseVO;
    }
    @RequestMapping("/createDownloadUrl/{userId}/{fileId}")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO createDownloadUrl(
            HttpSession session,
            @PathVariable("fileId")//绑定路径参数
            String fileId,
            @PathVariable("userId")//绑定路径参数
            String userId
    ) throws MyException {
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
    @RequestMapping("/delFile")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public ResponseVO smashFile(
            HttpSession session,
            String fileIdAndUserIds
    ) throws Exception {
        String[] entryStrArray = fileIdAndUserIds.split(",");
        HashMap<String, List<String>> map = new HashMap<>();
        for(String entryStr : entryStrArray)
        {
            String userId = entryStr.split("_")[0];
            String fileId = entryStr.split("_")[1];
            List<String> fileIdsList = map.getOrDefault(userId,new ArrayList<>());
            fileIdsList.add(fileId);
            map.put(userId,fileIdsList);
        }
        boolean is_success = true;
        for(String userId : map.keySet())
        {
            List<String> fileIds = map.get(userId);
            is_success =  is_success && manageService.smashFile(fileIds,userId) ;
            if(!is_success)
                break;
        }
        return  is_success? new ResponseVO(SUCCESS_RES_STATUS,"文件删除成功") :new ResponseVO(FAIL_RES_STATUS,"文件删除错误");
    }
    @RequestMapping("/ts/getVideoInfo/{userId}/{msg}")
    @GlobalInteceptor(checkLogin = true,checkAdmin = true)
    public void getVideoInfo(
            HttpSession session,
            HttpServletResponse response,
            @PathVariable("userId")//绑定路径参数
            String userId,
            @PathVariable("msg")//绑定路径参数
            String msg
    ) throws Exception {
        fileService.getVideoInfo(response,msg,userId,session);
    }
}

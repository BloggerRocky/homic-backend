package com.example.homic.controller;

import com.example.homic.annotation.GlobalInteceptor;
import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.exception.MyException;
import com.example.homic.model.Permission;
import com.example.homic.services.PermissionService;
import com.example.homic.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;

/**
 * 权限控制器
 */
@RestController
@RequestMapping("/permission")
public class PermissionController extends CommonController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取用户在某个对象下的所有权限
     */
    @RequestMapping("/getUserPermissions")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getUserPermissions(HttpSession session, String userId, String objectId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        // 检查是否有权限查看（只能是创建者或本人）
        if (!userDTO.getUserId().equals(userId)) {
            // TODO: 检查当前用户是否是该家庭的创建者
        }

        List<Permission> permissions = permissionService.getUserPermissions(userId, objectId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取权限成功");

        // 转换为Map格式返回
        Map<String, Integer> permissionMap = new HashMap<>();
        permissionMap.put(PermissionService.PERMISSION_UPLOAD, 0);
        permissionMap.put(PermissionService.PERMISSION_MODIFY, 0);
        permissionMap.put(PermissionService.PERMISSION_DELETE, 0);

        for (Permission p : permissions) {
            permissionMap.put(p.getPermissionKey(), p.getPermissionValue());
        }

        responseVO.setData(permissionMap);
        return responseVO;
    }

    /**
     * 获取当前用户自己的权限
     */
    @RequestMapping("/getMyPermissions")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO getMyPermissions(HttpSession session, String objectId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        String userId = userDTO.getUserId();

        List<Permission> permissions = permissionService.getUserPermissions(userId, objectId);
        ResponseVO responseVO = new ResponseVO(SUCCESS_RES_STATUS, "获取权限成功");

        // 转换为Map格式返回
        Map<String, Integer> permissionMap = new HashMap<>();
        permissionMap.put(PermissionService.PERMISSION_UPLOAD, 0);
        permissionMap.put(PermissionService.PERMISSION_MODIFY, 0);
        permissionMap.put(PermissionService.PERMISSION_DELETE, 0);

        for (Permission p : permissions) {
            permissionMap.put(p.getPermissionKey(), p.getPermissionValue());
        }

        responseVO.setData(permissionMap);
        return responseVO;
    }

    /**
     * 设置用户权限（只有创建者可以调用）
     */
    @RequestMapping("/setPermission")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO setPermission(HttpSession session,
                                     String targetUserId,
                                     String permissionKey,
                                     int permissionValue,
                                     String objectId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        String currentUserId = userDTO.getUserId();

        // TODO: 检查当前用户是否是该家庭的创建者

        boolean success = permissionService.setPermission(targetUserId, permissionKey, permissionValue, objectId);
        if (success) {
            return new ResponseVO(SUCCESS_RES_STATUS, "权限设置成功");
        } else {
            return new ResponseVO(FAIL_RES_STATUS, "权限设置失败");
        }
    }

    /**
     * 批量设置用户权限（只有创建者可以调用）
     */
    @RequestMapping("/batchSetPermissions")
    @GlobalInteceptor(checkLogin = true)
    public ResponseVO batchSetPermissions(HttpSession session,
                                           String targetUserId,
                                           String upload,
                                           String modify,
                                           String delete,
                                           String objectId) throws MyException {
        SessionWebUserDTO userDTO = getUserInfoFromSession(session);
        String currentUserId = userDTO.getUserId();

        // TODO: 检查当前用户是否是该家庭的创建者

        Map<String, Integer> permissions = new HashMap<>();
        permissions.put(PermissionService.PERMISSION_UPLOAD, Integer.parseInt(upload));
        permissions.put(PermissionService.PERMISSION_MODIFY, Integer.parseInt(modify));
        permissions.put(PermissionService.PERMISSION_DELETE, Integer.parseInt(delete));

        boolean success = permissionService.batchSetPermissions(targetUserId, permissions, objectId);
        if (success) {
            return new ResponseVO(SUCCESS_RES_STATUS, "权限设置成功");
        } else {
            return new ResponseVO(FAIL_RES_STATUS, "权限设置失败");
        }
    }
}

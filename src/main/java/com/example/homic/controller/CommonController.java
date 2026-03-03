package com.example.homic.controller;

import com.example.homic.dto.session.SessionWebUserDTO;
import com.example.homic.model.UserInfo;

import javax.servlet.http.HttpSession;

import static com.example.homic.constants.NormalConstants.SESSION_USER_INFO_KEY;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/18.14:02
 * 项目名：homic
 */
public class CommonController {
    String getUserIdBySession(HttpSession session) {
        SessionWebUserDTO userInfo = (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
        String userId = userInfo.getUserId();
        return userId;
    }
    SessionWebUserDTO getUserInfoFromSession(HttpSession session) {
        return (SessionWebUserDTO) session.getAttribute(SESSION_USER_INFO_KEY);
    }
}

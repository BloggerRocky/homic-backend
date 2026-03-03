package com.example.homic.services;

import com.example.homic.vo.ResponseVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 关怀账号服务
 */
public interface CareAccountService {
    
    /**
     * 创建关怀账号
     */
    ResponseVO createCareAccount(String creatorId, String nickName, MultipartFile avatar);
    
    /**
     * 获取家庭的关怀账号列表
     */
    ResponseVO getCareAccountList(String userId);
    
    /**
     * 生成登录码
     */
    ResponseVO generateLoginCode(String creatorId, String careAccountId, Integer validType);
    
    /**
     * 通过登录码登录
     */
    ResponseVO loginByCode(String loginCode);
    
    /**
     * 删除关怀账号
     */
    ResponseVO deleteCareAccount(String creatorId, String careAccountId);
    
    /**
     * 禁用/启用关怀账号
     */
    ResponseVO toggleCareAccountStatus(String creatorId, String careAccountId, Integer status);
}

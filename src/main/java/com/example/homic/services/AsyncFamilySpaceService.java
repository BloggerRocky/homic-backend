package com.example.homic.services;

/**
 * 异步空间服务
 * 用于处理空间大小的异步更新任务
 */
public interface AsyncFamilySpaceService {
    
    /**
     * 异步更新家庭空间使用大小
     * 通过计算数据库中家庭所有文件的大小来更新，确保数据准确性
     * @param familyId 家庭ID
     */
    void asyncUpdateFamilySpaceUsage(String familyId);
    
    /**
     * 异步更新个人空间使用大小
     * 通过计算数据库中用户所有文件的大小来更新，确保数据准确性
     * @param userId 用户ID
     */
    void asyncUpdatePersonalSpaceUsage(String userId);
}

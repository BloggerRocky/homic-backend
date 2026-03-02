package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.FriendFileShare;
import com.example.homic.vo.FriendShareVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友文件分享Mapper
 */
@Mapper
public interface FriendFileShareMapper extends BaseMapper<FriendFileShare> {
    
    /**
     * 查询用户与好友之间的分享记录（双向）
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 分享记录列表
     */
    List<FriendShareVO> selectShareHistory(@Param("userId") String userId, 
                                           @Param("friendId") String friendId);
    
    /**
     * 查询用户发送的分享记录
     * @param userId 用户ID
     * @return 分享记录列表
     */
    List<FriendShareVO> selectSentShares(@Param("userId") String userId);
    
    /**
     * 查询用户接收的分享记录
     * @param userId 用户ID
     * @return 分享记录列表
     */
    List<FriendShareVO> selectReceivedShares(@Param("userId") String userId);
}

package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友申请记录Mapper
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    /**
     * 查询用户收到的好友请求（待审核状态）
     */
    @Select("SELECT * FROM friend_request WHERE friend_id = #{userId} AND status = 0 ORDER BY create_time DESC")
    List<FriendRequest> selectPendingRequests(String userId);

    /**
     * 查询用户发送的好友请求（待审核状态）
     */
    @Select("SELECT * FROM friend_request WHERE user_id = #{userId} AND status = 0 ORDER BY create_time DESC")
    List<FriendRequest> selectSentRequests(String userId);

    /**
     * 查询两个用户之间的最近一条申请记录
     */
    @Select("SELECT * FROM friend_request WHERE user_id = #{userId} AND friend_id = #{friendId} ORDER BY create_time DESC LIMIT 1")
    FriendRequest selectRequest(String userId, String friendId);

    /**
     * 查询用户收到的所有好友请求（包括已处理）
     */
    @Select("SELECT * FROM friend_request WHERE friend_id = #{userId} ORDER BY create_time DESC")
    List<FriendRequest> selectAllReceivedRequests(String userId);

    /**
     * 查询用户发送的所有好友请求（包括已处理）
     */
    @Select("SELECT * FROM friend_request WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<FriendRequest> selectAllSentRequests(String userId);

    /**
     * 查询用户待审核的请求数量
     */
    @Select("SELECT COUNT(*) FROM friend_request WHERE friend_id = #{userId} AND status = 0")
    Integer selectPendingRequestCount(String userId);
}

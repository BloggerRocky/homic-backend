package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.FriendRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友关系Mapper
 */
@Mapper
public interface FriendRelationMapper extends BaseMapper<FriendRelation> {

    /**
     * 查询用户的所有好友（已接受状态）
     */
    @Select("SELECT * FROM friend_relation WHERE user_id = #{userId} AND status = 1 ORDER BY update_time DESC")
    List<FriendRelation> selectFriendsByUserId(String userId);

    /**
     * 查询两个用户之间的关系
     */
    @Select("SELECT * FROM friend_relation WHERE user_id = #{userId} AND friend_id = #{friendId}")
    FriendRelation selectRelation(String userId, String friendId);

    /**
     * 查询用户的好友数量
     */
    @Select("SELECT COUNT(*) FROM friend_relation WHERE user_id = #{userId} AND status = 1")
    Integer selectFriendCount(String userId);

    /**
     * 按分组查询好友
     */
    @Select("SELECT * FROM friend_relation WHERE user_id = #{userId} AND group_id = #{groupId} AND status = 1 ORDER BY update_time DESC")
    List<FriendRelation> selectFriendsByGroup(String userId, String groupId);

    /**
     * 搜索好友
     */
    @Select("SELECT fr.* FROM friend_relation fr " +
            "LEFT JOIN user_info ui ON fr.friend_id = ui.user_id " +
            "WHERE fr.user_id = #{userId} AND fr.status = 1 " +
            "AND (ui.nick_name LIKE CONCAT('%', #{keyword}, '%') OR fr.remark LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY fr.update_time DESC")
    List<FriendRelation> searchFriends(String userId, String keyword);
}

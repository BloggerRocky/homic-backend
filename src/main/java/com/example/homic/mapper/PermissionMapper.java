package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 查询用户在某个对象下的所有权限
     */
    @Select("SELECT * FROM permission WHERE user_id = #{userId} AND object_id = #{objectId}")
    List<Permission> selectByUserIdAndObjectId(@Param("userId") String userId, @Param("objectId") String objectId);

    /**
     * 查询特定权限
     */
    @Select("SELECT * FROM permission WHERE user_id = #{userId} AND permission_key = #{permissionKey} AND object_id = #{objectId}")
    Permission selectByUserKeyAndObject(@Param("userId") String userId, @Param("permissionKey") String permissionKey, @Param("objectId") String objectId);
}

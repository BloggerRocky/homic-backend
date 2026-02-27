package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.family.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 家庭成员Mapper
 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {

    /**
     * 根据家庭ID查询所有成员
     */
    @Select("SELECT * FROM family_member WHERE family_id = #{familyId} AND status = 1 ORDER BY join_time ASC")
    List<FamilyMember> selectByFamilyId(String familyId);

    /**
     * 根据用户ID查询家庭成员信息
     */
    @Select("SELECT * FROM family_member WHERE user_id = #{userId} AND status = 1")
    FamilyMember selectByUserId(String userId);

    /**
     * 根据家庭ID和用户ID查询成员
     */
    @Select("SELECT * FROM family_member WHERE family_id = #{familyId} AND user_id = #{userId}")
    FamilyMember selectByFamilyIdAndUserId(String familyId, String userId);
}

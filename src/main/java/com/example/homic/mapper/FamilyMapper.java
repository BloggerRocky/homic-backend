package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.family.Family;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 家庭信息Mapper
 */
@Mapper
public interface FamilyMapper extends BaseMapper<Family> {

    /**
     * 根据所有者ID查询家庭
     */
    @Select("SELECT * FROM family WHERE owner_id = #{ownerId} AND status = 1")
    Family selectByOwnerId(String ownerId);
}

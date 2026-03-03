package com.example.homic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homic.model.CareAccountLoginCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 关怀账号登录码Mapper
 */
@Mapper
public interface CareAccountLoginCodeMapper extends BaseMapper<CareAccountLoginCode> {
    
    /**
     * 根据登录码查询
     */
    @Select("SELECT * FROM care_account_login_code WHERE login_code = #{loginCode} AND expire_time > NOW()")
    CareAccountLoginCode selectByLoginCode(String loginCode);
    
    /**
     * 根据用户ID查询有效的登录码
     */
    @Select("SELECT * FROM care_account_login_code WHERE user_id = #{userId} AND expire_time > NOW() ORDER BY create_time DESC LIMIT 1")
    CareAccountLoginCode selectValidByUserId(String userId);
}

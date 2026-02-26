package com.example.homic.dto.redis;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.13:34
 * 项目名：homic
 */
//这是用于将邮箱验证码信息存入redis的包装实体类
public class RedisEmailCodeDTO {
    private String srcEmail;
    private String desEmail;
    private String code;

    public RedisEmailCodeDTO() {
    }

    public RedisEmailCodeDTO(String srcEmail, String desEmail, String code) {
        this.srcEmail = srcEmail;
        this.desEmail = desEmail;
        this.code = code;
    }

    public String getSrcEmail() {
        return srcEmail;
    }

    public void setSrcEmail(String srcEmail) {
        this.srcEmail = srcEmail;
    }

    public String getDesEmail() {
        return desEmail;
    }

    public void setDesEmail(String desEmail) {
        this.desEmail = desEmail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

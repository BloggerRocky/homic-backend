package com.example.homic.dto.redis;

import lombok.Data;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/19.14:41
 * 项目名：homic
 */
//返回系统设置信息的包装类
@Data
public class RedisSettingDTO {
    public String registerEmailTitle;
    public String registerEmailContent;
    public Long userInitUseSpace;

    public RedisSettingDTO(String registerEmailTitle, String registerEmailContent, Long userInitUseSpace) {
        this.registerEmailTitle = registerEmailTitle;
        this.registerEmailContent = registerEmailContent;
        this.userInitUseSpace = userInitUseSpace;
    }

    public RedisSettingDTO() {
    }

}

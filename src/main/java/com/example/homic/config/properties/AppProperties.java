package com.example.homic.config.properties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.12:00
 * 项目名：homic
 */
//系统配置信息类，方便调用获得系统信息
@Data
@Component("appProperties")
public class AppProperties {
    @Value("${spring.mail.username}")
    private  String username;
    @Value("${spring.mail.password}")
    private  String password;
    @Value("${local.path}")
    private  String localPath;
}

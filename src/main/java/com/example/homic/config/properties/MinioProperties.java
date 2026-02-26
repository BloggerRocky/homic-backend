package com.example.homic.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/15.21:54
 * 项目名：homic
 */
@Data
@Component("minioProperties")
@ConfigurationProperties(prefix = "minio")//通过该注释快速根据配置文件注入属性
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

}

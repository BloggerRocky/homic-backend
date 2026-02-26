package com.example.homic.config;

import com.example.homic.config.properties.MinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;
/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/15.21:57
 * 项目名：homic
 */
//Minio配置类
@Configuration
public class MinioConfig {

    @Autowired
    private MinioProperties minioProperties;
    @Bean
    public MinioClient minioClient(){
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(minioProperties.getEndpoint())//配置网址端口
                        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())//配置通行证和密钥
                        .build();
        return minioClient;

    }
}

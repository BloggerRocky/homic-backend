package com.example.homic.config;

import com.example.homic.config.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;

import javax.annotation.PostConstruct;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/15.21:57
 * 项目名：homic
 */
//Minio配置类
@Configuration
public class MinioConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);

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

package com.example.homic.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.homic.model.FileInfo;
import com.example.homic.model.FileShare;
import com.example.homic.model.UserInfo;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.21:00
 * 项目名：homic
 */
@Configuration
public class BeanProxy {
    @Bean
    public static ModelMapper modelMapper(){
        return new ModelMapper();
    }
    @Bean
    public LambdaQueryWrapper<UserInfo> userInfoLqw() {
        return new LambdaQueryWrapper<>();
    }
    @Bean
    public LambdaQueryWrapper<FileInfo> fileInfoLqw() {
        return new LambdaQueryWrapper<>();
    }
    @Bean
    public LambdaQueryWrapper<FileShare> fileShareLqw() {
        return new LambdaQueryWrapper<>();
    }
}

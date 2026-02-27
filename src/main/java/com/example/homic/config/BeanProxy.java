package com.example.homic.config;

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
}

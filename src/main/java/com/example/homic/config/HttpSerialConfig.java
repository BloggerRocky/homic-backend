package com.example.homic.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/16.19:55
 * 项目名：homic
 */
////配置http请求的序列和反序列化方式，这里配置的是fastJson
//@Configuration
//public class HttpSerialConfig {
//
//    @Bean
//    public HttpMessageConverter<?> fastJsonHttpMessageConverter() {
//        return new FastJsonHttpMessageConverter();
//    }
//
//    @Bean
//    public Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter() {
//        return new Jaxb2RootElementHttpMessageConverter();
//    }
//}

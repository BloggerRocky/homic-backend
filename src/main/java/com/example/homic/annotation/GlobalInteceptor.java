package com.example.homic.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.17:17
 * 项目名：homic
 */
@Target({ElementType.METHOD})//注解的目标类型（本注解为方法）
@Retention(RetentionPolicy.RUNTIME)//注解的声明周期
@Documented//自定义注解的标识
@Mapping
public @interface GlobalInteceptor {
    boolean checkParams() default false;//是否校验参数，默认不校验
    boolean checkLogin() default false;//是否需要登录，默认不需要
    boolean checkAdmin() default false;//是否需要管理员权限，默认不需要
}

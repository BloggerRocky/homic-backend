package com.example.homic.annotation;

import com.example.homic.constants.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.17:27
 * 项目名：homic
 */
@Target({ElementType.PARAMETER,ElementType.FIELD})//目标类型（参数和属性）
@Retention(RetentionPolicy.RUNTIME)//
public @interface VerifyParam {
    int min() default  0; //最短长度，默认为-1
    int max() default  Integer.MAX_VALUE; //最大长度，默认为最大整数
    int length() default  -1; //限定长度，默认为-1,表示不限定
    boolean required() default false; //默认不是必填
    VerifyRegexEnum regex() default VerifyRegexEnum.NO; //校验方式，默认选用空正则校验
}

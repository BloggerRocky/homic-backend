package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.17:35
 * 项目名：homic
 */
//各种存放用于不同校验的正则表达式枚举类
public enum VerifyRegexEnum {
    NO(".*","不校验"),
    IP("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}","IP地址"),
    POSITIVE_INTEGER("^[0-9]*[1-9][0-9]*$","正整数"),
    NUMBER_LETTER_UNDER_LINE("^\\w+$","由数字、26个英文字母或者下划线组成的字符串"),
    EMAIL("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$","邮箱"),
    PHONE("(1[0-9])\\d{9}$","手机号码"),
    COMMON("^[a-ZA-Z0-9 \\u4e00-\\u9fa5]+$","数字,字母,中文,下划线"),
    PASSWORD("^( ?=.* \\d)( ?=.* [a-zA-Z])[\\da-zA-Z ~! @#$%^&*]{8,}$","只能是数字,字母,特殊字符 8-18位"),
    ACCOUNT("^[0-9a-ZA-Z]{1,}$","字母开头,由数字、英文字母或者下划线组成");

    private String regex;//正则表达式
    private String desc;//描述

    VerifyRegexEnum(String regex, String desc) {
        this.regex = regex;
        this.desc = desc;
    }

    public String getRegex() {
        return regex;
    }

    public String getDesc() {
        return desc;
    }
}

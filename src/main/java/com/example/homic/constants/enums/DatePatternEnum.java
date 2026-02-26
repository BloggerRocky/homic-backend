package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/16.1:47
 * 项目名：homic
 */
//Date日期模板枚举类
public  enum DatePatternEnum {
    //年-月-日-时-分-秒
    YEAR_MONTH_DAY_HOUR_MINUTE_SECOND("yyyy-MM-dd HH:mm:ss"),
    //年-月-日
    YEAR_MONTH_DAY("yyyy-MM-dd"),
    //年-月
    YEAR_MONTH("yyyy-MM");
    private String pattern;

    public String getPattern() {
        return pattern;
    }
    DatePatternEnum(String pattern) {
    this.pattern = pattern;
    }
}

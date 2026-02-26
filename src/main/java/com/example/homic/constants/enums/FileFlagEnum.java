package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.18:31
 * 项目名：homic
 */
//文件删除标记枚举类
public enum FileFlagEnum {
    DEL(0,"已删除"),
    RECYCLE (1,"回收站"),
    NORMAL(2,"正常") ;;

    private Integer flag;
    private String desc;

    FileFlagEnum (Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }
        public Integer getFlag () { return flag; }

        public String getDesc() { return desc;}
}

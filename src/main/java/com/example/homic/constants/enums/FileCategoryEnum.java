package com.example.homic.constants.enums;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.18:10
 * 项目名：homic
 */
//文件粗分类枚举类
public enum FileCategoryEnum {
    VIDEO(1, "video"),
    MUSIC(2,"music"),
    IMAGE(3,"image"),
    DOC(4,"doc"),
    OTHERS(5,"others");

    private Integer category;
    private String desc;

    FileCategoryEnum(Integer category,  String desc) {
        this.category = category;
        this.desc = desc;

    }

    public static Integer getCategoryByDesc (String desc) {
        for (FileCategoryEnum item : FileCategoryEnum.values()) {
            if (item.getDesc().equals(desc))
                return item.category;
        }
            return null;
        }
    public Integer getCategory () { return category;}
    public String getDesc () { return desc;}
}

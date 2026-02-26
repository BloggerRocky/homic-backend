package com.example.homic.constants.enums;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/16.0:38
 * 项目名：homic
 */
//文件细分类枚举类
public enum FileTypeEnum
{
        //1:视频2：音频3：图片4：pdf 5：word 6：excel7:txt 8：code 9：zip 10：其他文件
        VIDEO(FileCategoryEnum.VIDEO,1,new String[]{".mp4",".avi",".rmvb",".mkv",".mov"},"视频"),
        MUSIC(FileCategoryEnum.MUSIC,2,new String[]{".mp3",".wav",".wma",".mp2",".flac",".midi",".ra",".ape",".aac",".cda"},"音频"),
        IMAGE(FileCategoryEnum.IMAGE,3,new String[]{".jpeg",".jpg",".png",".gif",".bmp",".dds",".psd",".pdt",".webp",".xmp",".svg",".tiff"},"图片"),
        PDF(FileCategoryEnum.DOC, 4, new String []{".pdf"}, "pdf"),
        WORD(FileCategoryEnum.DOC, 5, new String []{".docx","doc"}, "word"),
        EXCEL(FileCategoryEnum.DOC,6, new String[]{".xlsx"},"excel"),
        txt( FileCategoryEnum.DOC,7, new String[]{".txt"},"txt文本"),
        PROGRAM(FileCategoryEnum.OTHERS,8,new String[]{".h",".c",".hpp",".hxx",".cpp",".cc",".c++",".cxx",".m",".o",".s",".dll",".cs",
        ".java",".class",".js",".ts",".css",".scss",".vue",".jsx",".sgl",".md",".json",".html",".xml"},"代码"),
        ZIP(FileCategoryEnum.OTHERS,9, new String[]{"rar",".zip",".7z",".cab",".arj",".lzh",".tar",".gz",".ace",".uue",".bz",".jar",".iso", ".mpq"},"压缩包"),
        OTHERS(FileCategoryEnum.OTHERS,10,new String[]{},"其他");
        public FileCategoryEnum category;
        public Integer type;
        public String[] suffixs;
        public String desc;
        FileTypeEnum(FileCategoryEnum category, Integer type, String[] suffixs, String desc) {
            this.category = category;
            this.type = type;
            this.suffixs = suffixs;
            this.desc = desc;
        }
         public static FileTypeEnum getTypeBySuffix(String suffix) {
                for(FileTypeEnum fileTypeEnum : FileTypeEnum.values())
                {
                        String[] suffixs = fileTypeEnum.suffixs;
                        if(ArrayUtils.contains(suffixs,suffix))
                                return fileTypeEnum;
                }
                return OTHERS;
        }

    public FileCategoryEnum getCategory() {
        return category;
    }

    public Integer getType() {
        return type;
    }

    public String[] getSuffixs() {
        return suffixs;
    }

    public String getDesc() {
        return desc;
    }
}

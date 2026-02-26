package com.example.homic.constants;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/11.20:38
 * 项目名：homic
 */
//用于存储返回体中常量信息的类
public class CodeConstants {
    public static final String SUCCESS_RES_STATUS = "success";
    public static final String FAIL_RES_STATUS = "fail";

    public static final String SUCCESS_INFO = "请求成功";
    public static final String FAIL_INFO = "请求失败";

    public static final int SUCCESS_RES_CODE = 200;
    public static final int FAIL_RES_CODE = 404;
    public  static final int OFFLINE_RES_CODE = 901;
    public static final int EXPIRE_RES_CODE = 902;//文件过期或失效
    public static final int NO_SPACE_RES_CODE = 904;//内存不足
    public static final int ERROR_RES_CODE = 400;
}

package com.example.homic.exception;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/12.23:34
 * 项目名：homic
 */
//自定义异常处理
public class MyException extends Exception{
    public String msg;
    public Integer code;
    public MyException(String msg,Integer code){
        this.msg = msg;
        this.code = code;
    }
}

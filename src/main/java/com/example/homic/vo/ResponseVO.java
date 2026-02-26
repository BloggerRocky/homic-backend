package com.example.homic.vo;

import com.example.homic.constants.CodeConstants;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;
import static com.example.homic.constants.CodeConstants.SUCCESS_RES_STATUS;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/11.21:27
 * 项目名：homic
 */
public class ResponseVO {
    public String status;
    public Integer code;
    public String info;
    public Object data;

    public ResponseVO(String status, Integer code, String info) {
        this.status = status;
        this.code = code;
        this.info = info;
    }

    public ResponseVO(String type)
    {
        init(type);
    }
    public ResponseVO(String type,String info)
    {
        init(type);
        this.info = info;
    }
    public void init(String type)
    {
        //初始化成功返回类
        if(type.equals(SUCCESS_RES_STATUS))
        {
            this.status = SUCCESS_RES_STATUS;
            this.code = CodeConstants.SUCCESS_RES_CODE;
            this.info = CodeConstants.SUCCESS_INFO;
            this.data = null;
        }
        //初始化失败返回类
        if(type.equals(FAIL_RES_STATUS))
        {
            this.status = CodeConstants.FAIL_RES_STATUS;
            this.code = CodeConstants.FAIL_RES_CODE;
            this.info = CodeConstants.FAIL_INFO;
            this.data = null;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

package com.example.homic.exception.handler;

import com.example.homic.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 作者：Rocky23318
 * 时间：2024.2024/7/14.14:05
 * 项目名：homic
 */
@Order(1)
@ControllerAdvice
public class BaseExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(value=Exception.class)
    @ResponseBody
    public ResponseVO exceptionHandler(Exception e){
            logger.error("系统异常",e);
        return new ResponseVO("fail");
    }
}

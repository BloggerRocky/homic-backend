package com.example.homic.exception.handler;
import com.example.homic.exception.MyException;
import com.example.homic.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.example.homic.constants.CodeConstants.FAIL_RES_STATUS;

//全局异常处理
@Order(0)//处理优先级，0为最高
@ControllerAdvice
public class MyExceptionHandler {
    private static Logger  logger = LoggerFactory.getLogger(MyExceptionHandler.class);
    @ExceptionHandler(value= MyException.class)
    @ResponseBody
    public ResponseVO myExceptionHandler(MyException e){
        return new ResponseVO(FAIL_RES_STATUS,e.code,e.msg);
    }
}

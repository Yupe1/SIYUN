package com.yupe.siyun.config;




import com.yupe.siyun.util.MyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.yupe.siyun.util.ResultData.error;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MyException.class)
    public Object myExceptionHandler(MyException e){
        return error(e.getCode(), e.getMessage());
    }
}

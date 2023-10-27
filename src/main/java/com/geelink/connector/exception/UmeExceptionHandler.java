package com.geelink.connector.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class UmeExceptionHandler {

    @ExceptionHandler(UmeException.class)
    @ResponseBody
    public ResultVO handlerUmeExceptionHandler(UmeException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RequestException.class)
    @ResponseBody
    public ResultVO handlerRequestExceptionHandler(RequestException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultVO handlerExceptionHandler(Exception e) {
        return ResultUtils.error(e.getMessage());
    }
}

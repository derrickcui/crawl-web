package com.geelink.connector.exception;

import lombok.Getter;

@Getter
public class RequestException extends RuntimeException{

    private Integer code;

    public RequestException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }

    public RequestException(ResultEnum resultEnum, String parameter) {
        super(resultEnum.getMessage() + parameter);
        this.code = resultEnum.getCode();
    }

    public RequestException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

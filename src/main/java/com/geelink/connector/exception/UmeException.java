package com.geelink.connector.exception;

import lombok.Getter;

@Getter
public class UmeException extends RuntimeException{

    private Integer code;

    public UmeException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }

    public UmeException(ResultEnum resultEnum, String parameter) {
        super(resultEnum.getMessage() + parameter);
        this.code = resultEnum.getCode();
    }

    public UmeException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

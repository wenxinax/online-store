package com.example.onlinestore.exceptions;

import lombok.Getter;

import java.io.Serial;

// 业务异常
@Getter
public class BizException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 5048846853577124297L;

    private final ErrorCode errorCode;
    private  Object[] params;

    public BizException(ErrorCode errorCode, Object... params) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.params = params;
    }
    public BizException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

}

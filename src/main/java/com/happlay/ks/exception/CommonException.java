package com.happlay.ks.exception;

import com.happlay.ks.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义异常类
 */
public class CommonException extends RuntimeException{

    /**
     * 错误码
     */
    @Getter
    private final int code;

    public CommonException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public CommonException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}

package com.happlay.ks.exception;

import com.happlay.ks.common.BaseResponse;
import com.happlay.ks.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//全局异常处理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.error("Validation failed: {}", errors);
        // 抛出自定义异常
        throw new CommonException(ErrorCode.PARAMS_ERROR, errors.toString());
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<BaseResponse<Object>> handleCommonExceptions(CommonException ex) {
        log.error("Common exception: {}", ex.getMessage());
        return new ResponseEntity<>(new BaseResponse<>(ex.getCode(), null, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleOtherExceptions(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new BaseResponse<>(500, null, "系统内部异常"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

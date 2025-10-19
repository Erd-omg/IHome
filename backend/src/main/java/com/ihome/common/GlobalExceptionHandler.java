package com.ihome.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                .findFirst().map(err -> err.getDefaultMessage()).orElse("参数校验失败");
        return ApiResponse.error(msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<String> handleBind(BindException ex) {
        String msg = ex.getAllErrors().stream().findFirst()
                .map(err -> err.getDefaultMessage()).orElse("参数绑定失败");
        return ApiResponse.error(msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<String> handleNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponse.error("请求体解析失败");
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class, DataIntegrityViolationException.class})
    public ApiResponse<String> handleSqlIntegrity(Exception ex) {
        log.error("SQL integrity violation", ex);
        return ApiResponse.error("数据库约束冲突: " + getRootCauseMessage(ex));
    }

    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<String> handleDataAccess(DataAccessException ex) {
        log.error("Data access error", ex);
        return ApiResponse.error("数据访问错误: " + getRootCauseMessage(ex));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleOther(Exception ex) {
        log.error("Unhandled server error", ex);
        return ApiResponse.error("服务器错误: " + getRootCauseMessage(ex));
    }

    private String getRootCauseMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? ex.getClass().getSimpleName() : cause.getMessage();
    }
}



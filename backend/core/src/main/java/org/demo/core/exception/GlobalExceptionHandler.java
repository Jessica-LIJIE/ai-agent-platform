package org.demo.core.exception;

import org.demo.core.api.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理插件不存在异常
     */
    @ExceptionHandler(PluginNotFoundException.class)
    public ApiResponse<Void> handlePluginNotFoundException(PluginNotFoundException e) {
        return ApiResponse.fail(404, e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.fail(400, e.getMessage());
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        return ApiResponse.fail(500, "服务器内部错误: " + e.getMessage());
    }
}

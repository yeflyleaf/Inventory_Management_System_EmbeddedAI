package com.example.backend.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(org.springframework.dao.DuplicateKeyException e) {
        return Result.error("操作失败: 该数据已存在 (可能是商品编码或名称重复)");
    }



    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
            return Result.error("操作失败: 该数据已存在 (可能是商品编码或名称重复)");
        }
        return Result.error("数据完整性错误，请检查输入数据");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 最后的兜底：如果错误信息包含 Duplicate entry，也返回中文提示
        if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("SQLIntegrityConstraintViolationException"))) {
            return Result.error("操作失败: 该数据已存在 (可能是商品编码或名称重复)");
        }
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }
}

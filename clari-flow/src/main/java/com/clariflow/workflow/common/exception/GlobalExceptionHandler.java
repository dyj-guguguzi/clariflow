package com.clariflow.workflow.common.exception;

import com.clariflow.workflow.model.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，拦截所有未处理的异常并将其转换为标准化的
 * {@link ApiResponse} 错误载荷。
 *
 * <p>将业务异常映射到对应的 HTTP 状态码（404、409、422），
 * 通用异常映射到 500。</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 {@link BusinessException} — 将错误码映射到 HTTP 状态码。
     *
     * @param ex 业务异常
     * @return 包含 ApiResponse 错误体的 ResponseEntity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        String message = String.format("[%s] %s", ex.getErrorCode().getCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getHttpStatus(), message);
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * 处理 {@code @Valid} 注解的请求体校验错误。
     *
     * @param ex 校验异常
     * @return 422，包含字段级别的错误详情
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        ApiResponse<Void> response = ApiResponse.error(422, "参数校验失败: " + message);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * 处理类型不匹配错误（例如查询参数中的无效枚举值）。
     *
     * @param ex 类型不匹配异常
     * @return 400，包含错误详情
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("参数 '%s' 的值 '%s' 无效", ex.getName(), ex.getValue());
        log.warn("Type mismatch: {}", message);
        ApiResponse<Void> response = ApiResponse.error(400, message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理格式错误的 JSON 请求体。
     *
     * @param ex 消息不可读异常
     * @return 400，包含错误详情
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(400, "请求体格式错误");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理所有未捕获的异常 — 返回 500。
     *
     * @param ex 异常
     * @return 500 内部服务器错误
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error(500, "服务器内部错误: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

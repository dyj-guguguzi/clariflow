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
 * Global exception handler that intercepts all unhandled exceptions
 * and converts them into standardized {@link ApiResponse} error payloads.
 *
 * <p>Maps business exceptions to their corresponding HTTP status codes
 * (404, 409, 422) and generic exceptions to 500.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link BusinessException} — maps error code to HTTP status.
     *
     * @param ex the business exception
     * @return ResponseEntity with ApiResponse error body
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        String message = String.format("[%s] %s", ex.getErrorCode().getCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getHttpStatus(), message);
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handles validation errors from {@code @Valid} annotated request bodies.
     *
     * @param ex the validation exception
     * @return 422 with field-level error details
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
     * Handles type mismatch errors (e.g. invalid enum values in query params).
     *
     * @param ex the type mismatch exception
     * @return 400 with error details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("参数 '%s' 的值 '%s' 无效", ex.getName(), ex.getValue());
        log.warn("Type mismatch: {}", message);
        ApiResponse<Void> response = ApiResponse.error(400, message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles malformed JSON request bodies.
     *
     * @param ex the message not readable exception
     * @return 400 with error details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(400, "请求体格式错误");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles all uncaught exceptions — returns 500.
     *
     * @param ex the exception
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error(500, "服务器内部错误: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

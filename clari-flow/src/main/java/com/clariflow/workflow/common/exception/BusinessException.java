package com.clariflow.workflow.common.exception;

import com.clariflow.workflow.common.ErrorCode;

/**
 * 业务异常，携带 {@link ErrorCode} 用于生成标准化的错误响应。
 *
 * <p>由 Service 层方法在业务规则被违反时抛出。
 * 由 {@link GlobalExceptionHandler} 捕获并生成结构化的
 * {@code ApiResponse} 错误载荷。</p>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 使用给定的错误码和详细消息构造 BusinessException。
     *
     * @param errorCode 标准错误码
     * @param message   人类可读的详细消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 使用错误码的默认消息构造 BusinessException。
     *
     * @param errorCode 标准错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 返回关联的错误码。
     *
     * @return ErrorCode 枚举值
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 返回此错误对应的 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}

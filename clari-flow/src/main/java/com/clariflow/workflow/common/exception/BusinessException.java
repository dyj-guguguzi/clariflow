package com.clariflow.workflow.common.exception;

import com.clariflow.workflow.common.ErrorCode;

/**
 * Business exception that carries an {@link ErrorCode} for standardized
 * error responses.
 *
 * <p>Thrown by service-layer methods when business rules are violated.
 * Caught by {@link GlobalExceptionHandler} to produce structured
 * {@code ApiResponse} error payloads.</p>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructs a BusinessException with the given error code and detail message.
     *
     * @param errorCode the standardized error code
     * @param message   human-readable detail message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a BusinessException using the error code's default message.
     *
     * @param errorCode the standardized error code
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Returns the associated error code.
     *
     * @return the ErrorCode enum value
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the HTTP status code for this error.
     *
     * @return HTTP status code
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}

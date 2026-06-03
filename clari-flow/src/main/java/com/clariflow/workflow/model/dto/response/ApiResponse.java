package com.clariflow.workflow.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API response wrapper.
 *
 * <p>All REST endpoints return responses wrapped in this type.
 * Success responses use code=0, errors use the appropriate error code.</p>
 *
 * <pre>
 *   Success: { "code": 0, "message": "success", "data": {...}, "timestamp": ... }
 *   Error:   { "code": 422, "message": "WF-002: ...", "data": null, "timestamp": ... }
 * </pre>
 *
 * @param <T> the type of the data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** Response code — 0 for success, error code for failures. */
    private Integer code;

    /** Human-readable message. */
    private String message;

    /** Response payload — null for error responses. */
    private T data;

    /** Unix timestamp (milliseconds) of the response. */
    private long timestamp;

    /**
     * Creates a success response with the given data.
     *
     * @param data the response payload
     * @param <T>  the data type
     * @return ApiResponse with code=0
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, System.currentTimeMillis());
    }

    /**
     * Creates an error response with the given code and message.
     *
     * @param code    the error code (HTTP status or application-specific)
     * @param message human-readable error message
     * @return ApiResponse with null data
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }
}

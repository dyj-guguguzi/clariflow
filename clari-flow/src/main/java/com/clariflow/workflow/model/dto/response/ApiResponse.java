package com.clariflow.workflow.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装类。
 *
 * <p>所有 REST 接口的响应都包装在此类型中。
 * 成功响应使用 code=0，错误响应使用相应的错误码。</p>
 *
 * <pre>
 *   Success: { "code": 0, "message": "success", "data": {...}, "timestamp": ... }
 *   Error:   { "code": 422, "message": "WF-002: ...", "data": null, "timestamp": ... }
 * </pre>
 *
 * @param <T> 数据载荷的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 响应码 — 0 表示成功，其他为错误码。 */
    private Integer code;

    /** 可读的消息文本。 */
    private String message;

    /** 响应载荷 — 错误响应时为 null。 */
    private T data;

    /** 响应的 Unix 时间戳（毫秒）。 */
    private long timestamp;

    /**
     * 创建带有给定数据的成功响应。
     *
     * @param data 响应载荷
     * @param <T>  数据类型
     * @return code=0 的 ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, System.currentTimeMillis());
    }

    /**
     * 创建带有给定错误码和消息的错误响应。
     *
     * @param code    错误码（HTTP 状态码或应用自定义）
     * @param message 可读的错误消息
     * @return data 为 null 的 ApiResponse
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }
}

package com.clariflow.workflow.common;

/**
 * 统一错误码枚举。
 *
 * <p>每个错误码映射到一个特定的 HTTP 状态码，
 * 并携带人类可读的默认消息，用于生成一致的 API 错误响应。</p>
 *
 * <pre>
 *   WF-001 (404): WORK_ITEM_NOT_FOUND     — 工作项不存在
 *   WF-002 (422): INVALID_TRANSITION       — 非法状态流转
 *   WF-003 (422): HIGH_CLARIFICATION_BLOCK — 存在未解决的高优先级澄清问题
 *   WF-004 (404): CLARIFICATION_NOT_FOUND  — 澄清问题不存在
 *   WF-005 (409): VERSION_CONFLICT         — 版本冲突，请重试
 * </pre>
 */
public enum ErrorCode {

    /** 工作项不存在 (404)。 */
    WF_001(404, "WF-001", "工作项不存在"),

    /** 非法状态流转 (422)。 */
    WF_002(422, "WF-002", "非法状态流转"),

    /** 存在未解决的高优先级澄清问题，阻塞流转 (422)。 */
    WF_003(422, "WF-003", "存在未解决的高优先级澄清问题"),

    /** 澄清问题不存在 (404)。 */
    WF_004(404, "WF-004", "澄清问题不存在"),

    /** 版本冲突 — 乐观锁失败 (409)。 */
    WF_005(409, "WF-005", "版本冲突，请重试"),

    /** 用户名已存在 (409)。 */
    WF_006(409, "WF-006", "用户名已存在"),

    /** 用户名或密码错误 (401)。 */
    WF_007(401, "WF-007", "用户名或密码错误");

    private final int httpStatus;
    private final String code;
    private final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    /**
     * 返回此错误对应的 HTTP 状态码。
     *
     * @return HTTP 状态码（404、409、422）
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * 返回错误码字符串。
     *
     * @return 错误码（例如 "WF-001"）
     */
    public String getCode() {
        return code;
    }

    /**
     * 返回默认的人类可读消息。
     *
     * @return 默认错误消息
     */
    public String getMessage() {
        return message;
    }
}

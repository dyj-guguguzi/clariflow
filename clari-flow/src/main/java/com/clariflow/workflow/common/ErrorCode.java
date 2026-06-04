package com.clariflow.workflow.common;

/**
 * Unified error code enumeration.
 *
 * <p>Each error code maps to a specific HTTP status code and carries
 * a human-readable default message for consistent API error responses.</p>
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

    /** Work item not found (404). */
    WF_001(404, "WF-001", "工作项不存在"),

    /** Invalid state transition (422). */
    WF_002(422, "WF-002", "非法状态流转"),

    /** High-severity unresolved clarification blocks transition (422). */
    WF_003(422, "WF-003", "存在未解决的高优先级澄清问题"),

    /** Clarification not found (404). */
    WF_004(404, "WF-004", "澄清问题不存在"),

    /** Version conflict — optimistic lock failure (409). */
    WF_005(409, "WF-005", "版本冲突，请重试"),

    /** Username already exists (409). */
    WF_006(409, "WF-006", "用户名已存在"),

    /** Invalid username or password (401). */
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
     * Returns the HTTP status code for this error.
     *
     * @return HTTP status code (404, 409, 422)
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns the error code string.
     *
     * @return error code (e.g. "WF-001")
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the default human-readable message.
     *
     * @return default error message
     */
    public String getMessage() {
        return message;
    }
}

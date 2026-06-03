package com.clariflow.workflow.common;

/**
 * 用户上下文 — 基于 ThreadLocal 存储当前请求的用户。
 *
 * <p>由 {@code UserContextInterceptor} 从请求头 {@code X-User} 中解析并设置，
 * 请求结束后自动清除，避免内存泄漏。</p>
 *
 * <p>使用方式：
 * <pre>{@code
 * String user = UserContext.getCurrentUser();
 * }</pre>
 * </p>
 */
public final class UserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    /** 设置当前用户 */
    public static void setCurrentUser(String user) {
        CURRENT_USER.set(user);
    }

    /** 获取当前用户，未设置时返回 "anonymous" */
    public static String getCurrentUser() {
        return CURRENT_USER.get() != null ? CURRENT_USER.get() : "anonymous";
    }

    /** 清除当前用户（请求结束时调用） */
    public static void clear() {
        CURRENT_USER.remove();
    }
}

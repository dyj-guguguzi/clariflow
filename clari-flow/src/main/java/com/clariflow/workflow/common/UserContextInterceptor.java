package com.clariflow.workflow.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 从请求头 {@code X-User} 中解析当前用户并设置到 {@link UserContext}。
 *
 * <p>前端只需在请求头中携带 <code>X-User: 用户名</code> 即可标识当前操作人。</p>
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserContextInterceptor.class);
    private static final String HEADER_USER = "X-User";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) {
        String user = request.getHeader(HEADER_USER);
        if (user != null && !user.trim().isEmpty()) {
            UserContext.setCurrentUser(user.trim());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        UserContext.clear();
    }
}

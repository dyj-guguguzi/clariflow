package com.clariflow.workflow.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器 — 从 {@code Authorization: Bearer <token>} 头解析用户。
 *
 * <p>白名单路径跳过认证：
 * <ul>
 *   <li>{@code /api/auth/**} — 登录/注册</li>
 *   <li>{@code /v3/api-docs} — OpenAPI 文档</li>
 *   <li>{@code /swagger-ui/**}, {@code /doc.html} — API 文档页面</li>
 *   <li>{@code /h2-console/**} — 数据库控制台</li>
 *   <li>静态资源 — {@code index.html}, {@code *.css}, {@code *.js} 等</li>
 * </ul>
 * </p>
 */
public class JwtAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                          FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        String path = httpReq.getRequestURI();

        // 白名单：登录/注册、文档、控制台、静态资源
        if (isWhitelisted(path)) {
            chain.doFilter(request, response);
            return;
        }

        // API 请求：从 Authorization 头解析 Token
        if (path.startsWith("/api/")) {
            String header = httpReq.getHeader(AUTH_HEADER);
            if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
                String token = header.substring(BEARER_PREFIX.length());
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    UserContext.setCurrentUser(username);
                }
            }
            // Token 无效也放行 — 具体权限由业务层判断
        }

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isWhitelisted(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/doc.html")
                || path.startsWith("/webjars")
                || path.startsWith("/h2-console")
                || path.startsWith("/favicon")
                || !path.startsWith("/api/");  // 静态资源
    }
}

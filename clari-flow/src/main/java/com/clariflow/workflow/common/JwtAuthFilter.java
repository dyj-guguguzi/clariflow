package com.clariflow.workflow.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * JWT 认证过滤器。
 *
 * <p><b>白名单</b>（无需认证）：登录页、注册/登录 API、文档、H2 控制台、图标。</p>
 *
 * <p><b>页面保护</b>（非 API 路径，如 /、/index.html）：
 * 检查 {@code clariflow_authenticated} Cookie，缺失则重定向到 {@code /login.html}。</p>
 *
 * <p><b>API 保护</b>（/api/**，白名单外）：
 * 从 {@code Authorization: Bearer <token>} 头解析并验证 JWT（含黑名单检查），
 * 无效则返回 401。</p>
 */
public class JwtAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_COOKIE = "clariflow_authenticated";

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

        // 白名单：登录/注册 API、文档、控制台、图标、登录页
        if (isWhitelisted(path)) {
            chain.doFilter(request, response);
            return;
        }

        // API 请求：从 Authorization 头解析 Token
        if (path.startsWith("/api/")) {
            String header = httpReq.getHeader(AUTH_HEADER);
            boolean authenticated = false;

            if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
                String token = header.substring(BEARER_PREFIX.length());
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    UserContext.setCurrentUser(username);
                    authenticated = true;
                }
            }

            if (!authenticated) {
                httpResp.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResp.setContentType("application/json;charset=UTF-8");
                httpResp.getWriter().write("{\"code\":401,\"message\":\"未登录或 Token 已失效\"}");
                return;
            }
        } else {
            // 非 API 受保护页面：检查认证 Cookie
            if (!hasAuthCookie(httpReq)) {
                httpResp.sendRedirect("/login.html");
                return;
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    /** 确认 Cookie 中存在认证标记 */
    private boolean hasAuthCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        return Arrays.stream(cookies)
                .anyMatch(c -> AUTH_COOKIE.equals(c.getName()) && "true".equals(c.getValue()));
    }

    /** 白名单：无需认证即可访问 */
    private boolean isWhitelisted(String path) {
        return path.startsWith("/api/auth/")       // 登录/注册/登出 API
                || "/login.html".equals(path)       // 登录页面
                || path.startsWith("/v3/api-docs")  // OpenAPI 文档
                || path.startsWith("/swagger-ui")   // Swagger UI
                || path.startsWith("/doc.html")     // Knife4j 文档
                || path.startsWith("/webjars")      // 前端静态资源
                || path.startsWith("/h2-console")   // H2 数据库控制台
                || path.startsWith("/favicon")      // 网站图标
                || isStaticResource(path);          // CSS / JS / 图片
    }

    /** 判断是否为前端静态资源（允许在登录页加载） */
    private boolean isStaticResource(String path) {
        return path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".ico")
                || path.endsWith(".svg")
                || path.endsWith(".woff")
                || path.endsWith(".woff2");
    }
}

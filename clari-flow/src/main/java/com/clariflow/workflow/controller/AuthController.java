package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.request.LoginRequest;
import com.clariflow.workflow.model.dto.request.RegisterRequest;
import com.clariflow.workflow.model.dto.response.ApiResponse;
import com.clariflow.workflow.model.dto.response.LoginResponse;
import com.clariflow.workflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "用户认证接口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletResponse response) {
        LoginResponse result = authService.register(request);
        setAuthCookie(response);
        return ApiResponse.success(result);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {
        LoginResponse result = authService.login(request);
        setAuthCookie(response);
        return ApiResponse.success(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                     HttpServletResponse response) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        authService.logout(token);
        clearAuthCookie(response);
        return ApiResponse.success(null);
    }

    /** 设置认证 Cookie，JwtAuthFilter 据此判断页面访问权限 */
    private void setAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("clariflow_authenticated", "true");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(86400); // 24h，与 JWT 过期时间一致
        response.addCookie(cookie);
    }

    /** 清除认证 Cookie */
    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("clariflow_authenticated", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

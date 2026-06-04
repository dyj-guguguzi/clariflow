package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.response.ApiResponse;
import com.clariflow.workflow.model.entity.User;
import com.clariflow.workflow.repository.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户列表的 REST 控制器——供前端可搜索下拉框使用。
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "用户查询接口")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    @Operation(summary = "获取用户列表", description = "返回系统中所有用户的用户名列表，用于前端下拉选择")
    public ApiResponse<List<String>> listUsers() {
        List<String> usernames = userMapper.selectList(null).stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        return ApiResponse.success(usernames);
    }
}

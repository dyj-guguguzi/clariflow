package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.request.ClarificationCreateRequest;
import com.clariflow.workflow.model.dto.request.ClarificationResolveRequest;
import com.clariflow.workflow.model.dto.response.ApiResponse;
import com.clariflow.workflow.model.dto.response.ClarificationResponse;
import com.clariflow.workflow.service.ClarificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 澄清问题管理的 REST 控制器。
 *
 * <p>提供对工作项澄清问题的创建、列表和
 * 解决接口。</p>
 */
@RestController
@RequestMapping("/api/work-items/{workItemId}/clarifications")
@Tag(name = "Clarification", description = "澄清问题管理接口")
public class ClarificationController {

    private final ClarificationService clarificationService;

    public ClarificationController(ClarificationService clarificationService) {
        this.clarificationService = clarificationService;
    }

    /**
     * 向工作项添加澄清问题。
     *
     * @param workItemId 父工作项 ID
     * @param request    澄清问题创建请求
     * @return 已创建的澄清问题
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "添加澄清问题", description = "为指定工作项添加一个澄清问题")
    public ApiResponse<ClarificationResponse> addClarification(
            @Parameter(description = "工作项ID") @PathVariable String workItemId,
            @Valid @RequestBody ClarificationCreateRequest request) {
        ClarificationResponse response = clarificationService.addClarification(workItemId, request);
        return ApiResponse.success(response);
    }

    /**
     * 列出工作项的所有澄清问题。
     *
     * @param workItemId 父工作项 ID
     * @return 澄清问题响应列表
     */
    @GetMapping
    @Operation(summary = "获取澄清问题列表", description = "获取指定工作项的所有澄清问题")
    public ApiResponse<List<ClarificationResponse>> getClarifications(
            @Parameter(description = "工作项ID") @PathVariable String workItemId) {
        List<ClarificationResponse> responses = clarificationService.getClarifications(workItemId);
        return ApiResponse.success(responses);
    }

    /**
     * 通过提供答案解决澄清问题。
     *
     * @param workItemId      父工作项 ID
     * @param clarificationId 要解决的澄清问题 ID
     * @param request         解决请求（答案）
     * @return 更新后的澄清问题
     */
    @PutMapping("/{clarificationId}/resolve")
    @Operation(summary = "解决澄清问题", description = "为澄清问题提供答案并将其标记为已解决")
    public ApiResponse<ClarificationResponse> resolveClarification(
            @Parameter(description = "工作项ID") @PathVariable String workItemId,
            @Parameter(description = "澄清问题ID") @PathVariable Long clarificationId,
            @Valid @RequestBody ClarificationResolveRequest request) {
        ClarificationResponse response = clarificationService.resolveClarification(
                workItemId, clarificationId, request);
        return ApiResponse.success(response);
    }
}

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
 * REST controller for clarification management.
 *
 * <p>Provides endpoints for creating, listing, and resolving
 * clarification questions on work items.</p>
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
     * Adds a clarification question to a work item.
     *
     * @param workItemId the parent work item ID
     * @param request    the clarification creation request
     * @return the created clarification
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
     * Lists all clarifications for a work item.
     *
     * @param workItemId the parent work item ID
     * @return list of clarification responses
     */
    @GetMapping
    @Operation(summary = "获取澄清问题列表", description = "获取指定工作项的所有澄清问题")
    public ApiResponse<List<ClarificationResponse>> getClarifications(
            @Parameter(description = "工作项ID") @PathVariable String workItemId) {
        List<ClarificationResponse> responses = clarificationService.getClarifications(workItemId);
        return ApiResponse.success(responses);
    }

    /**
     * Resolves a clarification by providing an answer.
     *
     * @param workItemId       the parent work item ID
     * @param clarificationId  the clarification ID to resolve
     * @param request          the resolve request (answer)
     * @return the updated clarification
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

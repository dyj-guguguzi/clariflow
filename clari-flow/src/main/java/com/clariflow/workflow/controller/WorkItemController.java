package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.request.TransitionRequest;
import com.clariflow.workflow.model.dto.request.WorkItemCreateRequest;
import com.clariflow.workflow.model.dto.request.WorkItemUpdateRequest;
import com.clariflow.workflow.model.dto.response.ApiResponse;
import com.clariflow.workflow.model.dto.response.TransitionResponse;
import com.clariflow.workflow.model.dto.response.WorkItemListItemResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;
import com.clariflow.workflow.service.TransitionService;
import com.clariflow.workflow.service.WorkItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for work item management.
 *
 * <p>Provides endpoints for CRUD operations on work items
 * and state transition management.</p>
 */
@RestController
@RequestMapping("/api/work-items")
@Tag(name = "WorkItem", description = "工作项管理接口")
public class WorkItemController {

    private final WorkItemService workItemService;
    private final TransitionService transitionService;

    public WorkItemController(WorkItemService workItemService,
                               TransitionService transitionService) {
        this.workItemService = workItemService;
        this.transitionService = transitionService;
    }

    /**
     * Creates a new work item.
     *
     * @param request the creation request
     * @return the created work item
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建工作项", description = "创建一个新的工作项，初始状态为 DRAFT")
    public ApiResponse<WorkItemResponse> createWorkItem(
            @Valid @RequestBody WorkItemCreateRequest request) {
        WorkItemResponse response = workItemService.createWorkItem(request);
        return ApiResponse.success(response);
    }

    /**
     * Lists work items with optional filtering.
     *
     * @param type     optional type filter
     * @param priority optional priority filter
     * @param status   optional status filter
     * @return filtered list of work items
     */
    @GetMapping
    @Operation(summary = "获取工作项列表", description = "获取工作项列表，支持按类型、优先级、状态筛选")
    public ApiResponse<List<WorkItemListItemResponse>> listWorkItems(
            @Parameter(description = "工作项类型: STORY, BUG, TASK")
            @RequestParam(required = false) String type,
            @Parameter(description = "优先级: P0, P1, P2")
            @RequestParam(required = false) String priority,
            @Parameter(description = "状态: DRAFT, ANALYZING, READY, IN_DEVELOPMENT, TESTING, COMPLETED")
            @RequestParam(required = false) String status) {
        List<WorkItemListItemResponse> responses = workItemService.listWorkItems(type, priority, status);
        return ApiResponse.success(responses);
    }

    /**
     * Retrieves a work item by ID with full details.
     *
     * @param id the work item ID
     * @return the full work item response
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取工作项详情", description = "根据ID获取工作项完整信息，包含澄清问题和流转历史")
    public ApiResponse<WorkItemResponse> getWorkItem(
            @Parameter(description = "工作项ID") @PathVariable String id) {
        WorkItemResponse response = workItemService.getWorkItem(id);
        return ApiResponse.success(response);
    }

    /**
     * Updates a work item's metadata.
     *
     * @param id      the work item ID
     * @param request the update request (includes version for optimistic locking)
     * @return the updated work item
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新工作项", description = "更新工作项基本信息，需要传入版本号进行乐观锁校验")
    public ApiResponse<WorkItemResponse> updateWorkItem(
            @Parameter(description = "工作项ID") @PathVariable String id,
            @Valid @RequestBody WorkItemUpdateRequest request) {
        WorkItemResponse response = workItemService.updateWorkItem(id, request);
        return ApiResponse.success(response);
    }

    /**
     * Executes a state transition for the work item.
     *
     * @param id      the work item ID
     * @param request the transition request (target status, reason, operator)
     * @return the updated work item with new status
     */
    @PostMapping("/{id}/transitions")
    @Operation(summary = "执行状态流转", description = "将工作项从当前状态流转到目标状态（状态机核心接口）")
    public ApiResponse<WorkItemResponse> executeTransition(
            @Parameter(description = "工作项ID") @PathVariable String id,
            @Valid @RequestBody TransitionRequest request) {
        WorkItemResponse response = transitionService.executeTransition(id, request);
        return ApiResponse.success(response);
    }

    /**
     * Retrieves the transition history for a work item.
     *
     * @param id the work item ID
     * @return list of transition records
     */
    @GetMapping("/{id}/transitions")
    @Operation(summary = "获取流转历史", description = "获取工作项的所有状态流转记录")
    public ApiResponse<List<TransitionResponse>> getTransitionHistory(
            @Parameter(description = "工作项ID") @PathVariable String id) {
        List<TransitionResponse> responses = transitionService.getTransitionHistory(id);
        return ApiResponse.success(responses);
    }

    /**
     * Deletes a work item and all associated data.
     *
     * @param id the work item ID
     * @return empty success response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除工作项", description = "删除工作项及其关联的澄清问题和流转记录")
    public ApiResponse<Void> deleteWorkItem(
            @Parameter(description = "工作项ID") @PathVariable String id) {
        workItemService.deleteWorkItem(id);
        return ApiResponse.success(null);
    }
}

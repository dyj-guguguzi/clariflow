package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.request.WorkItemCreateRequest;
import com.clariflow.workflow.model.dto.request.WorkItemUpdateRequest;
import com.clariflow.workflow.model.dto.response.WorkItemListItemResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;

import java.util.List;

/**
 * 工作项管理的服务接口。
 *
 * <p>提供工作项的 CRUD 操作。状态流转逻辑
 * 由 {@link TransitionService} 处理。</p>
 */
public interface WorkItemService {

    /**
     * 创建新的工作项，生成 ID（WI-xxx 格式）。
     * 初始状态始终为 {@code DRAFT}。
     *
     * @param request 创建请求
     * @return 已创建的工作项响应
     */
    WorkItemResponse createWorkItem(WorkItemCreateRequest request);

    /**
     * 根据 ID 获取工作项，包含澄清问题、
     * 流转历史和（可选）AI 分析。
     *
     * @param id 工作项 ID
     * @return 完整的工作项响应
     */
    WorkItemResponse getWorkItem(String id);

    /**
     * 列出工作项，支持可选筛选。
     *
     * @param type     可选类型筛选（STORY, BUG, TASK）
     * @param priority 可选优先级筛选（P0, P1, P2）
     * @param status   可选状态筛选
     * @return 精简的工作项响应列表
     */
    List<WorkItemListItemResponse> listWorkItems(String type, String priority, String status);

    /**
     * 更新工作项的元数据（标题、描述、类型等）。
     * 使用乐观锁——版本号必须匹配。
     *
     * @param id      工作项 ID
     * @param request 更新请求（包含版本号）
     * @return 已更新的工作项响应
     */
    WorkItemResponse updateWorkItem(String id, WorkItemUpdateRequest request);

    /**
     * 删除工作项及其所有关联的澄清问题和流转记录。
     *
     * @param id 工作项 ID
     */
    void deleteWorkItem(String id);
}

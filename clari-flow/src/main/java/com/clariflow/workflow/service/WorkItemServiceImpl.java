package com.clariflow.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.request.WorkItemCreateRequest;
import com.clariflow.workflow.model.dto.request.WorkItemUpdateRequest;
import com.clariflow.workflow.model.dto.response.*;
import com.clariflow.workflow.model.entity.Clarification;
import com.clariflow.workflow.model.entity.WorkItem;
import com.clariflow.workflow.model.entity.WorkItemTransition;
import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import com.clariflow.workflow.model.enums.WorkItemType;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.clariflow.workflow.repository.WorkItemTransitionMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link WorkItemService} 的实现。
 *
 * <p>处理工作项的 CRUD 操作，并组装
 * 包含关联数据的完整 {@link WorkItemResponse}。</p>
 */
@Service
public class WorkItemServiceImpl implements WorkItemService {

    private final WorkItemMapper workItemMapper;
    private final ClarificationMapper clarificationMapper;
    private final WorkItemTransitionMapper transitionMapper;

    /** 工作项 ID 前缀。 */
    private static final String ID_PREFIX = "WI-";

    public WorkItemServiceImpl(WorkItemMapper workItemMapper,
                               ClarificationMapper clarificationMapper,
                               WorkItemTransitionMapper transitionMapper) {
        this.workItemMapper = workItemMapper;
        this.clarificationMapper = clarificationMapper;
        this.transitionMapper = transitionMapper;
    }

    @Override
    @CacheEvict(value = "workItems", allEntries = true)
    public WorkItemResponse createWorkItem(WorkItemCreateRequest request) {
        WorkItem workItem = new WorkItem();
        workItem.setId(generateNextId());
        workItem.setTitle(request.getTitle());
        workItem.setDescription(request.getDescription());
        workItem.setType(request.getType() != null ? request.getType() : WorkItemType.STORY);
        workItem.setPriority(request.getPriority() != null ? request.getPriority() : Priority.P2);
        workItem.setStatus(WorkItemStatus.DRAFT);
        workItem.setAssignee(request.getAssignee());
        workItem.setTags(request.getTags() != null ? request.getTags() : Collections.emptyList());
        workItem.setAcceptanceCriteria(request.getAcceptanceCriteria() != null ? request.getAcceptanceCriteria() : Collections.emptyList());
        workItem.setRiskLevel(request.getRiskLevel());
        workItem.setVersion(1);

        LocalDateTime now = LocalDateTime.now();
        workItem.setCreatedAt(now);
        workItem.setUpdatedAt(now);

        workItemMapper.insert(workItem);
        return toWorkItemResponse(workItem);
    }

    @Override
    @Cacheable(value = "workItems", key = "'detail:' + #id")
    public WorkItemResponse getWorkItem(String id) {
        WorkItem workItem = findWorkItemById(id);
        return toWorkItemResponse(workItem);
    }

    @Override
    @Cacheable(value = "workItems", key = "'list:' + (#type ?: '') + ':' + (#priority ?: '') + ':' + (#status ?: '')")
    public List<WorkItemListItemResponse> listWorkItems(String type, String priority, String status) {
        LambdaQueryWrapper<WorkItem> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(WorkItem::getType, WorkItemType.valueOf(type.toUpperCase()));
        }
        if (priority != null && !priority.isEmpty()) {
            wrapper.eq(WorkItem::getPriority, Priority.valueOf(priority.toUpperCase()));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(WorkItem::getStatus, WorkItemStatus.valueOf(status.toUpperCase()));
        }
        wrapper.orderByDesc(WorkItem::getUpdatedAt);

        return workItemMapper.selectList(wrapper).stream()
                .map(this::toListItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "workItems", allEntries = true)
    public WorkItemResponse updateWorkItem(String id, WorkItemUpdateRequest request) {
        WorkItem existing = findWorkItemById(id);

        // 对非空字段应用更新
        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            existing.setType(request.getType());
        }
        if (request.getPriority() != null) {
            existing.setPriority(request.getPriority());
        }
        if (request.getAssignee() != null) {
            existing.setAssignee(request.getAssignee());
        }
        if (request.getTags() != null) {
            existing.setTags(request.getTags());
        }
        if (request.getAcceptanceCriteria() != null) {
            existing.setAcceptanceCriteria(request.getAcceptanceCriteria());
        }
        if (request.getRiskLevel() != null) {
            existing.setRiskLevel(request.getRiskLevel());
        }

        // 设置来自请求的版本号，用于乐观锁
        existing.setVersion(request.getVersion());
        existing.setUpdatedAt(LocalDateTime.now());

        int rows = workItemMapper.updateById(existing);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.WF_005, "版本冲突，请刷新后重试");
        }

        // 重新查询以获取更新后的版本号
        WorkItem updated = findWorkItemById(id);
        return toWorkItemResponse(updated);
    }

    @Override
    @CacheEvict(value = "workItems", allEntries = true)
    public void deleteWorkItem(String id) {
        findWorkItemById(id);

        // 删除关联的澄清问题
        clarificationMapper.delete(new LambdaQueryWrapper<Clarification>()
                .eq(Clarification::getWorkItemId, id));

        // 删除关联的流转记录
        transitionMapper.delete(new LambdaQueryWrapper<WorkItemTransition>()
                .eq(WorkItemTransition::getWorkItemId, id));

        // 删除工作项
        workItemMapper.deleteById(id);
    }

    // ─── 辅助方法 ────────────────────────────────────────────

    /**
     * 根据 ID 查找工作项，找不到则抛出 WF-001 错误。
     *
     * @param id 工作项 ID
     * @return 工作项实体
     * @throws BusinessException 如果找不到
     */
    WorkItem findWorkItemById(String id) {
        WorkItem workItem = workItemMapper.selectById(id);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + id + " 不存在");
        }
        return workItem;
    }

    /**
     * 生成下一个顺序工作项 ID（WI-xxx 格式）。
     *
     * @return 格式化后的 ID 字符串
     */
    private String generateNextId() {
        // 统计现有行数以确定下一个序号
        Long count = workItemMapper.selectCount(new QueryWrapper<>());
        long nextSeq = count + 1;
        return String.format(ID_PREFIX + "%03d", nextSeq);
    }

    /**
     * 将 WorkItem 实体转换为完整的 WorkItemResponse。
     *
     * @param workItem 实体
     * @return 包含澄清问题和流转记录的响应 DTO
     */
    WorkItemResponse toWorkItemResponse(WorkItem workItem) {
        // 获取关联的澄清问题
        List<Clarification> clarifications = clarificationMapper.selectList(
                new LambdaQueryWrapper<Clarification>()
                        .eq(Clarification::getWorkItemId, workItem.getId())
                        .orderByDesc(Clarification::getCreatedAt));

        List<ClarificationResponse> clarificationResponses = clarifications.stream()
                .map(this::toClarificationResponse)
                .collect(Collectors.toList());

        // 获取关联的流转记录
        List<WorkItemTransition> transitions = transitionMapper.selectList(
                new LambdaQueryWrapper<WorkItemTransition>()
                        .eq(WorkItemTransition::getWorkItemId, workItem.getId())
                        .orderByDesc(WorkItemTransition::getCreatedAt));

        List<TransitionResponse> transitionResponses = transitions.stream()
                .map(this::toTransitionResponse)
                .collect(Collectors.toList());

        return WorkItemResponse.builder()
                .id(workItem.getId())
                .title(workItem.getTitle())
                .description(workItem.getDescription())
                .type(workItem.getType())
                .priority(workItem.getPriority())
                .status(workItem.getStatus())
                .assignee(workItem.getAssignee())
                .tags(workItem.getTags())
                .acceptanceCriteria(workItem.getAcceptanceCriteria())
                .riskLevel(workItem.getRiskLevel())
                .version(workItem.getVersion())
                .createdAt(workItem.getCreatedAt())
                .updatedAt(workItem.getUpdatedAt())
                .clarifications(clarificationResponses)
                .transitions(transitionResponses)
                .aiAnalysis(null)
                .build();
    }

    /**
     * 将 WorkItem 实体转换为精简的列表项响应。
     *
     * @param workItem 实体
     * @return 列表项响应 DTO
     */
    private WorkItemListItemResponse toListItemResponse(WorkItem workItem) {
        return WorkItemListItemResponse.builder()
                .id(workItem.getId())
                .title(workItem.getTitle())
                .type(workItem.getType())
                .priority(workItem.getPriority())
                .status(workItem.getStatus())
                .assignee(workItem.getAssignee())
                .riskLevel(workItem.getRiskLevel())
                .updatedAt(workItem.getUpdatedAt())
                .build();
    }

    /**
     * 将 Clarification 实体转换为其响应 DTO。
     *
     * @param clarification 实体
     * @return 响应 DTO
     */
    private ClarificationResponse toClarificationResponse(Clarification clarification) {
        return ClarificationResponse.builder()
                .id(clarification.getId())
                .workItemId(clarification.getWorkItemId())
                .question(clarification.getQuestion())
                .severity(clarification.getSeverity())
                .status(clarification.getStatus())
                .answer(clarification.getAnswer())
                .createdAt(clarification.getCreatedAt())
                .resolvedAt(clarification.getResolvedAt())
                .build();
    }

    /**
     * 将 WorkItemTransition 实体转换为其响应 DTO。
     *
     * @param transition 实体
     * @return 响应 DTO
     */
    private TransitionResponse toTransitionResponse(WorkItemTransition transition) {
        return TransitionResponse.builder()
                .id(transition.getId())
                .workItemId(transition.getWorkItemId())
                .fromStatus(transition.getFromStatus())
                .toStatus(transition.getToStatus())
                .reason(transition.getReason())
                .operator(transition.getOperator())
                .createdAt(transition.getCreatedAt())
                .build();
    }
}

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
 * Implementation of {@link WorkItemService}.
 *
 * <p>Handles CRUD operations for work items and assembles
 * the full {@link WorkItemResponse} with related data.</p>
 */
@Service
public class WorkItemServiceImpl implements WorkItemService {

    private final WorkItemMapper workItemMapper;
    private final ClarificationMapper clarificationMapper;
    private final WorkItemTransitionMapper transitionMapper;

    /** Prefix for work item IDs. */
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

        // Apply updates to non-null fields
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

        // Set the version from the request for optimistic locking
        existing.setVersion(request.getVersion());
        existing.setUpdatedAt(LocalDateTime.now());

        int rows = workItemMapper.updateById(existing);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.WF_005, "版本冲突，请刷新后重试");
        }

        // Re-fetch to get the updated version number
        WorkItem updated = findWorkItemById(id);
        return toWorkItemResponse(updated);
    }

    // ─── Helper methods ────────────────────────────────────────────

    /**
     * Finds a work item by ID or throws WF-001.
     *
     * @param id the work item ID
     * @return the work item entity
     * @throws BusinessException if not found
     */
    WorkItem findWorkItemById(String id) {
        WorkItem workItem = workItemMapper.selectById(id);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + id + " 不存在");
        }
        return workItem;
    }

    /**
     * Generates the next sequential work item ID (WI-xxx format).
     *
     * @return formatted ID string
     */
    private String generateNextId() {
        // Count existing rows to determine the next sequence number
        Long count = workItemMapper.selectCount(new QueryWrapper<>());
        long nextSeq = count + 1;
        return String.format(ID_PREFIX + "%03d", nextSeq);
    }

    /**
     * Converts a WorkItem entity to a full WorkItemResponse.
     *
     * @param workItem the entity
     * @return the response DTO with clarifications and transitions
     */
    WorkItemResponse toWorkItemResponse(WorkItem workItem) {
        // Fetch related clarifications
        List<Clarification> clarifications = clarificationMapper.selectList(
                new LambdaQueryWrapper<Clarification>()
                        .eq(Clarification::getWorkItemId, workItem.getId())
                        .orderByDesc(Clarification::getCreatedAt));

        List<ClarificationResponse> clarificationResponses = clarifications.stream()
                .map(this::toClarificationResponse)
                .collect(Collectors.toList());

        // Fetch related transitions
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
     * Converts a WorkItem entity to a lightweight list item response.
     *
     * @param workItem the entity
     * @return the list item response DTO
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
     * Converts a Clarification entity to its response DTO.
     *
     * @param clarification the entity
     * @return the response DTO
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
     * Converts a WorkItemTransition entity to its response DTO.
     *
     * @param transition the entity
     * @return the response DTO
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

package com.clariflow.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.request.TransitionRequest;
import com.clariflow.workflow.model.dto.response.TransitionResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;
import com.clariflow.workflow.model.entity.Clarification;
import com.clariflow.workflow.model.entity.WorkItem;
import com.clariflow.workflow.model.entity.WorkItemTransition;
import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.clariflow.workflow.common.UserContext;
import com.clariflow.workflow.repository.WorkItemTransitionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TransitionService} — the state machine core.
 *
 * <p>All state transition logic is centralized here. The
 * {@link #executeTransition} method is {@code @Transactional}
 * to ensure atomicity of validation, update, and history recording.</p>
 */
@Service
public class TransitionServiceImpl implements TransitionService {

    private static final Logger log = LoggerFactory.getLogger(TransitionServiceImpl.class);

    private final WorkItemMapper workItemMapper;
    private final WorkItemTransitionMapper transitionMapper;
    private final ClarificationMapper clarificationMapper;
    private final WorkItemServiceImpl workItemService;

    public TransitionServiceImpl(WorkItemMapper workItemMapper,
                                  WorkItemTransitionMapper transitionMapper,
                                  ClarificationMapper clarificationMapper,
                                  WorkItemServiceImpl workItemService) {
        this.workItemMapper = workItemMapper;
        this.transitionMapper = transitionMapper;
        this.clarificationMapper = clarificationMapper;
        this.workItemService = workItemService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "workItems", allEntries = true)
    public WorkItemResponse executeTransition(String workItemId, TransitionRequest request) {
        // 1. Retrieve current work item
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        WorkItemStatus currentStatus = workItem.getStatus();
        WorkItemStatus targetStatus = request.getTargetStatus();

        // 2. Validate state transition legality (WF-002)
        if (!currentStatus.canTransitionTo(targetStatus)) {
            String msg = String.format("非法状态流转: %s → %s 不被允许", currentStatus, targetStatus);
            log.warn("Invalid transition: workItemId={}, {} -> {}", workItemId, currentStatus, targetStatus);
            throw new BusinessException(ErrorCode.WF_002, msg);
        }

        // 3. Check for HIGH+UNRESOLVED clarification blockers (WF-003)
        //    This check applies when transitioning TO READY or IN_DEVELOPMENT
        if (targetStatus == WorkItemStatus.READY || targetStatus == WorkItemStatus.IN_DEVELOPMENT) {
            long blockerCount = clarificationMapper.selectCount(
                    new LambdaQueryWrapper<Clarification>()
                            .eq(Clarification::getWorkItemId, workItemId)
                            .eq(Clarification::getSeverity, Severity.HIGH)
                            .eq(Clarification::getStatus, ClarificationStatus.UNRESOLVED));

            if (blockerCount > 0) {
                String msg = String.format("存在 %d 个未解决的高优先级澄清问题，无法进入 %s 状态",
                        blockerCount, targetStatus);
                log.warn("Clarification block: workItemId={}, target={}, blockerCount={}",
                        workItemId, targetStatus, blockerCount);
                throw new BusinessException(ErrorCode.WF_003, msg);
            }
        }

        // 4. Update status with optimistic locking (WF-005)
        WorkItemStatus previousStatus = currentStatus;
        workItem.setStatus(targetStatus);
        workItem.setUpdatedAt(LocalDateTime.now());
        // version field is already set and will be auto-incremented by MyBatis-Plus @Version

        int rows = workItemMapper.updateById(workItem);
        if (rows == 0) {
            log.warn("Version conflict: workItemId={}, version={}", workItemId, workItem.getVersion());
            throw new BusinessException(ErrorCode.WF_005, "版本冲突，请刷新后重试");
        }

        // 5. Record transition history
        WorkItemTransition transition = new WorkItemTransition();
        transition.setWorkItemId(workItemId);
        transition.setFromStatus(previousStatus);
        transition.setToStatus(targetStatus);
        String operator = request.getOperator() != null && !request.getOperator().isEmpty()
                ? request.getOperator() : UserContext.getCurrentUser();
        transition.setReason(request.getReason());
        transition.setOperator(operator);
        transition.setCreatedAt(LocalDateTime.now());
        transitionMapper.insert(transition);

        log.info("Transition executed: workItemId={}, {} -> {}, operator={}",
                workItemId, previousStatus, targetStatus, operator);

        // 6. Return updated WorkItemResponse (re-read to get latest version)
        return workItemService.toWorkItemResponse(workItemMapper.selectById(workItemId));
    }

    @Override
    public List<TransitionResponse> getTransitionHistory(String workItemId) {
        // Verify work item exists
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        List<WorkItemTransition> transitions = transitionMapper.selectList(
                new LambdaQueryWrapper<WorkItemTransition>()
                        .eq(WorkItemTransition::getWorkItemId, workItemId)
                        .orderByDesc(WorkItemTransition::getCreatedAt));

        return transitions.stream()
                .map(t -> TransitionResponse.builder()
                        .id(t.getId())
                        .workItemId(t.getWorkItemId())
                        .fromStatus(t.getFromStatus())
                        .toStatus(t.getToStatus())
                        .reason(t.getReason())
                        .operator(t.getOperator())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}

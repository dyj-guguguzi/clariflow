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
 * {@link TransitionService} 的实现——状态机核心。
 *
 * <p>所有状态流转逻辑集中在此处。
 * {@link #executeTransition} 方法标记为 {@code @Transactional}
 * 以确保验证、更新和历史记录写入的原子性。</p>
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
        // 1. 获取当前工作项
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        WorkItemStatus currentStatus = workItem.getStatus();
        WorkItemStatus targetStatus = request.getTargetStatus();

        // 2. 验证状态流转合法性（WF-002）
        if (!currentStatus.canTransitionTo(targetStatus)) {
            String msg = String.format("非法状态流转: %s → %s 不被允许", currentStatus, targetStatus);
            log.warn("Invalid transition: workItemId={}, {} -> {}", workItemId, currentStatus, targetStatus);
            throw new BusinessException(ErrorCode.WF_002, msg);
        }

        // 3. 检查是否有 HIGH+UNRESOLVED 的澄清阻塞（WF-003）
        //    此检查在目标状态为 READY 或 IN_DEVELOPMENT 时执行
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

        // 4. 使用乐观锁更新状态（WF-005）
        WorkItemStatus previousStatus = currentStatus;
        workItem.setStatus(targetStatus);
        workItem.setUpdatedAt(LocalDateTime.now());
        // version 字段已经设置，MyBatis-Plus 的 @Version 会自动递增

        int rows = workItemMapper.updateById(workItem);
        if (rows == 0) {
            log.warn("Version conflict: workItemId={}, version={}", workItemId, workItem.getVersion());
            throw new BusinessException(ErrorCode.WF_005, "版本冲突，请刷新后重试");
        }

        // 5. 记录流转历史
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

        // 6. 返回更新后的 WorkItemResponse（重新查询以获取最新版本）
        return workItemService.toWorkItemResponse(workItemMapper.selectById(workItemId));
    }

    @Override
    public List<TransitionResponse> getTransitionHistory(String workItemId) {
        // 验证工作项存在
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

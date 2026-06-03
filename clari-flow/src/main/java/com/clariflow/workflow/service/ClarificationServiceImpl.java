package com.clariflow.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.request.ClarificationCreateRequest;
import com.clariflow.workflow.model.dto.request.ClarificationResolveRequest;
import com.clariflow.workflow.model.dto.response.ClarificationResponse;
import com.clariflow.workflow.model.entity.Clarification;
import com.clariflow.workflow.model.entity.WorkItem;
import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ClarificationService}.
 *
 * <p>Manages clarification lifecycle: creation, listing, and resolution.
 * Ensures work item existence before any operation.</p>
 */
@Service
public class ClarificationServiceImpl implements ClarificationService {

    private static final Logger log = LoggerFactory.getLogger(ClarificationServiceImpl.class);

    private final ClarificationMapper clarificationMapper;
    private final WorkItemMapper workItemMapper;

    public ClarificationServiceImpl(ClarificationMapper clarificationMapper,
                                     WorkItemMapper workItemMapper) {
        this.clarificationMapper = clarificationMapper;
        this.workItemMapper = workItemMapper;
    }

    @Override
    public ClarificationResponse addClarification(String workItemId, ClarificationCreateRequest request) {
        // Verify work item exists
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        Clarification clarification = new Clarification();
        clarification.setWorkItemId(workItemId);
        clarification.setQuestion(request.getQuestion());
        clarification.setSeverity(request.getSeverity());
        clarification.setStatus(ClarificationStatus.UNRESOLVED);
        clarification.setCreatedAt(LocalDateTime.now());

        clarificationMapper.insert(clarification);

        log.info("Clarification added: workItemId={}, clarificationId={}, severity={}",
                workItemId, clarification.getId(), request.getSeverity());

        return toResponse(clarification);
    }

    @Override
    public List<ClarificationResponse> getClarifications(String workItemId) {
        // Verify work item exists
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        List<Clarification> clarifications = clarificationMapper.selectList(
                new LambdaQueryWrapper<Clarification>()
                        .eq(Clarification::getWorkItemId, workItemId)
                        .orderByDesc(Clarification::getCreatedAt));

        return clarifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ClarificationResponse resolveClarification(String workItemId, Long clarificationId,
                                                       ClarificationResolveRequest request) {
        // Verify work item exists
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        // Find the clarification
        Clarification clarification = clarificationMapper.selectById(clarificationId);
        if (clarification == null || !clarification.getWorkItemId().equals(workItemId)) {
            throw new BusinessException(ErrorCode.WF_004,
                    "澄清问题 " + clarificationId + " 不存在或不属于工作项 " + workItemId);
        }

        // Update to RESOLVED
        clarification.setStatus(ClarificationStatus.RESOLVED);
        clarification.setAnswer(request.getAnswer());
        clarification.setResolvedAt(LocalDateTime.now());
        clarificationMapper.updateById(clarification);

        log.info("Clarification resolved: workItemId={}, clarificationId={}", workItemId, clarificationId);

        return toResponse(clarification);
    }

    /**
     * Converts a Clarification entity to its response DTO.
     *
     * @param clarification the entity
     * @return the response DTO
     */
    private ClarificationResponse toResponse(Clarification clarification) {
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
}

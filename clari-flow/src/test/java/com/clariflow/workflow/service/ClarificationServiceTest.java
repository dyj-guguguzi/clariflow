package com.clariflow.workflow.service;

import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.request.TransitionRequest;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;
import com.clariflow.workflow.model.entity.Clarification;
import com.clariflow.workflow.model.entity.WorkItem;
import com.clariflow.workflow.model.enums.*;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.clariflow.workflow.repository.WorkItemTransitionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for HIGH-severity clarification blocking rules.
 *
 * <p>Verifies that transitions to READY and IN_DEVELOPMENT are blocked
 * when there are unresolved HIGH-severity clarifications, but allowed
 * when clarifications are resolved or of lower severity.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClarificationService — HIGH 级拦截规则测试")
class ClarificationServiceTest {

    @Mock
    private WorkItemMapper workItemMapper;

    @Mock
    private WorkItemTransitionMapper transitionMapper;

    @Mock
    private ClarificationMapper clarificationMapper;

    @Mock
    private WorkItemServiceImpl workItemService;

    @InjectMocks
    private TransitionServiceImpl transitionService;

    private WorkItem wi;

    @BeforeEach
    void setUp() {
        wi = new WorkItem();
        wi.setId("WI-001");
        wi.setTitle("Test Item");
        wi.setDescription("Test");
        wi.setType(WorkItemType.STORY);
        wi.setPriority(Priority.P1);
        wi.setStatus(WorkItemStatus.ANALYZING);
        wi.setAssignee("tester");
        wi.setTags(Collections.emptyList());
        wi.setAcceptanceCriteria(Collections.emptyList());
        wi.setRiskLevel(Severity.MEDIUM);
        wi.setVersion(1);
        wi.setCreatedAt(LocalDateTime.now());
        wi.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("存在 HIGH+UNRESOLVED 澄清时，ANALYZING→READY 被拦截 (WF-003)")
    void highUnresolvedBlocksAnalyzingToReady() {
        when(workItemMapper.selectById("WI-001")).thenReturn(wi);
        // Simulate 1 HIGH+UNRESOLVED clarification
        when(clarificationMapper.selectCount(any())).thenReturn(1L);

        TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "分析完成", "user1");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> transitionService.executeTransition("WI-001", req));
        assertEquals(ErrorCode.WF_003, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("高优先级澄清问题"));
    }

    @Test
    @DisplayName("存在 HIGH+UNRESOLVED 澄清时，READY→IN_DEVELOPMENT 被拦截 (WF-003)")
    void highUnresolvedBlocksReadyToInDevelopment() {
        wi.setStatus(WorkItemStatus.READY);
        when(workItemMapper.selectById("WI-001")).thenReturn(wi);
        when(clarificationMapper.selectCount(any())).thenReturn(1L);

        TransitionRequest req = new TransitionRequest(WorkItemStatus.IN_DEVELOPMENT, "开始开发", "dev1");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> transitionService.executeTransition("WI-001", req));
        assertEquals(ErrorCode.WF_003, ex.getErrorCode());
    }

    @Test
    @DisplayName("不存在 HIGH+UNRESOLVED 时，ANALYZING→READY 正常通过")
    void noBlockersAllowsAnalyzingToReady() {
        when(workItemMapper.selectById("WI-001")).thenReturn(wi);
        when(clarificationMapper.selectCount(any())).thenReturn(0L);
        when(transitionMapper.insert(any())).thenReturn(1);
        when(workItemMapper.updateById(any())).thenReturn(1);
        when(workItemService.toWorkItemResponse(any())).thenReturn(
                WorkItemResponse.builder().id("WI-001").status(WorkItemStatus.READY).build());

        TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "分析完成", "user1");
        WorkItemResponse result = transitionService.executeTransition("WI-001", req);

        assertNotNull(result);
        assertEquals(WorkItemStatus.READY, result.getStatus());
    }

    @Test
    @DisplayName("HIGH+UNRESOLVED 不拦截 ANALYZING→DRAFT (非 READY/IN_DEVELOPMENT)")
    void highUnresolvedDoesNotBlockAnalyzingToDraft() {
        when(workItemMapper.selectById("WI-001")).thenReturn(wi);
        // Even with blockers, going back to DRAFT is fine — no blocker check needed
        when(transitionMapper.insert(any())).thenReturn(1);
        when(workItemMapper.updateById(any())).thenReturn(1);
        when(workItemService.toWorkItemResponse(any())).thenReturn(
                WorkItemResponse.builder().id("WI-001").status(WorkItemStatus.DRAFT).build());

        TransitionRequest req = new TransitionRequest(WorkItemStatus.DRAFT, "重新评估", "user1");
        WorkItemResponse result = transitionService.executeTransition("WI-001", req);

        assertNotNull(result);
        assertEquals(WorkItemStatus.DRAFT, result.getStatus());
    }

    @Test
    @DisplayName("HIGH+UNRESOLVED 不拦截 OTHER→OTHER (非 READY/IN_DEVELOPMENT 目标)")
    void highUnresolvedDoesNotBlockOtherTransitions() {
        wi.setStatus(WorkItemStatus.IN_DEVELOPMENT);
        when(workItemMapper.selectById("WI-001")).thenReturn(wi);
        when(transitionMapper.insert(any())).thenReturn(1);
        when(workItemMapper.updateById(any())).thenReturn(1);
        when(workItemService.toWorkItemResponse(any())).thenReturn(
                WorkItemResponse.builder().id("WI-001").status(WorkItemStatus.TESTING).build());

        TransitionRequest req = new TransitionRequest(WorkItemStatus.TESTING, "开发完成进入测试", "dev1");
        WorkItemResponse result = transitionService.executeTransition("WI-001", req);

        assertNotNull(result);
        assertEquals(WorkItemStatus.TESTING, result.getStatus());
    }
}

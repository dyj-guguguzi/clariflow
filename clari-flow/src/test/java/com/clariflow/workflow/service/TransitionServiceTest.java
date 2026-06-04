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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TransitionServiceImpl} 的单元测试 —— 覆盖所有合法与非法状态流转、
 * HIGH 级别澄清拦截规则以及乐观锁冲突处理。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransitionService — 状态流转核心测试")
class TransitionServiceTest {

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

    private WorkItem createWorkItem(String id, WorkItemStatus status) {
        WorkItem wi = new WorkItem();
        wi.setId(id);
        wi.setTitle("Test " + id);
        wi.setDescription("Test description");
        wi.setType(WorkItemType.STORY);
        wi.setPriority(Priority.P2);
        wi.setStatus(status);
        wi.setAssignee("tester");
        wi.setTags(Collections.emptyList());
        wi.setAcceptanceCriteria(Collections.emptyList());
        wi.setRiskLevel(Severity.LOW);
        wi.setVersion(1);
        wi.setCreatedAt(LocalDateTime.now());
        wi.setUpdatedAt(LocalDateTime.now());
        return wi;
    }

    // ── 合法流转 ──

    @Nested
    @DisplayName("合法流转测试")
    class ValidTransitions {

        @BeforeEach
        void setupMocks() {
            when(transitionMapper.insert(any())).thenReturn(1);
            when(workItemMapper.updateById(any())).thenReturn(1);
            when(workItemService.toWorkItemResponse(any())).thenReturn(
                    WorkItemResponse.builder().id("WI-001").status(WorkItemStatus.ANALYZING).build());
        }

        @Test
        @DisplayName("DRAFT → ANALYZING")
        void draftToAnalyzing() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.DRAFT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "开始分析", "user1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
            assertEquals(WorkItemStatus.ANALYZING, result.getStatus());
        }

        @Test
        @DisplayName("ANALYZING → READY")
        void analyzingToReady() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.ANALYZING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);
            when(clarificationMapper.selectCount(any())).thenReturn(0L);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "分析完成", "user1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("ANALYZING → DRAFT (回退)")
        void analyzingBackToDraft() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.ANALYZING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.DRAFT, "需要重新评估", "user1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("READY → IN_DEVELOPMENT")
        void readyToInDevelopment() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.READY);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);
            when(clarificationMapper.selectCount(any())).thenReturn(0L);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.IN_DEVELOPMENT, "开始开发", "dev1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("READY → ANALYZING (回退)")
        void readyBackToAnalyzing() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.READY);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "需要重新分析", "user1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("IN_DEVELOPMENT → TESTING")
        void inDevelopmentToTesting() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.IN_DEVELOPMENT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.TESTING, "开发完成，进入测试", "dev1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("IN_DEVELOPMENT → READY (回退)")
        void inDevelopmentBackToReady() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.IN_DEVELOPMENT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);
            when(clarificationMapper.selectCount(any())).thenReturn(0L);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "需求变更，返回准备阶段", "dev1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("TESTING → COMPLETED")
        void testingToCompleted() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.TESTING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.COMPLETED, "测试通过", "qa1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }

        @Test
        @DisplayName("TESTING → IN_DEVELOPMENT (回退)")
        void testingBackToInDevelopment() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.TESTING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);
            when(clarificationMapper.selectCount(any())).thenReturn(0L);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.IN_DEVELOPMENT, "测试发现缺陷", "qa1");
            WorkItemResponse result = transitionService.executeTransition("WI-001", req);

            assertNotNull(result);
        }
    }

    // ── 非法流转 ──

    @Nested
    @DisplayName("非法流转测试")
    class InvalidTransitions {

        @Test
        @DisplayName("DRAFT → READY (非法: 必须经过 ANALYZING)")
        void draftToReadyInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.DRAFT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "跳过分析", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("非法状态流转"));
        }

        @Test
        @DisplayName("DRAFT → IN_DEVELOPMENT (非法)")
        void draftToInDevelopmentInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.DRAFT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.IN_DEVELOPMENT, "跳过所有步骤", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("DRAFT → TESTING (非法)")
        void draftToTestingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.DRAFT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.TESTING, "不可能", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("DRAFT → COMPLETED (非法)")
        void draftToCompletedInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.DRAFT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.COMPLETED, "一步到位", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("ANALYZING → IN_DEVELOPMENT (非法: 必须经过 READY)")
        void analyzingToInDevelopmentInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.ANALYZING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.IN_DEVELOPMENT, "跳过准备", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("ANALYZING → TESTING (非法)")
        void analyzingToTestingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.ANALYZING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.TESTING, "跳级", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("ANALYZING → COMPLETED (非法)")
        void analyzingToCompletedInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.ANALYZING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.COMPLETED, "不可能", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("READY → DRAFT (非法: 不可直接回到 DRAFT)")
        void readyToDraftInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.READY);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.DRAFT, "重新开始", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("READY → TESTING (非法)")
        void readyToTestingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.READY);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.TESTING, "跳过开发", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("READY → COMPLETED (非法)")
        void readyToCompletedInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.READY);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.COMPLETED, "不可能", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("IN_DEVELOPMENT → DRAFT (非法)")
        void inDevelopmentToDraftInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.IN_DEVELOPMENT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.DRAFT, "重来", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("IN_DEVELOPMENT → ANALYZING (非法: 不可直接回退到 ANALYZING)")
        void inDevelopmentToAnalyzingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.IN_DEVELOPMENT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "回退分析", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("IN_DEVELOPMENT → COMPLETED (非法)")
        void inDevelopmentToCompletedInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.IN_DEVELOPMENT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.COMPLETED, "跳过测试", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("TESTING → DRAFT (非法)")
        void testingToDraftInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.TESTING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.DRAFT, "重来", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("TESTING → ANALYZING (非法)")
        void testingToAnalyzingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.TESTING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "回退", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("TESTING → READY (非法)")
        void testingToReadyInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.TESTING);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "回退", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("COMPLETED → DRAFT (非法)")
        void completedToDraftInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.COMPLETED);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.DRAFT, "重来", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("COMPLETED → ANALYZING (非法)")
        void completedToAnalyzingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.COMPLETED);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "回退", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("COMPLETED → READY (非法)")
        void completedToReadyInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.COMPLETED);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.READY, "回退", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("COMPLETED → IN_DEVELOPMENT (非法)")
        void completedToInDevelopmentInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.COMPLETED);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.IN_DEVELOPMENT, "回退", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }

        @Test
        @DisplayName("COMPLETED → TESTING (非法: 已完成是终态)")
        void completedToTestingInvalid() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.COMPLETED);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);

            TransitionRequest req = new TransitionRequest(WorkItemStatus.TESTING, "回归测试", "qa1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_002, ex.getErrorCode());
        }
    }

    // ── 版本冲突 ──

    @Nested
    @DisplayName("版本冲突测试")
    class VersionConflict {

        @Test
        @DisplayName("乐观锁冲突返回 WF-005")
        void versionConflict() {
            WorkItem wi = createWorkItem("WI-001", WorkItemStatus.DRAFT);
            when(workItemMapper.selectById("WI-001")).thenReturn(wi);
            when(workItemMapper.updateById(any())).thenReturn(0); // 模拟冲突

            TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "尝试流转", "user1");
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> transitionService.executeTransition("WI-001", req));
            assertEquals(ErrorCode.WF_005, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("版本冲突"));
        }
    }

    // ── 工作项不存在 ──

    @Test
    @DisplayName("工作项不存在返回 WF-001")
    void workItemNotFound() {
        when(workItemMapper.selectById("NONEXISTENT")).thenReturn(null);

        TransitionRequest req = new TransitionRequest(WorkItemStatus.ANALYZING, "test", "user1");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> transitionService.executeTransition("NONEXISTENT", req));
        assertEquals(ErrorCode.WF_001, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("不存在"));
    }
}

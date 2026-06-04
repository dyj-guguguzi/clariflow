package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.request.TransitionRequest;
import com.clariflow.workflow.model.dto.request.WorkItemCreateRequest;
import com.clariflow.workflow.model.dto.request.WorkItemUpdateRequest;
import com.clariflow.workflow.model.dto.response.WorkItemListItemResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;
import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import com.clariflow.workflow.model.enums.WorkItemType;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.UserMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.clariflow.workflow.repository.WorkItemTransitionMapper;
import com.clariflow.workflow.service.TransitionService;
import com.clariflow.workflow.service.WorkItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 MockMvc 对 {@link WorkItemController} 进行集成测试。
 *
 * <p>测试工作项 CRUD 和状态流转的 REST API 端点，
 * 验证正确的 HTTP 状态码、响应结构和 API 契约合规性。</p>
 */
@WebMvcTest(value = WorkItemController.class,
        excludeAutoConfiguration = {
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        })
@DisplayName("WorkItemController — API 集成测试")
class WorkItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkItemService workItemService;

    @MockBean
    private TransitionService transitionService;

    // 抑制 MyBatis Mapper 自动扫描（来自 @MapperScan）
    @MockBean
    private WorkItemMapper workItemMapper;

    @MockBean
    private WorkItemTransitionMapper workItemTransitionMapper;

    @MockBean
    private ClarificationMapper clarificationMapper;

    @MockBean
    private UserMapper userMapper;

    // ── 辅助方法：构建 Mock WorkItemResponse ──

    private WorkItemResponse buildMockResponse(String id, WorkItemStatus status) {
        return WorkItemResponse.builder()
                .id(id)
                .title("Test Work Item")
                .description("Test description")
                .type(WorkItemType.STORY)
                .priority(Priority.P1)
                .status(status)
                .assignee("tester")
                .tags(Arrays.asList("tag1", "tag2"))
                .acceptanceCriteria(Collections.singletonList("AC1"))
                .riskLevel(Severity.MEDIUM)
                .version(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .clarifications(Collections.emptyList())
                .transitions(Collections.emptyList())
                .aiAnalysis(null)
                .build();
    }

    private WorkItemListItemResponse buildListItem(String id, WorkItemStatus status) {
        return WorkItemListItemResponse.builder()
                .id(id)
                .title("Test Item " + id)
                .type(WorkItemType.STORY)
                .priority(Priority.P1)
                .status(status)
                .assignee("tester")
                .riskLevel(Severity.MEDIUM)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── 测试方法 ──

    @Test
    @DisplayName("POST /api/work-items — 创建工作项成功返回 201")
    void createWorkItemShouldReturn201() throws Exception {
        WorkItemCreateRequest request = new WorkItemCreateRequest();
        request.setTitle("New Feature");
        request.setDescription("Implement new feature");
        request.setType(WorkItemType.STORY);
        request.setPriority(Priority.P1);
        request.setAssignee("dev1");
        request.setTags(Arrays.asList("feature", "backend"));
        request.setRiskLevel(Severity.MEDIUM);

        when(workItemService.createWorkItem(any(WorkItemCreateRequest.class)))
                .thenReturn(buildMockResponse("WI-003", WorkItemStatus.DRAFT));

        mockMvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("WI-003"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.type").value("STORY"));
    }

    @Test
    @DisplayName("POST /api/work-items — 缺少必填字段返回 422")
    void createWorkItemMissingTitleShouldReturn422() throws Exception {
        WorkItemCreateRequest request = new WorkItemCreateRequest();
        // title 为空 —— 应触发校验失败

        mockMvc.perform(post("/api/work-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /api/work-items — 获取列表成功")
    void listWorkItemsShouldReturn200() throws Exception {
        List<WorkItemListItemResponse> items = Arrays.asList(
                buildListItem("WI-001", WorkItemStatus.DRAFT),
                buildListItem("WI-002", WorkItemStatus.ANALYZING));

        when(workItemService.listWorkItems(isNull(), isNull(), isNull()))
                .thenReturn(items);

        mockMvc.perform(get("/api/work-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("WI-001"))
                .andExpect(jsonPath("$.data[1].id").value("WI-002"));
    }

    @Test
    @DisplayName("GET /api/work-items?status=DRAFT — 按状态筛选")
    void listWorkItemsWithStatusFilter() throws Exception {
        List<WorkItemListItemResponse> items = Collections.singletonList(
                buildListItem("WI-001", WorkItemStatus.DRAFT));

        when(workItemService.listWorkItems(isNull(), isNull(), eq("DRAFT")))
                .thenReturn(items);

        mockMvc.perform(get("/api/work-items").param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /api/work-items/{id} — 获取详情成功")
    void getWorkItemShouldReturn200() throws Exception {
        when(workItemService.getWorkItem("WI-001"))
                .thenReturn(buildMockResponse("WI-001", WorkItemStatus.DRAFT));

        mockMvc.perform(get("/api/work-items/WI-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("WI-001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.title").value("Test Work Item"));
    }

    @Test
    @DisplayName("GET /api/work-items/{id} — 不存在返回 404")
    void getWorkItemNotFoundShouldReturn404() throws Exception {
        when(workItemService.getWorkItem("NONEXISTENT"))
                .thenThrow(new com.clariflow.workflow.common.exception.BusinessException(
                        com.clariflow.workflow.common.ErrorCode.WF_001, "工作项 NONEXISTENT 不存在"));

        mockMvc.perform(get("/api/work-items/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("PUT /api/work-items/{id} — 更新工作项成功")
    void updateWorkItemShouldReturn200() throws Exception {
        WorkItemUpdateRequest request = new WorkItemUpdateRequest();
        request.setTitle("Updated Title");
        request.setPriority(Priority.P0);
        request.setVersion(1);

        when(workItemService.updateWorkItem(eq("WI-001"), any(WorkItemUpdateRequest.class)))
                .thenReturn(buildMockResponse("WI-001", WorkItemStatus.DRAFT));

        mockMvc.perform(put("/api/work-items/WI-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("WI-001"));
    }

    @Test
    @DisplayName("PUT /api/work-items/{id} — 缺少版本号返回 422")
    void updateWorkItemMissingVersionShouldReturn422() throws Exception {
        WorkItemUpdateRequest request = new WorkItemUpdateRequest();
        request.setTitle("Updated Title");
        // version 为 null —— 应触发校验失败

        mockMvc.perform(put("/api/work-items/WI-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/work-items/{id}/transitions — 状态流转成功")
    void executeTransitionShouldReturn200() throws Exception {
        TransitionRequest request = new TransitionRequest();
        request.setTargetStatus(WorkItemStatus.ANALYZING);
        request.setReason("开始分析");
        request.setOperator("user1");

        when(transitionService.executeTransition(eq("WI-001"), any(TransitionRequest.class)))
                .thenReturn(buildMockResponse("WI-001", WorkItemStatus.ANALYZING));

        mockMvc.perform(post("/api/work-items/WI-001/transitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ANALYZING"));
    }

    @Test
    @DisplayName("POST /api/work-items/{id}/transitions — 非法流转返回 422")
    void executeInvalidTransitionShouldReturn422() throws Exception {
        TransitionRequest request = new TransitionRequest();
        request.setTargetStatus(WorkItemStatus.COMPLETED);
        request.setReason("不可能");
        request.setOperator("user1");

        when(transitionService.executeTransition(eq("WI-001"), any(TransitionRequest.class)))
                .thenThrow(new com.clariflow.workflow.common.exception.BusinessException(
                        com.clariflow.workflow.common.ErrorCode.WF_002,
                        "非法状态流转: DRAFT → COMPLETED 不被允许"));

        mockMvc.perform(post("/api/work-items/WI-001/transitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(422));
    }

    @Test
    @DisplayName("POST /api/work-items/{id}/transitions — 版本冲突返回 409")
    void executeTransitionVersionConflictShouldReturn409() throws Exception {
        TransitionRequest request = new TransitionRequest();
        request.setTargetStatus(WorkItemStatus.ANALYZING);
        request.setReason("test");
        request.setOperator("user1");

        when(transitionService.executeTransition(eq("WI-001"), any(TransitionRequest.class)))
                .thenThrow(new com.clariflow.workflow.common.exception.BusinessException(
                        com.clariflow.workflow.common.ErrorCode.WF_005, "版本冲突，请刷新后重试"));

        mockMvc.perform(post("/api/work-items/WI-001/transitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("GET /api/work-items/{id}/transitions — 获取流转历史成功")
    void getTransitionHistoryShouldReturn200() throws Exception {
        when(transitionService.getTransitionHistory("WI-001"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/work-items/WI-001/transitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }
}

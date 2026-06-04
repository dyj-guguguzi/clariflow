package com.clariflow.workflow.controller;

import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.request.ClarificationCreateRequest;
import com.clariflow.workflow.model.dto.request.ClarificationResolveRequest;
import com.clariflow.workflow.model.dto.response.ClarificationResponse;
import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.UserMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.clariflow.workflow.repository.WorkItemTransitionMapper;
import com.clariflow.workflow.service.ClarificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * {@link ClarificationController} 的 API 测试。
 *
 * <p>覆盖全部 3 个端点：POST（创建）、GET（列表）、PUT（解决）。</p>
 */
@WebMvcTest(value = ClarificationController.class,
        excludeAutoConfiguration = {
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        })
@DisplayName("ClarificationController — API 集成测试")
class ClarificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClarificationService clarificationService;

    @MockBean
    private WorkItemMapper workItemMapper;

    @MockBean
    private ClarificationMapper clarificationMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private WorkItemTransitionMapper workItemTransitionMapper;

    // ── 辅助方法 ──

    private ClarificationResponse buildResponse(Long id, String workItemId) {
        return ClarificationResponse.builder()
                .id(id)
                .workItemId(workItemId)
                .question("测试澄清问题")
                .severity(Severity.HIGH)
                .status(ClarificationStatus.UNRESOLVED)
                .answer(null)
                .createdAt(LocalDateTime.now())
                .resolvedAt(null)
                .build();
    }

    private ClarificationResponse buildResolvedResponse(Long id, String workItemId) {
        return ClarificationResponse.builder()
                .id(id)
                .workItemId(workItemId)
                .question("已解决的澄清问题")
                .severity(Severity.HIGH)
                .status(ClarificationStatus.RESOLVED)
                .answer("这是答案")
                .createdAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
    }

    // ── POST: 添加澄清问题 ──

    @Nested
    @DisplayName("POST /api/work-items/{wiId}/clarifications — 添加澄清问题")
    class AddClarification {

        @Test
        @DisplayName("✅ 添加成功返回 201")
        void shouldReturn201() throws Exception {
            ClarificationCreateRequest req = new ClarificationCreateRequest("边界是否明确？", Severity.HIGH);

            when(clarificationService.addClarification(eq("WI-001"), any()))
                    .thenReturn(buildResponse(1L, "WI-001"));

            mockMvc.perform(post("/api/work-items/WI-001/clarifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.severity").value("HIGH"))
                    .andExpect(jsonPath("$.data.status").value("UNRESOLVED"));
        }

        @Test
        @DisplayName("❌ workItemId 不存在返回 404")
        void shouldReturn404WhenWorkItemNotFound() throws Exception {
            ClarificationCreateRequest req = new ClarificationCreateRequest("问题", Severity.MEDIUM);

            when(clarificationService.addClarification(eq("NONEXISTENT"), any()))
                    .thenThrow(new BusinessException(ErrorCode.WF_001, "工作项 NONEXISTENT 不存在"));

            mockMvc.perform(post("/api/work-items/NONEXISTENT/clarifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("❌ question 为空返回 422")
        void shouldReturn422WhenQuestionBlank() throws Exception {
            ClarificationCreateRequest req = new ClarificationCreateRequest("", Severity.HIGH);

            mockMvc.perform(post("/api/work-items/WI-001/clarifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("❌ severity 为 null 返回 422")
        void shouldReturn422WhenSeverityNull() throws Exception {
            ClarificationCreateRequest req = new ClarificationCreateRequest("问题", null);

            mockMvc.perform(post("/api/work-items/WI-001/clarifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("❌ question 超过 1000 字返回 422")
        void shouldReturn422WhenQuestionTooLong() throws Exception {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1001; i++) sb.append("A");
            ClarificationCreateRequest req = new ClarificationCreateRequest(sb.toString(), Severity.MEDIUM);

            mockMvc.perform(post("/api/work-items/WI-001/clarifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ── GET: 获取澄清列表 ──

    @Nested
    @DisplayName("GET /api/work-items/{wiId}/clarifications — 获取澄清列表")
    class GetClarifications {

        @Test
        @DisplayName("✅ 获取列表成功返回 200")
        void shouldReturn200() throws Exception {
            List<ClarificationResponse> list = Arrays.asList(
                    buildResponse(1L, "WI-001"),
                    buildResponse(2L, "WI-001"));
            when(clarificationService.getClarifications("WI-001")).thenReturn(list);

            mockMvc.perform(get("/api/work-items/WI-001/clarifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[1].id").value(2));
        }

        @Test
        @DisplayName("✅ 空列表返回 200 + 空数组")
        void shouldReturnEmptyArray() throws Exception {
            when(clarificationService.getClarifications("WI-001"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/work-items/WI-001/clarifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("❌ workItemId 不存在返回 404")
        void shouldReturn404WhenWorkItemNotFound() throws Exception {
            when(clarificationService.getClarifications("NONEXISTENT"))
                    .thenThrow(new BusinessException(ErrorCode.WF_001, "工作项 NONEXISTENT 不存在"));

            mockMvc.perform(get("/api/work-items/NONEXISTENT/clarifications"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }
    }

    // ── PUT: 解决澄清问题 ──

    @Nested
    @DisplayName("PUT …/clarifications/{cId}/resolve — 解决澄清问题")
    class ResolveClarification {

        @Test
        @DisplayName("✅ 解决成功返回 200 + status=RESOLVED")
        void shouldReturn200() throws Exception {
            ClarificationResolveRequest req = new ClarificationResolveRequest("已确认边界条件");

            when(clarificationService.resolveClarification(eq("WI-001"), eq(1L), any()))
                    .thenReturn(buildResolvedResponse(1L, "WI-001"));

            mockMvc.perform(put("/api/work-items/WI-001/clarifications/1/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                    .andExpect(jsonPath("$.data.answer").value("这是答案"))
                    .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty());
        }

        @Test
        @DisplayName("❌ clarificationId 不存在返回 404")
        void shouldReturn404WhenClarificationNotFound() throws Exception {
            ClarificationResolveRequest req = new ClarificationResolveRequest("答案");

            when(clarificationService.resolveClarification(eq("WI-001"), eq(999L), any()))
                    .thenThrow(new BusinessException(ErrorCode.WF_004, "澄清问题 999 不存在"));

            mockMvc.perform(put("/api/work-items/WI-001/clarifications/999/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("❌ answer 为空返回 422")
        void shouldReturn422WhenAnswerBlank() throws Exception {
            ClarificationResolveRequest req = new ClarificationResolveRequest("");

            mockMvc.perform(put("/api/work-items/WI-001/clarifications/1/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }
}

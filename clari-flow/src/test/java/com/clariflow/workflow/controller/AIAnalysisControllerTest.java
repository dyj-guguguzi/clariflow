package com.clariflow.workflow.controller;

import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.repository.ClarificationMapper;
import com.clariflow.workflow.repository.UserMapper;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.clariflow.workflow.repository.WorkItemTransitionMapper;
import com.clariflow.workflow.service.AIAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API tests for {@link AIAnalysisController}.
 *
 * <p>Covers POST /api/work-items/{id}/ai-analysis with success,
 * not-found, and fallback scenarios.</p>
 */
@WebMvcTest(value = AIAnalysisController.class,
        excludeAutoConfiguration = {
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        })
@DisplayName("AIAnalysisController — API 集成测试")
class AIAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIAnalysisService aiAnalysisService;

    @MockBean
    private WorkItemMapper workItemMapper;

    @MockBean
    private ClarificationMapper clarificationMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private WorkItemTransitionMapper workItemTransitionMapper;

    // ── Helpers ──

    private AIAnalysisResponse buildAnalysisResponse() {
        return AIAnalysisResponse.builder()
                .summary("需求摘要：工作项涉及状态流转，需关注边界条件。")
                .risks(Arrays.asList(
                        new AIAnalysisResponse.RiskItem(Severity.HIGH, "状态流转规则可能遗漏边界情况"),
                        new AIAnalysisResponse.RiskItem(Severity.MEDIUM, "缺少异常流程处理")))
                .suggestions(Arrays.asList(
                        "建议补充异常流程的回退规则",
                        "编写完整的状态流转单元测试",
                        "明确各状态的准入/准出标准"))
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    // ── Tests ──

    @Nested
    @DisplayName("POST /api/work-items/{id}/ai-analysis — 触发 AI 分析")
    class Analyze {

        @Test
        @DisplayName("✅ AI 分析成功返回 200 + 结构化结果")
        void shouldReturn200() throws Exception {
            when(aiAnalysisService.analyze("WI-001"))
                    .thenReturn(buildAnalysisResponse());

            mockMvc.perform(post("/api/work-items/WI-001/ai-analysis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.summary").isNotEmpty())
                    .andExpect(jsonPath("$.data.risks").isArray())
                    .andExpect(jsonPath("$.data.risks.length()").value(2))
                    .andExpect(jsonPath("$.data.risks[0].level").value("HIGH"))
                    .andExpect(jsonPath("$.data.risks[0].description").isNotEmpty())
                    .andExpect(jsonPath("$.data.suggestions").isArray())
                    .andExpect(jsonPath("$.data.suggestions.length()").value(3))
                    .andExpect(jsonPath("$.data.analyzedAt").isNotEmpty());
        }

        @Test
        @DisplayName("❌ workItemId 不存在返回 404")
        void shouldReturn404WhenWorkItemNotFound() throws Exception {
            when(aiAnalysisService.analyze("NONEXISTENT"))
                    .thenThrow(new BusinessException(ErrorCode.WF_001, "工作项 NONEXISTENT 不存在"));

            mockMvc.perform(post("/api/work-items/NONEXISTENT/ai-analysis"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("✅ AI 服务降级时仍返回 200 + fallback 内容")
        void shouldReturnFallbackOnError() throws Exception {
            AIAnalysisResponse fallback = AIAnalysisResponse.builder()
                    .summary("AI 服务暂不可用：DeepSeek API 调用失败")
                    .risks(Collections.singletonList(
                            new AIAnalysisResponse.RiskItem(Severity.HIGH, "AI 服务暂时不可用，请稍后重试")))
                    .suggestions(Arrays.asList(
                            "请手动检查需求边界条件",
                            "确认验收标准是否完整"))
                    .analyzedAt(LocalDateTime.now())
                    .build();

            when(aiAnalysisService.analyze("WI-001"))
                    .thenReturn(fallback);

            mockMvc.perform(post("/api/work-items/WI-001/ai-analysis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.summary").isNotEmpty())
                    .andExpect(jsonPath("$.data.risks[0].level").value("HIGH"))
                    .andExpect(jsonPath("$.data.suggestions").isArray());
        }
    }
}

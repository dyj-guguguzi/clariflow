package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;
import com.clariflow.workflow.model.dto.response.ApiResponse;
import com.clariflow.workflow.service.AIAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * AI 驱动的工作项分析 REST 控制器。
 *
 * <p>提供触发工作项 AI 分析的接口。
 * 分析生成结构化洞察，包括摘要、
 * 已识别风险点和可操作建议。</p>
 */
@RestController
@RequestMapping("/api/work-items/{workItemId}/ai-analysis")
@Tag(name = "AI Analysis", description = "AI 分析接口")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    public AIAnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * 触发对工作项的 AI 分析。
     *
     * @param workItemId 要分析的工作项 ID
     * @return 结构化分析结果
     */
    @PostMapping
    @Operation(summary = "触发AI分析", description = "对指定工作项进行AI分析，生成摘要、风险点和建议")
    public ApiResponse<AIAnalysisResponse> analyze(
            @Parameter(description = "工作项ID") @PathVariable String workItemId) {
        AIAnalysisResponse response = aiAnalysisService.analyze(workItemId);
        return ApiResponse.success(response);
    }
}

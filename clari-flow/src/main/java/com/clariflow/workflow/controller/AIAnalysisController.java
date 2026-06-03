package com.clariflow.workflow.controller;

import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;
import com.clariflow.workflow.model.dto.response.ApiResponse;
import com.clariflow.workflow.service.AIAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI-powered work item analysis.
 *
 * <p>Provides endpoints for triggering mock AI analysis on work items.
 * The analysis generates structured insights including a summary,
 * identified risks, and actionable suggestions.</p>
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
     * Triggers AI analysis for a work item.
     *
     * @param workItemId the work item ID to analyze
     * @return structured analysis result
     */
    @PostMapping
    @Operation(summary = "触发AI分析", description = "对指定工作项进行AI分析，生成摘要、风险点和建议")
    public ApiResponse<AIAnalysisResponse> analyze(
            @Parameter(description = "工作项ID") @PathVariable String workItemId) {
        AIAnalysisResponse response = aiAnalysisService.analyze(workItemId);
        return ApiResponse.success(response);
    }
}

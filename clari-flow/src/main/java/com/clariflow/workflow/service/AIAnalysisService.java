package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;

/**
 * AI 驱动的工作项分析服务接口。
 *
 * <p>分析工作项的标题和描述，生成结构化洞察，
 * 包括摘要、已识别风险
 * 和可操作建议。</p>
 */
public interface AIAnalysisService {

    /**
     * 分析工作项并生成结构化的 AI 洞察。
     *
     * @param workItemId 要分析的工作项 ID
     * @return 结构化分析响应
     */
    AIAnalysisResponse analyze(String workItemId);
}

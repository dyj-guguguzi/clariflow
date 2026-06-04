package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 分析结果响应。
 *
 * <p>包含对工作项的结构化模拟分析，包括摘要、识别的风险和可操作的建议。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {

    /** 分析概要摘要。 */
    private String summary;

    /** 识别的风险及严重程度。 */
    private List<RiskItem> risks;

    /** 可操作的建议。 */
    private List<String> suggestions;

    /** 分析执行的时间戳。 */
    private LocalDateTime analyzedAt;

    /**
     * AI 分析中识别的单个风险项。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskItem {

        /** 风险的严重程度。 */
        private Severity level;

        /** 风险描述。 */
        private String description;
    }
}

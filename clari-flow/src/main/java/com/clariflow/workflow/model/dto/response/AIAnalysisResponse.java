package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI analysis result response.
 *
 * <p>Contains a structured mock analysis of the work item including
 * a summary, identified risks, and actionable suggestions.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {

    /** Executive summary of the analysis. */
    private String summary;

    /** Identified risks with severity levels. */
    private List<RiskItem> risks;

    /** Actionable suggestions. */
    private List<String> suggestions;

    /** Timestamp when the analysis was performed. */
    private LocalDateTime analyzedAt;

    /**
     * A single risk item identified during AI analysis.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskItem {

        /** Severity level of the risk. */
        private Severity level;

        /** Description of the risk. */
        private String description;
    }
}

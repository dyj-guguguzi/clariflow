package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;

/**
 * Service interface for AI-powered work item analysis.
 *
 * <p>Analyzes a work item's title and description to generate
 * structured insights including a summary, identified risks,
 * and actionable suggestions.</p>
 */
public interface AIAnalysisService {

    /**
     * Analyzes a work item and generates structured AI insights.
     *
     * @param workItemId the work item ID to analyze
     * @return structured analysis response
     */
    AIAnalysisResponse analyze(String workItemId);
}

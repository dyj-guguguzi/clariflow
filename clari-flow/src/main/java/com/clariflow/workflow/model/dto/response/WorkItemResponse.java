package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import com.clariflow.workflow.model.enums.WorkItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full work item detail response — includes clarifications, transitions,
 * and optional AI analysis result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemResponse {

    /** Unique identifier, e.g. "WI-001". */
    private String id;

    /** Title / summary of the work item. */
    private String title;

    /** Detailed description. */
    private String description;

    /** Type of work item. */
    private WorkItemType type;

    /** Priority level. */
    private Priority priority;

    /** Current status. */
    private WorkItemStatus status;

    /** Assigned person. */
    private String assignee;

    /** Tags list. */
    private List<String> tags;

    /** Acceptance criteria list. */
    private List<String> acceptanceCriteria;

    /** Risk level. */
    private Severity riskLevel;

    /** Current optimistic lock version. */
    private Integer version;

    /** Creation timestamp. */
    private LocalDateTime createdAt;

    /** Last update timestamp. */
    private LocalDateTime updatedAt;

    /** Associated clarifications (may be empty). */
    private List<ClarificationResponse> clarifications;

    /** Transition history (may be empty). */
    private List<TransitionResponse> transitions;

    /** Latest AI analysis result (may be null). */
    private AIAnalysisResponse aiAnalysis;
}

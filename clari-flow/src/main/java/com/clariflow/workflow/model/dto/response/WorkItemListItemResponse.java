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

/**
 * Lightweight work item response for list views.
 *
 * <p>Excludes heavy fields like description, tags, acceptance criteria,
 * clarifications, and transitions to optimize list performance.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemListItemResponse {

    /** Unique identifier. */
    private String id;

    /** Title / summary. */
    private String title;

    /** Type of work item. */
    private WorkItemType type;

    /** Priority level. */
    private Priority priority;

    /** Current status. */
    private WorkItemStatus status;

    /** Assigned person. */
    private String assignee;

    /** Risk level. */
    private Severity riskLevel;

    /** Last update timestamp. */
    private LocalDateTime updatedAt;
}

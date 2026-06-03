package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a single state transition record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionResponse {

    /** Transition record ID. */
    private Long id;

    /** Parent work item ID. */
    private String workItemId;

    /** Status before the transition. */
    private WorkItemStatus fromStatus;

    /** Status after the transition. */
    private WorkItemStatus toStatus;

    /** Reason or comment. */
    private String reason;

    /** Person who performed the transition. */
    private String operator;

    /** Timestamp of the transition. */
    private LocalDateTime createdAt;
}

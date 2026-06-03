package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a clarification question/answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationResponse {

    /** Clarification ID. */
    private Long id;

    /** Parent work item ID. */
    private String workItemId;

    /** The question text. */
    private String question;

    /** Severity level. */
    private Severity severity;

    /** Current status. */
    private ClarificationStatus status;

    /** Answer text — null if still UNRESOLVED. */
    private String answer;

    /** Creation timestamp. */
    private LocalDateTime createdAt;

    /** Resolution timestamp — null if UNRESOLVED. */
    private LocalDateTime resolvedAt;
}

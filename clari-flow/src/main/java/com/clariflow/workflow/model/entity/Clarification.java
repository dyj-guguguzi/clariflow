package com.clariflow.workflow.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clarification entity — maps to the {@code clarification} table.
 *
 * <p>Represents a question that needs to be resolved before a work item
 * can progress to certain states (READY, IN_DEVELOPMENT). HIGH-severity
 * + UNRESOLVED clarifications act as blockers.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("clarification")
public class Clarification {

    /** Auto-generated primary key. */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Foreign key to the parent work item. */
    private String workItemId;

    /** The clarification question text. */
    private String question;

    /** Severity of the clarification: HIGH, MEDIUM, or LOW. */
    private Severity severity;

    /** Current status: UNRESOLVED or RESOLVED. */
    private ClarificationStatus status;

    /** Answer text — populated when resolved. */
    private String answer;

    /** Timestamp when the clarification was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the clarification was resolved (nullable). */
    private LocalDateTime resolvedAt;
}

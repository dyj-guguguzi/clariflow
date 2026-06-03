package com.clariflow.workflow.model.enums;

/**
 * Clarification status enumeration.
 *
 * <p>Clarifications start as UNRESOLVED and transition to RESOLVED
 * when an answer is provided.</p>
 */
public enum ClarificationStatus {

    /** Unresolved — the clarification question has not been answered yet. */
    UNRESOLVED,

    /** Resolved — the clarification question has been answered. */
    RESOLVED
}

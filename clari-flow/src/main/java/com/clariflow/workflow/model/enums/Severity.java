package com.clariflow.workflow.model.enums;

/**
 * Severity / risk level enumeration.
 *
 * <p>Used for both work item risk level and clarification severity.
 * HIGH severity clarifications with UNRESOLVED status block transitions
 * to READY or IN_DEVELOPMENT.</p>
 */
public enum Severity {

    /** High severity — blocking issue requiring immediate attention. */
    HIGH,

    /** Medium severity — should be addressed but not blocking. */
    MEDIUM,

    /** Low severity — minor concern, can be deferred. */
    LOW
}

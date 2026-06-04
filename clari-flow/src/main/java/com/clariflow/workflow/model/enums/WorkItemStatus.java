package com.clariflow.workflow.model.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Work item status enumeration with built-in state transition rules.
 *
 * <p>This is the single source of truth for all valid state transitions.
 * Each status defines exactly which target statuses it can transition to.
 * Any transition not listed here is considered invalid and will result in
 * a {@code WF-002 INVALID_TRANSITION} error.</p>
 *
 * <pre>
 *   DRAFT          → ANALYZING
 *   ANALYZING      → READY, DRAFT
 *   READY          → IN_DEVELOPMENT, ANALYZING
 *   IN_DEVELOPMENT → TESTING, READY
 *   TESTING        → COMPLETED, IN_DEVELOPMENT
 *   COMPLETED      → (终态，不可再流转)
 * </pre>
 */
public enum WorkItemStatus {

    /** Initial draft state — work item has been created but not yet analyzed. */
    DRAFT {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Collections.singletonList(ANALYZING);
        }
    },

    /** Analysis in progress — requirements are being clarified. */
    ANALYZING {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(READY, DRAFT);
        }
    },

    /** Ready for development — all clarifications resolved. */
    READY {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(IN_DEVELOPMENT, ANALYZING);
        }
    },

    /** Development in progress — implementation is underway. */
    IN_DEVELOPMENT {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(TESTING, READY);
        }
    },

    /** Testing in progress — quality assurance is being performed. */
    TESTING {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(COMPLETED, IN_DEVELOPMENT);
        }
    },

    /** Completed — work item has been fully delivered. Terminal state, no further transitions allowed. */
    COMPLETED {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Collections.emptyList();
        }
    };

    /**
     * Returns the list of statuses this status can transition to.
     *
     * @return unmodifiable list of allowed target statuses
     */
    public abstract List<WorkItemStatus> getAllowedTargets();

    /**
     * Checks whether transitioning to the given target status is valid.
     *
     * @param target the desired target status
     * @return {@code true} if the transition is allowed
     */
    public boolean canTransitionTo(WorkItemStatus target) {
        return getAllowedTargets().contains(target);
    }
}

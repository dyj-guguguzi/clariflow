package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.request.TransitionRequest;
import com.clariflow.workflow.model.dto.response.TransitionResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;

import java.util.List;

/**
 * Service interface for state transition management.
 *
 * <p>This is the core business logic that enforces state machine rules:
 * valid transitions, clarification blocking rules, and optimistic locking.</p>
 */
public interface TransitionService {

    /**
     * Executes a state transition for the given work item.
     *
     * <p>In a single transaction, this method:
     * <ol>
     *   <li>Retrieves the current work item</li>
     *   <li>Validates the transition is allowed (WF-002)</li>
     *   <li>Checks for HIGH+UNRESOLVED clarification blockers (WF-003)
     *       when target is READY or IN_DEVELOPMENT</li>
     *   <li>Updates the work item status with optimistic locking</li>
     *   <li>Records the transition in history</li>
     *   <li>Returns the updated work item response</li>
     * </ol>
     *
     * @param workItemId the work item ID
     * @param request    the transition request (target status, reason, operator)
     * @return the updated work item response
     */
    WorkItemResponse executeTransition(String workItemId, TransitionRequest request);

    /**
     * Retrieves the transition history for a work item.
     *
     * @param workItemId the work item ID
     * @return list of transition records in descending chronological order
     */
    List<TransitionResponse> getTransitionHistory(String workItemId);
}

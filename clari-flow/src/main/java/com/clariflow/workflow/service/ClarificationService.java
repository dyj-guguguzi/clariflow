package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.request.ClarificationCreateRequest;
import com.clariflow.workflow.model.dto.request.ClarificationResolveRequest;
import com.clariflow.workflow.model.dto.response.ClarificationResponse;

import java.util.List;

/**
 * Service interface for clarification question management.
 *
 * <p>Clarifications help identify and resolve ambiguities in work item
 * requirements before development begins. HIGH-severity unresolved
 * clarifications block transitions to READY and IN_DEVELOPMENT.</p>
 */
public interface ClarificationService {

    /**
     * Adds a clarification question to a work item.
     *
     * @param workItemId the parent work item ID
     * @param request    the clarification creation request
     * @return the created clarification response
     */
    ClarificationResponse addClarification(String workItemId, ClarificationCreateRequest request);

    /**
     * Retrieves all clarifications for a work item.
     *
     * @param workItemId the parent work item ID
     * @return list of clarification responses
     */
    List<ClarificationResponse> getClarifications(String workItemId);

    /**
     * Resolves a clarification by providing an answer.
     *
     * @param workItemId       the parent work item ID
     * @param clarificationId  the clarification ID
     * @param request          the resolve request (answer)
     * @return the updated clarification response
     */
    ClarificationResponse resolveClarification(String workItemId, Long clarificationId,
                                                ClarificationResolveRequest request);
}

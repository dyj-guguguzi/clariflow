package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.request.WorkItemCreateRequest;
import com.clariflow.workflow.model.dto.request.WorkItemUpdateRequest;
import com.clariflow.workflow.model.dto.response.WorkItemListItemResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;

import java.util.List;

/**
 * Service interface for work item management.
 *
 * <p>Provides CRUD operations for work items. The state transition
 * logic is handled by {@link TransitionService}.</p>
 */
public interface WorkItemService {

    /**
     * Creates a new work item with a generated ID (WI-xxx format).
     * The initial status is always {@code DRAFT}.
     *
     * @param request the creation request
     * @return the created work item response
     */
    WorkItemResponse createWorkItem(WorkItemCreateRequest request);

    /**
     * Retrieves a work item by its ID, including clarifications,
     * transition history, and (optionally) AI analysis.
     *
     * @param id the work item ID
     * @return the full work item response
     */
    WorkItemResponse getWorkItem(String id);

    /**
     * Lists work items with optional filtering.
     *
     * @param type     optional type filter (STORY, BUG, TASK)
     * @param priority optional priority filter (P0, P1, P2)
     * @param status   optional status filter
     * @return list of lightweight work item responses
     */
    List<WorkItemListItemResponse> listWorkItems(String type, String priority, String status);

    /**
     * Updates a work item's metadata (title, description, type, etc.).
     * Uses optimistic locking — version must match.
     *
     * @param id      the work item ID
     * @param request the update request (includes version)
     * @return the updated work item response
     */
    WorkItemResponse updateWorkItem(String id, WorkItemUpdateRequest request);
}

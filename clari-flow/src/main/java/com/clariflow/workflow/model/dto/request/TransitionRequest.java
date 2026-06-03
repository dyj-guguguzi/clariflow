package com.clariflow.workflow.model.dto.request;

import com.clariflow.workflow.model.enums.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Request DTO for executing a state transition.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransitionRequest {

    /** Desired target status. */
    @NotNull(message = "目标状态不能为空")
    private WorkItemStatus targetStatus;

    /** Reason or comment for the transition. */
    private String reason;

    /** Person performing the transition. */
    private String operator;
}

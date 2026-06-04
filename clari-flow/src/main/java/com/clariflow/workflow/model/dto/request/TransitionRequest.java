package com.clariflow.workflow.model.dto.request;

import com.clariflow.workflow.model.enums.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 执行状态流转的请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransitionRequest {

    /** 期望的目标状态。 */
    @NotNull(message = "目标状态不能为空")
    private WorkItemStatus targetStatus;

    /** 流转原因或备注。 */
    private String reason;

    /** 执行流转的人员。 */
    private String operator;
}

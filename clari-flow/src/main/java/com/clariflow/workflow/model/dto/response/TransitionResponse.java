package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 单条状态流转记录的响应 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionResponse {

    /** 流转记录 ID。 */
    private Long id;

    /** 父工作项 ID。 */
    private String workItemId;

    /** 流转前的状态。 */
    private WorkItemStatus fromStatus;

    /** 流转后的状态。 */
    private WorkItemStatus toStatus;

    /** 原因或备注。 */
    private String reason;

    /** 执行流转的人员。 */
    private String operator;

    /** 流转时间戳。 */
    private LocalDateTime createdAt;
}

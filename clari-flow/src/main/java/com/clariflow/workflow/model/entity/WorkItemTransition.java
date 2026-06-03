package com.clariflow.workflow.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Work item transition entity — maps to the {@code work_item_transition} table.
 *
 * <p>Records every state transition for audit trail and history purposes.
 * Each record captures the source status, target status, reason, and operator.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work_item_transition")
public class WorkItemTransition {

    /** Auto-generated primary key. */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Foreign key to the parent work item. */
    private String workItemId;

    /** Status before the transition. */
    private WorkItemStatus fromStatus;

    /** Status after the transition. */
    private WorkItemStatus toStatus;

    /** Reason or comment for the transition. */
    private String reason;

    /** Person who performed the transition. */
    private String operator;

    /** Timestamp when the transition occurred. */
    private LocalDateTime createdAt;
}

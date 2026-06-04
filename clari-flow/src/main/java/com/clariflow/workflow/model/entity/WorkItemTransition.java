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
 * 工作项流转实体 — 映射到 {@code work_item_transition} 表。
 *
 * <p>记录每次状态流转，用于审计追踪和历史记录。
 * 每条记录包含源状态、目标状态、原因和操作人。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work_item_transition")
public class WorkItemTransition {

    /** 自增主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父工作项的外键。 */
    private String workItemId;

    /** 流转前的状态。 */
    private WorkItemStatus fromStatus;

    /** 流转后的状态。 */
    private WorkItemStatus toStatus;

    /** 流转原因或备注。 */
    private String reason;

    /** 执行流转的人员。 */
    private String operator;

    /** 流转发生的时间戳。 */
    private LocalDateTime createdAt;
}

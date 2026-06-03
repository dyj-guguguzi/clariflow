package com.clariflow.workflow.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.clariflow.workflow.common.handler.JsonListTypeHandler;
import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import com.clariflow.workflow.model.enums.WorkItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Work item entity — maps to the {@code work_item} table.
 *
 * <p>Represents a unit of work (story, bug, or task) that progresses
 * through a defined state flow from DRAFT to COMPLETED. Uses optimistic
 * locking via the {@code version} field.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work_item")
public class WorkItem {

    /** Unique identifier, e.g. "WI-001". */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** Title / summary of the work item. */
    private String title;

    /** Detailed description of the work. */
    private String description;

    /** Type of work: STORY, BUG, or TASK. */
    private WorkItemType type;

    /** Priority level: P0, P1, or P2. */
    private Priority priority;

    /** Current status in the state flow. */
    private WorkItemStatus status;

    /** Assigned person (free-text string in MVP). */
    private String assignee;

    /** Tags as a list, stored as JSON array in VARCHAR column. */
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> tags;

    /** Acceptance criteria as a list, stored as JSON array in VARCHAR column. */
    @TableField(value = "acceptance_criteria", typeHandler = JsonListTypeHandler.class)
    private List<String> acceptanceCriteria;

    /** Overall risk level of the work item. */
    private Severity riskLevel;

    /** Optimistic lock version — incremented on each update. */
    @Version
    private Integer version;

    /** Timestamp when the work item was created. */
    private LocalDateTime createdAt;

    /** Timestamp of the last update. */
    private LocalDateTime updatedAt;
}

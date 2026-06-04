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
 * 工作项实体 — 映射到 {@code work_item} 表。
 *
 * <p>表示一个工作单元（需求、缺陷或任务），在定义的状态流中从 DRAFT 流转到 COMPLETED。
 * 通过 {@code version} 字段使用乐观锁机制。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work_item")
public class WorkItem {

    /** 唯一标识符，例如 "WI-001"。 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 工作项的标题/摘要。 */
    private String title;

    /** 工作项的详细描述。 */
    private String description;

    /** 工作类型：STORY、BUG 或 TASK。 */
    private WorkItemType type;

    /** 优先级：P0、P1 或 P2。 */
    private Priority priority;

    /** 状态流中的当前状态。 */
    private WorkItemStatus status;

    /** 负责人（MVP 阶段为自由文本）。 */
    private String assignee;

    /** 标签列表，以 JSON 数组形式存储在 VARCHAR 列中。 */
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> tags;

    /** 验收标准列表，以 JSON 数组形式存储在 VARCHAR 列中。 */
    @TableField(value = "acceptance_criteria", typeHandler = JsonListTypeHandler.class)
    private List<String> acceptanceCriteria;

    /** 工作项的整体风险等级。 */
    private Severity riskLevel;

    /** 乐观锁版本号 — 每次更新时自增。 */
    @Version
    private Integer version;

    /** 工作项创建时间戳。 */
    private LocalDateTime createdAt;

    /** 最后更新时间戳。 */
    private LocalDateTime updatedAt;
}

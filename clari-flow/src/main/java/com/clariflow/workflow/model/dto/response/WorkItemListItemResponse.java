package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemStatus;
import com.clariflow.workflow.model.enums.WorkItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 列表视图的轻量级工作项响应。
 *
 * <p>排除了描述、标签、验收标准、澄清和流转记录等重字段，以优化列表性能。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemListItemResponse {

    /** 唯一标识符。 */
    private String id;

    /** 标题/摘要。 */
    private String title;

    /** 工作项类型。 */
    private WorkItemType type;

    /** 优先级。 */
    private Priority priority;

    /** 当前状态。 */
    private WorkItemStatus status;

    /** 负责人。 */
    private String assignee;

    /** 风险等级。 */
    private Severity riskLevel;

    /** 最后更新时间戳。 */
    private LocalDateTime updatedAt;
}

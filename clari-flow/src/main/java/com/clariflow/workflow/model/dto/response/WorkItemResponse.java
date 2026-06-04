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
import java.util.List;

/**
 * 工作项完整详情响应 — 包含澄清列表、流转记录和可选的 AI 分析结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemResponse {

    /** 唯一标识符，例如 "WI-001"。 */
    private String id;

    /** 工作项的标题/摘要。 */
    private String title;

    /** 详细描述。 */
    private String description;

    /** 工作项类型。 */
    private WorkItemType type;

    /** 优先级。 */
    private Priority priority;

    /** 当前状态。 */
    private WorkItemStatus status;

    /** 负责人。 */
    private String assignee;

    /** 标签列表。 */
    private List<String> tags;

    /** 验收标准列表。 */
    private List<String> acceptanceCriteria;

    /** 风险等级。 */
    private Severity riskLevel;

    /** 当前乐观锁版本号。 */
    private Integer version;

    /** 创建时间戳。 */
    private LocalDateTime createdAt;

    /** 最后更新时间戳。 */
    private LocalDateTime updatedAt;

    /** 关联的澄清列表（可能为空）。 */
    private List<ClarificationResponse> clarifications;

    /** 流转历史记录（可能为空）。 */
    private List<TransitionResponse> transitions;

    /** 最新的 AI 分析结果（可能为 null）。 */
    private AIAnalysisResponse aiAnalysis;
}

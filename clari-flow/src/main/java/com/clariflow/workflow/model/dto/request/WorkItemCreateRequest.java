package com.clariflow.workflow.model.dto.request;

import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建工作项的请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemCreateRequest {

    /** 标题 — 必填，最多 200 个字符。 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    /** 详细描述 — 可选，最多 2000 个字符。 */
    @Size(max = 2000, message = "描述不能超过2000个字符")
    private String description;

    /** 工作项类型。未指定时默认为 STORY。 */
    private WorkItemType type = WorkItemType.STORY;

    /** 优先级。默认为 P2。 */
    private Priority priority = Priority.P2;

    /** 负责人名称（自由文本）。 */
    private String assignee;

    /** 分类标签。 */
    private List<String> tags;

    /** 验收标准列表。 */
    private List<String> acceptanceCriteria;

    /** 整体风险等级。 */
    private Severity riskLevel = Severity.MEDIUM;
}

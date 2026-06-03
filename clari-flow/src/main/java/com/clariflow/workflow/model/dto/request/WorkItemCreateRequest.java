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
 * Request DTO for creating a new work item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemCreateRequest {

    /** Title — required, max 200 chars. */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    /** Detailed description — optional, max 2000 chars. */
    @Size(max = 2000, message = "描述不能超过2000个字符")
    private String description;

    /** Type of work item. Defaults to STORY if not specified. */
    private WorkItemType type = WorkItemType.STORY;

    /** Priority level. Defaults to P2. */
    private Priority priority = Priority.P2;

    /** Assignee name (free-text). */
    private String assignee;

    /** Tags for categorization. */
    private List<String> tags;

    /** Acceptance criteria list. */
    private List<String> acceptanceCriteria;

    /** Overall risk level. */
    private Severity riskLevel = Severity.MEDIUM;
}

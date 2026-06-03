package com.clariflow.workflow.model.dto.request;

import com.clariflow.workflow.model.enums.Priority;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.model.enums.WorkItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for updating an existing work item.
 *
 * <p>Includes the {@code version} field for optimistic locking.
 * The version must match the current version in the database;
 * otherwise a {@code WF-005 VERSION_CONFLICT} error is returned.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemUpdateRequest {

    /** Updated title — optional, max 200 chars. */
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    /** Updated description — optional, max 2000 chars. */
    @Size(max = 2000, message = "描述不能超过2000个字符")
    private String description;

    /** Updated work item type. */
    private WorkItemType type;

    /** Updated priority. */
    private Priority priority;

    /** Updated assignee. */
    private String assignee;

    /** Updated tags. */
    private List<String> tags;

    /** Updated acceptance criteria. */
    private List<String> acceptanceCriteria;

    /** Updated risk level. */
    private Severity riskLevel;

    /** Current version for optimistic locking — required. */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}

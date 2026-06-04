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
 * 更新工作项的请求 DTO。
 *
 * <p>包含用于乐观锁的 {@code version} 字段。
 * version 必须与数据库中的当前版本一致，否则返回 {@code WF-005 VERSION_CONFLICT} 错误。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkItemUpdateRequest {

    /** 更新后的标题 — 可选，最多 200 个字符。 */
    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;

    /** 更新后的描述 — 可选，最多 2000 个字符。 */
    @Size(max = 2000, message = "描述不能超过2000个字符")
    private String description;

    /** 更新后的工作项类型。 */
    private WorkItemType type;

    /** 更新后的优先级。 */
    private Priority priority;

    /** 更新后的负责人。 */
    private String assignee;

    /** 更新后的标签。 */
    private List<String> tags;

    /** 更新后的验收标准。 */
    private List<String> acceptanceCriteria;

    /** 更新后的风险等级。 */
    private Severity riskLevel;

    /** 乐观锁的当前版本号 — 必填。 */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}

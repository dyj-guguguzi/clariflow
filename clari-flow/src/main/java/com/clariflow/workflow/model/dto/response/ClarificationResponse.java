package com.clariflow.workflow.model.dto.response;

import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 澄清问题/答案的响应 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationResponse {

    /** 澄清 ID。 */
    private Long id;

    /** 父工作项 ID。 */
    private String workItemId;

    /** 问题文本。 */
    private String question;

    /** 严重程度。 */
    private Severity severity;

    /** 当前状态。 */
    private ClarificationStatus status;

    /** 答案文本 — 如果仍为 UNRESOLVED 则为 null。 */
    private String answer;

    /** 创建时间戳。 */
    private LocalDateTime createdAt;

    /** 解决时间戳 — 如果为 UNRESOLVED 则为 null。 */
    private LocalDateTime resolvedAt;
}

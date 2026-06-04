package com.clariflow.workflow.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clariflow.workflow.model.enums.ClarificationStatus;
import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 澄清实体 — 映射到 {@code clarification} 表。
 *
 * <p>表示工作项流转到特定状态（READY、IN_DEVELOPMENT）之前需要解决的问题。
 * HIGH 严重程度 + UNRESOLVED 状态的澄清为阻塞项。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("clarification")
public class Clarification {

    /** 自增主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父工作项的外键。 */
    private String workItemId;

    /** 澄清问题文本。 */
    private String question;

    /** 澄清的严重程度：HIGH、MEDIUM 或 LOW。 */
    private Severity severity;

    /** 当前状态：UNRESOLVED 或 RESOLVED。 */
    private ClarificationStatus status;

    /** 答案文本 — 解决时填充。 */
    private String answer;

    /** 澄清创建时间戳。 */
    private LocalDateTime createdAt;

    /** 澄清解决时间戳（可为空）。 */
    private LocalDateTime resolvedAt;
}

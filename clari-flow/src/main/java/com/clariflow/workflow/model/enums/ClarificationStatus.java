package com.clariflow.workflow.model.enums;

/**
 * 澄清状态枚举。
 *
 * <p>澄清从 UNRESOLVED 开始，提供答案后流转到 RESOLVED。</p>
 */
public enum ClarificationStatus {

    /** 未解决 — 澄清问题尚未得到回答。 */
    UNRESOLVED,

    /** 已解决 — 澄清问题已得到回答。 */
    RESOLVED
}

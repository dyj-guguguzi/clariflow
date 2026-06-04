package com.clariflow.workflow.model.enums;

/**
 * 严重程度/风险等级枚举。
 *
 * <p>同时用于工作项风险等级和澄清严重程度。
 * HIGH 严重程度且 UNRESOLVED 状态的澄清会阻塞向 READY 或 IN_DEVELOPMENT 的流转。</p>
 */
public enum Severity {

    /** 高严重性 — 阻塞问题，需要立即关注。 */
    HIGH,

    /** 中严重性 — 应处理但不阻塞。 */
    MEDIUM,

    /** 低严重性 — 轻微关注，可以推迟。 */
    LOW
}

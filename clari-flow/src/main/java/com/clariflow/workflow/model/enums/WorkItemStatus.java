package com.clariflow.workflow.model.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 工作项状态枚举，内置状态流转规则。
 *
 * <p>这是所有合法状态流转的唯一数据源。
 * 每个状态定义了自己可以流转到哪些目标状态。
 * 未在此处列出的流转将被视为无效，并返回 {@code WF-002 INVALID_TRANSITION} 错误。</p>
 *
 * <pre>
 *   DRAFT          → ANALYZING
 *   ANALYZING      → READY, DRAFT
 *   READY          → IN_DEVELOPMENT, ANALYZING
 *   IN_DEVELOPMENT → TESTING, READY
 *   TESTING        → COMPLETED, IN_DEVELOPMENT
 *   COMPLETED      → (终态，不可再流转)
 * </pre>
 */
public enum WorkItemStatus {

    /** 初始草稿状态 — 工作项已创建但尚未分析。 */
    DRAFT {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Collections.singletonList(ANALYZING);
        }
    },

    /** 分析进行中 — 需求正在澄清中。 */
    ANALYZING {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(READY, DRAFT);
        }
    },

    /** 准备就绪 — 所有澄清已解决。 */
    READY {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(IN_DEVELOPMENT, ANALYZING);
        }
    },

    /** 开发进行中 — 实现正在进行中。 */
    IN_DEVELOPMENT {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(TESTING, READY);
        }
    },

    /** 测试进行中 — 质量保证正在执行中。 */
    TESTING {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Arrays.asList(COMPLETED, IN_DEVELOPMENT);
        }
    },

    /** 已完成 — 工作项已完全交付。终态，不再允许流转。 */
    COMPLETED {
        @Override
        public List<WorkItemStatus> getAllowedTargets() {
            return Collections.emptyList();
        }
    };

    /**
     * 返回当前状态可以流转到的目标状态列表。
     *
     * @return 允许的目标状态不可变列表
     */
    public abstract List<WorkItemStatus> getAllowedTargets();

    /**
     * 检查流转到指定目标状态是否合法。
     *
     * @param target 期望的目标状态
     * @return 如果流转被允许则返回 {@code true}
     */
    public boolean canTransitionTo(WorkItemStatus target) {
        return getAllowedTargets().contains(target);
    }
}

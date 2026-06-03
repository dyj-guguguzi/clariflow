-- ClariFlow Seed Data (idempotent: MERGE to avoid duplicate key errors on restart)
-- WI-001: 支持工作项从需求分析到开发完成的状态流转
MERGE INTO work_item (id, title, description, type, priority, status, assignee, tags, acceptance_criteria, risk_level, version, created_at, updated_at)
KEY (id) VALUES (
    'WI-001',
    '支持工作项从需求分析到开发完成的状态流转',
    '作为研发负责人，我希望工作项能够按照分析、准备、开发、测试和完成等阶段进行流转，以便跟踪研发进度并避免需求未澄清就进入开发。',
    'STORY',
    'P1',
    'DRAFT',
    'candidate',
    '["workflow","requirement"]',
    '["工作项只能按合法状态流转","非法状态流转需要给出明确提示","存在未解决高优先级澄清问题时不能进入后续开发状态"]',
    'MEDIUM',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- WI-002: AI 辅助生成需求澄清问题
MERGE INTO work_item (id, title, description, type, priority, status, assignee, tags, acceptance_criteria, risk_level, version, created_at, updated_at)
KEY (id) VALUES (
    'WI-002',
    'AI 辅助生成需求澄清问题',
    '作为研发人员，我希望系统能够根据工作项描述生成可能需要澄清的问题，帮助我在开发前发现需求风险。',
    'STORY',
    'P2',
    'ANALYZING',
    'candidate',
    '["ai","clarification"]',
    '["可以触发 AI 分析","AI 分析结果包含需求摘要、风险点或澄清问题","AI 分析结果以结构化形式展示或返回"]',
    'LOW',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

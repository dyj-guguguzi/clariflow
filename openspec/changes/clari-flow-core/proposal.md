## Why

研发团队在需求阶段常因需求边界不清、验收标准缺失、异常流程未定义等问题导致返工。需要一个轻量级系统来管理工作项全生命周期、追踪需求澄清、并通过 AI 辅助识别风险——在开发前把问题暴露出来。

## What Changes

- **新增**工作项管理能力：创建、查询、更新工作项（Story/Bug/Task），含优先级、标签、验收标准、风险等级
- **新增**6 状态流转引擎：DRAFT → ANALYZING → READY → IN_DEVELOPMENT → TESTING → COMPLETED，支持回退，非法流转拦截
- **新增**澄清问题管理：创建、解决澄清问题，按严重程度（HIGH/MEDIUM/LOW）分级
- **新增**业务规则：存在未解决 HIGH 级澄清问题时，禁止进入 READY / IN_DEVELOPMENT 状态
- **新增**AI 辅助分析服务：生成需求摘要、识别风险点、建议澄清问题（Mock 实现，可替换为真实 LLM）
- **新增**简单前端演示页面：工作项列表 + 详情 + 状态流转 + 澄清问题 + AI 分析
- **新增**核心业务规则测试：合法/非法流转、HIGH 级拦截、乐观锁冲突

## Capabilities

### New Capabilities

- `work-item-crud`: 工作项创建、查询、更新，含类型/优先级/状态/标签/验收标准/风险等级等完整字段
- `status-workflow`: 6 状态流转引擎，单一真相源（枚举内定义），含流转历史记录、乐观锁并发保护
- `clarification-management`: 澄清问题创建、解决、查询，按严重程度分级，支持业务规则拦截
- `ai-analysis`: AI 辅助分析服务（需求摘要 + 风险识别 + 澄清建议），结构化返回，Mock 与真实 LLM 切换
- `frontend-demo`: 单页面演示（左侧列表 + 右侧详情），覆盖创建/流转/澄清/AI 分析全闭环

### Modified Capabilities

<!-- 新项目，无修改 -->

## Impact

- **代码**: clari-flow/ 子模块（Java 17+ → JDK 8 实际部署），约 49 个源文件
- **API**: 10 个 REST 端点（RESTful 风格，统一 ApiResponse 包装）
- **依赖**: Spring Boot 2.7.18, MyBatis-Plus 3.5.3.1, H2 2.1.214, SpringDoc 1.7.0
- **数据库**: H2 文件模式，3 张表（work_item, work_item_transition, clarification）
- **测试**: JUnit 5 + Spring Boot Test，覆盖状态流转、拦截规则、API 集成

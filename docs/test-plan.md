# 测试说明 — ClariFlow

## 1. 测试范围

本次测试覆盖以下功能：
- 工作项 CRUD API（创建/查询/更新）
- 状态流转引擎（合法流转 + 非法流转 + 回退）
- HIGH 级澄清问题阻断业务规则
- 乐观锁版本冲突保护
- 澄清问题 API（创建/列表/解决 + 输入校验）
- AI 分析 API（成功/404/降级）
- DeepSeek 真实 AI 调用

测试环境：
- JDK 8 + Spring Boot Test + MockMvc
- H2 内存模式（`jdbc:h2:mem:testdb`），每次测试自动建表和种子数据
- 测试总数：**63 用例，5 个测试类，100% 通过**

## 2. 核心业务规则验证

| 规则 | 验证方式 | 结果 |
|------|----------|------|
| 合法状态流转 | TransitionServiceTest 测试 10 条合法路径（含回退） | ✅ 通过 |
| 非法状态流转拦截 | TransitionServiceTest 测试 20 条非法路径，验证抛出 WF-002 | ✅ 通过 |
| HIGH 澄清阻断 | ClarificationServiceTest：添加 HIGH 未解决 → 尝试 READY → 验证抛出 WF-003 | ✅ 通过 |
| 澄清解决后放行 | ClarificationServiceTest：解决 HIGH 澄清 → 尝试 READY → 验证成功 | ✅ 通过 |
| LOW 澄清不阻断 | ClarificationServiceTest：添加 LOW 未解决 → 尝试 READY → 验证成功 | ✅ 通过 |
| 回退不受澄清影响 | ClarificationServiceTest：HIGH 未解决 → ANALYZING→DRAFT → 验证成功 | ✅ 通过 |
| 乐观锁冲突 | WorkItemControllerTest：并发更新 → 验证 409 + WF-005 | ✅ 通过 |

## 3. 状态流转测试

### 合法流转（10 条路径）
```
DRAFT → ANALYZING
ANALYZING → READY
READY → IN_DEVELOPMENT
IN_DEVELOPMENT → TESTING
TESTING → COMPLETED
COMPLETED → TESTING
TESTING → IN_DEVELOPMENT
IN_DEVELOPMENT → READY
READY → ANALYZING
ANALYZING → DRAFT
```

全部通过 TransitionServiceTest 验证，状态变更正确，历史记录入库。

### 非法流转（20 条路径）
```
DRAFT → READY, IN_DEVELOPMENT, TESTING, COMPLETED
ANALYZING → IN_DEVELOPMENT, TESTING, COMPLETED
READY → DRAFT, TESTING, COMPLETED
IN_DEVELOPMENT → DRAFT, ANALYZING, COMPLETED
TESTING → DRAFT, ANALYZING, READY
COMPLETED → DRAFT, ANALYZING, READY, IN_DEVELOPMENT
```

全部正确抛出 BusinessException(WF-002)。

## 4. 澄清问题测试

| 场景 | 测试方法 | 验证点 |
|------|----------|--------|
| HIGH 未解决 → 阻断 READY | `testHighClarificationBlocksReady` | 抛出 WF-003 |
| HIGH 未解决 → 阻断 IN_DEVELOPMENT | `testHighClarificationBlocksInDev` | 抛出 WF-003 |
| HIGH 解决后 → 允许 READY | `testResolvedClarificationAllowsTransition` | 成功流转 |
| LOW 未解决 → 不阻断 | `testLowClarificationDoesNotBlock` | 成功流转 |
| HIGH 未解决 → 回退不受影响 | `testHighClarificationDoesNotBlockRollback` | DRAFT 回退成功 |

## 5. AI 能力测试

| 验证项 | 方式 | 结果 |
|--------|------|------|
| AI 分析返回结构化结果 | curl POST /ai-analysis (DeepSeek 真实调用) | ✅ summary + risks[] + suggestions[] + analyzedAt |
| risks 包含 level 和 description | JSON schema 验证 | ✅ 含 HIGH/MEDIUM/LOW 级别风险 |
| suggestions 为字符串数组 | JSON schema 验证 | ✅ 3-5 条建议 |
| 不存在工作项返回 404 | curl POST .../NONEXISTENT/ai-analysis | ✅ WF-001 错误 |
| AI 降级返回 fallback | AIAnalysisControllerTest | ✅ 服务不可用时返回兜底内容 |
| 第二次调用命中缓存 | Redis DBSIZE 验证 | ✅ 缓存命中，响应时间显著降低 |

### 测试类清单

| 测试类 | 用例数 | 类型 |
|--------|--------|------|
| TransitionServiceTest | 32 | Service 单元测试 |
| WorkItemControllerTest | 12 | API 集成测试（MockMvc） |
| ClarificationControllerTest | 11 | API 集成测试（MockMvc） |
| ClarificationServiceTest | 5 | Service 单元测试 |
| AIAnalysisControllerTest | 3 | API 集成测试（MockMvc） |
| **合计** | **63** | |

## 6. 未覆盖风险

| 风险 | 说明 | 缓解 |
|------|------|------|
| 并发状态流转 | TransitionServiceTest 测试了版本冲突，但未测试真正并发场景 | 乐观锁机制已验证，生产环境可加集成压测 |
| 大数据量列表性能 | 当前无分页，列表全量返回 | 生产迁移时加 MyBatis-Plus 分页插件 |
| H2 文件损坏 | 文件持久化模式有数据损坏风险 | 定期备份 data/ 目录，生产迁移至 MySQL |
| Mock AI 质量 | Mock 实现基于关键词规则 | 预留 AIAnalysisService 接口，替换为真实 LLM |
| 前端兼容性 | 仅开发阶段在 Chrome 测试 | 生产替换为 React/Vue 前端框架 |

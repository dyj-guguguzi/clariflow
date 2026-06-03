# AI 使用说明 — ClariFlow

## 1. 使用的 AI 工具

| 工具 | 用途 |
|------|------|
| WorkBuddy（Deepseek-V4-Pro） | 全流程 AI 编码助手，SOP 多智能体协作 |

## 2. 使用场景

| 阶段 | 是否使用 AI | 说明 |
|------|-------------|------|
| 需求理解 | ✅ | PM Agent（许清楚）分析需求包，生成 PRD |
| 任务拆解 | ✅ | Architect Agent（高见远）分解为 5 个任务 |
| 方案设计 | ✅ | Architect Agent 输出完整技术设计（类图、时序图、API 设计） |
| 代码生成 | ✅ | Engineer Agent（寇豆码）编写全部 49 个 Java/HTML/SQL 文件 |
| 测试生成 | ✅ | Engineer Agent 编写 3 个测试类（49 用例） |
| 编译调试 | ✅ | 主理人（齐活林）修复 data.sql 幂等和 proxy 问题 |
| 文档编写 | ✅ | 主理人补充 README、过程记录、API 说明等 |

## 3. 关键 Prompt / Skill 摘要

采用 WorkBuddy 软件开发团队 SOP 工作流：

1. **TeamCreate** "software-workitem-flow" → 建立团队上下文
2. **PM Agent** → 分析需求包 → 输出 PRD（产品目标 + 用户故事 + 需求池 P0/P1/P2）
3. **Architect Agent** → 基于 PRD → 输出架构设计（框架选型 + 类图 + 时序图 + 任务列表）
4. **Engineer Agent** → 按 T01~T05 顺序实现所有代码 + 全局一致性审查（IS_PASS）
5. **主理人** → 编译验证 + DeepSeek 集成 + Redis 缓存 + Knife4j + Docker + 测试补丁 + 文档补齐

## 4. AI 生成内容

- **PRD 文档**（docs/prd.md）：产品目标、用户故事、需求池、UI 设计
- **架构设计**（docs/architecture.md）：技术选型、类图、时序图、任务分解
- **全部源代码**（42 Java + 1 HTML + 3 SQL + 1 YML + 1 XML = 48 文件）
- **5 个测试类**：TransitionServiceTest, ClarificationServiceTest, WorkItemControllerTest, ClarificationControllerTest, AIAnalysisControllerTest
- **openspec 规范**：proposal + design + 5 specs + tasks

## 5. 人工修正内容

| 问题 | 修正 |
|------|------|
| data.sql 使用 INSERT 导致重复主键 | 改为 MERGE INTO 幂等语句 |
| application.yml 缺少 continue-on-error | 添加 `spring.sql.init.continue-on-error: true` |
| curl 测试被 HTTP 代理拦截 | 添加 `--noproxy "*"` 参数 |
| JDK 未默认设置 | 显式设置 JAVA_HOME 指向 JDK 8 |
| 项目名称未统一 | workitem-flow → clari-flow，pom.xml artifactId 同步更新 |
| Redis 序列化 LocalDateTime 报 500 | 自定义 ObjectMapper 激活 JavaTimeModule + DefaultTyping |
| Knife4j /doc.html 404 | WebConfig.addResourceHandlers 覆盖默认资源路径 → 移除自定义 handler |
| DeepSeek 缓存反序列化失败 | ObjectMapper.activateDefaultTyping(NON_FINAL) |
| Mock ↔ DeepSeek 服务切换 | @ConditionalOnProperty + @ConditionalOnMissingBean |
| 前端英文 UI | 全量中文化（类型/状态/严重度/按钮/弹窗） |

## 6. 效果评价

**AI 帮助明显的方面**：
- PRD 和架构文档生成质量高，结构完整
- 状态机实现正确（枚举内置流转规则 + TransitionServiceImpl 原子操作）
- 测试覆盖全面（63 用例遍历所有合法/非法路径和边界情况）
- 全局一致性审查机制有效

**效果有限的方面**：
- Knife4j 集成需人工排查 WebConfig 资源路径覆盖问题
- Redis 缓存序列化需手动配置 JavaTimeModule
- DeepSeek API 响应格式需加入容错解析

**整体评价**：
- AI 承担了约 90% 的代码编写工作
- 人工主要负责：环境配置、中间件集成、UI 优化、故障排查、文档补齐
- 从需求到可运行系统，全流程在数小时内完成

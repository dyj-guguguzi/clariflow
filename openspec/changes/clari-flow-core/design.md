## Context

ClariFlow 是一个轻量级工作项流转系统，面向研发团队内部使用。当前实现为 MVP 版本，采用 Java 8 + Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 技术栈，H2 内嵌数据库文件持久化。

**约束**：
- JDK 8 兼容（团队标准）
- 零外部依赖部署（H2 内嵌）
- 前端原生 HTML/CSS/JS（无构建工具）
- AI 能力 Mock 实现，预留真实 LLM 接入接口

## Goals / Non-Goals

**Goals:**
- 提供工作项完整 CRUD（Story/Bug/Task）
- 实现 6 状态流转引擎（DRAFT→ANALYZING→READY→IN_DEVELOPMENT→TESTING→COMPLETED），含回退
- 支持澄清问题创建/解决/查询，HIGH 级未解决问题阻断后续状态
- 提供 AI 分析服务（需求摘要 + 风险识别 + 澄清建议），Mock + 真实 LLM 可切换
- 提供可运行的前端演示页面，覆盖核心闭环
- 核心业务规则全覆盖测试

**Non-Goals:**
- 完整用户认证与权限体系（MVP 用纯字符串 assignee）
- 企业级看板或复杂前端工程
- 真实 LLM 集成（Mock 实现，接口预留）
- 多模块微服务架构
- Docker Compose 部署编排（加分项，非必做）

## Decisions

### 1. 状态机设计：枚举内置流转规则

**选择**：在 `WorkItemStatus` 枚举中通过 `getAllowedTargets()` 定义每个状态的合法目标状态集合。

**理由**：
- 单一真相源（Single Source of Truth），前后端共享同一规则
- 编译期检查，新增状态时遗漏规则会编译失败（抽象方法强制实现）
- 无需外部配置文件或数据库表维护状态图
- 便于单元测试（枚举可直接调用 `canTransitionTo()`）

**备选方案**：数据库状态转换表 → 灵活但运行时才知道错误；外部 JSON 配置 → 分散维护、不一致风险

### 2. 乐观锁策略：MyBatis-Plus @Version

**选择**：WorkItem 实体添加 `@Version` 注解的 `version` 字段，MyBatis-Plus 更新时自动校验。

**理由**：
- 零代码侵入，框架自动处理版本号递增和冲突检测
- 冲突时返回明确错误码（WF-005 + 409）
- H2 数据库 `UPDATE WHERE version = ?` 天然原子

**备选方案**：悲观锁 SELECT FOR UPDATE → 不适用 Web 请求场景；CAS 手动实现 → 重复造轮子

### 3. JSON 列存储：自定义 TypeHandler

**选择**：`tags` 和 `acceptanceCriteria`（`List<String>`）存储为 VARCHAR(4000) JSON 字符串，通过 `JsonListTypeHandler` 自动序列化/反序列化。

**理由**：
- H2 不原生支持 JSONB，VARCHAR 兼容性最好
- MyBatis-Plus TypeHandler 机制透明转换，业务代码无感知
- 4000 字符足够容纳正常规模的工作项

**备选方案**：关联表 → 增加表数量和 JOIN 复杂度；PostgreSQL JSONB → 增加部署依赖

### 4. 统一错误体系

**选择**：ErrorCode 枚举（WF-001~005）+ BusinessException + GlobalExceptionHandler。

| 错误码 | HTTP | 含义 |
|--------|------|------|
| WF-001 | 404 | 工作项不存在 |
| WF-002 | 422 | 非法状态流转 |
| WF-003 | 422 | HIGH 级澄清阻断 |
| WF-004 | 404 | 澄清问题不存在 |
| WF-005 | 409 | 乐观锁版本冲突 |

**理由**：前端可通过错误码做差异化处理（如 409 提示刷新重试），而非仅依赖 HTTP 状态码

### 5. AI 服务接口抽象

**选择**：`AIAnalysisService` 接口 + `MockAIAnalysisServiceImpl` 实现。

```java
public interface AIAnalysisService {
    AIAnalysisResponse analyze(String workItemId);
}
```

**理由**：
- Mock 实现根据 title/description 关键词生成结构化 Mock 结果
- 替换为真实 LLM：只需新增一个实现类并注入，无需修改调用方
- 接口单一抽象，便于单元测试 Mock

### 6. 前端架构：单页面内联

**选择**：单个 `index.html`，内联 `<style>` 和 `<script>`，使用 Fetch API 调用后端。

**理由**：
- 零构建工具，Spring Boot 直接 serve（`src/main/resources/static/`）
- 考核场景下部署验证最简单
- 后续可替换为 React/Vue 前端，API 接口不变

## Risks / Trade-offs

- **[风险] H2 文件模式 → 数据库文件损坏**：`jdbc:h2:file:./data/clariflow` 文件可能因进程异常退出损坏 → **缓解**：开启 `DB_CLOSE_DELAY=-1`，生产环境可迁移至 MySQL/PostgreSQL
- **[风险] 无认证机制 → 开放访问**：MVP 无用户认证 → **缓解**：仅内部演示使用，后续可通过 Spring Security 添加
- **[风险] Mock AI → 分析结果质量有限**：Mock 实现基于关键词规则 → **缓解**：接口抽象，替换为真实 LLM 实现即可
- **[权衡] JSON 列存储 → 查询受限**：`tags` 存为 JSON 字符串，无法高效按标签查询 → **缓解**：当前规模下的必要取舍，后续可迁移为关联表

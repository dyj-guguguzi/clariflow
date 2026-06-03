# ClariFlow — AI 辅助研发工作项流转与需求澄清系统

## 1. 题目方向

后端方向：工作项状态流转与 AI 需求分析服务

## 2. 功能清单

| # | 功能 | 状态 |
|---|------|------|
| 1 | 工作项 CRUD（创建/查询/更新） | ✅ 完成 |
| 2 | 6 状态流转引擎（草稿→分析中→已准备→开发中→测试中→已完成） | ✅ 完成 |
| 3 | 状态流转历史记录 | ✅ 完成 |
| 4 | 澄清问题管理（创建/查询/解决） | ✅ 完成 |
| 5 | HIGH 级未解决澄清问题阻断状态流转 | ✅ 完成 |
| 6 | AI 辅助分析（DeepSeek 真实 API，需求摘要/风险识别/澄清建议） | ✅ 完成 |
| 7 | 全中文前端演示页面 | ✅ 完成 |
| 8 | Knife4j + OpenAPI 3.0 接口文档 | ✅ 完成 |
| 9 | 乐观锁并发保护 | ✅ 完成 |
| 10 | 统一错误码体系（WF-001~005） | ✅ 完成 |
| 11 | Redis 缓存（列表/详情/AI 分析） | ✅ 完成 |
| 12 | Docker Compose 一键部署（Redis + App） | ✅ 完成 |

## 3. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| JDK | 1.8 | 运行环境 |
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis-Plus | 3.5.3.1 | ORM + 乐观锁 |
| H2 Database | 2.1.214 | 内嵌数据库（文件持久化，MySQL 兼容模式） |
| Redis | 7-alpine (Docker) | 缓存中间件 |
| Spring Cache + Lettuce | 2.7.18 | 声明式缓存 |
| Knife4j | 4.3.0 | API 文档（增强 UI） |
| SpringDoc OpenAPI | 1.7.0 | OpenAPI 3.0 规范 |
| Lombok | 1.18.30 | 简化代码 |
| DeepSeek API | deepseek-chat | AI 分析（OpenAI 兼容） |
| 前端 | 原生 HTML/CSS/JS + Fetch API | 全中文界面 |

**数据库选型理由**：H2 文件持久化，零外部依赖，MySQL 兼容模式便于后续迁移。

## 4. 如何运行

### 设置 DeepSeek API Key（可选）
```bash
# 方式一：环境变量（推荐）
export DEEPSEEK_API_KEY=sk-your-key-here

# 方式二：直接写入配置文件
# 编辑 src/main/resources/application.yml，将 api-key 替换为你的 Key
```
> 不设置则自动使用 Mock 模式（基于关键词规则模拟 AI 分析结果）。

### 方式一：Docker Compose（推荐）
```bash
cd clari-flow
docker compose up -d
# 自动启动 Redis + App，访问 http://localhost:8080
```

### 方式二：本地启动（需先启动 Redis）
```bash
# 启动 Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 编译运行
cd clari-flow
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_491.jdk/Contents/Home
mvn clean package -DskipTests
java -jar target/clari-flow-1.0.0.jar
```

### 访问地址
| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:8080/index.html |
| Knife4j 文档 | http://localhost:8080/doc.html |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |

H2 Console：JDBC URL `jdbc:h2:file:./data/clariflow`，用户名 `sa`，密码空。

### 演示闭环
```
打开 index.html → 查看工作项列表 → 创建/详情 → 添加 HIGH 澄清问题
→ 尝试状态流转被阻断(422) → 解决澄清问题 → 状态流转成功
→ 触发 AI 分析（DeepSeek） → 展示结构化结果
```

## 5. 如何测试

```bash
cd clari-flow
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_491.jdk/Contents/Home
mvn test -Dmaven.repo.local=$HOME/.m2/repository
```

测试覆盖（63 用例，100% 通过）：

| 测试类 | 用例 | 覆盖范围 |
|--------|------|----------|
| TransitionServiceTest | 32 | 合法/非法流转、回退、版本冲突 |
| WorkItemControllerTest | 12 | 工作项 API 集成（200/201/404/409/422） |
| ClarificationControllerTest | 11 | 澄清问题 API 集成（创建/列表/解决 + 边界） |
| ClarificationServiceTest | 5 | HIGH 级澄清阻断业务规则 |
| AIAnalysisControllerTest | 3 | AI 分析 API 集成（成功/404/降级） |

## 6. 核心设计说明

### 状态机
```
草稿 → 分析中
分析中 → 已准备, 草稿
已准备 → 开发中, 分析中
开发中 → 测试中, 已准备
测试中 → 已完成, 开发中
已完成 → 测试中
```
单一真相源：`WorkItemStatus` 枚举的 `getAllowedTargets()`。

### AI 服务
```
AIAnalysisService (接口)
├── DeepSeekAIAnalysisService (当前启用, clariflow.ai.provider=deepseek)
└── MockAIAnalysisServiceImpl (备用, @ConditionalOnMissingBean)
```
切换方式：修改 `application.yml` 中 `clariflow.ai.provider` 为 `mock` 或 `deepseek`。

### 缓存策略
| Region | TTL | 刷新时机 |
|--------|-----|----------|
| workItems (详情) | 5 min | update/transition |
| workItems (列表) | 5 min | create/update/transition |
| aiAnalysis | 1 hour | 手动重新触发 |

## 7. 已完成内容

- 工作项完整 CRUD + 状态流转引擎 + 流转历史
- 澄清问题管理 + HIGH 级业务阻断规则
- DeepSeek 真实 AI 分析（或 Mock 回退）
- 全中文前端单页面
- Knife4j + OpenAPI 3.0 接口文档
- Redis 声明式缓存（列表/详情/AI 分析）
- Docker Compose 一键部署
- 63 个测试用例全部通过
- openspec 完整规范（proposal + design + 5 specs + tasks）

## 8. 后续优化方向

1. 数据库迁移至 MySQL/PostgreSQL（H2 预留 MySQL 兼容模式）
2. Spring Security 用户认证与权限控制
3. 前端升级为 React/Vue SPA
4. WebSocket 状态变更实时通知
5. 分布式 ID 替换 `generateNextId()`（并发场景）
6. API Key 迁至环境变量 / 配置中心

## 9. AI 使用说明

详见 [docs/ai-usage.md](../docs/ai-usage.md)

## 10. 文档索引

| 文档 | 路径 |
|------|------|
| PRD 产品需求 | [docs/prd.md](../docs/prd.md) |
| 技术架构设计 | [docs/architecture.md](../docs/architecture.md) |
| 过程记录 | [docs/process.md](../docs/process.md) |
| AI 使用说明 | [docs/ai-usage.md](../docs/ai-usage.md) |
| API 设计说明 | [docs/api-design-proposal.md](../docs/api-design-proposal.md) |
| 测试说明 | [docs/test-plan.md](../docs/test-plan.md) |
| openspec 规范 | [openspec/changes/clari-flow-core/](../openspec/changes/clari-flow-core/) |

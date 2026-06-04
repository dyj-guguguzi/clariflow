# 过程记录 — ClariFlow

## 1. 需求理解

核心场景：研发团队在需求阶段因边界不清、验收标准缺失、异常流程未定义等问题导致返工。需要一个轻量级系统来：
1. 管理工作项全生命周期
2. 跟踪状态流转（DRAFT → ANALYZING → READY → IN_DEVELOPMENT → TESTING → COMPLETED）
3. 记录并处理需求澄清问题
4. 通过 AI 辅助识别需求风险

关键业务规则：存在未解决的 HIGH 级澄清问题时，禁止进入 READY 或 IN_DEVELOPMENT 状态。

## 2. 任务拆解

| ID | 任务 | 文件数 | 依赖 |
|----|------|--------|------|
| T01 | 项目基础设施：pom.xml, application.yml, schema.sql, data.sql, 配置类, 异常体系 | 12 | — |
| T02 | 数据模型与持久层：3 entity, 5 enum, 11 dto, 3 mapper | 22 | T01 |
| T03 | 业务服务层：4 interface + 4 impl（状态机核心） | 8 | T02 |
| T04 | REST API 控制器：3 | 3 | T03 |
| T05 | 前端页面 + 测试：index.html + 3 测试类 | 4 | T04 |

## 3. 技术方案

- **框架**：Java 8 + Spring Boot 2.7.18（团队标准）
- **ORM**：MyBatis-Plus 3.5.3.1（内置乐观锁、TypeHandler 机制）
- **数据库**：H2 文件持久化（零依赖部署，MySQL 兼容模式）
- **API 文档**：SpringDoc OpenAPI 3.0
- **前端**：原生 HTML/CSS/JS + Fetch API（零构建工具）
- **架构**：经典三层（Controller → Service → Mapper）

## 4. API / 数据 / 状态设计

详见 [docs/api-design-proposal.md](docs/api-design-proposal.md) 和 [docs/architecture.md](docs/architecture.md)

**API 设计原则**：
- RESTful 风格，统一 `ApiResponse<T>` 包装
- 成功：`{"code":0, "message":"success", "data":{...}}`
- 失败：`{"code":<错误码>, "message":"<描述>", "data":null}`
- HTTP 状态码：200 成功，201 创建，404 不存在，409 版本冲突，422 业务错误

**状态流转设计**：
- 单一真相源：`WorkItemStatus` 枚举的 `getAllowedTargets()`
- 流转校验 + HIGH 拦截 + 乐观锁 + 历史记录 → 一个 `@Transactional` 方法
- COMPLETED 为终态：`getAllowedTargets()` 返回空列表，已完成项不可再流转

## 5. AI 使用过程

全过程使用 WorkBuddy（Deepseek-V4-Pro）AI 编码助手，通过标准 SOP 多智能体协作流程：

| 阶段 | AI 工具 | 用途 |
|------|---------|------|
| 需求分析 | WorkBuddy PM Agent | 生成 PRD 文档 |
| 架构设计 | WorkBuddy Architect Agent | 生成技术设计 + 任务分解 |
| 代码实现 | WorkBuddy Engineer Agent | 编写全部 49 个源文件 |
| 测试生成 | WorkBuddy Engineer Agent | 编写 3 个测试类 |
| 编译验证 | 主理人 | 修复 data.sql 幂等问题和 proxy 配置 |
| 文档编写 | 主理人 | 补充 README 和过程文档 |

详见 [docs/ai-usage.md](docs/ai-usage.md)

## 6. 遇到的问题

1. **data.sql 重复主键**：H2 文件持久化导致重启时 INSERT 冲突 → 改用 `MERGE INTO` 幂等语句
2. **HTTP 代理拦截**：本地 curl 测试被系统 HTTP 代理 `127.0.0.1:60572` 拦截 → 添加 `--noproxy "*"` 参数
3. **JDK 环境变量**：系统无默认 JDK → 显式设置 `JAVA_HOME` 为 JDK 8 路径
4. **Redis 序列化 LocalDateTime**：`GenericJackson2JsonRedisSerializer` 未注册 `JavaTimeModule` → 自定义 ObjectMapper 激活默认类型
5. **Knife4j doc.html 404**：`WebConfig.addResourceHandlers` 覆盖了 Spring Boot 默认的 `META-INF/resources/` 路径，导致 knife4j JAR 中静态资源不可达 → 移除自定义 resource handler，恢复默认行为
6. **Maven 仓库写入权限**：系统 Maven `/opt/homebrew/` 目录无写权限 → 指定 `-Dmaven.repo.local=$HOME/.m2/repository`

## 7. 优化迭代记录（2026-06-04）

1. **前端 localStorage 未初始化**：`index.html` 中 `token` 和 `currentUsername` 变量引用但未从 localStorage 读取 → 添加 `const token = localStorage.getItem('token')` 等初始化代码
2. **缺少删除功能**：工作项无删除入口 → 全栈添加 DELETE /api/work-items/{id}，级联删除关联的 clarification 和 transition，前端详情面板添加红色删除按钮
3. **DELETE 返回 204 导致前端 JSON 解析失败**：`@ResponseStatus(HttpStatus.NO_CONTENT)` 返回空响应体，前端 `res.json()` 报错 → 移除 204 注解，改为正常 200 返回 JSON
4. **COMPLETED 非终态**：已完成工作项允许回退到 TESTING → 将 `COMPLETED.getAllowedTargets()` 改为 `Collections.emptyList()`，前端同步移除
5. **流转历史不显示流转原因**：详情面板只显示状态/操作人/时间，缺少 reason 字段 → 添加 `「${esc(t.reason)}」` 显示

## 8. 验证记录

| 验证项 | 方式 | 结果 |
|--------|------|------|
| 编译 | `mvn clean compile -DskipTests` | BUILD SUCCESS |
| 单元测试 | `mvn test` | 63/63 通过 |
| API 手动验证 | curl 13 个端点（含 DELETE + Auth） | 全部返回正确 |
| 业务规则验证 | 非法流转 → 422, HIGH 阻断 → 422, 解决后流转 → 200 | 全部符合预期 |
| COMPLETED 终态验证 | COMPLETED → TESTING → 422 | 终态不可流转 ✅ |
| DeepSeek AI | POST /ai-analysis | 真实 LLM 返回结构化分析 |
| Redis 缓存 | 连续请求对比响应时间 | 315ms → 18ms（17x 提速） |
| Knife4j 文档 | 浏览器访问 /doc.html | 200 原生 UI 正常渲染 |
| 前端页面 | 浏览器访问 index.html | 全中文界面 200 |
| Docker Compose | `docker compose up` | Redis + App 一键启动 |
| Playwright E2E | 浏览器自动化：登录→创建→流转→查看历史→删除 | 全流程通过 |

## 9. 取舍说明

- **已完成**用户认证 → JWT Token + BCrypt 密码加密，`sys_user` 表 + `/api/auth/register|login` 接口，前端登录页面
- **已完成**删除功能 → 全栈 DELETE 端点 + 级联删除 + 前端确认弹窗
- **已完成**Docker Compose → Redis 7-alpine + App 容器编排
- **已完成**真实 LLM 集成 → DeepSeek API，Mock 作为 `@ConditionalOnMissingBean` 备用
- **已完成**用户上下文 → X-User 请求头 + `ThreadLocal` 轻量方案，同时 JwtAuthFilter 解析 JWT 设置 UserContext
- **未做**复杂前端看板 → 题目不强制要求，全中文原生 HTML 已覆盖核心演示
- **未做**MySQL 迁移 → H2 MySQL 兼容模式，迁移仅需改配置

## 10. 后续扩展路径

### 多人协作与权限控制
当前已实现 JWT + BCrypt 用户认证（`sys_user` 表、`/api/auth/register|login`、`JwtAuthFilter`）。扩展路径：
1. 添加 `sys_role` 表，建立 RBAC 模型
2. Controller 层添加 `@PreAuthorize` 注解控制操作权限（如仅 ADMIN 可删除工作项）
3. 区分管理员（admin）和普通用户（pm）的角色权限

### 专业前端扩展
当前原生 HTML 通过 Fetch API 调用 10 个 REST 端点。专业前端需要的额外 API：
- 分页查询 `GET /api/work-items?page=1&size=20`
- 批量状态变更 `PUT /api/work-items/batch-transitions`
- 看板统计 `GET /api/work-items/stats?groupBy=status`
- WebSocket 推送 `ws://.../notifications`（状态变更实时通知）
- 搜索 `GET /api/work-items/search?keyword=xxx`

### AI 服务演进
当前 `AIAnalysisService` 接口已封装。扩展方向：
- 支持流式返回（SSE）`POST /api/work-items/{id}/ai-analysis/stream`
- 多模型切换（DeepSeek / Qwen / GPT）通过配置选择
- 分析历史记录 `GET /api/work-items/{id}/ai-analysis/history`
- 批量分析 `POST /api/work-items/ai-analysis/batch`

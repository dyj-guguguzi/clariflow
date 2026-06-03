# ClariFlow — AI 辅助研发工作项流转与需求澄清系统

> 后端方向 · Java 8 + Spring Boot 2.7 · DeepSeek AI · Redis · Knife4j · Docker

基于 FDE 后端考题实现，支持工作项全生命周期管理、6 状态流转引擎、澄清问题管理、DeepSeek 真实 AI 分析。

## 项目结构

```
ClariFlow/
├── clari-flow/          # 🔧 Spring Boot 后端源码 + 前端 + Docker
│   ├── src/main/java/   # 后端 Java 源码（42 文件）
│   │   ├── config/          # Spring 配置（Redis/MyBatis/Knife4j/CORS/缓存降级/RestTemplate）
│   │   ├── controller/      # REST API 控制器（WorkItem/Clarification/AI Analysis）
│   │   ├── model/
│   │   │   ├── entity/      # 数据库实体（WorkItem/WorkItemTransition/Clarification）
│   │   │   ├── dto/         # 请求/响应 DTO（含 DeepSeek API 数据结构）
│   │   │   └── enums/       # 枚举（状态/类型/优先级/严重度）
│   │   ├── repository/      # MyBatis-Plus Mapper
│   │   ├── service/         # 业务服务（状态机核心 + DeepSeek AI + Mock 备用）
│   │   └── common/          # 公共（异常/错误码/TypeHandler）
│   ├── src/main/resources/  # 配置与静态资源
│   │   ├── application.yml  # 应用配置（H2/Redis/DeepSeek/Knife4j）
│   │   ├── schema.sql       # 建表 DDL
│   │   ├── data.sql         # 种子数据
│   │   └── static/          # 全中文前端页面
│   ├── src/test/java/       # 测试（5 类 / 63 用例）
│   ├── Dockerfile           # 多阶段构建镜像
│   ├── docker-compose.yml   # Redis + App 一键编排
│   └── README.md            # 模块详细文档
│
├── docs/                # 📄 项目文档
│   ├── prd.md               # 产品需求文档
│   ├── architecture.md      # 技术架构设计（含类图 + 时序图）
│   ├── process.md           # 过程记录（需求理解 · 任务拆解 · 遇坑记录 · 验证）
│   ├── ai-usage.md          # AI 使用说明（工具 · Prompt · 生成内容 · 人工修正）
│   ├── api-design-proposal.md  # API 设计说明（端点 · 错误码 · AI 分析结构）
│   ├── test-plan.md         # 测试说明（63 用例 · 覆盖范围 · 未覆盖风险）
│   ├── class-diagram.mermaid    # 类图（Mermaid 格式）
│   └── sequence-diagram.mermaid # 核心时序图（Mermaid 格式）
│
└── openspec/            # 📐 OpenSpec 规范驱动开发
    ├── config.yaml          # 项目上下文 + 规约
    └── changes/clari-flow-core/
        ├── proposal.md      # 变更提案（Why · What · Impact）
        ├── design.md        # 技术设计决策
        ├── tasks.md         # 实现清单（复选框跟踪）
        └── specs/           # 5 份能力规约
            ├── work-item-crud/
            ├── status-workflow/
            ├── clarification-management/
            ├── ai-analysis/
            └── frontend-demo/
```

## 快速开始

```bash
# 1. 启动 Redis（Docker）
docker start clariflow-redis || docker run -d --name clariflow-redis -p 6379:6379 redis:7-alpine

# 2. 编译并启动
cd clari-flow
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_491.jdk/Contents/Home
mvn clean package -DskipTests
java -jar target/clari-flow-1.0.0.jar

# 3. 访问
open http://localhost:8080/index.html
```

## 文档入口

| 页面 | 地址 |
|------|------|
| 前端演示 | http://localhost:8080/index.html |
| Knife4j API 文档 | http://localhost:8080/doc.html |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 Console | http://localhost:8080/h2-console |

## 技术栈

Java 8 · Spring Boot 2.7.18 · MyBatis-Plus 3.5.3.1 · H2 · Redis 7 · Knife4j 4.3 · DeepSeek API · Docker Compose · JUnit 5

## 测试

```bash
cd clari-flow
mvn test
# 63/63 通过
```

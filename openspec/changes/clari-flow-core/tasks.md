## 1. 项目基础设施与配置

- [x] 1.1 创建 Maven pom.xml，配置 Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + H2 2.1.214 + SpringDoc 1.7.0 + Lombok 1.18.30
- [x] 1.2 创建 application.yml，配置 H2 文件持久化、MyBatis-Plus 映射、Swagger 路径
- [x] 1.3 创建 schema.sql（DDL 建表：work_item, work_item_transition, clarification）和 data.sql（Seed: WI-001, WI-002）
- [x] 1.4 创建 Spring Boot 入口类 ClariFlowApplication
- [x] 1.5 创建 MyBatisPlusConfig（分页插件 + JsonListTypeHandler 注册）
- [x] 1.6 创建 SwaggerConfig（OpenAPI 3.0 文档配置）
- [x] 1.7 创建 WebConfig（CORS 允许所有源 + 静态资源配置）
- [x] 1.8 创建统一错误码 ErrorCode 枚举（WF-001~005）
- [x] 1.9 创建 BusinessException（携带 ErrorCode + HTTP 状态码）
- [x] 1.10 创建 GlobalExceptionHandler（@ControllerAdvice，统一异常 → ApiResponse 转换）
- [x] 1.11 创建 JsonListTypeHandler（List<String> ↔ VARCHAR(4000) JSON 序列化）

## 2. 数据模型与持久层

- [x] 2.1 创建枚举类：WorkItemType(STORY/BUG/TASK), Priority(P0/P1/P2), WorkItemStatus(含流转规则), Severity(HIGH/MEDIUM/LOW), ClarificationStatus(UNRESOLVED/RESOLVED)
- [x] 2.2 创建实体类：WorkItem, WorkItemTransition, Clarification（Lombok + MyBatis-Plus 注解）
- [x] 2.3 创建请求 DTO：WorkItemCreateRequest, WorkItemUpdateRequest, TransitionRequest, ClarificationCreateRequest, ClarificationResolveRequest
- [x] 2.4 创建响应 DTO：ApiResponse<T>, WorkItemResponse, WorkItemListItemResponse, TransitionResponse, ClarificationResponse, AIAnalysisResponse
- [x] 2.5 创建 Mapper 接口：WorkItemMapper, WorkItemTransitionMapper, ClarificationMapper（继承 MyBatis-Plus BaseMapper）

## 3. 业务服务层

- [x] 3.1 实现 WorkItemService：createWorkItem, getWorkItem, listWorkItems, updateWorkItem（含乐观锁）
- [x] 3.2 实现 TransitionService：executeTransition（状态校验 + HIGH 拦截 + 乐观锁更新 + 历史记录，@Transactional），getTransitionHistory
- [x] 3.3 实现 ClarificationService：addClarification, getClarifications, resolveClarification
- [x] 3.4 实现 AIAnalysisService 接口 + MockAIAnalysisServiceImpl（基于关键词规则生成结构化 Mock 结果）

## 4. REST API 控制器

- [x] 4.1 创建 WorkItemController：POST/GET/PUT /api/work-items, POST/GET /api/work-items/{id}/transitions
- [x] 4.2 创建 ClarificationController：POST/GET /api/work-items/{workItemId}/clarifications, PUT .../resolve
- [x] 4.3 创建 AIAnalysisController：POST /api/work-items/{workItemId}/ai-analysis

## 5. 前端页面与测试

- [x] 5.1 创建 index.html（单页面，左列表右详情，Fetch API，状态流转按钮动态渲染）
- [x] 5.2 创建 TransitionServiceTest（合法流转 10+ 路径 + 非法拦截 20+ 路径 + 版本冲突 + 不存在）
- [x] 5.3 创建 ClarificationServiceTest（HIGH 级拦截规则 5+ 场景）
- [x] 5.4 创建 WorkItemControllerTest（@SpringBootTest + @AutoConfigureMockMvc，10+ API 场景含 201/200/404/409/422）

# API 设计说明 — ClariFlow

## 1. API 设计目标

提供 RESTful API 支持前端页面完成以下闭环：
创建/查看工作项 → 添加澄清问题 → 尝试状态流转被阻断 → 解决澄清问题 → 状态流转成功 → 触发 AI 分析（DeepSeek） → 展示结构化结果

API 文档入口：
- **Knife4j UI**：http://localhost:8080/doc.html（增强 UI）
- **Swagger UI**：http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**：http://localhost:8080/v3/api-docs

## 2. 资源或模块划分

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| WorkItem | `/api/work-items` | 工作项 CRUD + 状态流转 |
| Clarification | `/api/work-items/{workItemId}/clarifications` | 澄清问题管理 |
| AI Analysis | `/api/work-items/{workItemId}/ai-analysis` | AI 分析触发 |

采用 REST 风格：资源路径表示实体，HTTP 方法表示操作。

## 3. API 列表

| 能力 | 方法/路径 | 输入 | 输出 | 说明 |
|------|-----------|------|------|------|
| 创建工作项 | POST `/api/work-items` | WorkItemCreateRequest | ApiResponse\<WorkItemResponse\> | 自动生成 WI-xxx ID, status=DRAFT |
| 查询列表 | GET `/api/work-items?type=&priority=&status=` | 可选筛选参数 | ApiResponse\<List\<ListItem\>\> | 按更新时间 DESC 排序 |
| 查询详情 | GET `/api/work-items/{id}` | — | ApiResponse\<WorkItemResponse\> | 含 clarifications, transitions, aiAnalysis |
| 更新工作项 | PUT `/api/work-items/{id}` | WorkItemUpdateRequest(含 version) | ApiResponse\<WorkItemResponse\> | 乐观锁校验 |
| 状态流转 | POST `/api/work-items/{id}/transitions` | TransitionRequest | ApiResponse\<WorkItemResponse\> | 状态机核心 |
| 流转历史 | GET `/api/work-items/{id}/transitions` | — | ApiResponse\<List\<TransitionResponse\>\> | 按时间排序 |
| 新增澄清 | POST `/api/work-items/{workItemId}/clarifications` | ClarificationCreateRequest | ApiResponse\<ClarificationResponse\> | severity: HIGH/MEDIUM/LOW |
| 查询澄清 | GET `/api/work-items/{workItemId}/clarifications` | — | ApiResponse\<List\<ClarificationResponse\>\> | 按创建时间排序 |
| 解决澄清 | PUT `/api/work-items/{workItemId}/clarifications/{clarificationId}/resolve` | ClarificationResolveRequest | ApiResponse\<ClarificationResponse\> | status→RESOLVED, 记录 resolvedAt |
| AI 分析 | POST `/api/work-items/{workItemId}/ai-analysis` | — | ApiResponse\<AIAnalysisResponse\> | 返回 summary + risks + suggestions |

### 统一响应格式

```json
// 成功
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": 1780414663126
}

// 失败
{
  "code": 422,
  "message": "[WF-002] 非法状态流转: DRAFT → READY 不被允许",
  "data": null,
  "timestamp": 1780414663203
}
```

## 4. 状态流转错误设计

| HTTP | 错误码 | 含义 | 触发条件 |
|------|--------|------|----------|
| 422 | WF-002 | 非法状态流转 | 目标状态不在当前状态的 getAllowedTargets() 中 |
| 422 | WF-003 | HIGH 澄清阻断 | 存在 UNRESOLVED + HIGH 级澄清，且目标为 READY/IN_DEVELOPMENT |
| 409 | WF-005 | 版本冲突 | 乐观锁 version 不匹配 |
| 404 | WF-001 | 工作项不存在 | ID 不在数据库中 |
| 404 | WF-004 | 澄清问题不存在 | clarification ID 无效 |

设计理念：HTTP 状态码表示错误类别（客户端错误 vs 服务端错误），业务错误码（WF-xxx）提供精确语义，前端可据此做差异化处理。

## 5. AI 分析结果设计

AI 服务架构：
```
AIAnalysisService (接口)
├── DeepSeekAIAnalysisService (当前启用, clariflow.ai.provider=deepseek)
│   └── 调用 https://api.deepseek.com/v1/chat/completions
└── MockAIAnalysisServiceImpl (@ConditionalOnMissingBean 备用)
```

DeepSeek 返回示例：

```json
{
  "summary": "基于对「标题」的分析，该工作项涉及...",
  "risks": [
    {
      "level": "HIGH",
      "description": "状态流转规则可能遗漏边界情况..."
    },
    {
      "level": "MEDIUM",
      "description": "需求澄清流程可能存在循环依赖..."
    }
  ],
  "suggestions": [
    "在进入开发前确保所有 HIGH 级别的澄清问题已解决",
    "编写完整的状态流转单元测试，覆盖所有合法和非法路径"
  ],
  "analyzedAt": "2026-06-02T23:37:53.591"
}
```

## 6. 前后端协作说明

前端（index.html）通过 Fetch API 调用后端：
```javascript
// 获取列表
const resp = await fetch('/api/work-items');
const { data } = await resp.json();
// data 为 WorkItemListItemResponse 数组

// 状态流转
const resp = await fetch(`/api/work-items/${id}/transitions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ targetStatus, reason, operator })
});
if (resp.status === 422) {
  // 显示错误信息（WF-002 或 WF-003）
}
```

前端状态流转按钮根据服务端返回的 `WorkItemStatus.getAllowedTargets()` 逻辑动态生成，前后端状态规则完全一致。

## 7. 后续扩展

专业前端工程化后需要补充的 API：
- 批量更新工作项（支持看板拖拽）
- 工作项搜索/全文检索
- 用户认证与授权（Spring Security + JWT）
- WebSocket 推送（状态变更实时通知）
- 分页查询（当前列表无分页）
- 报表/统计接口

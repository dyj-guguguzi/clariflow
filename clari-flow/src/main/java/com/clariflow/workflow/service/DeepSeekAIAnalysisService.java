package com.clariflow.workflow.service;

import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;
import com.clariflow.workflow.model.dto.response.DeepSeekDTO;
import com.clariflow.workflow.model.entity.WorkItem;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.repository.WorkItemMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * DeepSeek-powered implementation of {@link AIAnalysisService}.
 *
 * <p>Calls DeepSeek Chat API (OpenAI-compatible) to generate
 * structured analysis: summary, risks, and clarification suggestions.</p>
 *
 * <p>Activated when {@code clariflow.ai.provider=deepseek} (default).
 * Falls back to {@link MockAIAnalysisServiceImpl} otherwise.</p>
 */
@Service
@ConditionalOnProperty(name = "clariflow.ai.provider", havingValue = "deepseek", matchIfMissing = false)
public class DeepSeekAIAnalysisService implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekAIAnalysisService.class);

    private static final String SYSTEM_PROMPT =
            "你是一个资深的软件工程需求分析师。请根据工作项信息进行分析，严格按以下 JSON 格式返回（不要包含 markdown 代码块标记）：\n" +
            "{\n" +
            "  \"summary\": \"一段简洁的需求摘要（50-100字）\",\n" +
            "  \"risks\": [{\"level\": \"HIGH|MEDIUM|LOW\", \"description\": \"风险描述\"}],\n" +
            "  \"suggestions\": [\"澄清建议1\", \"澄清建议2\", ...]\n" +
            "}\n" +
            "要求：summary 必须精炼；risks 至少 1 条最多 3 条；suggestions 至少 3 条最多 5 条。";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WorkItemMapper workItemMapper;

    @Value("${clariflow.ai.deepseek.api-key}")
    private String apiKey;

    @Value("${clariflow.ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${clariflow.ai.deepseek.model:deepseek-chat}")
    private String model;

    public DeepSeekAIAnalysisService(RestTemplate restTemplate,
                                      ObjectMapper objectMapper,
                                      WorkItemMapper workItemMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.workItemMapper = workItemMapper;
    }

    @Override
    @Cacheable(value = "aiAnalysis", key = "#workItemId")
    public AIAnalysisResponse analyze(String workItemId) {
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        String userMessage = buildUserMessage(workItem);
        log.info("DeepSeek analyze: workItemId={}, title={}", workItemId, workItem.getTitle());

        try {
            String rawJson = callDeepSeek(userMessage);
            return parseResponse(rawJson);
        } catch (Exception e) {
            log.error("DeepSeek API call failed for {}, falling back to structured error", workItemId, e);
            return buildFallbackResponse(workItem, e.getMessage());
        }
    }

    // ─── Private helpers ─────────────────────────────────────────────

    private String buildUserMessage(WorkItem wi) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下工作项：\n");
        sb.append("标题：").append(wi.getTitle()).append("\n");
        sb.append("类型：").append(wi.getType()).append("\n");
        sb.append("优先级：").append(wi.getPriority()).append("\n");
        sb.append("当前状态：").append(wi.getStatus()).append("\n");
        if (wi.getDescription() != null) {
            sb.append("描述：").append(wi.getDescription()).append("\n");
        }
        if (wi.getAcceptanceCriteria() != null && !wi.getAcceptanceCriteria().isEmpty()) {
            sb.append("验收标准：").append(String.join("；", wi.getAcceptanceCriteria())).append("\n");
        }
        return sb.toString();
    }

    private String callDeepSeek(String userMessage) {
        String url = baseUrl + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<DeepSeekDTO.Message> messages = Arrays.asList(
                new DeepSeekDTO.Message("system", SYSTEM_PROMPT),
                new DeepSeekDTO.Message("user", userMessage)
        );

        DeepSeekDTO.ChatRequest request = new DeepSeekDTO.ChatRequest(
                model, messages, 0.3, 1024
        );

        HttpEntity<DeepSeekDTO.ChatRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<DeepSeekDTO.ChatResponse> response = restTemplate.postForEntity(
                url, entity, DeepSeekDTO.ChatResponse.class);

        DeepSeekDTO.ChatResponse body = response.getBody();
        if (body == null || body.getChoices() == null || body.getChoices().isEmpty()) {
            throw new RuntimeException("DeepSeek returned empty response");
        }

        String content = body.getChoices().get(0).getMessage().getContent();
        log.debug("DeepSeek response: {}", content);

        // Strip markdown code blocks if present
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }

    private AIAnalysisResponse parseResponse(String rawJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(rawJson,
                    new TypeReference<Map<String, Object>>() {});

            String summary = safeString(map.get("summary"), "分析结果解析异常");

            // Risks: handle both List and single object
            List<AIAnalysisResponse.RiskItem> risks = new ArrayList<>();
            Object risksObj = map.get("risks");
            if (risksObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> risksRaw = (List<Map<String, Object>>) risksObj;
                for (Map<String, Object> r : risksRaw) {
                    Severity level = parseSeverity(safeString(r.get("level"), "MEDIUM"));
                    risks.add(new AIAnalysisResponse.RiskItem(level, safeString(r.get("description"), "")));
                }
            }

            // Suggestions: handle both List and single object
            List<String> suggestions = new ArrayList<>();
            Object sugObj = map.get("suggestions");
            if (sugObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> rawList = (List<Object>) sugObj;
                for (Object item : rawList) {
                    suggestions.add(String.valueOf(item));
                }
            } else if (sugObj instanceof String) {
                suggestions.add((String) sugObj);
            }

            return AIAnalysisResponse.builder()
                    .summary(summary)
                    .risks(risks)
                    .suggestions(suggestions)
                    .analyzedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DeepSeek JSON response: " + rawJson, e);
        }
    }

    private Severity parseSeverity(String level) {
        if (level == null) return Severity.MEDIUM;
        try {
            return Severity.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Severity.MEDIUM;
        }
    }

    private String safeString(Object value, String defaultValue) {
        return value != null ? String.valueOf(value) : defaultValue;
    }

    private AIAnalysisResponse buildFallbackResponse(WorkItem wi, String errorMsg) {
        return AIAnalysisResponse.builder()
                .summary("DeepSeek API 调用失败: " + (errorMsg != null ? errorMsg.substring(0, Math.min(100, errorMsg.length())) : "未知错误"))
                .risks(Collections.singletonList(new AIAnalysisResponse.RiskItem(
                        Severity.HIGH, "AI 服务暂时不可用，请稍后重试")))
                .suggestions(Arrays.asList(
                        "请手动检查需求边界条件",
                        "确认验收标准是否完整",
                        "稍后重新触发 AI 分析"))
                .analyzedAt(LocalDateTime.now())
                .build();
    }
}

package com.clariflow.workflow.service;

import com.clariflow.workflow.common.ErrorCode;
import com.clariflow.workflow.common.exception.BusinessException;
import com.clariflow.workflow.model.dto.response.AIAnalysisResponse;
import com.clariflow.workflow.model.entity.WorkItem;
import com.clariflow.workflow.model.enums.Severity;
import com.clariflow.workflow.repository.WorkItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link AIAnalysisService}.
 *
 * <p>Generates structured analysis results based on keyword matching
 * in the work item's title and description. This is a placeholder
 * for a future real AI integration.</p>
 */
@Service
@ConditionalOnMissingBean(DeepSeekAIAnalysisService.class)
public class MockAIAnalysisServiceImpl implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(MockAIAnalysisServiceImpl.class);

    private final WorkItemMapper workItemMapper;

    public MockAIAnalysisServiceImpl(WorkItemMapper workItemMapper) {
        this.workItemMapper = workItemMapper;
    }

    @Override
    @Cacheable(value = "aiAnalysis", key = "#workItemId")
    public AIAnalysisResponse analyze(String workItemId) {
        WorkItem workItem = workItemMapper.selectById(workItemId);
        if (workItem == null) {
            throw new BusinessException(ErrorCode.WF_001, "工作项 " + workItemId + " 不存在");
        }

        String title = workItem.getTitle() != null ? workItem.getTitle() : "";
        String description = workItem.getDescription() != null ? workItem.getDescription() : "";

        // Generate mock summary based on title and description
        String summary = generateSummary(title, description);

        // Generate mock risks based on keyword analysis
        List<AIAnalysisResponse.RiskItem> risks = generateRisks(title, description);

        // Generate mock suggestions
        List<String> suggestions = generateSuggestions(title, description);

        log.info("Mock AI analysis completed for workItemId={}", workItemId);

        return AIAnalysisResponse.builder()
                .summary(summary)
                .risks(risks)
                .suggestions(suggestions)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generates a mock executive summary based on the work item content.
     */
    private String generateSummary(String title, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("基于对「").append(title).append("」的分析，");

        if (description.contains("需求") || description.contains("分析")) {
            sb.append("该工作项涉及需求分析相关工作，");
        }
        if (description.contains("开发") || description.contains("实现")) {
            sb.append("需要关注开发实现细节，");
        }
        if (description.contains("测试") || description.contains("质量")) {
            sb.append("测试和质量保障是需要重点关注的环节，");
        }
        if (description.contains("风险") || description.contains("问题")) {
            sb.append("已识别出潜在风险点需要进一步评估，");
        }
        if (description.contains("AI") || description.contains("智能")) {
            sb.append("该功能涉及AI/智能分析能力，");
        }
        if (description.contains("状态") || description.contains("流转") || description.contains("flow")) {
            sb.append("状态流转逻辑的正确性是核心关注点，");
        }

        sb.append("建议在进入开发阶段前完成需求澄清和风险识别。");
        return sb.toString();
    }

    /**
     * Generates mock risk items based on keyword analysis.
     */
    private List<AIAnalysisResponse.RiskItem> generateRisks(String title, String description) {
        List<AIAnalysisResponse.RiskItem> risks = new ArrayList<>();

        // Check for common risk patterns
        if (description.contains("状态") || description.contains("流转")) {
            risks.add(new AIAnalysisResponse.RiskItem(
                    Severity.HIGH,
                    "状态流转规则可能遗漏边界情况，建议补充完整的流转矩阵测试"));
        }

        if (description.contains("需求") && description.contains("澄清")) {
            risks.add(new AIAnalysisResponse.RiskItem(
                    Severity.MEDIUM,
                    "需求澄清流程可能存在循环依赖，建议明确澄清的截止条件"));
        }

        if (description.contains("AI") || description.contains("智能")) {
            risks.add(new AIAnalysisResponse.RiskItem(
                    Severity.MEDIUM,
                    "AI生成结果的准确性需要人工审核机制保障"));
        }

        if (description.contains("并发") || description.contains("冲突")) {
            risks.add(new AIAnalysisResponse.RiskItem(
                    Severity.HIGH,
                    "并发场景下的版本冲突处理需要完善的重试策略"));
        }

        // Default risk if none matched
        if (risks.isEmpty()) {
            risks.add(new AIAnalysisResponse.RiskItem(
                    Severity.LOW,
                    "整体风险可控，建议按标准流程推进"));
        }

        return risks;
    }

    /**
     * Generates mock actionable suggestions.
     */
    private List<String> generateSuggestions(String title, String description) {
        List<String> suggestions = new ArrayList<>();

        suggestions.add("在进入开发前确保所有 HIGH 级别的澄清问题已解决");

        if (description.contains("状态") || description.contains("流转")) {
            suggestions.add("编写完整的状态流转单元测试，覆盖所有合法和非法路径");
        }

        if (description.contains("需求")) {
            suggestions.add("建议与产品经理确认需求边界条件");
        }

        if (description.contains("AI") || description.contains("智能")) {
            suggestions.add("为AI分析结果添加置信度评分，便于人工判断采纳与否");
        }

        suggestions.add("设置明确的验收标准并通过Demo进行确认");
        suggestions.add("定期回顾工作项流转效率，识别流程瓶颈");

        return suggestions;
    }
}

package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.request.ClarificationCreateRequest;
import com.clariflow.workflow.model.dto.request.ClarificationResolveRequest;
import com.clariflow.workflow.model.dto.response.ClarificationResponse;

import java.util.List;

/**
 * 澄清问题管理的服务接口。
 *
 * <p>澄清问题用于识别和解决工作项需求中的模糊之处，
 * 在开发开始之前进行。HIGH 级别未解决的澄清问题会阻塞
 * 向 READY 和 IN_DEVELOPMENT 状态的流转。</p>
 */
public interface ClarificationService {

    /**
     * 向工作项添加澄清问题。
     *
     * @param workItemId 父工作项 ID
     * @param request    澄清问题创建请求
     * @return 已创建的澄清问题响应
     */
    ClarificationResponse addClarification(String workItemId, ClarificationCreateRequest request);

    /**
     * 获取工作项的所有澄清问题。
     *
     * @param workItemId 父工作项 ID
     * @return 澄清问题响应列表
     */
    List<ClarificationResponse> getClarifications(String workItemId);

    /**
     * 通过提供答案解决澄清问题。
     *
     * @param workItemId      父工作项 ID
     * @param clarificationId 澄清问题 ID
     * @param request         解决请求（答案）
     * @return 更新后的澄清问题响应
     */
    ClarificationResponse resolveClarification(String workItemId, Long clarificationId,
                                                ClarificationResolveRequest request);
}

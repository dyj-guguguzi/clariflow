package com.clariflow.workflow.service;

import com.clariflow.workflow.model.dto.request.TransitionRequest;
import com.clariflow.workflow.model.dto.response.TransitionResponse;
import com.clariflow.workflow.model.dto.response.WorkItemResponse;

import java.util.List;

/**
 * 状态流转管理的服务接口。
 *
 * <p>这是核心业务逻辑，执行状态机规则：
 * 合法流转、澄清阻塞规则和乐观锁。</p>
 */
public interface TransitionService {

    /**
     * 对指定工作项执行状态流转。
     *
     * <p>在单个事务中，此方法执行以下步骤：
     * <ol>
     *   <li>获取当前工作项</li>
     *   <li>验证流转是否允许（WF-002）</li>
     *   <li>目标状态为 READY 或 IN_DEVELOPMENT 时，
     *       检查是否有 HIGH+UNRESOLVED 的澄清阻塞（WF-003）</li>
     *   <li>使用乐观锁更新工作项状态</li>
     *   <li>记录流转历史</li>
     *   <li>返回更新后的工作项响应</li>
     * </ol>
     *
     * @param workItemId 工作项 ID
     * @param request    流转请求（目标状态、原因、操作人）
     * @return 更新后的工作项响应
     */
    WorkItemResponse executeTransition(String workItemId, TransitionRequest request);

    /**
     * 获取工作项的流转历史。
     *
     * @param workItemId 工作项 ID
     * @return 按时间倒序排列的流转记录列表
     */
    List<TransitionResponse> getTransitionHistory(String workItemId);
}

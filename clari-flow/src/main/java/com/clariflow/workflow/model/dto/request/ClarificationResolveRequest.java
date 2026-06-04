package com.clariflow.workflow.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 解决澄清的请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationResolveRequest {

    /** 澄清问题的答案 — 必填，最多 2000 个字符。 */
    @NotBlank(message = "答案不能为空")
    @Size(max = 2000, message = "答案不能超过2000个字符")
    private String answer;
}

package com.clariflow.workflow.model.dto.request;

import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 创建澄清问题的请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationCreateRequest {

    /** 澄清问题 — 必填，最多 1000 个字符。 */
    @NotBlank(message = "澄清问题不能为空")
    @Size(max = 1000, message = "澄清问题不能超过1000个字符")
    private String question;

    /** 澄清的严重程度。默认为 MEDIUM。 */
    @NotNull(message = "严重程度不能为空")
    private Severity severity = Severity.MEDIUM;
}

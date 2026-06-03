package com.clariflow.workflow.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Request DTO for resolving a clarification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationResolveRequest {

    /** Answer to the clarification question — required, max 2000 chars. */
    @NotBlank(message = "答案不能为空")
    @Size(max = 2000, message = "答案不能超过2000个字符")
    private String answer;
}

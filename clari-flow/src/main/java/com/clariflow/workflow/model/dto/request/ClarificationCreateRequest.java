package com.clariflow.workflow.model.dto.request;

import com.clariflow.workflow.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request DTO for creating a clarification question.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClarificationCreateRequest {

    /** The clarification question — required, max 1000 chars. */
    @NotBlank(message = "澄清问题不能为空")
    @Size(max = 1000, message = "澄清问题不能超过1000个字符")
    private String question;

    /** Severity of the clarification. Defaults to MEDIUM. */
    @NotNull(message = "严重程度不能为空")
    private Severity severity = Severity.MEDIUM;
}

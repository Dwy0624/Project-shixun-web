package org.example.springboot.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 测评提交DTO
 * @author system
 */
@Data
@Schema(description = "测评提交DTO")
public class EvaluationSubmitDTO {
    @NotNull(message = "测评ID不能为空")
    @Schema(description = "测评ID")
    private Long evaluationId;

    @NotEmpty(message = "答案不能为空")
    @Schema(description = "答题列表")
    private List<EvaluationAnswerDTO> answers;
}
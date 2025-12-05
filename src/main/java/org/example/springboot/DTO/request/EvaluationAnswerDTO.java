package org.example.springboot.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 测评答案DTO
 * @author system
 */
@Data
@Schema(description = "测评答案DTO")
public class EvaluationAnswerDTO {
    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID")
    private Long questionId;

    @NotEmpty(message = "选中的选项不能为空")
    @Schema(description = "选中的选项索引列表")
    private List<Integer> selectedOptions;
}
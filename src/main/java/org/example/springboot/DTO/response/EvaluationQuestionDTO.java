package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 测评题目DTO
 * @author system
 */
@Data
@Schema(description = "测评题目DTO")
public class EvaluationQuestionDTO {
    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "测评ID")
    private Long evaluationId;

    @Schema(description = "题目内容")
    private String content;

    @Schema(description = "选项列表")
    private List<String> options;

    @Schema(description = "分值权重")
    private Integer scoreWeight;

    @Schema(description = "题目类型：单选、多选")
    private String questionType;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
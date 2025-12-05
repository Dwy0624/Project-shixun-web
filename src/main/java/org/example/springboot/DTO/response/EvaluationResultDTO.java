package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 测评结果DTO
 * @author system
 */
@Data
@Schema(description = "测评结果DTO")
public class EvaluationResultDTO {
    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "用户ID")
    private String uid;

    @Schema(description = "量表ID")
    private String testid;

    @Schema(description = "量表名称")
    private String testname;

    @Schema(description = "得分")
    private String score;

    @Schema(description = "结果解读")
    private String result;

    @Schema(description = "测试时间")
    private String testtime;

    @Schema(description = "风险等级：0-正常 1-关注 2-预警 3-危机")
    private Integer riskLevel;

    @Schema(description = "风险描述")
    private String riskDescription;

    @Schema(description = "建议列表")
    private List<String> suggestions;

    @Schema(description = "答题记录，JSON格式")
    private String answers;
}
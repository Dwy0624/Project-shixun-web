package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 测评结果实体
 * @author alani
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_result")
@Schema(description = "测评结果实体")
public class AssessmentResult {
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "用户ID")
    @TableField("uid")
    private String uid;

    @Schema(description = "量表ID")
    @TableField("testid")
    private String testid;

    @Schema(description = "得分")
    @TableField("score")
    private String score;

    @Schema(description = "结果解读")
    @TableField("result")
    private String result;

    @Schema(description = "测试时间")
    @TableField("testtime")
    private String testtime;

    @Schema(description = "风险等级：0-正常 1-关注 2-预警 3-危机")
    @TableField("risk_level")
    private Integer riskLevel;

    @Schema(description = "建议")
    @TableField("suggestions")
    private String suggestions;

    @Schema(description = "答题记录，JSON格式")
    @TableField("answers")
    private String answers;

    /**
     * 判断是否存在风险
     */
    public boolean hasRisk() {
        return riskLevel != null && riskLevel > 0;
    }

    /**
     * 判断是否为严重风险
     */
    public boolean isSeriousRisk() {
        return riskLevel != null && riskLevel >= 2;
    }
}
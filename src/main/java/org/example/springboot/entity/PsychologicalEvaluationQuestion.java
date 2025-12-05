package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测评题目实体
 * @author system
 */
@Data
@TableName("psychological_evaluation_question")
@Schema(description = "测评题目实体")
public class PsychologicalEvaluationQuestion {
    @TableId(type = IdType.AUTO)
    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "测评ID")
    @TableField("evaluation_id")
    private Long evaluationId;

    @Schema(description = "题目内容")
    @TableField("content")
    private String content;

    @Schema(description = "选项，JSON格式")
    @TableField("options")
    private String options;

    @Schema(description = "分值权重")
    @TableField("score_weight")
    private Integer scoreWeight;

    @Schema(description = "题目类型：单选、多选")
    @TableField("question_type")
    private String questionType;

    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;
}
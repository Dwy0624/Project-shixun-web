package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 测评量表实体
 * @author alani
 */
@Data
@TableName("assessment_scale")
@Schema(description = "测评量表实体")
public class AssessmentScale {
    @TableId(type = IdType.AUTO)
    @Schema(description = "测评ID")
    private Integer id;

    @Schema(description = "量表名称")
    @TableField("testname")
    private String testname;

    @Schema(description = "测评类型")
    @TableField("testtype")
    private String testtype;

    @Schema(description = "量表描述")
    @TableField("description")
    private String description;

    @Schema(description = "状态：启用、禁用")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    /**
     * 判断量表是否可用
     */
    public boolean isEnabled() {
        return "启用".equals(status);
    }
}
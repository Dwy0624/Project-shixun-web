package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测评量表DTO
 * @author system
 */
@Data
@Schema(description = "测评量表DTO")
public class AssessmentScaleDTO {
    @Schema(description = "测评ID")
    private Integer id;

    @Schema(description = "量表名称")
    private String testname;

    @Schema(description = "测评类型")
    private String testtype;

    @Schema(description = "量表描述")
    private String description;

    @Schema(description = "状态")
    private String status;
}
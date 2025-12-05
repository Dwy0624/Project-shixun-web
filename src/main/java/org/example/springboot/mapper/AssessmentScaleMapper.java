package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.AssessmentScale;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测评量表Mapper
 * @author system
 */
@Mapper
public interface AssessmentScaleMapper extends BaseMapper<AssessmentScale> {
}
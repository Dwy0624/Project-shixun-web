package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.PsychologicalEvaluationQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测评题目Mapper
 * @author system
 */
@Mapper
public interface PsychologicalEvaluationQuestionMapper extends BaseMapper<PsychologicalEvaluationQuestion> {

    /**
     * 根据测评ID查询题目列表
     */
    List<PsychologicalEvaluationQuestion> selectByEvaluationId(@Param("evaluationId") Long evaluationId);
}
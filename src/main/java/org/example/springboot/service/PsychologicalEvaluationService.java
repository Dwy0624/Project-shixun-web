package org.example.springboot.service;

import org.example.springboot.DTO.request.EvaluationSubmitDTO;
import org.example.springboot.DTO.response.AssessmentScaleDTO;
import org.example.springboot.DTO.response.EvaluationQuestionDTO;
import org.example.springboot.DTO.response.EvaluationResultDTO;
import java.util.List;

/**
 * 心理测评服务接口
 * @author system
 */
public interface PsychologicalEvaluationService {

    /**
     * 获取测评量表列表
     */
    List<AssessmentScaleDTO> getEvaluationList();

    /**
     * 获取测评题目
     */
    List<EvaluationQuestionDTO> getEvaluationQuestions(Long evaluationId);

    /**
     * 提交测评答案
     */
    EvaluationResultDTO submitEvaluation(Long userId, EvaluationSubmitDTO submitDTO);

    /**
     * 获取用户的测评记录
     */
    List<EvaluationResultDTO> getUserEvaluationRecords(Long userId);

    /**
     * 获取用户某个测评的最新结果
     */
    EvaluationResultDTO getLatestEvaluationResult(Long userId, Long evaluationId);
}
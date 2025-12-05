package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.request.EvaluationSubmitDTO;
import org.example.springboot.DTO.response.AssessmentScaleDTO;
import org.example.springboot.DTO.response.EvaluationQuestionDTO;
import org.example.springboot.DTO.response.EvaluationResultDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.PsychologicalEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心理测评控制器
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/psychological-evaluation")
@Tag(name = "心理测评", description = "心理测评相关接口")
public class PsychologicalEvaluationController {

    @Autowired
    private PsychologicalEvaluationService evaluationService;

    @Operation(summary = "获取测评量表列表", description = "获取所有可用的心理测评量表")
    @GetMapping("/scales")
    public Result<List<AssessmentScaleDTO>> getEvaluationScales() {
        try {
            List<AssessmentScaleDTO> scales = evaluationService.getEvaluationList();
            return Result.success(scales);
        } catch (Exception e) {
            log.error("获取测评量表失败", e);
            return Result.error("获取测评量表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取测评题目", description = "根据测评ID获取对应的题目列表")
    @GetMapping("/questions/{evaluationId}")
    public Result<List<EvaluationQuestionDTO>> getEvaluationQuestions(@PathVariable Long evaluationId) {
        try {
            List<EvaluationQuestionDTO> questions = evaluationService.getEvaluationQuestions(evaluationId);
            return Result.success(questions);
        } catch (Exception e) {
            log.error("获取测评题目失败，evaluationId: {}", evaluationId, e);
            return Result.error("获取测评题目失败: " + e.getMessage());
        }
    }

    @Operation(summary = "提交测评答案", description = "提交用户的测评答案并获取结果")
    @PostMapping("/submit/{userId}")
    public Result<EvaluationResultDTO> submitEvaluation(
            @PathVariable Long userId,
            @Validated @RequestBody EvaluationSubmitDTO submitDTO) {
        try {
            EvaluationResultDTO result = evaluationService.submitEvaluation(userId, submitDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("提交测评答案失败，userId: {}", userId, e);
            return Result.error("提交测评失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户测评记录", description = "获取指定用户的所有测评记录")
    @GetMapping("/records/{userId}")
    public Result<List<EvaluationResultDTO>> getUserEvaluationRecords(@PathVariable Long userId) {
        try {
            List<EvaluationResultDTO> records = evaluationService.getUserEvaluationRecords(userId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取用户测评记录失败，userId: {}", userId, e);
            return Result.error("获取测评记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户最新测评结果", description = "获取指定用户某个测评的最新结果")
    @GetMapping("/latest/{userId}/{evaluationId}")
    public Result<EvaluationResultDTO> getLatestEvaluationResult(
            @PathVariable Long userId,
            @PathVariable Long evaluationId) {
        try {
            EvaluationResultDTO result = evaluationService.getLatestEvaluationResult(userId, evaluationId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户最新测评结果失败，userId: {}, evaluationId: {}", userId, evaluationId, e);
            return Result.error("获取最新测评结果失败: " + e.getMessage());
        }
    }
}
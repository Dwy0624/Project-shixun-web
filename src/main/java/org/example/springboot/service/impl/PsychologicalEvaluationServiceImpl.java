package org.example.springboot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.request.EvaluationAnswerDTO;
import org.example.springboot.DTO.request.EvaluationSubmitDTO;
import org.example.springboot.DTO.response.AssessmentScaleDTO;
import org.example.springboot.DTO.response.EvaluationQuestionDTO;
import org.example.springboot.DTO.response.EvaluationResultDTO;
import org.example.springboot.entity.AssessmentResult;
import org.example.springboot.entity.AssessmentScale;
import org.example.springboot.entity.PsychologicalEvaluationQuestion;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.AssessmentResultMapper;
import org.example.springboot.mapper.AssessmentScaleMapper;
import org.example.springboot.mapper.PsychologicalEvaluationQuestionMapper;
import org.example.springboot.service.PsychologicalEvaluationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 心理测评服务实现
 * @author system
 */
@Slf4j
@Service
public class PsychologicalEvaluationServiceImpl implements PsychologicalEvaluationService {

    @Autowired
    private PsychologicalEvaluationQuestionMapper questionMapper;

    @Autowired
    private AssessmentResultMapper resultMapper;

    @Autowired
    private AssessmentScaleMapper scaleMapper;

    @Autowired
    private ChatClient chatClient;

    @Override
    public List<AssessmentScaleDTO> getEvaluationList() {
        log.info("获取测评量表列表");
        // 查询启用的量表
        List<AssessmentScale> scales = scaleMapper.selectList(
                new LambdaQueryWrapper<AssessmentScale>()
                        .eq(AssessmentScale::getStatus, "启用")
        );
        return scales.stream().map(scale -> {
            AssessmentScaleDTO dto = new AssessmentScaleDTO();
            BeanUtil.copyProperties(scale, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EvaluationQuestionDTO> getEvaluationQuestions(Long evaluationId) {
        log.info("获取测评题目，evaluationId: {}", evaluationId);
        if (evaluationId == null) {
            throw new BusinessException("测评ID不能为空");
        }

        List<PsychologicalEvaluationQuestion> questions = questionMapper.selectByEvaluationId(evaluationId);
        if (questions.isEmpty()) {
            throw new BusinessException("该测评暂无题目");
        }

        return questions.stream().map(question -> {
            EvaluationQuestionDTO dto = new EvaluationQuestionDTO();
            BeanUtil.copyProperties(question, dto);
            dto.setOptions(JSONUtil.parseArray(question.getOptions()).toList(String.class));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EvaluationResultDTO submitEvaluation(Long userId, EvaluationSubmitDTO submitDTO) {
        log.info("用户提交测评，userId: {}, evaluationId: {}", userId, submitDTO.getEvaluationId());

        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 获取测评量表信息
        AssessmentScale scale = scaleMapper.selectById(submitDTO.getEvaluationId());
        if (scale == null || !scale.isEnabled()) {
            throw new BusinessException("测评量表不存在或已禁用");
        }

        // 获取题目列表
        List<PsychologicalEvaluationQuestion> questions = questionMapper.selectByEvaluationId(submitDTO.getEvaluationId());
        if (questions.isEmpty()) {
            throw new BusinessException("该测评暂无题目");
        }

        // 1. 计算总分
        int totalScore = calculateTotalScore(submitDTO.getAnswers(), questions);

        // 2. 生成测评结果分析（调用AI）
        String resultAnalysis = generateEvaluationAnalysis(submitDTO, totalScore, scale.getTestname());

        // 3. 评估风险等级
        int riskLevel = evaluateRiskLevel(totalScore, scale.getTesttype(), resultAnalysis);

        // 4. 保存测评记录
        AssessmentResult record = buildAssessmentResult(userId, submitDTO, totalScore,
                resultAnalysis, riskLevel, scale);
        resultMapper.insert(record);

        // 5. 构建并返回结果DTO
        return convertToResultDTO(record, scale);
    }

    @Override
    public List<EvaluationResultDTO> getUserEvaluationRecords(Long userId) {
        log.info("获取用户测评记录，userId: {}", userId);
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        List<AssessmentResult> records = resultMapper.selectByUserId(String.valueOf(userId));
        List<EvaluationResultDTO> resultList = new ArrayList<>();

        for (AssessmentResult record : records) {
            AssessmentScale scale = scaleMapper.selectById(Integer.parseInt(record.getTestid()));
            if (scale != null) {
                resultList.add(convertToResultDTO(record, scale));
            }
        }

        return resultList;
    }

    @Override
    public EvaluationResultDTO getLatestEvaluationResult(Long userId, Long evaluationId) {
        log.info("获取用户最新测评结果，userId: {}, evaluationId: {}", userId, evaluationId);
        if (userId == null || evaluationId == null) {
            throw new BusinessException("用户ID和测评ID不能为空");
        }

        AssessmentResult record = resultMapper.selectLatestByUserAndTest(
                String.valueOf(userId), String.valueOf(evaluationId));

        if (record == null) {
            return null;
        }

        AssessmentScale scale = scaleMapper.selectById(evaluationId);
        return convertToResultDTO(record, scale);
    }

    /**
     * 计算总分
     */
    private int calculateTotalScore(List<EvaluationAnswerDTO> answers, List<PsychologicalEvaluationQuestion> questions) {
        int totalScore = 0;

        for (EvaluationAnswerDTO answer : answers) {
            PsychologicalEvaluationQuestion question = questions.stream()
                    .filter(q -> q.getId().equals(answer.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("题目不存在: " + answer.getQuestionId()));

            // 根据选项计算得分
            totalScore += question.getScoreWeight() * answer.getSelectedOptions().size();
        }

        return totalScore;
    }

    /**
     * 生成测评分析
     */
    private String generateEvaluationAnalysis(EvaluationSubmitDTO submitDTO, int totalScore, String testName) {
        try {
            // 构建提示词
            StringBuilder prompt = new StringBuilder();
            prompt.append("请分析用户在[").append(testName).append("]中的测评结果，用户得分为").append(totalScore).append("分。");
            prompt.append("请从专业心理学角度进行解读，分析用户可能的心理状态，");
            prompt.append("语言要温和专业，避免使用过于专业的术语，让用户容易理解。");
            prompt.append("分析内容控制在200-300字左右。");

            // 调用AI接口生成分析
            return chatClient.prompt()
                    .user(prompt.toString())
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("生成测评分析失败", e);
            return "系统暂时无法生成详细分析，请稍后再试。您的测评得分为：" + totalScore + "分。";
        }
    }

    /**
     * 评估风险等级
     */
    private int evaluateRiskLevel(int totalScore, String testType, String analysis) {
        // 这里根据不同测评类型和得分实现风险等级评估逻辑
        // 实际应用中应该根据每种测评量表的标准分进行计算
        if ("抑郁自评".equals(testType)) {
            if (totalScore >= 70) return 3;
            if (totalScore >= 50) return 2;
            if (totalScore >= 30) return 1;
        } else if ("焦虑自评".equals(testType)) {
            if (totalScore >= 60) return 3;
            if (totalScore >= 40) return 2;
            if (totalScore >= 25) return 1;
        } else {
            // 通用评估逻辑
            if (analysis.contains("严重") || analysis.contains("危机") || analysis.contains("紧急")) {
                return 3;
            } else if (analysis.contains("明显") || analysis.contains("需要关注") || analysis.contains("建议咨询")) {
                return 2;
            } else if (analysis.contains("轻微") || analysis.contains("注意")) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 构建测评结果记录
     */
    private AssessmentResult buildAssessmentResult(Long userId, EvaluationSubmitDTO submitDTO,
                                                   int totalScore, String resultAnalysis,
                                                   int riskLevel, AssessmentScale scale) {
        return AssessmentResult.builder()
                .uid(String.valueOf(userId))
                .testid(String.valueOf(submitDTO.getEvaluationId()))
                .score(String.valueOf(totalScore))
                .result(resultAnalysis)
                .riskLevel(riskLevel)
                .testtime(DateUtil.now())
                .answers(JSONUtil.toJsonStr(submitDTO.getAnswers()))
                .suggestions(generateSuggestions(riskLevel, scale.getTesttype()))
                .build();
    }

    /**
     * 生成建议
     */
    private String generateSuggestions(int riskLevel, String testType) {
        List<String> suggestions = new ArrayList<>();

        if (riskLevel == 3) {
            suggestions.add("建议立即联系专业心理医生进行咨询");
            suggestions.add("可联系学校心理咨询中心获取紧急帮助");
            suggestions.add("与亲友保持联系，避免独处");
        } else if (riskLevel == 2) {
            suggestions.add("建议在一周内寻求专业心理咨询");
            suggestions.add("尝试规律作息，适当运动改善情绪");
            suggestions.add("与信任的人分享你的感受");
        } else if (riskLevel == 1) {
            suggestions.add("注意调整生活节奏，保证充足休息");
            suggestions.add("尝试放松训练，缓解当前状态");
            suggestions.add("保持社交活动，避免过度独处");
        } else {
            suggestions.add("保持当前良好状态，继续规律生活");
            suggestions.add("坚持适度运动，有益心理健康");
            suggestions.add("可定期进行自我评估，关注心理变化");
        }

        return JSONUtil.toJsonStr(suggestions);
    }

    /**
     * 转换为结果DTO
     */
    private EvaluationResultDTO convertToResultDTO(AssessmentResult record, AssessmentScale scale) {
        EvaluationResultDTO dto = new EvaluationResultDTO();
        BeanUtil.copyProperties(record, dto);
        dto.setTestname(scale.getTestname());
        dto.setRiskDescription(getRiskDescription(record.getRiskLevel()));
        dto.setSuggestions(JSONUtil.parseArray(record.getSuggestions()).toList(String.class));
        return dto;
    }

    /**
     * 获取风险描述
     */
    private String getRiskDescription(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        return switch (riskLevel) {
            case 0 -> "情绪稳定";
            case 1 -> "需要关注";
            case 2 -> "建议专业咨询";
            case 3 -> "需要紧急干预";
            default -> "未知";
        };
    }
}
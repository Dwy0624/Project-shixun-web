<template>
  <div class="assessment-result-page">
    <div class="container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-loading-spinner></el-loading-spinner>
        <p>正在加载测评结果...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <el-icon class="error-icon"><WarningFilled /></el-icon>
        <p>{{ error }}</p>
        <el-button @click="loadAssessmentData">重新加载</el-button>
        <el-button @click="goBack">返回测评列表</el-button>
      </div>

      <!-- 主内容 - 仅在数据加载完成后显示 -->
      <el-card v-else class="result-card">
        <div class="result-header">
          <h1>测评结果报告</h1>
          <div class="result-meta">
            <span>测评名称: {{ assessment.title }}</span>
            <span>测评日期: {{ new Date().toLocaleDateString() }}</span>
          </div>
        </div>

        <div class="score-section">
          <div class="score-circle">
            <span class="score-value">{{ score }}</span>
            <span class="score-label">总分</span>
          </div>
          <div class="score-interpretation">
            <h3>结果解读</h3>
            <p>{{ interpretation }}</p>
          </div>
        </div>

        <div class="details-section">
          <h2>答题详情</h2>
          <el-collapse v-model="activeNames">
            <el-collapse-item
                v-for="(question, index) in assessment.questions"
                :key="index"
                :title="`第${index + 1}题: ${question.content}`"
                :name="index.toString()"
            >
              <div class="question-detail">
                <p>您的选择: {{ question.options[answers[index]] }}</p>
                <p>得分: {{ questionScores[index]?.score || 0 }}</p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <div class="actions-section">
          <el-button @click="goBack">返回测评列表</el-button>
          <el-button type="primary" @click="reTest">重新测评</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElCard, ElCollapse, ElCollapseItem, ElButton, ElLoading, ElIcon } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { id } = route.params

// 状态管理
const loading = ref(true)
const error = ref('')
const answers = ref({})
const score = ref(0)

// 测评数据
const assessment = ref(null)
const questionScores = ref([])
const activeNames = ref(['0']) // 默认展开第一题

// 初始化数据
onMounted(() => {
  // 解析路由参数，添加错误处理
  try {
    answers.value = JSON.parse(route.query.answers || '{}')
    score.value = route.query.score || 0
  } catch (e) {
    error.value = '解析测评数据失败，请返回重新测评'
    loading.value = false
    return
  }

  // 加载测评数据
  loadAssessmentData()
})

// 结果解读 - 确保assessment存在再执行
const interpretation = computed(() => {
  if (!assessment.value) return ''

  // 根据不同测评类型和得分提供解读
  switch (assessment.value.id) {
    case 1:
      return getMentalHealthInterpretation(score.value)
    case 2:
      return getDepressionInterpretation(score.value)
    case 3:
      return getAnxietyInterpretation(score.value)
    case 4:
      return getPersonalityInterpretation(score.value)
    case 5:
      return getSleepQualityInterpretation(score.value)
    case 6:
      return getLifeEventInterpretation(score.value)
    default:
      return '测评结果正常'
  }
})

// 各类解读函数保持不变
const getSleepQualityInterpretation = (score) => {
  if (score <= 3) return '您的睡眠质量良好，继续保持当前的作息习惯和生活方式。'
  if (score <= 7) return '您存在轻微的睡眠问题，建议调整作息时间，睡前避免使用电子设备。'
  if (score <= 12) return '您的睡眠质量较差，可能存在睡眠障碍，建议改善睡眠环境和习惯。'
  return '您的睡眠问题较为严重，建议咨询专业医生进行进一步评估和治疗。'
}

const getLifeEventInterpretation = (score) => {
  if (score <= 3) return '您近期生活压力较小，心理状态稳定，继续保持积极的生活态度。'
  if (score <= 7) return '您正面临一些生活压力，但仍在可承受范围内，适当放松和调整即可。'
  if (score <= 12) return '您面临较大的生活压力，建议寻求家人朋友的支持，适当减压。'
  return '您正经历严重的生活压力，心理状态可能受到较大影响，建议寻求专业心理咨询帮助。'
}

const getMentalHealthInterpretation = (score) => {
  if (score <= 3) return '您的心理健康状态良好，保持积极乐观的生活态度。'
  if (score <= 7) return '您存在轻微的心理困扰，适当休息和放松即可缓解。'
  if (score <= 12) return '您的心理状态需要关注，建议尝试放松训练或与亲友倾诉。'
  return '您可能存在较明显的心理问题，建议寻求专业心理咨询师的帮助。'
}

const getDepressionInterpretation = (score) => {
  if (score <= 2) return '未发现明显抑郁倾向，保持良好的生活状态。'
  if (score <= 5) return '存在轻微抑郁情绪，注意自我调节和休息。'
  if (score <= 8) return '有一定抑郁倾向，建议增加社交活动，适当运动。'
  return '抑郁倾向明显，建议寻求专业心理评估和帮助。'
}

const getAnxietyInterpretation = (score) => {
  if (score <= 2) return '您的情绪状态稳定，几乎没有焦虑症状。'
  if (score <= 5) return '您存在轻微的焦虑情绪，通常不会影响日常生活。'
  if (score <= 8) return '您有中度焦虑症状，可能会对生活产生一定影响。'
  return '您的焦虑症状较为明显，建议寻求专业帮助进行调节。'
}

const getPersonalityInterpretation = (score) => {
  if (score <= 8) return '您偏向内向、谨慎，做事有条理，情绪较为稳定。'
  if (score <= 12) return '您的人格特质较为平衡，在不同情境下能较好地适应。'
  if (score <= 16) return '您性格外向，乐于社交，对新事物充满好奇。'
  return '您具有鲜明的人格特质，富有创造力和探索精神。'
}

const loadAssessmentData = () => {
  // 重置状态
  loading.value = true
  error.value = ''

  // 模拟API请求
  setTimeout(() => {
    try {
      // 完整的测评数据，包含所有6个测评
      const assessmentData = {
        1: {
          id: 1,
          title: '心理健康状态评估',
          duration: 10,
          difficulty: 1,
          questions: [
            { content: '我感到心情愉快', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我对未来充满希望', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我能够专注于工作或学习', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我觉得自己是一个失败者', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我觉得生活不值得继续', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我能享受日常活动', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我感到孤独或与他人隔离', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我对自己有信心', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我能有效地处理问题', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我感到焦虑或担忧', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我能控制自己的情绪', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我感到精力充沛', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我对自己的外貌满意', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我有良好的人际关系', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我能从困难中恢复过来', options: ['没有', '轻度', '中度', '重度'] }
          ]
        },
        2: {
          id: 2,
          title: '抑郁倾向筛查',
          duration: 8,
          difficulty: 1,
          questions: [
            { content: '我感到情绪低落', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我对事物失去兴趣', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我难以入睡或保持睡眠', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我感到疲倦或缺乏精力', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我觉得自己毫无价值', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我难以集中注意力', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我有自杀的想法', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我食欲明显变化', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我感到内疚或自责', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我说话或行动变慢', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我对性失去兴趣', options: ['没有', '轻度', '中度', '重度'] },
            { content: '我对未来感到绝望', options: ['没有', '轻度', '中度', '重度'] }
          ]
        },
        3: {
          id: 3,
          title: '焦虑程度测试',
          duration: 12,
          difficulty: 2,
          questions: [
            { content: '我感到害怕、惊恐或紧张', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到不安、紧张或惊恐', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到心跳加速', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到呼吸困难', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到头晕或晕眩', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到口干', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我难以放松', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我容易受惊', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到肌肉紧张', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我难以入睡', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我对许多事情感到担忧', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到坐立不安', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到胸闷', options: ['从不', '几天', '一半以上的日子', '几乎每天'] },
            { content: '我感到恶心或腹部不适', options: ['从不', '几天', '一半以上的日子', '几乎每天'] }
          ]
        },
        4: {
          id: 4,
          title: '人格特质分析',
          duration: 15,
          difficulty: 2,
          questions: [
            { content: '我喜欢与他人交往', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我做事有条理', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我容易信任他人', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我情绪稳定', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我喜欢思考抽象的概念', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我经常感到焦虑', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我是一个有创造力的人', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我倾向于服从权威', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我容易理解他人的感受', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我喜欢冒险', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我做事认真负责', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我容易生气', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我对新事物持开放态度', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我喜欢独处', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] },
            { content: '我注重细节', options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合'] }
          ]
        },
        5: {
          id: 5,
          title: '匹兹堡睡眠质量指数',
          duration: 12,
          difficulty: 1,
          questions: [
            { content: '我入睡时间通常需要30分钟以上', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我夜间醒来或早醒', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠质量很差', options: ['没有', '较少', '较多', '一直如此'] },
            { content: '我白天感到困倦', options: ['没有', '较少', '较多', '一直如此'] },
            { content: '我需要服药才能入睡', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠持续时间不足5小时', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠中打鼾', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠中呼吸暂停', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠中感到窒息', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠中咳嗽或鼾声', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠中感到冷或热', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我睡眠后仍感到疲惫', options: ['没有', '较少', '较多', '一直如此'] },
            { content: '我因如厕而中断睡眠', options: ['没有', '少于1周', '1-2周', '3周以上'] },
            { content: '我因疼痛而中断睡眠', options: ['没有', '少于1周', '1-2周', '3周以上'] }
          ]
        },
        6: {
          id: 6,
          title: '生活事件量表--压力测评',
          duration: 10,
          difficulty: 2,
          questions: [
            { content: '工作调动或失业', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '家庭重大变故', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '经济状况显著变化', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '人际关系冲突', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '健康状况变化', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '家庭成员去世', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '离婚或分居', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '结婚', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '怀孕或生育', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '子女离家', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '与配偶争吵频率增加', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '法律纠纷', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '退休', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '子女问题', options: ['未发生', '轻度影响', '中度影响', '重度影响'] },
            { content: '重要节日或纪念日带来压力', options: ['未发生', '轻度影响', '中度影响', '重度影响'] }
          ]
        }
      }

      // 验证id是否有效
      if (!id || !assessmentData[id]) {
        throw new Error('未找到对应的测评数据')
      }

      assessment.value = assessmentData[id]
      calculateQuestionScores()
      loading.value = false
    } catch (err) {
      error.value = err.message || '加载测评结果失败'
      loading.value = false
    }
  }, 500)
}

// 计算每题得分 - 添加安全检查
const calculateQuestionScores = () => {
  if (!assessment.value || !assessment.value.questions || !answers.value) {
    questionScores.value = []
    return
  }

  const scores = []
  Object.entries(answers.value).forEach(([index, selectedOption]) => {
    let score = 0
    if ([1, 2, 3, 5, 6].includes(assessment.value.id)) {
      score = selectedOption
    } else if (assessment.value.id === 4) {
      score = selectedOption + 1
    }

    scores.push({
      questionIndex: parseInt(index) + 1,
      score: score
    })
  })

  questionScores.value = scores
}

// 返回列表
const goBack = () => {
  router.push('/psychological-assessment')
}

// 重新测评
const reTest = () => {
  router.push(`/psychological-assessment/${id}`)
}
</script>

<style scoped>
.assessment-result-page {
  padding: 2rem 0;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
}

.loading-container, .error-container {
  text-align: center;
  padding: 3rem 1rem;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.loading-container p {
  margin-top: 1rem;
  color: #666;
}

.error-container {
  color: #F56C6C;
}

.error-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.error-container p {
  margin-bottom: 1.5rem;
}

.result-card {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-radius: 10px;
  overflow: hidden;
}

.result-header {
  padding: 1.5rem;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.result-header h1 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.result-meta {
  display: flex;
  justify-content: center;
  gap: 2rem;
  color: #666;
  font-size: 0.9rem;
}

.score-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 2rem;
  gap: 2rem;
  border-bottom: 1px solid #eee;
}

.score-circle {
  flex: 1;
  min-width: 200px;
  text-align: center;
  padding: 1rem;
}

.score-value {
  display: inline-block;
  font-size: 3rem;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
}

.score-label {
  display: block;
  color: #666;
  margin-top: 0.5rem;
}

.score-interpretation {
  flex: 2;
  min-width: 300px;
}

.score-interpretation h3 {
  margin-top: 0;
  color: #333;
}

.score-interpretation p {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #555;
}

.details-section {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #eee;
}

.details-section h2 {
  color: #2c3e50;
  margin-top: 0;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #409EFF;
  display: inline-block;
}

.question-detail {
  padding: 1rem;
  color: #555;
}

.question-detail p {
  margin: 0.5rem 0;
}

.actions-section {
  display: flex;
  justify-content: center;
  gap: 1rem;
  padding: 1.5rem;
  background-color: #fafafa;
}

@media (max-width: 768px) {
  .score-section {
    flex-direction: column;
    padding: 1.5rem;
  }

  .result-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
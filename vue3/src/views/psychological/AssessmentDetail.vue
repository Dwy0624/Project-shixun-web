<template>
  <div class="assessment-detail-page">
    <!-- 加载状态 -->
    <div v-if="!assessment" class="loading-container">
      <el-loading :visible="true" text="正在加载测评数据..."></el-loading>
    </div>

    <!-- 测评内容（数据加载完成后才显示） -->
    <div v-else class="assessment-detail-container">
      <div class="assessment-header">
        <h1>{{ assessment.title }}</h1>
        <div class="assessment-meta">
          <span>预计时长: {{ assessment.duration }}分钟</span>
          <span>难度: {{ getDifficultyText(assessment.difficulty) }}</span>
          <span>进度: {{ currentQuestionIndex + 1 }}/{{ assessment.questions.length }}</span>
        </div>
      </div>

      <div class="question-card">
        <div class="question-number">第 {{ currentQuestionIndex + 1 }} 题</div>
        <div class="question-content">{{ currentQuestion.content }}</div>

        <div class="options">
          <div
              class="option-item"
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              @click="selectOption(index)"
              :class="{ selected: selectedOption === index }"
          >
            {{ String.fromCharCode(65 + index) }}. {{ option }}
          </div>
        </div>
      </div>

      <div class="navigation-buttons">
        <el-button
            type="default"
            @click="prevQuestion"
            :disabled="currentQuestionIndex === 0"
        >
          上一题
        </el-button>
        <el-button
            type="primary"
            @click="nextQuestion"
            :disabled="selectedOption === null"
        >
          {{ currentQuestionIndex === assessment.questions.length - 1 ? '提交测评' : '下一题' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/store/user'
import { redirectToLogin } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 测评ID
const { id } = route.params

// 测评数据 - 初始化为null
const assessment = ref(null)
const currentQuestionIndex = ref(0)
const selectedOption = ref(null)
const answers = ref({})

// 检查登录状态并加载数据
onMounted(() => {
  if (!userStore.token) {
    redirectToLogin()
    return
  }
  // 加载测评数据
  loadAssessmentData()
})

// 加载测评数据
const loadAssessmentData = () => {
  // 模拟API请求延迟
  setTimeout(() => {
    try {
      // 根据测评ID获取不同的测评数据，补充了ID为5和6的测评数据
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

      // 验证数据是否存在
      if (!assessmentData[id]) {
        throw new Error('未找到对应的测评数据')
      }

      assessment.value = assessmentData[id]
    } catch (error) {
      ElMessage.error(error.message || '加载测评数据失败')
      // 跳回测评列表
      setTimeout(() => router.push('/psychological-assessment'), 1500)
    }
  }, 500) // 模拟网络请求延迟
}

// 获取当前问题
const currentQuestion = ref(null)
watch(
    () => [assessment.value, currentQuestionIndex.value],
    () => {
      if (assessment.value && assessment.value.questions) {
        currentQuestion.value = assessment.value.questions[currentQuestionIndex.value]
        // 恢复已选择的答案
        selectedOption.value = answers.value[currentQuestionIndex.value] !== undefined
            ? answers.value[currentQuestionIndex.value]
            : null
      }
    },
    { immediate: true }
)

// 选择选项
const selectOption = (index) => {
  selectedOption.value = index
  answers.value[currentQuestionIndex.value] = index
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 下一题/提交
const nextQuestion = () => {
  // 确保assessment数据已加载
  if (!assessment.value) return

  if (currentQuestionIndex.value < assessment.value.questions.length - 1) {
    currentQuestionIndex.value++
  } else {
    // 提交测评
    submitAssessment()
  }
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const texts = ['简单', '中等', '复杂']
  return texts[difficulty - 1] || '简单'
}

// 提交测评
const submitAssessment = () => {
  // 验证是否完成所有题目
  if (Object.keys(answers.value).length !== assessment.value.questions.length) {
    ElMessage.warning('请完成所有题目后再提交')
    return
  }

  // 计算得分
  const scoreData = calculateScore()

  // 实际项目中应将答案提交到后端
  ElMessage.success('测评提交成功！正在生成报告...')

  // 携带答案和得分跳转到结果页
  setTimeout(() => {
    router.push({
      path: `/psychological-assessment/result/${id}`,
      query: {
        answers: JSON.stringify(answers.value),
        score: scoreData.totalScore
      }
    })
  }, 1500)
}

// 计算得分的方法
const calculateScore = () => {
  let totalScore = 0
  const questionScores = []

  // 根据不同测评类型计算得分
  Object.entries(answers.value).forEach(([index, selectedOption]) => {
    // 获取当前问题对象
    const question = assessment.value.questions[parseInt(index)];
    let score = 0

    // 为不同选项赋予不同分值
    if ([1, 2, 3, 5, 6].includes(assessment.value.id)) {
      // 心理健康、抑郁、焦虑、睡眠质量、生活事件压力：选项对应0-3分
      score = selectedOption
    } else if (assessment.value.id === 4) {
      // 人格特质分析：选项对应1-5分
      score = selectedOption + 1
    }

    totalScore += score
    questionScores.push({
      questionIndex: parseInt(index) + 1,
      questionContent: question.content,
      score: score
    })
  })

  return {
    totalScore,
    questionScores,
    totalQuestions: assessment.value.questions.length
  }
}
</script>

<style scoped>
.assessment-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 2rem;
}

.loading-container {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.assessment-detail-container {
  max-width: 800px;
  margin: 0 auto;
}

.assessment-header {
  text-align: center;
  margin-bottom: 2rem;
}

.assessment-header h1 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.assessment-meta {
  display: flex;
  justify-content: center;
  gap: 2rem;
  color: #666;
  font-size: 0.9rem;
}

.question-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.question-number {
  color: #409EFF;
  font-weight: 600;
  margin-bottom: 1rem;
}

.question-content {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.option-item {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

.option-item.selected {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
  font-weight: 500;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
}
</style>
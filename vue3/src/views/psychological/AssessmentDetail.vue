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
      // 根据测评ID获取不同的测评数据
      const assessmentData = {
        1: {
          id: 1,
          title: '心理健康状态评估',
          duration: 10,
          difficulty: 1,
          questions: [
            {
              content: '我感到心情愉快',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对未来充满希望',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我能够专注于工作或学习',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我觉得自己是一个失败者',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我觉得生活不值得继续',
              options: ['没有', '轻度', '中度', '重度']
            }
          ]
        },
        2: {
          id: 2,
          title: '抑郁倾向筛查',
          duration: 8,
          difficulty: 1,
          questions: [
            {
              content: '我感到情绪低落',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对事物失去兴趣',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我难以入睡或保持睡眠',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我感到疲倦或缺乏精力',
              options: ['没有', '轻度', '中度', '重度']
            }
          ]
        },
        3: {
          id: 3,
          title: '焦虑程度测试',
          duration: 12,
          difficulty: 2,
          questions: [
            {
              content: '我感到害怕、惊恐或紧张',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到不安、紧张或惊恐',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到心跳加速',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到呼吸困难',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            }
          ]
        },
        4: {
          id: 4,
          title: '人格特质分析',
          duration: 15,
          difficulty: 2,
          questions: [
            {
              content: '我喜欢与他人交往',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我做事有条理',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我容易信任他人',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我情绪稳定',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我喜欢思考抽象的概念',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            }
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
// 计算得分的方法
const calculateScore = () => {
  let totalScore = 0
  const questionScores = []

  // 根据不同测评类型计算得分
  Object.entries(answers.value).forEach(([index, selectedOption]) => {
    // 获取当前问题对象（保留用于后续可能的扩展）
    const question = assessment.value.questions[parseInt(index)];
    let score = 0

    // 为不同选项赋予不同分值
    if (assessment.value.id === 1 || assessment.value.id === 3) {
      // 心理健康状态评估和焦虑程度测试：选项对应0-3分
      score = selectedOption
    } else if (assessment.value.id === 2) {
      // 抑郁倾向筛查：选项对应0-3分
      score = selectedOption
    } else if (assessment.value.id === 4) {
      // 人格特质分析：选项对应1-5分
      score = selectedOption + 1
    }

    totalScore += score
    questionScores.push({
      questionIndex: parseInt(index) + 1,
      // 可以在这里使用question变量，例如添加问题内容
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
  margin-bottom: 2rem;
  text-align: center;
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
}

.question-card {
  background: #fff;
  border-radius: 10px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.question-number {
  color: #409EFF;
  font-weight: bold;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.question-content {
  font-size: 1.2rem;
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
  border: 1px solid #eee;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #409EFF;
  background-color: #f5faff;
}

.option-item.selected {
  border-color: #409EFF;
  background-color: #e6f7ff;
  color: #1890ff;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
}
</style>
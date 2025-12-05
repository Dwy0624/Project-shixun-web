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
              content: '过去两周内，我感到情绪低落、沮丧或绝望',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我对做事情几乎没有兴趣或乐趣',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我难以入睡或保持睡眠，或睡得太多',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我感到疲倦或没有精力',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我感到紧张、焦虑或坐立不安',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我难以集中注意力做事情',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我感到自卑或觉得自己是个失败者',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我有过伤害自己的想法',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我的胃口比平时差很多或好很多',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我感到孤独，即使周围有人',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我对未来感到悲观',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我感到自己的生活没有意义',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '过去两周内，我感到难以做出决定',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
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
              content: '我感到心情沮丧，郁闷',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对未来感到希望',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我觉得自己是一个有用的人，有人需要我',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我感到孤独',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我觉得生活有意义',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对自己的评价很低',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我做事情时难以集中注意力',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我感到精力充沛',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对自己的过去感到后悔',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对身边发生的事情感兴趣',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我感到悲伤',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我觉得自己是一个失败者',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我觉得生活不值得继续',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我对自己的未来感到迷茫',
              options: ['没有', '轻度', '中度', '重度']
            },
            {
              content: '我能够从日常活动中获得乐趣',
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
            },
            {
              content: '我感到头晕或眩晕',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我担心会发生可怕的事情',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到容易疲劳',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我难以放松下来',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到肌肉紧张',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我因担忧而难以入睡',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我对很多事情都感到担忧',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到坐立不安',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我害怕失控',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            },
            {
              content: '我感到莫名的恐惧',
              options: ['从不', '几天', '一半以上的日子', '几乎每天']
            }
          ]
        },
        4: {
          id: 4,
          title: '人格特质分析',
          duration: 10,
          difficulty: 2,
          questions: [
            {
              content: '我喜欢参加大型社交活动',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我常常关注自己的感受和情绪',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我做事有条理，注重细节',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我乐于帮助他人',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我喜欢尝试新事物和新体验',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我容易感到焦虑或紧张',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我是一个可靠的人',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我喜欢与人合作完成任务',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我对艺术和美的事物有浓厚兴趣',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我倾向于独立完成工作',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我能够很好地控制自己的情绪',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我做事有计划，喜欢按计划进行',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我关心他人的感受',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我喜欢思考抽象的概念',
              options: ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
            },
            {
              content: '我性格外向，善于与人交流',
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
  // 实际项目中应将答案提交到后端
  ElMessage.success('测评提交成功！正在生成报告...')

  // 模拟延迟后跳转到结果页
  setTimeout(() => {
    router.push(`/psychological-assessment/result/${id}`)
  }, 1500)
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
  color: #1890ff;
  font-weight: bold;
  margin-bottom: 1rem;
}

.question-content {
  font-size: 1.2rem;
  margin-bottom: 1.5rem;
  color: #333;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.option-item {
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  background-color: #f5f7fa;
}

.option-item.selected {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
}
</style>
<template>
  <div class="test-questions-page">
    <div class="container">
      <!-- 顶部进度条 -->
      <div class="progress-container">
        <el-progress
            :percentage="currentProgress"
            stroke-width="6"
            :stroke-color="progressColor"
        />
        <p class="progress-text">
          第 {{ currentQuestionIndex + 1 }} / {{ questions.length }} 题
        </p>
      </div>

      <!-- 题目卡片 -->
      <el-card class="question-card">
        <div class="question-header">
          <h2>{{ testInfo.title }}</h2>
          <p class="question-desc">{{ testInfo.description }}</p>
        </div>

        <div class="question-content">
          <h3 class="question-text">{{ currentQuestion.question }}</h3>

          <div class="options-list">
            <el-radio-group
                v-model="selectedOption"
                class="options"
                @change="handleOptionSelect"
            >
              <el-radio
                  v-for="(option, idx) in currentQuestion.options"
                  :key="idx"
                  :label="option.value"
                  class="option-item"
              >
                <span class="option-text">{{ option.text }}</span>
              </el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="question-navigation">
          <el-button
              @click="prevQuestion"
              :disabled="currentQuestionIndex === 0"
          >
            上一题
          </el-button>

          <el-button
              v-if="currentQuestionIndex < questions.length - 1"
              @click="nextQuestion"
              :disabled="!selectedOption"
              type="primary"
          >
            下一题
          </el-button>

          <el-button
              v-else
              @click="submitTest"
              :disabled="!selectedOption"
              type="success"
          >
            提交测评
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElProgress, ElCard, ElRadioGroup, ElRadio, ElButton } from 'element-plus'
import { getPsychologicalTestDetail } from '@/api/psychologicalTest'

// 路由和导航
const route = useRoute()
const router = useRouter()
const testId = route.params.testId

// 响应式数据
const testInfo = ref({})
const questions = ref([])
const currentQuestionIndex = ref(0)
const selectedOption = ref(null)
const answers = ref({})

// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {})
const currentProgress = computed(() => {
  return questions.value.length
      ? Math.round(((currentQuestionIndex.value + 1) / questions.value.length) * 100)
      : 0
})
const progressColor = computed(() => {
  const p = currentProgress.value
  if (p < 30) return '#409EFF'
  if (p < 70) return '#67C23A'
  return '#E6A23C'
})

// 生命周期
onMounted(async () => {
  try {
    const data = await getPsychologicalTestDetail(testId)
    testInfo.value = data.testInfo
    questions.value = data.questions
    ElMessage.success(`开始${testInfo.value.title}`)
  } catch (error) {
    ElMessage.error('加载测评题目失败')
    console.error('加载失败:', error)
    setTimeout(() => router.push('/psychological-test'), 1500)
  }
})

// 选项选择处理
const handleOptionSelect = (value) => {
  answers.value[currentQuestionIndex.value] = value
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
    selectedOption.value = answers.value[currentQuestionIndex.value] || null
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
    selectedOption.value = answers.value[currentQuestionIndex.value] || null
  }
}

// 提交测评
const submitTest = async () => {
  try {
    // 验证是否完成所有题目
    if (Object.keys(answers.value).length !== questions.value.length) {
      ElMessage.warning('请完成所有题目后再提交')
      return
    }

    // 模拟提交（实际项目中替换为真实API调用）
    const result = {
      testId,
      score: calculateScore(),
      reportId: Math.floor(Math.random() * 10000) + 1
    }

    ElMessage.success('测评提交成功，正在生成报告...')
    router.push(`/psychological-test/report/${result.reportId}?testId=${testId}&score=${result.score}`)
  } catch (error) {
    ElMessage.error('提交失败，请重试')
    console.error('提交失败:', error)
  }
}

// 计算得分（根据实际测评规则调整）
const calculateScore = () => {
  let score = 0
  Object.values(answers.value).forEach(value => {
    score += parseInt(value)
  })
  return score
}
</script>

<style scoped>
.test-questions-page {
  padding: 2rem 0;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
}

.progress-container {
  margin-bottom: 2rem;
}

.progress-text {
  text-align: right;
  color: #666;
  margin-top: 0.5rem;
  font-size: 0.9rem;
}

.question-card {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-radius: 10px;
  overflow: hidden;
}

.question-header {
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.question-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.question-desc {
  color: #666;
  margin: 0;
}

.question-content {
  padding: 2rem 1.5rem;
}

.question-text {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.options-list {
  margin-top: 1.5rem;
}

.option-item {
  display: block;
  margin-bottom: 1rem;
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.option-item:hover {
  border-color: #409EFF;
  background-color: #f0f7ff;
}

.option-text {
  font-size: 1rem;
  color: #555;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-top: 1px solid #eee;
  background-color: #fafafa;
}

.question-navigation .el-button {
  min-width: 120px;
}
</style>
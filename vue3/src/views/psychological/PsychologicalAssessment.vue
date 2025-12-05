<template>
  <div class="psychological-assessment-page">
    <div class="assessment-container">
      <h1>心理测评中心</h1>
      <p class="page-description">
        通过专业量表评估，帮助您更好地了解自己的心理状态
      </p>

      <div class="assessment-list">
        <!-- 测评列表项 -->
        <div class="assessment-card" v-for="assessment in assessments" :key="assessment.id">
          <div class="assessment-header">
            <h3>{{ assessment.title }}</h3>
            <span class="assessment-duration">{{ assessment.duration }}分钟</span>
          </div>
          <div class="assessment-description">
            {{ assessment.description }}
          </div>
          <div class="assessment-footer">
            <span class="assessment-difficulty">{{ getDifficultyText(assessment.difficulty) }}</span>
            <el-button type="primary" @click="startAssessment(assessment.id)">
              开始测评
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { redirectToLogin } from '@/utils/auth' // 引入登录跳转工具函数

const router = useRouter()
const userStore = useUserStore()

// 检查登录状态
if (!userStore.token) {
  redirectToLogin()
}

// 模拟测评数据（实际项目中应从API获取）
const assessments = ref([
  {
    id: 1,
    title: '心理健康状态评估',
    description: '全面评估您当前的心理健康状态，包括情绪、压力、睡眠等维度',
    duration: 10,
    difficulty: 1
  },
  {
    id: 2,
    title: '抑郁倾向筛查',
    description: '专业抑郁量表，帮助识别潜在的抑郁倾向',
    duration: 8,
    difficulty: 1
  },
  {
    id: 3,
    title: '焦虑程度测试',
    description: '评估您当前的焦虑水平和主要焦虑来源',
    duration: 7,
    difficulty: 1
  },
  {
    id: 4,
    title: '人格特质分析',
    description: '通过多维度评估，帮助您了解自己的人格特质',
    duration: 15,
    difficulty: 2
  }
])

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const texts = ['简单', '中等', '复杂']
  return texts[difficulty - 1] || '简单'
}

// 开始测评
const startAssessment = (id) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    redirectToLogin()
    return
  }
  router.push(`/psychological-assessment/${id}`)
}
</script>

<style scoped>
.psychological-assessment-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 2rem;
}

.assessment-container {
  max-width: 1000px;
  margin: 0 auto;
}

.psychological-assessment-page h1 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.page-description {
  text-align: center;
  color: #7f8c8d;
  margin-bottom: 2rem;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.assessment-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.assessment-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}

.assessment-card:hover {
  transform: translateY(-5px);
}

.assessment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.assessment-header h3 {
  margin: 0;
  color: #333;
}

.assessment-duration {
  font-size: 0.875rem;
  color: #666;
  background: #f5f5f5;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
}

.assessment-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  min-height: 60px;
}

.assessment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assessment-difficulty {
  font-size: 0.875rem;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  background: #f0f7ff;
  color: #1890ff;
}
</style>
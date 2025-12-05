<template>
  <div class="test-report-page">
    <div class="container">
      <el-card class="report-card">
        <div class="report-header">
          <h1>测评结果报告</h1>
          <div class="report-meta">
            <span>测评名称: {{ testInfo.title }}</span>
            <span>测评日期: {{ new Date().toLocaleDateString() }}</span>
          </div>
        </div>

        <!-- 得分卡片 -->
        <div class="score-card">
          <div class="score-circle">
            <span class="score-value">{{ score }}</span>
            <span class="score-text">得分</span>
          </div>
          <div class="score-interpretation">
            <h3>结果评定</h3>
            <p>{{ interpretation }}</p>
          </div>
        </div>

        <!-- 详细分析 -->
        <div class="report-section">
          <h2>详细分析</h2>
          <div class="analysis-content">
            <p>{{ analysis.content }}</p>

            <div v-if="analysis.factors && analysis.factors.length">
              <h4>维度分析:</h4>
              <el-table :data="analysis.factors" border>
                <el-table-column prop="name" label="维度" align="center" />
                <el-table-column prop="score" label="得分" align="center" />
                <el-table-column prop="level" label="等级" align="center" />
                <el-table-column prop="desc" label="说明" />
              </el-table>
            </div>
          </div>
        </div>

        <!-- 建议 -->
        <div class="report-section">
          <h2>建议与指导</h2>
          <div class="suggestions">
            <ul>
              <li v-for="(suggestion, idx) in suggestions" :key="idx">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="report-actions">
          <el-button @click="goToTestList">返回测评列表</el-button>
          <el-button type="primary" @click="reTest">重新测评</el-button>
          <el-button type="success" @click="downloadReport">下载报告</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElCard, ElTable, ElTableColumn, ElButton } from 'element-plus'
import { getPsychologicalTestInfo, getTestReport } from '@/api/psychologicalTest'

// 路由和导航
const route = useRoute()
const router = useRouter()
const reportId = route.params.reportId
const testId = route.query.testId
const score = route.query.score

// 响应式数据
const testInfo = ref({})
const reportData = ref({})

// 计算属性
const interpretation = computed(() => {
  if (!reportData.value.interpretation) return '正在生成结果解读...'
  return reportData.value.interpretation
})

const analysis = computed(() => {
  return reportData.value.analysis || { content: '正在生成详细分析...', factors: [] }
})

const suggestions = computed(() => {
  return reportData.value.suggestions || ['正在生成建议...']
})

// 生命周期
onMounted(async () => {
  try {
    // 获取测评基本信息
    const testData = await getPsychologicalTestInfo(testId)
    testInfo.value = testData

    // 获取报告详情
    const reportDetail = await getTestReport(reportId, score)
    reportData.value = reportDetail
  } catch (error) {
    ElMessage.error('加载报告失败')
    console.error('报告加载失败:', error)
  }
})

// 导航方法
const goToTestList = () => {
  router.push('/psychological-test')
}

const reTest = () => {
  router.push(`/psychological-test/${testId}`)
}

// 下载报告
const downloadReport = () => {
  // 模拟下载功能
  ElMessage.info('报告生成中，即将下载...')
  setTimeout(() => {
    ElMessage.success('报告已下载')
  }, 1500)
}
</script>

<style scoped>
.test-report-page {
  padding: 2rem 0;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 1rem;
}

.report-card {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-radius: 10px;
  overflow: hidden;
}

.report-header {
  padding: 1.5rem;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.report-header h1 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
}

.report-meta {
  display: flex;
  justify-content: center;
  gap: 2rem;
  color: #666;
  font-size: 0.9rem;
}

.score-card {
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

.score-text {
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

.report-section {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #eee;
}

.report-section h2 {
  color: #2c3e50;
  margin-top: 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #409EFF;
  display: inline-block;
}

.analysis-content {
  padding: 1rem 0;
}

.analysis-content p {
  line-height: 1.7;
  color: #555;
}

.analysis-content h4 {
  margin: 1.5rem 0 1rem 0;
  color: #333;
}

.suggestions {
  padding: 1rem 0;
}

.suggestions ul {
  padding-left: 1.5rem;
}

.suggestions li {
  margin-bottom: 1rem;
  line-height: 1.7;
  color: #555;
}

.report-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  padding: 1.5rem;
  background-color: #fafafa;
  flex-wrap: wrap;
}

.report-actions .el-button {
  min-width: 140px;
}

@media (max-width: 768px) {
  .score-card {
    flex-direction: column;
    padding: 1.5rem;
  }

  .report-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
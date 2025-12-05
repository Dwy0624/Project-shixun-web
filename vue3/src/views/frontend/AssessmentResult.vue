<template>
  <div class="assessment-result">
    <!-- 结果概览卡片 -->
    <el-card shadow="hover" class="result-overview">
      <template #header>
        <div class="overview-header">
          <h3>测评结果概览</h3>
          <span class="result-date">{{ formatDate(resultData.timestamp) }}</span>
        </div>
      </template>

      <div class="overview-stats">
        <div class="stat-item total">
          <div class="stat-label">总测试项</div>
          <div class="stat-value">{{ resultData.totalCount }}</div>
        </div>
        <div class="stat-item success">
          <div class="stat-label">通过项</div>
          <div class="stat-value">{{ resultData.successCount }}</div>
          <div class="stat-percentage">{{ successRate }}%</div>
        </div>
        <div class="stat-item error">
          <div class="stat-label">风险项</div>
          <div class="stat-value">{{ resultData.errorCount }}</div>
          <div class="stat-percentage">{{ errorRate }}%</div>
        </div>
        <div class="stat-item warning" v-if="resultData.warningCount > 0">
          <div class="stat-label">关注项</div>
          <div class="stat-value">{{ resultData.warningCount }}</div>
          <div class="stat-percentage">{{ warningRate }}%</div>
        </div>
      </div>

      <div class="result-status">
        <el-progress
            :percentage="successRate"
            :stroke-color="getProgressColor()"
            stroke-width="8"
        ></el-progress>
        <div class="status-text" v-html="getStatusText()"></div>
      </div>
    </el-card>

    <!-- 筛选和操作 -->
    <div class="result-actions">
      <el-select
          v-model="filterStatus"
          placeholder="筛选结果"
          size="small"
          @change="handleFilterChange"
      >
        <el-option label="全部" value="all"></el-option>
        <el-option label="通过项" value="success"></el-option>
        <el-option label="关注项" value="warning"></el-option>
        <el-option label="风险项" value="error"></el-option>
      </el-select>

      <div class="action-buttons">
        <el-button
            type="primary"
            size="small"
            @click="handleRetry"
        >
          <i class="fas fa-redo"></i> 重新测评
        </el-button>
        <el-button
            type="warning"
            size="small"
            @click="handleExport"
        >
          <i class="fas fa-download"></i> 导出报告
        </el-button>
        <el-button
            type="danger"
            size="small"
            @click="handleClear"
        >
          <i class="fas fa-trash"></i> 清空结果
        </el-button>
      </div>
    </div>

    <!-- 详细结果列表 -->
    <el-card shadow="hover" class="result-details">
      <template #header>
        <h3>详细结果</h3>
      </template>

      <el-table
          :data="filteredItems"
          border
          stripe
      >
        <el-table-column
            prop="title"
            label="题目"
            width="400"
        ></el-table-column>
        <el-table-column
            prop="status"
            label="状态"
            width="120"
        >
          <template #default="scope">
            <el-tag
                :type="scope.row?.status === 'success' ? 'success' : scope.row?.status === 'warning' ? 'warning' : 'danger'"
            >
              {{ scope.row?.status === 'success' ? '通过' : scope.row?.status === 'warning' ? '关注' : '风险' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
            prop="message"
            label="评估"
        ></el-table-column>
        <el-table-column
            prop="timestamp"
            label="时间"
            width="180"
        ></el-table-column>
        <el-table-column
            label="操作"
            width="100"
        >
          <template #default="scope">
            <el-button
                type="text"
                @click="scope.row.showDetails = !scope.row.showDetails"
                v-if="scope.row"
            >
              {{ scope.row.showDetails ? '收起' : '详情' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div
          v-for="(item, index) in filteredItems"
          :key="`detail-${index}`"
      v-if="filteredItems.length && item?.showDetails"
      class="detail-expand"
      >
      <el-card>
        <div class="detail-content">
          <p><strong>选择的选项：</strong>{{ item.data?.selectedOption }}</p>  <!-- 使用可选链操作符 -->
          <p><strong>风险等级：</strong>
            <span :class="`risk-level ${item.data?.riskLevel}`">
                {{ item.data?.riskLevel === 'high' ? '高风险' : item.data?.riskLevel === 'medium' ? '中等风险' : '低风险' }}
              </span>
          </p>
          <p><strong>建议：</strong>{{ item.message }}</p>
        </div>
      </el-card>
      </div>
    </el-card>
  </div>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const { id } = route.params
const { answers: answersJson } = route.query

// 解析从题目页面传递过来的答案
const answers = ref({})
if (answersJson) {
  try {
    answers.value = JSON.parse(answersJson)
  } catch (e) {
    console.error('解析答案失败', e)
  }
}

// 测评数据和结果 - 确保初始化showDetails属性
const assessment = ref(null)
const resultData = ref({
  timestamp: new Date().toISOString(),
  totalCount: 0,
  successCount: 0,
  errorCount: 0,
  warningCount: 0,
  items: [] // 每个item都会在添加时初始化showDetails
})

const filterStatus = ref('all')

onMounted(() => {
  // 加载测评信息和计算结果
  loadAssessmentInfo()
  calculateResult()
})

// 加载测评基本信息
const loadAssessmentInfo = () => {
  // 完整的测评数据
  const assessmentData = {
    1: {
      id: 1,
      title: '心理健康状态评估',
      questionsCount: 15, // 增加问题数量标识
      questionsType: '心理健康',
      questionsLevel: '基础',
      questions: [
        { content: '我感到心情愉快', options: ['没有', '轻度', '中度', '重度'] },
        { content: '我对未来充满希望', options: ['没有', '轻度', '中度', '重度'] },
        { content: '我能够专注于工作或学习', options: ['没有', '轻度', '中度', '重度'] },
        { content: '我觉得自己是一个失败者', options: ['没有', '轻度', '中度', '重度'] },
        { content: '我觉得生活不值得继续', options: ['没有', '轻度', '中度', '重度'] },
        { content: '我能够享受日常活动', options: ['没有', '轻度', '中度', '重度'] },
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
      questionCount: 12,
      questionType: '情绪障碍',
      questionLevel: '临床',
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
      questionCount: 14,
      questionType: '情绪障碍',
      questionLevel: '临床',
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
      questionCount: 15,
      questionType: '人格评估',
      questionLevel: '基础',
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
      questionCount: 14,
      questionType: '生理健康',
      questionLevel: '临床',
      questions: [
        { content: '入睡时间（分钟）', options: ['≤15分钟', '16-30分钟', '31-60分钟', '>60分钟'] },
        { content: '睡眠持续时间（小时）', options: ['≥7小时', '6-7小时', '5-6小时', '<5小时'] },
        { content: '睡眠质量', options: ['很好', '较好', '较差', '很差'] },
        { content: '入睡困难', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '睡眠中断', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '早醒', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '需要药物辅助睡眠', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '日间功能障碍', options: ['无', '轻微', '中度', '严重'] },
        { content: '白天感到困倦', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '睡眠中打鼾', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '睡眠中呼吸暂停', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '睡眠中感到窒息', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '睡眠中咳嗽或鼾声', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] },
        { content: '睡眠中感到冷或热', options: ['无', '<1次/周', '1-2次/周', '≥3次/周'] }
      ]
    },
    6: {
      id: 6,
      title: '生活事件量表--压力测评',
      questionCount: 15,
      questionType: '社会心理',
      questionLevel: '基础',
      questions: [
        { content: '家庭成员去世', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '离婚或分居', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '结婚', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '怀孕或生育', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '家庭成员健康状况恶化', options: ['无', '轻微', '中度', '严重'] },
        { content: '工作变动或失业', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '经济状况显著变化', options: ['无', '轻微', '中度', '严重'] },
        { content: '住房变动', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '子女离家', options: ['无', '1次', '2次及以上', '近3个月内发生'] },
        { content: '与配偶争吵频率', options: ['无', '偶尔', '经常', '几乎每天'] },
        { content: '法律纠纷', options: ['无', '1次', '2次及以上', '进行中'] },
        { content: '退休', options: ['无', '1年内', '1-5年', '5年以上'] },
        { content: '子女问题', options: ['无', '轻微', '中度', '严重'] },
        { content: '个人健康问题', options: ['无', '轻微', '中度', '严重'] },
        { content: '重要节日或纪念日', options: ['无特殊意义', '一般', '较重要', '非常重要'] }
      ]
    }
  }

  assessment.value = assessmentData[id] || { title: '心理测评' }

  // 设置总测试项
  resultData.value.totalCount = assessment.value.questions ? assessment.value.questions.length : 0
}

// 计算测评结果
const calculateResult = () => {
  if (!assessment.value || !assessment.value.questions) return

  // 计算各类结果数量
  let highRiskCount = 0
  let mediumRiskCount = 0
  let lowRiskCount = 0

  // 分析每个问题的答案
  Object.entries(answers.value).forEach(([index, selectedOption]) => {
    const questionIndex = parseInt(index)
    const question = assessment.value.questions[questionIndex]
    let riskLevel = 'low' // 低风险

    // 根据不同测评类型和选项判断风险等级
    if (assessment.value.id === 1 || assessment.value.id === 3) {
      // 心理健康状态评估和焦虑程度测试
      if (selectedOption >= 2) {
        riskLevel = 'high' // 高风险
        highRiskCount++
      } else if (selectedOption === 1) {
        riskLevel = 'medium' // 中等风险
        mediumRiskCount++
      } else {
        lowRiskCount++
      }
    } else if (assessment.value.id === 2) {
      // 抑郁倾向筛查
      if (selectedOption >= 2) {
        riskLevel = 'high'
        highRiskCount++
      } else if (selectedOption === 1) {
        riskLevel = 'medium'
        mediumRiskCount++
      } else {
        lowRiskCount++
      }
    } else if (assessment.value.id === 4) {
      // 人格特质分析，不区分风险，仅做记录
      riskLevel = 'neutral'
      lowRiskCount++
    }

    // 添加到详细结果 - 确保每个item都有showDetails属性
    resultData.value.items.push({
      title: `第${questionIndex + 1}题: ${question.content}`,
      status: riskLevel === 'high' ? 'error' : riskLevel === 'medium' ? 'warning' : 'success',
      message: getResultMessage(riskLevel),
      timestamp: new Date().toLocaleString(),
      showDetails: false, // 明确初始化showDetails属性
      data: {
        selectedOption: question.options[selectedOption],
        riskLevel: riskLevel
      }
    })
  })

  // 更新结果统计
  resultData.value.successCount = lowRiskCount
  resultData.value.warningCount = mediumRiskCount
  resultData.value.errorCount = highRiskCount
}

// 获取结果描述信息
const getResultMessage = (riskLevel) => {
  const messages = {
    high: '需要关注，建议进一步评估',
    medium: '有轻微倾向，建议适当调整',
    low: '状态良好',
    neutral: '正常范围'
  }
  return messages[riskLevel] || '评估正常'
}

// 计算属性
const successRate = computed(() => {
  return resultData.value.totalCount > 0
      ? Math.round((resultData.value.successCount / resultData.value.totalCount) * 100)
      : 0
})

const errorRate = computed(() => {
  return resultData.value.totalCount > 0
      ? Math.round((resultData.value.errorCount / resultData.value.totalCount) * 100)
      : 0
})

const warningRate = computed(() => {
  return resultData.value.totalCount > 0
      ? Math.round((resultData.value.warningCount / resultData.value.totalCount) * 100)
      : 0
})

const filteredItems = computed(() => {
  if (filterStatus.value === 'all') {
    return resultData.value.items || [] // 确保返回数组避免undefined
  }
  return (resultData.value.items || []).filter(item => item?.status === filterStatus.value)
})

// 方法
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

const getStatusText = () => {
  if (successRate.value === 100) return '<i class="fas fa-check-circle"></i> 全部测试通过'
  if (errorRate.value > 50) return '<i class="fas fa-times-circle"></i> 测试失败，存在较多问题'
  return '<i class="fas fa-exclamation-triangle"></i> 部分测试通过'
}

const getProgressColor = () => {
  if (successRate.value === 100) return '#4caf50'
  if (successRate.value > 60) return '#ffc107'
  return '#f44336'
}

const handleFilterChange = (value) => {
  filterStatus.value = value
}

const handleRetry = () => {
  router.push(`/psychological-assessment/${id}`)
}

const handleClear = () => {
  ElMessage.success('结果已清空')
  router.push('/psychological-assessment')
}

const handleExport = () => {
  // 简单实现导出功能
  const exportData = {
    title: `${assessment.value.title} 结果报告`,
    date: formatDate(resultData.value.timestamp),
    summary: {
      total: resultData.value.totalCount,
      success: resultData.value.successCount,
      error: resultData.value.errorCount,
      warning: resultData.value.warningCount,
      successRate: `${successRate.value}%`
    },
    details: (resultData.value.items || []).map(item => ({
      title: item.title,
      status: item.status === 'success' ? '通过' : item.status === 'warning' ? '关注' : '风险',
      selectedOption: item.data?.selectedOption,
      assessment: item.message
    }))
  }

  // 实际项目中可以使用专门的导出库如xlsx或pdf-lib
  const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(exportData, null, 2));
  const downloadAnchorNode = document.createElement('a');
  downloadAnchorNode.setAttribute("href", dataStr);
  downloadAnchorNode.setAttribute("download", `${assessment.value.title}_结果报告.json`);
  document.body.appendChild(downloadAnchorNode);
  downloadAnchorNode.click();
  downloadAnchorNode.remove();

  ElMessage.success('报告导出成功')
}
</script>

<style scoped>
.assessment-result {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.result-overview {
  margin-bottom: 20px;
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-date {
  color: #666;
  font-size: 14px;
}

.overview-stats {
  display: flex;
  gap: 15px;
  margin: 20px 0;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 120px;
  padding: 15px;
  border-radius: 6px;
  background-color: #f5f7fa;
  text-align: center;
}

.stat-item.total {
  background-color: #f0f2f5;
}

.stat-item.success {
  background-color: #f0f9eb;
}

.stat-item.warning {
  background-color: #fffbeb;
}

.stat-item.error {
  background-color: #fef2f2;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-percentage {
  font-size: 12px;
  color: #999;
}

.result-status {
  margin-top: 20px;
}

.status-text {
  text-align: center;
  margin-top: 10px;
  font-size: 16px;
  font-weight: bold;
}

.result-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.result-details {
  margin-top: 20px;
}

.detail-expand {
  margin-top: 10px;
}

.detail-content {
  padding: 10px;
}

.risk-level {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.risk-level.high {
  background-color: #fef2f2;
  color: #f53f3f;
}

.risk-level.medium {
  background-color: #fffbeb;
  color: #ff7d00;
}

.risk-level.low {
  background-color: #f0f9eb;
  color: #00b42a;
}
</style>
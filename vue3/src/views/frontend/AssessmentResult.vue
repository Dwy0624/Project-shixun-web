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

        <!-- 新增选择情况卡片 -->
        <div class="choices-card">
          <h2>选择情况统计</h2>
          <div class="choices-stats">
            <div class="stat-item" v-for="(stat, key) in choiceStats" :key="key">
              <div class="stat-label">{{ getOptionLabel(key) }}</div>
              <div class="stat-bar-container">
                <div class="stat-bar" :style="{ width: stat.percentage + '%' }"></div>
              </div>
              <div class="stat-value">{{ stat.count }} ({{ stat.percentage }}%)</div>
            </div>
          </div>
        </div>

        <!-- 原有得分部分 -->
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

        <!-- 原有详情部分 -->
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
          <el-button type="success" @click="printReport">打印报告</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {ElCard, ElCollapse, ElCollapseItem, ElButton, ElIcon, ElMessage} from 'element-plus'
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

// 新增选择情况统计数据
const choiceStats = ref({})

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

// 加载测评数据
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

      // 获取对应ID的测评数据
      assessment.value = assessmentData[id]

      if (!assessment.value) {
        throw new Error('未找到对应的测评数据')
      }

      // 计算每题得分
      calculateQuestionScores()

      // 处理选择情况统计
      choiceStats.value = processChoiceStats(answers.value)

      loading.value = false
    } catch (err) {
      error.value = err.message || '加载测评数据失败'
      loading.value = false
    }
  }, 800) // 模拟网络延迟
}

// 计算每题得分
const calculateQuestionScores = () => {
  const scores = []

  Object.entries(answers.value).forEach(([index, selectedOption]) => {
    const question = assessment.value.questions[parseInt(index)]
    let questionScore = 0

    // 根据测评类型计算得分
    if ([1, 2, 3, 5, 6].includes(assessment.value.id)) {
      questionScore = parseInt(selectedOption)
    } else if (assessment.value.id === 4) {
      questionScore = parseInt(selectedOption) + 1
    }

    scores.push({
      questionIndex: parseInt(index) + 1,
      questionContent: question.content,
      score: questionScore
    })
  })

  questionScores.value = scores
}

// 处理选择情况统计
const processChoiceStats = (answers) => {
  const stats = {}
  const total = Object.keys(answers).length

  if (total === 0) return stats

  Object.values(answers).forEach(choice => {
    stats[choice] = (stats[choice] || 0) + 1
  })

  // 计算百分比
  Object.keys(stats).forEach(key => {
    stats[key] = {
      count: stats[key],
      percentage: Math.round((stats[key] / total) * 100)
    }
  })

  return stats
}

// 打印报告功能
const printReport = () => {
  // 显示打印提示
  ElMessage.info('正在准备打印文档...')

  // 延迟执行打印，确保用户看到提示
  setTimeout(() => {
    // 调用浏览器打印API
    window.print()
  }, 500)
}

// 获取选项标签文本
const getOptionLabel = (key) => {
  // 根据不同测评类型返回对应的选项文本
  if ([1, 2, 3, 5, 6].includes(assessment.value.id)) {
    const labels = ['从不/没有', '几天/轻度', '一半以上/中度', '几乎每天/重度']
    return labels[parseInt(key)] || `选项${key}`
  } else if (assessment.value.id === 4) {
    const labels = ['非常不符合', '不太符合', '一般', '比较符合', '非常符合']
    return labels[parseInt(key)] || `选项${key}`
  }
  return `选项${key}`
}

// 结果解读
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

// 各类解读函数（增强版，更具体的专业建议）

const getMentalHealthInterpretation = (score) => {
  if (score <= 3) return '您的心理健康状态良好，情绪调节能力较强。建议保持规律作息与适度运动，每周至少进行3次30分钟以上的有氧运动，同时维持良好的社交关系，可有效巩固当前状态。'
  if (score <= 7) return '您存在轻微的心理困扰，可能表现为偶尔的情绪波动或精力不足。建议每天预留15-20分钟进行正念呼吸练习，减少睡前电子设备使用时间，尝试记录情绪日记梳理压力源，多数情况下可自行缓解。'
  if (score <= 12) return '您的心理健康状态需要关注，可能出现持续情绪低落、注意力不集中等情况。建议制定合理的目标分解计划，避免过度自我要求，每周与亲友进行2-3次深度交流，必要时可联系学校心理咨询中心进行短期疏导。'
  return '您可能存在较明显的心理问题，症状可能已影响日常生活功能。建议尽快前往专业心理门诊进行标准化评估，同时可采取"小步骤行动法"：每天完成1件简单的愉悦活动（如散步、听音乐），避免独处时间过长，及时获取专业支持是改善状态的关键。'
}

const getDepressionInterpretation = (score) => {
  if (score <= 2) return '您目前没有明显的抑郁症状，心理弹性良好。建议保持现有社交频率，每周安排2-3次兴趣活动，同时注意规律饮食（每日摄入适量蛋白质与Omega-3脂肪酸），有助于维持情绪稳定。'
  if (score <= 5) return '您有轻微的抑郁倾向，可能表现为偶尔的兴趣减退或疲劳感。建议采用"行为激活法"：每天设定3件微小可行的任务（如整理房间、购买物品），完成后记录成就感；睡前1小时避免思考复杂问题，可尝试温水泡脚改善睡眠质量。'
  if (score <= 8) return '您存在中度抑郁症状，可能出现持续情绪低落、自我否定等表现。建议主动向信任的人倾诉感受，减少独处时间；每天保证30分钟光照时间（自然光最佳），适当进行低强度运动（如瑜伽、慢走）；考虑预约学校或社区心理咨询师进行专业干预。'
  return '您的抑郁症状较为明显，可能伴随食欲/睡眠改变、精力下降等问题。建议尽快前往精神心理科进行专业评估，目前的状态并非个人意志力问题，寻求帮助是勇敢的表现。在专业帮助前，可尝试"5-4-3-2-1感官着陆法"缓解急性情绪低谷：识别5个可见物体、4种可触摸物品、3种声音、2种气味和1种味道。'
}

const getAnxietyInterpretation = (score) => {
  if (score <= 2) return '您目前没有明显的焦虑症状，情绪调节能力良好。建议保持规律的作息节奏，每周进行2-3次中等强度运动（如游泳、骑行），有助于维持自主神经系统平衡。'
  if (score <= 5) return '您存在轻微的焦虑情绪，可能表现为偶尔的紧张或担忧。建议学习"478呼吸法"：吸气4秒-屏息7秒-呼气8秒，每天练习3次可有效降低焦虑水平；同时可尝试"担忧时间"技术：每天固定15分钟专门处理焦虑想法，其他时间出现担忧时记录下来延后处理。'
  if (score <= 8) return '您有中度焦虑症状，可能出现持续紧张、注意力难以集中等情况。建议减少咖啡因摄入（包括咖啡、能量饮料），睡前进行渐进式肌肉放松训练（从脚趾到头部依次绷紧再放松）；可记录"焦虑触发情境-自动想法-合理回应"三栏表，帮助识别并调整非理性信念。'
  return '您的焦虑症状较为明显，可能伴随躯体不适（如心悸、胸闷）或回避行为。建议寻求专业心理咨询师帮助，认知行为疗法对焦虑问题有明确效果；同时可采用"接地技术"应对急性焦虑：双手紧握冰块或冷水洗手，通过感官刺激快速平静神经系统；避免自行使用酒精或药物缓解症状。'
}

const getPersonalityInterpretation = (score) => {
  if (score <= 8) return '您偏向内向稳定型人格，这类特质在需要专注、细致和深度思考的领域具有优势。建议充分发挥观察力敏锐的特点，可选择深度阅读、独立研究等活动获得成就感；在社交中不必强迫自己成为焦点，高质量的一对一交流更适合您建立连接；职业发展上可考虑科研、设计等需要专注力的方向。'
  if (score <= 12) return '您的人格特质属于平衡灵活型，能根据环境调整自身状态。建议利用这种适应性优势，在团队中扮演协调者角色；注意避免过度迁就他人需求而忽视自身感受，可定期进行"自我觉察记录"明确个人边界；职业选择上兼容性较强，可尝试结合兴趣探索多元化发展路径。'
  if (score <= 16) return '您属于外向探索型人格，具有较强的社交能量和行动力。建议通过团队合作、公开演讲等活动发挥优势，同时注意倾听他人观点以避免决策冲动；在职业中可选择需要沟通协调、开拓创新的领域；适当安排独处时间进行深度思考，平衡外向特质带来的精力消耗。'
  return '您具有鲜明的创造性人格特质，思维独特且富有想象力。这种特质在艺术创作、战略规划等领域极具价值；建议为自己创造弹性工作环境，避免过多规则束缚；注意在团队合作中适度妥协，可通过书面形式清晰表达复杂想法；职业发展上适合选择允许创新突破的领域，如设计、创业等。'
}

const getSleepQualityInterpretation = (score) => {
  if (score <= 3) return '您的睡眠质量良好，这是身心健康的重要基础。建议保持固定作息（包括周末），维持睡前1小时"缓冲期"（避免工作或剧烈运动）；睡眠环境保持黑暗（使用遮光窗帘）、安静（必要时用白噪音）、凉爽（18-22℃最佳）；可继续保持当前的睡眠习惯，每天睡眠时间建议维持在7-9小时。'
  if (score <= 7) return '您存在轻微的睡眠问题，可能表现为入睡稍慢或偶尔醒夜。建议建立"睡前仪式"：如温水泡脚（40℃左右，15分钟）、阅读纸质书或听舒缓音乐；睡前1小时避免使用电子屏幕（蓝光会抑制褪黑素分泌）；晚餐避免过饱或摄入咖啡因、酒精，可在睡前喝一小杯温牛奶（含色氨酸助眠）。'
  if (score <= 12) return '您的睡眠质量较差，可能存在入睡困难或早醒问题。建议采用"刺激控制法"：只有困了才上床，床上仅用于睡眠（不玩手机、工作）；若20分钟未睡着则起床到昏暗环境中做单调活动，有困意再返回床上；每天固定起床时间（包括周末），逐步调整生物钟；可尝试记录"睡眠日志"追踪影响因素。'
  return '您的睡眠问题较为严重，可能已形成睡眠障碍（如失眠症）。建议尽快咨询睡眠医学专科或精神科医生，进行专业评估（可能包括多导睡眠图检查）；在医生指导下可短期使用助眠药物调整，同时结合认知行为疗法（CBT-I）进行长期改善；避免自行使用安眠药或酒精助眠，可能导致依赖或加重问题。'
}

const getLifeEventInterpretation = (score) => {
  if (score <= 4) return '您近期经历的生活压力事件较少，整体生活状态平稳。建议利用这段时期进行"心理储备"：培养1-2项放松技能（如冥想、绘画），建立支持性社交网络；可制定短期目标保持生活节奏感，同时预留"空白时间"避免过度填充日程，维持这种平衡状态对长期心理健康有益。'
  if (score <= 8) return '您近期面临一些生活压力，可能来自学业、人际关系等方面。建议采用"任务分解法"：将压力事件拆解为具体步骤，按优先级逐步完成；每天安排15分钟"压力释放时间"，通过运动、书写等方式宣泄情绪；学习"边界设定"技巧，适当拒绝额外责任，避免压力累积。'
  if (score <= 12) return '您正经历较多生活压力，多项压力源可能产生叠加效应。建议进行"压力源分类"：区分可控因素（如时间管理）和不可控因素（如他人态度），专注于可改变的部分；每周与亲友进行1-2次深度交流，获得情感支持；学习"正念减压"技术，每天10分钟专注于呼吸，帮助从压力思维中抽离。'
  return '您目前承受较大生活压力，可能已出现身心反应（如疲劳、情绪波动）。建议进行"压力暂停"：暂时减少非必要责任，优先保障睡眠和饮食基本需求；寻求专业帮助（心理咨询师可提供压力管理策略）；采用"小胜利原则"：每天完成1件微小的积极行动（如散步10分钟），积累掌控感；记住高压状态是暂时的，主动调整和求助是勇敢的表现。'
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

/* 新增选择情况卡片样式 */
.choices-card {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #eee;
}

.choices-card h2 {
  color: #2c3e50;
  margin-top: 0;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #409EFF;
  display: inline-block;
  margin-bottom: 1rem;
}

.choices-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-label {
  width: 120px;
  flex-shrink: 0;
  color: #666;
}

.stat-bar-container {
  flex-grow: 1;
  height: 10px;
  background-color: #f0f2f5;
  border-radius: 5px;
  overflow: hidden;
}

.stat-bar {
  height: 100%;
  background-color: #409EFF;
  transition: width 0.3s ease;
}

.stat-value {
  width: 100px;
  flex-shrink: 0;
  text-align: right;
  color: #333;
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

/* 添加打印样式，优化打印效果 */
@media print {
  /* 隐藏不需要打印的元素 */
  .actions-section,
  .el-collapse-item__header {
    display: none !important;
  }

  /* 调整打印页面样式 */
  body, .assessment-result-page {
    background-color: #fff;
    padding: 0;
    min-height: auto;
  }

  .container {
    max-width: 100%;
    padding: 20px;
  }

  .result-card {
    box-shadow: none;
    border: none;
  }

  /* 强制展开所有折叠内容 */
  .el-collapse-item__content {
    display: block !important;
    height: auto !important;
    overflow: visible !important;
  }

  /* 调整打印字体和间距 */
  h1, h2, h3 {
    color: #000 !important;
    page-break-after: avoid;
  }

  p {
    color: #333 !important;
    font-size: 14px !important;
  }

  .choices-card, .score-section, .details-section {
    page-break-inside: avoid;
    margin-bottom: 20px;
  }
}
</style>
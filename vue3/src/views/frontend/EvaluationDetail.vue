<template>
  <div class="evaluation-detail">
    <el-page-header @back="handleBack" content="测评作答"></el-page-header>

    <div v-if="loading" class="loading-container">
      <el-loading loading="true" background="rgba(255,255,255,0.8)"></el-loading>
    </div>

    <div v-else-if="error" class="error-container">
      <el-alert
          title="加载失败"
          :description="error"
          type="error"
          show-icon
      ></el-alert>
      <el-button @click="loadQuestions" style="margin-top: 15px;">重新加载</el-button>
    </div>

    <div v-else class="evaluation-content">
      <div class="evaluation-title">
        <h2>{{ scaleName }}</h2>
        <p class="progress-text">
          第 {{ currentQuestionIndex + 1 }} / {{ questions.length }} 题
        </p>
        <el-progress
            :percentage="progress"
            stroke-width="6"
            style="margin: 15px 0;"
        ></el-progress>
      </div>

      <div class="question-card">
        <div class="question-content">
          <span class="question-number">{{ currentQuestionIndex + 1 }}.</span>
          <span>{{ currentQuestion.content }}</span>
        </div>

        <div class="question-options">
          <el-radio-group
              v-if="currentQuestion.questionType === '单选'"
              v-model="selectedOptions"
              @change="handleOptionChange"
          >
            <el-radio
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                :label="[index]"
                border
                class="option-item"
            >
              {{ option }}
            </el-radio>
          </el-radio-group>

          <el-checkbox-group
              v-if="currentQuestion.questionType === '多选'"
              v-model="selectedOptions"
              @change="handleOptionChange"
          >
            <el-checkbox
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                :label="index"
                border
                class="option-item"
            >
              {{ option }}
            </el-checkbox>
          </el-checkbox-group>
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
            :disabled="!hasSelectedOptions"
        >
          下一题
        </el-button>

        <el-button
            v-else
            type="primary"
            @click="submitEvaluation"
        :disabled="!hasSelectedOptions"
        >
        提交测评
        </el-button>
      </div>
    </div>

    <!-- 结果弹窗 -->
    <el-dialog
        v-model="resultVisible"
        title="测评结果"
        width="800px"
        :close-on-click-modal="false"
    >
      <div class="result-content">
        <div class="result-header">
          <div class="result-title">
            {{ resultData.testname }} 结果
          </div>
          <div class="result-score">
            测评得分: {{ resultData.score }}
          </div>
          <div class="risk-level" :class="getRiskClass()">
            风险等级: {{ resultData.riskDescription }}
          </div>
        </div>

        <div class="result-analysis">
          <h4>结果分析:</h4>
          <p>{{ resultData.result }}</p>
        </div>

        <div class="result-suggestions">
          <h4>建议:</h4>
          <ul>
            <li v-for="(suggestion, index) in resultData.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleBack">返回列表</el-button>
        <el-button type="primary" @click="saveResultAndBack">保存结果并返回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getEvaluationQuestions, submitEvaluation as apiSubmitEvaluation } from '@/api/psychologicalEvaluation';
import { ElMessage, ElLoading } from 'element-plus';


const router = useRouter();
const route = useRoute();

// 状态变量
const questions = ref([]);
const currentQuestionIndex = ref(0);
const selectedOptions = ref([]);
const answers = ref([]);
const loading = ref(true);
const error = ref('');
const resultVisible = ref(false);
const resultData = ref({});
const scaleId = ref('');
const scaleName = ref('');

// 计算属性
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || {};
});

const progress = computed(() => {
  return questions.value.length ? Math.round((currentQuestionIndex.value / questions.value.length) * 100) : 0;
});

const hasSelectedOptions = computed(() => {
  return selectedOptions.value && selectedOptions.value.length > 0;
});

// 生命周期
onMounted(() => {
  scaleId.value = route.query.scaleId;
  if (!scaleId.value) {
    ElMessage.error('缺少测评ID');
    router.push('/psychological/evaluation-list');
    return;
  }
  loadQuestions();
});

// 方法
const loadQuestions = async () => {
  try {
    loading.value = true;
    error.value = '';
    const res = await getEvaluationQuestions(scaleId.value);
    if (res.success && res.data && res.data.length > 0) {
      questions.value = res.data;
      scaleName.value = route.query.scaleName || `心理测评 #${scaleId.value}`;

      // 初始化答案数组
      answers.value = questions.value.map(question => ({
        questionId: question.id,
        selectedOptions: []
      }));
    } else {
      error.value = res.message || '未找到测评题目';
    }
  } catch (err) {
    console.error('加载题目失败', err);
    error.value = '加载题目失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

const handleOptionChange = (value) => {
  // 保存当前题目的答案
  answers.value[currentQuestionIndex.value].selectedOptions = value;
};

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
    // 恢复之前的选择
    selectedOptions.value = [...answers.value[currentQuestionIndex.value].selectedOptions];
  }
};

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
    // 恢复之前的选择或清空
    selectedOptions.value = [...answers.value[currentQuestionIndex.value].selectedOptions];
  }
};

// 修复：统一函数命名为驼峰式，避免与导入的apiSubmitEvaluation冲突
const submitEvaluation = async () => {  // 修复：函数名改为submitEvaluation，与模板中的调用保持一致
  // 保存最后一题的答案
  answers.value[currentQuestionIndex.value].selectedOptions = selectedOptions.value;

  const submitData = {
    evaluationId: scaleId.value,
    answers: answers.value
  };

  // 声明loading变量，避免重复创建实例
  let loadingInstance;
  try {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '正在生成测评结果...',
      background: 'rgba(255, 255, 255, 0.7)'
    });

    // 获取当前用户ID（实际应用中应从登录状态获取）
    const userId = 1; // 示例用户ID

    // 调用API，使用导入的apiSubmitEvaluation
    const res = await apiSubmitEvaluation(userId, submitData);
    if (res.success && res.data) {
      resultData.value = res.data;
      resultVisible.value = true;
    } else {
      ElMessage.error(res.message || '提交测评失败');
    }
  } catch (err) {
    console.error('提交测评失败', err);
    ElMessage.error('提交测评失败，请稍后重试');
  } finally {
    // 关闭之前创建的同一个loading实例
    if (loadingInstance) {
      loadingInstance.close();
    }
  }
};

const handleBack = () => {
  router.push('/psychological/evaluation-list');
};

const saveResultAndBack = () => {
  // 实际应用中可以在这里处理结果保存逻辑
  ElMessage.success('测评结果已保存');
  handleBack();
};

const getRiskClass = () => {
  if (!resultData.value.riskLevel) return '';

  switch (resultData.value.riskLevel) {
    case 0: return 'risk-normal';
    case 1: return 'risk-attention';
    case 2: return 'risk-warning';
    case 3: return 'risk-crisis';
    default: return '';
  }
};
</script>

<style scoped>
/* 样式部分保持不变 */
.evaluation-detail {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.loading-container, .error-container {
  padding: 50px 20px;
  text-align: center;
}

.evaluation-content {
  margin-top: 20px;
}

.evaluation-title {
  text-align: center;
  margin-bottom: 30px;
}

.evaluation-title h2 {
  margin-bottom: 10px;
  color: #333;
}

.progress-text {
  color: #666;
  margin-bottom: 5px;
}

.question-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 25px;
  margin-bottom: 30px;
}

.question-content {
  font-size: 18px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.question-number {
  display: inline-block;
  width: 30px;
  height: 30px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 30px;
  margin-right: 10px;
  vertical-align: middle;
}

.question-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.option-item {
  width: 100%;
  padding: 10px 15px;
}

.question-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.result-content {
  padding: 10px 0;
}

.result-header {
  text-align: center;
  margin-bottom: 25px;
}

.result-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 10px;
}

.result-score {
  font-size: 18px;
  margin-bottom: 15px;
}

.risk-level {
  display: inline-block;
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: 500;
}

.risk-normal {
  background-color: #f0f9eb;
  color: #52c41a;
}

.risk-attention {
  background-color: #fffbeb;
  color: #faad14;
}

.risk-warning {
  background-color: #fff2e8;
  color: #fa8c16;
}

.risk-crisis {
  background-color: #fff1f0;
  color: #f5222d;
}

.result-analysis, .result-suggestions {
  margin-bottom: 20px;
}

.result-analysis h4, .result-suggestions h4 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #1f2937;
}

.result-analysis p {
  line-height: 1.7;
  color: #4b5563;
  text-align: justify;
}

.result-suggestions ul {
  padding-left: 20px;
}

.result-suggestions li {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #4b5563;
}

@media (max-width: 768px) {
  .question-options {
    grid-template-columns: 1fr;
  }
}
</style>
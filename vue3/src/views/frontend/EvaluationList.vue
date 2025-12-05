<template>
  <div class="evaluation-container">
    <div class="page-header">
      <h1>心理测评</h1>
      <p>通过专业测评了解您的心理状态，获取个性化建议</p>
    </div>

    <div class="scale-list">
      <div
          v-for="scale in scales"
          :key="scale.id"
          class="scale-card"
          @click="handleSelectScale(scale)"
      >
        <div class="scale-header">
          <div class="scale-title">{{ scale.testname }}</div>
          <div class="scale-type">{{ scale.testtype }}</div>
        </div>
        <div class="scale-desc">{{ scale.description }}</div>
        <div class="scale-action">
          <el-button type="primary">开始测评</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getEvaluationScales } from '@/api/psychologicalEvaluation';
import { ElMessage } from 'element-plus';

const router = useRouter();
const scales = ref([]);
const loading = ref(true);

onMounted(() => {
  loadScales();
});

const loadScales = async () => {
  try {
    loading.value = true;
    const res = await getEvaluationScales();
    if (res.success) {
      scales.value = res.data;
    } else {
      ElMessage.error(res.message || '加载测评量表失败');
    }
  } catch (error) {
    console.error('加载测评量表失败', error);
    ElMessage.error('加载测评量表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const handleSelectScale = (scale) => {
  router.push({
    path: '/psychological/evaluation',
    query: { scaleId: scale.id }
  });
};
</script>

<style scoped>
.evaluation-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
  color: #333;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

.scale-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.scale-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fff;
}

.scale-card:hover {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
  transform: translateY(-3px);
  border-color: #d1d5db;
}

.scale-header {
  margin-bottom: 16px;
}

.scale-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.scale-type {
  font-size: 14px;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 3px 10px;
  border-radius: 12px;
  display: inline-block;
}

.scale-desc {
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 20px;
  min-height: 60px;
}

.scale-action {
  text-align: right;
}
</style>
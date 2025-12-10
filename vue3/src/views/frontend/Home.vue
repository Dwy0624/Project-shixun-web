<template>
  <div class="home">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-text">
            <h2 class="hero-title">
              一次温暖的对话<br>
              <span class="highlight-text">化孤独为慰藉</span>
            </h2>
            <p class="hero-description">
              每个深夜，每个焦虑的时刻，我们都在这里。不必独自承受，让心与心的连接温暖您的每一天
            </p>
            <div class="hero-actions">
              <router-link to="/consultation" class="primary-btn" v-if="isLoggedIn">
                <i class="fas fa-heart"></i>开始倾诉，获得陪伴
              </router-link>
              <router-link to="/auth/login" class="primary-btn" v-else>
                <i class="fas fa-heart"></i>开始倾诉，获得陪伴
              </router-link>
              <router-link to="/psychological-assessment" class="secondary-btn" v-if="isLoggedIn">
                <i class="fas fa-clipboard-check"></i>心理测评，了解自我
              </router-link>
              <router-link to="/auth/login" class="secondary-btn" v-else>
                <i class="fas fa-clipboard-check"></i>心理测评，了解自我
              </router-link>
              <router-link to="/emotion-diary" class="secondary-btn" v-if="isLoggedIn">
                <i class="fas fa-pen-fancy"></i>记录心情，释放情感
              </router-link>
              <router-link to="/auth/login" class="secondary-btn" v-else>
                <i class="fas fa-pen-fancy"></i>记录心情，释放情感
              </router-link>
            </div>
          </div>

          <div class="hero-image">
            <div class="mindfulness-icon">
              <i class="fas fa-heart"></i>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心功能区域 -->
    <section class="features-section">
      <div class="features-container">
        <div class="section-header">
          <h3 class="section-title">我们的服务</h3>
          <p class="section-description">用心倾听每一个故事，用爱温暖每一颗心灵</p>
        </div>

        <div class="features-grid">
          <!-- AI咨询 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon ai-icon">
                <i class="fas fa-comments"></i>
              </div>
              <h4 class="feature-title">心灵倾诉</h4>
              <p class="feature-description">无论何时何地，都有一颗温暖的心在倾听。分享您的烦恼，收获理解与支持</p>
              <router-link :to="isLoggedIn ? '/consultation' : '/auth/login'" class="feature-link">
                开始倾诉 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 情绪管理 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon emotion-icon">
                <i class="fas fa-heart"></i>
              </div>
              <h4 class="feature-title">情感记录</h4>
              <p class="feature-description">每一种情绪都值得被记录和珍视。让我们陪您一起观察内心，拥抱真实的自己</p>
              <router-link :to="isLoggedIn ? '/emotion-diary' : '/auth/login'" class="feature-link">
                记录心情 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 知识学习 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon knowledge-icon">
                <i class="fas fa-seedling"></i>
              </div>
              <h4 class="feature-title">心灵成长</h4>
              <p class="feature-description">在知识的花园中漫步，发现内心的力量。每一篇文章都是通往更好自己的路径</p>
              <router-link to="/knowledge" class="feature-link">
                开始探索 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 心理测评 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon assessment-icon">
                <i class="fas fa-clipboard-check"></i>
              </div>
              <h4 class="feature-title">心理测评</h4>
              <p class="feature-description">专业量表评估，帮助您更好地了解自己的心理状态和需求</p>
              <router-link :to="isLoggedIn ? '/psychological-assessment' : '/auth/login'" class="feature-link">
                开始测评 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 我的收藏 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon favorite-icon">
                <i class="fas fa-heart"></i>
              </div>
              <h4 class="feature-title">我的收藏</h4>
              <p class="feature-description">珍藏每一份触动心灵的瞬间，汇聚成长路上的智慧点滴</p>
              <router-link :to="isLoggedIn ? '/my-favorites' : '/auth/login'" class="feature-link">
                查看收藏 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 个人中心 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon profile-icon">
                <i class="fas fa-user"></i>
              </div>
              <h4 class="feature-title">个人中心</h4>
              <p class="feature-description">管理您的个人信息，追踪成长足迹，定制专属心灵旅程</p>
              <router-link :to="isLoggedIn ? '/profile' : '/auth/login'" class="feature-link">
                进入中心 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
</script>

<style scoped>
/* 替换原机器人图标的样式 */
.mindfulness-icon {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(126, 211, 33, 0.2) 0%, rgba(74, 144, 226, 0.2) 100%);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow:
      0 20px 40px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: hidden;
  animation: pulse 4s ease-in-out infinite;
}

.mindfulness-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transform: rotate(45deg);
  animation: shine 6s ease-in-out infinite;
}

.mindfulness-icon i {
  font-size: 8rem;
  color: rgba(255, 255, 255, 0.9);
  position: relative;
  z-index: 1;
  text-shadow: 0 0 30px rgba(255, 255, 255, 0.5);
  animation: float 3s ease-in-out infinite;
}

/* 新动画效果 */
@keyframes pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1); }
  50% { transform: scale(1.03); box-shadow: 0 25px 45px rgba(74, 144, 226, 0.2); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes shine {
  0%, 100% { transform: translateX(-100%) rotate(45deg); }
  50% { transform: translateX(100%) rotate(45deg); }
}

/* 导入Font Awesome图标库样式 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.home {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 英雄区域样式 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 5rem 0;
  min-height: 70vh;
  display: flex;
  align-items: center;
}

.hero-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  align-items: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1.5rem;
}

.highlight-text {
  color: #ffd700;
}

.hero-description {
  font-size: 1.25rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: rgba(255, 255, 255, 0.9);
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-btn {
  background: white;
  color: #4A90E2;
  padding: 0.75rem 2rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 255, 255, 0.2);
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.primary-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 255, 255, 0.3);
}

.secondary-btn {
  border: 2px solid white;
  color: white;
  padding: 0.75rem 2rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.secondary-btn:hover {
  background: white;
  color: #4A90E2;
  transform: translateY(-2px);
}

.hero-image {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-wrapper {
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

/* 功能区域样式 */
.features-section {
  padding: 6rem 0;
  background-color: white;
}

.features-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.section-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1rem;
}

.section-description {
  font-size: 1.1rem;
  color: #666;
  max-width: 700px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  background-color: #f9f9f9;
  border-radius: 1rem;
  padding: 2rem;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.ai-icon {
  background: rgba(74, 144, 226, 0.1);
  color: #4A90E2;
}

.emotion-icon {
  background: rgba(255, 152, 158, 0.1);
  color: #ff9a9e;
}

.knowledge-icon {
  background: rgba(126, 211, 33, 0.1);
  color: #7ED321;
}

.assessment-icon {
  background: rgba(75, 192, 192, 0.1);
  color: #4bc0c0;
}

.feature-icon i {
  font-size: 2rem;
  padding: 1rem;
}

.feature-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.feature-description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.feature-link {
  display: inline-flex;
  align-items: center;
  color: #4A90E2;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.feature-link:hover {
  color: #3a7bc8;
}

.feature-link i {
  margin-left: 0.5rem;
  transition: transform 0.3s ease;
}

.feature-link:hover i {
  transform: translateX(5px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .hero-actions {
    justify-content: center;
  }

  .mindfulness-icon {
    width: 200px;
    height: 200px;
  }

  .mindfulness-icon i {
    font-size: 5rem;
  }

  .section-title {
    font-size: 2rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 3rem 0;
  }

  .hero-title {
    font-size: 1.75rem;
  }

  .primary-btn,
  .secondary-btn {
    padding: 0.5rem 1.5rem;
    font-size: 0.875rem;
  }

  .mindfulness-icon {
    width: 150px;
    height: 150px;
  }

  .mindfulness-icon i {
    font-size: 3.5rem;
  }

  .features-section {
    padding: 3rem 0;
  }

  .section-title {
    font-size: 1.75rem;
  }

  .feature-card {
    padding: 1.5rem;
  }
}

.favorite-icon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.profile-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.favorite-icon i, .profile-icon i {
  font-size: 2rem;
  padding: 1rem;
}
</style>
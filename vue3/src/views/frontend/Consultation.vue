<template>
  <div class="consultation-page">
    <div class="container">
      <div class="consultation-layout">
        <!-- 左侧工具栏 -->
        <div class="sidebar">
          <!-- AI助手信息 -->
          <div class="ai-assistant-info">
            <div class="assistant-avatar">
              <div class="breathing-circle">
                <i class="fas fa-robot"></i>
              </div>
            </div>
            <h3 class="assistant-name">小暖AI助手</h3>
            <div class="online-status">
              <div class="status-dot"></div>
              在线服务中
            </div>
          </div>

          <!-- 情绪花园 -->
          <div class="emotion-garden">
            <div class="garden-header">
              <div class="garden-title">
                <i class="fas fa-seedling garden-icon"></i>
                情绪花园
              </div>
              <div class="weather-icon">{{ currentEmotion.icon || '😐' }}</div>
            </div>
            
            <!-- 情绪花朵 -->
            <div class="emotion-flower">
              <div class="flower-center" :class="getEmotionFlowerClass(currentEmotion.isNegative)">
                <div class="emotion-name">{{ currentEmotion.primaryEmotion || '中性' }}</div>
                <div class="emotion-score">{{ currentEmotion.emotionScore || 50 }}</div>
              </div>
              <div class="flower-petals">
                <div class="petal petal-1" :class="getPetalClass(currentEmotion.emotionScore)"></div>
                <div class="petal petal-2" :class="getPetalClass(currentEmotion.emotionScore)"></div>
                <div class="petal petal-3" :class="getPetalClass(currentEmotion.emotionScore)"></div>
                <div class="petal petal-4" :class="getPetalClass(currentEmotion.emotionScore)"></div>
                <div class="petal petal-5" :class="getPetalClass(currentEmotion.emotionScore)"></div>
                <div class="petal petal-6" :class="getPetalClass(currentEmotion.emotionScore)"></div>
              </div>
            </div>

            <!-- 温暖提示 -->
            <div class="warm-tips">
              <div class="emotion-status-text">
                <span class="status-label">今天感觉</span>
                <span class="status-emotion" :class="getEmotionStatusClass(currentEmotion.isNegative)">
                  {{ getEmotionStatusText(currentEmotion.isNegative) }}
                </span>
              </div>
              
              <div class="emotion-intensity">
                <span class="intensity-dots">
                  <span 
                    v-for="i in 3" 
                    :key="i" 
                    class="dot" 
                    :class="{ active: getIntensityDots(currentEmotion.emotionScore) >= i }"
                  ></span>
                </span>
                <span class="intensity-text">{{ getIntensityText(currentEmotion.emotionScore) }}</span>
              </div>
            </div>

            <!-- 情绪关键词云 -->
            <div class="emotion-cloud" v-if="currentEmotion.keywords && currentEmotion.keywords.length > 0">
              <div class="cloud-title">💭 心情关键词</div>
              <div class="keywords-cloud">
                <span 
                  v-for="(keyword, index) in currentEmotion.keywords" 
                  :key="keyword" 
                  class="keyword-bubble"
                  :style="{ animationDelay: index * 0.2 + 's' }"
                >
                  {{ keyword }}
                </span>
              </div>
            </div>

            <!-- 温暖建议卡片 -->
            <div class="warm-suggestion" v-if="currentEmotion.suggestion">
              <div class="suggestion-icon">💝</div>
              <div class="suggestion-content">
                <div class="suggestion-title">给你的小建议</div>
                <div class="suggestion-text">{{ currentEmotion.suggestion }}</div>
              </div>
            </div>

            <!-- 治愈行动清单 -->
            <div class="healing-actions" v-if="currentEmotion.improvementSuggestions && currentEmotion.improvementSuggestions.length > 0">
              <div class="actions-title">
                <i class="fas fa-heart healing-heart"></i>
                治愈小行动
              </div>
              <div class="actions-list">
                <div 
                  v-for="(action, index) in currentEmotion.improvementSuggestions" 
                  :key="action"
                  class="action-item"
                  :style="{ animationDelay: index * 0.1 + 's' }"
                >
                  <div class="action-icon">✨</div>
                  <div class="action-text">{{ action }}</div>
                </div>
              </div>
            </div>

            <!-- 风险提示 -->
            <div class="risk-notice" v-if="currentEmotion.isNegative && currentEmotion.riskLevel > 1">
              <div class="notice-icon">🤗</div>
              <div class="notice-content">
                <div class="notice-title">温馨提醒</div>
                <div class="notice-text">{{ currentEmotion.riskDescription || '需要更多关注和陪伴' }}</div>
              </div>
            </div>
          </div>

          <!-- 会话历史 -->
          <div class="session-history">
            <h4 class="section-title">
              会话历史
              <div class="title-actions">
                <el-button 
                  text 
                  size="small" 
                  type="primary"
                  @click="createNewFrontendSession"
                  title="新建会话"
                >
                  <i class="fas fa-plus"></i>
                </el-button>
                <el-button 
                  text 
                  size="small" 
                  @click="refreshSessionList"
                  :loading="sessionListLoading"
                  title="刷新列表"
                >
                  <i class="fas fa-sync-alt"></i>
                </el-button>
              </div>
            </h4>
            <div class="session-list" v-loading="sessionListLoading">
              <div 
                v-for="session in sessionList" 
                :key="session.id"
                class="session-item"
                :class="{ 
                  'active': currentSession && currentSession.dbSessionId == session.id 
                }"
                :data-session-id="session.id"
                @click="switchToSession(session)"
              >
                <div class="session-info">
                  <div class="session-title" @dblclick="startEditTitle(session)">
                    <span v-if="!session.isEditing">{{ session.sessionTitle || '未命名会话' }}</span>
                    <el-input 
                      v-else
                      v-model="session.editingTitle"
                      size="small"
                      @blur="saveSessionTitle(session)"
                      @keyup.enter="saveSessionTitle(session)"
                      @keyup.esc="cancelEditTitle(session)"
                      ref="titleInput"
                      placeholder="请输入会话标题"
                    />
                  </div>
                  <div class="session-meta">
                    <span class="session-time">{{ formatSessionTime(session.startedAt) }}</span>
                  </div>
                  <div class="session-preview">
                    {{ session.lastMessageContent || '暂无消息' }}
                  </div>
                  <div class="session-stats">
                    <span class="message-count">
                      <i class="fas fa-comments"></i>
                      {{ session.messageCount || 0 }}
                    </span>
                    <span class="duration">
                      <i class="fas fa-clock"></i>
                      {{ session.durationMinutes || 0 }}分钟
                    </span>
                  </div>
                </div>
                <div class="session-actions">
                  <el-button 
                    text 
                    size="small" 
                    type="primary"
                    @click.stop="startEditTitle(session)"
                    title="编辑标题"
                  >
                    <i class="fas fa-edit"></i>
                  </el-button>
                  <el-button 
                    text 
                    size="small" 
                    type="danger"
                    @click.stop="confirmDeleteSession(session)"
                    title="删除会话"
                  >
                    <i class="fas fa-trash"></i>
                  </el-button>
                </div>
              </div>
              
              <!-- 空状态 -->
              <div v-if="sessionList.length === 0 && !sessionListLoading" class="empty-sessions">
                <i class="fas fa-comments"></i>
                <p>暂无会话记录</p>
              </div>
              
              <!-- 加载更多 -->
              <div v-if="sessionList.length > 0 && hasMoreSessions" class="load-more">
                <el-button 
                  text 
                  size="small" 
                  @click="loadMoreSessions"
                  :loading="loadingMore"
                >
                  加载更多
                </el-button>
              </div>
            </div>
          </div>


          <!-- 紧急求助 -->
          <div class="emergency-help">
            <h4 class="emergency-title">紧急求助</h4>
            <p class="emergency-desc">如果您正在经历心理危机，请立即寻求专业帮助</p>
            <el-button type="danger" class="emergency-btn" @click="showEmergencyDialog">
              <i class="fas fa-phone"></i>
              危机热线
            </el-button>
          </div>
        </div>

        <!-- 主聊天区域 -->
        <div class="chat-main">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="header-left">
              <div class="chat-avatar">
                <i class="fas fa-heart"></i>
              </div>
              <div class="chat-info">
                <div class="chat-title-container">
                  <h2 v-if="!currentSession || !isEditingHeaderTitle" class="chat-title" @dblclick="startEditHeaderTitle">
                    {{ currentSession ? (currentSession.sessionTitle || '未命名会话') : '小暖AI助手' }}
                  </h2>
                  <el-input 
                    v-else
                    v-model="headerTitleEdit"
                    size="large"
                    @blur="saveHeaderTitle"
                    @keyup.enter="saveHeaderTitle"
                    @keyup.esc="cancelEditHeaderTitle"
                    ref="headerTitleInput"
                    placeholder="请输入会话标题"
                    class="header-title-input"
                  />
                  <el-button 
                    v-if="currentSession && !isEditingHeaderTitle"
                    text 
                    size="small" 
                    @click="startEditHeaderTitle"
                    class="edit-title-btn"
                    title="编辑标题"
                  >
                    <i class="fas fa-edit"></i>
                  </el-button>
                </div>
                <p class="chat-subtitle">{{ currentSession ? '专业 · 陪伴 · 理解' : '您的贴心AI心理健康助手' }}</p>
              </div>
            </div>
            <div class="header-actions">
              <el-button circle @click="createNewFrontendSession" title="新建会话">
                <i class="fas fa-plus"></i>
              </el-button>
           
            </div>
          </div>

          <!-- 聊天消息区域 -->
          <div class="chat-messages" ref="messagesContainer">
            <!-- 欢迎消息 -->
            <div v-if="messages.length === 0" class="welcome-message">
              <div class="message-item ai-message">
                <div class="message-avatar">
                  <i class="fas fa-robot"></i>
                </div>
                <div class="message-content">
                  <div class="message-bubble">
                    <p>您好！我是小暖，您的AI心理健康助手。很高兴陪伴您，为您提供温暖的心理支持。请告诉我，今天您感觉怎么样？有什么想要分享的吗？</p>
                  </div>
                  <div class="message-time">刚刚</div>
                </div>
              </div>
            </div>

            <!-- 消息列表 -->
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="message.senderType === 1 ? 'user-message' : 'ai-message'"
            >
              <div class="message-avatar">
                <i :class="message.senderType === 1 ? 'fas fa-user' : 'fas fa-robot'"></i>
              </div>
              <div class="message-content">
                <div class="message-bubble" :class="{ 
                  'typing': message.senderType === 2 && message.isTyping,
                  'error': message.isError
                }">
                  <!-- AI消息正在输入状态 -->
                  <div v-if="message.senderType === 2 && message.isTyping && !message.content" class="typing-indicator">
                    <div class="typing-dot"></div>
                    <div class="typing-dot"></div>
                    <div class="typing-dot"></div>
                  </div>
                  <!-- AI错误消息 -->
                  <div v-else-if="message.isError" class="error-message">
                    <i class="fas fa-exclamation-triangle"></i>
                    {{ message.content }}
                  </div>
                  <!-- 使用MarkdownRenderer组件渲染AI消息（仅当消息完整时） -->
                  <MarkdownRenderer
                    v-else-if="message.senderType === 2 && message.isComplete && !message.isError"
                    :content="message.content"
                    :is-ai-message="true"
                  />
                  <!-- 流式输出中的AI消息或用户消息保持原样 -->
                  <p v-else-if="message.content" v-html="formatMessageContent(message.content)"></p>
                  <div v-if="message.emotionTag" class="emotion-tag">
                    {{ message.emotionTag }}
                  </div>
                </div>
                <div class="message-time">
                  {{ message.senderType === 2 && message.isTyping ? '正在输入...' : formatTime(message.createdAt) }}
                </div>
              </div>
            </div>


          </div>

          <!-- 消息输入区域 -->
          <div class="chat-input">
            <div class="input-container">
              <el-input
                v-model="userMessage"
                type="textarea"
                :rows="3"
                placeholder="请输入您想要分享的内容..."
                :disabled="isLoading || isAiTyping"
                @keydown="handleKeyDown"
                resize="none"
                class="message-input"
              >
                <template #suffix>
                  <div class="input-actions">
                    <el-button text @click="showEmojiPicker">
                      <i class="fas fa-smile"></i>
                    </el-button>
                  </div>
                </template>
              </el-input>
              <div class="input-footer">
                <span class="input-tip">按Enter发送，Shift+Enter换行</span>
                <span class="char-count">{{ userMessage.length }}/500</span>
              </div>
            </div>
            <el-button 
              type="primary" 
              class="send-btn"
              :loading="isLoading || isAiTyping"
              @click="sendMessage"
              :disabled="!userMessage.trim() || userMessage.length > 500"
            >
              <i class="fas fa-paper-plane"></i>
            </el-button>
          </div>
        </div>
      </div>
    </div>


    <!-- 紧急求助对话框 -->
    <el-dialog v-model="showEmergency" title="紧急求助" width="500px">
      <div class="emergency-content">
        <div class="emergency-item">
          <h4>24小时心理危机干预热线</h4>
          <p class="hotline-number">400-161-9995</p>
          <p class="hotline-desc">免费专业心理咨询服务</p>
        </div>
        <div class="emergency-item">
          <h4>全国心理援助热线</h4>
          <p class="hotline-number">400-1619-995</p>
          <p class="hotline-desc">24小时免费心理咨询</p>
        </div>
        <div class="emergency-item">
          <h4>如果您有自杀或自伤想法</h4>
          <p class="emergency-warning">请立即拨打以上热线或前往最近的医院急诊科</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showEmergency = false">我知道了</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  startChatSession, 
  getSessionMessages,
  getSessionEmotion,
  getSessionsPage,
  getSessionDetail,
  deleteSession,
  updateSessionTitle
} from '@/api/consultationSession'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
const userStore = useUserStore()
// 响应式数据
const currentSession = ref(null)
const messages = ref([])
const userMessage = ref('')
const isLoading = ref(false)
const isAiTyping = ref(false)
const riskWarning = ref('')
const currentEmotion = ref({})
const messagesContainer = ref(null)

// 情绪分析轮询相关
const emotionPollingInterval = ref(null)
const emotionPollingCount = ref(0)
const maxEmotionPollingCount = 30 // 最大轮询30次（60秒）

// 对话框状态
const showEmergency = ref(false)

// Header标题编辑状态
const isEditingHeaderTitle = ref(false)
const headerTitleEdit = ref('')
const headerTitleInput = ref(null)

// 会话列表相关数据
const sessionList = ref([])
const sessionListLoading = ref(false)
const loadingMore = ref(false)
const hasMoreSessions = ref(true)
const sessionQuery = reactive({
  currentPage: 1,
  size: 10,
  keyword: ''
})



// 生命周期
onMounted(() => {
  loadSessionList()
  // 初始化时创建一个默认的临时会话（不显示消息）
  createNewFrontendSession(false)
})

onUnmounted(() => {
  // 清理资源
  stopEmotionPolling()
  console.log('心理咨询组件卸载')
})

// 方法

// 情绪分析轮询管理方法
const startEmotionPolling = (sessionId) => {
  // 先停止现有轮询
  stopEmotionPolling()
  
  if (!sessionId) {
    console.warn('无效的会话ID，无法开始情绪分析轮询')
    return
  }
  
  console.log('开始情绪分析轮询，会话ID:', sessionId)
  emotionPollingCount.value = 0
  
  // 立即获取一次
  loadSessionEmotion(sessionId)
  
  // 开始定时轮询
  emotionPollingInterval.value = setInterval(() => {
    emotionPollingCount.value++
    
    // 超过最大轮询次数则停止
    if (emotionPollingCount.value >= maxEmotionPollingCount) {
      console.log('情绪分析轮询达到最大次数，停止轮询')
      stopEmotionPolling()
      return
    }
    
    // 继续轮询
    loadSessionEmotion(sessionId)
  }, 2000) // 每2秒轮询一次
}

const stopEmotionPolling = () => {
  if (emotionPollingInterval.value) {
    clearInterval(emotionPollingInterval.value)
    emotionPollingInterval.value = null
    emotionPollingCount.value = 0
    console.log('情绪分析轮询已停止')
  }
}

const startNewSession = async (initialMessage, customTitle = null) => {
  try {
    isLoading.value = true
    
    // 构建会话参数
    const sessionParams = {
      initialMessage: initialMessage
    }
    
    // 优先使用自定义标题，其次使用临时会话的标题，最后使用默认格式
    if (customTitle) {
      sessionParams.sessionTitle = customTitle
    } else if (currentSession.value && currentSession.value.status === 'TEMP' && currentSession.value.sessionTitle !== '新对话') {
      sessionParams.sessionTitle = currentSession.value.sessionTitle
    } else {
      sessionParams.sessionTitle = `小暖助手 - ${new Date().toLocaleString()}`
    }
    
    // 使用数据库持久化会话接口
    await startChatSession(sessionParams, {
      onSuccess: async (data) => {
        // 将数据库会话转换为前端会话格式
        const sessionData = {
          sessionId: data.sessionId, // 使用返回的会话ID (session_xxx格式)
          userHash: data.userHash,
          status: 'ACTIVE',
          dbSessionId: data.sessionId.replace('session_', ''), // 提取纯数字ID
          sessionTitle: data.sessionTitle
        }
        
        console.log('数据库会话创建成功:', sessionData)
        
        // 如果当前有临时会话，更新其信息；否则创建新会话
        if (currentSession.value && currentSession.value.status === 'TEMP') {
          // 更新临时会话为正式会话
          Object.assign(currentSession.value, sessionData)
        } else {
          // 创建新会话
          currentSession.value = sessionData
        }
        
        // 刷新会话列表
        refreshSessionList()
        
        // 添加初始用户消息到界面
        messages.value.push({
          id: Date.now(),
          senderType: 1,
          content: initialMessage,
          createdAt: new Date().toISOString(),
          isComplete: true
        })
        
        // 开始流式对话
        await startAIResponse(data.sessionId, initialMessage)
      },
      onError: async (error) => {
        console.error('创建数据库会话失败:', error)
        
        // 简化错误处理
        
        ElMessage.error(error.message || '创建会话失败')
      }
    })
  } catch (error) {
    console.error('开始新会话失败:', error)
    ElMessage.error('创建会话失败')
  } finally {
    isLoading.value = false
  }
}

const sendMessage = async () => {
  if (!userMessage.value.trim()) return
  
  // 防止在AI回复中发送消息
  if (isAiTyping.value) {
    ElMessage.warning('请等待AI回复完成后再发送消息')
    return
  }
  
  const message = userMessage.value.trim()
  userMessage.value = ''

  try {
    if (!currentSession.value || currentSession.value.status === 'TEMP') {
      // 如果没有会话或者是临时会话，创建新的后端会话
      await startNewSession(message)
    } else {
      // 继续现有会话
      // 添加用户消息到界面
      messages.value.push({
        id: `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        senderType: 1,
        content: message,
        createdAt: new Date().toISOString(),
        isComplete: true
      })
      
      scrollToBottom()
      
      // 开始流式对话
      await startAIResponse(currentSession.value.sessionId, message)
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

function getAuthToken() {
  // 优先从用户store获取token
  if (userStore.token) {
    return userStore.token
  }
  
  // 降级方案：直接从localStorage获取
  return localStorage.getItem('token')
}
const startAIResponse = async (sessionId, userMessage) => {
  // 防止重复发送：如果已经在输入中，直接返回
  if (isAiTyping.value) {
    console.warn('AI正在回复中，忽略重复请求')
    return
  }
  
  isAiTyping.value = true
  riskWarning.value = ''
  const token = getAuthToken()
  
  // 创建AI消息占位符，使用唯一ID便于后续管理
  const aiMessageId = `ai_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  const aiMessage = {
    id: aiMessageId,
    senderType: 2,
    content: '',
    createdAt: new Date().toISOString(),
    isComplete: false,
    isTyping: true // 添加打字状态标记
  }
  messages.value.push(aiMessage)
  
  // 使用消息ID而非索引来管理消息
  const findAiMessage = () => messages.value.find(msg => msg.id === aiMessageId)
  
  const ctrl = new AbortController()
  
  // 在流式对话完成后开始持续的情绪分析轮询
  const startSessionEmotionPolling = () => {
    if (currentSession.value) {
      const sessionId = currentSession.value.dbSessionId || currentSession.value.sessionId
      startEmotionPolling(sessionId)
    }
  }
  
  // 统一的清理函数
  const cleanup = (markComplete = false) => {
    isAiTyping.value = false
    
    const aiMessage = findAiMessage()
    if (aiMessage) {
      aiMessage.isTyping = false
      if (markComplete) {
        aiMessage.isComplete = true
      }
    }
    
    // 如果对话完成，开始情绪分析轮询
    if (markComplete) {
      startSessionEmotionPolling()
    }
  }
  
  // 错误处理函数
  const handleError = (error, shouldRemoveMessage = false) => {
    console.error('AI回复错误:', error)
    cleanup(false)
    
    if (shouldRemoveMessage) {
      // 移除失败的消息占位符
      const messageIndex = messages.value.findIndex(msg => msg.id === aiMessageId)
      if (messageIndex !== -1) {
        messages.value.splice(messageIndex, 1)
      }
    } else {
      // 将失败的消息标记为错误状态
      const aiMessage = findAiMessage()
      if (aiMessage) {
        aiMessage.content = '❌ AI回复失败，请重试'
        aiMessage.isComplete = true
        aiMessage.isError = true
      }
    }
    
    ElMessage.error(typeof error === 'string' ? error : (error.message || 'AI回复失败'))
  }
  
  // 注意：这里不需要立即启动轮询，会在对话完成后启动
  
  try {
    await fetchEventSource('/api/psychological-chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Token': token,
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        sessionId: sessionId,
        userMessage: userMessage
      }),
      signal: ctrl.signal,
      
      onopen: async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`)
        }
        const contentType = response.headers.get('content-type') || ''
        if (!contentType.includes('text/event-stream')) {
          throw new Error('响应类型不是SSE: ' + contentType)
        }
      },
      
      onclose: () => {
        // 连接正常关闭
        cleanup(true)
        nextTick(() => { scrollToBottom() })
      },
      
      onmessage: (event) => {
        let raw = (event.data || '').trim()
        if (!raw) return
        console.log("流式响应：", raw)
        
        // 兼容某些代理把整块SSE原文塞进 data 的情况
        let evtName = event.event
        let payloadText = raw
        if (!evtName && raw.startsWith('event:')) {
          const lines = raw.split('\n')
          const evLine = lines.find(l => l.startsWith('event:'))
          const dataLine = lines.find(l => l.startsWith('data:'))
          evtName = evLine ? evLine.slice(6).trim() : ''
          payloadText = dataLine ? dataLine.slice(5).trim() : ''
        }
        
        if (evtName === 'done') {
          cleanup(true)
          nextTick(() => { scrollToBottom() })
          ctrl.abort()
          return
        }
        
        try {
          const payload = JSON.parse(payloadText)
          const ok = String(payload.code) === '200'
          
          // 统一后端字段为 data.content
          if (ok && payload.data && payload.data.content) {
            // 检查是否是风险警告消息
            if (evtName === 'risk-warning' || payload.data.type === 'risk') {
              console.log('🚨 收到风险警告消息:', payload.data.content)
              
              // 为风险警告创建特殊样式的消息
              const riskMessage = {
                id: Date.now() + Math.random(),
                senderType: 2,
                content: `<div class="risk-warning-message">
                  <div class="risk-warning-header">
                    <i class="fas fa-exclamation-triangle risk-icon"></i>
                    <span class="risk-title">心理健康提醒</span>
                  </div>
                  <div class="risk-warning-content">${payload.data.content}</div>
                </div>`,
                createdAt: new Date().toISOString(),
                isRiskWarning: true,
                isComplete: true // 风险警告消息直接标记为完整
              }
              
              messages.value.push(riskMessage)
              console.log('✅ 风险警告已添加到消息列表')
              nextTick(() => { scrollToBottom() })
            } else {
              // 正常消息处理
              const aiMessage = findAiMessage()
              if (aiMessage) {
                aiMessage.content += payload.data.content
                console.log('✅ 累积内容:', aiMessage.content.length, '字符')
                scrollToBottom()
              }
            }
          } else if (!ok) {
            console.error('服务返回错误:', payload)
            handleError(payload.message || 'AI回复失败', false)
          }
        } catch (e) {
          console.error('解析流式数据失败:', e, payloadText)
          handleError('数据解析失败', false)
        }
      },
      
      onerror: (err) => {
        console.error('流式对话发生错误:', err)
        handleError(err, false)
        throw err
      }
    })
  } catch (error) {
    console.error('发送流式消息失败:', error)
    handleError(error, true) // 连接失败时移除消息占位符
  } finally {
    // 兜底确保状态清理
    if (isAiTyping.value) {
      cleanup(false)
    }
  }
}

const loadSessionMessages = async (sessionId) => {
  try {
    await getSessionMessages(sessionId, {
      onSuccess: (sessionMessages) => {
        // 为加载的消息添加isComplete属性
        messages.value = sessionMessages.map(msg => ({
          ...msg,
          isComplete: true
        }))
        nextTick(() => {
          scrollToBottom()
        })
      }
    })
  } catch (error) {
    console.error('加载会话消息失败:', error)
  }
}

const loadSessionEmotion = async (sessionId) => {
  try {
    // 确保sessionId格式正确，如果不是以session_开头则添加前缀
    const formattedSessionId = sessionId.toString().startsWith('session_') 
      ? sessionId 
      : `session_${sessionId}`
    
    console.log('loadSessionEmotion - 输入sessionId:', sessionId, '处理后:', formattedSessionId)
    
    // 从数据库获取情绪状态
    await getSessionEmotion(formattedSessionId, {
      onSuccess: (data) => {
        console.log('获取情绪分析成功:', data)
        
        // 检查是否有新的情绪分析结果
        const newTimestamp = data.timestamp || Date.now()
        const currentTimestamp = currentEmotion.value.timestamp || 0
        
        // 如果有新的分析结果，则停止轮询（避免不必要的请求）
        if (newTimestamp > currentTimestamp && emotionPollingCount.value > 0) {
          console.log('检测到新的情绪分析结果，停止轮询')
          stopEmotionPolling()
        }
        
        // 更新情绪状态
        currentEmotion.value = {
          primaryEmotion: data.primaryEmotion || '中性',
          emotionScore: data.emotionScore || 50,
          isNegative: data.isNegative || false,
          riskLevel: data.riskLevel || 0,
          keywords: data.keywords || [],
          suggestion: data.suggestion || '情绪状态平稳',
          icon: data.icon || '😐',
          label: data.label || '良好',
          riskDescription: data.riskDescription || '情绪状态稳定',
          improvementSuggestions: data.improvementSuggestions || [],
          timestamp: newTimestamp
        }
      },
      onError: (error) => {
        console.log('暂无情绪状态，使用默认值:', error)
        
        // 设置默认情绪状态
        if (Object.keys(currentEmotion.value).length === 0) {
          currentEmotion.value = {
            primaryEmotion: '中性',
            emotionScore: 50,
            isNegative: false,
            riskLevel: 0,
            keywords: [],
            suggestion: '情绪状态平稳',
            icon: '😐',
            label: '良好',
            riskDescription: '情绪状态稳定',
            improvementSuggestions: [],
            timestamp: Date.now()
          }
        }
      }
    })
  } catch (error) {
    console.warn('加载情绪分析失败:', error)
    // 只有在没有情绪状态时才设置默认值
    if (Object.keys(currentEmotion.value).length === 0) {
      currentEmotion.value = {
        primaryEmotion: '中性',
        emotionScore: 50,
        isNegative: false,
        riskLevel: 0,
        keywords: [],
        suggestion: '情绪状态平稳',
        icon: '😐',
        label: '良好',
        riskDescription: '情绪状态稳定',
        improvementSuggestions: [],
        timestamp: Date.now()
      }
    }
  }
}

// 会话列表相关方法
const loadSessionList = async (reset = true) => {
  if (reset) {
    sessionQuery.currentPage = 1
    sessionListLoading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    await getSessionsPage(sessionQuery, {
      onSuccess: (data) => {
        if (reset) {
          sessionList.value = data.records || []
        } else {
          sessionList.value.push(...(data.records || []))
        }
        
        // 检查是否还有更多数据
        hasMoreSessions.value = sessionQuery.currentPage < data.pages
        
        console.log('会话列表加载成功:', data)
      },
      onError: (error) => {
        console.error('加载会话列表失败:', error)
        ElMessage.error('加载会话列表失败')
      }
    })
  } catch (error) {
    console.error('加载会话列表异常:', error)
  } finally {
    sessionListLoading.value = false
    loadingMore.value = false
  }
}

const refreshSessionList = () => {
  loadSessionList(true)
}

const loadMoreSessions = () => {
  if (hasMoreSessions.value && !loadingMore.value) {
    sessionQuery.currentPage++
    loadSessionList(false)
  }
}

const switchToSession = async (session) => {
  if (session.id === currentSession.value?.dbSessionId) {
    return // 已经是当前会话
  }

  try {
    // 切换到选中的会话（后端会自动处理会话状态）
    const restoredSession = {
      sessionId: "session_" + session.id,
      userHash: session.userId,
      status: 'ACTIVE', // 切换后默认为活跃状态
      dbSessionId: session.id,
      sessionTitle: session.sessionTitle
    }
    
    currentSession.value = restoredSession
    
    // 加载会话消息和情绪状态
    await loadSessionMessages(session.id)
    await loadSessionEmotion(session.id)
    
    // 开始新会话的情绪分析轮询
    startEmotionPolling(session.id)
    
    // 刷新会话列表以更新状态
    refreshSessionList()
    
    ElMessage.success(`已切换到会话：${session.sessionTitle || '未命名会话'}`)
    
  } catch (error) {
    console.error('切换会话失败:', error)
    ElMessage.error('切换会话失败')
  }
}


const confirmDeleteSession = async (session) => {
  try {
    const result = await ElMessageBox.confirm(
      `确定要删除会话"${session.sessionTitle || '未命名会话'}"吗？\n\n删除后将无法恢复，包括所有聊天记录。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: false
      }
    )
    
    if (result !== 'confirm') return
    
    await deleteSession(session.id, {
      successMsg: '会话删除成功',
      onSuccess: () => {
        // 如果删除的是当前会话，创建新的临时会话
        if (currentSession.value?.dbSessionId === session.id) {
          createNewFrontendSession(false) // 创建新的临时会话但不显示成功消息
          ElMessage.success('会话已删除，已为您创建新对话')
        }
        
        // 刷新会话列表
        refreshSessionList()
      },
      onError: (error) => {
        console.error('删除会话失败:', error)
        ElMessage.error('删除会话失败：' + (error.message || '未知错误'))
      }
    })
    
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除会话失败:', error)
  }
}

// 工具方法
const formatSessionTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString()
}


// 情绪状态相关工具方法
const getEmotionFlowerClass = (isNegative) => {
  if (isNegative) return 'flower-negative'
  return 'flower-positive'
}

const getEmotionStatusClass = (isNegative) => {
  if (isNegative) return 'status-negative'
  return 'status-positive'
}

const getEmotionStatusText = (isNegative) => {
  if (isNegative) return '需要关注'
  return '很不错'
}

const getIntensityText = (score) => {
  if (score >= 61) return '强烈'
  if (score >= 31) return '中等'
  return '轻微'
}

const getRiskClass = (level) => {
  switch (level) {
    case 0: return 'risk-normal'
    case 1: return 'risk-attention'
    case 2: return 'risk-warning'
    case 3: return 'risk-crisis'
    default: return 'risk-normal'
  }
}

const getRiskText = (level) => {
  switch (level) {
    case 0: return '正常'
    case 1: return '关注'
    case 2: return '预警'
    case 3: return '危机'
    default: return '正常'
  }
}

// 治愈风格的工具方法
const getPetalClass = (score) => {
  if (score >= 61) return 'petal-bloom'
  if (score >= 31) return 'petal-growing'
  return 'petal-budding'
}

const getIntensityDots = (score) => {
  if (score >= 61) return 3
  if (score >= 31) return 2
  return 1
}





const showEmergencyDialog = () => {
  showEmergency.value = true
}

// 新建会话相关方法
const createNewFrontendSession = (showMessage = true) => {
  // 创建一个新的前端会话对象，不调用后端接口
  const newSession = {
    sessionId: `temp_${Date.now()}`, // 临时ID，发送消息时会被替换
    userHash: null,
    status: 'TEMP', // 临时状态，表示还未在后端创建
    dbSessionId: null, // 数据库ID为空
    sessionTitle: '新对话', // 默认标题
    isNew: true // 标记为新会话
  }
  
  // 设置为当前会话
  currentSession.value = newSession
  
  // 清空消息列表
  messages.value = []
  
  // 清空当前情绪状态和停止轮询
  currentEmotion.value = {}
  stopEmotionPolling()
  
  // 滚动到顶部
  nextTick(() => {
    scrollToBottom()
  })
  
  if (showMessage) {
    ElMessage.success('新会话已创建，请开始对话')
  }
}


// 会话标题编辑相关方法
const startEditTitle = (session) => {
  // 设置编辑状态
  session.isEditing = true
  session.editingTitle = session.sessionTitle || ''
  
  // 使用nextTick确保DOM更新后再聚焦
  nextTick(() => {
    // 这里需要通过ref获取input元素并聚焦
    const inputElements = document.querySelectorAll('.session-item .el-input__inner')
    const targetInput = Array.from(inputElements).find(input => 
      input.closest('.session-item') === document.querySelector(`[data-session-id="${session.id}"]`)
    )
    if (targetInput) {
      targetInput.focus()
      targetInput.select()
    }
  })
}

const saveSessionTitle = async (session) => {
  if (!session.isEditing) return
  
  const newTitle = (session.editingTitle || '').trim()
  
  // 如果标题没有变化，直接取消编辑
  if (newTitle === (session.sessionTitle || '')) {
    cancelEditTitle(session)
    return
  }
  
  try {
    await updateSessionTitle(session.id, {
      sessionTitle: newTitle || null // 空标题传null，让后端生成默认标题
    }, {
      onSuccess: () => {
        // 更新本地数据
        session.sessionTitle = newTitle || `小暖助手 - ${formatSessionTime(session.startedAt)}`
        session.isEditing = false
        
        // 如果是当前会话，也要更新currentSession的标题
        if (currentSession.value && currentSession.value.dbSessionId === session.id) {
          // 这里可以考虑是否需要更新currentSession的标题显示
        }
        
        ElMessage.success('标题更新成功')
      },
      onError: (error) => {
        console.error('更新会话标题失败:', error)
        ElMessage.error('更新标题失败')
        cancelEditTitle(session) // 失败时恢复原状态
      }
    })
  } catch (error) {
    console.error('更新会话标题异常:', error)
    ElMessage.error('更新标题失败')
    cancelEditTitle(session)
  }
}

const cancelEditTitle = (session) => {
  session.isEditing = false
  session.editingTitle = ''
}

// Header标题编辑相关方法
const startEditHeaderTitle = () => {
  if (!currentSession.value) {
    ElMessage.warning('请先选择一个会话')
    return
  }
  
  // 如果是临时会话，只允许编辑本地标题
  if (currentSession.value.status === 'TEMP') {
    isEditingHeaderTitle.value = true
    headerTitleEdit.value = currentSession.value.sessionTitle || ''
    
    nextTick(() => {
      if (headerTitleInput.value) {
        headerTitleInput.value.focus()
        headerTitleInput.value.select()
      }
    })
    return
  }
  
  isEditingHeaderTitle.value = true
  headerTitleEdit.value = currentSession.value.sessionTitle || ''
  
  nextTick(() => {
    if (headerTitleInput.value) {
      headerTitleInput.value.focus()
      headerTitleInput.value.select()
    }
  })
}

const saveHeaderTitle = async () => {
  if (!currentSession.value || !isEditingHeaderTitle.value) return
  
  const newTitle = headerTitleEdit.value.trim()
  
  // 如果标题没有变化，直接取消编辑
  if (newTitle === (currentSession.value.sessionTitle || '')) {
    cancelEditHeaderTitle()
    return
  }
  
  // 如果是临时会话，只更新本地标题
  if (currentSession.value.status === 'TEMP') {
    currentSession.value.sessionTitle = newTitle || '新对话'
    isEditingHeaderTitle.value = false
    ElMessage.success('标题已更新（将在发送第一条消息时保存）')
    return
  }
  
  // 正式会话，更新后端
  try {
    await updateSessionTitle(currentSession.value.dbSessionId, {
      sessionTitle: newTitle || null
    }, {
      onSuccess: () => {
        // 更新当前会话标题
        currentSession.value.sessionTitle = newTitle || `小暖助手 - ${new Date().toLocaleString()}`
        
        // 同时更新会话列表中对应的项目
        const sessionInList = sessionList.value.find(s => s.id === currentSession.value.dbSessionId)
        if (sessionInList) {
          sessionInList.sessionTitle = currentSession.value.sessionTitle
        }
        
        isEditingHeaderTitle.value = false
        ElMessage.success('标题更新成功')
      },
      onError: (error) => {
        console.error('更新Header标题失败:', error)
        ElMessage.error('更新标题失败')
        cancelEditHeaderTitle()
      }
    })
  } catch (error) {
    console.error('更新Header标题异常:', error)
    ElMessage.error('更新标题失败')
    cancelEditHeaderTitle()
  }
}

const cancelEditHeaderTitle = () => {
  isEditingHeaderTitle.value = false
  headerTitleEdit.value = ''
}

const showEmojiPicker = () => {
  ElMessage.info('表情选择功能开发中...')
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessageContent = (content) => {
  // 简单的换行处理
  return content.replace(/\n/g, '<br>')
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 24小时内
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
  }
}
</script>

<style scoped>
.consultation-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fafbfc 0%, #f7f9fc 50%, #f2f6fa 100%);
  padding: 20px 0;
  position: relative;
}

.consultation-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 20%, rgba(255, 242, 235, 0.3) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(240, 248, 255, 0.2) 0%, transparent 50%);
  pointer-events: none;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.consultation-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  min-height: calc(100vh - 40px);
}

/* 左侧边栏样式 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ai-assistant-info {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 252, 248, 0.95) 100%);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(251, 146, 60, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(251, 146, 60, 0.08);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.emergency-help {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 252, 248, 0.95) 100%);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(251, 146, 60, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(251, 146, 60, 0.08);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.ai-assistant-info:hover,
.emotion-garden:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(251, 146, 60, 0.1), 0 4px 12px rgba(0, 0, 0, 0.06);
}

.ai-assistant-info {
  text-align: center;
}

.breathing-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #fb923c 0%, #f59e0b 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  animation: breathing 4s ease-in-out infinite;
  box-shadow: 0 6px 24px rgba(251, 146, 60, 0.25);
  position: relative;
}

.breathing-circle::before {
  content: '';
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.3), rgba(245, 158, 11, 0.3));
  border-radius: 50%;
  animation: breathing-glow 4s ease-in-out infinite;
  z-index: -1;
}

@keyframes breathing-glow {
  0%, 100% { 
    opacity: 0.5;
    transform: scale(1);
  }
  50% { 
    opacity: 0.8;
    transform: scale(1.1);
  }
}

@keyframes breathing {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.breathing-circle i {
  color: white;
  font-size: 20px;
}

.assistant-name {
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #fb923c, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 12px;
}


.online-status {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #059669;
  font-size: 12px;
  font-weight: 600;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #059669;
  border-radius: 50%;
  margin-right: 8px;
  animation: pulse 2s infinite;
  box-shadow: 0 0 8px rgba(5, 150, 105, 0.4);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-actions {
  display: flex;
  gap: 4px;
  align-items: center;
}

.title-actions .el-button {
  min-height: auto;
  padding: 4px 6px;
}

.title-actions .el-button i {
  font-size: 12px;
}

.emotion-wave {
  height: 12px;
  background: linear-gradient(45deg, #7ED321, #4A90E2, #9013FE);
  background-size: 300% 300%;
  border-radius: 6px;
  animation: emotionWave 3s ease-in-out infinite;
  margin-bottom: 16px;
}

@keyframes emotionWave {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.emotion-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  text-align: center;
}

.emotion-value {
  font-size: 24px;
  font-weight: bold;
  color: #4A90E2;
}

.emotion-good {
  color: #52c41a;
}

.emotion-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

/* 温暖治愈的情绪花园样式 */
.emotion-garden {
  background: linear-gradient(135deg, #fef9e7 0%, #fcf4e6 50%, #f6f0e8 100%);
  border-radius: 20px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(252, 244, 230, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  min-height: 300px;
}

.emotion-garden::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 223, 186, 0.1) 0%, transparent 70%);
  animation: gentle-glow 6s ease-in-out infinite alternate;
}

@keyframes gentle-glow {
  0% { opacity: 0.3; transform: scale(1); }
  100% { opacity: 0.6; transform: scale(1.05); }
}

.garden-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
  z-index: 2;
}

.garden-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #8b4513;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
}

.garden-icon {
  color: #90ee90;
  font-size: 18px;
  animation: sway 3s ease-in-out infinite;
}

@keyframes sway {
  0%, 100% { transform: rotate(-3deg); }
  50% { transform: rotate(3deg); }
}

.weather-icon {
  font-size: 24px;
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

/* 情绪花朵 */
.emotion-flower {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 16px;
}

.flower-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.flower-center.flower-positive {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
  color: #fff;
}

.flower-center.flower-negative {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #fff;
}

.emotion-name {
  font-size: 10px;
  font-weight: 600;
  line-height: 1;
  margin-bottom: 2px;
}

.emotion-score {
  font-size: 12px;
  font-weight: 700;
  opacity: 0.9;
}

.flower-petals {
  position: relative;
  width: 100%;
  height: 100%;
}

.petal {
  position: absolute;
  width: 30px;
  height: 20px;
  border-radius: 50% 50% 50% 0;
  transform-origin: bottom right;
}

.petal-1 { top: 10px; left: 45px; transform: rotate(0deg); }
.petal-2 { top: 20px; right: 10px; transform: rotate(60deg); }
.petal-3 { bottom: 20px; right: 20px; transform: rotate(120deg); }
.petal-4 { bottom: 10px; left: 45px; transform: rotate(180deg); }
.petal-5 { bottom: 20px; left: 20px; transform: rotate(240deg); }
.petal-6 { top: 20px; left: 10px; transform: rotate(300deg); }

.petal-bloom {
  background: linear-gradient(135deg, #ffeaa7, #fd79a8);
  animation: bloom 2s ease-out infinite alternate;
}

.petal-growing {
  background: linear-gradient(135deg, #a8e6cf, #7fdbda);
  animation: grow 2.5s ease-out infinite alternate;
}

.petal-budding {
  background: linear-gradient(135deg, #ffd3a5, #fd9853);
  animation: bud 3s ease-out infinite alternate;
}

.petal-wilting {
  background: linear-gradient(135deg, #d1c4e9, #b39ddb);
  animation: wilt 4s ease-out infinite alternate;
}

@keyframes bloom {
  0% { transform: scale(1) rotate(var(--rotation)); }
  100% { transform: scale(1.1) rotate(calc(var(--rotation) + 5deg)); }
}

@keyframes grow {
  0% { transform: scale(0.95) rotate(var(--rotation)); }
  100% { transform: scale(1.05) rotate(calc(var(--rotation) + 3deg)); }
}

@keyframes bud {
  0% { transform: scale(0.9) rotate(var(--rotation)); }
  100% { transform: scale(1) rotate(calc(var(--rotation) + 2deg)); }
}

@keyframes wilt {
  0% { transform: scale(0.85) rotate(var(--rotation)); opacity: 0.8; }
  100% { transform: scale(0.95) rotate(calc(var(--rotation) - 2deg)); opacity: 0.6; }
}

/* 温暖提示 */
.warm-tips {
  text-align: center;
  margin-bottom: 16px;
}

.emotion-status-text {
  margin-bottom: 12px;
}

.status-label {
  font-size: 14px;
  color: #8b7355;
  margin-right: 8px;
}

.status-emotion {
  font-size: 16px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 16px;
  display: inline-block;
}

.status-emotion.status-positive {
  background: linear-gradient(135deg, #ff9a9e, #fecfef);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 154, 158, 0.3);
}

.status-emotion.status-negative {
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
  color: white;
  box-shadow: 0 4px 12px rgba(252, 182, 159, 0.3);
}

.emotion-intensity {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.intensity-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e0e0e0;
  transition: all 0.3s ease;
}

.dot.active {
  background: linear-gradient(135deg, #ff9a9e, #fecfef);
  transform: scale(1.2);
  box-shadow: 0 2px 8px rgba(255, 154, 158, 0.4);
}

.intensity-text {
  font-size: 12px;
  color: #8b7355;
  font-weight: 500;
}

/* 情绪关键词云 */
.emotion-cloud {
  margin-bottom: 16px;
}

.cloud-title {
  font-size: 14px;
  color: #8b7355;
  margin-bottom: 12px;
  text-align: center;
  font-weight: 500;
}

.keywords-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.keyword-bubble {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  color: #8b7355;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  animation: bubble-float 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes bubble-float {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 温暖建议卡片 */
.warm-suggestion {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.8));
  border-radius: 16px;
  padding: 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.suggestion-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.suggestion-content {
  flex: 1;
}

.suggestion-title {
  font-size: 14px;
  font-weight: 600;
  color: #8b7355;
  margin-bottom: 6px;
}

.suggestion-text {
  font-size: 13px;
  color: #6b5b47;
  line-height: 1.5;
}

/* 治愈行动清单 */
.healing-actions {
  margin-bottom: 16px;
}

.actions-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #8b7355;
  margin-bottom: 16px;
}

.healing-heart {
  color: #ff9a9e;
  animation: heartbeat 2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-item {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  border-radius: 12px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  animation: slide-in 0.5s ease-out forwards;
  opacity: 0;
  transform: translateX(-20px);
}

@keyframes slide-in {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.action-icon {
  font-size: 14px;
  color: #ffd700;
  flex-shrink: 0;
}

.action-text {
  font-size: 12px;
  color: #6b5b47;
  line-height: 1.4;
  flex: 1;
}

/* 风险提示 */
.risk-notice {
  background: linear-gradient(135deg, #fff9e6, #ffeaa7);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border: 1px solid rgba(255, 234, 167, 0.6);
  box-shadow: 0 6px 20px rgba(255, 234, 167, 0.3);
}

.notice-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #d4840f;
  margin-bottom: 6px;
}

.notice-text {
  font-size: 13px;
  color: #b8740c;
  line-height: 1.5;
}

/* 会话历史样式 */
.session-history {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  min-height: 250px;
  display: flex;
  flex-direction: column;
}

.session-history .section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-shrink: 0;
}

.session-list {
  overflow-y: auto;
  max-height: 200px;
  scrollbar-width: thin;
  scrollbar-color: rgba(64, 150, 255, 0.3) transparent;
}

.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-track {
  background: transparent;
}

.session-list::-webkit-scrollbar-thumb {
  background: rgba(64, 150, 255, 0.3);
  border-radius: 3px;
}

.session-list::-webkit-scrollbar-thumb:hover {
  background: rgba(64, 150, 255, 0.5);
}

.session-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.session-item:hover {
  background: #f8f9ff;
  border-color: #e6f0ff;
}

.session-item.active {
  background: #e6f0ff;
  border-color: #4096ff;
}


.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.session-time {
  font-size: 12px;
  color: #999;
}

.session-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}


.session-preview {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-stats span {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.session-stats i {
  font-size: 10px;
}

.session-actions {
  flex-shrink: 0;
  display: flex;
  gap: 4px;
  align-items: center;
}

.session-actions .el-button {
  padding: 4px 6px;
  min-height: auto;
}

.session-actions .el-button i {
  font-size: 12px;
}

.empty-sessions {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-sessions i {
  font-size: 32px;
  margin-bottom: 12px;
  color: #ddd;
}

.empty-sessions p {
  margin: 0;
  font-size: 14px;
}

.load-more {
  text-align: center;
  padding: 12px;
}


.emergency-help {
  background: linear-gradient(135deg, rgba(255, 245, 245, 0.9) 0%, rgba(255, 237, 237, 0.95) 100%);
  border: 1px solid rgba(255, 77, 79, 0.2);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(255, 77, 79, 0.1);
}

.emergency-title {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 12px;
}

.emergency-desc {
  color: #ff7875;
  font-size: 13px;
  margin: 0 0 16px;
  font-weight: 500;
}

.emergency-btn {
  width: 100%;
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%) !important;
  border: none !important;
  border-radius: 16px !important;
  box-shadow: 0 6px 20px rgba(255, 77, 79, 0.25);
  transition: all 0.3s ease;
}

.emergency-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 77, 79, 0.35);
}

/* 主聊天区域样式 */
.chat-main {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 252, 250, 0.98) 100%);
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(251, 146, 60, 0.08), 0 4px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(251, 146, 60, 0.1);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 40px);
}

.chat-header {
  background: linear-gradient(135deg, #fb923c 0%, #f59e0b 100%);
  color: white;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  flex-shrink: 0;
}

.chat-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  pointer-events: none;
}

.header-left {
  display: flex;
  align-items: center;
}

.chat-avatar {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.chat-avatar i {
  font-size: 20px;
}

.chat-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  position: relative;
  z-index: 1;
  cursor: pointer;
  transition: color 0.2s ease;
}

.chat-title:hover {
  color: #4096ff;
}

.chat-title-container {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
}

.header-title-input {
  width: 250px;
}

.header-title-input .el-input__wrapper {
  box-shadow: 0 0 0 1px #4096ff inset;
  border-radius: 6px;
}

.edit-title-btn {
  opacity: 0;
  transition: opacity 0.2s ease;
  padding: 4px 6px;
  min-height: auto;
}

.chat-title-container:hover .edit-title-btn {
  opacity: 1;
}

.edit-title-btn i {
  font-size: 12px;
}

.chat-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 4px 0 0;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.02) 0%, rgba(255, 252, 248, 0.05) 100%);
  min-height: 0;
  max-height: calc(100vh - 200px);
  scrollbar-width: thin;
  scrollbar-color: rgba(251, 146, 60, 0.3) transparent;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(251, 146, 60, 0.3);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(251, 146, 60, 0.5);
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  flex-shrink: 0;
}

.ai-message .message-avatar {
  background: linear-gradient(135deg, #fb923c, #f59e0b);
  box-shadow: 0 4px 12px rgba(251, 146, 60, 0.3);
}

.user-message .message-avatar {
  background: linear-gradient(135deg, #6b7280, #4b5563);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

.message-content {
  max-width: 70%;
}

.user-message .message-content {
  text-align: right;
}

.message-bubble {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 252, 248, 0.95) 100%);
  border-radius: 16px;
  padding: 12px 16px;
  position: relative;
  animation: fadeInUp 0.4s ease-out;
  border: 1px solid rgba(251, 146, 60, 0.1);
  box-shadow: 0 4px 16px rgba(251, 146, 60, 0.05);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message .message-bubble {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: white;
  border: 1px solid rgba(107, 114, 128, 0.2);
  box-shadow: 0 4px 16px rgba(107, 114, 128, 0.15);
}

.message-bubble p {
  margin: 0;
  line-height: 1.5;
}

.typing {
  background: #f0f0f0;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-dot {
  width: 8px;
  height: 8px;
  background: #ccc;
  border-radius: 50%;
  animation: typing 1.5s ease-in-out infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  40% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.emotion-tag {
  background: rgba(255, 255, 255, 0.9);
  color: #4A90E2;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  margin-top: 8px;
  display: inline-block;
}

.user-message .emotion-tag {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}


@keyframes pulseWarning {
  0% { 
    background: linear-gradient(135deg, #FEF2F2 0%, #FECACA 100%);
    transform: scale(1);
  }
  50% { 
    background: linear-gradient(135deg, #FECACA 0%, #FCA5A5 100%);
    transform: scale(1.02);
  }
  100% { 
    background: linear-gradient(135deg, #FEF2F2 0%, #FECACA 100%);
    transform: scale(1);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-3px); }
  75% { transform: translateX(3px); }
}

/* 错误消息样式 */
.error-message {
  background: linear-gradient(135deg, #FEF2F2 0%, #FECACA 100%);
  border: 1px solid #F87171;
  border-radius: 12px;
  padding: 12px 16px;
  color: #991B1B;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-message i {
  color: #DC2626;
  font-size: 14px;
}

.message-bubble.error {
  border-color: #F87171;
  box-shadow: 0 4px 16px rgba(248, 113, 113, 0.15);
}

/* 风险警告消息样式 */
.risk-warning-message {
  background: linear-gradient(135deg, #FEF2F2 0%, #FECACA 100%);
  border: 2px solid #F87171;
  border-radius: 1rem;
  padding: 1rem;
  margin: 0.5rem 0;
  box-shadow: 0 4px 12px rgba(248, 113, 113, 0.2);
  animation: pulseWarning 2s ease-in-out;
}

.risk-warning-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: #991B1B;
}

.risk-icon {
  color: #DC2626;
  font-size: 1.25rem;
  animation: shake 1s ease-in-out;
}

.risk-title {
  font-size: 1rem;
  font-weight: 700;
}

.risk-warning-content {
  color: #7F1D1D;
  line-height: 1.6;
  font-size: 0.875rem;
  white-space: pre-line;
}

.chat-input {
  border-top: 1px solid rgba(251, 146, 60, 0.1);
  padding: 20px 24px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.5) 0%, rgba(255, 252, 248, 0.7) 100%);
  backdrop-filter: blur(10px);
  flex-shrink: 0;
}

.input-container {
  flex: 1;
}

.message-input {
  margin-bottom: 8px;
}

.input-actions {
  padding: 8px;
  display: flex;
  gap: 4px;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #78716c;
  font-weight: 500;
}

.send-btn {
  height: 60px;
  width: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, #fb923c 0%, #f59e0b 100%) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(251, 146, 60, 0.25);
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(251, 146, 60, 0.35);
}

/* 对话框样式 */
.end-session-content p {
  margin: 0 0 20px;
  text-align: center;
  color: #666;
}

.emergency-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.emergency-item h4 {
  color: #333;
  margin: 0 0 8px;
  font-size: 16px;
}

.hotline-number {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
  margin: 0 0 4px;
}

.hotline-desc {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.emergency-warning {
  color: #ff4d4f;
  font-weight: 500;
  margin: 0;
}


/* 响应式设计 */
@media (max-width: 1024px) {
  .consultation-layout {
    grid-template-columns: 280px 1fr;
  }
}

@media (max-width: 768px) {
  .consultation-page {
    min-height: 100vh;
    padding: 10px 0;
  }
  
  .consultation-layout {
    grid-template-columns: 1fr;
    min-height: calc(100vh - 20px);
  }
  
  .sidebar {
    order: 2;
    flex-direction: row;
    overflow-x: auto;
    gap: 16px;
    padding-bottom: 20px;
  }
  
  .ai-assistant-info,
  .emotion-status,
  .emotion-garden,
  .session-history,
  .emergency-help {
    flex-shrink: 0;
    min-width: 280px;
  }
  
  .session-history {
    height: 300px;
  }
  
  .chat-main {
    order: 1;
    height: 70vh;
  }
  
  .chat-messages {
    max-height: calc(70vh - 180px);
  }
}
</style>

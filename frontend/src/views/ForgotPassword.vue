<template>
  <div class="cyber-forgot-password-wrapper">
    <!-- 动态背景网格 -->
    <div class="cyber-grid-background"></div>
    
    <!-- 粒子效果容器 -->
    <div class="cyber-particles-container" ref="particlesContainer"></div>
    
    <div class="cyber-forgot-password-container">
      <h2 class="cyber-title">星际密码重置</h2>
      <p class="cyber-subtitle">身份验证与密码恢复系统</p>
    
    <!-- 步骤1：输入邮箱 -->
    <div v-if="currentStep === 1" class="cyber-step-content cyber-step-active">
      <div class="cyber-form-group">
        <label for="email" class="cyber-label">邮箱地址</label>
        <div class="cyber-input-wrapper">
          <input
            type="email"
            id="email"
            v-model="email"
            placeholder="请输入注册邮箱"
            :disabled="isSendingCode"
            class="cyber-input"
            @focus="onInputFocus"
            @blur="onInputBlur"
          />
          <div class="cyber-input-glow"></div>
        </div>
        <div v-if="isEmailValid && email" class="cyber-validation-hint success">✓ 有效邮箱</div>
      </div>
      <div class="cyber-error-message" v-if="errorMessage">{{ errorMessage }}</div>
      <button 
        class="cyber-btn-primary" 
        @click="sendVerificationCode"
        :disabled="isSendingCode || !isEmailValid"
        :class="{ 'cyber-btn-pulse': isSendingCode }"
      >
        <span v-if="!isSendingCode">{{ isSendingCode ? '发送中...' : '发送验证码' }}</span>
        <div v-else class="cyber-loading-spinner"></div>
      </button>
      <p v-if="countdown > 0" class="cyber-countdown">
        验证码已发送，请查收 <span class="cyber-countdown-number">{{ countdown }}</span> 秒后可重新发送
      </p>
    </div>
    
    <!-- 步骤2：验证验证码 -->
    <div v-if="currentStep === 2" class="cyber-step-content cyber-step-active">
      <div class="cyber-form-group">
        <label for="verificationCode" class="cyber-label">验证码</label>
        <div class="cyber-code-input-group">
          <div class="cyber-input-wrapper">
            <input
              type="text"
              id="verificationCode"
              v-model="verificationCode"
              placeholder="请输入验证码"
              maxlength="6"
              :disabled="isVerifyingCode"
              class="cyber-input"
              @focus="onInputFocus"
              @blur="onInputBlur"
            />
            <div class="cyber-input-glow"></div>
          </div>
          <button 
            class="cyber-btn-secondary" 
            @click="resendVerificationCode"
            :disabled="isSendingCode || countdown > 0"
          >
            {{ countdown > 0 ? `${countdown}秒后重发` : '重新发送' }}
          </button>
        </div>
      </div>
      <div class="cyber-error-message" v-if="errorMessage">{{ errorMessage }}</div>
      <button 
        class="cyber-btn-primary" 
        @click="verifyCode"
        :disabled="isVerifyingCode || !verificationCode"
        :class="{ 'cyber-btn-pulse': isVerifyingCode }"
      >
        <span v-if="!isVerifyingCode">{{ isVerifyingCode ? '验证中...' : '验证' }}</span>
        <div v-else class="cyber-loading-spinner"></div>
      </button>
      <button class="cyber-btn-link" @click="goBack">返回</button>
    </div>
    
    <!-- 步骤3：重置密码 -->
    <div v-if="currentStep === 3" class="cyber-step-content cyber-step-active">
      <div class="cyber-form-group">
        <label for="newPassword" class="cyber-label">新密码</label>
        <div class="cyber-input-wrapper">
          <input
            type="password"
            id="newPassword"
            v-model="newPassword"
            placeholder="请输入新密码"
            class="cyber-input"
            @focus="onInputFocus"
            @blur="onInputBlur"
          />
          <div class="cyber-input-glow"></div>
        </div>
        <div v-if="newPassword" class="cyber-password-strength">
          <div class="cyber-strength-label">密码强度:</div>
          <div class="cyber-strength-bars">
            <div 
              class="cyber-strength-bar"
              :class="['level-1', { active: passwordStrength >= 1 }]"
            ></div>
            <div 
              class="cyber-strength-bar"
              :class="['level-2', { active: passwordStrength >= 2 }]"
            ></div>
            <div 
              class="cyber-strength-bar"
              :class="['level-3', { active: passwordStrength >= 3 }]"
            ></div>
            <div 
              class="cyber-strength-bar"
              :class="['level-4', { active: passwordStrength >= 4 }]"
            ></div>
          </div>
          <div class="cyber-strength-text">{{ passwordStrengthText }}</div>
        </div>
      </div>
      <div class="cyber-form-group">
        <label for="confirmPassword" class="cyber-label">确认新密码</label>
        <div class="cyber-input-wrapper">
          <input
            type="password"
            id="confirmPassword"
            v-model="confirmPassword"
            placeholder="请再次输入新密码"
            class="cyber-input"
            @focus="onInputFocus"
            @blur="onInputBlur"
          />
          <div class="cyber-input-glow"></div>
        </div>
        <div v-if="confirmPassword" class="cyber-validation-hint" :class="{ success: newPassword === confirmPassword, error: newPassword !== confirmPassword }">
          {{ newPassword === confirmPassword ? '✓ 密码一致' : '✗ 密码不一致' }}
        </div>
      </div>
      <div class="cyber-error-message" v-if="errorMessage">{{ errorMessage }}</div>
      <button 
        class="cyber-btn-primary" 
        @click="resetPassword"
        :disabled="isResettingPassword || !isPasswordValid"
        :class="{ 'cyber-btn-pulse': isResettingPassword }"
      >
        <span v-if="!isResettingPassword">{{ isResettingPassword ? '重置中...' : '重置密码' }}</span>
        <div v-else class="cyber-loading-spinner"></div>
      </button>
      <button class="cyber-btn-link" @click="goBack">返回</button>
    </div>
    
    <!-- 成功提示 - 增加更明确的显示逻辑 -->
    <div v-if="isSuccess" class="cyber-success-message cyber-step-active" style="display: block; position: relative; z-index: 100;">
      <div class="cyber-success-icon">✓</div>
      <h3>密码重置成功！</h3>
      <p>您的密码已成功更新，请使用新密码登录。</p>
      <button class="cyber-btn-primary" @click="goToLogin">前往登录</button>
    </div>
    
    <!-- 步骤指示器 -->
    <div class="cyber-step-indicator">
      <div 
        class="cyber-step-dot" 
        :class="{ active: currentStep >= 1 }"
      >
        <span>1</span>
      </div>
      <div class="cyber-step-line" :class="{ active: currentStep >= 2 }"></div>
      <div 
        class="cyber-step-dot" 
        :class="{ active: currentStep >= 2 }"
      >
        <span>2</span>
      </div>
      <div class="cyber-step-line" :class="{ active: currentStep >= 3 }"></div>
      <div 
        class="cyber-step-dot" 
        :class="{ active: currentStep >= 3 || isSuccess }"
      >
        <span>3</span>
      </div>
    </div>
  </div>
</div>
</template>

<script>
import axios from '../axios'
import { ref, computed, onUnmounted, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'ForgotPassword',
  setup() {
    const router = useRouter()
    
    // 表单数据
    const email = ref('')
    const verificationCode = ref('')
    const newPassword = ref('')
    const confirmPassword = ref('')
    
    // 状态管理
    const currentStep = ref(1)
    const isSendingCode = ref(false)
    const isVerifyingCode = ref(false)
    const isResettingPassword = ref(false)
    const isSuccess = ref(false)
    const errorMessage = ref('')
    const countdown = ref(0)
    let countdownTimer = null
    
    // 邮箱验证
    const isEmailValid = computed(() => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email.value)
    })
    
    // 密码验证
    const isPasswordValid = computed(() => {
      return newPassword.value.length >= 6 && 
             newPassword.value === confirmPassword.value
    })
    
    // 密码强度检测
    const passwordStrength = computed(() => {
      let strength = 0
      const password = newPassword.value
      
      // 长度检查
      if (password.length >= 8) strength++
      
      // 包含数字
      if (password.match(/\d/)) strength++
      
      // 包含小写字母
      if (password.match(/[a-z]/)) strength++
      
      // 包含大写字母或特殊字符
      if (password.match(/[A-Z]/) || password.match(/[^a-zA-Z0-9]/)) strength++
      
      return strength
    })
    
    // 密码强度文本
    const passwordStrengthText = computed(() => {
      const strength = passwordStrength.value
      switch (strength) {
        case 0: return '未输入'
        case 1: return '弱'
        case 2: return '中'
        case 3: return '强'
        case 4: return '非常强'
        default: return ''
      }
    })
    
    // 发送验证码
    const sendVerificationCode = async () => {
      errorMessage.value = ''
      if (!isEmailValid.value) {
        errorMessage.value = '请输入有效的邮箱地址'
        return
      }
      
      try {
        isSendingCode.value = true
        await axios.post('/api/users/forgot-password/send-code', { email: email.value })
        
        // 开始倒计时
        startCountdown()
        
        // 跳转到验证码验证步骤
        currentStep.value = 2
      } catch (error) {
        errorMessage.value = error.response?.data?.message || '发送验证码失败，请稍后重试'
      } finally {
        isSendingCode.value = false
      }
    }
    
    // 重新发送验证码
    const resendVerificationCode = async () => {
      errorMessage.value = ''
      try {
        isSendingCode.value = true
        await axios.post('/api/users/forgot-password/send-code', { email: email.value })
        startCountdown()
      } catch (error) {
        errorMessage.value = error.response?.data?.message || '发送验证码失败，请稍后重试'
      } finally {
        isSendingCode.value = false
      }
    }
    
    // 验证验证码
    const verifyCode = async () => {
      errorMessage.value = ''
      if (!verificationCode.value || verificationCode.value.length !== 6) {
        errorMessage.value = '请输入6位验证码'
        return
      }
      
      try {
        isVerifyingCode.value = true
        const response = await axios.post('/api/users/forgot-password/verify-code', {
          email: email.value,
          code: verificationCode.value
        })
        
        if (response.data.valid) {
          // 验证码正确，跳转到重置密码步骤
          currentStep.value = 3
        } else {
          errorMessage.value = '验证码错误或已过期'
        }
      } catch (error) {
        errorMessage.value = error.response?.data?.message || '验证失败，请稍后重试'
      } finally {
        isVerifyingCode.value = false
      }
    }
    
    // 重置密码
    const resetPassword = async () => {
      errorMessage.value = ''
      
      if (newPassword.value.length < 6) {
        errorMessage.value = '密码长度至少为6位'
        return
      }
      
      if (newPassword.value !== confirmPassword.value) {
        errorMessage.value = '两次输入的密码不一致'
        return
      }
      
      try {
        isResettingPassword.value = true
        await axios.post('/api/users/forgot-password/reset', {
          email: email.value,
          code: verificationCode.value,
          newPassword: newPassword.value,
          confirmPassword: confirmPassword.value
        })
        
        // 重置成功 - 增加调试日志并确保状态正确设置
        console.log('密码重置成功，显示成功提示')
        isSuccess.value = true
        // 确保步骤指示器也能正确显示
        currentStep.value = 4
      } catch (error) {
        errorMessage.value = error.response?.data?.message || '重置密码失败，请稍后重试'
        console.error('重置密码失败:', error)
      } finally {
        isResettingPassword.value = false
      }
    }
    
    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 60
      
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
        }
      }, 1000)
    }
    
    // 返回上一步
    const goBack = () => {
      if (currentStep.value > 1) {
        currentStep.value--
      } else {
        router.push('/login')
      }
    }
    
    // 前往登录页面
    const goToLogin = () => {
      router.push('/login')
    }
    
    // 输入框焦点效果
    const onInputFocus = (event) => {
      const wrapper = event.target.closest('.cyber-input-wrapper')
      if (wrapper) {
        wrapper.classList.add('focused')
      }
    }
    
    const onInputBlur = (event) => {
      const wrapper = event.target.closest('.cyber-input-wrapper')
      if (wrapper) {
        wrapper.classList.remove('focused')
      }
    }
    
    // 创建粒子效果
    const particlesContainer = ref(null)
    let particles = []
    
    const createParticles = () => {
      if (!particlesContainer.value) return
      
      const container = particlesContainer.value
      container.innerHTML = ''
      particles = []
      
      const particleCount = 20
      
      for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div')
        particle.className = 'cyber-particle'
        
        // 随机位置和大小
        const size = Math.random() * 3 + 1
        particle.style.width = `${size}px`
        particle.style.height = `${size}px`
        particle.style.left = `${Math.random() * 100}%`
        particle.style.top = `${Math.random() * 100}%`
        particle.style.opacity = Math.random() * 0.5 + 0.2
        particle.style.animationDuration = `${Math.random() * 20 + 10}s`
        particle.style.animationDelay = `${Math.random() * 5}s`
        
        container.appendChild(particle)
        particles.push(particle)
      }
    }
    
    // 组件挂载
    onMounted(() => {
      createParticles()
    })
    
    // 清理定时器和粒子效果
    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
      particles.forEach(particle => particle.remove())
    })
    
    return {
      email,
      verificationCode,
      newPassword,
      confirmPassword,
      currentStep,
      isSendingCode,
      isVerifyingCode,
      isResettingPassword,
      isSuccess,
      errorMessage,
      countdown,
      isEmailValid,
      isPasswordValid,
      passwordStrength,
      passwordStrengthText,
      particlesContainer,
      sendVerificationCode,
      resendVerificationCode,
      verifyCode,
      resetPassword,
      goBack,
      goToLogin,
      onInputFocus,
      onInputBlur
    }
  }
}
</script>

<style scoped>
/* 玄幻科技背景和容器 */
.cyber-forgot-password-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--main-bg) 0%, var(--bg-secondary) 50%, var(--main-bg) 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Arial', sans-serif;
}

/* 动态网格背景 */
.cyber-grid-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(var(--grid-color) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
  background-size: 30px 30px;
  animation: gridMove 20s linear infinite;
  z-index: 0;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 60px 60px;
  }
}

/* 粒子效果容器 */
.cyber-particles-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  overflow: hidden;
}

.cyber-particle {
  position: absolute;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight));
  border-radius: 50%;
  filter: blur(1px);
  animation: float linear infinite;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(10px, 10px) rotate(90deg);
  }
  50% {
    transform: translate(20px, 0) rotate(180deg);
  }
  75% {
    transform: translate(10px, -10px) rotate(270deg);
  }
  100% {
    transform: translate(0, 0) rotate(360deg);
  }
}

/* 主容器 */
.cyber-forgot-password-container {
  max-width: 450px;
  width: 100%;
  padding: 40px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  box-shadow: 
    0 0 20px var(--highlight-glow),
    0 0 40px var(--highlight-glow);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  position: relative;
  z-index: 1;
  overflow: hidden;
}

/* 容器装饰边框 */
.cyber-forgot-password-container::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight), var(--highlight));
  z-index: -1;
  border-radius: 14px;
  animation: glowBorder 3s linear infinite;
  opacity: 0.7;
}

@keyframes glowBorder {
  0% {
    background-position: 0 0;
  }
  50% {
    background-position: 300% 0;
  }
  100% {
    background-position: 0 0;
  }
}

/* 标题样式 */
.cyber-title {
  text-align: center;
  color: var(--highlight);
  margin-bottom: 10px;
  font-size: 28px;
  text-shadow: 0 0 10px var(--highlight-glow);
  letter-spacing: 1px;
}

.cyber-subtitle {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 30px;
  font-size: 14px;
  opacity: 0.8;
}

/* 步骤内容 */
.cyber-step-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s ease;
}

.cyber-step-active {
  opacity: 1;
  transform: translateY(0);
}

/* 表单组 */
.cyber-form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cyber-label {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 输入框包装器 */
.cyber-input-wrapper {
  position: relative;
  transition: all 0.3s ease;
}

.cyber-input-wrapper.focused {
  transform: translateY(-2px);
}

/* 输入框样式 */
.cyber-input {
  width: 100%;
  padding: 14px 16px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 16px;
  color: var(--text-primary);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.cyber-input:focus {
  outline: none;
  border-color: var(--highlight);
  box-shadow: 0 0 10px var(--highlight-glow);
}

.cyber-input::placeholder {
  color: var(--text-secondary);
  opacity: 0.5;
}

.cyber-input:disabled {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  opacity: 0.5;
  cursor: not-allowed;
}

/* 输入框发光效果 */
.cyber-input-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight));
  border-radius: 6px;
  filter: blur(2px);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 0;
}

.cyber-input-wrapper.focused .cyber-input-glow {
  opacity: 0.6;
}

/* 验证码输入组 */
.cyber-code-input-group {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.cyber-code-input-group .cyber-input-wrapper {
  flex: 1;
}

/* 按钮样式 */
.cyber-btn-primary {
  padding: 14px 24px;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight));
  color: var(--text-primary);
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.cyber-btn-primary:hover:not(:disabled) {
  background: linear-gradient(45deg, var(--secondary-highlight), var(--highlight));
  transform: translateY(-2px);
  box-shadow: 0 5px 15px var(--highlight-glow);
}

.cyber-btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.cyber-btn-primary:disabled {
  background: var(--disabled-color);
  color: var(--text-secondary);
  cursor: not-allowed;
  transform: none;
}

.cyber-btn-secondary {
  padding: 12px 20px;
  background: var(--bg-tertiary);
  color: var(--highlight);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.cyber-btn-secondary:hover:not(:disabled) {
  background: var(--bg-secondary);
  border-color: var(--highlight);
  transform: translateY(-1px);
}

.cyber-btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.cyber-btn-link {
  padding: 12px;
  background-color: transparent;
  color: var(--highlight);
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.cyber-btn-link:hover {
  color: var(--text-secondary);
  text-decoration: underline;
  text-underline-offset: 4px;
}

/* 按钮脉冲效果 */
.cyber-btn-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 var(--highlight-glow);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(0, 0, 0, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 0, 0, 0);
  }
}

/* 加载动画 */
.cyber-loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-radius: 50%;
  border-top-color: var(--text-primary);
  animation: spin 1s ease-in-out infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误消息 */
.cyber-error-message {
  color: var(--danger-color);
  font-size: 14px;
  padding: 10px;
  background: var(--danger-bg);
  border: 1px solid var(--danger-border);
  border-radius: 4px;
  text-align: center;
  animation: errorShake 0.5s ease-in-out;
}

@keyframes errorShake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

/* 倒计时 */
.cyber-countdown {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: -10px;
  opacity: 0.8;
}

.cyber-countdown-number {
  color: var(--highlight);
  font-weight: bold;
  animation: countdownGlow 1s ease-in-out infinite alternate;
}

@keyframes countdownGlow {
  from { text-shadow: 0 0 5px var(--highlight-glow); }
  to { text-shadow: 0 0 15px var(--highlight-glow); }
}

/* 成功消息 */
.cyber-success-message {
  text-align: center;
  padding: 20px;
  opacity: 1;
  transform: translateY(0);
  transition: all 0.5s ease;
  background: var(--bg-tertiary);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--success-border);
  box-shadow: 0 0 30px var(--success-glow);
}

.cyber-success-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(45deg, var(--success-color), var(--success-color-light));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  font-size: 32px;
  font-weight: bold;
  margin: 0 auto 20px;
  box-shadow: 0 0 20px var(--success-glow);
  animation: successPop 0.5s ease-out;
}

@keyframes successPop {
  0% {
    transform: scale(0);
  }
  60% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.cyber-success-message h3 {
  color: var(--success-color);
  margin-bottom: 15px;
  font-size: 24px;
  text-shadow: 0 0 10px var(--success-glow);
}

.cyber-success-message p {
  color: var(--text-secondary);
  margin-bottom: 30px;
  font-size: 16px;
  opacity: 0.8;
}

/* 步骤指示器 */
.cyber-step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}

.cyber-step-dot {
  width: 30px;
  height: 30px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s ease;
  position: relative;
}

.cyber-step-dot.active {
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight));
  border-color: var(--highlight);
  color: var(--text-primary);
  box-shadow: 0 0 10px var(--highlight-glow);
}

.cyber-step-line {
  width: 40px;
  height: 2px;
  background: var(--border-color);
  transition: all 0.3s ease;
}

.cyber-step-line.active {
  background: linear-gradient(90deg, var(--highlight), var(--secondary-highlight));
  box-shadow: 0 0 5px var(--highlight-glow);
}

/* 密码强度指示器 */
.cyber-password-strength {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 5px;
}

.cyber-strength-label {
  font-size: 12px;
  color: var(--text-secondary);
  opacity: 0.8;
}

.cyber-strength-bars {
  display: flex;
  gap: 5px;
  height: 4px;
}

.cyber-strength-bar {
  flex: 1;
  background: var(--bg-tertiary);
  border-radius: 2px;
  transition: all 0.3s ease;
}

.cyber-strength-bar.active {
  animation: barFill 0.5s ease-out forwards;
}

@keyframes barFill {
  from {
    width: 0;
  }
  to {
    width: 100%;
  }
}

.cyber-strength-bar.level-1.active {
  background: var(--danger-color);
}

.cyber-strength-bar.level-2.active {
  background: var(--warning-color);
}

.cyber-strength-bar.level-3.active {
  background: var(--success-color);
}

.cyber-strength-bar.level-4.active {
  background: var(--success-color-light);
}

.cyber-strength-text {
  font-size: 12px;
  color: var(--text-secondary);
  opacity: 0.8;
}

/* 验证提示 */
.cyber-validation-hint {
  font-size: 12px;
  padding: 5px 0;
  transition: all 0.3s ease;
}

.cyber-validation-hint.success {
  color: var(--success-color);
}

.cyber-validation-hint.error {
  color: var(--danger-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cyber-forgot-password-container {
    margin: 20px;
    padding: 30px 20px;
  }
  
  .cyber-title {
    font-size: 24px;
  }
  
  .cyber-step-indicator {
    gap: 15px;
  }
  
  .cyber-step-line {
    width: 30px;
  }
}

@media (max-width: 480px) {
  .cyber-forgot-password-container {
    margin: 10px;
    padding: 25px 15px;
  }
  
  .cyber-title {
    font-size: 22px;
  }
  
  .cyber-subtitle {
    font-size: 12px;
  }
  
  .cyber-code-input-group {
    flex-direction: column;
  }
  
  .cyber-step-indicator {
    gap: 10px;
  }
  
  .cyber-step-dot {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
  
  .cyber-step-line {
    width: 20px;
  }
  
  .cyber-btn-primary,
  .cyber-btn-secondary {
    padding: 12px 16px;
    font-size: 14px;
  }
}
</style>
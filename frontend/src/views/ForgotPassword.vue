<template>
  <div class="forgot-password-container">
    <h2 class="title">忘记密码</h2>
    
    <!-- 步骤指示器 -->
    <div class="step-indicator" v-if="!isSuccess">
      <div class="step" :class="{ 'active': currentStep === 1, 'completed': currentStep > 1 }">1</div>
      <div class="step-line" :class="{ 'completed': currentStep > 1 }"></div>
      <div class="step" :class="{ 'active': currentStep === 2, 'completed': currentStep > 2 }">2</div>
      <div class="step-line" :class="{ 'completed': currentStep > 2 }"></div>
      <div class="step" :class="{ 'active': currentStep === 3, 'completed': currentStep > 3 }">3</div>
    </div>
    
    <!-- 步骤1：输入邮箱 -->
    <div v-if="currentStep === 1" class="step-content">
      <div class="form-group">
        <label for="email">邮箱地址</label>
        <input
          type="email"
          id="email"
          v-model="email"
          placeholder="请输入注册邮箱"
          :disabled="isSendingCode"
        />
      </div>
      <div class="error-message" v-if="errorMessage">{{ errorMessage }}</div>
      <button 
        class="btn-primary" 
        @click="sendVerificationCode"
        :disabled="isSendingCode || !isEmailValid"
      >
        {{ isSendingCode ? '发送中...' : '发送验证码' }}
      </button>
      <p v-if="countdown > 0" class="countdown">
        验证码已发送，请查收（{{ countdown }}秒后可重新发送）
      </p>
    </div>
    
    <!-- 步骤2：验证验证码 -->
    <div v-if="currentStep === 2" class="step-content">
      <div class="form-group">
        <label for="verificationCode">验证码</label>
        <div class="code-input-group">
          <input
            type="text"
            id="verificationCode"
            v-model="verificationCode"
            placeholder="请输入验证码"
            maxlength="6"
            :disabled="isVerifyingCode"
          />
          <button 
            class="btn-secondary" 
            @click="resendVerificationCode"
            :disabled="isSendingCode || countdown > 0"
          >
            {{ countdown > 0 ? `${countdown}秒后重发` : '重新发送' }}
          </button>
        </div>
      </div>
      <div class="error-message" v-if="errorMessage">{{ errorMessage }}</div>
      <button 
        class="btn-primary" 
        @click="verifyCode"
        :disabled="isVerifyingCode || !verificationCode"
      >
        {{ isVerifyingCode ? '验证中...' : '验证' }}
      </button>
      <button class="btn-link" @click="goBack">返回</button>
    </div>
    
    <!-- 步骤3：重置密码 -->
    <div v-if="currentStep === 3" class="step-content">
      <div class="form-group">
        <label for="newPassword">新密码</label>
        <input
          type="password"
          id="newPassword"
          v-model="newPassword"
          placeholder="请输入新密码"
        />
      </div>
      <div class="form-group">
        <label for="confirmPassword">确认新密码</label>
        <input
          type="password"
          id="confirmPassword"
          v-model="confirmPassword"
          placeholder="请再次输入新密码"
        />
      </div>
      <div class="error-message" v-if="errorMessage">{{ errorMessage }}</div>
      <button 
        class="btn-primary" 
        @click="resetPassword"
        :disabled="isResettingPassword || !isPasswordValid"
      >
        {{ isResettingPassword ? '重置中...' : '重置密码' }}
      </button>
      <button class="btn-link" @click="goBack">返回</button>
    </div>
    
    <!-- 成功提示 -->
    <div v-if="isSuccess" class="success-message">
      <h3>密码重置成功！</h3>
      <p>您的密码已成功更新，请使用新密码登录。</p>
      <button class="btn-primary" @click="goToLogin">前往登录</button>
    </div>
  </div>
</template>

<script>
import axios from '../axios'
import { ref, computed, onUnmounted } from 'vue'
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
        
        // 重置成功
        isSuccess.value = true
      } catch (error) {
        errorMessage.value = error.response?.data?.message || '重置密码失败，请稍后重试'
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
    
    // 清理定时器
    onUnmounted(() => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
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
      sendVerificationCode,
      resendVerificationCode,
      verifyCode,
      resetPassword,
      goBack,
      goToLogin
    }
  }
}
</script>

<style scoped>
/* 动画关键帧定义 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-5px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(5px);
  }
}

@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

/* 容器样式 */
.forgot-password-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 40px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.6s ease-out;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  /* 添加居中显示样式 */
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  max-width: 400px;
}

.forgot-password-container:hover {
  transform: translate(-50%, -50%) translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* 标题样式 */
.title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 600;
  animation: slideInUp 0.5s ease-out 0.2s both;
}

/* 步骤内容 */
.step-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: slideInUp 0.5s ease-out 0.3s both;
}

/* 表单组样式 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  animation: slideInUp 0.5s ease-out both;
}

.form-group:nth-child(1) {
  animation-delay: 0.4s;
}

.form-group:nth-child(2) {
  animation-delay: 0.5s;
}

.form-group:nth-child(3) {
  animation-delay: 0.6s;
}

/* 标签样式 */
label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
  transition: color 0.3s ease;
}

input:focus + label,
input:not(:placeholder-shown) + label {
  color: #4CAF50;
}

/* 输入框样式 */
input {
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #fff;
  position: relative;
  z-index: 1;
}

input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
  transform: translateY(-1px);
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
  opacity: 0.7;
}

input::placeholder {
  color: #999;
  transition: color 0.3s ease;
}

input:focus::placeholder {
  color: #ccc;
}

/* 验证码输入组 */
.code-input-group {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.code-input-group input {
  flex: 1;
}

/* 主要按钮样式 */
.btn-primary {
  padding: 12px 24px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  animation: slideInUp 0.5s ease-out 0.7s both;
}

.btn-primary:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
  transform: none;
}

/* 按钮点击波纹效果 */
.btn-primary::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn-primary:active:not(:disabled)::after {
  width: 300px;
  height: 300px;
}

/* 次要按钮样式 */
.btn-secondary {
  padding: 12px 16px;
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e0e0e0;
  border-color: #ccc;
  transform: translateY(-1px);
}

.btn-secondary:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
  color: #999;
  opacity: 0.7;
  transform: none;
}

/* 链接按钮样式 */
.btn-link {
  padding: 12px;
  background-color: transparent;
  color: #4CAF50;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-link:hover {
  color: #45a049;
  transform: translateX(3px);
  text-decoration: none;
}

/* 错误消息样式 */
.error-message {
  color: #f44336;
  font-size: 14px;
  margin-top: -10px;
  animation: shake 0.5s ease-in-out;
  padding: 8px 12px;
  background-color: #ffebee;
  border-radius: 4px;
  border-left: 4px solid #f44336;
}

/* 倒计时样式 */
.countdown {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: -10px;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
  animation: fadeIn 0.5s ease-out;
}

/* 成功消息样式 */
.success-message {
  text-align: center;
  padding: 30px 20px;
  background-color: #f1f8e9;
  border-radius: 8px;
  border: 1px solid #dcedc8;
  animation: fadeIn 0.6s ease-out, pulse 1s ease-out 0.5s;
}

.success-message h3 {
  color: #4CAF50;
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: 600;
}

.success-message p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

/* 步骤指示器样式 */
.step-indicator {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;
  gap: 10px;
}

.step {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.step.active {
  background-color: #4CAF50;
  color: white;
  transform: scale(1.1);
}

.step.completed {
  background-color: #81c784;
  color: white;
}

.step-line {
  height: 2px;
  background-color: #f0f0f0;
  flex: 1;
  align-self: center;
  transition: background-color 0.3s ease;
}

.step-line.completed {
  background-color: #4CAF50;
}
</style>
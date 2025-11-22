<template>
  <div class="forgot-password-container">
    <h2 class="title">忘记密码</h2>
    
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
.forgot-password-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 40px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

input:focus {
  outline: none;
  border-color: #4CAF50;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group input {
  flex: 1;
}

.btn-primary {
  padding: 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-primary:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 12px 16px;
  background-color: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  white-space: nowrap;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.btn-secondary:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
  color: #999;
}

.btn-link {
  padding: 12px;
  background-color: transparent;
  color: #4CAF50;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.btn-link:hover {
  color: #45a049;
  text-decoration: underline;
}

.error-message {
  color: #f44336;
  font-size: 14px;
  margin-top: -10px;
}

.countdown {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: -10px;
}

.success-message {
  text-align: center;
  padding: 20px;
}

.success-message h3 {
  color: #4CAF50;
  margin-bottom: 10px;
}

.success-message p {
  color: #666;
  margin-bottom: 20px;
}
</style>
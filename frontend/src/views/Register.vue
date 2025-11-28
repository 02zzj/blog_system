<template>
  <div class="register-container cyber-bg">
    <!-- 动态背景网格 -->
    <div class="cyber-grid"></div>
    
    <!-- 动态粒子效果 -->
    <div class="cyber-particles"></div>
    
    <div class="register-form cyber-card">
      <div class="cyber-title">
        <h2>星际注册</h2>
        <div class="cyber-subtitle">创建您的安全身份</div>
      </div>
      
      <form @submit.prevent="handleRegister" class="cyber-form">
        <div class="form-group cyber-input-group">
          <label for="nickname" class="cyber-label">昵称</label>
          <div class="input-with-feedback cyber-input-wrapper">
            <span class="cyber-input-icon">👤</span>
            <input 
              type="text" 
              id="nickname" 
              v-model="form.nickname" 
              placeholder="请输入昵称"
              required
              class="cyber-input"
              :class="{ 'input-error': nicknameError, 'input-success': nicknameAvailable === true && form.nickname }"
            />
            <div class="cyber-input-focus"></div>
            <div v-if="nicknameAvailable === true && form.nickname" class="validation-success cyber-validation-success">
              昵称可用
            </div>
            <div v-if="nicknameError" class="validation-error cyber-validation-error">
              {{ nicknameError }}
            </div>
          </div>
        </div>
        
        <div class="form-group cyber-input-group">
          <label for="email" class="cyber-label">邮箱</label>
          <div class="cyber-input-wrapper">
            <span class="cyber-input-icon">@</span>
            <input
              type="email"
              id="email"
              v-model="form.email"
              placeholder="请输入邮箱"
              required
              class="cyber-input"
            />
            <div class="cyber-input-focus"></div>
          </div>
        </div>
        
        <div class="form-group cyber-input-group">
          <label for="password" class="cyber-label">密码</label>
          <div class="password-input-container cyber-input-wrapper">
            <span class="cyber-input-icon">🔒</span>
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="form.password"
              placeholder="请输入密码（至少6位）"
              required
              minlength="6"
              class="cyber-input"
              :class="{ 'password-toggled': passwordToggleAnimating }"
              @focus="handlePasswordFocus"
              @blur="handlePasswordBlur"
            />
            <div class="cyber-input-focus"></div>
            <button 
              type="button" 
              class="toggle-password-btn cyber-toggle-btn"
              @click="togglePasswordVisibility('password')"
              aria-label="切换密码可见性"
            >
              <img 
                :src="showPassword ? '/src/assets/images/icon-eye-close.png' : '/src/assets/images/icon-eye-open.png'" 
                :alt="showPassword ? '隐藏密码' : '显示密码'"
                class="eye-icon"
              />
            </button>
            
            <!-- 密码强度指示器 -->
            <div v-if="passwordFocused && form.password.length > 0" class="password-strength-indicator">
              <div class="strength-bars">
                <div class="strength-bar" 
                     :class="['bar-1', form.password.length > 0 ? 'active' : '']"></div>
                <div class="strength-bar" 
                     :class="['bar-2', form.password.length > 4 ? 'active' : '']"></div>
                <div class="strength-bar" 
                     :class="['bar-3', form.password.length > 6 ? 'active' : '']"></div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-group cyber-input-group">
          <label for="confirmPassword" class="cyber-label">确认密码</label>
          <div class="password-input-container cyber-input-wrapper">
            <span class="cyber-input-icon">🔐</span>
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              id="confirmPassword"
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
              required
              class="cyber-input"
              :class="{ 'password-toggled': confirmPasswordToggleAnimating }"
            />
            <div class="cyber-input-focus"></div>
            <button 
              type="button" 
              class="toggle-password-btn cyber-toggle-btn"
              @click="togglePasswordVisibility('confirmPassword')"
              aria-label="切换确认密码可见性"
            >
              <img 
                :src="showConfirmPassword ? '/src/assets/images/icon-eye-close.png' : '/src/assets/images/icon-eye-open.png'" 
                :alt="showConfirmPassword ? '隐藏确认密码' : '显示确认密码'"
                class="eye-icon"
              />
            </button>
          </div>
        </div>
        
        <div class="form-group cyber-input-group">
          <label for="code" class="cyber-label">验证码</label>
          <div class="code-input-container cyber-input-wrapper">
            <span class="cyber-input-icon">🔑</span>
            <input
              type="text"
              id="code"
              v-model="form.code"
              placeholder="请输入验证码"
              required
              class="cyber-input verification-input"
            />
            <button type="button" class="send-code-btn cyber-code-btn" 
                    @click="sendCode" 
                    :disabled="codeSending || countdown > 0"
                    :class="{ 'code-sent': codeSentSuccess }">
              <span v-if="codeSending" class="code-btn-content">
                <span class="loading-spinner small"></span>
                发送中...
              </span>
              <span v-else>{{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}</span>
            </button>
            <div class="cyber-input-focus"></div>
          </div>
        </div>
        
        <div v-if="error" class="error-message cyber-error" :class="{ 'error-shake': errorShaking }">{{ error }}</div>
        <div v-if="success" class="success-message cyber-success">{{ success }}</div>
        
        <button type="submit" class="register-btn cyber-btn" :disabled="loading" :class="{ 'register-success': registerSuccess }">
          <span v-if="loading" class="btn-content">
            <span class="loading-spinner"></span>
            注册中...
          </span>
          <span v-else>注册</span>
        </button>
      </form>
      
      <div class="login-link cyber-link">
        已有账号？<router-link to="/login" class="cyber-link-text">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../axios'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const form = reactive({
      nickname: '',
      email: '',
      password: '',
      confirmPassword: '',
      code: ''
    })
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    const codeSending = ref(false)
    const countdown = ref(0)
    const showPassword = ref(false)
    const showConfirmPassword = ref(false)
    const nicknameAvailable = ref(null) // null: 未验证, true: 可用, false: 不可用
    const nicknameError = ref('')
    
    // 新增状态：用于交互动画
    const passwordFocused = ref(false)
    const passwordToggleAnimating = ref(false)
    const confirmPasswordToggleAnimating = ref(false)
    const errorShaking = ref(false)
    const registerSuccess = ref(false)
    const codeSentSuccess = ref(false)
    
    let nicknameCheckTimer = null
    let countdownTimer = null

    // 组件卸载时清除计时器
    onUnmounted(() => {
      if (nicknameCheckTimer) {
        clearTimeout(nicknameCheckTimer)
      }
      if (countdownTimer) {
        clearInterval(countdownTimer)
      }
    })

    const checkNickname = async () => {
      if (!form.nickname.trim()) {
        nicknameAvailable.value = null
        nicknameError.value = ''
        return
      }
      
      try {
        const response = await axios.get('/api/users/nickname/available', {
          params: { nickname: form.nickname }
        })
        nicknameAvailable.value = response.data.available
        nicknameError.value = nicknameAvailable.value ? '' : '该昵称已被使用'
        
        // 添加成功动画效果
        if (nicknameAvailable.value) {
          const nicknameInput = document.getElementById('nickname')
          if (nicknameInput) {
            nicknameInput.classList.add('nickname-available-animation')
            setTimeout(() => {
              nicknameInput.classList.remove('nickname-available-animation')
            }, 600)
          }
        }
      } catch (error) {
        console.error('检查昵称失败:', error)
        nicknameAvailable.value = null
        nicknameError.value = '检查昵称失败，请稍后重试'
      }
    }

    // 防抖处理，避免频繁请求
    const debouncedCheckNickname = () => {
      clearTimeout(nicknameCheckTimer)
      nicknameCheckTimer = setTimeout(checkNickname, 500)
    }

    // 监听昵称输入变化
    watch(() => form.nickname, () => {
      debouncedCheckNickname()
    })
    
    // 密码框聚焦状态管理
    const handlePasswordFocus = () => {
      passwordFocused.value = true
    }
    
    const handlePasswordBlur = () => {
      passwordFocused.value = false
    }
    
    // 切换密码可见性，添加动画效果
    const togglePasswordVisibility = (field) => {
      if (field === 'password') {
        showPassword.value = !showPassword.value
        passwordToggleAnimating.value = true
        setTimeout(() => {
          passwordToggleAnimating.value = false
        }, 300)
      } else if (field === 'confirmPassword') {
        showConfirmPassword.value = !showConfirmPassword.value
        confirmPasswordToggleAnimating.value = true
        setTimeout(() => {
          confirmPasswordToggleAnimating.value = false
        }, 300)
      }
    }

    const sendCode = async () => {
      // 简单的邮箱格式验证
      if (!form.email.includes('@')) {
        error.value = '请输入有效的邮箱地址'
        errorShaking.value = true
        setTimeout(() => {
          errorShaking.value = false
        }, 500)
        return
      }
      
      codeSending.value = true
      error.value = ''
      
      try {
        await axios.post('/api/users/send-verification', {
          email: form.email
        })
        
        // 启动60秒倒计时
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
        
        success.value = '验证码已发送，请查收邮箱'
        codeSentSuccess.value = true
        setTimeout(() => {
          codeSentSuccess.value = false
        }, 1000)
        
        console.log('验证码已发送到邮箱')
      } catch (err) {
        error.value = err.response?.data?.message || '发送验证码失败'
        errorShaking.value = true
        setTimeout(() => {
          errorShaking.value = false
        }, 500)
        console.error('发送验证码失败:', err)
      } finally {
        codeSending.value = false
      }
    }

    const handleRegister = async () => {
      // 表单验证
      if (form.password !== form.confirmPassword) {
        error.value = '两次输入的密码不一致'
        errorShaking.value = true
        setTimeout(() => {
          errorShaking.value = false
        }, 500)
        return
      }

      if (form.password.length < 6) {
        error.value = '密码长度至少为6位'
        errorShaking.value = true
        setTimeout(() => {
          errorShaking.value = false
        }, 500)
        return
      }
      
      if (!form.code) {
        error.value = '请输入验证码'
        errorShaking.value = true
        setTimeout(() => {
          errorShaking.value = false
        }, 500)
        return
      }

      loading.value = true
      error.value = ''
      success.value = ''
      
      try {
        // 进行注册，包含验证码
        const registerData = {
          nickname: form.nickname,
          email: form.email,
          password: form.password,
          code: form.code
        }
        
        const response = await axios.post('/api/users/register', registerData)
        
        success.value = '注册成功！即将跳转到登录页...'
        registerSuccess.value = true
        
        // 延迟跳转到登录页，让用户看到成功反馈
        setTimeout(() => {
          router.push('/login')
        }, 3000)
      } catch (err) {
        error.value = err.response?.data?.message || '注册失败，请稍后重试'
        errorShaking.value = true
        setTimeout(() => {
          errorShaking.value = false
        }, 500)
        console.error('注册失败:', err)
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      loading,
      error,
      success,
      codeSending,
      countdown,
      showPassword,
      showConfirmPassword,
      nicknameAvailable,
      nicknameError,
      passwordFocused,
      passwordToggleAnimating,
      confirmPasswordToggleAnimating,
      errorShaking,
      registerSuccess,
      codeSentSuccess,
      sendCode,
      togglePasswordVisibility,
      handlePasswordFocus,
      handlePasswordBlur,
      handleRegister
    }
  }
}
</script>

<style scoped>
/* 基础容器样式 */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* 玄幻科技背景 */
.cyber-bg {
  background: linear-gradient(135deg, var(--main-bg) 0%, var(--bg-secondary) 50%, var(--bg-tertiary) 100%);
}

/* 动态网格背景 */
.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(to right, var(--grid-color) 1px, transparent 1px),
    linear-gradient(to bottom, var(--grid-color) 1px, transparent 1px);
  background-size: 40px 40px;
  animation: gridMove 15s linear infinite;
  opacity: 0.5;
  z-index: 1;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 40px 40px;
  }
}

/* 动态粒子效果 */
.cyber-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  pointer-events: none;
}

/* 动画粒子效果实现 */
.cyber-particles::before,
.cyber-particles::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(circle, var(--highlight) 1px, transparent 1px),
    radial-gradient(circle, var(--secondary-highlight) 1px, transparent 1px);
  background-size: 100px 100px;
  animation: particleFloat 20s ease-in-out infinite;
}

.cyber-particles::after {
  background-size: 80px 80px;
  opacity: 0.6;
  animation-delay: -5s;
}

@keyframes particleFloat {
  0%, 100% {
    background-position: 0 0;
  }
  25% {
    background-position: 20px 20px;
  }
  50% {
    background-position: 0 40px;
  }
  75% {
    background-position: -20px 20px;
  }
}

/* 注册表单 - 玻璃态效果 */
.register-form {
  position: relative;
  z-index: 10;
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: var(--shadow);
  transform-style: preserve-3d;
  transition: transform 0.3s ease;
}

/* 卡片悬停效果 */
.register-form:hover {
  transform: translateY(-5px) perspective(1000px) rotateX(2deg);
}

/* 卡片发光边框效果 */
.register-form::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 18px;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight), var(--highlight));
  z-index: -1;
  animation: borderGlow 3s linear infinite;
  opacity: 0.7;
}

@keyframes borderGlow {
  0% {
    background-position: 0 0;
  }
  50% {
    background-position: 400% 0;
  }
  100% {
    background-position: 0 0;
  }
}

/* 标题样式 */
.cyber-title {
  text-align: center;
  margin-bottom: 30px;
  transform: translateZ(20px);
}

.cyber-title h2 {
  color: var(--text-primary);
  font-size: 28px;
  margin-bottom: 8px;
  text-shadow: 0 0 10px var(--highlight-glow);
  font-weight: 600;
  animation: titleGlow 2s ease-in-out infinite alternate;
  margin: 0 0 8px 0;
}

@keyframes titleGlow {
  0% {
    text-shadow: 0 0 10px var(--highlight-glow);
  }
  100% {
    text-shadow: 0 0 20px var(--highlight-glow), 0 0 30px var(--highlight-glow);
  }
}

.cyber-subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* 表单样式 */
.cyber-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 输入组 */
.cyber-input-group {
  position: relative;
  transform-style: preserve-3d;
  margin-bottom: 0;
}

.cyber-label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 1px;
  transform: translateZ(10px);
}

/* 输入框包装器 */
.cyber-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

/* 输入框图标 */
.cyber-input-icon {
  position: absolute;
  left: 16px;
  color: var(--highlight);
  font-size: 18px;
  z-index: 2;
  transition: all 0.3s ease;
}

/* 输入框样式 */
.cyber-input {
  width: 100%;
  padding: 14px 16px 14px 44px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 16px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  transform: translateZ(5px);
}

/* 验证码输入框 */
.cyber-input.verification-input {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  flex: 1;
}

/* 输入框错误状态 */
.cyber-input.input-error {
  border-color: var(--danger-color);
  box-shadow: 0 0 10px var(--danger-glow);
}

/* 输入框成功状态 */
.cyber-input.input-success {
  border-color: var(--success-color);
  box-shadow: 0 0 10px var(--success-glow);
}

/* 密码切换动画 */
.cyber-input.password-toggled {
  animation: passwordToggle 0.3s ease;
}

@keyframes passwordToggle {
  0%, 100% { transform: scaleX(1) translateZ(5px); }
  50% { transform: scaleX(0.95) translateZ(5px); }
}

/* 昵称可用动画 */
.cyber-input.nickname-available-animation {
  animation: nicknameAvailable 0.6s ease;
}

@keyframes nicknameAvailable {
  0%, 100% { transform: translateZ(5px); }
  50% { transform: translateZ(5px) scale(1.02); }
}

.cyber-input::placeholder {
  color: var(--text-secondary);
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.cyber-input:focus::placeholder {
  opacity: 0.4;
}

.cyber-input:focus {
  outline: none;
  border-color: var(--highlight);
  box-shadow: 0 0 15px var(--highlight-glow);
  background: var(--bg-tertiary);
  transform: translateZ(5px) scale(1.02);
}

.cyber-input:focus + .cyber-input-icon,
.cyber-input:focus + * + .cyber-input-icon {
  color: var(--text-primary);
  text-shadow: 0 0 8px var(--highlight-glow);
  transform: scale(1.1);
}

/* 输入框聚焦效果 */
.cyber-input-focus {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--highlight), var(--secondary-highlight));
  transition: width 0.3s ease;
  z-index: 3;
}

.cyber-input:focus + .cyber-input-focus,
.cyber-input:focus + * + .cyber-input-focus,
.cyber-input:focus + * + * + .cyber-input-focus {
  width: 100%;
}

/* 验证码输入组特殊处理 */
.code-input-container .cyber-input-focus {
  width: 100%;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

/* 验证提示 */
.cyber-validation-success,
.cyber-validation-error {
  position: absolute;
  bottom: -22px;
  left: 0;
  font-size: 12px;
  z-index: 10;
  animation: fadeIn 0.3s ease;
}

.cyber-validation-success {
  color: var(--success-color);
}

.cyber-validation-error {
  color: var(--danger-color);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 密码输入容器 */
.password-input-container {
  position: relative;
}

/* 验证码输入容器 */
.code-input-container {
  display: flex;
  align-items: stretch;
  position: relative;
}

/* 切换密码按钮 */
.cyber-toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 2;
  transform: translateY(-50%) translateZ(10px);
}

.cyber-toggle-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--highlight);
  transform: translateY(-50%) translateZ(10px) scale(1.1);
  box-shadow: 0 0 10px var(--highlight-glow);
}

.eye-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  filter: invert(1) hue-rotate(180deg);
  transition: all 0.3s ease;
}

/* 发送验证码按钮 */
.cyber-code-btn {
  padding: 14px 16px;
  background: linear-gradient(45deg, var(--secondary-highlight), var(--secondary-highlight-dark));
  color: var(--text-primary);
  border: none;
  border-radius: 0 8px 8px 0;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
  transition: all 0.3s ease;
  transform: translateZ(10px);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 验证码发送成功动画 */
.cyber-code-btn.code-sent {
  animation: codeSent 0.6s ease;
  background: linear-gradient(45deg, var(--success-color), var(--success-color-light));
}

@keyframes codeSent {
  0%, 100% { transform: translateZ(10px) scale(1); }
  50% { transform: translateZ(10px) scale(1.05); }
}

.cyber-code-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.cyber-code-btn:hover:not(:disabled)::before {
  left: 100%;
}

.cyber-code-btn:hover:not(:disabled) {
  transform: translateZ(10px) translateY(-2px);
  box-shadow: 0 5px 15px var(--secondary-highlight-glow);
}

.cyber-code-btn:active:not(:disabled) {
  transform: translateZ(10px) translateY(0) scale(0.98);
}

.cyber-code-btn:disabled {
  background: var(--disabled-color);
  color: var(--text-secondary);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.code-btn-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 密码强度指示器 */
.password-strength-indicator {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  z-index: 5;
  animation: fadeIn 0.3s ease;
}

.strength-bars {
  display: flex;
  gap: 4px;
  height: 4px;
}

.strength-bar {
  flex: 1;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-bar.active.bar-1 {
  background: var(--danger-color);
}

.strength-bar.active.bar-2 {
  background: var(--warning-color);
}

.strength-bar.active.bar-3 {
  background: var(--success-color);
}

/* 错误消息 */
.cyber-error {
  color: var(--danger-color);
  padding: 12px 16px;
  background: var(--danger-bg);
  border: 1px solid var(--danger-border);
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  animation: errorPulse 0.5s ease;
  transform: translateZ(5px);
}

/* 错误摇晃动画 */
.cyber-error.error-shake {
  animation: errorShake 0.5s ease;
}

@keyframes errorShake {
  0%, 100% { transform: translateX(0) translateZ(5px); }
  25% { transform: translateX(-5px) translateZ(5px); }
  75% { transform: translateX(5px) translateZ(5px); }
}

@keyframes errorPulse {
  0%, 100% {
    transform: scale(1) translateZ(5px);
  }
  50% {
    transform: scale(1.02) translateZ(5px);
  }
}

/* 成功消息 */
.cyber-success {
  color: var(--success-color);
  padding: 12px 16px;
  background: var(--success-bg);
  border: 1px solid var(--success-border);
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  animation: successGlow 1s ease-in-out infinite alternate;
  transform: translateZ(5px);
}

@keyframes successGlow {
  0% {
    box-shadow: 0 0 10px var(--success-glow);
  }
  100% {
    box-shadow: 0 0 20px var(--success-glow);
  }
}

/* 按钮样式 */
.cyber-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight));
  border: none;
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  z-index: 1;
  transform-style: preserve-3d;
  transform: translateZ(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* 注册成功动画 */
.cyber-btn.register-success {
  animation: registerSuccess 0.6s ease;
  background: linear-gradient(45deg, var(--success-color), var(--success-color-light));
}

@keyframes registerSuccess {
  0% { transform: translateZ(20px) scale(1); }
  50% { transform: translateZ(20px) scale(1.05); }
  100% { transform: translateZ(20px) scale(1); }
}

.cyber-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
  z-index: -1;
}

.cyber-btn:hover:not(:disabled)::before {
  left: 100%;
}

.cyber-btn:hover:not(:disabled) {
  transform: translateY(-2px) translateZ(20px);
  box-shadow: 0 5px 20px var(--highlight-glow);
}

.cyber-btn:active:not(:disabled) {
  transform: translateY(0) translateZ(20px) scale(0.98);
}

.cyber-btn:disabled {
  background: var(--disabled-color);
  color: var(--text-secondary);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 加载动画 */
.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid var(--text-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.small {
  width: 12px;
  height: 12px;
  border-width: 1.5px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 链接样式 */
.cyber-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-secondary);
  transform: translateZ(5px);
}

.cyber-link-text {
  color: var(--highlight);
  text-decoration: none;
  position: relative;
  padding: 2px 0;
  transition: all 0.3s ease;
  display: inline-block;
}

.cyber-link-text::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--highlight);
  transition: width 0.3s ease;
  box-shadow: 0 0 5px var(--highlight-glow);
}

.cyber-link-text:hover {
  color: var(--text-primary);
  text-shadow: 0 0 8px var(--highlight-glow);
  transform: translateY(-2px);
}

.cyber-link-text:hover::after {
  width: 100%;
}

/* 禁用Edge浏览器默认的密码切换按钮 */
input[type="password"] {
  -webkit-text-security: disc;
}

input[type="password"]::-ms-reveal,
input[type="password"]::-ms-clear {
  display: none !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-form {
    padding: 30px 20px;
    margin: 0 15px;
  }
  
  .cyber-title h2 {
    font-size: 24px;
  }
  
  .cyber-subtitle {
    font-size: 12px;
    letter-spacing: 1px;
  }
  
  .cyber-input {
    padding: 12px 14px 12px 40px;
    font-size: 14px;
  }
  
  .cyber-input-icon {
    font-size: 16px;
    left: 14px;
  }
  
  .cyber-btn {
    padding: 12px;
    font-size: 15px;
  }
  
  .cyber-code-btn {
    padding: 12px 12px;
    font-size: 12px;
  }
  
  .cyber-label {
    font-size: 13px;
  }
  
  .cyber-form {
    gap: 20px;
  }
  
  .cyber-link {
    font-size: 13px;
  }
  
  /* 适配移动设备的网格背景 */
  .cyber-grid {
    background-size: 30px 30px;
  }
}

@media (max-width: 480px) {
  .register-form {
    padding: 25px 15px;
    border-radius: 12px;
    margin: 0 10px;
  }
  
  .register-form::before {
    border-radius: 14px;
  }
  
  .cyber-title h2 {
    font-size: 22px;
  }
  
  .cyber-input {
    padding: 12px 12px 12px 38px;
  }
  
  .cyber-toggle-btn {
    padding: 4px;
    right: 8px;
  }
  
  .eye-icon {
    width: 18px;
    height: 18px;
  }
  
  .code-input-container {
    flex-direction: column;
    gap: 10px;
  }
  
  .cyber-input.verification-input {
    border-radius: 8px;
    width: 100%;
  }
  
  .cyber-code-btn {
    border-radius: 8px;
    width: 100%;
  }
}

/* 设备方向响应式 */
@media (max-height: 700px) {
  .register-container {
    min-height: 100vh;
    padding: 20px 0;
  }
  
  .register-form {
    padding: 25px;
  }
}
</style>
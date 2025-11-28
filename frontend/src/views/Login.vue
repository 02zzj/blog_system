<template>
  <div class="login-container cyber-bg">
    <!-- 动态背景网格 -->
    <div class="cyber-grid"></div>
    
    <!-- 动态粒子效果 -->
    <div class="cyber-particles"></div>
    
    <div class="login-wrapper">
      <div class="login-form cyber-card">
        <div class="cyber-title">
          <h2>星际认证</h2>
          <div class="cyber-subtitle">安全登录终端</div>
        </div>
        
        <form @submit.prevent="handleLogin" class="cyber-form">
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
                :class="{ 'input-error': !emailValid }"
              />
              <div class="cyber-input-focus"></div>
              <div v-if="!emailValid && form.email" class="validation-hint">
                请输入有效的邮箱地址
              </div>
            </div>
          </div>
          
          <div class="form-group cyber-input-group">
            <label for="password" class="cyber-label">密码</label>
            <div class="cyber-input-wrapper password-input-container">
              <span class="cyber-input-icon">🔒</span>
              <input
                :type="showPassword ? 'text' : 'password'"
                id="password"
                v-model="form.password"
                placeholder="请输入密码"
                required
                class="cyber-input"
                @focus="handlePasswordFocus"
                @blur="handlePasswordBlur"
              />
              <div class="cyber-input-focus"></div>
              <button 
                type="button" 
                class="toggle-password-btn cyber-toggle-btn"
                @click="togglePasswordVisibility"
                aria-label="切换密码可见性"
              >
                <img 
                  :src="showPassword ? '/src/assets/images/icon-eye-close.png' : '/src/assets/images/icon-eye-open.png'" 
                  :alt="showPassword ? '隐藏密码' : '显示密码'"
                  class="eye-icon"
                />
              </button>
              
              <!-- 密码强度指示器 -->
              <div v-if="passwordFocused && form.password" class="password-strength-indicator">
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
          
          <div v-if="error" class="error-message cyber-error">{{ error }}</div>
          
          <button type="submit" class="login-btn cyber-btn" :disabled="loading">
            <span class="cyber-btn-text">{{ loading ? '登录中...' : '登录' }}</span>
            <span v-if="loading" class="loading-spinner"></span>
            <span class="cyber-btn-glow"></span>
          </button>
        </form>
        
        <div class="register-link cyber-link">
          还没有账号？<router-link to="/register" class="cyber-link-text">立即注册</router-link>
        </div>
        
        <div class="forgot-password-link cyber-link">
          <router-link to="/forgot-password" class="cyber-link-text">忘记密码？</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../axios'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const form = ref({
      email: '',
      password: ''
    })
    const loading = ref(false)
    const error = ref('')
    const showPassword = ref(false)
    const emailValid = ref(true)
    const passwordFocused = ref(false)
    
    // 邮箱格式验证
    const isEmailValid = computed(() => {
      if (!form.value.email) return true
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(form.value.email)
    })
    
    // 监听邮箱输入变化，提供实时验证反馈
    watch(() => form.value.email, (newEmail) => {
      if (newEmail) {
        emailValid.value = isEmailValid.value
      } else {
        emailValid.value = true
      }
    })
    
    // 密码框聚焦状态管理
    const handlePasswordFocus = () => {
      passwordFocused.value = true
    }
    
    const handlePasswordBlur = () => {
      passwordFocused.value = false
    }
    
    // 切换密码可见性，添加微动画效果
    const togglePasswordVisibility = () => {
      // 添加视觉反馈动画
      const passwordInput = document.getElementById('password')
      if (passwordInput) {
        passwordInput.classList.add('password-toggled')
        setTimeout(() => {
          passwordInput.classList.remove('password-toggled')
        }, 300)
      }
      
      showPassword.value = !showPassword.value
    }

    // 增强的登录处理，添加输入验证和视觉反馈
    const handleLogin = async () => {
      // 表单验证
      if (!form.value.email || !form.value.password) {
        error.value = '请填写所有必填字段'
        return
      }
      
      if (!isEmailValid.value) {
        error.value = '请输入有效的邮箱地址'
        emailValid.value = false
        return
      }
      
      // 清除之前的错误
      error.value = ''
      
      // 显示加载状态
      loading.value = true
      
      try {
        // 第一步：登录获取token
        const loginResponse = await axios.post('/api/users/login', form.value)
        
        // 保存token
        const token = loginResponse.token || loginResponse.data?.token
        if (token) {
          localStorage.setItem('token', token)
          // 更新axios实例的认证头
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        } else {
          throw new Error('登录失败，未返回token')
        }
        
        // 第二步：使用token获取用户信息
        const userResponse = await axios.get('/api/users/me')
        const userInfo = userResponse.data || userResponse
        
        // 保存用户信息到localStorage
        localStorage.setItem('user', JSON.stringify(userInfo))
        console.log('User logged in successfully, saved user info:', userInfo)
        
        // 登录成功动画反馈
        const loginBtn = document.querySelector('.cyber-btn')
        if (loginBtn) {
          loginBtn.classList.add('login-success')
        }
        
        // 延迟跳转到首页，让用户看到成功反馈
        setTimeout(() => {
          router.push('/')
        }, 500)
      } catch (err) {
        // 清除可能保存的token
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        
        // 优先使用后端返回的消息，否则显示自定义消息
        const defaultMessage = '邮箱或密码错误'
        error.value = err.response?.data?.message?.includes('用户名') 
          ? defaultMessage 
          : (err.response?.data?.message || defaultMessage)
        
        // 添加错误动画效果
        const errorElement = document.querySelector('.cyber-error')
        if (errorElement) {
          errorElement.classList.add('error-shake')
          setTimeout(() => {
            errorElement.classList.remove('error-shake')
          }, 500)
        }
        
        console.error('登录失败:', err)
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      loading,
      error,
      showPassword,
      emailValid,
      passwordFocused,
      togglePasswordVisibility,
      handlePasswordFocus,
      handlePasswordBlur,
      handleLogin
    }
  }
}
</script>

<style scoped>
/* 基础容器样式 */
.login-container {
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

/* 登录表单包装器 */
.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

/* 表单卡片 - 玻璃态效果 */
.cyber-card {
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 40px;
  box-shadow: var(--shadow);
  position: relative;
  overflow: hidden;
  transform-style: preserve-3d;
  transition: transform 0.3s ease;
}

/* 卡片悬停效果 */
.cyber-card:hover {
  transform: translateY(-5px) perspective(1000px) rotateX(2deg);
}

/* 卡片发光边框效果 */
.cyber-card::before {
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
    text-shadow: 0 0 10px var(--highlight);
  }
  100% {
    text-shadow: 0 0 20px var(--highlight), 0 0 30px var(--highlight-glow);
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

/* 输入框错误状态 */
.cyber-input.input-error {
  border-color: var(--danger-color);
  box-shadow: 0 0 10px var(--danger-glow);
}

/* 密码切换动画 */
.cyber-input.password-toggled {
  animation: passwordToggle 0.3s ease;
}

@keyframes passwordToggle {
  0%, 100% { transform: scaleX(1); }
  50% { transform: scaleX(0.95); }
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
  background: var(--bg-secondary);
  transform: translateZ(5px) scale(1.02);
}

.cyber-input:focus + .cyber-input-icon {
  color: var(--text-primary);
  text-shadow: 0 0 8px var(--highlight);
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
.cyber-input:focus + * + .cyber-input-focus {
  width: 100%;
}

/* 验证提示 */
.validation-hint {
  position: absolute;
  bottom: -22px;
  left: 0;
  color: #ff6b6b;
  font-size: 12px;
  z-index: 10;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 密码输入容器 */
.password-input-container {
  position: relative;
}

.cyber-input.password-input {
  padding-right: 48px;
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
.error-shake {
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

/* 登录成功动画 */
.cyber-btn.login-success {
  animation: loginSuccess 0.6s ease;
  background: linear-gradient(45deg, var(--success-color), var(--success-color-light));
}

@keyframes loginSuccess {
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

.cyber-btn-text {
  position: relative;
  z-index: 2;
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
  box-shadow: 0 0 5px var(--highlight);
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
  .login-wrapper {
    padding: 15px;
  }
  
  .cyber-card {
    padding: 30px 20px;
    margin: 0;
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
  .cyber-card {
    padding: 25px 15px;
    border-radius: 12px;
  }
  
  .cyber-card::before {
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
}

/* 设备方向响应式 */
@media (max-height: 600px) {
  .login-container {
    min-height: 100vh;
    padding: 20px 0;
  }
  
  .cyber-card {
    padding: 25px;
  }
}

/* 动画粒子效果实现（通过CSS伪元素） */
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
</style>
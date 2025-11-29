<template>
  <div class="register-container">
    <div class="register-form">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="nickname">昵称</label>
          <div class="input-with-feedback">
            <input 
              type="text" 
              id="nickname" 
              v-model="form.nickname" 
              placeholder="请输入昵称"
              required
              :class="{ 'input-error': nicknameError, 'input-success': nicknameAvailable === true && form.nickname }"
            />
            <div v-if="nicknameAvailable === true && form.nickname" class="validation-success">
              昵称可用
            </div>
            <div v-if="nicknameError" class="validation-error">
              {{ nicknameError }}
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label for="email">邮箱</label>
          <div class="input-with-feedback">
            <input
              type="email"
              id="email"
              v-model="form.email"
              placeholder="请输入邮箱"
              required
              :class="{ 'input-error': emailValid === false, 'input-success': emailValid === true && form.email }"
            />
            <div v-if="emailValid === true && form.email" class="validation-success">
              邮箱格式正确
            </div>
            <div v-if="emailError" class="validation-error">
              {{ emailError }}
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="form.password"
              placeholder="请输入密码（至少6位）"
              required
              minlength="6"
            />
            <button 
              type="button" 
              class="toggle-password-btn"
              @click="togglePasswordVisibility('password')"
              aria-label="切换密码可见性"
            >
              <img 
                :src="showPassword ? '/src/assets/images/icon-eye-close.png' : '/src/assets/images/icon-eye-open.png'" 
                :alt="showPassword ? '隐藏密码' : '显示密码'"
                class="eye-icon"
              />
            </button>
          </div>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <div class="password-input-container">
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              id="confirmPassword"
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
              required
            />
            <button 
              type="button" 
              class="toggle-password-btn"
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
        
        <div class="form-group">
          <label for="code">验证码</label>
          <div class="code-input-container">
            <input
              type="text"
              id="code"
              v-model="form.code"
              placeholder="请输入验证码"
              required
            />
            <button type="button" class="send-code-btn" @click="sendCode" :disabled="codeSending || countdown > 0 || !emailValid">
              {{ codeSending ? '发送中...' : countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </div>
        </div>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        
        <button type="submit" class="register-btn" :disabled="loading || !emailValid || !nicknameAvailable">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      
      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, watch } from 'vue'
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
    const emailValid = ref(null) // null: 未验证, true: 有效, false: 无效
    const emailError = ref('')
    let nicknameCheckTimer = null

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
      } catch (error) {
        console.error('检查昵称失败:', error)
        nicknameAvailable.value = null
        nicknameError.value = '检查昵称失败，请稍后重试'
      }
    }

    // 邮箱格式验证
    const checkEmailFormat = () => {
      if (!form.email.trim()) {
        emailValid.value = null
        emailError.value = ''
        return
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (emailRegex.test(form.email)) {
        emailValid.value = true
        emailError.value = ''
      } else {
        emailValid.value = false
        emailError.value = '请输入有效的邮箱地址'
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
    
    // 监听邮箱输入变化
    watch(() => form.email, () => {
      checkEmailFormat()
    })
    
    const togglePasswordVisibility = (field) => {
      if (field === 'password') {
        showPassword.value = !showPassword.value
      } else if (field === 'confirmPassword') {
        showConfirmPassword.value = !showConfirmPassword.value
      }
    }

    const sendCode = async () => {
      // 验证邮箱格式
      if (!emailValid.value) {
        error.value = emailError.value || '请输入有效的邮箱地址'
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
        const timer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(timer)
          }
        }, 1000)
        
        success.value = '验证码已发送，请查收邮箱'
        console.log('验证码已发送到邮箱')
      } catch (err) {
        error.value = err.response?.data?.message || '发送验证码失败'
        console.error('发送验证码失败:', err)
      } finally {
        codeSending.value = false
      }
    }

    const handleRegister = async () => {
      // 表单验证
      if (!emailValid.value) {
        error.value = emailError.value || '请输入有效的邮箱地址'
        return
      }
      
      if (form.password !== form.confirmPassword) {
        error.value = '两次输入的密码不一致'
        return
      }

      if (form.password.length < 6) {
        error.value = '密码长度至少为6位'
        return
      }
      
      if (!form.code) {
        error.value = '请输入验证码'
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
        
        // 延迟3秒后跳转到登录页
        setTimeout(() => {
          router.push('/login')
        }, 3000)
      } catch (err) {
        error.value = err.response?.data?.message || '注册失败，请稍后重试'
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
      emailValid,
      emailError,
      sendCode,
      togglePasswordVisibility,
      handleRegister
    }
  }
}
</script>

<style scoped>
/* 全局动画定义 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideInUp {
  from { 
    transform: translateY(30px) scale(0.95); 
    opacity: 0; 
  }
  to { 
    transform: translateY(0) scale(1); 
    opacity: 1; 
  }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 600px;
  animation: fadeIn 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.register-form {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  animation: slideInUp 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transition: box-shadow 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94), transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.register-form:hover {
  box-shadow: 0 8px 25px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

.register-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: bold;
  animation: slideInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.2s both;
  position: relative;
  opacity: 0;
}

/* 移除标题下划线 */
/* .register-form h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #1890ff, #40a9ff);
  border-radius: 2px;
  animation: slideInUp 0.5s ease-out 0.4s both;
} */

.form-group {
  margin-bottom: 20px;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  opacity: 0;
  animation-fill-mode: forwards;
}

/* 为每个表单组添加不同的动画延迟，创建顺序出现效果 */
.form-group:nth-child(1) { animation-delay: 0.3s; }
.form-group:nth-child(2) { animation-delay: 0.4s; }
.form-group:nth-child(3) { animation-delay: 0.5s; }
.form-group:nth-child(4) { animation-delay: 0.6s; }
.form-group:nth-child(5) { animation-delay: 0.7s; }

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  transition: color 0.3s ease;
}

.form-group:hover label {
  color: #1890ff;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e8e8e8;
  border-radius: 6px;
  font-size: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #fff;
  position: relative;
  z-index: 1;
}

/* 禁用Edge浏览器默认的密码切换按钮 */
input[type="password"] {
  -webkit-text-security: disc;
}

/* 针对Edge和IE的特定样式 */
input[type="password"]::-ms-reveal,
input[type="password"]::-ms-clear {
  display: none !important;
}

.form-group input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
  transform: translateY(-1px);
}

.form-group input::placeholder {
  color: #bfbfbf;
  transition: color 0.3s ease;
}

.form-group input:focus::placeholder {
  color: #999;
}

/* 输入验证样式 */
.input-error {
  border-color: #ff4d4f;
  animation: shake 0.5s ease-out;
}

.input-success {
  border-color: #52c41a;
}

.input-with-feedback {
  position: relative;
}

.validation-success,
.validation-error {
  font-size: 14px;
  margin-top: 5px;
  animation: fadeIn 0.3s ease-out;
}

.validation-success {
  color: #52c41a;
}

.validation-error {
  color: #ff4d4f;
}

.password-input-container {
  position: relative;
  width: 100%;
}

.password-input-container input {
  padding-right: 45px;
}

.toggle-password-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s ease;
  z-index: 2;
}

.toggle-password-btn:hover {
  background-color: #f5f5f5;
  transform: translateY(-50%) scale(1.1);
}

.eye-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.toggle-password-btn:hover .eye-icon {
  transform: scale(1.1);
}

.code-input-container {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.code-input-container input {
  flex: 1;
}

.send-code-btn {
  padding: 0 18px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-code-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.send-code-btn:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.send-code-btn:hover:not(:disabled)::before {
  left: 100%;
}

.send-code-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 验证码按钮倒计时动画 */
.send-code-btn:disabled {
  position: relative;
  overflow: hidden;
}

.send-code-btn:disabled::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.error-message,
.success-message {
  padding: 10px 16px;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 16px;
  animation: slideInUp 0.4s ease-out;
  border-width: 2px;
  border-style: solid;
}

.error-message {
  color: #ff4d4f;
  background-color: #fff2f0;
  border-color: #ffccc7;
}

.success-message {
  color: #52c41a;
  background-color: #f6ffed;
  border-color: #b7eb8f;
}

.register-btn {
  width: 100%;
  padding: 12px 0;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.6s both;
  opacity: 0;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.register-btn:active:not(:disabled)::before {
  width: 300px;
  height: 300px;
}

.register-btn:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.register-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.7s both;
  opacity: 0;
}

.login-link a {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding: 0 4px;
}

.login-link a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #1890ff;
  transition: width 0.3s ease;
}

.login-link a:hover {
  color: #40a9ff;
}

.login-link a:hover::after {
  width: 100%;
}

/* 加载状态动画 */
.register-btn:disabled::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  animation: shimmer 1.5s infinite;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .register-container {
    min-height: 500px;
    padding: 20px;
  }
  
  .register-form {
    padding: 30px 20px;
    max-width: 100%;
  }
  
  .register-form h2 {
    font-size: 22px;
    margin-bottom: 24px;
  }
  
  .form-group {
    margin-bottom: 16px;
  }
  
  .form-group input {
    padding: 10px 14px;
    font-size: 14px;
  }
  
  .code-input-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .send-code-btn {
    min-width: auto;
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .register-btn {
    padding: 10px 0;
    font-size: 14px;
  }
  
  .login-link {
    margin-top: 20px;
    font-size: 13px;
  }
  
  .error-message,
  .success-message {
    padding: 8px 12px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .register-container {
    min-height: auto;
    padding: 10px;
  }
  
  .register-form {
    padding: 20px 16px;
  }
  
  .register-form h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }
  
  .form-group input {
    padding: 8px 12px;
    font-size: 13px;
  }
  
  .register-btn {
    padding: 8px 0;
    font-size: 13px;
  }
  
  .login-link {
    margin-top: 16px;
    font-size: 12px;
  }
}
</style>
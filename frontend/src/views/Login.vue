<template>
  <div class="login-container">
    <div class="login-form">
      <h2>登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            type="email"
            id="email"
            v-model="form.email"
            placeholder="请输入邮箱"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <div class="password-input-container">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="form.password"
              placeholder="请输入密码"
              required
            />
            <button 
              type="button" 
              class="toggle-password-btn"
              @click="togglePasswordVisibility"
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
        
        <div v-if="error" class="error-message">{{ error }}</div>
        
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
      
      <div class="forgot-password-link">
        <router-link to="/forgot-password">忘记密码？</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
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
    
    const togglePasswordVisibility = () => {
      showPassword.value = !showPassword.value
    }

    const handleLogin = async () => {
      loading.value = true
      error.value = ''
      
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
        
        // 跳转到首页
        router.push('/')
      } catch (err) {
        // 清除可能保存的token
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        
        // 优先使用后端返回的消息，否则显示自定义消息
        const defaultMessage = '邮箱或密码错误'
        error.value = err.response?.data?.message?.includes('用户名') 
          ? defaultMessage 
          : (err.response?.data?.message || defaultMessage)
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
      togglePasswordVisibility,
      handleLogin
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

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 600px;
  animation: fadeIn 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.login-form {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  animation: slideInUp 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transition: box-shadow 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94), transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.login-form:hover {
  box-shadow: 0 8px 25px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

.login-form h2 {
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
/* .login-form h2::after {
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

.error-message {
  color: #ff4d4f;
  margin-bottom: 16px;
  padding: 10px 16px;
  background-color: #fff2f0;
  border: 2px solid #ffccc7;
  border-radius: 6px;
  font-size: 14px;
  animation: slideInUp 0.4s ease-out;
}

.login-btn {
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
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.5s both;
  opacity: 0;
}

.login-btn::before {
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

.login-btn:active:not(:disabled)::before {
  width: 300px;
  height: 300px;
}

.login-btn:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.login-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 加载状态动画 */
.login-btn:disabled::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  animation: shimmer 1.5s infinite;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.4s both;
  opacity: 0;
}

.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.remember-me:hover {
  transform: translateX(2px);
}

.remember-me input[type="checkbox"] {
  width: auto;
  margin-right: 8px;
  transition: transform 0.2s ease;
}

.remember-me input[type="checkbox"]:checked {
  transform: scale(1.1);
}

.forgot-password {
  color: #1890ff;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
  padding: 2px 4px;
}

.forgot-password::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #1890ff;
  transition: width 0.3s ease;
}

.forgot-password:hover {
  color: #40a9ff;
  transform: translateX(-2px);
}

.forgot-password:hover::after {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.6s both;
  opacity: 0;
}

.register-link a {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding: 0 4px;
}

.register-link a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #1890ff;
  transition: width 0.3s ease;
}

.register-link a:hover {
  color: #40a9ff;
}

.register-link a:hover::after {
  width: 100%;
}

.forgot-password-link {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  animation: slideInUp 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.7s both;
  opacity: 0;
}

.forgot-password-link a {
  color: #1890ff;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
  padding: 2px 4px;
}

.forgot-password-link a::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #1890ff;
  transition: width 0.3s ease;
}

.forgot-password-link a:hover {
  color: #40a9ff;
}

.forgot-password-link a:hover::after {
  width: 100%;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .login-container {
    min-height: 500px;
    padding: 20px;
  }
  
  .login-form {
    padding: 30px 20px;
    max-width: 100%;
  }
  
  .login-form h2 {
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
  
  .login-btn {
    padding: 10px 0;
    font-size: 14px;
  }
  
  .register-link,
  .forgot-password-link {
    margin-top: 20px;
    font-size: 13px;
  }
  
  .error-message {
    padding: 8px 12px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .login-container {
    min-height: auto;
    padding: 10px;
  }
  
  .login-form {
    padding: 20px 16px;
  }
  
  .login-form h2 {
    font-size: 20px;
    margin-bottom: 20px;
  }
  
  .form-group input {
    padding: 8px 12px;
    font-size: 13px;
  }
  
  .login-btn {
    padding: 8px 0;
    font-size: 13px;
  }
  
  .register-link,
  .forgot-password-link {
    margin-top: 16px;
    font-size: 12px;
  }
}
</style>
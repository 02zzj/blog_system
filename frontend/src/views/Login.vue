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
        const response = await axios.post('/api/users/login', form.value)
        
        // 保存token和用户信息到localStorage
        // 根据后端API结构调整数据获取方式
        if (response.data && response.data.token) {
          localStorage.setItem('token', response.data.token)
        }
        
        // 保存用户信息
        if (response.data && response.data.data) {
          localStorage.setItem('user', JSON.stringify(response.data.data))
        }
        
        // 更新axios实例的认证头
        axios.defaults.headers.common['Authorization'] = `Bearer ${response.data?.token || ''}`
        
        // 跳转到首页
        router.push('/')
      } catch (err) {
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 600px;
}

.login-form {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
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

.password-input-container {
  position: relative;
  width: 100%;
}

.password-input-container input {
  padding-right: 40px;
}

.toggle-password-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.eye-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.form-group input:focus {
  outline: none;
  border-color: #1890ff;
}

.error-message {
  color: #ff4d4f;
  margin-bottom: 16px;
  padding: 8px 12px;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  padding: 10px 0;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.login-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #1890ff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
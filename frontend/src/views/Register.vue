<template>
  <div class="register-container">
    <div class="register-form">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="nickname">昵称</label>
          <input
            type="text"
            id="nickname"
            v-model="form.nickname"
            placeholder="请输入昵称"
            required
          />
        </div>
        
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
            <button type="button" class="send-code-btn" @click="sendCode" :disabled="codeSending || countdown > 0">
              {{ codeSending ? '发送中...' : countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </div>
        </div>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        
        <button type="submit" class="register-btn" :disabled="loading">
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
import { ref, reactive } from 'vue'
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
    
    const togglePasswordVisibility = (field) => {
      if (field === 'password') {
        showPassword.value = !showPassword.value
      } else if (field === 'confirmPassword') {
        showConfirmPassword.value = !showConfirmPassword.value
      }
    }

    const sendCode = async () => {
      // 简单的邮箱格式验证
      if (!form.email.includes('@')) {
        error.value = '请输入有效的邮箱地址'
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
      sendCode,
      togglePasswordVisibility,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 600px;
}

.register-form {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.register-form h2 {
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

.form-group input:focus {
    outline: none;
    border-color: #1890ff;
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
  
  .code-input-container {
    display: flex;
    gap: 10px;
  }
  
  .code-input-container input {
    flex: 1;
  }
  
  .send-code-btn {
    padding: 0 16px;
    background-color: #1890ff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
    font-size: 14px;
  }
  
  .send-code-btn:hover:not(:disabled) {
    background-color: #40a9ff;
  }
  
  .send-code-btn:disabled {
    background-color: #f5f5f5;
    color: #ccc;
    cursor: not-allowed;
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

.success-message {
  color: #52c41a;
  margin-bottom: 16px;
  padding: 8px 12px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  font-size: 14px;
}

.register-btn {
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

.register-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.register-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #1890ff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
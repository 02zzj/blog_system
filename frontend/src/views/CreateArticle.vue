<template>
  <div class="create-article">
    <h1>写文章</h1>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="title">标题</label>
        <input
          type="text"
          id="title"
          v-model="form.title"
          placeholder="请输入文章标题"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="content">内容</label>
        <textarea
          id="content"
          v-model="form.content"
          placeholder="请输入文章内容"
          rows="15"
          required
        ></textarea>
      </div>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <div class="form-actions">
        <button type="button" class="cancel-btn" @click="cancel">取消</button>
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '发布中...' : '发布文章' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../axios'

export default {
  name: 'CreateArticle',
  setup() {
    const router = useRouter()
    const form = reactive({
      title: '',
      content: ''
    })
    const loading = ref(false)
    const error = ref('')

    const handleSubmit = async () => {
      // 表单验证
      if (!form.title.trim()) {
        error.value = '请输入文章标题'
        return
      }

      if (!form.content.trim()) {
        error.value = '请输入文章内容'
        return
      }

      loading.value = true
      error.value = ''
      
      try {
        const response = await axios.post('/api/articles', form)
        alert('文章发布成功！')
        
        // 跳转到文章详情页
        // 由于axios拦截器直接返回了response.data，所以直接从response中获取id
        if (response?.id) {
          router.push(`/article/${response.id}`)
        } else {
          // 如果没有返回文章ID，则跳转到首页
          router.push('/')
        }
      } catch (err) {
        error.value = err.response?.data?.message || '发布失败，请稍后重试'
        console.error('发布文章失败:', err)
      } finally {
        loading.value = false
      }
    }

    const cancel = () => {
      if (form.title || form.content) {
        if (confirm('确定要放弃编辑吗？')) {
          router.push('/')
        }
      } else {
        router.push('/')
      }
    }

    return {
      form,
      loading,
      error,
      handleSubmit,
      cancel
    }
  }
}
</script>

<style scoped>
.create-article {
  max-width: 800px;
  margin: 0 auto;
}

.create-article h1 {
  margin-bottom: 30px;
  font-size: 32px;
  color: #333;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 16px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1890ff;
}

.form-group textarea {
  resize: vertical;
  min-height: 300px;
  line-height: 1.6;
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
}

.cancel-btn {
  padding: 10px 20px;
  background-color: #fff;
  color: #333;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

.submit-btn {
  padding: 10px 20px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.submit-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}
</style>
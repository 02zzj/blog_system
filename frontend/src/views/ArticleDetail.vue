<template>
  <div class="article-detail">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="article" class="article-content">
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-meta">
        <span class="author">{{ article.author?.nickname || '匿名用户' }}</span>
        <span class="date">{{ formatDate(article.createdAt) }}</span>
        <button 
          v-if="isAuthor" 
          @click="deleteArticle" 
          class="delete-btn"
          :disabled="deleting"
        >
          {{ deleting ? '删除中...' : '删除文章' }}
        </button>
      </div>
      <div class="article-body" v-html="article.content"></div>
    </div>
    <div v-else class="not-found">文章不存在或已被删除</div>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '../axios'

export default {
  name: 'ArticleDetail',
  props: {
    id: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const article = ref(null)
    const loading = ref(false)
    const error = ref('')
    const deleting = ref(false)

    // 获取当前登录用户
    const currentUser = computed(() => {
      const userStr = localStorage.getItem('user')
      return userStr ? JSON.parse(userStr) : null
    })

    // 判断当前用户是否是文章作者
    const isAuthor = computed(() => {
      return currentUser.value && article.value && 
        currentUser.value.id === article.value.author?.id
    })

    const fetchArticle = async () => {
      const articleId = props.id || route.params.id
      if (!articleId) {
        error.value = '文章ID不存在'
        return
      }

      loading.value = true
      error.value = ''
      try {
        // 确保使用正确的API路径，与后端保持一致
        // 注意：axios响应拦截器已直接返回response.data
        // 但根据后端ResponseResult格式，我们需要从data字段中提取文章数据
        console.log('Fetching article with ID:', articleId)
        const response = await axios.get(`/api/articles/${articleId}`)
        console.log('API response:', response)
        // 确保正确提取文章数据，处理后端ResponseResult格式
        article.value = response.data || response
        console.log('Article data after assignment:', article.value)
      } catch (err) {
        error.value = '获取文章详情失败'
        console.error('获取文章详情失败:', err)
      } finally {
        loading.value = false
      }
    }

    const deleteArticle = async () => {
      if (!confirm('确定要删除这篇文章吗？')) {
        return
      }

      deleting.value = true
      try {
        const articleId = props.id || route.params.id
        // 使用正确的API路径，与后端接口保持一致
        await axios.delete(`/api/articles/${articleId}`)
        alert('文章删除成功')
        router.push('/')
      } catch (err) {
        alert('删除失败，请稍后重试')
        console.error('删除文章失败:', err)
      } finally {
        deleting.value = false
      }
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    onMounted(() => {
      fetchArticle()
    })

    // 添加调试观察器
    watch(() => article.value, (newValue) => {
      console.log('Article value changed:', newValue)
    }, { deep: true })

    return {
      article,
      loading,
      error,
      deleting,
      isAuthor,
      deleteArticle,
      formatDate
    }
  }
}
</script>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
}

.loading,
.error,
.not-found {
  text-align: center;
  padding: 80px 0;
  font-size: 16px;
}

.error {
  color: #ff4d4f;
}

.article-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 32px;
}

.article-title {
  font-size: 32px;
  margin-bottom: 20px;
  color: #333;
  font-weight: 500;
}

.article-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  color: #666;
  font-size: 14px;
  align-items: center;
  flex-wrap: wrap;
}

.delete-btn {
  margin-left: auto;
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.delete-btn:hover:not(:disabled) {
  background-color: #ff7875;
}

.delete-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.article-body {
  line-height: 1.8;
  color: #333;
  font-size: 16px;
}

.article-body h1,
.article-body h2,
.article-body h3 {
  margin: 24px 0 16px;
  color: #262626;
}

.article-body p {
  margin-bottom: 16px;
}

.article-body img {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
  border-radius: 4px;
}

.article-body blockquote {
  border-left: 4px solid #d9d9d9;
  margin: 16px 0;
  padding: 8px 16px;
  color: #666;
  background-color: #fafafa;
}

.article-body pre {
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 16px 0;
}

.article-body code {
  background-color: #f5f5f5;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.article-body ul,
.article-body ol {
  margin: 16px 0;
  padding-left: 24px;
}

.article-body li {
  margin-bottom: 8px;
}
</style>
<template>
  <div class="profile">
    <h1>个人中心</h1>
    
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="user" class="profile-content">
      <div class="profile-info">
        <h2>用户信息</h2>
        <div class="info-item">
          <label>昵称：</label>
          <span>{{ user.nickname }}</span>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span>{{ user.email }}</span>
        </div>
        <div class="info-item">
          <label>注册时间：</label>
          <span>{{ formatDate(user.createdAt) }}</span>
        </div>
      </div>
      
      <div class="my-articles">
        <h2>我的文章</h2>
        <div v-if="articles.length === 0" class="no-articles">暂无文章</div>
        <div v-else class="article-list">
          <div v-for="article in articles" :key="article.id" class="article-card">
            <router-link :to="'/article/' + article.id" class="article-link">
              <h3 class="article-title">{{ article.title }}</h3>
              <div class="article-meta">
                <span class="date">{{ formatDate(article.createdAt) }}</span>
              </div>
            </router-link>
            <div class="article-actions">
              <button @click="deleteArticle(article.id)" class="delete-btn">删除</button>
            </div>
          </div>
        </div>
        
        <div class="pagination" v-if="articles.length > 0">
          <button @click="prevPage" :disabled="currentPage <= 1">上一页</button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
          <button @click="nextPage" :disabled="currentPage >= totalPages">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from '../axios'

export default {
  name: 'Profile',
  setup() {
    const user = ref(null)
    const articles = ref([])
    const loading = ref(false)
    const error = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalPages = ref(1)

    const fetchUserInfo = async () => {
      loading.value = true
      error.value = ''
      try {
        const response = await axios.get('/api/users/me')
        user.value = response.data || response
      } catch (err) {
        error.value = '获取用户信息失败'
        console.error('获取用户信息失败:', err)
      } finally {
        loading.value = false
      }
    }

    const fetchUserArticles = async (page = 1) => {
      try {
        // 添加用户ID参数，确保只获取当前用户的文章
        const response = await axios.get(`/api/articles?page=${page}&size=${pageSize.value}&userId=${user.value?.id}`)
        articles.value = response.data?.content || response.data || []
        // 计算总页数
        const totalElements = response.data?.totalElements || response.data?.length || 0
        totalPages.value = Math.ceil(totalElements / pageSize.value)
        currentPage.value = page
      } catch (err) {
        console.error('获取用户文章失败:', err)
      }
    }

    const deleteArticle = async (articleId) => {
      if (!confirm('确定要删除这篇文章吗？')) {
        return
      }

      try {
        await axios.delete(`/api/articles/${articleId}`)
        alert('文章删除成功')
        // 重新获取文章列表
        fetchUserArticles(currentPage.value)
      } catch (err) {
        alert('删除失败，请稍后重试')
        console.error('删除文章失败:', err)
      }
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        fetchUserArticles(currentPage.value - 1)
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        fetchUserArticles(currentPage.value + 1)
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

    onMounted(async () => {
      // 从localStorage获取用户信息，如果没有则从API获取
      const userStr = localStorage.getItem('user')
      if (userStr) {
        user.value = JSON.parse(userStr)
        // 获取到用户信息后再获取文章列表
        fetchUserArticles()
      } else {
        // 等待用户信息加载完成后再获取文章列表
        await fetchUserInfo()
        if (user.value) {
          fetchUserArticles()
        }
      }
    })

    return {
      user,
      articles,
      loading,
      error,
      currentPage,
      totalPages,
      deleteArticle,
      prevPage,
      nextPage,
      formatDate
    }
  }
}
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 0 auto;
}

.profile h1 {
  margin-bottom: 30px;
  font-size: 32px;
  color: #333;
}

.loading,
.error {
  text-align: center;
  padding: 40px 0;
  font-size: 16px;
}

.error {
  color: #ff4d4f;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.profile-info {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.profile-info h2,
.my-articles h2 {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.info-item {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  font-weight: 500;
  color: #666;
  width: 100px;
}

.info-item span {
  color: #333;
  font-size: 16px;
}

.my-articles {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.no-articles {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 16px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 16px;
  transition: all 0.3s;
  position: relative;
}

.article-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.article-title {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.article-meta {
  color: #666;
  font-size: 14px;
}

.article-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 1;
}

.delete-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.delete-btn:hover {
  background-color: #ff7875;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.pagination button {
  padding: 6px 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.pagination button:hover:not(:disabled) {
  background-color: #40a9ff;
}

.pagination button:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.pagination span {
  font-size: 14px;
  color: #666;
}
</style>
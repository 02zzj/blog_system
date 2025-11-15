<template>
  <div class="home">
    <h1>博客列表</h1>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="article-list">
      <div v-for="article in articles" :key="article.id" class="article-card">
        <router-link :to="'/article/' + article.id" class="article-link">
          <h2 class="article-title">{{ article.title }}</h2>
          <div class="article-meta">
            <span class="author">{{ article.author?.nickname || '匿名用户' }}</span>
            <span class="date">{{ formatDate(article.createdAt) }}</span>
          </div>
          <div class="article-excerpt" v-html="getExcerpt(article.content)"></div>
        </router-link>
      </div>
    </div>
    <div class="pagination" v-if="!loading && !error">
      <button @click="prevPage" :disabled="currentPage <= 1">上一页</button>
      <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages">下一页</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from '../axios'

export default {
  name: 'Home',
  setup() {
    const articles = ref([])
    const loading = ref(false)
    const error = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalPages = ref(1)

    const fetchArticles = async (page = 1) => {
      loading.value = true
      error.value = ''
      try {
        // 添加/api前缀以匹配axios配置
        const response = await axios.get(`/api/articles?page=${page}&size=${pageSize.value}`)
        articles.value = response.data?.content || response.data || []
        // 计算总页数
        const totalElements = response.data?.totalElements || response.data?.length || 0
        totalPages.value = Math.ceil(totalElements / pageSize.value)
        currentPage.value = page
      } catch (err) {
        error.value = '获取文章列表失败'
        console.error('获取文章列表失败:', err)
      } finally {
        loading.value = false
      }
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        fetchArticles(currentPage.value - 1)
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        fetchArticles(currentPage.value + 1)
      }
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }

    // 获取文章摘要
    const getExcerpt = (html) => {
      if (!html) return ''
      // 去除HTML标签并截取前100个字符
      const text = html.replace(/<[^>]+>/g, '')
      return text.length > 100 ? text.substring(0, 100) + '...' : text
    }

    onMounted(() => {
      fetchArticles()
    })

    return {
      articles,
      loading,
      error,
      currentPage,
      totalPages,
      prevPage,
      nextPage,
      formatDate,
      getExcerpt
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 0 auto;
}

.home h1 {
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

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 24px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.article-title {
  font-size: 24px;
  margin-bottom: 12px;
  color: #333;
  font-weight: 500;
}

.article-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  color: #666;
  font-size: 14px;
}

.article-excerpt {
  color: #555;
  line-height: 1.6;
  font-size: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
}

.pagination button {
  padding: 8px 16px;
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
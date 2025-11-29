<template>
  <div class="home">
    <div class="header-controls">
      <h1>文章列表</h1>
      <div class="sort-controls">
        <label>排序方式：</label>
        <select v-model="sortOrder" @change="handleSortChange">
          <option value="desc">最新优先</option>
          <option value="asc">最早优先</option>
        </select>
      </div>
    </div>
    <div v-if="loading" class="loading">
      <div class="skeleton-container">
        <div class="skeleton-card" v-for="i in 5" :key="i">
          <div class="skeleton-title"></div>
          <div class="skeleton-meta"></div>
          <div class="skeleton-cover"></div>
          <div class="skeleton-excerpt"></div>
        </div>
      </div>
    </div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="article-list">
      <div v-for="(article, index) in articles" :key="article.id" class="article-card" :style="{ animationDelay: `${index * 0.08}s` }">
        <router-link :to="'/article/' + article.id" class="article-link">
          <h2 class="article-title">{{ article.title }}</h2>
          <div class="article-meta">
            <span class="author">{{ article.author?.nickname || '匿名用户' }}</span>
            <span class="date">{{ formatDate(article.updatedAt) }}</span>
          </div>
          <div class="article-cover-container">
            <img :src="article.coverImage || defaultCover" alt="文章封面" class="article-cover">
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
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '../axios'
import defaultCover from '../assets/images/default_cover.png'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const articles = ref([])
    const loading = ref(false)
    const error = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalPages = ref(1)
    const sortOrder = ref('desc') // 默认降序，最新优先
    // 不再在组件内部管理搜索状态，而是从全局状态获取

    const fetchArticles = async (page = 1, order = sortOrder.value) => {
      loading.value = true
      error.value = ''
      try {
        // 添加/api前缀以匹配axios配置，增加排序参数和搜索关键词
        const queryParams = new URLSearchParams()
        queryParams.append('page', page)
        queryParams.append('size', pageSize.value)
        queryParams.append('sortField', 'updatedAt')
        queryParams.append('sortDirection', order)
        
        // 从全局状态获取搜索参数
        const globalSearch = window.globalSearchState || {}
        if (globalSearch.keyword) {
          if (globalSearch.type === 'author') {
            // 按作者昵称搜索
            queryParams.append('authorNickname', globalSearch.keyword)
          } else if (globalSearch.type === 'title_content') {
            // 仅搜索标题和内容
            queryParams.append('keyword', globalSearch.keyword)
            queryParams.append('searchType', 'title_content')
          } else {
            // 默认搜索标题、内容和作者昵称
            queryParams.append('keyword', globalSearch.keyword)
          }
        }
        
        const response = await axios.get(`/api/articles?${queryParams.toString()}`)
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
    
    const handleSortChange = () => {
      // 切换排序时重置到第一页，保留搜索关键词
      currentPage.value = 1
      fetchArticles(1, sortOrder.value)
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
    
    // 不再需要handleSearch方法，由App组件处理搜索

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

    // 重置搜索状态的函数
    const resetSearchState = () => {
      // 清除全局搜索状态
      window.globalSearchState = {}
      currentPage.value = 1
      fetchArticles()
    }
    
    onMounted(() => {
      // 页面首次加载时重置搜索状态
      resetSearchState()
    })
    
    // 监听路由变化，确保每次导航到首页时都重置搜索状态
    watch(
      () => route.fullPath, 
      () => {
        if (route.path === '/') {
          resetSearchState()
        }
      },
      { immediate: true }
    )
    
    // 监听全局搜索状态变化
    // 使用定时器定期检查全局状态的变化
    let lastSearchStateTimestamp = null
    const resetCheckInterval = setInterval(() => {
      // 检查搜索状态变化
      const currentSearch = window.globalSearchState || {}
      if (currentSearch.timestamp && currentSearch.timestamp !== lastSearchStateTimestamp) {
        lastSearchStateTimestamp = currentSearch.timestamp
        currentPage.value = 1
        fetchArticles()
      }
    }, 300) // 降低检查频率，避免过度检查和重复加载
    
    // 组件卸载时清除定时器
    onUnmounted(() => {
      clearInterval(resetCheckInterval)
    })

    return {
      articles,
      loading,
      error,
      currentPage,
      totalPages,
      sortOrder,
      defaultCover,
      formatDate,
      getExcerpt,
      handleSortChange,
      prevPage,
      nextPage
    }
  }
}
</script>

<style scoped>
/* 全局动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.home {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 搜索功能样式已移至App.vue */



.header-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.home h1 {
  margin: 0;
  font-size: 32px;
  color: #333;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.1s both;
  opacity: 0;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.2s both;
  opacity: 0;
}

.sort-controls label {
  font-size: 14px;
  color: #666;
}

.sort-controls select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
}

.sort-controls select:hover {
  border-color: #40a9ff;
}

.sort-controls select:focus {
  outline: none;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.loading,
.error {
  text-align: center;
  padding: 40px 0;
  font-size: 16px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 骨架屏样式 */
.skeleton-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skeleton-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  padding: 24px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  position: relative;
}

.skeleton-title {
  width: 80%;
  height: 28px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 6px;
  margin-bottom: 16px;
  animation: shimmer 1.5s infinite;
}

.skeleton-meta {
  width: 50%;
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin-bottom: 16px;
  animation: shimmer 1.5s infinite;
}

.skeleton-cover {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 8px;
  margin-bottom: 16px;
  animation: shimmer 1.5s infinite;
}

.skeleton-excerpt {
  width: 100%;
  height: 60px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
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
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 1px solid #f0f0f0;
  overflow: hidden;
  position: relative;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation-fill-mode: both;
  opacity: 0;
}

/* 卡片悬停效果增强 */
.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  border-color: transparent;
}

/* 伪元素动画效果 */
.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #1890ff, #52c41a, #faad14, #f5222d);
  transform: scaleX(0);
  transition: transform 0.4s ease;
  z-index: 1;
}

.article-card:hover::before {
  transform: scaleX(1);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

/* 标题动画效果 */
.article-title {
  font-size: 24px;
  margin-bottom: 12px;
  color: #333;
  font-weight: 600;
  line-height: 1.4;
  transition: color 0.3s ease, transform 0.3s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-card:hover .article-title {
  color: #1890ff;
  transform: translateX(5px);
}

/* 元信息样式优化 */
.article-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
  color: #666;
  font-size: 14px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
}

.article-meta .author {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.3s ease;
}

.article-meta .author::before {
  content: '👤';
  font-size: 14px;
}

.article-meta .date {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.3s ease;
}

.article-meta .date::before {
  content: '📅';
  font-size: 14px;
}

.article-card:hover .article-meta .author,
.article-card:hover .article-meta .date {
  color: #1890ff;
}

/* 封面图容器优化 */
.article-cover-container {
  width: 100%;
  height: 200px;
  margin: 16px 0;
  overflow: hidden;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.article-card:hover .article-cover-container {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 封面图缩放动画 */
.article-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.article-card:hover .article-cover {
  transform: scale(1.08);
}

/* 摘要样式优化 */
.article-excerpt {
  color: #555;
  line-height: 1.7;
  font-size: 16px;
  transition: all 0.3s ease;
  opacity: 0.9;
}

.article-card:hover .article-excerpt {
  opacity: 1;
  transform: translateX(3px);
}

/* 阅读更多指示 */
.article-excerpt::after {
  content: '阅读更多 →';
  display: inline-block;
  margin-top: 8px;
  font-weight: 500;
  color: #1890ff;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.article-card:hover .article-excerpt::after {
  opacity: 1;
  transform: translateX(0);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.3s both;
  opacity: 0;
}

.pagination button {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
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

/* 响应式样式 */
@media (max-width: 768px) {
  .home {
    padding: 10px;
  }
  
  .header-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    margin-bottom: 20px;
  }
  
  .home h1 {
    font-size: 28px;
  }
  
  .sort-controls {
    align-self: stretch;
  }
  
  .article-card {
    padding: 16px;
  }
  
  .article-title {
    font-size: 20px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding-bottom: 12px;
  }
  
  .article-cover-container {
    height: 160px;
  }
  
  .article-excerpt {
    font-size: 14px;
  }
  
  .pagination {
    gap: 15px;
    margin-top: 30px;
  }
  
  .pagination button {
    padding: 6px 12px;
    font-size: 14px;
  }
  
  .skeleton-card {
    padding: 16px;
  }
  
  .skeleton-cover {
    height: 160px;
  }
}

@media (max-width: 480px) {
  .home h1 {
    font-size: 24px;
  }
  
  .article-title {
    font-size: 18px;
  }
  
  .article-cover-container {
    height: 140px;
  }
  
  .pagination {
    flex-direction: column;
    gap: 10px;
  }
  
  .pagination button {
    width: 100%;
    max-width: 200px;
  }
  
  .skeleton-cover {
    height: 140px;
  }
}
</style>
<template>
  <div class="home">
    <h1>博客列表</h1>
    <div class="sort-controls">
      <label>排序方式：</label>
      <select v-model="sortOrder" @change="handleSortChange">
        <option value="desc">最新优先</option>
        <option value="asc">最早优先</option>
      </select>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="article-list">
      <div v-for="article in articles" :key="article.id" class="article-card">
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
    
    // 监听全局重置标志和搜索状态变化
    // 使用定时器定期检查全局状态的变化
    let lastResetFlag = null
    let lastSearchStateTimestamp = null
    const resetCheckInterval = setInterval(() => {
      // 检查重置标志
      if (window.homeResetFlag && window.homeResetFlag !== lastResetFlag) {
        lastResetFlag = window.homeResetFlag
        // 检查是否有搜索条件，如果有则执行搜索，否则重置搜索
        const currentSearch = window.globalSearchState || {}
        if (currentSearch.keyword) {
          // 有搜索条件，执行搜索
          currentPage.value = 1
          fetchArticles()
        } else {
          // 无搜索条件，重置搜索
          resetSearchState()
        }
      }
      
      // 检查搜索状态变化
      const currentSearch = window.globalSearchState || {}
      if (currentSearch.keyword && currentSearch.timestamp !== lastSearchStateTimestamp) {
        lastSearchStateTimestamp = currentSearch.timestamp
        currentPage.value = 1
        fetchArticles()
      }
    }, 100) // 每100毫秒检查一次
    
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
.home {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

/* 搜索功能样式已移至App.vue */



.home h1 {
  margin-bottom: 20px;
  font-size: 32px;
  color: #333;
}

.sort-controls {
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
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

.article-cover-container {
  width: 100%;
  height: 200px;
  margin: 16px 0;
  overflow: hidden;
  border-radius: 4px;
}

.article-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
<template>
  <div class="home">
    <!-- 装饰性背景元素 -->
    <div class="cyber-bg"></div>
    <div class="cyber-grid"></div>
    
    <div class="header-row">
      <h1 class="cyber-title">文章列表</h1>
      <div class="sort-controls cyber-dropdown-container">
        <label>排序方式：</label>
        <div class="cyber-dropdown" ref="sortDropdown">
          <div class="cyber-dropdown-select" @click="toggleSortDropdown">
            <span>{{ sortOrder === 'desc' ? '最新优先' : '最早优先' }}</span>
            <span class="cyber-dropdown-arrow" :class="{ 'rotate': showSortDropdown }">▼</span>
          </div>
          <div class="cyber-dropdown-menu" v-if="showSortDropdown">
            <div class="cyber-dropdown-item" @click="selectSort('desc')">最新优先</div>
            <div class="cyber-dropdown-item" @click="selectSort('asc')">最早优先</div>
          </div>
        </div>
      </div>
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
    const showSortDropdown = ref(false)
    const sortDropdown = ref(null)
    const lastScrollY = ref(0)
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
    
    const toggleSortDropdown = () => {
      showSortDropdown.value = !showSortDropdown.value
    }
    
    const selectSort = (order) => {
      sortOrder.value = order
      handleSortChange()
      showSortDropdown.value = false
    }
    
    // 点击外部关闭下拉菜单
    const handleClickOutside = (event) => {
      if (sortDropdown.value && !sortDropdown.value.contains(event.target)) {
        showSortDropdown.value = false
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
      document.addEventListener('click', handleClickOutside)
      // 添加滚动监听以实现视差效果
      window.addEventListener('scroll', handleScroll)
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
    // 处理滚动事件，实现视差效果
    const handleScroll = () => {
      const currentScrollY = window.scrollY
      const scrollDiff = currentScrollY - lastScrollY.value
      
      // 为背景元素添加视差效果
      const cyberBgElements = document.querySelectorAll('.cyber-bg')
      cyberBgElements.forEach(el => {
        const currentTransform = el.style.transform || 'translateY(0px)'
        const translateY = parseInt(currentTransform.match(/-?\d+/)?.[0] || '0')
        el.style.transform = `translateY(${translateY + scrollDiff * 0.05}px)`
      })
      
      // 为网格添加视差效果
      const cyberGridElements = document.querySelectorAll('.cyber-grid')
      cyberGridElements.forEach(el => {
        const currentTransform = el.style.transform || 'translateY(0px)'
        const translateY = parseInt(currentTransform.match(/-?\d+/)?.[0] || '0')
        el.style.transform = `translateY(${translateY + scrollDiff * 0.02}px)`
      })
      
      lastScrollY.value = currentScrollY
    }

    onUnmounted(() => {
      clearInterval(resetCheckInterval)
      document.removeEventListener('click', handleClickOutside)
      window.removeEventListener('scroll', handleScroll)
    })

    return {
      articles,
      loading,
      error,
      currentPage,
      totalPages,
      sortOrder,
      showSortDropdown,
      sortDropdown,
      defaultCover,
      formatDate,
      getExcerpt,
      handleSortChange,
      toggleSortDropdown,
      selectSort,
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
  padding: 40px 20px;
  position: relative;
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
  /* 移除overflow: hidden，避免裁剪下拉菜单 */
  animation: pageFadeIn 1s ease-out;
}

@keyframes pageFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 装饰性背景元素 */
.cyber-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--main-bg-pattern);
  z-index: 0;
}

.cyber-grid {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(to right, rgba(var(--text-accent), 0.05) 1px, transparent 1px),
                    linear-gradient(to bottom, rgba(var(--text-accent), 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  z-index: 0;
  animation: gridMove 30s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 0 500px;
  }
}

.home > * {
  position: relative;
  z-index: 1;
}

.header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;
    /* 确保下拉菜单容器有更高的z-index，避免被覆盖 */
    z-index: 12;
  }

  .cyber-title {
    font-size: 32px;
    font-weight: 700;
    background: linear-gradient(90deg, var(--highlight), var(--text-accent));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 0 20px rgba(var(--highlight), 0.3);
    position: relative;
    margin: 0;
  }

  .cyber-title::after {
     content: '';
     position: absolute;
     bottom: -10px;
     left: 50%;
     transform: translateX(-50%);
     width: 80px;
     height: 3px;
     background: linear-gradient(90deg, var(--highlight), var(--text-accent));
     border-radius: 3px;
     box-shadow: 0 0 10px rgba(var(--highlight), 0.3);
     animation: pulse 3s infinite;
   }

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: translateX(-50%) scaleX(1);
  }
  50% {
    opacity: 0.7;
    transform: translateX(-50%) scaleX(1.2);
  }
}

/* 自定义下拉菜单样式 */
.cyber-dropdown-container {
    display: flex;
    align-items: center;
    gap: 15px;
  }

.cyber-dropdown-container label {
  font-size: 16px;
  color: var(--text-secondary);
  text-shadow: 0 0 5px rgba(var(--text-secondary), 0.2);
}

.cyber-dropdown {
  position: relative;
  min-width: 150px;
  z-index: 11; /* 比下拉菜单更高，确保定位上下文 */
}

.cyber-dropdown-select {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
  box-shadow: var(--shadow);
}

.cyber-dropdown-select:hover {
  border-color: var(--text-accent);
  box-shadow: 0 0 15px rgba(var(--highlight), 0.2);
}

.cyber-dropdown-arrow {
  font-size: 12px;
  transition: transform 0.3s;
  color: var(--highlight);
}

.cyber-dropdown-arrow.rotate {
  transform: rotate(180deg);
  color: var(--text-accent);
}

.cyber-dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 5px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
  animation: dropdownSlide 0.3s ease-out;
  z-index: 10;
  /* 确保下拉菜单不会被任何容器裁剪 */
  transform: translateY(0);
}

@keyframes dropdownSlide {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cyber-dropdown-item {
  padding: 12px 15px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s;
}

.cyber-dropdown-item:hover {
  background: linear-gradient(90deg, rgba(var(--highlight), 0.1), rgba(var(--text-accent), 0.1));
  color: var(--text-primary);
  text-shadow: 0 0 5px rgba(var(--highlight), 0.2);
}

.cyber-dropdown-item:first-child {
  border-bottom: 1px solid var(--border-color);
}

.loading,
.error {
  text-align: center;
  padding: 60px 0;
  font-size: 18px;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading {
  color: var(--highlight);
  font-weight: 500;
  position: relative;
}

.loading::after {
  content: '';
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(var(--highlight), 0.3);
  border-radius: 50%;
  border-top-color: var(--highlight);
  animation: spin 1s linear infinite;
  margin-left: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error {
  color: rgba(var(--error-rgb), 1);
  background: rgba(var(--error-rgb), 0.05);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  margin: 20px 0;
  box-shadow: var(--shadow);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 文章卡片进入动画 */
.article-card {
  animation: cardSlideIn 0.5s ease-out forwards;
  opacity: 0;
}

.article-card:nth-child(1) { animation-delay: 0.1s; }
.article-card:nth-child(2) { animation-delay: 0.2s; }
.article-card:nth-child(3) { animation-delay: 0.3s; }
.article-card:nth-child(4) { animation-delay: 0.4s; }
.article-card:nth-child(5) { animation-delay: 0.5s; }

@keyframes cardSlideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.article-card {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 30px;
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow);
}

.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--highlight), var(--text-accent));
  box-shadow: 0 0 10px rgba(var(--highlight), 0.3);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.article-card:hover {
  transform: translateY(-5px);
  border-color: var(--highlight);
  box-shadow: 0 10px 30px rgba(var(--highlight), 0.2);
}

.article-card:hover::before {
  transform: scaleX(1);
}

.article-card::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(var(--highlight), 0.15), transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.5s, height 0.5s;
}

.article-card:hover::after {
  width: 200%;
  height: 200%;
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
  position: relative;
  z-index: 2;
}

.article-cover-container {
  width: 100%;
  height: 220px;
  margin: 20px 0;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow);
  position: relative;
}

.article-cover-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(var(--highlight), 0.1), rgba(var(--text-accent), 0.1));
  pointer-events: none;
}

.article-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.article-card:hover .article-cover {
  transform: scale(1.05);
}

.article-title {
  font-size: 28px;
  margin-bottom: 16px;
  color: var(--text-primary);
  font-weight: 700;
  line-height: 1.3;
  position: relative;
  display: inline-block;
}

.article-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, var(--highlight), transparent);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.article-card:hover .article-title::after {
  transform: scaleX(1);
}

.article-meta {
  display: flex;
  gap: 25px;
  margin-bottom: 20px;
  font-size: 14px;
  position: relative;
}

.author {
  color: var(--text-accent);
  font-weight: 500;
  text-shadow: 0 0 5px rgba(var(--text-accent), 0.2);
}

.date {
  color: var(--text-secondary);
}

.article-excerpt {
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 16px;
  position: relative;
}

.article-excerpt::first-letter {
  color: var(--highlight);
  font-size: 1.5em;
  font-weight: bold;
  margin-right: 3px;
}

/* 文章卡片悬停时的文本效果 */
.article-card:hover .article-title {
  text-shadow: 0 0 10px rgba(var(--text-primary), 0.3);
}

.article-card:hover .author {
  text-shadow: 0 0 10px rgba(var(--text-accent), 0.3);
}

.article-card:hover .date {
  text-shadow: 0 0 10px rgba(var(--text-secondary), 0.3);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 60px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(5px);
  position: relative;
  animation: paginationSlideIn 0.8s ease-out;
}

@keyframes paginationSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pagination::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
  box-shadow: 0 0 10px rgba(var(--highlight), 0.2);
}

.pagination button {
  padding: 12px 24px;
  background: linear-gradient(135deg, rgba(var(--highlight), 0.1), rgba(var(--text-accent), 0.1));
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow);
}

.pagination button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(var(--highlight), 0.2), transparent);
  transition: left 0.5s ease;
}

.pagination button:hover:not(:disabled)::before {
  left: 100%;
}

.pagination button:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(var(--highlight), 0.2), rgba(var(--text-accent), 0.2));
  border-color: var(--text-accent);
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(var(--highlight), 0.2);
}

.pagination button:disabled {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border-color: var(--border-color);
  cursor: not-allowed;
  box-shadow: none;
}

.pagination span {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
  text-shadow: 0 0 5px rgba(var(--text-secondary), 0.2);
}

/* 响应式设计 */
/* 平板设备 */
@media (max-width: 768px) {
  .home {
    padding: 30px 15px;
    background: var(--bg-primary);
  }
  
  .cyber-bg {
    background: var(--main-bg-pattern);
  }
  
  .cyber-title {
    font-size: 32px;
    text-shadow: 0 0 15px rgba(var(--highlight), 0.3);
  }
  
  .cyber-title::after {
    width: 80px;
    height: 2px;
  }
  
  .cyber-dropdown-container {
    gap: 12px;
  }
  
  .cyber-dropdown-select {
    padding: 12px 15px;
  }
  
  .article-list {
    gap: 25px;
  }
  
  .article-card {
    padding: 25px 20px;
    border-radius: 10px;
    backdrop-filter: blur(8px);
  }
  
  .article-cover-container {
    height: 180px;
    margin: 16px 0;
  }
  
  .article-title {
    font-size: 24px;
    line-height: 1.25;
  }
  
  .article-meta {
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;
  }
  
  .article-excerpt {
    font-size: 15px;
    line-height: 1.7;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 15px;
    padding: 16px;
    margin-top: 50px;
  }
  
  .pagination button {
    padding: 10px 20px;
    font-size: 14px;
    min-width: 100px;
  }
  
  .pagination span {
    font-size: 15px;
  }
  
  /* 调整动画效果 */
  .article-card {
    animation: cardSlideIn 0.4s ease-out forwards;
  }
  
  @keyframes gridMove {
    0% {
      background-position: 0 0;
    }
    100% {
      background-position: 0 400px;
    }
  }
}

/* 移动设备 */
@media (max-width: 480px) {
  .home {
    padding: 25px 12px;
  }
  
  .cyber-bg {
    background: var(--main-bg-pattern);
  }
  
  .cyber-grid {
    background-size: 30px 30px;
  }
  
  .cyber-title {
    font-size: 28px;
    text-align: center;
    margin-bottom: 25px;
  }
  
  .cyber-title::after {
    width: 60px;
    height: 2px;
  }
  
  .cyber-dropdown-container {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    margin-bottom: 25px;
  }
  
  .cyber-dropdown-container label {
    text-align: center;
    font-size: 15px;
  }
  
  .cyber-dropdown {
    min-width: auto;
  }
  
  .cyber-dropdown-select {
    padding: 14px;
    font-size: 15px;
    text-align: center;
  }
  
  .cyber-dropdown-menu {
    margin-top: 3px;
  }
  
  .cyber-dropdown-item {
    padding: 14px;
    font-size: 15px;
    text-align: center;
  }
  
  .loading,
  .error {
    padding: 40px 15px;
    font-size: 16px;
    min-height: 200px;
  }
  
  .error {
    margin: 15px 0;
  }
  
  .article-list {
    gap: 20px;
  }
  
  .article-card {
    padding: 20px 15px;
    border-radius: 8px;
    backdrop-filter: blur(5px);
  }
  
  .article-cover-container {
    height: 160px;
    margin: 15px 0;
    border-radius: 6px;
  }
  
  .article-title {
    font-size: 20px;
    margin-bottom: 12px;
    line-height: 1.3;
  }
  
  .article-meta {
    font-size: 13px;
    gap: 6px;
  }
  
  .article-excerpt {
    font-size: 14px;
    line-height: 1.6;
  }
  
  .pagination {
    flex-direction: column;
    gap: 12px;
    padding: 15px;
    margin-top: 40px;
  }
  
  .pagination button {
    padding: 12px 24px;
    font-size: 15px;
    width: 100%;
    min-width: auto;
    /* 移动端触摸反馈优化 */
    -webkit-tap-highlight-color: transparent;
    touch-action: manipulation;
  }
  
  .pagination span {
    font-size: 14px;
    order: -1;
  }
  
  /* 优化移动设备动画效果 */
  .article-card {
    animation: cardSlideIn 0.3s ease-out forwards;
  }
  
  .article-card:nth-child(1) { animation-delay: 0.05s; }
  .article-card:nth-child(2) { animation-delay: 0.1s; }
  .article-card:nth-child(3) { animation-delay: 0.15s; }
  .article-card:nth-child(4) { animation-delay: 0.2s; }
  .article-card:nth-child(5) { animation-delay: 0.25s; }
  
  @keyframes pageFadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  @keyframes gridMove {
    0% {
      background-position: 0 0;
    }
    100% {
      background-position: 0 300px;
    }
  }
}

/* 超小屏幕设备 */
@media (max-width: 360px) {
  .cyber-title {
    font-size: 24px;
  }
  
  .article-cover-container {
    height: 140px;
  }
  
  .article-title {
    font-size: 18px;
  }
  
  .article-excerpt {
    font-size: 13px;
  }
}

/* 触摸设备交互优化 */
@media (hover: none) and (pointer: coarse) {
  .article-card:active {
    transform: scale(0.98);
    transition: transform 0.1s;
  }
  
  .cyber-dropdown-select:active {
    background: linear-gradient(135deg, rgba(var(--highlight), 0.2), rgba(var(--text-accent), 0.2));
  }
  
  .cyber-dropdown-item:active {
    background: linear-gradient(90deg, rgba(var(--highlight), 0.2), rgba(var(--text-accent), 0.2));
  }
  
  .pagination button:active:not(:disabled) {
    transform: translateY(0);
    background: linear-gradient(135deg, rgba(var(--highlight), 0.3), rgba(var(--text-accent), 0.3));
  }
}
</style>
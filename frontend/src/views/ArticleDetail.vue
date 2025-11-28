<template>
  <div class="article-detail">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="article" class="article-content">
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-meta">
        <span class="author">{{ article.author?.nickname || '匿名用户' }}</span>
        <span class="date">{{ formatDate(article.updatedAt) }}</span>
      </div>
      <div class="article-body" v-html="article.content"></div>
      
      <!-- 评论区域 -->
        <div class="comments-section">
          <div class="comments-header">
            <h2 class="comments-title">评论 ({{ commentCount }})</h2>
            <div class="sort-controls">
              <span>排序方式：</span>
              <select v-model="sortOrder" @change="loadComments(1)">
                <option value="desc">最新优先</option>
                <option value="asc">最早优先</option>
              </select>
            </div>
          </div>
        
        <!-- 发表评论表单 -->
        <div class="comment-form" v-if="currentUser">
          <textarea 
            v-model="newCommentContent"
            placeholder="写下你的评论..."
            rows="4"
            :disabled="submittingComment"
          ></textarea>
          <div class="comment-form-actions">
            <button 
              @click="submitComment"
              :disabled="!newCommentContent.trim() || submittingComment"
              class="submit-comment-btn"
            >
              {{ submittingComment ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>
        <div v-else class="login-prompt">
          请先<a href="/login">登录</a>后再发表评论
        </div>
        
        <!-- 评论列表 -->
        <div class="comments-list" v-if="comments.length > 0">
          <div 
            v-for="(comment, index) in comments" 
            :key="comment.id" 
            class="comment-item"
          >
            <div class="comment-header">
                <span class="comment-floor">{{ sortOrder === 'asc' ? comment.floorNumber + index : comment.floorNumber - index }}楼</span>
              <span class="comment-author">{{ comment.userNickname }}</span>
              <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
              <button 
                v-if="canDeleteComment(comment)" 
                @click="deleteComment(comment.id)"
                class="delete-comment-btn"
                :disabled="deletingComment"
              >
                删除
              </button>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        <div v-else class="no-comments">暂无评论，快来发表第一条评论吧！</div>
        
        <!-- 分页 -->
        <div class="pagination" v-if="totalPages > 1">
          <button 
            @click="currentPage > 1 && loadComments(currentPage - 1)"
            :disabled="currentPage === 1 || loadingComments"
          >
            上一页
          </button>
          <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
          <button 
            @click="currentPage < totalPages && loadComments(currentPage + 1)"
            :disabled="currentPage === totalPages || loadingComments"
          >
            下一页
          </button>
        </div>
      </div>
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
    
    // 评论相关状态
    const comments = ref([])
    const commentCount = ref(0)
    const newCommentContent = ref('')
    const submittingComment = ref(false)
    const deletingComment = ref(false)
    const loadingComments = ref(false)
    const currentPage = ref(1)
    const totalPages = ref(1)
    const sortOrder = ref('desc') // 排序方式：'desc'为降序（最新优先），'asc'为升序（最早优先）

    // 获取当前登录用户
    const currentUser = computed(() => {
      try {
        const userStr = localStorage.getItem('user')
        if (userStr) {
          const user = JSON.parse(userStr)
          console.log('Current user from localStorage:', user)
          return user
        }
      } catch (e) {
        console.error('Error parsing user from localStorage:', e)
        localStorage.removeItem('user')
      }
      return null
    })

    // 判断当前用户是否是文章作者（为其他功能保留）
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
        console.log('Fetching article with ID:', articleId)
        const response = await axios.get(`/api/articles/${articleId}`)
        console.log('API response:', response)
        // 确保正确提取文章数据，处理后端ResponseResult格式
        article.value = response.data || response
        console.log('Article data after assignment:', article.value)
        
        // 文章加载成功后，加载评论
        if (article.value) {
          loadComments(1)
          loadCommentCount()
        }
      } catch (err) {
        error.value = '获取文章详情失败'
        console.error('获取文章详情失败:', err)
      } finally {
        loading.value = false
      }
    }

    // 加载评论列表
    const loadComments = async (page = 1) => {
      const articleId = props.id || route.params.id
      if (!articleId) return

      loadingComments.value = true
      try {
        const response = await axios.get(`/api/comments/article/${articleId}`, {
          params: { page, size: 20, sortOrder: sortOrder.value }
        })
        const data = response.data || response
        if (data.content) {
          comments.value = data.content
          totalPages.value = data.totalPages
          currentPage.value = data.number + 1 // Spring Data JPA页码从0开始
        }
      } catch (err) {
        console.error('获取评论列表失败:', err)
      } finally {
        loadingComments.value = false
      }
    }

    // 加载评论数量
    const loadCommentCount = async () => {
      const articleId = props.id || route.params.id
      if (!articleId) return

      try {
        const response = await axios.get(`/api/comments/article/${articleId}/count`)
        // 处理响应格式，提取实际的评论数量
        commentCount.value = response.data?.data !== undefined ? response.data.data : (response.data || 0)
      } catch (err) {
        console.error('获取评论数量失败:', err)
      }
    }

    // 提交评论
    const submitComment = async () => {
      if (!newCommentContent.value.trim() || submittingComment.value) return

      submittingComment.value = true
      try {
        const articleId = props.id || route.params.id
        await axios.post('/api/comments', {
          articleId: parseInt(articleId),
          content: newCommentContent.value.trim()
        })
        
        // 清空输入框
        newCommentContent.value = ''
        
        // 重新加载评论（回到第一页）
        currentPage.value = 1
        await loadComments(1)
        await loadCommentCount()
        
        alert('评论发表成功')
      } catch (err) {
        alert('评论发表失败，请稍后重试')
        console.error('发表评论失败:', err)
      } finally {
        submittingComment.value = false
      }
    }

    // 删除评论
    const deleteComment = async (commentId) => {
      if (!confirm('确定要删除这条评论吗？')) return

      deletingComment.value = true
      try {
        await axios.delete(`/api/comments/${commentId}`)
        
        // 重新加载当前页的评论
        await loadComments(currentPage.value)
        await loadCommentCount()
      } catch (err) {
        alert('删除评论失败，请稍后重试')
        console.error('删除评论失败:', err)
      } finally {
        deletingComment.value = false
      }
    }

    // 判断是否可以删除评论（评论作者或文章作者）
    const canDeleteComment = (comment) => {
      if (!currentUser.value || !article.value) return false
      
      const isCommentAuthor = currentUser.value.id === comment.userId
      const isArticleAuthor = currentUser.value.id === article.value.author?.id
      
      return isCommentAuthor || isArticleAuthor
    }

    // 删除文章功能已移除，改为在个人中心管理

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
      deletingComment,
      isAuthor,
      formatDate,
      currentUser,
      // 评论相关
      comments,
      commentCount,
      newCommentContent,
      sortOrder,
      submittingComment,
      deletingComment,
      loadingComments,
      currentPage,
      totalPages,
      submitComment,
      deleteComment,
      canDeleteComment,
      loadComments
    }
  }
}
</script>

<style scoped>
/* 全局玄幻科技风格背景 */
body {
  background: var(--main-bg);
  color: var(--text-primary);
  min-height: 100vh;
}

.article-detail {
  max-width: 900px;
  margin: 2rem auto;
  width: 100%;
  overflow-x: hidden;
  padding: 0 1rem;
}

.loading,
.error,
.not-found {
  text-align: center;
  padding: 80px 0;
  font-size: 16px;
  color: var(--text-primary);
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.3);
}

.error {
  color: #ff6b6b;
  text-shadow: 0 0 10px rgba(255, 107, 107, 0.5);
}

/* 科技感文章容器 */
.article-content {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 2.5rem;
  overflow-x: hidden;
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: visible;
}

/* 科技装饰线条 */
.article-content::before,
.article-content::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
  left: 50%;
  transform: translateX(-50%);
  animation: scanLine 8s ease-in-out infinite;
}

.article-content::before {
  top: 0;
}

.article-content::after {
  bottom: 0;
  animation-delay: 4s;
}

@keyframes scanLine {
  0%, 100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  50% {
    opacity: 0.8;
    transform: translateX(-50%) translateY(0);
  }
}

.article-title {
  font-size: 2.5rem;
  margin-bottom: 1.5rem;
  font-weight: 700;
  word-break: break-word;
  text-align: center;
  line-height: 1.3;
  position: relative;
  /* 渐变文字效果 */
  background: linear-gradient(90deg, var(--text-primary), var(--highlight), var(--text-accent), var(--text-primary));
  background-size: 200% auto;
  color: transparent;
  background-clip: text;
  -webkit-background-clip: text;
  text-shadow: 0 0 20px rgba(138, 43, 226, 0.2);
  /* 文字流动动画 */
  animation: textGradient 5s ease-in-out infinite;
  margin-top: 1rem;
}

@keyframes textGradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 标题装饰效果 */
.article-title::before,
.article-title::after {
  content: '[';
  position: absolute;
  color: var(--highlight);
  font-size: 1.5em;
  opacity: 0.7;
  animation: bracketPulse 2s ease-in-out infinite;
}

.article-title::before {
  left: -1.2em;
}

.article-title::after {
  content: ']';
  right: -1.2em;
  animation-delay: 0.5s;
}

@keyframes bracketPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.9);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

.article-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 2rem;
  color: var(--highlight);
  font-size: 1rem;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  padding: 1rem;
  background: var(--bg-tertiary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.article-meta .author,
.article-meta .date {
  position: relative;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  background: var(--bg-primary);
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
}

.article-meta .author:hover,
.article-meta .date:hover {
  background: rgba(138, 43, 226, 0.2);
  transform: translateY(-2px);
}

.delete-btn {
  margin-left: 20px;
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
  color: #e0e0ff;
  font-size: 1.1rem;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  width: 100%;
  background: rgba(255, 255, 255, 0.02);
  padding: 1.5rem;
  border-radius: 8px;
  border-left: 3px solid rgba(138, 43, 226, 0.5);
  position: relative;
}

/* 内容区域装饰点 */
.article-body::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(138, 43, 226, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(20px);
  z-index: -1;
}

/* 评论区域样式 - 科技感设计 */
.comments-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: var(--comment-section-border);
  position: relative;
}

/* 评论区装饰元素 */
.comments-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 2px;
  background: var(--comment-section-decoration);
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 1rem;
  background: var(--comment-header-bg);
  border-radius: 8px;
  border: var(--comment-header-border);
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1rem;
  color: var(--comment-sort-text);
}

.sort-controls select {
  padding: 0.4rem 2rem 0.4rem 0.8rem;
  border: var(--comment-sort-select-border);
  border-radius: 8px;
  background: var(--comment-sort-select-bg);
  color: var(--comment-sort-select-color);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%23a855f7' viewBox='0 0 16 16'%3E%3Cpath d='M8 12a.5.5 0 0 0 .5-.5V5.707l2.146 2.147a.5.5 0 0 0 .708-.708l-3-3a.5.5 0 0 0-.708 0l-3 3a.5.5 0 1 0 .708.708L7.5 5.707V11.5a.5.5 0 0 0 .5.5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
}

.sort-controls select:hover {
  border-color: var(--comment-sort-select-hover-border);
  background: var(--comment-sort-select-hover-bg);
  box-shadow: var(--comment-sort-select-hover-shadow);
}

.sort-controls select:focus {
  outline: none;
  border-color: rgba(138, 43, 226, 1);
  box-shadow: 0 0 20px rgba(138, 43, 226, 0.5);
}

/* 下拉菜单选项样式 */
.sort-controls select option {
  background: rgba(16, 16, 36, 1);
  color: #e0e0e0;
  padding: 8px 12px;
}

.sort-controls select option:hover {
  background: rgba(138, 43, 226, 0.2);
}

.sort-controls select option:checked {
  background: rgba(138, 43, 226, 0.3);
  color: #ffffff;
}

.comments-title {
  font-size: 1.5rem;
  margin-bottom: 0;
  color: var(--comment-title-color);
  font-weight: 600;
  text-shadow: var(--comment-title-text-shadow);
  position: relative;
  display: inline-flex;
  align-items: center;
}

.comments-title::before {
  content: '//';
  margin-right: 0.8rem;
  color: var(--comment-title-prefix);
  font-size: 1.2em;
}

/* 评论表单样式 - 科技感设计 */
.comment-form {
  margin-bottom: 2rem;
  position: relative;
  background: var(--comment-form-bg);
  padding: 1.5rem;
  border-radius: 10px;
  border: var(--comment-form-border);
  box-shadow: var(--comment-form-shadow);
}

.comment-form::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  height: 1px;
  background: var(--comment-form-top-border);
}

.comment-form textarea {
  width: 100%;
  padding: 1rem;
  border: var(--comment-textarea-border);
  border-radius: 8px;
  resize: vertical;
  font-size: 1rem;
  line-height: 1.6;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background: var(--comment-textarea-bg);
  color: var(--comment-textarea-color);
  backdrop-filter: blur(5px);
  min-height: 120px;
}

.comment-form textarea:focus {
  outline: none;
  border-color: var(--comment-textarea-focus-border);
  box-shadow: var(--comment-textarea-focus-shadow);
  background: var(--comment-textarea-focus-bg);
}

.comment-form textarea:disabled {
  background-color: rgba(255, 255, 255, 0.03);
  color: #888;
  cursor: not-allowed;
}

.comment-form-actions {
  margin-top: 1rem;
  text-align: right;
}

.submit-comment-btn {
  background: var(--comment-submit-btn-bg);
  color: white;
  border: none;
  padding: 0.6rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: var(--comment-submit-btn-shadow);
}

.submit-comment-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.submit-comment-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--comment-submit-btn-hover-shadow);
}

.submit-comment-btn:hover:not(:disabled)::before {
  left: 100%;
}

.submit-comment-btn:disabled {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 登录提示 - 科技感设计 */
.login-prompt {
  margin-bottom: 2rem;
  padding: 1.2rem;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  text-align: center;
  font-size: 1rem;
  color: var(--highlight);
  position: relative;
  overflow: hidden;
}

.login-prompt::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

.login-prompt a {
  color: var(--highlight);
  text-decoration: none;
  margin: 0 4px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding: 0 2px;
}

.login-prompt a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: var(--highlight);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.login-prompt a:hover {
  color: var(--text-accent);
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.login-prompt a:hover::after {
  transform: scaleX(1);
}

/* 评论列表样式 - 科技感设计 */
.comments-list {
  margin-bottom: 2rem;
  position: relative;
}

.comment-item {
  padding: 1.5rem 0;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
  position: relative;
}

.comment-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -20px;
  width: 4px;
  height: 0;
  background: linear-gradient(to bottom, var(--highlight), transparent);
  transition: height 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.comment-item:hover {
  transform: translateX(5px);
  background: var(--bg-tertiary);
}

.comment-item:hover::before {
  height: 100%;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.8rem;
  font-size: 0.9rem;
  position: relative;
}

.comment-floor {
  background: linear-gradient(90deg, var(--highlight), rgba(138, 43, 226, 0.4));
  color: white;
  padding: 0.2rem 0.6rem;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-right: 1rem;
  font-family: 'Consolas', monospace;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(138, 43, 226, 0.3);
}

.comment-floor::before {
  content: '#';
  margin-right: 2px;
}

.comment-author {
  font-weight: 600;
  color: var(--highlight);
  margin-right: 1rem;
  position: relative;
  padding: 0 0.2rem;
  transition: all 0.3s ease;
}

.comment-author:hover {
  color: var(--text-accent);
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.5);
}

.comment-date {
  color: var(--text-secondary);
  margin-right: auto;
  font-family: 'Consolas', monospace;
  font-size: 0.8rem;
  padding: 0.2rem 0.5rem;
  background: var(--bg-tertiary);
  border-radius: 4px;
}

.delete-comment-btn {
  background: rgba(255, 77, 79, 0.1);
  color: #ff7875;
  border: 1px solid rgba(255, 77, 79, 0.2);
  padding: 0.3rem 0.7rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.delete-comment-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.4s ease;
}

.delete-comment-btn:hover:not(:disabled) {
  background: rgba(255, 77, 79, 0.2);
  border-color: rgba(255, 77, 79, 0.4);
  box-shadow: 0 0 10px rgba(255, 77, 79, 0.2);
}

.delete-comment-btn:hover:not(:disabled)::before {
  left: 100%;
}

.delete-comment-btn:disabled {
  color: var(--text-secondary);
  cursor: not-allowed;
  background: var(--bg-tertiary);
  border-color: var(--border-color);
}

.comment-content {
  font-size: 1rem;
  line-height: 1.7;
  color: var(--text-primary);
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
  z-index: 1;
  padding: 0.5rem 0;
}

/* 无评论提示 - 科技感设计 */
.no-comments {
  text-align: center;
  padding: 4rem 1rem;
  color: var(--text-secondary);
  font-size: 1.1rem;
  position: relative;
  background: var(--bg-tertiary);
  border-radius: 12px;
  border: 1px dashed var(--border-color);
}

.no-comments::before {
  content: '/*';
  font-family: 'Consolas', monospace;
  position: absolute;
  top: 2rem;
  left: 50%;
  transform: translateX(-50%);
  font-size: 1.5rem;
  color: var(--highlight);
}

.no-comments::after {
  content: '*/';
  font-family: 'Consolas', monospace;
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  font-size: 1.5rem;
  color: var(--highlight);
}

.no-comments:hover {
  border-color: var(--highlight);
  background: var(--bg-tertiary);
  box-shadow: 0 0 20px rgba(138, 43, 226, 0.1);
  transition: all 0.3s ease;
}

/* 分页样式 - 科技感设计 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.8rem;
  margin-top: 2.5rem;
  margin-bottom: 2rem;
  position: relative;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.pagination::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

.pagination button {
  background: var(--bg-secondary);
  color: var(--highlight);
  border: 1px solid var(--border-color);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  font-family: 'Consolas', monospace;
  min-width: 2rem;
}

.pagination button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.2), transparent);
  transition: left 0.4s ease;
}

.pagination button:hover:not(:disabled) {
  border-color: var(--highlight);
  background: var(--bg-tertiary);
  color: var(--text-accent);
  box-shadow: 0 0 15px rgba(138, 43, 226, 0.3);
  transform: translateY(-1px);
}

.pagination button:hover:not(:disabled)::before {
  left: 100%;
}

.pagination button:disabled {
  color: var(--text-secondary);
  cursor: not-allowed;
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  transform: none;
  box-shadow: none;
}

.page-info {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-family: 'Consolas', monospace;
  padding: 0 1rem;
  background: var(--bg-tertiary);
  border-radius: 6px;
  border: 1px solid var(--border-color);
}

/* 响应式设计 - 科技感适配 */
/* 平板设备 */
@media (max-width: 992px) {
  .article-detail {
    max-width: 95%;
    margin: 0 auto;
    padding: 0 1rem;
  }
  
  .article-content {
    padding: 2rem 1.5rem;
  }
  
  .article-title {
    font-size: 2rem;
  }
  
  .comments-section {
    margin-top: 2.5rem;
  }
  
  /* 调整评论区域布局 */
  .comment-header {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .comment-floor {
    margin-bottom: 0.5rem;
  }
}

/* 移动设备 - 中屏幕 */
@media (max-width: 768px) {
  .article-detail {
    max-width: 100%;
    margin: 0;
    padding: 0 1rem;
    border: none;
  }
  
  .article-content {
    padding: 1.5rem 1rem;
    margin: 1rem 0;
    border-radius: 8px;
  }
  
  .article-title {
    font-size: 1.8rem;
    line-height: 1.3;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.8rem;
  }
  
  .comments-section {
    margin-top: 2rem;
  }
  
  /* 调整评论和分页样式 */
  .comment-item {
    padding: 1.2rem 0;
  }
  
  .comment-item::before {
    left: -15px;
  }
  
  .comment-floor {
    font-size: 0.7rem;
    padding: 0.2rem 0.4rem;
  }
  
  .comment-date {
    font-size: 0.7rem;
    margin-right: 0;
    margin-top: 0.3rem;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 0.5rem;
    padding: 0.8rem;
  }
  
  .pagination button {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
    min-width: auto;
  }
  
  .page-info {
    font-size: 0.8rem;
    padding: 0 0.5rem;
    margin: 0.5rem 0;
  }
  
  /* 调整编辑器和表单 */
  .comment-form {
    padding: 1rem;
  }
  
  .comment-form textarea {
    min-height: 100px;
    font-size: 0.9rem;
  }
  
  .submit-comment-btn {
    padding: 0.5rem 1.2rem;
    font-size: 0.9rem;
  }
}

/* 移动设备 - 小屏幕 */
@media (max-width: 480px) {
  .article-detail {
    padding: 0 0.8rem;
  }
  
  .article-content {
    padding: 1.2rem 0.8rem;
  }
  
  .article-title {
    font-size: 1.5rem;
    margin-bottom: 1rem;
  }
  
  .article-meta {
    font-size: 0.9rem;
  }
  
  /* 优化内容样式 */
  :deep(.article-body) h1 {
    font-size: 1.5rem;
  }
  
  :deep(.article-body) h2 {
    font-size: 1.3rem;
  }
  
  :deep(.article-body) h3 {
    font-size: 1.1rem;
  }
  
  :deep(.article-body) p {
    font-size: 0.95rem;
    text-indent: 1.5em;
  }
  
  /* 优化评论区样式 */
  .comments-section h3 {
    font-size: 1.3rem;
  }
  
  .comment-content {
    font-size: 0.95rem;
  }
  
  .delete-comment-btn {
    padding: 0.2rem 0.5rem;
    font-size: 0.75rem;
  }
  
  /* 无评论提示 */
  .no-comments {
    padding: 3rem 0.8rem;
    font-size: 1rem;
  }
  
  .no-comments::before,
  .no-comments::after {
    font-size: 1.2rem;
  }
  
  /* 简化动画效果，提升移动端性能 */
  .article-title {
    animation: none;
  }
  
  .comment-item:hover {
    transform: none;
  }
  
  .pagination button:hover {
    transform: none;
  }
  
  .submit-comment-btn:hover {
    transform: none;
  }
}


/* 使用深度选择器确保样式能够穿透到v-html渲染的内容 */
:deep(.article-body) h1,
:deep(.article-body) h2,
:deep(.article-body) h3 {
  margin: 2rem 0 1.5rem;
  color: var(--highlight);
  word-break: break-word;
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.3);
  position: relative;
}

:deep(.article-body) h1 {
  font-size: 1.8rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-color);
}

:deep(.article-body) h2 {
  font-size: 1.5rem;
  padding-left: 1rem;
  border-left: 4px solid var(--highlight);
}

:deep(.article-body) h3 {
  font-size: 1.3rem;
  color: var(--highlight);
}

/* 段落缩进 - 必须使用深度选择器 */
:deep(.article-body) p {
  margin-bottom: 1.2rem;
  word-break: break-word;
  text-indent: 2em;
  position: relative;
  z-index: 1;
  color: var(--text-primary);
}

/* 图片样式 - 科技感边框 */
:deep(.article-body) img {
  max-width: 100% !important;
  width: 100% !important;
  height: auto !important;
  margin: 1.5rem auto;
  border-radius: 8px;
  display: block;
  object-fit: contain;
  overflow: hidden;
  box-sizing: border-box;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow);
  transition: all 0.3s ease;
}

:deep(.article-body) img:hover {
  transform: scale(1.02);
  box-shadow: 0 0 20px rgba(138, 43, 226, 0.4);
}

/* 处理可能从富文本编辑器过来的图片容器 */
:deep(.article-body *) {
  max-width: 100% !important;
  height: auto !important;
  box-sizing: border-box;
}

/* 补充Quill编辑器默认样式 - 使用深度选择器 */
:deep(.article-body .ql-indent-1) {
  padding-left: 32px !important;
}

:deep(.article-body .ql-indent-2) {
  padding-left: 64px !important;
}

:deep(.article-body .ql-indent-3) {
  padding-left: 96px !important;
}

:deep(.article-body .ql-indent-4) {
  padding-left: 128px !important;
}

/* 文本对齐样式 */
:deep(.article-body .ql-align-center) {
  text-align: center !important;
}

:deep(.article-body .ql-align-right) {
  text-align: right !important;
}

:deep(.article-body .ql-align-justify) {
  text-align: justify !important;
}

/* 引用块增强样式 - 科技感设计 */
:deep(.article-body blockquote) {
  border-left: 4px solid var(--highlight) !important;
  margin: 1.5rem 0 !important;
  padding: 1.2rem 1.5rem !important;
  color: var(--text-primary) !important;
  background: var(--bg-tertiary) !important;
  border-radius: 0 8px 8px 0 !important;
  border: 1px solid var(--border-color) !important;
  position: relative;
  font-style: italic;
}

:deep(.article-body blockquote)::before {
  content: '"';
  position: absolute;
  top: 0;
  left: 10px;
  font-size: 3rem;
  color: var(--highlight);
  opacity: 0.3;
  line-height: 1;
}

/* 表格样式增强 - 科技感设计 */
:deep(.article-body table) {
  width: 100% !important;
  overflow-x: auto !important;
  display: block !important;
  border-collapse: collapse !important;
  margin: 1.5rem 0 !important;
  border: 1px solid var(--border-color) !important;
  border-radius: 8px !important;
  overflow: hidden !important;
}

:deep(.article-body table th) {
  background: var(--bg-tertiary) !important;
  border: 1px solid var(--border-color) !important;
  padding: 0.8rem 1rem !important;
  text-align: left !important;
  color: var(--highlight) !important;
  font-weight: 600 !important;
  position: relative;
}

:deep(.article-body table th::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

:deep(.article-body table td) {
  border: 1px solid var(--border-color) !important;
  padding: 0.8rem 1rem !important;
  text-align: left !important;
  color: var(--text-primary) !important;
  background: var(--bg-secondary) !important;
  transition: background-color 0.3s ease;
}

:deep(.article-body table tr:hover td) {
  background: var(--bg-tertiary) !important;
}

/* 列表项增强 */
:deep(.article-body li.ql-indent-1) {
  padding-left: 0 !important;
}

:deep(.article-body li.ql-indent-2) {
  padding-left: 0 !important;
}

/* 代码块样式 - 高科技终端感 */
:deep(.article-body pre) {
  background: var(--bg-tertiary) !important;
  color: var(--highlight) !important;
  padding: 1.5rem !important;
  border-radius: 8px !important;
  overflow-x: auto !important;
  margin: 1.5rem 0 !important;
  display: block !important;
  max-width: 100% !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
  border: 1px solid var(--border-color) !important;
  position: relative;
  box-shadow: var(--shadow);
}

/* 代码块装饰元素 */
:deep(.article-body pre)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
  animation: codeLine 3s linear infinite;
}

@keyframes codeLine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

/* 行内代码样式 */
:deep(.article-body code) {
  background-color: var(--bg-tertiary) !important;
  color: var(--highlight) !important;
  padding: 2px 6px !important;
  border-radius: 4px !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 0.9em !important;
  border: 1px solid var(--border-color) !important;
}

/* 列表样式 - 科技感设计 */
:deep(.article-body ul),
:deep(.article-body ol) {
  margin: 1.5rem 0 !important;
  padding-left: 2rem !important;
}

:deep(.article-body ul) {
  list-style-type: none !important;
  position: relative;
}

:deep(.article-body ul li)::before {
  content: '⟐';
  color: var(--highlight);
  position: absolute;
  left: 0;
  font-size: 0.8em;
}

:deep(.article-body ol) {
  list-style-type: none !important;
  counter-reset: item;
  position: relative;
}

:deep(.article-body ol li) {
  counter-increment: item;
  position: relative;
  padding-left: 1.5rem;
}

:deep(.article-body ol li)::before {
  content: counter(item);
  position: absolute;
  left: 0;
  top: 0;
  color: var(--highlight);
  font-size: 0.8em;
  background: var(--bg-tertiary);
  width: 1.2rem;
  height: 1.2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.article-body li) {
  margin-bottom: 0.8rem !important;
  word-break: break-word !important;
  text-align: justify !important;
  color: var(--text-primary) !important;
}

/* 直接添加Quill编辑器核心样式的深度选择器版本 */
:deep(ol.ql-list),
:deep(ul.ql-list) {
  padding-left: 2rem !important;
}

/* 确保代码块的样式正确显示 */
:deep(.ql-syntax) {
  background: var(--bg-tertiary) !important;
  color: var(--highlight) !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  border-radius: 8px !important;
  padding: 1.5rem !important;
  margin: 1.5rem 0 !important;
  border: 1px solid var(--border-color) !important;
  box-shadow: var(--shadow);
}
</style>


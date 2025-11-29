<template>
  <div class="article-detail">
    <div v-if="loading" class="loading">
      <div class="skeleton-container">
        <div class="skeleton-title"></div>
        <div class="skeleton-meta"></div>
        <div class="skeleton-body">
          <div class="skeleton-paragraph"></div>
          <div class="skeleton-paragraph"></div>
          <div class="skeleton-paragraph"></div>
          <div class="skeleton-image"></div>
          <div class="skeleton-paragraph"></div>
          <div class="skeleton-paragraph"></div>
        </div>
      </div>
    </div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="article" class="article-wrapper">
      <!-- 文章头部区域 -->
      <article class="article-content">
        <h1 class="article-title">{{ article.title }}</h1>
        
        <!-- 文章元信息 -->
        <div class="article-meta">
          <span class="author">
            <i class="icon-user"></i>
            {{ article.author?.nickname || '匿名用户' }}
          </span>
          <span class="date">
            <i class="icon-date"></i>
            {{ formatDate(article.updatedAt) }}
          </span>
        </div>
        
        <!-- 文章正文 -->
        <div class="article-body" v-html="article.content"></div>
        
        <!-- 文章底部信息 -->
        <div class="article-footer">
          <div class="article-tags" v-if="article.tags && article.tags.length > 0">
            <span class="tag" v-for="tag in article.tags" :key="tag.id">
              # {{ tag.name }}
            </span>
          </div>
        </div>
      </article>
      
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
.article-detail {
  max-width: 860px;
  margin: 0 auto;
  width: 100%;
  padding: 20px 16px;
  overflow-x: hidden;
}

.article-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.loading,
.error,
.not-found {
  text-align: center;
  padding: 100px 20px;
  font-size: 16px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error {
  color: #ff4d4f;
  background-color: #fff1f0;
  border: 1px solid #ffccc7;
}

.article-content {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 40px;
  overflow-x: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation: fadeIn 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
}

.article-content:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 闪烁动画 */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.article-title {
  font-size: 2.2rem;
  margin-bottom: 24px;
  color: #262626;
  font-weight: 600;
  word-break: break-word;
  text-align: center;
  line-height: 1.3;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  opacity: 0;
  animation-fill-mode: forwards;
}

/* 淡入上移动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.article-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
  color: #8c8c8c;
  font-size: 15px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.2s both;
  opacity: 0;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.article-meta i {
  width: 16px;
  height: 16px;
  opacity: 0.7;
}

/* 为图标添加简单的占位样式 */
.icon-user::before { content: "👤"; }
.icon-date::before { content: "📅"; }


.article-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.4s both;
  opacity: 0;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag {
  display: inline-block;
  padding: 6px 14px;
  background-color: #f0f7ff;
  color: #1890ff;
  border-radius: 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.tag:hover {
  background-color: #1890ff;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
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
  color: #333;
  font-size: 17px;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.3s both;
  opacity: 0;
}

/* 评论区域样式 */
.comments-section {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 40px;
  overflow-x: hidden;
  animation: fadeInUp 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.5s both;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  opacity: 0;
}

.comments-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.6s both;
  opacity: 0;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.sort-controls select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background-color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sort-controls select:hover {
  border-color: #40a9ff;
  transform: translateY(-1px);
}

.sort-controls select:focus {
  outline: none;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.comments-title {
  font-size: 1.5rem;
  margin-bottom: 32px;
  color: #262626;
  font-weight: 600;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/* 评论表单样式 */
.comment-form {
  margin-bottom: 32px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.7s both;
  opacity: 0;
}

.comment-form textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  resize: vertical;
  font-size: 15px;
  line-height: 1.6;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background-color: #fafafa;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #1890ff;
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  transform: translateY(-1px);
}

.comment-form textarea:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
  opacity: 0.7;
}

.comment-form-actions {
  margin-top: 16px;
  text-align: right;
}

.submit-comment-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 10px 28px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.submit-comment-btn:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.submit-comment-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-comment-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 登录提示 */
.login-prompt {
  margin-bottom: 32px;
  padding: 16px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
  color: #52c41a;
}

.login-prompt a {
  color: #1890ff;
  text-decoration: none;
  margin: 0 4px;
}

.login-prompt a:hover {
  text-decoration: underline;
}

/* 评论列表样式 */
.comments-list {
  margin-bottom: 24px;
}

.comment-item {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation: fadeInUp 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation-fill-mode: both;
}

.comment-item:hover {
  background-color: #fafafa;
  padding-left: 8px;
  margin-left: -8px;
  border-left: 3px solid #1890ff;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.comment-floor {
  background-color: #1890ff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 12px;
}

.comment-author {
  font-weight: 600;
  color: #262626;
  margin-right: 12px;
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.comment-date {
  color: #8c8c8c;
  margin-right: auto;
  font-style: italic;
}

.delete-comment-btn {
  background-color: transparent;
  color: #ff4d4f;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  opacity: 0.7;
}

.delete-comment-btn:hover:not(:disabled) {
  background-color: rgba(255, 77, 79, 0.1);
  opacity: 1;
  transform: translateY(-1px);
}

.delete-comment-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.comment-content {
  font-size: 14px;
  line-height: 1.7;
  color: #333;
  word-break: break-word;
  white-space: pre-wrap;
  padding: 12px;
  background-color: #fff;
  border-radius: 6px;
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
}

.comment-item:hover .comment-content {
  border-left-color: #722ed1;
  transform: translateX(2px);
}

/* 无评论提示 */
.no-comments {
  text-align: center;
  padding: 60px 0;
  color: #8c8c8c;
  font-size: 16px;
  animation: fadeIn 0.8s ease-out;
}

/* 为评论项添加交错动画延迟 */
.comment-item:nth-child(1) { animation-delay: 0.1s; }
.comment-item:nth-child(2) { animation-delay: 0.2s; }
.comment-item:nth-child(3) { animation-delay: 0.3s; }
.comment-item:nth-child(4) { animation-delay: 0.4s; }
.comment-item:nth-child(5) { animation-delay: 0.5s; }
.comment-item:nth-child(n+6) { animation-delay: 0.6s; }

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  animation: fadeInUp 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.8s both;
  opacity: 0;
}

.pagination button {
  background-color: #fff;
  color: #333;
  border: 1px solid #d9d9d9;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.pagination button:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination button:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-detail {
    padding: 10px;
  }
  
  .article-content {
    padding: 24px 16px;
  }
  
  .article-title {
    font-size: 24px;
    margin-bottom: 20px;
  }
  
  .article-meta {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    padding: 12px;
  }
  
  .article-body {
    font-size: 15px;
    line-height: 1.7;
  }
  
  .article-footer {
    margin-top: 32px;
    padding-top: 20px;
  }
  
  .article-tags {
    gap: 8px;
  }
  
  .tag {
    padding: 4px 12px;
    font-size: 13px;
  }
  
  .comments-section {
    margin-top: 32px;
    padding: 24px 16px;
  }
  
  .comments-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 24px;
  }
  
  .comments-title {
    font-size: 1.25rem;
    margin-bottom: 0;
  }
  
  .comment-form textarea {
    padding: 12px;
    font-size: 14px;
  }
  
  .comment-form-actions {
    margin-top: 12px;
  }
  
  .submit-comment-btn {
    padding: 8px 24px;
    font-size: 14px;
  }
  
  .comment-item {
    padding: 16px 0;
  }
  
  .comment-header {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .comment-floor {
    margin-right: 8px;
  }
  
  .comment-content {
    font-size: 13px;
    line-height: 1.6;
  }
  
  .pagination {
    gap: 12px;
    margin-top: 20px;
  }
  
  .pagination button {
    padding: 5px 12px;
    font-size: 13px;
  }
  
  .skeleton-container {
    padding: 24px 16px;
  }
  
  .skeleton-title {
    height: 36px;
    margin-bottom: 20px;
  }
  
  .skeleton-meta {
    height: 20px;
    margin-bottom: 24px;
  }
  
  .skeleton-image {
    height: 160px;
    margin: 16px 0;
  }
  
  /* 深度选择器样式的响应式调整 */
  :deep(.article-body) h1 {
    font-size: 1.5rem;
    margin: 32px 0 16px;
  }
  
  :deep(.article-body) h2 {
    font-size: 1.25rem;
    margin: 28px 0 14px;
  }
  
  :deep(.article-body) h3 {
    font-size: 1.1rem;
    margin: 24px 0 12px;
  }
  
  :deep(.article-body) p {
    margin-bottom: 16px;
    text-indent: 1.5em;
  }
  
  :deep(.article-body) img {
    margin: 16px auto;
  }
  
  :deep(.article-body blockquote) {
    margin: 16px 0 !important;
    padding: 12px 16px !important;
  }
  
  :deep(.article-body pre) {
    padding: 16px !important;
    font-size: 13px !important;
  }
  
  :deep(.article-body ul),
  :deep(.article-body ol) {
    padding-left: 24px !important;
  }
  
  :deep(.article-body li) {
    margin-bottom: 8px !important;
  }
}

@media (max-width: 480px) {
  .article-detail {
    padding: 5px;
  }
  
  .article-content {
    padding: 20px 12px;
  }
  
  .article-title {
    font-size: 20px;
    margin-bottom: 16px;
  }
  
  .article-body {
    font-size: 14px;
  }
  
  .comments-section {
    padding: 20px 12px;
  }
  
  .comments-title {
    font-size: 1.1rem;
  }
  
  .comment-item:hover {
    padding-left: 4px;
    margin-left: -4px;
  }
  
  .comment-header {
    font-size: 13px;
  }
  
  .pagination {
    flex-direction: column;
  }
  
  .pagination button {
    width: 100%;
    max-width: 200px;
  }
  
  .skeleton-title {
    height: 32px;
  }
  
  .skeleton-image {
    height: 140px;
  }
  
  :deep(.article-body) h1 {
    font-size: 1.3rem;
  }
  
  :deep(.article-body) h2 {
    font-size: 1.15rem;
  }
  
  :deep(.article-body) p {
    text-indent: 1.2em;
  }
}

/* 使用深度选择器确保样式能够穿透到v-html渲染的内容 */
:deep(.article-body) h1 {
  font-size: 1.8rem;
  margin: 40px 0 20px;
  color: #171717;
  font-weight: 600;
  word-break: break-word;
  line-height: 1.4;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

:deep(.article-body) h2 {
  font-size: 1.5rem;
  margin: 36px 0 18px;
  color: #262626;
  font-weight: 600;
  word-break: break-word;
  line-height: 1.4;
  padding-left: 12px;
  border-left: 4px solid #1890ff;
}

:deep(.article-body) h3 {
  font-size: 1.25rem;
  margin: 32px 0 16px;
  color: #333;
  font-weight: 600;
  word-break: break-word;
  line-height: 1.4;
}

/* 段落样式优化 */
:deep(.article-body) p {
  margin-bottom: 20px;
  word-break: break-word;
  text-indent: 2em;
  color: #434343;
  line-height: 1.8;
  text-align: justify;
}

/* 图片样式优化 */
:deep(.article-body) img {
  max-width: 100% !important;
  width: auto !important;
  height: auto !important;
  margin: 24px auto;
  border-radius: 8px;
  display: block;
  object-fit: contain;
  overflow: hidden;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

:deep(.article-body) img:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
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

/* 引用块样式增强 */
:deep(.article-body blockquote) {
  border-left: 4px solid #1890ff !important;
  margin: 24px 0 !important;
  padding: 16px 20px !important;
  color: #595959 !important;
  background-color: #f0f7ff !important;
  border-radius: 0 8px 8px 0 !important;
  font-style: italic;
  line-height: 1.6 !important;
}

:deep(.article-body blockquote p) {
  text-indent: 0 !important;
  margin-bottom: 8px !important;
}

:deep(.article-body blockquote p:last-child) {
  margin-bottom: 0 !important;
}

/* 表格样式增强 */
:deep(.article-body table) {
  width: 100% !important;
  overflow-x: auto !important;
  display: block !important;
  border-collapse: collapse !important;
  margin: 24px 0 !important;
  border-radius: 8px !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08) !important;
}

:deep(.article-body table th),
:deep(.article-body table td) {
  border: 1px solid #f0f0f0 !important;
  padding: 12px 16px !important;
  text-align: left !important;
  transition: background-color 0.2s ease !important;
}

:deep(.article-body table th) {
  background-color: #fafafa !important;
  font-weight: 600 !important;
  color: #262626 !important;
  font-size: 15px !important;
}

:deep(.article-body table tr:hover td) {
  background-color: #fafafa !important;
}

/* 列表样式优化 */
:deep(.article-body ul),
:deep(.article-body ol) {
  margin: 20px 0 !important;
  padding-left: 32px !important;
  line-height: 1.8 !important;
}

:deep(.article-body ul) {
  list-style-type: disc !important;
}

:deep(.article-body ol) {
  list-style-type: decimal !important;
}

:deep(.article-body li) {
  margin-bottom: 12px !important;
  word-break: break-word !important;
  text-align: justify !important;
  color: #434343 !important;
}

/* 代码块样式增强 */
:deep(.article-body pre) {
  background-color: #1e1e1e !important;
  color: #d4d4d4 !important;
  padding: 24px !important;
  border-radius: 8px !important;
  overflow-x: auto !important;
  margin: 24px 0 !important;
  display: block !important;
  max-width: 100% !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 14px !important;
  line-height: 1.6 !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

:deep(.article-body code) {
  background-color: #f5f5f5 !important;
  padding: 4px 6px !important;
  border-radius: 4px !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 0.9em !important;
  color: #e83e8c !important;
  word-break: break-word !important;
}

/* 列表样式 - 使用深度选择器并增加优先级 */
:deep(.article-body ul),
:deep(.article-body ol) {
  margin: 16px 0 !important;
  padding-left: 32px !important;
}

:deep(.article-body ul) {
  list-style-type: disc !important;
}

:deep(.article-body ol) {
  list-style-type: decimal !important;
}

:deep(.article-body li) {
  margin-bottom: 8px !important;
  word-break: break-word !important;
  text-align: justify !important;
}

/* 直接添加Quill编辑器核心样式的深度选择器版本 */
:deep(ol.ql-list),
:deep(ul.ql-list) {
  padding-left: 32px !important;
}

:deep(ol.ql-list) {
  list-style-type: decimal !important;
}

:deep(ul.ql-list) {
  list-style-type: disc !important;
}

/* 确保代码块的黑色背景和文字颜色正确显示 */
:deep(.ql-syntax) {
  background-color: #1e1e1e !important;
  color: #d4d4d4 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  border-radius: 4px !important;
  padding: 16px !important;
  margin: 16px 0 !important;
}

/* 骨架屏样式 */
.skeleton-container {
  width: 100%;
  max-width: 800px;
  padding: 40px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.skeleton-title {
  width: 80%;
  height: 48px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 8px;
  margin: 0 auto 24px;
  animation: shimmer 1.5s infinite;
}

.skeleton-meta {
  width: 60%;
  height: 24px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin: 0 auto 32px;
  animation: shimmer 1.5s infinite;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skeleton-paragraph {
  width: 100%;
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}

.skeleton-paragraph:nth-child(2) {
  width: 90%;
}

.skeleton-paragraph:nth-child(3) {
  width: 85%;
}

.skeleton-paragraph:nth-child(5) {
  width: 95%;
}

.skeleton-paragraph:nth-child(6) {
  width: 80%;
}

.skeleton-image {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 8px;
  margin: 20px 0;
  animation: shimmer 1.5s infinite;
}
</style>
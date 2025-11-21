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
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  overflow-x: hidden;
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
  overflow-x: hidden;
}

.article-title {
  font-size: 32px;
  margin-bottom: 20px;
  color: #333;
  font-weight: 500;
  word-break: break-word;
  text-align: center;
}

.article-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  color: #666;
  font-size: 14px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
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
  font-size: 16px;
  word-break: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  width: 100%;
}

/* 评论区域样式 */
.comments-section {
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.sort-controls select {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.sort-controls select:hover {
  border-color: #40a9ff;
}

.sort-controls select:focus {
  outline: none;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.comments-title {
  font-size: 20px;
  margin-bottom: 24px;
  color: #333;
  font-weight: 500;
}

/* 评论表单样式 */
.comment-form {
  margin-bottom: 32px;
}

.comment-form textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
  font-size: 14px;
  line-height: 1.5;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #1890ff;
}

.comment-form textarea:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.comment-form-actions {
  margin-top: 12px;
  text-align: right;
}

.submit-comment-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.submit-comment-btn:hover:not(:disabled) {
  background-color: #40a9ff;
}

.submit-comment-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
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
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
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
  font-weight: 500;
  color: #333;
  margin-right: 12px;
}

.comment-date {
  color: #999;
  margin-right: auto;
}

.delete-comment-btn {
  background-color: transparent;
  color: #ff4d4f;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.delete-comment-btn:hover:not(:disabled) {
  background-color: #fff1f0;
}

.delete-comment-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  word-break: break-word;
  white-space: pre-wrap;
}

/* 无评论提示 */
.no-comments {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.pagination button {
  background-color: #fff;
  color: #333;
  border: 1px solid #d9d9d9;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
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
  .article-content {
    padding: 24px 16px;
  }
  
  .article-title {
    font-size: 24px;
  }
  
  .comments-section {
    margin-top: 32px;
  }
}


/* 使用深度选择器确保样式能够穿透到v-html渲染的内容 */
:deep(.article-body) h1,
:deep(.article-body) h2,
:deep(.article-body) h3 {
  margin: 24px 0 16px;
  color: #262626;
  word-break: break-word;
}

/* 段落缩进 - 必须使用深度选择器 */
:deep(.article-body) p {
  margin-bottom: 16px;
  word-break: break-word;
  text-indent: 2em;
}

/* 图片样式 */
:deep(.article-body) img {
  max-width: 100% !important;
  width: 100% !important;
  height: auto !important;
  margin: 16px auto;
  border-radius: 4px;
  display: block;
  object-fit: contain;
  overflow: hidden;
  box-sizing: border-box;
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

/* 引用块增强样式 */
:deep(.article-body blockquote) {
  border-left: 4px solid #1890ff !important;
  margin: 16px 0 !important;
  padding: 12px 16px !important;
  color: #666 !important;
  background-color: #f0f8ff !important;
  border-radius: 0 4px 4px 0 !important;
}

/* 表格样式增强 */
:deep(.article-body table) {
  width: 100% !important;
  overflow-x: auto !important;
  display: block !important;
  border-collapse: collapse !important;
  margin: 16px 0 !important;
}

:deep(.article-body table th),
:deep(.article-body table td) {
  border: 1px solid #d9d9d9 !important;
  padding: 8px 12px !important;
  text-align: left !important;
}

:deep(.article-body table th) {
  background-color: #fafafa !important;
  font-weight: 500 !important;
}

/* 列表项增强 */
:deep(.article-body li.ql-indent-1) {
  padding-left: 0 !important;
}

:deep(.article-body li.ql-indent-2) {
  padding-left: 0 !important;
}

/* 代码块黑色背景 - 使用深度选择器并增加优先级 */
:deep(.article-body pre) {
  background-color: #1e1e1e !important;
  color: #d4d4d4 !important;
  padding: 16px !important;
  border-radius: 4px !important;
  overflow-x: auto !important;
  margin: 16px 0 !important;
  display: block !important;
  max-width: 100% !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 14px !important;
  line-height: 1.5 !important;
}

:deep(.article-body code) {
  background-color: #f5f5f5 !important;
  padding: 2px 4px !important;
  border-radius: 4px !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  font-size: 0.9em !important;
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
</style>
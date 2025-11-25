<template>
  <div class="admin-container">
    <!-- 左侧导航栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理中心</h2>
      </div>
      <nav class="sidebar-nav">
        <ul>
          <li class="nav-item" :class="{ active: activeTab === 'statistics' }">
            <a @click="switchTab('statistics')">
              <span class="nav-icon">📊</span>
              <span class="nav-text">统计概览</span>
            </a>
          </li>
          <li class="nav-item" :class="{ active: activeTab === 'articles' }">
            <a @click="switchTab('articles')">
              <span class="nav-icon">📝</span>
              <span class="nav-text">文章管理</span>
            </a>
          </li>
          <li class="nav-item" :class="{ active: activeTab === 'comments' }">
            <a @click="switchTab('comments')">
              <span class="nav-icon">💬</span>
              <span class="nav-text">评论管理</span>
            </a>
          </li>
          <li class="nav-item" :class="{ active: activeTab === 'users' }">
            <a @click="switchTab('users')">
              <span class="nav-icon">👥</span>
              <span class="nav-text">用户管理</span>
            </a>
          </li>
        </ul>
      </nav>
    </aside>
    
    <!-- 主内容区域 -->
    <main class="admin-main">
      <!-- 统计概览 -->
      <div v-if="activeTab === 'statistics'" class="statistics-overview">
        <div class="container">
          <h1 class="dashboard-title">统计概览</h1>
          
          <!-- 统计信息卡片区域 - 更现代的布局 -->
          <div class="overview-cards">
            <!-- 文章相关统计 -->
            <div class="stats-group">
              <h2 class="group-title">📝 文章统计</h2>
              <div class="cards-grid">
                <div class="stat-card primary">
                  <div class="stat-icon">📚</div>
                  <div class="stat-content">
                    <div class="stat-number">{{ articleCount }}</div>
                    <div class="stat-label">总文章数</div>
                  </div>
                </div>
                <div class="stat-card success">
                  <div class="stat-icon">📈</div>
                  <div class="stat-content">
                    <div class="stat-number">{{ todayArticleCount }}</div>
                    <div class="stat-label">今日新增</div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 评论相关统计 -->
            <div class="stats-group">
              <h2 class="group-title">💬 评论统计</h2>
              <div class="cards-grid">
                <div class="stat-card secondary">
                  <div class="stat-icon">💭</div>
                  <div class="stat-content">
                    <div class="stat-number">{{ commentCount }}</div>
                    <div class="stat-label">总评论数</div>
                  </div>
                </div>
                <div class="stat-card warning">
                  <div class="stat-icon">🔥</div>
                  <div class="stat-content">
                    <div class="stat-number">{{ todayCommentCount }}</div>
                    <div class="stat-label">今日新增</div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 用户相关统计 -->
            <div class="stats-group">
              <h2 class="group-title">👥 用户统计</h2>
              <div class="cards-grid">
                <div class="stat-card info">
                  <div class="stat-icon">👪</div>
                  <div class="stat-content">
                    <div class="stat-number">{{ userCount }}</div>
                    <div class="stat-label">总用户数</div>
                  </div>
                </div>
                <div class="stat-card danger">
                  <div class="stat-icon">✨</div>
                  <div class="stat-content">
                    <div class="stat-number">{{ activeUserCount }}</div>
                    <div class="stat-label">活跃用户数</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 更新时间信息 -->
          <div class="update-info">
            <p>数据最后更新时间: {{ lastUpdateTime }}</p>
          </div>
        </div>
      </div>
      
      <!-- 文章管理 -->
      <div v-if="activeTab === 'articles'" class="admin-dashboard">
        <div class="container">
          <h1 class="dashboard-title">文章管理</h1>
          
          <!-- 统计信息卡片 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-number">{{ articleCount }}</div>
              <div class="stat-label">总文章数</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ todayArticleCount }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
          
          <!-- 文章管理区域 -->
          <div class="article-management">
        <div class="management-header">
          <h2>文章列表</h2>
          <div class="search-bar">
            <select v-model="articleSearchType" @change="handleSearch" style="margin-right: 10px; padding: 6px;">
              <option value="title_content">标题/内容</option>
              <option value="author">作者名</option>
              <option value="all">全部</option>
            </select>
            <input 
              type="text" 
              v-model="searchKeyword" 
              placeholder="搜索文章..." 
              @input="handleSearch"
              class="search-input"
            >
          </div>
        </div>
            
            <!-- 文章列表 -->
            <div class="article-list">
              <div v-if="loading" class="loading">加载中...</div>
              
              <div v-else-if="filteredArticles.length === 0" class="empty-state">
                <p>暂无文章</p>
              </div>
              
              <div v-else class="article-table">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>标题</th>
                      <th>作者</th>
                      <th>发布时间</th>
                      <th>状态</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="article in filteredArticles" :key="article.id">
                      <td>{{ article.id }}</td>
                      <td class="article-title">{{ article.title }}</td>
                      <td>{{ article.author.nickname || article.author.username }}</td>
                      <td>{{ formatDate(article.createdAt) }}</td>
                      <td>
                        <span class="status-badge" :class="getArticleStatus(article)">
                          {{ getArticleStatus(article) === 'published' ? '已发布' : '草稿' }}
                        </span>
                      </td>
                      <td class="action-buttons">
                        <router-link 
                          :to="`/create/${article.id}`" 
                          class="btn btn-edit"
                        >
                          编辑
                        </router-link>
                        <button 
                          class="btn btn-delete" 
                          @click="deleteArticle(article.id, article.title)"
                        >
                          删除
                        </button>
                        <router-link 
                          :to="`/article/${article.id}`" 
                          class="btn btn-view" 
                          target="_blank"
                        >
                          查看
                        </router-link>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <!-- 分页控制 -->
              <div v-if="!loading && filteredArticles.length > 0" class="pagination">
                <button 
                  class="pagination-btn" 
                  @click="goToPage(currentPage - 1)" 
                  :disabled="currentPage === 1"
                >
                  上一页
                </button>
                <span class="pagination-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
                <button 
                  class="pagination-btn" 
                  @click="goToPage(currentPage + 1)" 
                  :disabled="currentPage === totalPages"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 评论管理 -->
      <div v-if="activeTab === 'comments'" class="comments-management">
        <div class="container">
          <h1 class="dashboard-title">评论管理</h1>
          
          <!-- 统计信息卡片 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-number">{{ commentCount }}</div>
              <div class="stat-label">总评论数</div>
            </div>
            <div class="stat-card">
              <div class="stat-number">{{ todayCommentCount }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
          
          <!-- 评论列表 -->
          <div class="comments-container">
            <div class="management-header">
              <h2>评论列表</h2>
              <div class="search-bar">
                <select v-model="commentSearchType" @change="handleCommentSearch" style="margin-right: 10px; padding: 6px;">
                  <option value="content">评论内容</option>
                  <option value="author">评论者</option>
                  <option value="all">全部</option>
                </select>
                <input 
                  type="text" 
                  v-model="commentSearchKeyword" 
                  placeholder="搜索评论..." 
                  @input="handleCommentSearch"
                  class="search-input"
                >
              </div>
            </div>
            <div class="comments-list">
              <div class="comments-list-header">
                <div class="header-item id">ID</div>
                <div class="header-item content">评论内容</div>
                <div class="header-item author">评论者</div>
                <div class="header-item article">所属文章</div>
                <div class="header-item date">评论时间</div>
                <div class="header-item actions">操作</div>
              </div>
              
              <div v-if="comments.length === 0" class="empty-state">
                暂无评论数据
              </div>
              
              <div 
                v-for="comment in comments" 
                :key="comment.id" 
                class="comment-item"
              >
                <div class="item id">{{ comment.id }}</div>
                <div class="item content">{{ comment.content }}</div>
                <div class="item author">{{ comment.userNickname }}</div>
                <div class="item article" @click="navigateToArticle(comment.articleId)">
                  <span class="article-link">文章 #{{ comment.articleId }}</span>
                </div>
                <div class="item date">{{ formatDate(comment.createdAt) }}</div>
                <div class="item actions">
                  <button class="delete-btn" @click="deleteComment(comment.id, comment.content)">删除</button>
                </div>
              </div>
            </div>
            
            <!-- 评论分页 -->
            <div class="pagination" v-if="comments.length > 0">
              <button 
                @click="changeCommentPage(commentPage - 1)"
                :disabled="commentPage === 1"
                class="page-btn"
              >
                上一页
              </button>
              <span class="page-info">第 {{ commentPage }} / {{ commentTotalPages }} 页</span>
              <button 
                @click="changeCommentPage(commentPage + 1)"
                :disabled="commentPage === commentTotalPages"
                class="page-btn"
              >
                下一页
              </button>
              <div class="page-size">
                <span>每页显示：</span>
                <select v-model="commentPageSize" @change="handleCommentPageSizeChange">
                  <option :value="10">10</option>
                  <option :value="20">20</option>
                  <option :value="50">50</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 用户管理 -->
      <div v-if="activeTab === 'users'" class="users-management">
        <div class="container">
          <h1 class="dashboard-title">用户管理</h1>
          
          <!-- 统计信息卡片 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-number">{{ userCount }}</div>
              <div class="stat-label">总用户数</div>
            </div>
            <div class="stat-card" @click="toggleActiveUsers">
                <div class="stat-number">{{ activeUserCount }}</div>
                <div class="stat-label">活跃用户</div>
              </div>
          </div>
          
          <!-- 用户列表 -->
          <div class="users-container article-management">
            <div class="management-header">
              <h2>用户列表</h2>
              <div class="search-bar">
                <select v-model="userSearchType" @change="handleUserSearch" style="margin-right: 10px; padding: 6px;">
                  <option value="all">全部</option>
                  <option value="nickname">昵称</option>
                  <option value="email">邮箱</option>
                </select>
                <input 
                  type="text" 
                  v-model="userSearchKeyword" 
                  placeholder="搜索用户..." 
                  @input="handleUserSearch"
                  class="search-input"
                >
              </div>
            </div>
            
            <div class="users-list">
              <div v-if="userLoading" class="loading">加载中...</div>
              
              <div v-else-if="users.length === 0" class="empty-state">
                暂无用户数据
              </div>
              
              <div v-else class="user-table">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>邮箱</th>
                      <th>昵称</th>
                      <th>角色</th>
                      <th>状态</th>
                      <th>注册时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="user in users" :key="user.id">
                      <td>{{ user.id }}</td>
                      <td>{{ user.email }}</td>
                      <td>{{ user.nickname }}</td>
                      <td>
                        <span class="role-badge" :class="user.role">
                          {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                        </span>
                      </td>
                      <td>
                        <span class="status-badge" :class="user.enabled ? 'enabled' : 'disabled'">
                          {{ user.enabled ? '启用' : '禁用' }}
                        </span>
                      </td>
                      <td>{{ formatDate(user.createdAt) }}</td>
                      <td class="action-buttons">
                        <button 
                          class="btn btn-disable" 
                          :disabled="user.role === 'ADMIN' || (user.id === currentUserId)" 
                          @click="toggleUserStatus(user)"
                        >
                          {{ user.enabled ? '禁用' : '启用' }}
                        </button>
                        <button 
                          class="btn btn-delete" 
                          :disabled="user.role === 'ADMIN' || (user.id === currentUserId)" 
                          @click="deleteUser(user.id, user.nickname)"
                        >
                          删除
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <!-- 用户分页 -->
              <div class="pagination" v-if="users.length > 0">
                <button 
                  @click="changeUserPage(userPage - 1)"
                  :disabled="userPage === 1"
                  class="page-btn"
                >
                  上一页
                </button>
                <span class="page-info">第 {{ userPage }} / {{ userTotalPages }} 页</span>
                <button 
                  @click="changeUserPage(userPage + 1)"
                  :disabled="userPage === userTotalPages"
                  class="page-btn"
                >
                  下一页
                </button>
                <div class="page-size">
                  <span>每页显示：</span>
                  <select v-model="userPageSize" @change="handleUserPageSizeChange">
                    <option :value="10">10</option>
                    <option :value="20">20</option>
                    <option :value="50">50</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import axios from '../axios'

export default {
  name: 'AdminDashboard',
  data() {
      return {
        activeTab: 'statistics',
        articles: [],
        allArticles: [], // 用于缓存所有已加载的文章
        comments: [],
        allComments: [], // 用于缓存所有已加载的评论
        commentPage: 1,
        commentTotalPages: 1,
        commentPageSize: 20,
      loading: true,
      searchKeyword: '',
      articleSearchType: 'all',
      currentPage: 1,
      pageSize: 10,
      // 统计数据
      articleCount: 0,
      todayArticleCount: 0,
      commentCount: 0,
        todayCommentCount: 0,
        commentSearchKeyword: '',
        commentSearchType: 'all',
        commentSearchTimer: null,
        // 用户管理相关数据
        users: [],
        userCount: 0,
        activeUserCount: 0,
        lastUpdateTime: '',
        userLoading: false,
        userSearchKeyword: '',
        userSearchType: 'all',
        userSearchTimer: null,
        userPage: 1,
        userTotalPages: 1,
        userPageSize: 20,
        currentUserId: null, // 当前登录用户ID
        isActiveUsersFilter: false // 是否只显示活跃用户
    }
  },
  mounted() {
    this.fetchStatistics()
    this.fetchComments()
    this.fetchUsers()
    this.fetchActiveUserCount()
    // 获取当前登录用户ID
    this.getCurrentUserId()
  },
  computed: {
    filteredArticles() {
      // 由于现在是后端分页，直接返回当前页的文章
      return this.articles
    },
    totalPages() {
      // 计算总页数 = 总文章数 / 每页大小，并向上取整
      return Math.ceil(this.articleCount / this.pageSize)
    }
  },
  methods: {
      // 切换标签页
      switchTab(tab) {
        this.activeTab = tab;
        // 切换到评论标签时获取评论列表
        if (tab === 'comments') {
          this.fetchComments();
        }
        // 切换到用户标签时获取用户列表
        if (tab === 'users') {
          this.fetchUsers();
        }
      },
      
      // 获取当前登录用户ID
      getCurrentUserId() {
        try {
          const userStr = localStorage.getItem('user')
          if (userStr) {
            const user = JSON.parse(userStr)
            this.currentUserId = user.id
          }
        } catch (e) {
          console.error('Error parsing user from localStorage:', e)
        }
      },
      
      // 获取评论列表
  async fetchComments() {
    try {
      const params = {
        page: this.commentPage,
        size: this.commentPageSize
      };
      
      // 确保搜索关键词进行trim处理，避免空格问题
      const trimmedKeyword = this.commentSearchKeyword ? this.commentSearchKeyword.trim() : '';
      
      // 如果有搜索关键词，添加到请求参数中
      if (trimmedKeyword) {
        params.keyword = trimmedKeyword;
        params.searchType = this.commentSearchType;
      }
      
      // 使用导入的axios实例并添加授权头
      const response = await axios.get('/api/comments', {
        params,
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      // 正确处理后端ResponseResult格式的响应
      let data = response.data;
      // 检查是否有data字段（ResponseResult格式）
      if (data.data) {
        data = data.data;
      }
      
      // 确保内容和总数都正确获取
      this.comments = data.content || [];
      this.commentTotalPages = data.totalPages || 1;
      this.commentCount = data.totalElements || 0;
      
      // 缓存评论数据 - 简化逻辑
      // 搜索模式下不缓存，直接使用当前结果
      if (trimmedKeyword) {
        this.allComments = [...this.comments];
      } else {
        // 非搜索模式下的缓存逻辑
        if (this.commentPage === 1) {
          // 第一页时重置缓存
          this.allComments = [...this.comments];
        } else {
          // 非第一页时合并数据（避免重复）
          const newComments = this.comments.filter(
            newComment => !this.allComments.some(
              cachedComment => cachedComment.id === newComment.id
            )
          );
          this.allComments.push(...newComments);
        }
      }
      
      // 计算今日新增评论数
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // 根据是否在搜索状态选择不同的数据来源计算今日评论数
      if (trimmedKeyword) {
        // 搜索状态下，只从当前页数据计算
        this.todayCommentCount = this.comments.filter(comment => {
          const commentDate = new Date(comment.createdAt);
          return commentDate >= today;
        }).length;
      } else {
        // 非搜索状态下，从缓存数据计算
        this.todayCommentCount = this.allComments.filter(comment => {
          const commentDate = new Date(comment.createdAt);
          return commentDate >= today;
        }).length;
      }
    } catch (error) {
      console.error('获取评论列表失败:', error);
      alert('获取评论列表失败');
    }
  },
      
      // 删除评论
      async deleteComment(commentId, content) {
        try {
          // 使用标准confirm对话框代替this.$confirm
          if (!confirm(`确定要删除评论「${content.length > 20 ? content.substring(0, 20) + '...' : content}」吗？`)) {
            return;
          }
          
          // 使用导入的axios实例，与文章详情页保持一致
          await axios.delete(`/api/comments/${commentId}`, {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          });
          alert('评论删除成功');
          
          // 重新获取评论列表
          this.fetchComments();
        } catch (error) {
          console.error('删除评论失败:', error);
          alert('删除评论失败');
        }
      },
      
      // 切换评论页码
      changeCommentPage(page) {
        if (page >= 1 && page <= this.commentTotalPages) {
          this.commentPage = page;
          this.fetchComments();
        }
      },
      
      // 评论分页大小变化
      handleCommentPageSizeChange() {
        this.commentPage = 1;
        this.fetchComments();
      },
      
      // 评论防抖搜索方法
      handleCommentSearch() {
        // 清除之前的定时器
        if (this.commentSearchTimer) {
          clearTimeout(this.commentSearchTimer);
        }
        
        // 设置新的定时器，延迟500ms执行搜索
        this.commentSearchTimer = setTimeout(() => {
          // 搜索时重置到第一页并重新获取数据
          this.commentPage = 1;
          this.fetchComments();
        }, 500);
      },
      
      // 跳转到文章详情
      navigateToArticle(articleId) {
        this.$router.push(`/article/${articleId}`);
      },
      
      // 获取文章列表
      async fetchArticles() {
      try {
        this.loading = true
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        
        // 确保搜索关键词进行trim处理，避免空格问题
        const trimmedKeyword = this.searchKeyword ? this.searchKeyword.trim() : ''
        
        // 如果有搜索关键词，添加到请求参数中
        if (trimmedKeyword) {
          params.keyword = trimmedKeyword
          params.searchType = this.articleSearchType
        }
        
        const response = await axios.get('/api/articles', {
          params,
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        // 正确处理不同格式的响应数据
        let data = response.data
        // 检查是否有data字段（ResponseResult格式）
        if (data.data) {
          data = data.data
        }
        
        // 确保内容和总数都正确获取
        this.articles = data.content || []
        this.articleCount = data.totalElements || 0
        
        // 简化缓存逻辑，避免复杂处理导致的数据不一致
        // 搜索模式下不进行缓存，直接使用当前结果
        if (trimmedKeyword) {
          this.allArticles = [...this.articles]
        } else {
          // 非搜索模式下的缓存逻辑
          if (this.currentPage === 1) {
            // 第一页时重置缓存
            this.allArticles = [...this.articles]
          } else {
            // 非第一页时合并数据（避免重复）
            const newArticles = this.articles.filter(
              newArticle => !this.allArticles.some(
                cachedArticle => cachedArticle.id === newArticle.id
              )
            )
            this.allArticles.push(...newArticles)
          }
        }
        
        // 计算今日新增文章数 - 使用完整的缓存列表
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        
        // 如果有搜索关键词，只统计当前页的今日新增
        if (this.searchKeyword) {
          this.todayArticleCount = this.articles.filter(article => {
            const articleDate = new Date(article.createdAt)
            return articleDate >= today
          }).length
        } else {
          // 非搜索状态下，统计所有缓存文章中的今日新增
          this.todayArticleCount = this.allArticles.filter(article => {
            const articleDate = new Date(article.createdAt)
            return articleDate >= today
          }).length
        }
      } catch (error) {
        console.error('获取文章列表失败:', error)
        alert('获取文章列表失败，请稍后再试')
      } finally {
        this.loading = false
      }
    },
    fetchStatistics() {
      // 获取文章统计数据
      this.fetchArticles();
      // 获取评论统计数据
      this.fetchComments();
      // 获取用户统计数据
      this.fetchUsers();
      // 获取活跃用户数
      this.fetchActiveUserCount();
      
      // 更新最后更新时间
      this.lastUpdateTime = this.formatDateTime(new Date());
    },
    
    formatDateTime(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    // 防抖定时器ID
    searchTimer: null,
    
    // 防抖搜索方法
    handleSearch() {
      // 清除之前的定时器
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      
      // 设置新的定时器，延迟500ms执行搜索
      this.searchTimer = setTimeout(() => {
        // 搜索时重置到第一页并重新获取数据
        this.currentPage = 1
        this.fetchArticles()
      }, 500)
    },
    goToPage(page) {
      // 跳转到指定页并获取数据
      this.currentPage = page
      this.fetchArticles()
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    
    // 获取用户列表
    async fetchUsers() {
      try {
        // 如果开启了活跃用户过滤，则调用专门的活跃用户列表API
        if (this.isActiveUsersFilter) {
          await this.fetchActiveUsersList();
        } else {
          this.userLoading = true
          const params = {
            page: this.userPage,
            size: this.userPageSize
          }
          
          // 确保搜索关键词进行trim处理
          const trimmedKeyword = this.userSearchKeyword ? this.userSearchKeyword.trim() : ''
          
          // 如果有关键词，添加到请求参数
          if (trimmedKeyword) {
            params.keyword = trimmedKeyword
            params.searchType = this.userSearchType
          }
          
          const response = await axios.get('/api/users/admin', {
            params,
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          })
          
          // 正确处理后端ResponseResult格式的响应
          let data = response.data
          if (data.data) {
            data = data.data
          }
          
          this.users = data.content || []
          this.userCount = data.totalElements || 0  // 确保设置用户总数
          this.userTotalPages = data.totalPages || 1
        }
        
        // 获取活跃用户数
        await this.fetchActiveUserCount();
      } catch (error) {
        console.error('获取用户列表失败:', error)
        alert('获取用户列表失败')
      } finally {
        this.userLoading = false
      }
    },
    
    // 获取活跃用户数（当天登录的用户数）
    async fetchActiveUserCount() {
      try {
        const response = await axios.get('/api/users/admin/active-count', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        let data = response.data;
        if (data.data) {
          data = data.data;
        }
        this.activeUserCount = data || 0;
      } catch (error) {
        console.error('获取活跃用户数失败:', error);
        this.activeUserCount = 0;
      }
    },
    
    // 切换是否只显示活跃用户
    toggleActiveUsers() {
      this.isActiveUsersFilter = !this.isActiveUsersFilter;
      this.userPage = 1; // 重置到第一页
      this.fetchUsers(); // 重新获取用户列表
    },
    
    // 获取活跃用户列表
    async fetchActiveUsersList() {
      try {
        this.userLoading = true;
        const params = {
          page: this.userPage,
          size: this.userPageSize
        };
        
        // 确保搜索关键词进行trim处理
        const trimmedKeyword = this.userSearchKeyword ? this.userSearchKeyword.trim() : '';
        
        // 如果有关键词，添加到请求参数
        if (trimmedKeyword) {
          params.keyword = trimmedKeyword;
          params.searchType = this.userSearchType;
        }
        
        const response = await axios.get('/api/users/admin/active-users', {
          params,
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        
        let data = response.data;
        if (data.data) {
          data = data.data;
        }
        
        this.users = data.content || [];
        this.userCount = data.totalElements || 0;
        this.userTotalPages = data.totalPages || 1;
      } catch (error) {
        console.error('获取活跃用户列表失败:', error);
        alert('获取活跃用户列表失败');
      } finally {
        this.userLoading = false;
      }
    },
    
    // 切换用户状态（启用/禁用）
    async toggleUserStatus(user) {
      try {
        const action = user.enabled ? '禁用' : '启用'
        if (!confirm(`确定要${action}用户「${user.nickname}」吗？`)) {
          return
        }
        
        const url = user.enabled 
          ? `/api/users/admin/${user.id}/disable` 
          : `/api/users/admin/${user.id}/enable`
        
        await axios.put(url, null, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        alert(`用户${action}成功`)
        this.fetchUsers()
      } catch (error) {
        console.error(`用户状态更新失败:`, error)
        alert('操作失败，请重试')
      }
    },
    
    // 删除用户
    async deleteUser(userId, nickname) {
      try {
        if (!confirm(`确定要删除用户「${nickname}」吗？此操作将删除该用户的所有文章和评论，且无法恢复！`)) {
          return
        }
        
        await axios.delete(`/api/users/admin/${userId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        alert('用户删除成功')
        this.fetchUsers()
      } catch (error) {
        console.error('删除用户失败:', error)
        alert('删除失败，请重试')
      }
    },
    
    // 用户搜索防抖方法
    handleUserSearch() {
      if (this.userSearchTimer) {
        clearTimeout(this.userSearchTimer)
      }
      
      this.userSearchTimer = setTimeout(() => {
        this.userPage = 1
        this.fetchUsers()
      }, 500)
    },
    
    // 切换用户页码
    changeUserPage(page) {
      if (page >= 1 && page <= this.userTotalPages) {
        this.userPage = page
        this.fetchUsers()
      }
    },
    
    // 用户分页大小变化
    handleUserPageSizeChange() {
      this.userPage = 1
      this.fetchUsers()
    },
    getArticleStatus(article) {
      // 根据文章状态返回对应的标识
      return article.status || 'published'
    },
    async deleteArticle(id, title) {
      if (confirm(`确定要删除文章《${title}》吗？此操作不可恢复。`)) {
        try {
          await axios.delete(`/api/articles/${id}`, {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          })
          // 重新获取文章列表
          this.fetchArticles()
          alert('文章删除成功')
        } catch (error) {
          console.error('删除文章失败:', error)
          alert('删除文章失败，请稍后再试')
        }
      }
    }
  }
}
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background-color: white;
}

/* 左侧导航栏样式 */
.admin-sidebar {
  width: 250px;
  background-color: white;
  color: #333;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #e8e8e8;
}

/* 统计概览样式 */
.statistics-overview .overview-cards {
  margin-bottom: 30px;
}

.statistics-overview .stats-group {
  margin-bottom: 30px;
}

.statistics-overview .group-title {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #3498db;
}

.statistics-overview .cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.statistics-overview .stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.statistics-overview .stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.statistics-overview .stat-card.primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.statistics-overview .stat-card.secondary { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.statistics-overview .stat-card.success { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.statistics-overview .stat-card.warning { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.statistics-overview .stat-card.info { background: linear-gradient(135deg, #0ba360 0%, #3cba92 100%); }
.statistics-overview .stat-card.danger { background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%); }

.statistics-overview .stat-icon {
  font-size: 32px;
  margin-right: 20px;
  opacity: 0.9;
}

.statistics-overview .stat-content {
  flex: 1;
}

.statistics-overview .stat-card .stat-number {
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: white;
}

.statistics-overview .stat-card .stat-label {
  font-size: 16px;
  opacity: 0.9;
  color: white;
}

.statistics-overview .update-info {
  text-align: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  color: #6c757d;
  font-size: 14px;
}

@media (max-width: 768px) {
  .statistics-overview .cards-grid {
    grid-template-columns: 1fr;
  }
  
  .statistics-overview .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;
  }
  
  .statistics-overview .stat-icon {
    margin-right: 0;
    margin-bottom: 15px;
  }
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #fafafa;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
  font-weight: bold;
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin: 0;
}

.nav-item a {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
  font-size: 18px;
}

.nav-item a:hover {
  background-color: #f5f5f5;
  color: #1890ff;
}

.nav-item.active a {
  background-color: #e6f7ff;
  color: #1890ff;
  font-weight: 500;
}

.nav-icon {
  font-size: 20px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.nav-text {
  font-size: 18px;
}

/* 主内容区域样式 */
.admin-main {
  flex: 1;
  margin-left: 250px;
  padding: 20px 0;
  background-color: white;
}

.admin-dashboard {
  min-height: calc(100vh - 40px);
}

.comments-management {
  min-height: calc(100vh - 40px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 用户管理相关样式 */
.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.user-table th {
  background-color: #f5f5f5;
  font-weight: 600;
}

.user-table tr:hover {
  background-color: #f9f9f9;
}

.user-actions {
  display: flex;
  gap: 8px;
}

.status-indicator {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-active {
  background-color: #52c41a;
}

.status-disabled {
  background-color: #d9d9d9;
}

.enable-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.enable-btn:hover {
  background-color: #73d13d;
}

.enable-btn:disabled {
  background-color: #b7eb8f;
  cursor: not-allowed;
}

.disable-btn {
  background-color: #faad14;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.disable-btn:hover {
  background-color: #ffc53d;
}

.disable-btn:disabled {
  background-color: #ffe58f;
  cursor: not-allowed;
}

/* 评论管理样式 */
.comments-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 24px;
  margin-top: 20px;
}

.comments-list {
  width: 100%;
  margin-bottom: 20px;
}

.comments-list-header {
  display: grid;
  grid-template-columns: 80px 1fr 120px 100px 150px 100px;
  gap: 10px;
  padding: 12px 8px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.comment-item {
  display: grid;
  grid-template-columns: 80px 1fr 120px 100px 150px 100px;
  gap: 10px;
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.comment-item:hover {
  background-color: #fafafa;
}

.comment-item:last-child {
  border-bottom: none;
}

.header-item, .comment-item .item {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  color: #666;
}

.header-item {
  color: #333;
}

.item.id, .header-item.id {
  text-align: center;
  justify-content: center;
  font-weight: 500;
}

.item.content {
  white-space: normal;
  line-height: 1.4;
  max-height: 80px;
  overflow: auto;
  word-break: break-word;
}

.item.author {
  font-weight: 500;
}

.item.article .article-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s;
  border-bottom: 1px dashed #1890ff;
  padding-bottom: 1px;
}

.item.article .article-link:hover {
  color: #40a9ff;
  border-bottom-color: #40a9ff;
}

.delete-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.delete-btn:hover {
  background-color: #ff7875;
  transform: translateY(-1px);
}

.delete-btn:active {
  transform: translateY(0);
}

/* 空状态样式 */
.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #6c757d;
  font-size: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-top: 20px;
}

/* 评论分页样式 */
.comments-container .pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  gap: 16px;
  padding: 15px 0;
  border-top: 1px solid #f0f0f0;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
  transform: translateY(-1px);
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  background-color: #f5f5f5;
}

.page-info {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.page-size {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.page-size select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: white;
  font-size: 14px;
  color: #666;
  transition: border-color 0.3s;
}

.page-size select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.dashboard-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  justify-content: center;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex: 1;
  min-width: 200px;
  text-align: center;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 16px;
  color: #666;
}

.article-management {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.management-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.search-bar {
  display: flex;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  width: 300px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #1890ff;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: #999;
}

.article-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  font-weight: 600;
  color: #333;
  background-color: #fafafa;
}

td {
  color: #666;
}

.article-title {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.published {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.draft {
  background-color: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

/* 用户状态样式 */
.status-badge.enabled {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.disabled {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

/* 用户角色样式 */
.role-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.role-badge.ADMIN {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.role-badge.USER {
  background-color: #f0f5ff;
  color: #597ef7;
  border: 1px solid #adc6ff;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.3s;
  text-decoration: none;
  display: inline-block;
}

.btn:hover {
  opacity: 0.8;
}

.btn-edit {
  background-color: #1890ff;
  color: white;
}

.btn-delete {
  background-color: #ff4d4f;
  color: white;
}

.btn-view {
  background-color: #52c41a;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  gap: 16px;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-info {
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 80px;
  }
  
  .admin-main {
    margin-left: 80px;
    padding: 10px 0;
  }
  
  .sidebar-header h2,
  .nav-text {
    display: none;
  }
  
  .nav-icon {
    margin-right: 0;
    font-size: 20px;
  }
  
  .stats-cards {
    flex-direction: column;
  }
  
  .management-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
  
  .article-table {
    font-size: 14px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
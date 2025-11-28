<template>
  <!-- 玄幻科技背景装饰 -->
  <div class="cyber-bg-wrapper">
    <div class="cyber-grid"></div>
    <div class="cyber-particles"></div>
    <div class="cyber-gradient-overlay"></div>
    <div class="cyber-glow-lines"></div>
  </div>
  
  <div class="admin-container">
      <!-- 左侧导航栏 -->
      <aside class="admin-sidebar cyber-sidebar">
        <div class="sidebar-header cyber-sidebar-header">
          <div class="cyber-logo">
            <span class="logo-accent">◈</span>
            <h2>管理中心</h2>
          </div>
        </div>
        <nav class="sidebar-nav">
          <ul>
            <li class="nav-item cyber-nav-item" :class="{ active: activeTab === 'statistics' }">
              <a @click="switchTab('statistics')" class="cyber-nav-link">
                <span class="nav-icon cyber-icon">📊</span>
                <span class="nav-text">统计概览</span>
                <div class="cyber-glow-effect"></div>
              </a>
            </li>
            <li class="nav-item cyber-nav-item" :class="{ active: activeTab === 'articles' }">
              <a @click="switchTab('articles')" class="cyber-nav-link">
                <span class="nav-icon cyber-icon">📝</span>
                <span class="nav-text">文章管理</span>
                <div class="cyber-glow-effect"></div>
              </a>
            </li>
            <li class="nav-item cyber-nav-item" :class="{ active: activeTab === 'comments' }">
              <a @click="switchTab('comments')" class="cyber-nav-link">
                <span class="nav-icon cyber-icon">💬</span>
                <span class="nav-text">评论管理</span>
                <div class="cyber-glow-effect"></div>
              </a>
            </li>
            <li class="nav-item cyber-nav-item" :class="{ active: activeTab === 'users' }">
              <a @click="switchTab('users')" class="cyber-nav-link">
                <span class="nav-icon cyber-icon">👥</span>
                <span class="nav-text">用户管理</span>
                <div class="cyber-glow-effect"></div>
              </a>
            </li>
          </ul>
        </nav>
        <div class="cyber-sidebar-footer">
          <div class="cyber-footer-glow"></div>
        </div>
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
            <div class="cyber-dropdown-container">
              <div class="cyber-dropdown">
                <div class="cyber-dropdown-select" @click="toggleArticleSearchTypeDropdown">
                  <span>{{ getArticleSearchTypeText(articleSearchType) }}</span>
                  <span class="cyber-dropdown-arrow" :class="{ 'rotate': showArticleSearchTypeDropdown }">▼</span>
                </div>
                <div class="cyber-dropdown-menu" v-if="showArticleSearchTypeDropdown">
                  <div class="cyber-dropdown-item" @click="selectArticleSearchType('title_content')">标题/内容</div>
                  <div class="cyber-dropdown-item" @click="selectArticleSearchType('author')">作者名</div>
                  <div class="cyber-dropdown-item" @click="selectArticleSearchType('all')">全部</div>
                </div>
              </div>
            </div>
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
                <div class="cyber-dropdown-container">
                  <div class="cyber-dropdown">
                    <div class="cyber-dropdown-select" @click="toggleCommentSearchTypeDropdown">
                      <span>{{ getCommentSearchTypeText(commentSearchType) }}</span>
                      <span class="cyber-dropdown-arrow" :class="{ 'rotate': showCommentSearchTypeDropdown }">▼</span>
                    </div>
                    <div class="cyber-dropdown-menu" v-if="showCommentSearchTypeDropdown">
                      <div class="cyber-dropdown-item" @click="selectCommentSearchType('content')">评论内容</div>
                      <div class="cyber-dropdown-item" @click="selectCommentSearchType('author')">评论者</div>
                      <div class="cyber-dropdown-item" @click="selectCommentSearchType('all')">全部</div>
                    </div>
                  </div>
                </div>
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
                <div class="cyber-dropdown-container">
                  <div class="cyber-dropdown">
                    <div class="cyber-dropdown-select" @click="toggleUserSearchTypeDropdown">
                      <span>{{ getUserSearchTypeText(userSearchType) }}</span>
                      <span class="cyber-dropdown-arrow" :class="{ 'rotate': showUserSearchTypeDropdown }">▼</span>
                    </div>
                    <div class="cyber-dropdown-menu" v-if="showUserSearchTypeDropdown">
                      <div class="cyber-dropdown-item" @click="selectUserSearchType('all')">全部</div>
                      <div class="cyber-dropdown-item" @click="selectUserSearchType('nickname')">昵称</div>
                      <div class="cyber-dropdown-item" @click="selectUserSearchType('email')">邮箱</div>
                    </div>
                  </div>
                </div>
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
        isActiveUsersFilter: false, // 是否只显示活跃用户
        // 下拉菜单显示状态
        showArticleSearchTypeDropdown: false,
        showCommentSearchTypeDropdown: false,
        showUserSearchTypeDropdown: false
    }
  },
  mounted() {
    this.fetchStatistics()
    this.fetchComments()
    this.fetchUsers()
    this.fetchActiveUserCount()
    // 获取当前登录用户ID
    this.getCurrentUserId()
    // 添加全局点击事件监听器，用于关闭下拉菜单
    document.addEventListener('click', this.handleGlobalClick);
  },
  beforeUnmount() {
    // 移除全局点击事件监听器
    document.removeEventListener('click', this.handleGlobalClick);
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
      
      // 获取搜索类型文本
      getArticleSearchTypeText(type) {
        const typeMap = {
          'title_content': '标题/内容',
          'author': '作者名',
          'all': '全部'
        };
        return typeMap[type] || '全部';
      },
      
      getCommentSearchTypeText(type) {
        const typeMap = {
          'content': '评论内容',
          'author': '评论者',
          'all': '全部'
        };
        return typeMap[type] || '全部';
      },
      
      getUserSearchTypeText(type) {
        const typeMap = {
          'all': '全部',
          'nickname': '昵称',
          'email': '邮箱'
        };
        return typeMap[type] || '全部';
      },
      
      // 切换下拉菜单显示状态
      toggleArticleSearchTypeDropdown() {
        // 关闭其他下拉菜单
        this.showCommentSearchTypeDropdown = false;
        this.showUserSearchTypeDropdown = false;
        // 切换当前下拉菜单
        this.showArticleSearchTypeDropdown = !this.showArticleSearchTypeDropdown;
      },
      
      toggleCommentSearchTypeDropdown() {
        // 关闭其他下拉菜单
        this.showArticleSearchTypeDropdown = false;
        this.showUserSearchTypeDropdown = false;
        // 切换当前下拉菜单
        this.showCommentSearchTypeDropdown = !this.showCommentSearchTypeDropdown;
      },
      
      toggleUserSearchTypeDropdown() {
        // 关闭其他下拉菜单
        this.showArticleSearchTypeDropdown = false;
        this.showCommentSearchTypeDropdown = false;
        // 切换当前下拉菜单
        this.showUserSearchTypeDropdown = !this.showUserSearchTypeDropdown;
      },
      
      // 选择搜索类型
      selectArticleSearchType(type) {
        this.articleSearchType = type;
        this.showArticleSearchTypeDropdown = false;
        this.handleSearch();
      },
      
      selectCommentSearchType(type) {
        this.commentSearchType = type;
        this.showCommentSearchTypeDropdown = false;
        this.handleCommentSearch();
      },
      
      selectUserSearchType(type) {
        this.userSearchType = type;
        this.showUserSearchTypeDropdown = false;
        this.handleUserSearch();
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
      
      // 全局点击处理，用于关闭下拉菜单
      handleGlobalClick(event) {
        // 检查点击是否发生在下拉菜单内部
        const dropdownElements = document.querySelectorAll('.cyber-dropdown-container');
        let clickedInsideDropdown = false;
        
        dropdownElements.forEach(element => {
          if (element.contains(event.target)) {
            clickedInsideDropdown = true;
          }
        });
        
        // 如果点击不在下拉菜单内部，则关闭所有下拉菜单
        if (!clickedInsideDropdown) {
          this.showArticleSearchTypeDropdown = false;
          this.showCommentSearchTypeDropdown = false;
          this.showUserSearchTypeDropdown = false;
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
/* 玄幻科技背景装饰 */
.cyber-bg-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--main-bg);
  overflow: hidden;
  z-index: -1;
}

/* 网格背景 */
.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(var(--grid-color, rgba(0, 174, 255, 0.1)) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-color, rgba(0, 174, 255, 0.1)) 1px, transparent 1px);
  background-size: 30px 30px;
  animation: cyberGridMove 20s linear infinite;
}

@keyframes cyberGridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(30px, 30px); }
}

/* 粒子效果 */
.cyber-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, var(--highlight), transparent),
    radial-gradient(2px 2px at 40px 70px, var(--secondary-highlight, rgba(138, 43, 226, 0.8)), transparent),
    radial-gradient(1px 1px at 90px 40px, var(--highlight), transparent),
    radial-gradient(1px 1px at 130px 80px, var(--secondary-highlight, rgba(138, 43, 226, 0.8)), transparent),
    radial-gradient(2px 2px at 160px 30px, var(--highlight), transparent);
  background-repeat: repeat;
  background-size: 200px 200px;
  animation: cyberParticlesMove 10s linear infinite;
  opacity: 0.5;
}

@keyframes cyberParticlesMove {
  0% { transform: translateY(0) translateX(0); }
  50% { transform: translateY(-20px) translateX(10px); }
  100% { transform: translateY(0) translateX(0); }
}

/* 渐变叠加 */
.cyber-gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(ellipse at center, var(--secondary-highlight, rgba(138, 43, 226, 0.1)) 0%, transparent 70%);
}

/* 发光线条 */
.cyber-glow-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--highlight-glow, rgba(0, 174, 255, 0.1)), transparent);
  animation: cyberGlowLinesMove 5s ease-in-out infinite alternate;
}

@keyframes cyberGlowLinesMove {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

.admin-container {
  display: flex;
  min-height: 100vh;
  background: transparent;
  position: relative;
}

/* 玄幻科技风格左侧导航栏样式 */
.cyber-sidebar {
  width: 250px;
  background: var(--admin-sidebar-bg);
  color: var(--admin-sidebar-text);
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  overflow-y: auto;
  border-right: 1px solid var(--admin-sidebar-border);
  backdrop-filter: blur(10px);
  box-shadow: var(--admin-sidebar-shadow);
  z-index: 10;
  padding: 20px 0;
}

.cyber-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--highlight), var(--secondary-highlight, #8a2be2), transparent);
  animation: cyberGlow 3s ease-in-out infinite alternate;
}

@keyframes cyberGlow {
  0% { opacity: 0.5; }
  100% { opacity: 1; }
}

.cyber-sidebar-header {
  padding: 0 20px 20px;
  border-bottom: 1px solid var(--admin-sidebar-header-border);
  margin-bottom: 20px;
}

.cyber-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cyber-logo h2 {
  margin: 0;
  font-size: 22px;
  color: var(--admin-sidebar-logo-color);
  font-weight: 600;
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight, #8a2be2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

.logo-accent {
  font-size: 24px;
  color: var(--highlight);
  text-shadow: 0 0 10px var(--highlight-glow, rgba(0, 174, 255, 0.7));
  animation: logoGlow 2s ease-in-out infinite alternate;
}

@keyframes logoGlow {
  0% { text-shadow: 0 0 10px var(--highlight-glow, rgba(0, 174, 255, 0.7)); }
  100% { text-shadow: 0 0 20px var(--highlight-glow, rgba(0, 174, 255, 0.9)), 0 0 30px var(--highlight-glow, rgba(0, 174, 255, 0.6)); }
}

/* 玄幻科技风格统计概览样式 */
.statistics-overview .overview-cards {
  margin-bottom: 30px;
}

.statistics-overview .stats-group {
  margin-bottom: 30px;
}

.statistics-overview .group-title {
  font-size: 20px;
  color: var(--admin-group-title-color);
  margin-bottom: 20px;
  padding-left: 15px;
  border-left: 4px solid var(--highlight);
  text-shadow: var(--admin-group-title-text-shadow);
  position: relative;
  border-bottom: var(--admin-group-title-border);
}

.statistics-overview .group-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100px;
  height: 2px;
  background: linear-gradient(90deg, var(--highlight), var(--secondary-highlight, #8a2be2));
  border-radius: 2px;
}

.statistics-overview .cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
}

.statistics-overview .stat-card {
  display: flex;
  align-items: center;
  padding: 25px;
  border-radius: 15px;
  color: var(--text-primary);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow);
}

.statistics-overview .stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.statistics-overview .stat-card::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    var(--highlight-glow, rgba(0, 174, 255, 0.1)),
    var(--secondary-highlight, rgba(138, 43, 226, 0.1)),
    transparent
  );
  transform: rotate(45deg);
  animation: cyberShine 3s infinite linear;
  z-index: -1;
}

@keyframes cyberShine {
  0% { transform: rotate(45deg) translateX(-100%); }
  100% { transform: rotate(45deg) translateX(100%); }
}

.statistics-overview .stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 15px 35px var(--highlight-glow, rgba(0, 174, 255, 0.4));
  border-color: var(--highlight-glow, rgba(0, 174, 255, 0.6));
}

.statistics-overview .stat-card.primary { 
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.8), rgba(118, 75, 162, 0.8)); 
}

.statistics-overview .stat-card.secondary { 
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.8), rgba(245, 87, 108, 0.8)); 
}

.statistics-overview .stat-card.success { 
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.8), rgba(0, 242, 254, 0.8)); 
}

.statistics-overview .stat-card.warning { 
  background: linear-gradient(135deg, rgba(250, 112, 154, 0.8), rgba(254, 225, 64, 0.8)); 
}

.statistics-overview .stat-card.info { 
  background: linear-gradient(135deg, rgba(11, 163, 96, 0.8), rgba(60, 186, 146, 0.8)); 
}

.statistics-overview .stat-card.danger { 
  background: linear-gradient(135deg, rgba(255, 154, 158, 0.8), rgba(250, 208, 196, 0.8)); 
}

.statistics-overview .stat-icon {
  font-size: 36px;
  margin-right: 20px;
  opacity: 0.9;
  color: var(--highlight);
  text-shadow: 0 0 10px var(--highlight-glow, rgba(0, 174, 255, 0.7));
  transition: all 0.3s ease;
}

.statistics-overview .stat-card:hover .stat-icon {
  transform: scale(1.2);
  text-shadow: 0 0 15px var(--highlight-glow, rgba(0, 174, 255, 0.9));
}

.statistics-overview .stat-content {
  flex: 1;
}

.statistics-overview .stat-card .stat-number {
  font-size: 40px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: var(--text-primary);
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.statistics-overview .stat-card .stat-label {
  font-size: 16px;
  opacity: 0.9;
  color: var(--text-secondary);
  letter-spacing: 0.5px;
}

.statistics-overview .update-info {
  text-align: center;
  padding: 20px;
  border-radius: 10px;
  color: var(--text-secondary);
  font-size: 14px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  backdrop-filter: blur(5px);
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
  padding: 0 20px 20px;
  border-bottom: 1px solid var(--border-color);
  background: transparent;
  margin-bottom: 20px;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 22px;
  color: var(--text-primary);
  font-weight: 600;
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight, #8a2be2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 玄幻科技风格导航项样式 */
.cyber-nav-item {
  margin: 0;
  list-style: none;
  position: relative;
}

.cyber-nav-item:not(:last-child) {
  margin-bottom: 5px;
}

.cyber-nav-link {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: var(--admin-sidebar-nav-text);
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 0 15px;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.cyber-nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, var(--highlight-glow, rgba(0, 174, 255, 0.1)), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.cyber-nav-link:hover {
  color: var(--highlight);
  transform: translateX(5px);
  background: var(--admin-sidebar-nav-hover-bg);
  box-shadow: 0 5px 15px var(--highlight-glow, rgba(0, 174, 255, 0.2));
}

.cyber-nav-link:hover::before {
  opacity: 1;
}

.cyber-nav-link.active {
  color: var(--text-primary);
  background: var(--admin-sidebar-nav-active-bg);
  box-shadow: 0 5px 15px var(--highlight-glow, rgba(0, 174, 255, 0.3));
  border-left: 3px solid var(--admin-sidebar-nav-active-border);
}

.cyber-nav-link.active::before {
  opacity: 1;
}

/* 发光效果容器 */
.cyber-glow-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cyber-nav-link:hover .cyber-glow-effect,
.cyber-nav-link.active .cyber-glow-effect {
  opacity: 1;
}

.cyber-glow-effect::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 8px;
  box-shadow: 
    inset 0 0 20px var(--highlight-glow, rgba(0, 174, 255, 0.5)),
    0 0 20px var(--highlight-glow, rgba(0, 174, 255, 0.3));
  animation: cyberInnerGlow 2s ease-in-out infinite alternate;
}

@keyframes cyberInnerGlow {
  0% { box-shadow: inset 0 0 20px var(--highlight-glow, rgba(0, 174, 255, 0.5)), 0 0 20px var(--highlight-glow, rgba(0, 174, 255, 0.3)); }
  100% { box-shadow: inset 0 0 30px var(--highlight-glow, rgba(0, 174, 255, 0.7)), 0 0 30px var(--highlight-glow, rgba(0, 174, 255, 0.5)); }
}

/* 玄幻科技风格图标样式 */
.cyber-nav-icon {
  margin-right: 12px;
  font-size: 18px;
  width: 20px;
  text-align: center;
  color: var(--highlight);
  text-shadow: 0 0 5px var(--highlight);
  transition: all 0.3s ease;
}

.cyber-nav-link:hover .cyber-nav-icon {
  color: var(--text-primary);
  text-shadow: 0 0 10px var(--highlight);
  transform: scale(1.2);
}

.cyber-nav-link.active .cyber-nav-icon {
  color: var(--text-primary);
  text-shadow: 0 0 10px var(--highlight);
}

.nav-text {
  font-size: 18px;
}

/* 主内容区域样式 */
.admin-main {
  flex: 1;
  margin-left: 250px;
  padding: 20px 0;
  background: transparent;
  position: relative;
  z-index: 1;
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
  background: var(--bg-secondary);
  border-radius: 10px;
  overflow: hidden;
}

.user-table th,
.user-table td {
  padding: 15px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.user-table th {
  font-weight: 600;
  color: var(--text-primary);
  background: var(--bg-tertiary);
  text-shadow: 0 0 5px var(--highlight);
  position: relative;
}

.user-table th::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

.user-table td {
  color: var(--text-secondary);
}

.user-table tr:hover {
  background: var(--bg-hover);
  transition: all 0.3s ease;
}

.user-actions {
  display: flex;
  gap: 10px;
}

.status-indicator {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
}

.status-active {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  box-shadow: 0 0 10px rgba(82, 196, 26, 0.5);
}

.status-disabled {
  background: linear-gradient(135deg, #d9d9d9, #bfbfbf);
  box-shadow: 0 0 10px rgba(217, 217, 217, 0.3);
}

.enable-btn {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.3), rgba(82, 196, 26, 0.5));
  color: #73d13d;
  border: 1px solid rgba(82, 196, 26, 0.6);
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(5px);
}

.enable-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.enable-btn:hover {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.5), rgba(82, 196, 26, 0.7));
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(82, 196, 26, 0.3);
}

.enable-btn:disabled {
  background: var(--bg-secondary);
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
  box-shadow: none;
}

.disable-btn {
  background: linear-gradient(135deg, rgba(250, 173, 20, 0.3), rgba(250, 173, 20, 0.5));
  color: #ffc53d;
  border: 1px solid rgba(250, 173, 20, 0.6);
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(5px);
}

.disable-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.disable-btn:hover {
  background: linear-gradient(135deg, rgba(250, 173, 20, 0.5), rgba(250, 173, 20, 0.7));
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(250, 173, 20, 0.3);
}

.disable-btn:disabled {
  background: var(--bg-secondary);
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
  box-shadow: none;
}

/* 玄幻科技风格评论管理样式 */
.comments-container {
  background: var(--bg-secondary);
  border-radius: 15px;
  box-shadow: var(--shadow);
  padding: 30px;
  margin-top: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.comments-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.comments-list {
  width: 100%;
  margin-bottom: 25px;
}

.comments-list-header {
  display: grid;
  grid-template-columns: 80px 1fr 120px 100px 150px 100px;
  gap: 12px;
  padding: 15px 10px;
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  text-shadow: 0 0 5px var(--highlight);
  position: relative;
}

.comments-list-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

.comment-item {
  display: grid;
  grid-template-columns: 80px 1fr 120px 100px 150px 100px;
  gap: 12px;
  padding: 15px 10px;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.comment-item:hover {
  background: var(--bg-hover);
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
  color: var(--text-secondary);
}

.header-item {
  color: var(--text-primary);
  font-weight: 600;
}

.item.id, .header-item.id {
  text-align: center;
  justify-content: center;
  font-weight: 500;
}

.item.content {
  white-space: normal;
  line-height: 1.5;
  max-height: 80px;
  overflow: auto;
  word-break: break-word;
  color: var(--text-secondary);
}

.item.author {
  font-weight: 500;
  color: var(--highlight);
}

.item.article .article-link {
  color: var(--highlight);
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
  border-bottom: 1px dashed var(--highlight);
  padding-bottom: 2px;
}

.item.article .article-link:hover {
  color: var(--secondary-highlight);
  border-bottom-color: var(--secondary-highlight);
  text-shadow: 0 0 10px var(--highlight);
}

.delete-btn {
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.3), rgba(255, 77, 79, 0.5));
  color: #ff7875;
  border: 1px solid rgba(255, 77, 79, 0.6);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 10px rgba(255, 77, 79, 0.2);
}

.delete-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.delete-btn:hover {
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.5), rgba(255, 77, 79, 0.7));
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 77, 79, 0.4);
}

.delete-btn:active {
  transform: translateY(0);
}

/* 空状态样式 */
.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 16px;
  background-color: var(--bg-tertiary);
  border-radius: 4px;
  margin-top: 20px;
}

/* 玄幻科技风格评论分页样式 */
.comments-container .pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 20px;
  padding: 20px 0;
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
  border-radius: 10px;
  backdrop-filter: blur(5px);
}

.page-btn {
  padding: 10px 20px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 10px var(--highlight-glow);
}

.page-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight));
  border-color: var(--highlight);
  color: var(--highlight);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px var(--highlight-glow);
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  background: var(--bg-secondary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.page-info {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  text-shadow: 0 0 5px var(--highlight);
}

.page-size {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--text-secondary);
}

.page-size select {
  padding: 8px 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-secondary);
  font-size: 14px;
  color: var(--text-primary);
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 10px var(--highlight-glow);
}

.page-size select:focus {
  outline: none;
  border-color: var(--highlight);
  box-shadow: 0 0 15px var(--highlight-glow);
}

.page-size select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.dashboard-title {
  font-size: 32px;
  color: var(--admin-title-color);
  margin-bottom: 35px;
  text-align: center;
  text-shadow: var(--admin-title-text-shadow);
  background: var(--admin-title-bg-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.dashboard-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
  border-radius: 50%;
}

.stats-cards {
  display: flex;
  gap: 25px;
  margin-bottom: 45px;
  justify-content: center;
  flex-wrap: wrap;
}

.stat-card {
  background: var(--bg-secondary);
  border-radius: 15px;
  padding: 25px;
  box-shadow: var(--shadow);
  flex: 1;
  min-width: 220px;
  text-align: center;
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px var(--shadow);
}

.stat-number {
  font-size: 40px;
  font-weight: bold;
  color: var(--highlight);
  margin-bottom: 10px;
  text-shadow: 0 0 10px var(--highlight);
}

.stat-label {
  font-size: 16px;
  color: var(--text-secondary);
}

.article-management {
  background: var(--bg-secondary);
  border-radius: 15px;
  padding: 30px;
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.article-management::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.management-header h2 {
  font-size: 24px;
  color: var(--admin-management-title-color);
  margin: 0;
  text-shadow: var(--admin-management-title-text-shadow);
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

.search-bar {
  display: flex;
  align-items: center;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
  width: 300px;
  outline: none;
  transition: all 0.3s ease;
  background: var(--bg-tertiary);
  color: var(--text-primary);
  box-shadow: var(--shadow);
}

.search-input:focus {
  border-color: var(--highlight);
  box-shadow: var(--highlight-glow);
}

.search-input::placeholder {
  color: var(--text-secondary);
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--text-secondary);
}

.article-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-secondary);
  border-radius: 10px;
  overflow: hidden;
}

th, td {
  padding: 15px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

th {
  font-weight: 600;
  color: var(--text-primary);
  background: var(--bg-tertiary);
  text-shadow: 0 0 5px var(--highlight);
  position: relative;
}

th::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

td {
  color: var(--text-secondary);
}

tr:hover {
  background: var(--bg-hover);
  transition: all 0.3s ease;
}

.article-title {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  display: inline-block;
  text-align: center;
  min-width: 70px;
}

.status-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.status-badge.published {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.2), rgba(82, 196, 26, 0.4));
  color: #73d13d;
  border: 1px solid rgba(82, 196, 26, 0.5);
  box-shadow: 0 0 10px rgba(82, 196, 26, 0.3);
}

.status-badge.draft {
  background: linear-gradient(135deg, rgba(250, 140, 22, 0.2), rgba(250, 140, 22, 0.4));
  color: #ffc53d;
  border: 1px solid rgba(250, 140, 22, 0.5);
  box-shadow: 0 0 10px rgba(250, 140, 22, 0.3);
}

/* 用户状态样式 */
.status-badge.enabled {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.2), rgba(82, 196, 26, 0.4));
  color: #73d13d;
  border: 1px solid rgba(82, 196, 26, 0.5);
  box-shadow: 0 0 10px rgba(82, 196, 26, 0.3);
}

.status-badge.disabled {
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.2), rgba(255, 77, 79, 0.4));
  color: #ff7875;
  border: 1px solid rgba(255, 77, 79, 0.5);
  box-shadow: 0 0 10px rgba(255, 77, 79, 0.3);
}

/* 用户角色样式 */
.role-badge {
  padding: 6px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  display: inline-block;
  text-align: center;
  min-width: 70px;
}

.role-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.role-badge.ADMIN {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.2), rgba(24, 144, 255, 0.4));
  color: #40a9ff;
  border: 1px solid rgba(24, 144, 255, 0.5);
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
}

.role-badge.USER {
  background: linear-gradient(135deg, rgba(89, 126, 247, 0.2), rgba(89, 126, 247, 0.4));
  color: #7a9fff;
  border: 1px solid rgba(89, 126, 247, 0.5);
  box-shadow: 0 0 10px rgba(89, 126, 247, 0.3);
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
  text-shadow: 0 0 5px var(--highlight);
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
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
  box-shadow: var(--shadow);
  font-size: 14px;
  min-width: 120px;
  height: 42px; /* 与搜索输入框高度保持一致 (padding 10px + border 1px * 2 = 22px, font-size 14px + line-height) */
  box-sizing: border-box; /* 确保padding不会增加元素总高度 */
  line-height: 1; /* 确保文字垂直居中 */
}

.cyber-dropdown-select:hover {
  border-color: var(--highlight);
  box-shadow: var(--highlight-glow);
}

.cyber-dropdown-arrow {
  font-size: 12px;
  transition: transform 0.3s;
  color: var(--highlight);
}

.cyber-dropdown-arrow.rotate {
  transform: rotate(180deg);
  color: var(--highlight);
}

.cyber-dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 5px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
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
  padding: 10px 15px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  display: flex;
  align-items: center;
  min-height: 42px; /* 与下拉菜单选择器高度保持一致 */
}

.cyber-dropdown-item:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
  text-shadow: 0 0 5px var(--highlight);
}

.cyber-dropdown-item:first-child {
  border-bottom: 1px solid var(--border-color);
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 15px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  display: inline-block;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(5px);
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.btn-edit {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.3), rgba(24, 144, 255, 0.5));
  color: #40a9ff;
  border: 1px solid rgba(24, 144, 255, 0.6);
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.2);
}

.btn-delete {
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.3), rgba(255, 77, 79, 0.5));
  color: #ff7875;
  border: 1px solid rgba(255, 77, 79, 0.6);
  box-shadow: 0 0 10px rgba(255, 77, 79, 0.2);
}

.btn-view {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.3), rgba(82, 196, 26, 0.5));
  color: #73d13d;
  border: 1px solid rgba(82, 196, 26, 0.6);
  box-shadow: 0 0 10px rgba(82, 196, 26, 0.2);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 20px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 10px;
  backdrop-filter: blur(5px);
  border: 1px solid var(--border-color);
}

.pagination-btn {
  padding: 10px 20px;
  border: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: var(--text-secondary);
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.pagination-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  z-index: -1;
}

.pagination-btn:hover:not(:disabled) {
  border-color: var(--highlight);
  color: var(--highlight);
  transform: translateY(-2px);
  box-shadow: var(--highlight-glow);
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  background: var(--bg-secondary);
}

.pagination-info {
  color: var(--text-secondary);
  font-weight: 500;
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
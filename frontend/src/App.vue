<template>
  <div class="app">
    <header class="header">
      <nav class="nav">
        <div class="container">
          <router-link to="/" class="logo">博客系统</router-link>
          
          <!-- 搜索功能组件 -->
          <div class="search-container">
            <!-- 自定义搜索类型下拉菜单 -->
            <div class="search-type-dropdown">
              <div class="search-type-select" @click="toggleSearchTypeMenu">
                <span>{{ getSearchTypeName() }}</span>
                <span class="dropdown-arrow" :class="{ 'active': showSearchTypeMenu }"></span>
              </div>
              <!-- 搜索类型下拉菜单内容 -->
              <div v-if="showSearchTypeMenu" class="search-type-menu show">
                <div 
                  v-for="type in searchTypes" 
                  :key="type.value"
                  class="search-type-item"
                  :class="{ 'active': searchType === type.value }"
                  @click="selectSearchType(type.value)"
                >
                  {{ type.label }}
                </div>
              </div>
            </div>
            <input 
              type="text" 
              v-model="searchKeyword" 
              :placeholder="getSearchPlaceholder()" 
              class="search-input"
              @keyup.enter="handleSearch"
            >
            <button @click="handleSearch" class="search-button">搜索</button>
          </div>
          
          <div class="nav-links">
            <router-link to="/" class="nav-link" @click.native="handleHomeClick">首页</router-link>
            <router-link v-if="isLoggedIn" to="/create" class="nav-link">写文章</router-link>
            <router-link v-if="isLoggedIn" to="/profile" class="nav-link">个人中心</router-link>
            <div v-else>
              <router-link to="/login" class="nav-link">登录</router-link>
              <router-link to="/register" class="nav-link">注册</router-link>
            </div>
            
            <!-- 用户头像和昵称，点击显示下拉菜单 -->
            <div v-if="isLoggedIn" class="user-menu-container">
              <div class="user-info" @click="toggleUserMenu">
                <img 
                  :src="user?.avatar || '/src/assets/images/avatar.jpg'" 
                  alt="用户头像"
                  class="user-avatar"
                  @error="$event.target.src = '/src/assets/images/avatar.jpg'"
                >
                <span class="user-nickname">{{ user?.nickname || '用户' }}</span>
                <span class="dropdown-arrow" :class="{ 'active': showUserMenu }"></span>
              </div>
              
              <!-- 下拉菜单 -->
              <div v-if="showUserMenu" class="user-dropdown-menu show">
                <router-link to="/profile" class="dropdown-item" @click="showUserMenu = false">个人中心</router-link>
                <router-link to="/create" class="dropdown-item" @click="showUserMenu = false">写文章</router-link>
                <!-- 管理员中心选项，仅对管理员显示 -->
                <router-link v-if="user?.role === 'ADMIN'" to="/admin/dashboard" class="dropdown-item" @click="showUserMenu = false">管理员中心</router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout-item" @click="logout">退出登录</button>
              </div>
            </div>
          </div>
        </div>
      </nav>
    </header>
    
    <main class="main">
      <div class="container">
        <router-view v-slot="{ Component }">
          <transition name="fade">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
    
    <footer class="footer">
      <div class="container">
        <p>&copy; 2023 博客系统. All rights reserved.</p>
      </div>
    </footer>
  
  <!-- 主题切换按钮 -->
  <ThemeToggle @theme-change="handleThemeChange" />
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from './axios'
import ThemeToggle from './components/ThemeToggle.vue'

export default {
  name: 'App',
  components: {
    ThemeToggle
  },
  setup() {
    const isLoggedIn = ref(false)
    const user = ref(null)
    const showUserMenu = ref(false)
    const showSearchTypeMenu = ref(false) // 搜索类型下拉菜单显示状态
    const router = useRouter()
    const homeResetEvent = ref(false)
    
    // 搜索相关数据
    const searchKeyword = ref('')
    const searchType = ref('all')
    
    // 搜索类型选项
    const searchTypes = [
      { value: 'all', label: '全部搜索' },
      { value: 'title_content', label: '文章内容' },
      { value: 'author', label: '作者昵称' }
    ]
    
    // 初始化主题设置
    const initializeTheme = () => {
      // 从localStorage获取保存的主题设置
      const savedTheme = localStorage.getItem('theme')
      
      // 设置默认主题为浅色模式
      if (savedTheme === 'dark') {
        document.documentElement.classList.add('dark-theme')
      } else {
        document.documentElement.classList.remove('dark-theme')
      }
    }
    
    // 全局搜索状态标志，用于通知Home组件更新搜索
    window.globalSearchState = window.globalSearchState || {}

    const checkLoginStatus = () => {
      const token = localStorage.getItem('token')
      isLoggedIn.value = !!token
      // 获取用户信息
      if (isLoggedIn.value) {
        try {
          const userStr = localStorage.getItem('user')
          user.value = userStr ? JSON.parse(userStr) : null
        } catch (e) {
          console.error('解析用户信息失败:', e)
          user.value = null
        }
      } else {
        user.value = null
      }
    }

    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      isLoggedIn.value = false
      user.value = null
      showUserMenu.value = false
      router.push('/')
    }
    
    // 切换用户菜单显示状态
    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }
    
    // 点击其他地方关闭菜单
    const handleClickOutside = (event) => {
      if (!event.target.closest('.user-menu-container')) {
        showUserMenu.value = false
      }
      if (!event.target.closest('.search-type-dropdown')) {
        showSearchTypeMenu.value = false
      }
    }

    onMounted(() => {
      // 初始化主题设置
      initializeTheme()
      
      checkLoginStatus()
      
      // 全局设置axios的token
      const token = localStorage.getItem('token')
      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
      
      // 监听路由变化，在每次路由切换后检查登录状态
      router.afterEach(() => {
        checkLoginStatus()
      })
      
      // 监听点击事件，点击其他地方关闭菜单
      document.addEventListener('click', handleClickOutside)
    })
    
    onBeforeUnmount(() => {
      document.removeEventListener('click', handleClickOutside)
    })

    // 处理首页链接点击事件，确保重置搜索状态
    const handleHomeClick = () => {
      // 清除搜索输入框和搜索类型
      searchKeyword.value = ''
      searchType.value = 'all'
      
      // 清除全局搜索状态
      window.globalSearchState = {
        keyword: '',
        type: 'all',
        timestamp: Date.now()
      }
      
      // 通过设置一个全局状态来通知Home组件重置搜索状态
      window.homeResetFlag = Date.now() // 使用时间戳确保每次都触发变化
    }
    
    // 获取搜索框占位符文本
    const getSearchPlaceholder = () => {
      switch (searchType.value) {
        case 'author':
          return '搜索作者昵称...'
        case 'title_content':
          return '搜索文章标题或内容...'
        default:
          return '搜索文章标题、内容或作者昵称...'
      }
    }
    
    // 获取搜索类型显示名称
    const getSearchTypeName = () => {
      const type = searchTypes.find(t => t.value === searchType.value)
      return type ? type.label : '全部搜索'
    }
    
    // 切换搜索类型下拉菜单显示状态
    const toggleSearchTypeMenu = () => {
      showSearchTypeMenu.value = !showSearchTypeMenu.value
    }
    
    // 选择搜索类型
    const selectSearchType = (type) => {
      searchType.value = type
      showSearchTypeMenu.value = false
    }
    
    // 处理搜索
    const handleSearch = () => {
      // 更新全局搜索状态
      window.globalSearchState = {
        keyword: searchKeyword.value,
        type: searchType.value,
        timestamp: Date.now()
      }
      
      // 如果不在首页，则跳转到首页
      if (router.currentRoute.value.path !== '/') {
        router.push('/')
      } else {
        // 如果已经在首页，通过重置标志触发搜索
        window.homeResetFlag = Date.now()
      }
    }
    
    // 处理主题变化
    const handleThemeChange = (isDark) => {
      console.log('Theme changed:', isDark ? 'dark' : 'light')
      // 根据切换的主题更新DOM类
      if (isDark) {
        document.documentElement.classList.add('dark-theme')
        localStorage.setItem('theme', 'dark')
      } else {
        document.documentElement.classList.remove('dark-theme')
        localStorage.setItem('theme', 'light')
      }
    }
    
    return {
      isLoggedIn,
      user,
      showUserMenu,
      showSearchTypeMenu,
      searchType,
      searchTypes,
      logout,
      toggleUserMenu,
      handleHomeClick,
      searchKeyword,
      getSearchPlaceholder,
      getSearchTypeName,
      toggleSearchTypeMenu,
      selectSearchType,
      handleSearch,
      handleThemeChange
    }
  }
}
</script>

<style>
/* 全局主题CSS变量 */
:root {
  /* 浅色主题变量 */
  --bg-primary-light: #f8f9fa;
  --bg-secondary-light: #ffffff;
  --bg-tertiary-light: #e9ecef;
  --text-primary-light: #212529;
  --text-secondary-light: #6c757d;
  --text-accent-light: #00aaff;
  --border-color-light: #dee2e6;
  --header-bg-light: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #e3f2fd 100%);
  --header-glow-light: linear-gradient(90deg, transparent, #0066cc, transparent);
  --main-bg-light: #f5f5f5;
  --main-bg-pattern-light: 
    radial-gradient(circle at 10% 20%, rgba(0, 174, 255, 0.05) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(138, 43, 226, 0.05) 0%, transparent 20%);
  --footer-bg-light: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #e3f2fd 100%);
  --footer-text-light: rgba(33, 37, 41, 0.8);
  --shadow-light: 0 4px 20px rgba(0, 0, 0, 0.08);
  --highlight-light: #8a2be2;
  --secondary-highlight-light: #5a7fdb;
  
  /* 模态框浅色主题变量 */
  --modal-overlay-bg-light: radial-gradient(circle, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.7) 100%);
  --modal-bg-light: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(240, 240, 245, 0.95) 100%);
  --modal-shadow-light: 0 8px 32px rgba(0, 0, 0, 0.1), 0 0 30px rgba(138, 43, 226, 0.2);
  --modal-border-light: 1px solid rgba(138, 43, 226, 0.3);
  --modal-title-underline-light: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.6), transparent);
  
  /* 输入框浅色主题变量 */
  --input-border-light: 1px solid rgba(138, 43, 226, 0.3);
  --input-bg-light: rgba(255, 255, 255, 0.8);
  --input-shadow-light: 0 0 10px rgba(138, 43, 226, 0.05);
  --input-focus-shadow-light: 0 0 20px rgba(138, 43, 226, 0.15);
  --input-focus-bg-light: rgba(255, 255, 255, 0.95);
  
  /* 按钮浅色主题变量 */
  --edit-btn-border-gradient-light: linear-gradient(45deg, #9b4dc7, #5a7fdb, #9b4dc7, #5a7fdb);
  --edit-btn-hover-shadow-light: 0 0 25px rgba(138, 43, 226, 0.4);
  --disabled-btn-bg-light: #bdc3c7;
  
  /* 分页控件浅色主题变量 */
  --pagination-bg-light: rgba(255, 255, 255, 0.8);
  --pagination-border-light: 1px solid rgba(138, 43, 226, 0.2);
  
  /* 其他UI浅色主题变量 */
  --no-articles-text-shadow-light: 0 0 10px rgba(0, 0, 0, 0.1);
  
  /* 评论输入框卡片浅色主题变量 */
  --comment-form-bg-light: rgba(255, 255, 255, 0.9);
  --comment-form-border-light: 1px solid rgba(138, 43, 226, 0.2);
  --comment-form-shadow-light: 0 0 20px rgba(138, 43, 226, 0.05);
  --comment-form-top-border-light: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.6), transparent);
  --comment-textarea-bg-light: rgba(255, 255, 255, 0.8);
  --comment-textarea-border-light: 1px solid rgba(138, 43, 226, 0.2);
  --comment-textarea-color-light: #333333;
  --comment-textarea-focus-bg-light: rgba(255, 255, 255, 0.95);
  --comment-textarea-focus-border-light: 1px solid rgba(138, 43, 226, 0.5);
  --comment-textarea-focus-shadow-light: 0 0 15px rgba(138, 43, 226, 0.1);
  --comment-submit-btn-bg-light: linear-gradient(90deg, #8b5cf6, #a855f7);
  --comment-submit-btn-shadow-light: 0 4px 15px rgba(138, 43, 226, 0.2);
  --comment-submit-btn-hover-shadow-light: 0 6px 20px rgba(138, 43, 226, 0.3);
  --comment-section-border-light: 1px solid rgba(138, 43, 226, 0.2);
  --comment-section-decoration-light: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.6), transparent);
  --comment-header-bg-light: rgba(138, 43, 226, 0.03);
  --comment-header-border-light: 1px solid rgba(138, 43, 226, 0.15);
  --comment-sort-text-light: #6b46c1;
  --comment-sort-select-bg-light: rgba(255, 255, 255, 0.9);
  --comment-sort-select-border-light: 1px solid rgba(138, 43, 226, 0.3);
  --comment-sort-select-color-light: #333333;
  --comment-sort-select-hover-bg-light: rgba(255, 255, 255, 1);
  --comment-sort-select-hover-border-light: 1px solid rgba(138, 43, 226, 0.5);
  --comment-sort-select-hover-shadow-light: 0 0 10px rgba(138, 43, 226, 0.15);
  --comment-title-color-light: #6b46c1;
  --comment-title-text-shadow-light: 0 0 10px rgba(138, 43, 226, 0.2);
  --comment-title-prefix-light: rgba(138, 43, 226, 0.5);
  
  /* 管理员中心左侧导航栏浅色主题变量 */
  --admin-sidebar-bg-light: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #e3f2fd 100%);
  --admin-sidebar-border-light: 1px solid rgba(0, 102, 204, 0.2);
  --admin-sidebar-shadow-light: 0 4px 20px rgba(0, 0, 0, 0.08);
  --admin-sidebar-text-light: #212529;
  --admin-sidebar-header-border-light: 1px solid rgba(0, 102, 204, 0.2);
  --admin-sidebar-logo-color-light: #0066cc;
  --admin-sidebar-nav-text-light: #212529;
  --admin-sidebar-nav-active-bg-light: rgba(0, 102, 204, 0.1);
  --admin-sidebar-nav-active-border-light: 1px solid rgba(0, 102, 204, 0.3);
  --admin-sidebar-nav-hover-bg-light: rgba(0, 102, 204, 0.05);
  
  /* 管理员中心页面标题浅色主题变量 */
  --admin-title-color-light: #212529;
  --admin-title-text-shadow-light: none;
  --admin-title-bg-gradient-light: linear-gradient(135deg, #0066cc, #0066cc);
  --admin-group-title-color-light: #212529;
  --admin-group-title-text-shadow-light: none;
  --admin-group-title-border-light: 4px solid #0066cc;
  --admin-management-title-color-light: #212529;
  --admin-management-title-text-shadow-light: none;
  
  /* 深色主题变量 */
  --bg-primary-dark: #0a0a0f;
  --bg-secondary-dark: #0a1128;
  --bg-tertiary-dark: #101f47;
  --text-primary-dark: #ffffff;
  --text-secondary-dark: rgba(255, 255, 255, 0.7);
  --text-accent-dark: #00aaff;
  --border-color-dark: rgba(0, 174, 255, 0.3);
  --header-bg-dark: linear-gradient(135deg, #0a1128 0%, #101f47 50%, #0a1128 100%);
  --header-glow-dark: linear-gradient(90deg, transparent, #00aaff, transparent);
  --main-bg-dark: #0a0a0f;
  --main-bg-pattern-dark: 
    radial-gradient(circle at 10% 20%, rgba(0, 174, 255, 0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(138, 43, 226, 0.1) 0%, transparent 20%);
  --footer-bg-dark: linear-gradient(135deg, #0a1128 0%, #101f47 50%, #0a1128 100%);
  --footer-text-dark: rgba(255, 255, 255, 0.7);
  --shadow-dark: 0 4px 20px rgba(0, 174, 255, 0.2);
  --highlight-dark: #00aaff;
  
  /* 模态框深色主题变量 */
  --modal-overlay-bg-dark: radial-gradient(circle, rgba(10, 10, 26, 0.95) 0%, rgba(18, 18, 46, 0.98) 100%);
  --modal-bg-dark: linear-gradient(135deg, rgba(16, 16, 36, 0.95) 0%, rgba(22, 22, 50, 0.95) 100%);
  --modal-shadow-dark: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 30px rgba(138, 43, 226, 0.4);
  --modal-border-dark: 1px solid rgba(138, 43, 226, 0.5);
  --modal-title-underline-dark: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.8), transparent);
  
  /* 输入框深色主题变量 */
  --input-border-dark: 1px solid rgba(138, 43, 226, 0.5);
  --input-bg-dark: rgba(10, 10, 26, 0.7);
  --input-shadow-dark: 0 0 10px rgba(138, 43, 226, 0.1);
  --input-focus-shadow-dark: 0 0 20px rgba(138, 43, 226, 0.3);
  --input-focus-bg-dark: rgba(16, 16, 36, 0.9);
  
  /* 按钮深色主题变量 */
  --edit-btn-border-gradient-dark: linear-gradient(45deg, #8a2be2, #4169e1, #8a2be2, #4169e1);
  --edit-btn-hover-shadow-dark: 0 0 25px rgba(138, 43, 226, 0.7);
  --disabled-btn-bg-dark: #4a5568;
  
  /* 分页控件深色主题变量 */
  --pagination-bg-dark: rgba(16, 16, 36, 0.7);
  --pagination-border-dark: 1px solid rgba(138, 43, 226, 0.3);
  
  /* 其他UI深色主题变量 */
  --no-articles-text-shadow-dark: 0 0 10px rgba(0, 0, 0, 0.5);
  
  /* 评论输入框卡片深色主题变量 */
  --comment-form-bg-dark: rgba(16, 16, 36, 0.8);
  --comment-form-border-dark: 1px solid rgba(138, 43, 226, 0.3);
  --comment-form-shadow-dark: 0 0 20px rgba(138, 43, 226, 0.1);
  --comment-form-top-border-dark: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.8), transparent);
  --comment-textarea-bg-dark: rgba(255, 255, 255, 0.05);
  --comment-textarea-border-dark: 1px solid rgba(138, 43, 226, 0.3);
  --comment-textarea-color-dark: #e0e0ff;
  --comment-textarea-focus-bg-dark: rgba(255, 255, 255, 0.08);
  --comment-textarea-focus-border-dark: 1px solid rgba(138, 43, 226, 0.8);
  --comment-textarea-focus-shadow-dark: 0 0 20px rgba(138, 43, 226, 0.2);
  --comment-submit-btn-bg-dark: linear-gradient(90deg, #8b5cf6, #a855f7);
  --comment-submit-btn-shadow-dark: 0 4px 15px rgba(138, 43, 226, 0.3);
  --comment-submit-btn-hover-shadow-dark: 0 6px 20px rgba(138, 43, 226, 0.4);
  --comment-section-border-dark: 1px solid rgba(138, 43, 226, 0.3);
  --comment-section-decoration-dark: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.8), transparent);
  --comment-header-bg-dark: rgba(138, 43, 226, 0.05);
  --comment-header-border-dark: 1px solid rgba(138, 43, 226, 0.2);
  --comment-sort-text-dark: #a855f7;
  --comment-sort-select-bg-dark: rgba(16, 16, 36, 0.8);
  --comment-sort-select-border-dark: 1px solid rgba(138, 43, 226, 0.5);
  --comment-sort-select-color-dark: #e0e0ff;
  --comment-sort-select-hover-bg-dark: rgba(16, 16, 36, 1);
  --comment-sort-select-hover-border-dark: 1px solid rgba(138, 43, 226, 0.8);
  --comment-sort-select-hover-shadow-dark: 0 0 15px rgba(138, 43, 226, 0.3);
  --comment-title-color-dark: #c084fc;
  --comment-title-text-shadow-dark: 0 0 10px rgba(138, 43, 226, 0.3);
  --comment-title-prefix-dark: rgba(138, 43, 226, 0.6);
  
  /* 管理员中心左侧导航栏深色主题变量 */
  --admin-sidebar-bg-dark: linear-gradient(135deg, #0a1128 0%, #101f47 50%, #0a1128 100%);
  --admin-sidebar-border-dark: 1px solid rgba(0, 174, 255, 0.3);
  --admin-sidebar-shadow-dark: 0 4px 20px rgba(0, 174, 255, 0.2);
  --admin-sidebar-text-dark: #ffffff;
  --admin-sidebar-header-border-dark: 1px solid rgba(0, 174, 255, 0.3);
  --admin-sidebar-logo-color-dark: #00aaff;
  --admin-sidebar-nav-text-dark: #ffffff;
  --admin-sidebar-nav-active-bg-dark: rgba(0, 174, 255, 0.1);
  --admin-sidebar-nav-active-border-dark: 1px solid rgba(0, 174, 255, 0.3);
  --admin-sidebar-nav-hover-bg-dark: rgba(0, 174, 255, 0.05);
  
  /* 管理员中心页面标题深色主题变量 */
  --admin-title-color-dark: #ffffff;
  --admin-title-text-shadow-dark: 0 0 15px rgba(0, 174, 255, 0.5);
  --admin-title-bg-gradient-dark: linear-gradient(135deg, #00aaff, #0066cc);
  --admin-group-title-color-dark: #ffffff;
  --admin-group-title-text-shadow-dark: 0 0 10px rgba(0, 174, 255, 0.7);
  --admin-group-title-border-dark: 4px solid #00aaff;
  --admin-management-title-color-dark: #ffffff;
  --admin-management-title-text-shadow-dark: 0 0 10px rgba(0, 174, 255, 0.7);
  
  /* 玄幻科技风格深色主题变量 */
  --grid-color-dark: rgba(0, 174, 255, 0.2);
  --secondary-highlight-dark: #9b4dc7;
  --highlight-glow-dark: rgba(0, 174, 255, 0.9);
  --success-color-light-dark: rgba(0, 180, 42, 0.3);
  --danger-bg-dark: rgba(245, 63, 63, 0.2);
  --danger-border-dark: rgba(245, 63, 63, 0.5);
  --danger-color-dark: #ff6b6b;
  --success-color-dark: #00d04a;
  --success-glow-dark: rgba(0, 208, 74, 0.6);
  --warning-color-dark: #ff9933;
  --disabled-color-dark: #6e7882;
  --text-accent-light-dark: #66b3ff;
  
  /* RGB颜色值深色主题版本 */
  --bg-secondary-rgb-dark: 10, 17, 40;
  --bg-tertiary-rgb-dark: 16, 31, 71;
  --highlight-rgb-dark: 0, 170, 255;
  --error-rgb-dark: 255, 107, 107;
  --border-color-dark: rgba(0, 174, 255, 0.3);
  --shadow-dark: 0 4px 20px rgba(0, 174, 255, 0.2);
}

/* 浅色主题默认样式 */
:root {
  --bg-primary: var(--bg-primary-light);
  --bg-secondary: var(--bg-secondary-light);
  --bg-tertiary: var(--bg-tertiary-light);
  --text-primary: var(--text-primary-light);
  --text-secondary: var(--text-secondary-light);
  --text-accent: var(--text-accent-light);
  --border-color: var(--border-color-light);
  --header-bg: var(--header-bg-light);
  --header-glow: var(--header-glow-light);
  --main-bg: var(--main-bg-light);
  --main-bg-pattern: var(--main-bg-pattern-light);
  --footer-bg: var(--footer-bg-light);
  --footer-text: var(--footer-text-light);
  --shadow: var(--shadow-light);
  --highlight: var(--highlight-light);
  --secondary-highlight: var(--secondary-highlight-light);
  
  /* 模态框浅色主题变量映射 */
  --modal-overlay-bg: var(--modal-overlay-bg-light);
  --modal-bg: var(--modal-bg-light);
  --modal-shadow: var(--modal-shadow-light);
  --modal-border: var(--modal-border-light);
  --modal-title-underline: var(--modal-title-underline-light);
  
  /* 输入框浅色主题变量映射 */
  --input-border: var(--input-border-light);
  --input-bg: var(--input-bg-light);
  --input-shadow: var(--input-shadow-light);
  --input-focus-shadow: var(--input-focus-shadow-light);
  --input-focus-bg: var(--input-focus-bg-light);
  
  /* 按钮浅色主题变量映射 */
  --edit-btn-border-gradient: var(--edit-btn-border-gradient-light);
  --edit-btn-hover-shadow: var(--edit-btn-hover-shadow-light);
  --disabled-btn-bg: var(--disabled-btn-bg-light);
  
  /* 分页控件浅色主题变量映射 */
  --pagination-bg: var(--pagination-bg-light);
  --pagination-border: var(--pagination-border-light);
  
  /* 其他UI浅色主题变量映射 */
  --no-articles-text-shadow: var(--no-articles-text-shadow-light);
  
  /* 评论输入框卡片浅色主题变量映射 */
  --comment-form-bg: var(--comment-form-bg-light);
  --comment-form-border: var(--comment-form-border-light);
  --comment-form-shadow: var(--comment-form-shadow-light);
  --comment-form-top-border: var(--comment-form-top-border-light);
  --comment-textarea-bg: var(--comment-textarea-bg-light);
  --comment-textarea-border: var(--comment-textarea-border-light);
  --comment-textarea-color: var(--comment-textarea-color-light);
  --comment-textarea-focus-bg: var(--comment-textarea-focus-bg-light);
  --comment-textarea-focus-border: var(--comment-textarea-focus-border-light);
  --comment-textarea-focus-shadow: var(--comment-textarea-focus-shadow-light);
  --comment-submit-btn-bg: var(--comment-submit-btn-bg-light);
  --comment-submit-btn-shadow: var(--comment-submit-btn-shadow-light);
  --comment-submit-btn-hover-shadow: var(--comment-submit-btn-hover-shadow-light);
  --comment-section-border: var(--comment-section-border-light);
  --comment-section-decoration: var(--comment-section-decoration-light);
  --comment-header-bg: var(--comment-header-bg-light);
  --comment-header-border: var(--comment-header-border-light);
  --comment-sort-text: var(--comment-sort-text-light);
  --comment-sort-select-bg: var(--comment-sort-select-bg-light);
  --comment-sort-select-border: var(--comment-sort-select-border-light);
  --comment-sort-select-color: var(--comment-sort-select-color-light);
  --comment-sort-select-hover-bg: var(--comment-sort-select-hover-bg-light);
  --comment-sort-select-hover-border: var(--comment-sort-select-hover-border-light);
  --comment-sort-select-hover-shadow: var(--comment-sort-select-hover-shadow-light);
  --comment-title-color: var(--comment-title-color-light);
  --comment-title-text-shadow: var(--comment-title-text-shadow-light);
  --comment-title-prefix: var(--comment-title-prefix-light);
  
  /* 管理员中心左侧导航栏浅色主题变量映射 */
  --admin-sidebar-bg: var(--admin-sidebar-bg-light);
  --admin-sidebar-border: var(--admin-sidebar-border-light);
  --admin-sidebar-shadow: var(--admin-sidebar-shadow-light);
  --admin-sidebar-text: var(--admin-sidebar-text-light);
  --admin-sidebar-header-border: var(--admin-sidebar-header-border-light);
  --admin-sidebar-logo-color: var(--admin-sidebar-logo-color-light);
  --admin-sidebar-nav-text: var(--admin-sidebar-nav-text-light);
  --admin-sidebar-nav-active-bg: var(--admin-sidebar-nav-active-bg-light);
  --admin-sidebar-nav-active-border: var(--admin-sidebar-nav-active-border-light);
  --admin-sidebar-nav-hover-bg: var(--admin-sidebar-nav-hover-bg-light);
  
  /* 管理员中心页面标题浅色主题变量映射 */
  --admin-title-color: var(--admin-title-color-light);
  --admin-title-text-shadow: var(--admin-title-text-shadow-light);
  --admin-title-bg-gradient: var(--admin-title-bg-gradient-light);
  --admin-group-title-color: var(--admin-group-title-color-light);
  --admin-group-title-text-shadow: var(--admin-group-title-text-shadow-light);
  --admin-group-title-border: var(--admin-group-title-border-light);
  --admin-management-title-color: var(--admin-management-title-color-light);
  --admin-management-title-text-shadow: var(--admin-management-title-text-shadow-light);
}

/* 深色主题样式 */
.dark-theme {
  --bg-primary: var(--bg-primary-dark);
  --bg-secondary: var(--bg-secondary-dark);
  --bg-tertiary: var(--bg-tertiary-dark);
  --text-primary: var(--text-primary-dark);
  --text-secondary: var(--text-secondary-dark);
  --text-accent: var(--text-accent-dark);
  --border-color: var(--border-color-dark);
  --header-bg: var(--header-bg-dark);
  --header-glow: var(--header-glow-dark);
  --main-bg: var(--main-bg-dark);
  --main-bg-pattern: var(--main-bg-pattern-dark);
  --footer-bg: var(--footer-bg-dark);
  --footer-text: var(--footer-text-dark);
  --shadow: var(--shadow-dark);
  --highlight: var(--highlight-dark);
  
  /* 玄幻科技风格深色主题变量映射 */
  --grid-color: var(--grid-color-dark);
  --secondary-highlight: var(--secondary-highlight-dark);
  --highlight-glow: var(--highlight-glow-dark);
  --success-color-light: var(--success-color-light-dark);
  --danger-bg: var(--danger-bg-dark);
  --danger-border: var(--danger-border-dark);
  --danger-color: var(--danger-color-dark);
  --success-color: var(--success-color-dark);
  --success-glow: var(--success-glow-dark);
  --warning-color: var(--warning-color-dark);
  --disabled-color: var(--disabled-color-dark);
  --text-accent-light: var(--text-accent-light-dark);
  
  /* RGB颜色值深色主题映射 */
  --bg-secondary-rgb: var(--bg-secondary-rgb-dark);
  --bg-tertiary-rgb: var(--bg-tertiary-rgb-dark);
  --highlight-rgb: var(--highlight-rgb-dark);
  --error-rgb: var(--error-rgb-dark);
  
  /* 模态框深色主题变量映射 */
  --modal-overlay-bg: var(--modal-overlay-bg-dark);
  --modal-bg: var(--modal-bg-dark);
  --modal-shadow: var(--modal-shadow-dark);
  --modal-border: var(--modal-border-dark);
  --modal-title-underline: var(--modal-title-underline-dark);
  
  /* 管理员中心左侧导航栏深色主题变量映射 */
  --admin-sidebar-bg: var(--admin-sidebar-bg-dark);
  --admin-sidebar-border: var(--admin-sidebar-border-dark);
  --admin-sidebar-shadow: var(--admin-sidebar-shadow-dark);
  --admin-sidebar-text: var(--admin-sidebar-text-dark);
  --admin-sidebar-header-border: var(--admin-sidebar-header-border-dark);
  --admin-sidebar-logo-color: var(--admin-sidebar-logo-color-dark);
  --admin-sidebar-nav-text: var(--admin-sidebar-nav-text-dark);
  --admin-sidebar-nav-active-bg: var(--admin-sidebar-nav-active-bg-dark);
  --admin-sidebar-nav-active-border: var(--admin-sidebar-nav-active-border-dark);
  --admin-sidebar-nav-hover-bg: var(--admin-sidebar-nav-hover-bg-dark);
  
  /* 管理员中心页面标题深色主题变量映射 */
  --admin-title-color: var(--admin-title-color-dark);
  --admin-title-text-shadow: var(--admin-title-text-shadow-dark);
  --admin-title-bg-gradient: var(--admin-title-bg-gradient-dark);
  --admin-group-title-color: var(--admin-group-title-color-dark);
  --admin-group-title-text-shadow: var(--admin-group-title-text-shadow-dark);
  --admin-group-title-border: var(--admin-group-title-border-dark);
  --admin-management-title-color: var(--admin-management-title-color-dark);
  --admin-management-title-text-shadow: var(--admin-management-title-text-shadow-dark);
  
  /* 输入框深色主题变量映射 */
  --input-border: var(--input-border-dark);
  --input-bg: var(--input-bg-dark);
  --input-shadow: var(--input-shadow-dark);
  --input-focus-shadow: var(--input-focus-shadow-dark);
  --input-focus-bg: var(--input-focus-bg-dark);
  
  /* 按钮深色主题变量映射 */
  --edit-btn-border-gradient: var(--edit-btn-border-gradient-dark);
  --edit-btn-hover-shadow: var(--edit-btn-hover-shadow-dark);
  --disabled-btn-bg: var(--disabled-btn-bg-dark);
  
  /* 分页控件深色主题变量映射 */
  --pagination-bg: var(--pagination-bg-dark);
  --pagination-border: var(--pagination-border-dark);
  
  /* 其他UI深色主题变量映射 */
  --no-articles-text-shadow: var(--no-articles-text-shadow-dark);
  
  /* 评论输入框卡片深色主题变量映射 */
  --comment-form-bg: var(--comment-form-bg-dark);
  --comment-form-border: var(--comment-form-border-dark);
  --comment-form-shadow: var(--comment-form-shadow-dark);
  --comment-form-top-border: var(--comment-form-top-border-dark);
  --comment-textarea-bg: var(--comment-textarea-bg-dark);
  --comment-textarea-border: var(--comment-textarea-border-dark);
  --comment-textarea-color: var(--comment-textarea-color-dark);
  --comment-textarea-focus-bg: var(--comment-textarea-focus-bg-dark);
  --comment-textarea-focus-border: var(--comment-textarea-focus-border-dark);
  --comment-textarea-focus-shadow: var(--comment-textarea-focus-shadow-dark);
  --comment-submit-btn-bg: var(--comment-submit-btn-bg-dark);
  --comment-submit-btn-shadow: var(--comment-submit-btn-shadow-dark);
  --comment-submit-btn-hover-shadow: var(--comment-submit-btn-hover-shadow-dark);
  --comment-section-border: var(--comment-section-border-dark);
  --comment-section-decoration: var(--comment-section-decoration-dark);
  --comment-header-bg: var(--comment-header-bg-dark);
  --comment-header-border: var(--comment-header-border-dark);
  --comment-sort-text: var(--comment-sort-text-dark);
  --comment-sort-select-bg: var(--comment-sort-select-bg-dark);
  --comment-sort-select-border: var(--comment-sort-select-border-dark);
  --comment-sort-select-color: var(--comment-sort-select-color-dark);
  --comment-sort-select-hover-bg: var(--comment-sort-select-hover-bg-dark);
  --comment-sort-select-hover-border: var(--comment-sort-select-hover-border-dark);
  --comment-sort-select-hover-shadow: var(--comment-sort-select-hover-shadow-dark);
  --comment-title-color: var(--comment-title-color-dark);
  --comment-title-text-shadow: var(--comment-title-text-shadow-dark);
  --comment-title-prefix: var(--comment-title-prefix-dark);
}

/* 全局过渡效果 */
* {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease, background-image 0.3s ease;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 玄幻科技风格导航栏 */
.header {
  background: var(--header-bg);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
}

/* 科技光效装饰 */
.header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--header-glow);
  animation: glow 4s ease-in-out infinite;
}

@keyframes glow {
  0% { opacity: 0.3; }
  50% { opacity: 1; }
  100% { opacity: 0.3; }
}

.nav {
  padding: 12px 0;
  position: relative;
}

.nav .container {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 科技风格Logo */
.logo {
  margin-right: auto;
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(135deg, var(--text-accent) 0%, var(--highlight) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
  text-decoration: none;
  position: relative;
  padding: 5px 0;
}

.logo::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, #00aaff, #8a2be2);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.logo:hover::after {
  transform: scaleX(1);
}

/* 搜索功能样式 - 科技感 */
.search-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.05);
  padding: 5px;
  border-radius: 25px;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(5px);
}

/* 搜索类型下拉菜单 */
.search-type-dropdown {
  position: relative;
  min-width: 120px;
}

.search-type-select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 14px;
  color: var(--text-accent);
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.search-type-select:hover {
  border-color: var(--highlight);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 10px var(--text-accent);
}

.search-type-menu {
  position: absolute;
  top: 110%;
  left: 0;
  min-width: 100%;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  z-index: 100;
  margin-top: 4px;
  padding: 4px 0;
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-type-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.search-type-item {
  padding: 10px 16px;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.search-type-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(to bottom, var(--text-accent), var(--highlight));
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.search-type-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-accent);
  padding-left: 20px;
}

.search-type-item:hover::before {
  transform: scaleY(1);
}

.search-type-item.active {
  background: linear-gradient(135deg, rgba(var(--text-accent-rgb), 0.2) 0%, rgba(var(--highlight), 0.2) 100%);
  color: var(--text-primary);
  padding-left: 20px;
  font-weight: 600;
}

.search-type-item.active::before {
  transform: scaleY(1);
}

.search-input {
  flex: 1;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.search-input::placeholder {
  color: var(--text-secondary);
}

.search-input:focus {
  border-color: var(--text-accent);
  box-shadow: 0 0 0 2px rgba(var(--text-accent-rgb), 0.3);
  background: rgba(255, 255, 255, 0.1);
}

.search-button {
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--text-accent) 0%, var(--highlight) 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.search-button::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transform: rotate(45deg);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%) rotate(45deg); }
  100% { transform: translateX(100%) rotate(45deg); }
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px var(--text-accent);
}

/* 导航链接 - 科技感 */
.nav-links {
  display: flex;
  gap: 10px;
  align-items: center;
}

.nav-link {
  color: var(--text-primary);
  text-decoration: none;
  padding: 8px 16px;
  font-size: 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(135deg, rgba(var(--text-accent-rgb), 0.2) 0%, rgba(var(--highlight), 0.2) 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.nav-link:hover::before {
  transform: scaleX(1);
}

.nav-link:hover {
  color: var(--text-accent);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 10px rgba(var(--text-accent-rgb), 0.3);
}

/* 导航链接激活状态 - 科技感 */
.nav-link.router-link-active {
  color: var(--text-primary);
  background: linear-gradient(135deg, rgba(var(--text-accent-rgb), 0.3) 0%, rgba(var(--highlight), 0.3) 100%);
  box-shadow: 0 0 15px rgba(var(--text-accent-rgb), 0.4), 0 0 0 1px rgba(var(--text-accent-rgb), 0.6);
  font-weight: 600;
}

.nav-link.router-link-active::before {
  transform: scaleX(1);
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--text-accent), var(--highlight), transparent);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

/* 用户菜单样式 - 玄幻科技风 */
.user-menu-container {
  position: relative;
  display: inline-block;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(var(--text-accent-rgb), 0.2);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 15px rgba(var(--text-accent-rgb), 0.4);
  border-color: var(--text-accent);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--text-accent);
  transition: all 0.3s ease;
  box-shadow: 0 0 10px rgba(var(--text-accent-rgb), 0.5);
}

.user-info:hover .user-avatar {
  border-color: var(--highlight);
  box-shadow: 0 0 15px rgba(var(--highlight), 0.6);
  transform: scale(1.05);
}

.user-nickname {
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.dropdown-arrow {
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid var(--text-accent);
  margin-left: 8px;
  transition: all 0.3s ease;
}

.dropdown-arrow.active {
  transform: rotate(180deg);
  border-top-color: var(--highlight);
}

/* 科技风下拉菜单 */
.user-dropdown-menu {
  position: absolute;
  top: 110%;
  left: 50%;
  transform: translateX(-50%) translateY(-10px);
  min-width: 200px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  z-index: 1000;
  margin-top: 4px;
  padding: 8px 0;
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow);
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-dropdown-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 12px 20px;
  border: none;
  background: none;
  text-align: left;
  font-size: 15px;
  color: var(--text-primary);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.dropdown-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(to bottom, var(--text-accent), var(--highlight));
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-accent);
  padding-left: 25px;
}

/* 下拉菜单中导航链接激活状态 */
.dropdown-item.router-link-active {
  background: linear-gradient(135deg, rgba(0, 174, 255, 0.2) 0%, rgba(138, 43, 226, 0.2) 100%);
  color: var(--text-primary);
  padding-left: 25px;
  font-weight: 600;
}

.dropdown-item.router-link-active::before {
  transform: scaleY(1);
}

.dropdown-item:hover::before {
  transform: scaleY(1);
}

.dropdown-divider {
  height: 1px;
  margin: 6px 0;
  background: linear-gradient(90deg, transparent, rgba(var(--text-accent-rgb), 0.3), transparent);
}

.logout-item {
  color: var(--error-text-color);
}

.logout-item:hover {
  color: var(--error-text-color);
  background: rgba(var(--error-rgb), 0.1);
}

.logout-item::before {
  background: linear-gradient(to bottom, var(--error-text-color), rgba(var(--error-rgb), 0.8));
}

/* 主内容区样式 */
.main {
  padding: 40px 0;
  min-height: calc(100vh - 180px);
  background: var(--main-bg);
  background-image: var(--main-bg-pattern);
}

/* 页脚样式 - 科技感 */
.footer {
  background: var(--footer-bg);
  padding: 25px 0;
  text-align: center;
  color: var(--footer-text);
  border-top: 1px solid var(--border-color);
  position: relative;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--header-glow);
}

.footer p {
  margin: 0;
  font-size: 14px;
  letter-spacing: 0.5px;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 导航栏和下拉菜单的响应式适配 */
@media (max-width: 768px) {
  .user-dropdown-menu {
    left: auto;
    right: 0;
    transform: none;
    min-width: 180px;
  }
  
  .user-dropdown-menu.show {
    transform: translateY(0);
  }
}

@media (max-width: 480px) {
  .nav {
    padding: 8px 0;
  }
  
  .logo {
    font-size: 20px;
  }
  
  .nav-link {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
  }
  
  .search-input,
  .search-button,
  .search-type-select {
    font-size: 14px;
    padding: 6px 12px;
  }
}
</style>
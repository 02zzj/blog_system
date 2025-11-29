<template>
  <div class="app">
    <header class="header">
      <nav class="nav">
        <div class="container">
          <router-link to="/" class="logo">博客系统</router-link>
          
          <!-- 移动端菜单按钮 -->
          <button class="mobile-menu-btn" :class="{ 'active': showMobileMenu }" @click="toggleMobileMenu" aria-label="菜单">
            <span class="menu-icon"></span>
            <span class="menu-icon"></span>
            <span class="menu-icon"></span>
          </button>
          
          <!-- 搜索功能组件 -->
          <div class="search-container">
            <select v-model="searchType" class="search-type-select">
              <option value="all">全部搜索</option>
              <option value="title_content">文章内容</option>
              <option value="author">作者昵称</option>
            </select>
            <input 
              type="text" 
              v-model="searchKeyword" 
              :placeholder="getSearchPlaceholder()" 
              class="search-input"
              @keyup.enter="handleSearch"
            >
            <button @click="handleSearch" class="search-button">搜索</button>
          </div>
          
          <div class="nav-links" :class="{ 'mobile-menu-open': showMobileMenu }">
            <router-link to="/" class="nav-link" @click.native="handleHomeClick; showMobileMenu = false">首页</router-link>
            <router-link v-if="isLoggedIn" to="/create" class="nav-link" @click.native="showMobileMenu = false">写文章</router-link>
            <router-link v-if="isLoggedIn" to="/profile" class="nav-link" @click.native="showMobileMenu = false">个人中心</router-link>
            <div v-else>
              <router-link to="/login" class="nav-link" @click.native="showMobileMenu = false">登录</router-link>
              <router-link to="/register" class="nav-link" @click.native="showMobileMenu = false">注册</router-link>
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
              <div v-if="showUserMenu" class="user-dropdown-menu">
                <router-link to="/profile" class="dropdown-item" @click="handleMenuItemClick">个人中心</router-link>
                <router-link to="/create" class="dropdown-item" @click="handleMenuItemClick">写文章</router-link>
                <!-- 管理员中心选项，仅对管理员显示 -->
                <router-link v-if="user?.role === 'ADMIN'" to="/admin/dashboard" class="dropdown-item" @click="handleMenuItemClick">管理员中心</router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout-item" @click="handleLogout">退出登录</button>
              </div>
            </div>
          </div>
        </div>
      </nav>
    </header>
    
    <main class="main">
      <div class="container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
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
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from './axios'

export default {
  name: 'App',
  setup() {
    const isLoggedIn = ref(false)
    const user = ref(null)
    const showUserMenu = ref(false)
    const showMobileMenu = ref(false)
    const router = useRouter()
    const homeResetEvent = ref(false)
    
    // 搜索相关数据
    const searchKeyword = ref('')
    const searchType = ref('all')
    
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
      showMobileMenu.value = false
      router.push('/')
    }
    
    const handleLogout = () => {
      logout()
      showMobileMenu.value = false
    }
    
    const handleMenuItemClick = () => {
      showUserMenu.value = false
      showMobileMenu.value = false
    }
    
    // 切换用户菜单显示状态
    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }
    
    // 切换移动端菜单显示状态
    const toggleMobileMenu = () => {
      showMobileMenu.value = !showMobileMenu.value
    }
    
    // 点击其他地方关闭用户菜单和移动端菜单
    const handleClickOutside = (event) => {
      if (!event.target.closest('.user-menu-container') && !event.target.closest('.mobile-menu-btn')) {
        showUserMenu.value = false
      }
      if (!event.target.closest('.nav-links') && !event.target.closest('.mobile-menu-btn')) {
        showMobileMenu.value = false
      }
    }

    onMounted(() => {
      checkLoginStatus()
      
      // 全局设置axios的token
      const token = localStorage.getItem('token')
      if (token) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
      
      // 监听路由变化，在每次路由切换后检查登录状态
      router.afterEach(() => {
        checkLoginStatus()
        showMobileMenu.value = false
      })
      
      // 监听点击事件，点击其他地方关闭用户菜单和移动端菜单
      document.addEventListener('click', handleClickOutside)
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
      
      // 不再设置homeResetFlag，避免与路由变化监听器造成重复加载
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
    
    return {
      isLoggedIn,
      user,
      showUserMenu,
      showMobileMenu,
      logout,
      handleLogout,
      handleMenuItemClick,
      toggleUserMenu,
      toggleMobileMenu,
      handleHomeClick,
      searchKeyword,
      searchType,
      getSearchPlaceholder,
      handleSearch
    }
  }
}
</script>

<style>
/* 重置默认样式并设置基础布局 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

/* App容器使用flex布局确保页脚固定在底部 */
.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav {
  padding: 15px 0; /* 调整导航栏垂直内边距 */
}

.nav .container {
  display: flex;
  align-items: center;
  gap: 15px; /* 减小项目间距 */
}

.logo {
  margin-right: auto;
}

/* 搜索功能样式 */
.search-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  max-width: 500px;
}

.search-type-select {
  padding: 10px 16px; /* 进一步增大内边距 */
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 17px; /* 进一步增大字体 */
  outline: none;
  background-color: white;
  cursor: pointer;
}

.search-type-select:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.search-input {
  flex: 1;
  padding: 10px 16px; /* 进一步增大内边距 */
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 18px; /* 进一步增大字体 */
  outline: none;
}

.search-input:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.search-button {
  padding: 10px 20px; /* 进一步增大内边距 */
  background-color: #ffffff;
  color: #000000;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 18px; /* 进一步增大字体 */
  cursor: pointer;
  transition: all 0.3s;
}

.search-button:hover {
  background-color: #f5f5f5;
  border-color: #40a9ff;
}

.logo {
  font-size: 32px; /* 进一步增大logo字体 */
  font-weight: bold;
  color: #333;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 15px; /* 减小导航链接间距 */
  align-items: center;
}

.nav-link {
  color: #333;
  text-decoration: none;
  padding: 8px 14px; /* 增大内边距 */
  font-size: 18px; /* 进一步增大字体 */
  border-radius: 4px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  background-color: #f5f5f5;
}

/* 导航链接选中状态样式 */
.nav-link.router-link-active {
  color: #1890ff;
  font-weight: 500;
  background-color: #f0f9ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1);
}

/* 精确匹配当前路径的样式 */
.nav-link.router-link-exact-active {
  color: white;
  background-color: #1890ff;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

/* 移动端菜单按钮样式 */
.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  padding: 10px;
  cursor: pointer;
  z-index: 1001;
}

.menu-icon {
  display: block;
  width: 24px;
  height: 2px;
  background-color: #333;
  margin: 5px 0;
  transition: all 0.3s ease;
}

/* 移动端菜单按钮激活状态 */
.mobile-menu-btn.active .menu-icon:nth-child(1) {
  transform: rotate(45deg) translate(6px, 6px);
}

.mobile-menu-btn.active .menu-icon:nth-child(2) {
  opacity: 0;
}

.mobile-menu-btn.active .menu-icon:nth-child(3) {
  transform: rotate(-45deg) translate(6px, -6px);
}

.logout-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #ff7875;
}

/* 用户菜单样式 */
.user-menu-container {
  position: relative;
  display: inline-block;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px; /* 减小内边距 */
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative; /* 为下拉菜单居中定位做准备 */
}

.user-info:hover {
  background-color: #f5f5f5;
}

.user-avatar {
  width: 40px; /* 进一步增大头像 */
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e8e8e8;
}

.user-nickname {
  font-size: 18px; /* 进一步增大字体 */
  color: #333;
  white-space: nowrap;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-arrow {
  width: 0;
  height: 0;
  border-left: 6px solid transparent; /* 进一步增大箭头 */
  border-right: 6px solid transparent;
  border-top: 6px solid #666;
  margin-left: 4px;
  transition: transform 0.3s;
}

.dropdown-arrow.active {
  transform: rotate(180deg);
}

.user-dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%; /* 居中对齐 */
  transform: translateX(-50%); /* 向左移动50%宽度实现居中 */
  min-width: 180px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  z-index: 1001; /* 提高z-index，确保显示在移动端菜单上方 */
  margin-top: 4px;
  padding: 6px 0;
  /* 确保下拉菜单不会被父容器裁剪 */
  overflow: visible;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 12px 20px; /* 进一步增大内边距 */
  border: none;
  background: none;
  text-align: center; /* 修改为居中显示 */
  font-size: 18px; /* 进一步增大字体 */
  color: #333;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.3s;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.dropdown-divider {
  height: 1px;
  margin: 4px 0;
  background-color: #e8e8e8;
}

.logout-item {
  color: #ff4d4f;
}

.logout-item:hover {
  color: #ff4d4f;
  background-color: #fff2f0;
}

/* 主内容区域使用flex-grow占据剩余空间，确保页脚被推到底部 */
.main {
  flex: 1;
  padding: 40px 0;
}

/* 页脚样式 */
.footer {
  background-color: #f5f5f5;
  padding: 20px 0;
  text-align: center;
  color: #666;
  margin-top: auto;
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

/* 响应式样式 */
@media (max-width: 768px) {
  .nav .container {
    flex-direction: row;
    align-items: center;
    gap: 10px;
    position: relative;
  }
  
  /* 显示移动端菜单按钮 */
  .mobile-menu-btn {
    display: block;
  }
  
  .logo {
    margin-right: auto;
    text-align: left;
    font-size: 24px;
  }
  
  /* 搜索容器样式调整 */
  .search-container {
    max-width: 300px;
    flex: 1;
  }
  
  .search-type-select,
  .search-input,
  .search-button {
    padding: 6px 10px;
    font-size: 14px;
  }
  
  /* 移动端导航链接样式 */
  .nav-links {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background-color: white;
    flex-direction: column;
    align-items: stretch;
    gap: 0;
    padding: 0;
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    z-index: 1000;
  }
  
  /* 移动端菜单展开样式 */
  .nav-links.mobile-menu-open {
    max-height: 500px;
    overflow: visible; /* 确保下拉菜单不会被裁剪 */
  }
  
  /* 移动端导航链接样式 */
  .nav-links .nav-link {
    padding: 12px 20px;
    font-size: 16px;
    border-bottom: 1px solid #f0f0f0;
    display: block;
  }
  
  .nav-links .nav-link:last-child {
    border-bottom: none;
  }
  
  /* 用户菜单容器在移动端的样式 */
  .nav-links .user-menu-container {
    padding: 12px 20px;
    border-top: 1px solid #f0f0f0;
    position: relative; /* 确保下拉菜单相对于此容器定位 */
    overflow: visible; /* 确保下拉菜单不会被裁剪 */
  }
  
  .user-nickname {
    font-size: 16px;
  }
  
  .user-avatar {
    width: 36px;
    height: 36px;
  }
  
  .main {
    padding: 20px 0;
  }
}

@media (max-width: 480px) {
  /* 隐藏搜索容器，在移动端菜单中显示 */
  .search-container {
    display: none;
  }
  
  /* 移动端菜单中的搜索容器 */
  .nav-links.mobile-menu-open .search-container {
    display: flex;
    padding: 12px 20px;
    border-bottom: 1px solid #f0f0f0;
    max-width: 100%;
  }
  
  .nav-links.mobile-menu-open .search-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .nav-links.mobile-menu-open .search-type-select,
  .nav-links.mobile-menu-open .search-input,
  .nav-links.mobile-menu-open .search-button {
    width: 100%;
  }
  
  .nav-link {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .user-dropdown-menu {
    left: 0;
    transform: none;
    min-width: 150px;
  }
  
  .dropdown-item {
    padding: 10px 16px;
    font-size: 16px;
  }
}
</style>
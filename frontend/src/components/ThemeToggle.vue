<template>
  <button 
    class="theme-toggle"
    :class="{ 'active': isDarkMode }"
    @click="toggleTheme"
    aria-label="切换浅色/深色主题"
    :aria-pressed="isDarkMode"
  >
    <div class="toggle-icon">
      <!-- 太阳图标（浅色模式） -->
      <svg v-if="!isDarkMode" class="sun-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="5"/>
        <line x1="12" y1="1" x2="12" y2="3"/>
        <line x1="12" y1="21" x2="12" y2="23"/>
        <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
        <line x1="1" y1="12" x2="3" y2="12"/>
        <line x1="21" y1="12" x2="23" y2="12"/>
        <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
        <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
      </svg>
      <!-- 月亮图标（深色模式） -->
      <svg v-else class="moon-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
      </svg>
    </div>
    <span class="toggle-label">{{ isDarkMode ? '深色模式' : '浅色模式' }}</span>
  </button>
</template>

<script>
import { ref, onMounted, watch } from 'vue'

export default {
  name: 'ThemeToggle',
  emits: ['theme-change', 'themeChanged'],
  setup(props, { emit }) {
    const isDarkMode = ref(false)

    // 初始化主题
    const initTheme = () => {
      // 从localStorage获取主题设置，如果没有则默认为浅色模式
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme === 'dark') {
        isDarkMode.value = true
        updateThemeClass()
      } else {
        isDarkMode.value = false
        updateThemeClass()
      }
    }

    // 更新主题类
    const updateThemeClass = () => {
      // 应用主题类到html元素
      document.documentElement.classList.toggle('dark-theme', isDarkMode.value)
    }

    // 切换主题
    const toggleTheme = () => {
      isDarkMode.value = !isDarkMode.value
      
      // 更新localStorage
      localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
      
      // 应用主题类
      updateThemeClass()
      
      // 发送主题变化事件
      emit('theme-change', isDarkMode.value)
      emit('themeChanged', isDarkMode.value ? 'dark' : 'light')
    }

    // 组件挂载时初始化主题
    onMounted(() => {
      initTheme()
    })

    // 监听外部主题变化
    watch(isDarkMode, (newVal) => {
      updateThemeClass()
    })

    return {
      isDarkMode,
      toggleTheme
    }
  }
}
</script>

<style scoped>
.theme-toggle {
  position: fixed;
  bottom: 30px;
  right: 30px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  min-width: 150px;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(var(--highlight), 0.2);
}

.theme-toggle:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 35px rgba(var(--highlight), 0.3);
  border-color: rgba(var(--highlight), 0.4);
}

.theme-toggle.active {
  background: linear-gradient(135deg, rgba(var(--bg-secondary-rgb), 0.9) 0%, rgba(var(--bg-tertiary-rgb), 0.9) 100%);
  border-color: rgba(var(--highlight), 0.4);
}

.toggle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  transition: transform 0.3s ease;
}

.theme-toggle:hover .toggle-icon {
  transform: rotate(15deg) scale(1.1);
}

.sun-icon, .moon-icon {
  width: 24px;
  height: 24px;
  color: var(--text-accent);
  transition: all 0.3s ease;
}

.theme-toggle:hover .sun-icon,
.theme-toggle:hover .moon-icon {
  color: var(--text-accent-light);
}

.toggle-label {
  font-size: 14px;
  font-weight: 600;
  transition: color 0.3s ease;
}

.theme-toggle:hover .toggle-label {
  color: var(--text-accent);
}

/* 动画效果 */
@keyframes pulse {
  0% { box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2); }
  50% { box-shadow: 0 10px 30px rgba(var(--highlight), 0.4); }
  100% { box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2); }
}

.theme-toggle:active {
  transform: translateY(0);
  animation: pulse 0.5s ease-in-out;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-toggle {
    bottom: 20px;
    right: 20px;
    padding: 10px 14px;
    min-width: auto;
  }
  
  .toggle-label {
    display: none;
  }
}

@media (max-width: 480px) {
  .theme-toggle {
    bottom: 15px;
    right: 15px;
    width: 45px;
    height: 45px;
    padding: 0;
  }
}
</style>
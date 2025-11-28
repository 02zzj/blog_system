<template>
  <div class="profile">
    <h1>个人中心</h1>
    
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="user" class="profile-content">
      <div class="profile-info">
        <div class="avatar-container">
          <img :src="getAvatarUrl()" alt="用户头像" class="avatar">
        </div>
        <h2>用户信息</h2>
        <div class="info-item">
          <label>昵称：</label>
          <span>{{ user.nickname }}</span>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span>{{ user.email }}</span>
        </div>
        <div class="info-item">
          <label>简介：</label>
          <span>{{ user.introduction || '暂无简介' }}</span>
        </div>
        <div class="info-item">
          <label>注册时间：</label>
          <span>{{ formatDate(user.createdAt) }}</span>
        </div>
        <button @click="showEditForm = true" class="edit-btn">编辑资料</button>
      </div>
      
      <div class="my-articles">
        <div class="articles-header">
          <h2>我的文章</h2>
          <div class="sort-controls">
            <label>排序方式：</label>
            <select v-model="sortOrder" @change="handleSortChange">
              <option value="desc">最新优先</option>
              <option value="asc">最早优先</option>
            </select>
          </div>
        </div>
        <div v-if="articles.length === 0" class="no-articles">暂无文章</div>
        <div v-else class="article-list">
          <div v-for="article in articles" :key="article.id" class="article-card">
            <router-link :to="'/article/' + article.id" class="article-link">
              <h3 class="article-title">{{ article.title }}</h3>
              <div class="article-meta">
                <span class="date">{{ formatDate(article.updatedAt) }}</span>
              </div>
            </router-link>
            <div class="article-actions">
              <router-link :to="'/create/' + article.id" class="edit-btn">编辑</router-link>
              <button @click="deleteArticle(article.id)" class="delete-btn">删除</button>
            </div>
          </div>
        </div>
        
        <div class="pagination" v-if="articles.length > 0">
          <button @click="prevPage" :disabled="currentPage <= 1">上一页</button>
          <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
          <button @click="nextPage" :disabled="currentPage >= totalPages">下一页</button>
        </div>
      </div>
    </div>
    
    <!-- 编辑用户信息模态框 -->
    <div v-if="showEditForm" class="modal-overlay" @click.self="cancelEdit">
      <div class="edit-form modal">
        <h3>编辑个人资料</h3>
        <div class="form-item">
          <label>头像：</label>
          <div class="avatar-upload">
            <img :src="getAvatarUrl()" alt="当前头像" class="preview-avatar">
            <input 
              ref="avatarInput"
              type="file" 
              accept="image/jpeg,image/png" 
              style="display: none"
              @change="handleAvatarUpload"
            >
            <button @click="triggerAvatarUpload" type="button" class="upload-btn">
              更换头像
            </button>
          </div>
          <div v-if="avatarLoading" class="upload-loading">正在上传...</div>
          <div v-if="avatarError" class="upload-error">{{ avatarError }}</div>
        </div>
        
        <div class="form-item">
          <label>昵称：</label>
          <div class="input-with-feedback">
            <input 
              v-model="editForm.nickname" 
              type="text" 
              placeholder="请输入昵称"
              :class="{ 'input-error': nicknameError, 'input-success': nicknameAvailable === true }"
            >
            <div v-if="nicknameAvailable === true && editForm.nickname" class="validation-success">
              昵称可用
            </div>
            <div v-if="nicknameError" class="validation-error">
              {{ nicknameError }}
            </div>
          </div>
        </div>
        <div class="form-item">
          <label>简介：</label>
          <textarea v-model="editForm.introduction" placeholder="请输入简介" rows="4"></textarea>
        </div>
        <div class="form-actions">
          <button @click="handleUpdate" :disabled="updateLoading" class="save-btn">
            {{ updateLoading ? '保存中...' : '保存' }}
          </button>
          <button @click="cancelEdit" class="cancel-btn">取消</button>
        </div>
        <div v-if="updateError" class="update-error">{{ updateError }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import axios from '../axios'

export default {
  name: 'Profile',
  setup() {
    const user = ref(null)
    const articles = ref([])
    const loading = ref(false)
    const error = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const totalPages = ref(1)
    const sortOrder = ref('desc') // 默认降序，最新优先
    // 编辑表单相关
    const showEditForm = ref(false)
    const editForm = ref({
      nickname: '',
      introduction: '',
      avatar: ''
    })
    const nicknameAvailable = ref(null) // null: 未验证, true: 可用, false: 不可用
    const nicknameError = ref('')
    const updateLoading = ref(false)
    const updateError = ref('')
    // 头像上传相关
    const avatarInput = ref(null)
    const avatarLoading = ref(false)
    const avatarError = ref('')
    let nicknameCheckTimer = null
    
    const checkNickname = async () => {
      // 如果昵称没变，不需要验证
      if (editForm.value.nickname === user.value?.nickname) {
        nicknameAvailable.value = null
        nicknameError.value = ''
        return
      }
      
      if (!editForm.value.nickname.trim()) {
        nicknameAvailable.value = null
        nicknameError.value = ''
        return
      }
      
      try {
        const response = await axios.get('/api/users/nickname/available', {
          params: {
            nickname: editForm.value.nickname,
            excludeUserId: user.value?.id
          }
        })
        nicknameAvailable.value = response.data.available
        nicknameError.value = nicknameAvailable.value ? '' : '该昵称已被使用'
      } catch (error) {
        console.error('检查昵称失败:', error)
        nicknameAvailable.value = null
        nicknameError.value = '检查昵称失败，请稍后重试'
      }
    }
    
    // 防抖处理，避免频繁请求
    const debouncedCheckNickname = () => {
      clearTimeout(nicknameCheckTimer)
      nicknameCheckTimer = setTimeout(checkNickname, 500)
    }

    const fetchUserInfo = async () => {
      loading.value = true
      error.value = ''
      try {
        const response = await axios.get('/api/users/me')
        user.value = response.data || response
        // 更新localStorage中的用户信息
        localStorage.setItem('user', JSON.stringify(user.value))
      } catch (err) {
        error.value = '获取用户信息失败'
        console.error('获取用户信息失败:', err)
      } finally {
        loading.value = false
      }
    }
    
    // 上传单个文件到后端，参考CreateArticle.vue中的实现
    const uploadFile = async (file) => {
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        console.log('开始上传文件:', file.name)
        // 注意：由于axios响应拦截器已经直接返回response.data，所以这里接收到的就是后端返回的数据
        const responseData = await axios.post('/api/files/upload', formData)
        
        console.log('上传响应数据:', responseData)
        console.log('响应数据类型:', typeof responseData)
        console.log('响应数据结构:', JSON.stringify(responseData))
        
        // 检查响应结构，确保正确获取URL
        if (responseData) {
          // 直接返回FileUploadResponse对象中的url字段
          if (responseData.url) {
            console.log('成功获取URL:', responseData.url)
            return responseData.url
          } 
          // 特殊情况处理
          else if (typeof responseData === 'string') {
            console.log('直接返回字符串URL:', responseData)
            return responseData
          } 
          // 打印响应数据以便调试
          console.error('响应数据结构异常，缺少url字段:', responseData)
          throw new Error('上传响应格式不正确: 缺少url字段')
        }
        console.error('上传响应为空')
        throw new Error('上传响应为空')
      } catch (err) {
        console.error('文件上传失败:', err)
        // 详细错误信息
        let errorMsg = '文件上传失败'
        if (err.response) {
          errorMsg = `文件上传失败: ${err.response.status} ${err.response.statusText}`
          if (err.response.data) {
            errorMsg += ` - ${typeof err.response.data === 'string' ? err.response.data : JSON.stringify(err.response.data)}`
          }
        } else if (err.request) {
          errorMsg = '文件上传失败: 无法连接到服务器'
        } else {
          errorMsg = `文件上传失败: ${err.message}`
        }
        throw new Error(errorMsg)
      }
    }
    
    // 更新用户信息
    const handleUpdate = async () => {
      updateLoading.value = true
      updateError.value = ''
      
      try {
        // 准备提交的数据
        const updateData = {
          nickname: editForm.value.nickname,
          introduction: editForm.value.introduction,
          avatar: editForm.value.avatar
        }
        
        // 如果有新头像文件，先上传头像
        if (editForm.value._avatarFile) {
          avatarLoading.value = true
          
          try {
            // 使用优化的uploadFile函数上传头像
            const uploadedAvatarUrl = await uploadFile(editForm.value._avatarFile)
            
            // 确保成功获取到URL并更新
            if (uploadedAvatarUrl) {
              // 更新编辑表单中的头像URL
              editForm.value.avatar = uploadedAvatarUrl
              // 更新提交数据中的头像URL
              updateData.avatar = uploadedAvatarUrl
              
              console.log('头像上传成功，URL已更新:', uploadedAvatarUrl)
              
              // 如果有旧头像，删除旧头像
              if (editForm.value._oldAvatarUrl && editForm.value._oldAvatarUrl !== uploadedAvatarUrl) {
                try {
                  // 从URL中提取文件名
                  const oldAvatarFilename = editForm.value._oldAvatarUrl.split('/').pop()
                  console.log('尝试删除旧头像:', oldAvatarFilename)
                  await axios.delete(`/api/files/delete/${oldAvatarFilename}`)
                  console.log('旧头像删除成功')
                } catch (deleteErr) {
                  console.error('删除旧头像失败:', deleteErr)
                  // 删除失败不影响主流程
                }
              }
            } else {
              throw new Error('未能获取到有效的头像URL')
            }
          } catch (uploadErr) {
            console.error('头像上传失败:', uploadErr)
            updateError.value = `头像上传失败: ${uploadErr.message}`
            throw uploadErr
          } finally {
            avatarLoading.value = false
          }
        }
        
        // 打印准备提交的数据
        console.log('准备更新用户信息:', updateData)
        
        // 更新用户信息
        const response = await axios.put('/api/users/me', updateData)
        
        // 检查响应数据
        console.log('用户信息更新响应:', response.data)
        
        // 更新用户信息并保存到localStorage
        user.value = response.data
        localStorage.setItem('user', JSON.stringify(user.value))
        console.log('用户信息已更新并保存到localStorage')
        
        showEditForm.value = false
      } catch (err) {
        updateError.value = '更新用户信息失败'
        console.error(err)
        // 上传失败时，恢复旧头像
        if (editForm.value._oldAvatarUrl) {
          user.value.avatar = editForm.value._oldAvatarUrl
        }
      } finally {
        updateLoading.value = false
        avatarLoading.value = false
      }
    }
    
    // 取消编辑
    const cancelEdit = () => {
      showEditForm.value = false
      updateError.value = ''
      avatarError.value = ''
      // 清空临时文件和预览
      editForm.value._avatarFile = null
      editForm.value._tempAvatarUrl = null
    }
    
    // 触发头像上传
    const triggerAvatarUpload = () => {
      avatarInput.value?.click()
    }
    
    // 处理头像上传（仅验证，不上传）
    const handleAvatarUpload = (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      // 验证文件类型
      const validTypes = ['image/jpeg', 'image/png']
      if (!validTypes.includes(file.type)) {
        avatarError.value = '请上传JPG或PNG格式的图片'
        // 清空input
        event.target.value = ''
        return
      }
      
      // 验证文件大小（2MB）
      if (file.size > 2 * 1024 * 1024) {
        avatarError.value = '图片大小不能超过2MB'
        // 清空input
        event.target.value = ''
        return
      }
      
      // 保存文件对象，待保存时上传
      editForm.value._avatarFile = file
      
      // 显示本地预览
      const reader = new FileReader()
      reader.onload = (e) => {
        // 使用临时URL进行预览，不更新user.value
        editForm.value._tempAvatarUrl = e.target.result
      }
      reader.readAsDataURL(file)
      
      avatarError.value = ''
      // 清空input，允许重复上传同一文件
      event.target.value = ''
    }

    const fetchUserArticles = async (page = 1, order = sortOrder.value) => {
      try {
        // 添加用户ID参数，确保只获取当前用户的文章，增加排序参数
        const response = await axios.get(`/api/articles?page=${page}&size=${pageSize.value}&userId=${user.value?.id}&sortField=updatedAt&sortDirection=${order}`)
        articles.value = response.data?.content || response.data || []
        // 计算总页数
        const totalElements = response.data?.totalElements || response.data?.length || 0
        totalPages.value = Math.ceil(totalElements / pageSize.value)
        currentPage.value = page
      } catch (err) {
        console.error('获取用户文章失败:', err)
      }
    }
    
    const handleSortChange = () => {
      // 切换排序时重置到第一页
      currentPage.value = 1
      fetchUserArticles(1, sortOrder.value)
    }

    const deleteArticle = async (articleId) => {
      if (!confirm('确定要删除这篇文章吗？')) {
        return
      }

      try {
        await axios.delete(`/api/articles/${articleId}`)
        alert('文章删除成功')
        // 重新获取文章列表
        fetchUserArticles(currentPage.value)
      } catch (err) {
        alert('删除失败，请稍后重试')
        console.error('删除文章失败:', err)
      }
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        fetchUserArticles(currentPage.value - 1)
      }
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        fetchUserArticles(currentPage.value + 1)
      }
    }

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
    
    // 获取头像URL，如果有临时预览URL则使用，否则使用用户头像，最后使用默认头像
    const getAvatarUrl = () => {
      // 在编辑模式下，优先使用临时预览URL
      if (showEditForm.value && editForm.value._tempAvatarUrl) {
        return editForm.value._tempAvatarUrl
      }
      return user.value?.avatar || '/src/assets/images/avatar.jpg'
    }

    onMounted(async () => {
      // 从localStorage获取用户信息，如果没有则从API获取
      const userStr = localStorage.getItem('user')
      if (userStr) {
        user.value = JSON.parse(userStr)
        // 获取到用户信息后再获取文章列表
        fetchUserArticles()
      } else {
        // 等待用户信息加载完成后再获取文章列表
        await fetchUserInfo()
        if (user.value) {
          fetchUserArticles()
        }
      }
    })
    
    // 监听showEditForm变化，当显示编辑表单时初始化表单数据
    watch(showEditForm, (newVal) => {
      if (newVal && user.value) {
        editForm.value = {
          nickname: user.value.nickname || '',
          introduction: user.value.introduction || '',
          avatar: user.value.avatar || '',
          _avatarFile: null, // 临时存储头像文件
          _tempAvatarUrl: null, // 临时预览URL
          _oldAvatarUrl: user.value.avatar || '' // 保存旧头像URL，用于删除
        }
        // 重置昵称验证状态
        nicknameAvailable.value = null
        nicknameError.value = ''
      }
    })
    
    // 监听昵称输入变化
    watch(() => editForm.value.nickname, () => {
      debouncedCheckNickname()
    })

    return {
        user,
        articles,
        loading,
        error,
        currentPage,
        totalPages,
        sortOrder,
        showEditForm,
        editForm,
        nicknameAvailable,
        nicknameError,
        updateLoading,
        updateError,
        avatarInput,
        avatarLoading,
        avatarError,
        deleteArticle,
        prevPage,
        nextPage,
        handleSortChange,
        formatDate,
        getAvatarUrl,
        handleUpdate,
        cancelEdit,
        triggerAvatarUpload,
        handleAvatarUpload
      }
  }
}
</script>

<style scoped>
/* 整体容器和背景样式 - 玄幻科技风 */
.profile {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--main-bg) 0%, var(--bg-secondary) 50%, var(--main-bg) 100%);
  background-attachment: fixed;
  position: relative;
}

/* 动态网格背景 */
.profile::before {
  content: "";
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(var(--grid-color) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: -1;
  animation: gridMove 60s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 1000px 1000px;
  }
}

/* 标题样式 */
.profile h1 {
  margin-bottom: 40px;
  font-size: 36px;
  color: var(--text-primary);
  text-align: center;
  text-shadow: 0 0 10px var(--highlight), 0 0 20px var(--highlight-glow);
  letter-spacing: 2px;
  font-weight: 600;
}

/* 加载和错误状态 */
.loading,
.error {
  text-align: center;
  padding: 60px 0;
  font-size: 18px;
  color: var(--text-secondary);
}

.error {
  color: #ff6b6b;
  text-shadow: 0 0 10px rgba(255, 107, 107, 0.3);
}

/* 主内容容器 */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
  backdrop-filter: blur(10px);
  border-radius: 15px;
  border: 1px solid var(--border-color);
  padding: 20px;
  box-shadow: var(--shadow);
}

.profile-info {
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
  border-radius: 15px;
  box-shadow: var(--shadow);
  padding: 30px;
  text-align: center;
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

/* 科技感发光边框效果 */
.profile-info::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, var(--highlight), var(--secondary-highlight), var(--highlight), var(--secondary-highlight));
  z-index: -1;
  border-radius: 16px;
  background-size: 400%;
  animation: glowing 20s linear infinite;
  opacity: 0.7;
}

@keyframes glowing {
  0% { background-position: 0 0; }
  50% { background-position: 400% 0; }
  100% { background-position: 0 0; }
}

/* 头像容器和头像样式 */
.avatar-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
}

.avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--highlight);
  box-shadow: var(--highlight-glow);
  transition: all 0.3s ease;
  background: var(--bg-secondary);
}

.avatar:hover {
  box-shadow: 0 0 30px var(--highlight), 0 0 40px var(--highlight-glow);
  transform: scale(1.05);
}

/* 标题样式 */
.profile-info h2 {
  margin-bottom: 25px;
  font-size: 24px;
  color: var(--text-primary);
  text-shadow: 0 0 10px var(--highlight);
  position: relative;
  display: inline-block;
  width: 100%;
  text-align: center;
}

.profile-info h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

.my-articles h2 {
  margin-bottom: 20px;
  font-size: 24px;
  color: var(--text-primary);
  text-shadow: 0 0 10px var(--highlight);
  position: relative;
  display: inline-block;
}

.my-articles h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
}

/* 文章头部容器 */
.articles-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

/* 排序控制样式 */
.sort-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.sort-controls label {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.sort-controls select {
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s;
  background: var(--bg-tertiary);
  box-shadow: var(--shadow);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%23a855f7' viewBox='0 0 16 16'%3E%3Cpath d='M8 12a.5.5 0 0 0 .5-.5V5.707l2.146 2.147a.5.5 0 0 0 .708-.708l-3-3a.5.5 0 0 0-.708 0l-3 3a.5.5 0 1 0 .708.708L7.5 5.707V11.5a.5.5 0 0 0 .5.5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px;
}

.sort-controls select:hover {
  border-color: var(--highlight);
  box-shadow: var(--highlight-glow);
  background-color: var(--bg-secondary);
}

.sort-controls select:focus {
  outline: none;
  border-color: var(--highlight);
  box-shadow: 0 0 20px var(--highlight);
}

/* 下拉菜单选项样式 */
.sort-controls select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
  padding: 8px 12px;
}

.sort-controls select option:hover {
  background: var(--highlight);
}

.sort-controls select option:checked {
  background: var(--highlight);
  color: var(--text-primary);
}

/* 用户信息项样式 */
.info-item {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  font-weight: 600;
  color: var(--highlight);
  width: 120px;
  font-size: 15px;
  text-align: right;
}

.info-item span {
  color: var(--text-primary);
  font-size: 16px;
  flex: 1;
  text-align: left;
  word-break: break-word;
  line-height: 1.6;
}

.edit-btn {
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight));
  color: var(--text-primary);
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 25px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
  box-shadow: var(--highlight-glow);
}

.edit-btn::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: var(--edit-btn-border-gradient);
  z-index: -1;
  border-radius: 9px;
  background-size: 400%;
  opacity: 0;
  transition: opacity 0.3s;
}

.edit-btn:hover::before {
  opacity: 1;
  animation: glowing 8s linear infinite;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--edit-btn-hover-shadow);
}

.edit-btn:active {
  transform: translateY(0);
}

/* 模态框遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--modal-overlay-bg);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

/* 模态框卡片 */
.edit-form.modal {
  background: var(--modal-bg);
  border-radius: 15px;
  box-shadow: var(--modal-shadow);
  padding: 30px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  border: var(--modal-border);
  backdrop-filter: blur(10px);
  z-index: 1001; /* 确保模态框在遮罩层之上 */
}

/* 模态框标题样式 */
.edit-form.modal h3 {
  color: var(--text-primary);
  text-shadow: 0 0 10px var(--highlight);
  margin-bottom: 25px;
  font-size: 22px;
  text-align: center;
  position: relative;
}

.edit-form.modal h3::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 3px;
  background: var(--modal-title-underline);
}
  
  /* 响应式样式 */
  @media (max-width: 768px) {
    .edit-form.modal {
      width: 95%;
      padding: 20px;
      margin: 0 10px;
    }
    
    .avatar-upload {
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    
    .preview-avatar {
      margin-bottom: 10px;
    }
    
    .form-actions {
      flex-direction: column;
    }
    
    .form-actions button {
      width: 100%;
    }
  }
  
  @media (max-width: 480px) {
    .edit-form.modal {
      padding: 16px;
      max-height: 95vh;
    }
    
    h3 {
      font-size: 20px;
    }
  }

  .my-articles {
  background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
  border-radius: 15px;
  box-shadow: var(--shadow);
  padding: 30px;
  border: 1px solid var(--border-color);
  position: relative;
}

/* 无文章提示样式 */
.no-articles {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
  font-size: 18px;
  text-shadow: var(--no-articles-text-shadow);
}

/* 文章列表样式 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 文章卡片样式 */
.article-card {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-secondary) 100%);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--highlight), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.4), 0 0 20px var(--highlight-glow);
  border-color: var(--border-color);
}

.article-card:hover::before {
  transform: translateX(100%);
}

/* 文章链接样式 */
.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
  transition: transform 0.3s ease;
}

.article-link:hover .article-title {
  text-shadow: 0 0 15px var(--highlight-glow);
}

/* 文章标题样式 */
.article-title {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--text-primary);
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
}

.article-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--highlight);
  transition: width 0.3s ease;
}

.article-card:hover .article-title::after {
  width: 100%;
}

/* 文章元信息样式 */
.article-meta {
  color: var(--text-secondary);
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 1;
  display: flex;
  gap: 10px;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.article-card:hover .article-actions {
  opacity: 1;
}

/* 文章卡片中的编辑按钮样式 */
.article-actions .edit-btn {
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight));
  color: var(--text-primary);
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  text-decoration: none;
  transition: all 0.3s ease;
  display: inline-block;
  line-height: 1.5;
  text-align: center;
  box-sizing: border-box;
  font-family: inherit;
  margin: 0;
  box-shadow: var(--highlight-glow);
}

.article-actions .edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 15px var(--highlight-glow);
  color: var(--text-primary);
}

/* 删除按钮样式 */
.delete-btn {
  background: linear-gradient(135deg, var(--danger-color), var(--danger-color-light));
  color: var(--text-primary);
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 0 10px var(--danger-glow);
}

.delete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 15px var(--danger-glow);
}

.form-item {
  margin-bottom: 25px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

/* 头像上传样式 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 15px;
}

.preview-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--highlight);
  box-shadow: var(--highlight-glow);
  background: var(--bg-tertiary);
}

.upload-btn {
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight));
  color: var(--text-primary);
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: var(--highlight-glow);
}

.upload-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.upload-btn:hover::before {
  left: 100%;
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 20px var(--highlight);
}

.upload-loading {
  margin-top: 10px;
  color: var(--highlight);
  font-size: 14px;
  font-weight: 500;
}

.upload-error {
  margin-top: 10px;
  color: var(--danger-color);
  font-size: 14px;
  font-weight: 500;
}

/* 表单输入框样式 */
.modal .form-item input,
.modal .form-item textarea {
  width: 100%;
  padding: 12px 16px;
  border: var(--input-border);
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: var(--input-bg);
  color: var(--text-primary);
  box-shadow: var(--input-shadow);
}

.modal .form-item input::placeholder,
.modal .form-item textarea::placeholder {
  color: var(--text-secondary);
}

.modal .form-item .input-error {
  border-color: var(--danger-color);
  box-shadow: var(--danger-glow);
}

.modal .form-item .input-success {
  border-color: var(--success-color);
  box-shadow: var(--success-glow);
}

.modal .form-item .input-with-feedback {
  position: relative;
}

.modal .form-item .validation-success,
.modal .form-item .validation-error {
  font-size: 13px;
  margin-top: 5px;
  line-height: 1.4;
}

.modal .form-item .validation-success {
  color: var(--success-color);
}

.modal .form-item .validation-error {
  color: var(--danger-color);
}

.form-item input:focus,
.form-item textarea:focus {
  outline: none;
  border-color: var(--highlight);
  box-shadow: var(--input-focus-shadow);
  background: var(--input-focus-bg);
}

/* 表单操作按钮样式 */
.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
  justify-content: center;
}

/* 保存按钮样式 */
.save-btn {
  background: linear-gradient(135deg, var(--success-color), var(--success-color-light));
  color: var(--text-primary);
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
  box-shadow: var(--success-glow);
}

.save-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.save-btn:hover::before {
  left: 100%;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 0 20px var(--success-color);
}

.save-btn:disabled {
  background: var(--disabled-btn-bg);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

/* 取消按钮样式 */
.cancel-btn {
  background: linear-gradient(135deg, var(--secondary-color), var(--secondary-color-light));
  color: var(--text-primary);
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
  box-shadow: var(--secondary-glow);
}

.cancel-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.cancel-btn:hover::before {
  left: 100%;
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 20px var(--secondary-color);
}

/* 更新错误提示样式 */
.update-error {
  color: var(--danger-color);
  margin-top: 15px;
  font-size: 14px;
  text-align: center;
  font-weight: 500;
  text-shadow: 0 0 10px rgba(var(--danger-color-rgb), 0.3);
}

/* 分页控件样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 30px;
  padding: 15px;
  background: var(--pagination-bg);
  border-radius: 10px;
  border: var(--pagination-border);
}

.pagination button {
  padding: 8px 16px;
  background: linear-gradient(135deg, var(--highlight), var(--secondary-highlight));
  color: var(--text-primary);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: var(--highlight-glow);
  position: relative;
  overflow: hidden;
}

.pagination button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.pagination button:hover:not(:disabled)::before {
  left: 100%;
}

.pagination button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 0 15px var(--highlight);
}

.pagination button:disabled {
  background: var(--disabled-color);
  color: var(--text-secondary);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.pagination span {
    font-size: 14px;
    color: var(--text-primary);
    font-weight: 500;
  }

  /* 响应式设计 - 平板设备 */
  @media (max-width: 992px) {
    .profile {
      max-width: 100%;
      padding: 15px;
    }
    
    .profile-content {
      padding: 15px;
    }
    
    .profile h1 {
      font-size: 30px;
      margin-bottom: 30px;
    }
    
    .profile-info,
    .my-articles {
      padding: 25px;
    }
    
    .avatar {
      width: 120px;
      height: 120px;
    }
    
    .profile-info h2,
    .my-articles h2 {
      font-size: 22px;
    }
    
    .article-title {
      font-size: 18px;
    }
    
    .pagination {
      padding: 12px;
      gap: 8px;
    }
    
    .pagination button {
      padding: 6px 12px;
      font-size: 13px;
    }
  }

  /* 响应式设计 - 中小屏幕移动设备 */
  @media (max-width: 768px) {
    .profile {
      padding: 10px;
    }
    
    .profile-content {
      gap: 25px;
      padding: 10px;
    }
    
    .profile h1 {
      font-size: 28px;
      margin-bottom: 25px;
      letter-spacing: 1px;
    }
    
    .profile-info,
    .my-articles {
      padding: 20px;
      border-radius: 12px;
    }
    
    .avatar {
      width: 100px;
      height: 100px;
    }
    
    .profile-info h2,
    .my-articles h2 {
      font-size: 20px;
    }
    
    .profile-info h2::after,
    .my-articles h2::after {
      width: 60px;
      bottom: -8px;
    }
    
    .info-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    
    .info-item label {
      width: 100%;
      font-size: 14px;
    }
    
    .info-item span {
      text-align: left;
      font-size: 15px;
    }
    
    .introduction {
      padding: 12px;
      font-size: 15px;
      line-height: 1.7;
    }
    
    .edit-btn {
      padding: 10px 20px;
      font-size: 13px;
    }
    
    .sort-controls {
      flex-direction: column;
      gap: 10px;
    }
    
    .sort-controls select {
      width: 100%;
      max-width: 300px;
    }
    
    .article-card {
      padding: 16px;
      border-radius: 8px;
    }
    
    .article-title {
      font-size: 16px;
      margin-bottom: 10px;
    }
    
    .article-meta {
      flex-direction: column;
      gap: 5px;
      align-items: flex-start;
      font-size: 13px;
    }
    
    .article-actions {
      position: relative;
      top: auto;
      right: auto;
      justify-content: flex-end;
      margin-top: 15px;
    }
    
    .edit-form.modal {
      width: 95%;
      padding: 25px;
      margin: 0 10px;
    }
    
    .form-item {
      margin-bottom: 20px;
    }
    
    .avatar-upload {
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 15px;
    }
    
    .preview-avatar {
      margin-bottom: 10px;
    }
    
    .form-actions {
      flex-direction: column;
      gap: 12px;
    }
    
    .form-actions button {
      width: 100%;
      padding: 12px 0;
    }
    
    .pagination {
      flex-wrap: wrap;
      padding: 10px;
    }
  }

  /* 响应式设计 - 小屏幕移动设备 */
  @media (max-width: 480px) {
    .profile {
      padding: 8px;
    }
    
    .profile-content {
      gap: 20px;
      padding: 8px;
    }
    
    .profile h1 {
      font-size: 24px;
      margin-bottom: 20px;
    }
    
    .profile-info,
    .my-articles {
      padding: 16px;
      border-radius: 10px;
    }
    
    .avatar {
      width: 90px;
      height: 90px;
    }
    
    .profile-info h2,
    .my-articles h2 {
      font-size: 18px;
    }
    
    .loading,
    .error {
      padding: 40px 0;
      font-size: 16px;
    }
    
    .no-articles {
      padding: 40px 0;
      font-size: 16px;
    }
    
    .article-list {
      gap: 15px;
    }
    
    .article-card {
      padding: 14px;
    }
    
    .article-title {
      font-size: 16px;
    }
    
    .article-actions {
      gap: 6px;
    }
    
    .article-actions .edit-btn,
    .delete-btn {
      padding: 4px 8px;
      font-size: 11px;
    }
    
    .edit-form.modal {
      padding: 20px 16px;
      max-height: 95vh;
    }
    
    .edit-form.modal h3 {
      font-size: 20px;
      margin-bottom: 20px;
    }
    
    .modal .form-item input,
    .modal .form-item textarea {
      padding: 10px 14px;
      font-size: 14px;
    }
    
    .upload-btn {
      padding: 8px 16px;
      font-size: 13px;
    }
    
    .pagination button {
      padding: 5px 10px;
      font-size: 12px;
    }
    
    .pagination span {
      font-size: 13px;
    }
    
    /* 优化小屏幕上的动画性能 */
    .profile::before {
      animation: none;
      background-size: 15px 15px;
    }
    
    .profile-info::before {
      animation: none;
    }
    
    .edit-btn::before,
    .save-btn::before,
    .cancel-btn::before,
    .upload-btn::before,
    .pagination button::before {
      animation: none;
    }
  }
  </style>

<template>
  <div class="profile">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="user" class="profile-content">
      <div class="profile-info">
        <div class="avatar-container">
          <img :src="getAvatarUrl()" alt="用户头像" class="avatar">
        </div>
        <h2>个人中心</h2>
        <div class="info-item">
          <label>昵称：</label>
          <span>{{ user.nickname }}</span>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span>{{ user.email }}</span>
        </div>
        <div class="info-item introduction-item">
          <label class="label-fixed">简介：</label>
          <div class="introduction">{{ user.introduction || '暂无简介' }}</div>
        </div>
        <div class="info-item">
          <label>注册时间：</label>
          <span>{{ formatDate(user.createdAt) }}</span>
        </div>
        <button @click="showEditForm = true" class="edit-btn">编辑资料</button>
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
      
      <div class="my-articles">
        <div class="article-header">
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
/* 全局样式优化 */
.profile {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.profile h1 {
  margin-bottom: 30px;
  font-size: 32px;
  color: #333;
  position: relative;
  display: inline-block;
}

.profile h1::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -8px;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #1890ff, #40a9ff);
  border-radius: 3px;
  animation: growLine 0.6s ease-out;
}

@keyframes growLine {
  from {
    width: 0;
  }
  to {
    width: 100%;
  }
}

.loading,
.error {
  text-align: center;
  padding: 40px 0;
  font-size: 16px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.error {
  color: #ff4d4f;
  animation: fadeInError 0.3s ease-out;
}

@keyframes fadeInError {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

/* 用户信息卡片优化 */
.profile-info {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  padding: 32px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.profile-info::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #1890ff, #52c41a, #faad14);
  animation: gradientFlow 8s ease infinite;
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.profile-info:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 头像样式优化 */
.avatar-container {
  margin-bottom: 20px;
  position: relative;
  display: inline-block;
}

.avatar {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f5f5f5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.avatar-container:hover .avatar {
  transform: scale(1.05) rotate(2deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.avatar-container::after {
  content: '';
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border: 2px solid transparent;
  border-radius: 50%;
  background: linear-gradient(45deg, #1890ff, #52c41a, #faad14, #f5222d);
  background-size: 400%;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: gradientRotate 8s linear infinite;
}

@keyframes gradientRotate {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.avatar-container:hover::after {
  opacity: 1;
}

.profile-info h2,
.my-articles h2 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
  font-weight: 600;
  transition: color 0.3s ease;
}

.profile-info h2:hover {
  color: #1890ff;
}

/* 排序控件优化 */
.sort-controls {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-controls label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.sort-controls select {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fafafa;
  outline: none;
}

.sort-controls select:hover {
  border-color: #1890ff;
  background-color: #fff;
  transform: translateY(-1px);
}

.sort-controls select:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.15);
  transform: translateY(-1px);
}

/* 用户信息项优化 */
.info-item {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  padding: 8px 0;
  transition: transform 0.2s ease;
}

.info-item:hover {
  transform: translateX(5px);
}

/* 简介项特殊样式 */
.introduction-item {
  align-items: flex-start;
}

.label-fixed {
  align-self: flex-start;
  flex-shrink: 0;
  margin-top: 4px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  font-weight: 600;
  color: #595959;
  width: 120px;
  font-size: 14px;
  position: relative;
}

.info-item label::after {
  content: '';
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background-color: #1890ff;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.info-item:hover label::after {
  opacity: 1;
}

.info-item span {
  color: #333;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.info-item:hover span {
  color: #1890ff;
}

.introduction {
  color: #333;
  font-size: 16px;
  line-height: 1.7;
  text-align: left;
  flex: 1;
  margin-left: 0;
  margin-top: 0;
  padding: 12px 16px;
  background-color: #fafafa;
  border-radius: 8px;
  border-left: 3px solid #1890ff;
  transition: all 0.3s ease;
}

.introduction:hover {
  background-color: #f0f7ff;
  transform: translateX(3px);
}

/* 编辑按钮优化 */
.edit-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.edit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.edit-btn:hover {
  background-color: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.edit-btn:hover::before {
  left: 100%;
}

.edit-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
}

/* 模态框样式优化 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.edit-form.modal {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  padding: 24px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: slideInUp 0.4s ease-out;
  transform-origin: center;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
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
  
  .profile {
    padding: 10px;
  }
  
  .profile-info {
    padding: 20px;
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
  
  .profile h1 {
    font-size: 28px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .info-item label {
    width: auto;
    margin-bottom: 5px;
  }
}

/* 我的文章部分优化 */
.my-articles {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: box-shadow 0.3s ease;
}

.my-articles:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.no-articles {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  margin: 20px 0;
  transition: all 0.3s ease;
}

.no-articles:hover {
  background-color: #f0f0f0;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 文章卡片优化 */
.article-card {
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 20px;
  transition: all 0.3s ease;
  position: relative;
  background-color: #fafafa;
  overflow: hidden;
}

.article-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #1890ff, #52c41a);
  transform: scaleY(0);
  transform-origin: top;
  transition: transform 0.3s ease;
}

.article-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  background-color: #fff;
}

.article-card:hover::before {
  transform: scaleY(1);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
  transition: transform 0.2s ease;
}

.article-link:hover {
  transform: translateX(3px);
}

.article-title {
  font-size: 20px;
  margin-bottom: 12px;
  color: #333;
  font-weight: 600;
  transition: color 0.3s ease, transform 0.3s ease;
  position: relative;
  padding-left: 10px;
}

.article-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background-color: #1890ff;
  border-radius: 3px;
  transition: height 0.3s ease;
}

.article-card:hover .article-title {
  color: #1890ff;
  transform: translateX(5px);
}

.article-card:hover .article-title::before {
  height: 70%;
}

.article-meta {
  color: #666;
  font-size: 14px;
  transition: color 0.3s ease;
}

.article-card:hover .article-meta {
  color: #1890ff;
}

/* 文章操作按钮优化 */
.article-actions {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 1;
  display: flex;
  gap: 10px;
  opacity: 1;
  transform: translateX(0);
  transition: all 0.3s ease;
}

.edit-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.3s ease;
  display: inline-block;
  line-height: 1.5;
  text-align: center;
  box-sizing: border-box;
  font-family: inherit;
  margin: 0;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
}

.edit-btn:hover {
  background-color: #40a9ff;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(24, 144, 255, 0.4);
}

.delete-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
}

.delete-btn:hover {
  background-color: #ff7875;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(255, 77, 79, 0.4);
}

.delete-btn:active,
.edit-btn:active {
  transform: translateY(0);
}

/* 表单样式优化 */
.form-item {
  margin-bottom: 24px;
  transition: transform 0.2s ease;
}

.form-item:hover {
  transform: translateX(2px);
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 10px;
}

.preview-avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
  transition: transform 0.3s ease, border-color 0.3s ease;
}

.preview-avatar:hover {
  transform: scale(1.05);
  border-color: #1890ff;
}

.upload-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
}

.upload-btn:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.4);
}

.upload-loading {
  margin-top: 8px;
  color: #1890ff;
  font-size: 14px;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.upload-error {
  margin-top: 8px;
  color: #ff4d4f;
  font-size: 14px;
  animation: shake 0.3s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.modal .form-item input,
.modal .form-item textarea {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #f0f0f0;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: #fafafa;
  outline: none;
}

.modal .form-item input:focus,
.modal .form-item textarea:focus {
  border-color: #1890ff;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
  transform: translateY(-1px);
}

.modal .form-item .input-error {
  border-color: #ff4d4f;
  background-color: #fff2f0;
}

.modal .form-item .input-success {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.modal .form-item .input-with-feedback {
  position: relative;
}

.modal .form-item .validation-success,
.modal .form-item .validation-error {
  font-size: 12px;
  margin-top: 6px;
  line-height: 1.4;
  transition: opacity 0.3s ease;
}

.modal .form-item .validation-success {
  color: #52c41a;
}

.modal .form-item .validation-error {
  color: #ff4d4f;
}

/* 表单按钮优化 */
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.save-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(82, 196, 26, 0.3);
}

.save-btn:hover:not(:disabled) {
  background-color: #73d13d;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
}

.save-btn:disabled {
  background-color: #d9d9d9;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.cancel-btn:hover {
  background-color: #868e96;
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(108, 117, 125, 0.3);
}

.update-error {
  color: #ff4d4f;
  margin-top: 12px;
  font-size: 14px;
  padding: 8px 12px;
  background-color: #fff2f0;
  border-radius: 4px;
  border-left: 3px solid #ff4d4f;
  animation: fadeInError 0.3s ease-out;
}

/* 分页样式优化 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.pagination button {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
}

.pagination button:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.4);
}

.pagination button:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.pagination span {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ddd;
}

.upload-btn {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.upload-btn:hover {
  background-color: #0056b3;
}

.upload-loading {
  margin-top: 8px;
  color: #007bff;
  font-size: 14px;
}

.upload-error {
  margin-top: 8px;
  color: #dc3545;
  font-size: 14px;
}

.modal .form-item input,
.modal .form-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.modal .form-item .input-error {
  border-color: #ff4d4f;
}

.modal .form-item .input-success {
  border-color: #52c41a;
}

.modal .form-item .input-with-feedback {
  position: relative;
}

.modal .form-item .validation-success,
.modal .form-item .validation-error {
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.4;
}

.modal .form-item .validation-success {
  color: #52c41a;
}

.modal .form-item .validation-error {
  color: #ff4d4f;
}

.form-item input:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #007bff;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.save-btn {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.save-btn:hover {
  background-color: #218838;
}

.save-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.cancel-btn:hover {
  background-color: #5a6268;
}

.update-error {
  color: #dc3545;
  margin-top: 10px;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.pagination button {
  padding: 6px 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.pagination button:hover:not(:disabled) {
  background-color: #40a9ff;
}

.pagination button:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.pagination span {
  font-size: 14px;
  color: #666;
}
</style>
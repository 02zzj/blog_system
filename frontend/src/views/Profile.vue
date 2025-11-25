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
        <h2>我的文章</h2>
        <div class="sort-controls">
          <label>排序方式：</label>
          <select v-model="sortOrder" @change="handleSortChange">
            <option value="desc">最新优先</option>
            <option value="asc">最早优先</option>
          </select>
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
.profile {
  max-width: 800px;
  margin: 0 auto;
}

.profile h1 {
  margin-bottom: 30px;
  font-size: 32px;
  color: #333;
}

.loading,
.error {
  text-align: center;
  padding: 40px 0;
  font-size: 16px;
}

.error {
  color: #ff4d4f;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.profile-info {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 24px;
  text-align: center;
}

.avatar-container {
  margin-bottom: 20px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-info h2,
.my-articles h2 {
  margin-bottom: 15px;
  font-size: 20px;
  color: #333;
}

.sort-controls {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-controls label {
  font-size: 14px;
  color: #666;
}

.sort-controls select {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
}

.sort-controls select:hover {
  border-color: #40a9ff;
}

.sort-controls select:focus {
  outline: none;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.info-item {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

/* 简介项特殊样式 */
.introduction-item {
  align-items: flex-start;
}

.label-fixed {
  align-self: flex-start;
  flex-shrink: 0;
  margin-top: 0;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  font-weight: 500;
  color: #666;
  width: 100px;
}

.info-item span {
  color: #333;
  font-size: 16px;
}

.introduction {
  color: #333;
  font-size: 16px;
  line-height: 1.6;
  text-align: left;
  flex: 1;
  margin-left: 0;
  margin-top: 0;
  padding: 0;
  background-color: transparent;
  border-radius: 0;
  min-height: auto;
}

.edit-btn {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 20px;
  transition: background-color 0.3s;
}

.edit-btn:hover {
  background-color: #0056b3;
}

/* 模态框遮罩层 */
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
  }
  
  /* 模态框卡片 */
  .edit-form.modal {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
    padding: 24px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
    position: relative;
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
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.no-articles {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 16px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 16px;
  transition: all 0.3s;
  position: relative;
}

.article-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.article-title {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.article-meta {
  color: #666;
  font-size: 14px;
}

.article-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 1;
  display: flex;
  gap: 8px;
}

.edit-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  text-decoration: none;
  transition: background-color 0.3s;
  display: inline-block;
  line-height: 1.5;
  text-align: center;
  box-sizing: border-box;
  font-family: inherit;
  margin: 0;
}

.edit-btn:hover {
  background-color: #40a9ff;
  color: white;
}

.delete-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.delete-btn:hover {
  background-color: #ff7875;
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
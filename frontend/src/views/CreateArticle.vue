<template>
  <div class="create-article">
    <h1>{{ isEditing ? '编辑文章' : '发布文章' }}</h1>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="title">标题</label>
        <input
          type="text"
          id="title"
          v-model="form.title"
          placeholder="请输入文章标题"
          required
        />
      </div>
      
      <div class="form-group">
        <label>封面图（可选）</label>
        <div class="cover-image-upload">
          <div v-if="coverImagePreview" class="preview-container">
            <img :src="coverImagePreview" alt="封面图预览" class="cover-preview" />
            <button type="button" class="remove-cover-btn" @click="removeCoverImage">
              移除
            </button>
          </div>
          <div v-else class="upload-placeholder">
            <input
              type="file"
              id="coverImage"
              ref="coverInput"
              accept="image/*"
              @change="handleCoverImageUpload"
              style="display: none"
            />
            <button type="button" class="upload-btn" @click="triggerCoverUpload">
              点击上传封面图
            </button>
            <p class="upload-hint">支持JPG、PNG格式，大小不超过2MB</p>
          </div>
          <div v-if="coverImageUploading" class="uploading-indicator">
            上传中...
          </div>
        </div>
      </div>
      
      <div class="form-group">
        <label for="content">内容</label>
        <div class="editor-container">
          <div ref="editor" class="quill-editor"></div>
        </div>
      </div>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <div class="form-actions">
        <button type="button" class="cancel-btn" @click="cancel">取消</button>
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? (isEditing ? '更新中...' : '发布中...') : (isEditing ? '更新文章' : '发布文章') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '../axios'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'

const editorOptions = {
  theme: 'snow',
  placeholder: '请输入文章内容...',
  modules: {
    toolbar: {
      container: [
        ['bold', 'italic', 'underline', 'strike'],        // 文本格式
        [{ 'color': [] }, { 'background': [] }],          // 颜色选择
        [{ 'font': [] }],                                 // 字体
        [{ 'align': [] }],                                // 对齐方式
        ['blockquote', 'code-block'],                     // 引用和代码块
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],     // 列表
        [{ 'script': 'sub'}, { 'script': 'super' }],      // 上标下标
        [{ 'indent': '-1'}, { 'indent': '+1' }],          // 缩进
        [{ 'header': [1, 2, 3, 4, 5, 6, false] }],        // 标题
        ['link', 'image'],                                // 链接和图片
        ['clean']                                         // 清除格式
      ]
    }
  }
}

export default {
  name: 'CreateArticle',
  props: {
    articleId: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const router = useRouter()
    const route = useRoute()
    const form = reactive({
      title: '',
      content: '',
      coverImage: '',
      // 存储临时图片数据
      tempImages: []
    })
    
    const editor = ref(null)
    let quillInstance = null
    const loading = ref(false)
    const error = ref('')
    const coverImagePreview = ref('')
    const coverImageUploading = ref(false)
    const coverInput = ref(null)
    const isEditing = ref(false)
    const originalCoverImage = ref('')
    
    // 获取文章详情
    const fetchArticleDetail = async (id) => {
      if (!id) return
      
      try {
        loading.value = true
        const response = await axios.get(`/api/articles/${id}`)
        const article = response.data || response
        
        // 填充表单数据
        form.title = article.title || ''
        form.content = article.content || ''
        originalCoverImage.value = article.coverImage || ''
        
        // 设置封面图预览
        if (article.coverImage) {
          coverImagePreview.value = article.coverImage
        }
        
        // 如果有Quill实例，设置内容
        if (quillInstance) {
          quillInstance.root.innerHTML = article.content || ''
        }
        
        isEditing.value = true
      } catch (err) {
        console.error('获取文章详情失败:', err)
        error.value = '获取文章详情失败，请稍后重试'
        alert(error.value)
        router.push('/profile')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      if (editor.value) {
        quillInstance = new Quill(editor.value, editorOptions)
        
        // 监听内容变化
        quillInstance.on('text-change', () => {
          form.content = quillInstance.root.innerHTML
        })
        
        // 检查是否为编辑模式
        const id = props.articleId || route.params.id
        if (id) {
          fetchArticleDetail(id)
        }
        
        // 自定义图片上传处理 - 使用本地预览，延迟上传
        quillInstance.getModule('toolbar').addHandler('image', function() {
          const input = document.createElement('input');
          input.setAttribute('type', 'file');
          input.setAttribute('accept', 'image/*');
          input.click();
          
          input.onchange = function() {
            const file = input.files[0];
            if (file) {
              // 验证文件类型
              const validTypes = ['image/jpeg', 'image/png'];
              if (!validTypes.includes(file.type)) {
                alert('请上传JPG或PNG格式的图片');
                return;
              }
              
              // 验证文件大小（2MB）
              if (file.size > 2 * 1024 * 1024) {
                alert('图片大小不能超过2MB');
                return;
              }
              
              // 创建本地预览
              const reader = new FileReader();
              reader.onload = function(e) {
                const tempUrl = e.target.result;
                // 生成唯一ID标识临时图片
                const tempId = 'temp_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                
                // 存储临时图片信息
                form.tempImages.push({
                  id: tempId,
                  file: file,
                  tempUrl: tempUrl
                });
                
                // 获取当前光标位置
                const range = quillInstance.getSelection(true);
                // 插入图片（使用临时URL）
                quillInstance.insertEmbed(range.index, 'image', tempUrl);
                // 为图片元素添加临时标记，方便后续替换
                setTimeout(() => {
                  // 查找刚刚插入的图片元素
                  const imgElements = document.querySelectorAll(`[src="${tempUrl}"]`);
                  imgElements.forEach(img => {
                    img.dataset.tempId = tempId;
                  });
                }, 100);
                // 移动光标到图片后面
                quillInstance.setSelection(range.index + 1);
              };
              reader.readAsDataURL(file);
            }
          };
        })
      }
    })

    // 上传单个文件到后端
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

    const handleSubmit = async () => {
      // 验证表单
      if (!form.title.trim()) {
        alert('请输入文章标题')
        return
      }
      
      if (!form.content.trim()) {
        alert('请输入文章内容')
        return
      }
      
      loading.value = true
      error.value = ''
      
      try {
        const id = props.articleId || route.params.id
        const isEditMode = isEditing.value || id
        
        // 准备要提交的数据
        const articleData = {
          title: form.title,
          content: form.content,
          coverImage: '',
          removeCoverImage: false
        }
        
        // 处理封面图
        if (form.coverImage && form.coverImage instanceof File) {
          // 如果是新选择的图片文件，需要上传
          try {
            articleData.coverImage = await uploadFile(form.coverImage)
            console.log('封面图上传成功，URL:', articleData.coverImage)
          } catch (err) {
            alert('封面图上传失败，请重试')
            loading.value = false
            return
          }
        } else if (isEditMode) {
          // 编辑模式下，如果没有选择新图片但预览为空，说明要删除封面图
          if (!coverImagePreview.value && originalCoverImage.value) {
            articleData.removeCoverImage = true
          } else if (!form.coverImage) {
            // 如果没有选择新图片且预览不为空，保持原有封面图
            articleData.coverImage = originalCoverImage.value
          }
        }
        
        // 上传内容中的图片
        let contentHtml = form.content
        
        // 无论是否有新图片，都需要确保内容被正确设置
        if (form.tempImages.length > 0) {
          // 创建上传任务列表
          const uploadPromises = form.tempImages.map(async (tempImage) => {
            try {
              const actualUrl = await uploadFile(tempImage.file)
              console.log('内容图片上传成功，URL:', actualUrl)
              // 替换HTML中的临时URL - 避免使用正则表达式，直接进行字符串替换
              contentHtml = contentHtml.split(tempImage.tempUrl).join(actualUrl)
              return actualUrl
            } catch (err) {
              console.error('内容图片上传失败:', err)
              throw err
            }
          })
          
          // 等待所有图片上传完成
          await Promise.all(uploadPromises)
        }
        
        // 确保更新文章内容（包含原有图片和新上传的图片）
        articleData.content = contentHtml
        
        // 根据模式调用不同的API
        if (isEditMode) {
          // 更新文章
          await axios.put(`/api/articles/${id}`, articleData)
          alert('文章更新成功')
        } else {
          // 创建文章
          await axios.post('/api/articles', articleData)
          alert('文章发布成功')
        }
        
        router.push('/profile')
      } catch (err) {
        console.error(isEditing.value ? '更新文章失败:' : '发布文章失败:', err)
        error.value = isEditing.value ? '更新文章失败，请稍后重试' : '发布文章失败，请稍后重试'
        alert(error.value)
      } finally {
        loading.value = false
      }
    }

    const triggerCoverUpload = () => {
      coverInput.value?.click()
    }
    
    const handleCoverImageUpload = (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      // 验证文件类型
      const validTypes = ['image/jpeg', 'image/png']
      if (!validTypes.includes(file.type)) {
        alert('请上传JPG或PNG格式的图片')
        // 使用事件对象直接清空文件输入，避免null引用
        event.target.value = ''
        return
      }
      
      // 验证文件大小（2MB）
      if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过2MB')
        // 使用事件对象直接清空文件输入，避免null引用
        event.target.value = ''
        return
      }
      
      // 创建临时预览
      const reader = new FileReader()
      reader.onload = (e) => {
        coverImagePreview.value = e.target.result
      }
      reader.readAsDataURL(file)
      
      // 存储封面图片文件对象，延迟到发布时上传
      form.coverImage = file
      console.log('封面图已选择，将在发布时上传')
      
      // 清空input，允许重复上传同一文件
      if (coverInput.value) {
        coverInput.value.value = ''
      }
    }
    
    const removeCoverImage = () => {
      coverImagePreview.value = ''
      form.coverImage = ''
      // 安全地清空文件输入
      if (coverInput.value) {
        coverInput.value.value = ''
      }
    }
    
    const cancel = () => {
      if (form.title || form.content || form.coverImage) {
        if (confirm('确定要放弃编辑吗？')) {
          router.push('/')
        }
      } else {
        router.push('/')
      }
    }

    return {
      form,
      editor,
      loading,
      error,
      coverImagePreview,
      coverImageUploading,
      coverInput,
      isEditing,
      handleSubmit,
      triggerCoverUpload,
      handleCoverImageUpload,
      removeCoverImage,
      cancel
    }
  }
}
</script>

<style scoped>
/* 全局容器样式 */
.create-article {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
  background-color: #ffffff;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  margin-top: 30px;
  margin-bottom: 50px;
  animation: fadeInScale 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  opacity: 0;
  animation-fill-mode: forwards;
}

/* 页面标题样式 */
.create-article h1 {
  margin-bottom: 40px;
  font-size: 36px;
  color: #1a1a1a;
  text-align: center;
  font-weight: 600;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  animation: fadeInUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 32px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  opacity: 0;
  animation-fill-mode: forwards;
}

.form-group:hover label {
  color: #1890ff;
}

.form-group label {
  display: block;
  margin-bottom: 12px;
  color: #333;
  font-weight: 600;
  font-size: 16px;
  transition: color 0.3s ease;
}

/* 移除所有表单组的动画延迟 */
.form-group:nth-child(1),
.form-group:nth-child(2),
.form-group:nth-child(3) { animation-delay: 0s; }

/* 输入框样式 */
.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  font-family: inherit;
  background-color: #ffffff;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.03);
}

.form-group input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
  transform: translateY(-1px);
}

/* 封面图上传区域样式 */
.cover-image-upload {
  position: relative;
}

.upload-placeholder {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: #fafafa;
}

.upload-placeholder:hover {
  border-color: #1890ff;
  background-color: #f0f8ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.upload-btn {
  padding: 10px 24px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.upload-btn:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.upload-hint {
  margin-top: 10px;
  color: #999;
  font-size: 14px;
}

/* 预览容器样式 */
.preview-container {
  position: relative;
  max-width: 100%;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.preview-container:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.cover-preview {
  width: 100%;
  height: auto;
  max-height: 350px;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.preview-container:hover .cover-preview {
  transform: scale(1.02);
}

.remove-cover-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 8px 16px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.9;
}

.remove-cover-btn:hover {
  background-color: #ff4d4f;
  opacity: 1;
  transform: scale(1.05);
}

.uploading-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  z-index: 10;
  backdrop-filter: blur(4px);
}

/* 编辑器容器样式 */
.editor-container {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s ease;
  overflow: hidden;
  animation: fadeInScale 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.editor-container:focus-within {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

:deep(.ql-container) {
  min-height: 450px;
  font-size: 16px;
  font-family: inherit;
  border: none;
}

:deep(.ql-toolbar) {
  border: none;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  padding: 12px;
}

:deep(.ql-editor) {
  line-height: 1.7;
  min-height: 450px;
  padding: 20px;
  color: #333;
}

/* 自定义Quill工具栏按钮样式 */
:deep(.ql-stroke) {
  fill: none;
  stroke: #666;
}

:deep(.ql-fill) {
  fill: #666;
}

:deep(.ql-picker) {
  color: #666;
}

:deep(.ql-toolbar button:hover .ql-stroke) {
  stroke: #1890ff;
}

:deep(.ql-toolbar button:hover .ql-fill) {
  fill: #1890ff;
}

:deep(.ql-toolbar button.ql-active .ql-stroke) {
  stroke: #1890ff;
}

:deep(.ql-toolbar button.ql-active .ql-fill) {
  fill: #1890ff;
}

/* 错误信息样式 */
.error-message {
  color: #ff4d4f;
  margin-bottom: 24px;
  padding: 12px 16px;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: shake 0.5s ease-in-out;
  animation-fill-mode: both;
}

/* 入场动画定义 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes fadeInScale {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

/* 表单操作按钮样式 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  animation: fadeInUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.cancel-btn {
  padding: 12px 28px;
  background-color: #fff;
  color: #666;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation: fadeInUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.cancel-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.submit-btn {
  padding: 12px 28px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.submit-btn:hover:not(:disabled) {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  background-color: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

/* 添加提交按钮的加载效果 */
.submit-btn:disabled::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  margin: -10px 0 0 -10px;
  border: 2px solid transparent;
  border-top: 2px solid #ccc;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .create-article {
    margin-top: 20px;
    margin-bottom: 30px;
    padding: 20px 16px;
    box-shadow: 0 1px 10px rgba(0, 0, 0, 0.05);
  }

  .create-article h1 {
    font-size: 28px;
    margin-bottom: 30px;
    padding-bottom: 15px;
  }

  .form-group {
    margin-bottom: 24px;
  }

  .upload-placeholder {
    padding: 30px 15px;
  }

  .cover-preview {
    max-height: 250px;
  }

  :deep(.ql-container) {
    min-height: 350px;
  }

  :deep(.ql-editor) {
    min-height: 350px;
    padding: 15px;
  }

  .form-actions {
    flex-direction: column-reverse;
    gap: 12px;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
    padding: 12px 20px;
  }
}

@media (max-width: 480px) {
  .create-article {
    padding: 15px 12px;
  }

  .create-article h1 {
    font-size: 24px;
  }

  .form-group input {
    padding: 12px 14px;
    font-size: 15px;
  }

  :deep(.ql-container) {
    min-height: 300px;
  }

  :deep(.ql-editor) {
    min-height: 300px;
    font-size: 15px;
  }
}
</style>
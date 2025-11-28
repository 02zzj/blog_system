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
  max-width: 850px;
  margin: 2rem auto;
  padding: 2rem;
  background: var(--bg-primary);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(var(--highlight), 0.1);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

/* 背景装饰效果 */
.create-article::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: var(--main-bg-pattern);
  animation: gridMove 20s linear infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

/* 标题样式 */
.create-article h1 {
  margin-bottom: 2rem;
  font-size: 2.5rem;
  color: var(--text-primary);
  text-align: center;
  font-weight: 700;
  text-shadow: 0 0 10px rgba(var(--highlight), 0.3), 0 0 20px rgba(var(--highlight), 0.2);
  position: relative;
  z-index: 1;
}

.create-article h1::after {
  content: '';
  display: block;
  width: 100px;
  height: 3px;
  margin: 0.75rem auto 0;
  background: linear-gradient(90deg, transparent, var(--text-accent), transparent);
  box-shadow: 0 0 10px rgba(var(--text-accent), 0.6);
}

/* 表单组样式 */
.form-group {
  margin-bottom: 1.5rem;
  position: relative;
  z-index: 1;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 输入框样式 */
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

/* 输入框内部发光效果 */
.form-group input::before,
.form-group textarea::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(var(--highlight), 0.1), transparent);
  transition: left 0.5s ease;
  z-index: 0;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--text-accent);
  box-shadow: 0 0 15px rgba(var(--highlight), 0.3), inset 0 1px 3px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  animation: glowPulse 2s ease-in-out infinite alternate;
}

/* 输入框脉冲动画 */
@keyframes glowPulse {
  from { box-shadow: 0 0 15px rgba(var(--highlight), 0.3), inset 0 1px 3px rgba(0, 0, 0, 0.1); }
  to { box-shadow: 0 0 25px rgba(var(--highlight), 0.5), inset 0 1px 3px rgba(0, 0, 0, 0.1); }
}

/* 输入框字符提示 */
.form-group input::placeholder,
.form-group textarea::placeholder {
  color: var(--text-placeholder);
  font-style: italic;
  transition: all 0.3s ease;
}

.form-group input:focus::placeholder,
.form-group textarea:focus::placeholder {
  color: var(--text-placeholder-focus);
  transform: translateX(5px);
}

.form-group textarea {
  resize: vertical;
  min-height: 300px;
  line-height: 1.6;
}

/* 编辑器容器样式 */
.editor-container {
  margin-bottom: 1.5rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  transition: all 0.3s ease;
  background-color: var(--bg-secondary);
  position: relative;
  z-index: 1;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 编辑器顶部扫描线动画 */
.editor-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--text-accent), transparent);
  animation: scanLine 3s linear infinite;
  z-index: 2;
}

@keyframes scanLine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 编辑器聚焦效果 */
.editor-container:focus-within {
  border-color: var(--text-accent);
  box-shadow: 0 0 30px rgba(var(--highlight), 0.2), 0 0 10px rgba(var(--highlight), 0.1);
  animation: editorGlow 2s ease-in-out infinite alternate;
}

@keyframes editorGlow {
  from { box-shadow: 0 0 30px rgba(var(--highlight), 0.2), 0 0 10px rgba(var(--highlight), 0.1); }
  to { box-shadow: 0 0 40px rgba(var(--highlight), 0.4), 0 0 15px rgba(var(--highlight), 0.2); }
}

/* Quill编辑器容器样式 */
:deep(.ql-container) {
  min-height: 450px;
  font-size: 1rem;
  font-family: inherit;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border: none;
  backdrop-filter: blur(5px);
  position: relative;
}

/* 编辑器背景网格装饰 */
:deep(.ql-container)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(var(--grid-line-color) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-line-color) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
  z-index: 0;
}

/* 工具栏样式增强 */
:deep(.ql-toolbar) {
  background: var(--bg-secondary);
  border: none;
  border-bottom: 1px solid var(--border-color);
  padding: 0.75rem;
  border-radius: 8px 8px 0 0;
  position: relative;
  z-index: 1;
}

/* 工具栏发光效果 */
:deep(.ql-toolbar)::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--text-accent), transparent);
}

/* 编辑区域样式 */
:deep(.ql-editor) {
  line-height: 1.7;
  min-height: 450px;
  background-color: transparent;
  color: var(--text-primary);
  padding: 1.5rem;
  position: relative;
  z-index: 1;
}

/* 空白编辑区提示文字 */
:deep(.ql-editor.ql-blank::before) {
  color: var(--text-placeholder);
  font-style: italic;
  content: '开始创作你的文章...';
}

/* 编辑器文本样式 */
:deep(.ql-editor p) {
  margin-bottom: 1rem;
  transition: color 0.3s ease;
  color: var(--text-primary);
}

:deep(.ql-editor h1),
:deep(.ql-editor h2),
:deep(.ql-editor h3),
:deep(.ql-editor h4),
:deep(.ql-editor h5),
:deep(.ql-editor h6) {
  color: var(--text-secondary);
  margin-top: 1.5rem;
  margin-bottom: 1rem;
  font-weight: 600;
  position: relative;
}

/* 标题装饰效果 */
:deep(.ql-editor h1::before),
:deep(.ql-editor h2::before) {
  content: '//';
  color: var(--text-accent);
  margin-right: 0.5rem;
  font-family: monospace;
  font-weight: bold;
}

/* 链接样式 */
:deep(.ql-editor a) {
  color: var(--text-accent);
  text-decoration: none;
  border-bottom: 1px dashed var(--text-accent);
  transition: all 0.3s ease;
}

:deep(.ql-editor a:hover) {
  color: var(--link-hover-color);
  border-bottom: 1px solid var(--link-hover-color);
  text-shadow: 0 0 5px rgba(var(--highlight), 0.1);
}

/* 代码块样式 */
:deep(.ql-editor pre) {
  background-color: var(--bg-code-block);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 1rem;
  font-family: 'Courier New', monospace;
  color: var(--text-secondary);
  overflow-x: auto;
  margin: 1rem 0;
  position: relative;
}

/* 代码块装饰 */
:deep(.ql-editor pre)::before {
  content: '</>';
  position: absolute;
  top: -8px;
  left: 10px;
  background: var(--bg-code-block);
  padding: 0 8px;
  font-family: monospace;
  color: var(--text-accent);
  font-size: 0.8rem;
}

/* 列表样式 */
:deep(.ql-editor ul),
:deep(.ql-editor ol) {
  padding-left: 2rem;
  margin: 1rem 0;
}

:deep(.ql-editor li) {
  margin-bottom: 0.5rem;
  position: relative;
}

:deep(.ql-editor ul li::marker) {
  color: var(--text-accent);
}

/* 引用样式 */
:deep(.ql-editor blockquote) {
  border-left: 3px solid var(--text-accent);
  background-color: var(--bg-quote);
  padding: 1rem 1.5rem;
  margin: 1rem 0;
  border-radius: 0 6px 6px 0;
  position: relative;
}

/* 引用装饰 */
:deep(.ql-editor blockquote::before) {
  content: '"';
  position: absolute;
  top: -10px;
  left: -10px;
  font-size: 3rem;
  color: rgba(var(--highlight), 0.1);
  font-family: Georgia, serif;
}

/* 工具栏按钮样式 */
:deep(.ql-toolbar .ql-stroke) {
  stroke: var(--text-secondary);
  transition: all 0.3s ease;
}

:deep(.ql-toolbar .ql-fill) {
  fill: var(--text-secondary);
  transition: all 0.3s ease;
}

:deep(.ql-toolbar .ql-picker-label) {
  color: var(--text-secondary);
  transition: all 0.3s ease;
  border-radius: 4px;
  padding: 2px 8px;
}

:deep(.ql-toolbar .ql-picker-options) {
  background-color: var(--bg-secondary);
  border-color: var(--border-color);
  border-radius: 6px;
  margin-top: 5px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

:deep(.ql-toolbar .ql-picker-item) {
  color: var(--text-secondary);
  transition: all 0.3s ease;
  border-radius: 4px;
}

:deep(.ql-toolbar .ql-picker-item:hover),
:deep(.ql-toolbar .ql-picker-item.ql-selected) {
  color: var(--text-accent);
  background-color: rgba(var(--highlight), 0.1);
}

/* 工具栏按钮交互效果 */
:deep(.ql-toolbar button) {
  transition: all 0.3s ease;
  border-radius: 4px;
  padding: 4px;
  margin: 0 2px;
}

:deep(.ql-toolbar button:hover) {
  color: var(--text-accent);
  background-color: rgba(var(--highlight), 0.1);
  transform: translateY(-1px);
}

:deep(.ql-toolbar button:hover .ql-stroke) {
  stroke: var(--text-accent);
  stroke-width: 2;
}

:deep(.ql-toolbar button:hover .ql-fill) {
  fill: var(--text-accent);
}

/* 工具栏按钮激活状态 */
:deep(.ql-toolbar button.ql-active) {
  background-color: rgba(var(--highlight), 0.2);
  color: var(--text-accent);
}

:deep(.ql-toolbar button.ql-active .ql-stroke) {
  stroke: var(--text-accent);
  stroke-width: 2;
}

:deep(.ql-toolbar button.ql-active .ql-fill) {
  fill: var(--text-accent);
}

/* 错误信息样式 */
.error-message {
  color: var(--error-text-color);
  margin-bottom: 1rem;
  padding: 0.75rem 1rem;
  background-color: var(--error-bg-color);
  border: 1px solid var(--error-border-color);
  border-radius: 8px;
  font-size: 0.875rem;
  position: relative;
  z-index: 1;
}

/* 表单操作按钮区域 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1.5rem;
  margin-top: 2.5rem;
  padding-top: 1.5rem;
  position: relative;
  z-index: 1;
  border-top: 1px solid var(--border-color-light);
}

/* 操作区域顶部装饰 */
.form-actions::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--text-accent), transparent);
  animation: actionGlow 3s ease-in-out infinite alternate;
}

@keyframes actionGlow {
  from { opacity: 0.5; }
  to { opacity: 1; }
}

/* 取消按钮样式 */
.cancel-btn {
  padding: 0.875rem 1.75rem;
  background: linear-gradient(135deg, rgba(var(--bg-tertiary-rgb), 0.8), rgba(var(--bg-quaternary-rgb), 0.8));
  color: var(--text-secondary);
  border: 1px solid rgba(var(--highlight), 0.3);
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

/* 取消按钮内部扫描效果 */
.cancel-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(var(--highlight), 0.1), transparent);
  transition: left 0.3s ease;
}

.cancel-btn:hover {
  background: linear-gradient(135deg, rgba(var(--bg-quaternary-rgb), 0.9), rgba(var(--bg-tertiary-rgb), 0.9));
  border-color: var(--text-accent);
  color: var(--text-accent);
  box-shadow: 0 4px 20px rgba(var(--highlight), 0.4), 0 0 10px rgba(var(--highlight), 0.3);
  transform: translateY(-2px) scale(1.02);
  text-shadow: 0 0 5px rgba(var(--highlight), 0.5);
}

.cancel-btn:hover::before {
  left: 100%;
}

/* 提交按钮样式 */
.submit-btn {
  padding: 0.875rem 1.75rem;
  background: linear-gradient(135deg, var(--text-accent), var(--text-accent-dark));
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 
    0 6px 20px rgba(var(--highlight), 0.4),
    0 0 15px rgba(var(--highlight), 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  z-index: 1;
}

/* 提交按钮发光背景 */
.submit-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--text-accent-light), var(--text-accent-dark));
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

/* 提交按钮扫描效果 */
.submit-btn::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transform: rotate(45deg);
  transition: transform 0.6s ease;
}

.submit-btn:hover:not(:disabled) {
  box-shadow: 
    0 8px 30px rgba(var(--highlight), 0.6),
    0 0 20px rgba(var(--highlight), 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transform: translateY(-3px) scale(1.03);
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
  animation: submitPulse 2s ease-in-out infinite alternate;
}

@keyframes submitPulse {
  from {
    box-shadow: 
      0 8px 30px rgba(var(--highlight), 0.6),
      0 0 20px rgba(var(--highlight), 0.5),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }
  to {
    box-shadow: 
      0 10px 40px rgba(var(--highlight), 0.8),
      0 0 30px rgba(var(--highlight), 0.7),
      inset 0 1px 0 rgba(255, 255, 255, 0.4);
  }
}

.submit-btn:hover:not(:disabled)::before {
  transform: rotate(45deg) translate(100%, 100%);
}

.submit-btn:hover:not(:disabled)::after {
  opacity: 1;
}

/* 按钮点击效果 */
.submit-btn:active:not(:disabled) {
  transform: translateY(-1px) scale(0.98);
  box-shadow: 
    0 4px 15px rgba(var(--highlight), 0.5),
    0 0 10px rgba(var(--highlight), 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* 禁用状态 */
.submit-btn:disabled {
  background: linear-gradient(135deg, var(--disabled-bg-light), var(--disabled-bg-dark));
  color: var(--disabled-text-color);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
  animation: none;
}

.submit-btn:disabled::before,
.submit-btn:disabled::after {
  display: none;
}

/* 封面图片上传样式 */
.cover-image-upload {
  position: relative;
  margin-bottom: 1.5rem;
  z-index: 1;
}

.upload-placeholder {
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: var(--bg-secondary);
  backdrop-filter: blur(5px);
  position: relative;
  overflow: hidden;
}

/* 上传区域装饰线条 */
.upload-placeholder::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, transparent, transparent 40%, var(--text-accent), transparent, transparent 60%, transparent);
  background-size: 200% 200%;
  animation: uploadBorder 3s linear infinite;
  z-index: -1;
  border-radius: 10px;
}

@keyframes uploadBorder {
  0% { background-position: 0 0; }
  100% { background-position: 100% 100%; }
}

.upload-placeholder:hover {
  border-color: var(--text-accent);
  background-color: var(--bg-hover);
  box-shadow: 0 0 30px rgba(var(--highlight), 0.4);
  transform: translateY(-2px);
}

/* 上传按钮样式增强 */
.upload-btn {
  padding: 0.5rem 1.25rem;
  background: linear-gradient(135deg, var(--text-accent), var(--text-accent-dark));
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(var(--highlight), 0.3);
  font-weight: 500;
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 按钮内部扫描效果 */
.upload-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.3s ease;
}

.upload-btn:hover {
  background: linear-gradient(135deg, var(--text-accent-light), var(--text-accent-dark));
  box-shadow: 0 4px 20px rgba(var(--highlight), 0.6), 0 0 10px rgba(var(--highlight), 0.5);
  transform: translateY(-1px) scale(1.02);
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
}

.upload-btn:hover::before {
  left: 100%;
}

.upload-hint {
  margin-top: 0.75rem;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.preview-container {
  position: relative;
  max-width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--bg-secondary);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.cover-preview {
  width: 100%;
  height: auto;
  max-height: 300px;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.preview-container:hover .cover-preview {
  transform: scale(1.02);
}

.remove-cover-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 5px 12px;
  background-color: var(--error-text-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
  backdrop-filter: blur(5px);
}

.remove-cover-btn:hover {
  background-color: var(--error-hover-color);
  box-shadow: 0 0 15px rgba(var(--error-rgb), 0.5);
  transform: translateY(-1px);
}

.uploading-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: var(--bg-secondary);
  color: var(--text-secondary);
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  z-index: 10;
  backdrop-filter: blur(5px);
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.uploading-indicator::after {
  content: '';
  display: inline-block;
  width: 15px;
  height: 15px;
  margin-left: 10px;
  border: 2px solid var(--border-color);
  border-radius: 50%;
  border-top-color: var(--text-accent);
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式设计 - 平板设备 */
@media (max-width: 992px) {
  .create-article-container {
    padding: 1.5rem;
    margin: 1rem;
    border-radius: 12px;
  }
  
  /* 优化网格背景在平板上的显示 */
  .create-article::before {
    background-size: 50px 50px;
  }
  
  .create-article h1 {
    font-size: 1.75rem;
    margin-bottom: 2rem;
  }
  
  /* 标题装饰线调整 */
  .create-article h1::after {
    width: 80px;
  }
  
  .editor-container {
    min-height: 400px;
  }
  
  /* 工具栏布局优化 */
  .editor-toolbar {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .editor-toolbar button {
    padding: 0.4rem;
  }
}

/* 响应式设计 - 中小屏幕移动设备 */
@media (max-width: 768px) {
  .create-article-container {
    padding: 1.25rem;
    margin: 0.75rem;
    border-radius: 10px;
    border: 1px solid rgba(120, 119, 198, 0.4);
  }
  
  /* 简化背景效果，提升性能 */
  .create-article::before {
    background-size: 40px 40px;
    opacity: 0.08;
  }
  
  .create-article h1 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .create-article h1::after {
    width: 60px;
  }
  
  .form-group {
    margin-bottom: 1.5rem;
  }
  
  .form-group label {
    font-size: 0.9rem;
    margin-bottom: 0.6rem;
  }
  
  /* 输入框在移动端的优化 */
  .form-control {
    font-size: 1rem;
    padding: 0.8rem;
    border-radius: 6px;
  }
  
  /* 上传区域简化 */
  .cover-image-upload {
    margin-bottom: 1.25rem;
  }
  
  .upload-placeholder {
    min-height: 120px;
    padding: 1rem;
  }
  
  .upload-btn {
    padding: 0.6rem 1.2rem;
    font-size: 0.9rem;
  }
  
  /* 编辑器区域高度调整 */
  .editor-container {
    min-height: 300px;
  }
  
  /* 简化编辑器工具栏 */
  .editor-toolbar {
    padding: 0.5rem;
    gap: 0.25rem;
  }
  
  .editor-toolbar button {
    padding: 0.3rem;
    font-size: 0.85rem;
  }
  
  .editor-content {
    min-height: 200px;
    padding: 0.8rem;
    font-size: 0.95rem;
  }
  
  /* 按钮区域垂直排列 */
  .form-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
    padding-top: 1rem;
  }
  
  .cancel-btn,
  .submit-btn {
    width: 100%;
    padding: 0.8rem 1.5rem;
    font-size: 0.95rem;
  }
}

/* 响应式设计 - 小屏幕移动设备 */
@media (max-width: 480px) {
  .create-article-container {
    padding: 1rem;
    margin: 0.5rem;
    border-radius: 8px;
  }
  
  /* 进一步简化背景效果 */
  .create-article::before {
    background-size: 30px 30px;
    opacity: 0.05;
  }
  
  .create-article h1 {
    font-size: 1.3rem;
    margin-bottom: 1.25rem;
  }
  
  .create-article h1::after {
    width: 50px;
    height: 2px;
  }
  
  .form-group {
    margin-bottom: 1.25rem;
  }
  
  .form-control {
    padding: 0.75rem;
    font-size: 0.95rem;
  }
  
  /* 最小化上传区域 */
  .upload-placeholder {
    min-height: 100px;
    padding: 0.8rem;
  }
  
  .upload-btn {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }
  
  .editor-container {
    min-height: 250px;
  }
  
  /* 更紧凑的编辑器工具栏 */
  .editor-toolbar {
    padding: 0.4rem;
  }
  
  .editor-toolbar button {
    padding: 0.25rem;
    min-width: 28px;
    height: 28px;
  }
  
  .editor-content {
    min-height: 180px;
    padding: 0.75rem;
    font-size: 0.9rem;
  }
  
  /* 更紧凑的按钮 */
  .cancel-btn,
  .submit-btn {
    padding: 0.75rem 1.25rem;
    font-size: 0.9rem;
  }
  
  /* 简化按钮特效以提升性能 */
  .submit-btn {
    box-shadow: 
      0 4px 10px rgba(var(--highlight), 0.3),
      0 0 8px rgba(var(--highlight), 0.2);
  }
  
  .submit-btn:hover:not(:disabled) {
    animation: none;
    transform: translateY(-2px) scale(1.01);
  }
  
  /* 简化操作区域装饰 */
  .form-actions::before {
    animation: none;
  }
}
</style>
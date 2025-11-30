# 博客系统前端

## 项目概述

这是一个基于Vue 3的博客系统前端，提供了完整的博客功能支持，包括用户登录注册、文章浏览、文章发布、评论管理、个人中心等核心功能。系统采用现代化的前端技术栈，具有良好的用户体验和响应式设计。

## 功能特性

### 1. 用户管理
- 用户注册（邮箱验证码验证）
- 用户登录（支持密码可见性切换）
- 忘记密码（邮箱验证码重置）
- 个人信息管理
- 头像上传

### 2. 文章管理
- 文章列表展示（支持分页、排序）
- 文章搜索（支持全文搜索、内容搜索、作者搜索）
- 文章详情查看
- 文章发布与编辑（支持富文本编辑器）
- 文章封面图片上传

### 3. 评论管理
- 文章评论展示
- 评论发布
- 评论删除

### 4. 管理员功能
- 管理员仪表盘
- 用户管理
- 文章管理
- 评论管理

### 5. 其他功能
- 响应式设计，适配移动端和桌面端
- 平滑的页面过渡动画
- 加载状态和错误处理
- 全局搜索功能

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.2.47 | 前端框架 |
| Vue Router | 4.1.6 | 路由管理 |
| Axios | 1.3.4 | HTTP客户端 |
| Vue Quill Editor | 3.0.6 | 富文本编辑器 |
| Vite | 4.2.0 | 构建工具 |

## 环境配置

### 1. 安装Node.js

确保你的系统中已安装Node.js 16+版本。可以通过以下命令检查Node.js版本：

```bash
node -v
```

### 2. 安装依赖

在项目根目录下执行以下命令安装项目依赖：

```bash
npm install
```

### 3. 配置API代理

项目已在`vite.config.js`中配置了API代理，默认代理到`http://localhost:8080`。如果后端服务运行在不同的地址，可以修改该配置：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 修改为你的后端服务地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 开发指南

### 1. 启动开发服务器

执行以下命令启动开发服务器：

```bash
npm run dev
```

开发服务器将在`http://localhost:3000`上运行。

### 2. 代码结构

项目采用清晰的代码结构，主要目录说明如下：

```
src/
├── assets/          # 静态资源
│   └── images/      # 图片资源
├── views/           # 页面组件
│   ├── Home.vue          # 首页
│   ├── ArticleDetail.vue # 文章详情
│   ├── CreateArticle.vue # 创建/编辑文章
│   ├── Login.vue         # 登录页
│   ├── Register.vue      # 注册页
│   ├── Profile.vue       # 个人中心
│   ├── ForgotPassword.vue # 忘记密码
│   └── AdminDashboard.vue # 管理员仪表盘
├── App.vue          # 根组件
├── axios.js         # Axios配置
├── main.js          # 应用入口
└── router.js        # 路由配置
```

### 3. 组件开发规范

- 组件命名采用PascalCase命名法（如`ArticleDetail.vue`）
- 组件内部使用Composition API
- 样式使用scoped属性，避免样式冲突
- 组件逻辑清晰，职责单一
- 适当使用动画和过渡效果提升用户体验

### 4. API调用规范

- 所有API调用通过`axios.js`中配置的实例进行
- API路径统一使用相对路径，通过代理转发到后端
- 请求和响应拦截器处理全局的认证和错误
- 使用async/await处理异步请求

### 5. 状态管理

项目目前使用localStorage存储用户信息和token，主要状态包括：

- 用户登录状态
- JWT令牌
- 用户信息
- 全局搜索状态

### 6. 路由管理

路由配置在`router.js`中，采用懒加载方式加载组件，提高应用性能。路由守卫用于保护需要认证的路由。

## 构建与部署

### 1. 构建生产版本

执行以下命令构建生产版本：

```bash
npm run build
```

构建产物将生成在`dist`目录中。

### 2. 预览生产版本

执行以下命令预览生产版本：

```bash
npm run preview
```

预览服务器将在`http://localhost:4173`上运行。

### 3. 部署

将`dist`目录中的文件部署到任何静态文件服务器上即可，例如：

- Nginx
- Apache
- Netlify
- Vercel
- GitHub Pages

## 目录结构说明

```
├── src/                  # 源代码目录
│   ├── assets/           # 静态资源
│   │   └── images/       # 图片资源
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   ├── axios.js          # Axios配置
│   ├── main.js           # 应用入口
│   └── router.js         # 路由配置
├── index.html            # HTML模板
├── package.json          # 项目配置和依赖
├── vite.config.js        # Vite配置
└── README.md             # 项目说明文档
```

### 核心文件说明

| 文件 | 功能 |
|------|------|
| `main.js` | Vue应用初始化，配置全局组件和插件 |
| `router.js` | 路由配置，定义页面导航结构 |
| `axios.js` | Axios实例配置，包含请求和响应拦截器 |
| `App.vue` | 根组件，包含应用布局和全局状态 |
| `vite.config.js` | Vite构建配置，包含代理设置 |

## 常见问题解答

### 1. 如何修改API代理地址？

修改`vite.config.js`文件中的`server.proxy`配置：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://your-backend-url:port',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

### 2. 如何添加新页面？

1. 在`views`目录下创建新的Vue组件
2. 在`router.js`中添加路由配置
3. 在需要的地方添加导航链接

### 3. 如何处理API错误？

API错误通过axios的响应拦截器统一处理，在`axios.js`文件中可以修改错误处理逻辑。

### 4. 如何添加新的API请求？

使用配置好的axios实例发送请求，例如：

```javascript
import axios from '../axios'

const fetchData = async () => {
  try {
    const response = await axios.get('/api/resource')
    return response.data
  } catch (error) {
    console.error('请求失败:', error)
    throw error
  }
}
```

### 5. 如何修改主题样式？

可以在各组件的scoped样式中修改，或者在`App.vue`中添加全局样式。

### 6. 如何添加动画效果？

使用Vue的`<transition>`组件添加过渡动画，或者使用CSS动画。

## 浏览器兼容性

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 开发工具推荐

- VS Code + Volar插件
- Chrome DevTools
- Vue DevTools

## 版本更新日志

### v1.0.0
- 初始版本
- 实现用户管理功能
- 实现文章管理功能
- 实现评论管理功能
- 实现响应式设计


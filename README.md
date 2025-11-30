# 博客系统

## 1. 项目概述

### 1.1 主要功能

这是一个功能完整的博客系统，包含以下核心功能：

- **用户管理**：注册、登录、忘记密码、个人信息管理
- **文章管理**：发布、编辑、浏览、搜索、分页
- **评论管理**：发表评论、查看评论、删除评论
- **文件管理**：图片上传、图片删除
- **管理员功能**：用户管理、文章管理、评论管理、活跃用户统计

### 1.2 设计目标

- 采用前后端分离架构，提高系统的可维护性和扩展性
- 提供良好的用户体验，包括响应式设计、平滑动画和直观的操作界面
- 确保系统的安全性，包括JWT认证、密码加密、权限控制
- 实现高效的数据查询和管理，支持分页、排序和搜索功能

### 1.3 应用场景

- 个人博客系统
- 企业内部知识库
- 内容管理系统
- 学习和教学演示

## 2. 技术栈说明

### 2.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11 | 开发语言 |
| Spring Boot | 2.7.5 | 应用框架 |
| Spring Data JPA | 2.7.5 | ORM框架 |
| Spring Security | 2.7.5 | 安全框架 |
| Spring Mail | 2.7.5 | 邮件服务 |
| MySQL | 8.0+ | 数据库 |
| JWT | 0.9.1 | 身份认证 |
| Lombok | 1.18.24 | 简化代码 |
| Maven | 3.8+ | 构建工具 |

### 2.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.2.47 | 前端框架 |
| Vue Router | 4.1.6 | 路由管理 |
| Axios | 1.3.4 | HTTP客户端 |
| Vue Quill Editor | 3.0.6 | 富文本编辑器 |
| Vite | 4.2.0 | 构建工具 |
| Node.js | 16+ | 运行环境 |

## 3. 项目结构

```
blog1114/
├── backend/                    # 后端代码
│   ├── src/                     # 源代码目录
│   │   ├── main/                # 主代码目录
│   │   │   ├── java/            # Java源代码
│   │   │   │   └── com/blog/    # 包路径
│   │   │   │       ├── article/  # 文章模块
│   │   │   │       ├── comment/  # 评论模块
│   │   │   │       ├── common/   # 公共模块
│   │   │   │       ├── config/   # 配置模块
│   │   │   │       ├── file/     # 文件模块
│   │   │   │       ├── security/ # 安全模块
│   │   │   │       ├── user/     # 用户模块
│   │   │   │       └── BlogApplication.java # 应用入口
│   │   │   └── resources/       # 资源文件
│   │   │       ├── application.properties # 应用配置
│   │   │       └── blog_db.sql   # 数据库初始化脚本
│   │   └── test/                 # 测试代码目录
│   ├── uploads/                  # 文件上传目录
│   ├── README.md                 # 后端说明文档
│   └── pom.xml                   # Maven配置文件
├── frontend/                    # 前端代码
│   ├── src/                     # 源代码目录
│   │   ├── assets/              # 静态资源
│   │   │   └── images/          # 图片资源
│   │   ├── views/               # 页面组件
│   │   ├── App.vue              # 根组件
│   │   ├── axios.js             # Axios配置
│   │   ├── main.js              # 应用入口
│   │   └── router.js            # 路由配置
│   ├── README.md                # 前端说明文档
│   ├── index.html               # HTML模板
│   ├── package.json             # 项目配置和依赖
│   └── vite.config.js           # Vite配置
├── .gitignore                   # Git忽略文件
├── 博客系统详细设计文档.md        # 系统设计文档
└── README.md                    # 项目主说明文档
```

## 4. 前端部署运行指南

### 4.1 环境要求

- Node.js 16+版本
- npm 8+版本

### 4.2 依赖安装

1. 进入前端目录：
   ```bash
   cd frontend
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

### 4.3 开发环境启动

```bash
npm run dev
```

开发服务器将在 `http://localhost:3000` 上运行。

### 4.4 生产环境构建

```bash
npm run build
```

构建产物将生成在 `dist` 目录中。

### 4.5 部署方式

#### 4.5.1 Nginx部署

1. 安装Nginx
2. 配置Nginx：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       root /path/to/frontend/dist;
       index index.html;
       
       location / {
           try_files $uri $uri/ /index.html;
       }
       
       location /api {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
   ```
3. 重启Nginx：
   ```bash
   systemctl restart nginx
   ```

#### 4.5.2 Docker部署

1. 在前端目录下创建Dockerfile：
   ```dockerfile
   # 阶段1：构建前端应用
   FROM node:16-alpine AS build
   WORKDIR /app
   
   # 复制package.json和package-lock.json
   COPY package*.json ./
   
   # 安装依赖
   RUN npm install
   
   # 复制源代码
   COPY . .
   
   # 构建生产版本
   RUN npm run build
   
   # 阶段2：使用Nginx作为Web服务器
   FROM nginx:alpine
   
   # 复制构建产物到Nginx的html目录
   COPY --from=build /app/dist /usr/share/nginx/html
   
   # 复制自定义Nginx配置文件
   COPY nginx.conf /etc/nginx/conf.d/default.conf
   
   # 暴露80端口
   EXPOSE 80
   
   # 启动Nginx
   CMD ["nginx", "-g", "daemon off;"]
   ```

2. 在前端目录下创建nginx.conf文件：
   ```nginx
   server {
       listen 80;
       server_name localhost;
       root /usr/share/nginx/html;
       index index.html;
       
       location / {
           try_files $uri $uri/ /index.html;
       }
       
       location /api {
           proxy_pass http://host.docker.internal:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
   ```
   > 注意：如果Docker版本不支持`host.docker.internal`，可以使用实际的后端服务IP地址。

3. 构建Docker镜像：
   ```bash
   cd frontend
   docker build -t blog-frontend .
   ```

4. 运行Docker容器：
   ```bash
   # 直接运行
   docker run -d -p 80:80 --name blog-frontend blog-frontend
   
   # 如果需要连接到后端服务所在的网络，可以使用--network参数
   # 假设后端服务运行在名为blog-network的网络中
   # docker run -d -p 80:80 --name blog-frontend --network blog-network blog-frontend
   ```

5. 访问应用：
   打开浏览器访问 `http://localhost` 即可查看博客系统前端。

#### 4.5.3 其他部署方式

- Netlify
- Vercel
- GitHub Pages

## 5. 后端部署运行指南

### 5.1 环境要求

- Java 11+版本
- MySQL 8.0+数据库
- Maven 3.8+版本

### 5.2 数据库初始化

1. 创建数据库：
   ```sql
   CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 导入初始数据：
   ```bash
   mysql -u root -p blog_db < backend/src/main/resources/blog_db.sql
   ```

### 5.3 依赖安装

```bash
cd backend
mvn clean install
```

### 5.4 服务启动

#### 5.4.1 开发环境启动

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 上运行。

#### 5.4.2 生产环境启动

1. 构建生产包：
   ```bash
   mvn clean package -DskipTests
   ```

2. 运行jar包：
   ```bash
   java -jar target/blog-system-1.0-SNAPSHOT.jar
   ```

### 5.5 环境变量配置

修改 `backend/src/main/resources/application.properties` 文件：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true&characterSetResults=UTF-8
spring.datasource.username=root
spring.datasource.password=your-password

# JWT配置
jwt.secret=your-secret-key
jwt.expiration=86400000

# 文件上传配置
file.upload-dir=./uploads
file.base-url=http://your-domain.com/uploads

# 邮箱配置
spring.mail.username=your-email@example.com
spring.mail.password=your-email-password
```

### 5.6 部署方案

#### 5.6.1 直接部署

1. 将构建好的jar包上传到服务器
2. 配置application.properties文件
3. 运行jar包

#### 5.6.2 Docker部署

1. 创建Dockerfile：
   ```dockerfile
   FROM openjdk:11-jre-slim
   COPY target/blog-system-1.0-SNAPSHOT.jar /app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```

2. 构建Docker镜像：
   ```bash
   docker build -t blog-backend .
   ```

3. 运行Docker容器：
   ```bash
   docker run -d -p 8080:8080 --name blog-backend blog-backend
   ```

#### 5.6.3 容器编排部署

使用Docker Compose或Kubernetes进行容器编排部署。

## 6. 常见问题解决

### 6.1 前端问题

#### 6.1.1 依赖安装失败

**问题**：npm install 命令执行失败

**解决方案**：
- 检查Node.js版本是否符合要求
- 清除npm缓存：`npm cache clean --force`
- 使用国内镜像源：`npm config set registry https://registry.npmmirror.com`
- 删除node_modules目录后重新安装

#### 6.1.2 开发服务器启动失败

**问题**：npm run dev 命令执行失败

**解决方案**：
- 检查端口3000是否被占用
- 检查vite.config.js配置是否正确
- 查看错误日志，根据具体错误信息进行修复

#### 6.1.3 API请求失败

**问题**：前端无法正常调用后端API

**解决方案**：
- 检查后端服务是否正常运行
- 检查vite.config.js中的代理配置是否正确
- 检查浏览器控制台的网络请求和错误信息

### 6.2 后端问题

#### 6.2.1 数据库连接失败

**问题**：应用启动时无法连接到数据库

**解决方案**：
- 检查MySQL服务是否正常运行
- 检查application.properties中的数据库配置是否正确
- 检查数据库用户权限是否足够
- 检查数据库是否存在

#### 6.2.2 端口被占用

**问题**：应用启动时端口8080被占用

**解决方案**：
- 查找并关闭占用端口的进程
- 修改application.properties中的server.port配置

#### 6.2.3 邮件发送失败

**问题**：注册或忘记密码时无法发送验证码

**解决方案**：
- 检查application.properties中的邮件配置是否正确
- 检查邮箱服务器是否允许第三方登录
- 检查网络连接是否正常

### 6.3 其他问题

#### 6.3.1 文件上传失败

**问题**：无法上传图片文件

**解决方案**：
- 检查文件大小是否超过限制（默认2MB）
- 检查文件类型是否为图片
- 检查uploads目录权限是否正确
- 检查file.upload-dir配置是否正确

#### 6.3.2 JWT令牌失效

**问题**：登录后一段时间内令牌失效

**解决方案**：
- 检查jwt.expiration配置是否合理
- 实现令牌刷新机制
- 增加客户端自动重新登录功能




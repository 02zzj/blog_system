# 博客系统后端

## 项目概述

这是一个基于Spring Boot的博客系统后端，提供了完整的博客功能支持，包括用户管理、文章管理、评论管理、文件上传等核心功能。系统采用分层架构设计，具有良好的可扩展性和可维护性。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.5 | 应用框架 |
| Spring Data JPA | 2.7.5 | ORM框架 |
| Spring Security | 2.7.5 | 安全框架 |
| Spring Mail | 2.7.5 | 邮件服务 |
| MySQL | 8.0+ | 数据库 |
| JWT | 0.9.1 | 身份认证 |
| Lombok | 1.18.24 | 简化代码 |
| Java | 11 | 开发语言 |

## 环境配置

### 1. 数据库配置

1. 安装MySQL 8.0+数据库
2. 创建数据库：`CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. 导入初始数据：执行`src/main/resources/blog_db.sql`文件

### 2. 应用配置

修改`src/main/resources/application.properties`文件中的配置项：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true&characterSetResults=UTF-8
spring.datasource.username=root
spring.datasource.password=123456

# JWT配置
jwt.secret=blog-secret-key-change-in-production
jwt.expiration=86400000

# 文件上传配置
file.upload-dir=./uploads
file.base-url=http://localhost:8080/uploads

# QQ邮箱SMTP配置（用于发送验证码）
spring.mail.username=your-email@qq.com
spring.mail.password=your-email-password
```

### 3. 依赖安装

```bash
# 使用Maven安装依赖
mvn clean install
```

## 启动与部署

### 开发环境启动

```bash
# 方式1：使用Maven命令启动
mvn spring-boot:run

# 方式2：直接运行主类
java -jar target/blog-system-1.0-SNAPSHOT.jar
```

### 生产环境部署

1. 构建生产环境包：
   ```bash
   mvn clean package -DskipTests
   ```

2. 部署到服务器：
   ```bash
   java -jar blog-system-1.0-SNAPSHOT.jar --spring.profiles.active=prod
   ```

## 项目结构

```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── blog
│   │   │           ├── article/           # 文章模块
│   │   │           │   ├── controller/    # 文章控制器
│   │   │           │   ├── dto/           # 文章数据传输对象
│   │   │           │   ├── entity/        # 文章实体类
│   │   │           │   ├── repository/    # 文章数据访问层
│   │   │           │   └── service/       # 文章业务逻辑层
│   │   │           ├── comment/           # 评论模块
│   │   │           │   ├── controller/    # 评论控制器
│   │   │           │   ├── dto/           # 评论数据传输对象
│   │   │           │   ├── entity/        # 评论实体类
│   │   │           │   ├── repository/    # 评论数据访问层
│   │   │           │   └── service/       # 评论业务逻辑层
│   │   │           ├── common/            # 公共模块
│   │   │           │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   │           │   └── ResponseResult.java          # 统一响应结果
│   │   │           ├── config/            # 配置模块
│   │   │           │   └── WebConfig.java # Web配置
│   │   │           ├── file/              # 文件模块
│   │   │           │   ├── config/        # 文件配置
│   │   │           │   ├── controller/    # 文件控制器
│   │   │           │   └── util/          # 文件工具类
│   │   │           ├── security/          # 安全模块
│   │   │           │   ├── JwtFilter.java     # JWT过滤器
│   │   │           │   ├── JwtProvider.java   # JWT工具类
│   │   │           │   └── SecurityConfig.java # 安全配置
│   │   │           ├── user/              # 用户模块
│   │   │           │   ├── controller/    # 用户控制器
│   │   │           │   ├── dto/           # 用户数据传输对象
│   │   │           │   ├── entity/        # 用户实体类
│   │   │           │   ├── repository/    # 用户数据访问层
│   │   │           │   ├── service/       # 用户业务逻辑层
│   │   │           │   └── util/          # 用户工具类
│   │   │           └── BlogApplication.java # 应用入口
│   │   └── resources
│   │       ├── application.properties     # 应用配置
│   │       └── blog_db.sql                # 数据库初始化脚本
│   └── test
├── uploads/                                # 文件上传目录
└── pom.xml                                 # Maven配置文件
```

## 核心功能模块

### 1. 用户管理

#### 功能描述
- 用户注册（邮箱验证码验证）
- 用户登录（JWT认证）
- 忘记密码（邮箱验证码重置）
- 个人信息管理
- 管理员用户管理
- 活跃用户统计

#### 关键API
- `POST /api/users/send-verification` - 发送注册验证码
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/me` - 更新用户信息

### 2. 文章管理

#### 功能描述
- 文章创建
- 文章列表查询（支持分页、排序、搜索）
- 文章详情查询
- 文章更新
- 文章删除

#### 关键API
- `POST /api/articles` - 创建文章
- `GET /api/articles` - 获取文章列表
- `GET /api/articles/{id}` - 获取文章详情
- `PUT /api/articles/{id}` - 更新文章
- `DELETE /api/articles/{id}` - 删除文章

### 3. 评论管理

#### 功能描述
- 评论创建
- 文章评论列表查询
- 评论删除

#### 关键API
- `POST /api/comments` - 创建评论
- `GET /api/comments/article/{articleId}` - 获取文章评论
- `DELETE /api/comments/{id}` - 删除评论

### 4. 文件管理

#### 功能描述
- 图片上传
- 图片删除

#### 关键API
- `POST /api/files/upload` - 上传图片
- `DELETE /api/files/delete/{filename}` - 删除图片

### 5. 管理员功能

#### 功能描述
- 用户列表查询
- 用户禁用/启用
- 用户删除
- 活跃用户统计
- 活跃用户列表查询

#### 关键API
- `GET /api/users/admin` - 获取所有用户
- `PUT /api/users/admin/{userId}/disable` - 禁用用户
- `PUT /api/users/admin/{userId}/enable` - 启用用户
- `DELETE /api/users/admin/{userId}` - 删除用户
- `GET /api/users/admin/active-count` - 获取活跃用户数

## API接口文档

### 1. 用户相关接口

#### 注册验证码发送
```
POST /api/users/send-verification
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 用户注册
```
POST /api/users/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "测试用户",
  "code": "123456"
}
```

#### 用户登录
```
POST /api/users/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### 2. 文章相关接口

#### 创建文章
```
POST /api/articles
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "文章标题",
  "content": "文章内容",
  "coverImage": "http://localhost:8080/uploads/image.jpg"
}
```

#### 获取文章列表
```
GET /api/articles?page=1&size=10
```

### 3. 评论相关接口

#### 创建评论
```
POST /api/comments
Content-Type: application/json
Authorization: Bearer {token}

{
  "articleId": 1,
  "content": "评论内容"
}
```

### 4. 文件相关接口

#### 上传图片
```
POST /api/files/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: [图片文件]
```

## 安全机制

### JWT认证流程
1. 用户登录成功后，服务器生成JWT令牌
2. 客户端将JWT令牌存储在本地
3. 每次请求时，客户端在请求头中携带JWT令牌
4. 服务器通过JWT过滤器验证令牌有效性
5. 验证通过后，允许访问受保护资源

### 权限控制
- 公开接口：注册、登录、忘记密码、文章列表、文章详情、评论列表
- 需要认证的接口：创建文章、更新文章、删除文章、创建评论、删除评论、文件上传、个人信息管理
- 管理员接口：用户管理、活跃用户统计等

## 数据库设计

### 1. 用户表（user）
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 用户ID |
| email | VARCHAR(100) | 邮箱（唯一） |
| password | VARCHAR(255) | 密码（加密存储） |
| nickname | VARCHAR(50) | 昵称 |
| introduction | VARCHAR(500) | 简介 |
| avatar | VARCHAR(255) | 头像URL |
| role | VARCHAR(20) | 角色（USER/ADMIN） |
| enabled | BOOLEAN | 账号状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2. 文章表（article）
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 文章ID |
| author_id | BIGINT | 作者ID |
| title | VARCHAR(255) | 标题 |
| content | TEXT | 内容 |
| cover_image | VARCHAR(255) | 封面图片URL |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 3. 评论表（comment）
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 评论ID |
| article_id | BIGINT | 文章ID |
| user_id | BIGINT | 用户ID |
| content | TEXT | 评论内容 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 常见问题解决

### 1. 验证码发送失败
- 检查邮箱配置是否正确
- 检查网络连接是否正常
- 检查邮箱服务器是否允许第三方登录

### 2. 图片上传失败
- 检查文件大小是否超过限制（默认2MB）
- 检查文件类型是否为图片
- 检查上传目录权限是否正确

### 3. JWT令牌失效
- 检查令牌是否过期（默认24小时）
- 检查令牌是否被篡改
- 重新登录获取新令牌

### 4. 数据库连接失败
- 检查数据库服务是否启动
- 检查数据库配置是否正确
- 检查数据库用户权限是否足够

## 开发指南

### 代码规范
- 遵循Java编码规范
- 使用Lombok简化代码
- 分层架构设计
- 统一响应格式

### 测试
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

### 日志
- 使用SLF4J+Logback日志框架
- 日志级别：DEBUG、INFO、WARN、ERROR
- 生产环境建议使用INFO级别

## 部署建议

### 1. 生产环境配置
- 修改`jwt.secret`为复杂随机字符串
- 使用HTTPS协议
- 配置合适的JWT过期时间
- 配置生产环境数据库连接

### 2. 性能优化
- 使用Redis缓存热点数据
- 配置数据库连接池
- 启用Gzip压缩
- 配置CDN加速静态资源

### 3. 监控与告警
- 配置Spring Boot Actuator
- 集成Prometheus+Grafana监控
- 配置日志收集与分析

## 版本更新日志

### v1.0.0
- 初始版本
- 实现用户管理功能
- 实现文章管理功能
- 实现评论管理功能
- 实现文件上传功能
- 实现管理员功能


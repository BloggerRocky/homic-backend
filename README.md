# Homic 家庭云存储服务 - 后端

## 项目简介

Homic 家庭云存储服务后端是基于 Spring Boot 2.6.1 构建的现代化云存储平台服务端，提供稳定、高效、安全的文件存储和管理服务。

## 版本信息

**当前版本：v1.0.2**

## 技术栈

- **框架：** Spring Boot 2.6.1
- **数据库：** MySQL + MyBatis Plus 3.4.1
- **缓存：** Redis
- **文件存储：** MinIO 8.3.6
- **消息队列：** RabbitMQ
- **工具库：** Hutool 5.8.25、Apache Commons
- **日志：** Logback 1.2.10
- **其他：** Java 17、Maven、Lombok

## 核心功能模块

### 🔐 用户认证与授权
- **用户注册登录：** 支持邮箱注册、密码加密存储
- **权限管理：** 基于角色的访问控制（RBAC）
- **验证码：** 图形验证码防止恶意注册

### 📁 文件管理系统
- **文件上传：** 支持大文件分片上传、断点续传
- **文件存储：** 基于MinIO的分布式文件存储
- **文件管理：** 文件增删改查、文件夹管理
- **文件预览：** 多格式文件在线预览支持
- **文件下载：** 安全的文件下载服务
- **文件回收：** 软删除机制，10天恢复期

### 👥 好友系统
- **好友关系管理：** 添加好友、删除好友、好友列表
- **好友搜索：** 根据用户名或邮箱搜索
- **好友申请：** 好友请求发送与处理
- **好友分享：** 文件向指定好友分享

### 👨‍👩‍👧‍👦 家庭系统
- **家庭管理：** 创建家庭、加入家庭、成员管理
- **家庭空间：** 家庭共享文件存储空间
- **关怀账号：** 为家庭成员创建简化操作账号
- **家庭权限：** 不同角色的权限控制

### 📤 分享系统
- **链接分享：** 生成分享链接和访问码
- **分享管理：** 分享记录查询、分享取消
- **外部分享：** 支持无登录访问分享内容
- **分享统计：** 分享访问统计

### 🗑️ 回收站系统
- **软删除：** 文件删除后进入回收站
- **自动清理：** 超过10天自动永久删除
- **文件恢复：** 支持从回收站恢复文件

### ⚙️ 系统管理
- **用户管理：** 用户列表、用户状态管理
- **文件管理：** 系统文件统计、用户文件管理
- **系统配置：** 系统参数配置管理
- **数据统计：** 存储空间、用户活跃度统计

## 项目结构

```
back-end/
├── src/main/java/com/example/homic/
│   ├── controller/          # 控制器层
│   │   ├── AccountController.java      # 用户账户相关
│   │   ├── AdminController.java        # 管理员功能
│   │   ├── CareAccountController.java  # 关怀账号
│   │   ├── FamilyController.java       # 家庭管理
│   │   ├── FileController.java         # 文件管理
│   │   ├── FriendController.java       # 好友系统
│   │   ├── ShareController.java        # 分享功能
│   │   └── ...
│   ├── services/            # 服务层接口
│   ├── services/implement/  # 服务层实现
│   ├── entity/              # 实体类
│   ├── mapper/              # 数据访问层
│   ├── dto/                 # 数据传输对象
│   ├── config/              # 配置类
│   │   ├── MinioConfig.java         # MinIO配置
│   │   ├── RedisConfig.java         # Redis配置
│   │   ├── MybatisPlusConfig.java   # MyBatis Plus配置
│   │   └── ...
│   ├── annotation/          # 自定义注解
│   ├── aspect/              # 切面编程
│   ├── constants/           # 常量定义
│   ├── enums/               # 枚举类
│   ├── exception/           # 异常处理
│   ├── utils/               # 工具类
│   └── HomicApplication.java # 启动类
├── src/main/resources/
│   ├── application.yml      # 配置文件
│   ├── mapper/              # MyBatis映射文件
│   └── static/              # 静态资源
└── pom.xml                  # Maven配置
```

## 主要API接口

### 用户相关
- `POST /account/register` - 用户注册
- `POST /account/login` - 用户登录
- `POST /account/logout` - 用户登出
- `PUT /account/updatePassword` - 修改密码
- `POST /account/recoverPassword` - 密码找回

### 文件相关
- `POST /file/upload` - 文件上传
- `GET /file/download/{fileId}` - 文件下载
- `POST /file/newFolder` - 创建文件夹
- `PUT /file/rename` - 文件重命名
- `DELETE /file/delFile` - 删除文件
- `POST /file/changeFileFolder` - 移动文件
- `GET /file/loadDataList` - 获取文件列表

### 好友相关
- `GET /friend/myFriends` - 获取好友列表
- `POST /friend/search` - 搜索好友
- `POST /friend/addFriend` - 添加好友
- `POST /friend/applyFriend` - 申请添加好友
- `GET /friend/requests` - 获取好友申请列表

### 家庭相关
- `POST /family/create` - 创建家庭
- `POST /family/join` - 加入家庭
- `GET /family/info` - 获取家庭信息
- `PUT /family/update` - 更新家庭信息
- `POST /family/createCareAccount` - 创建关怀账号

### 分享相关
- `POST /share/share` - 创建分享
- `GET /share/myShare` - 获取我的分享
- `GET /share/checkShare/{shareId}` - 校验分享
- `GET /share/share/{shareId}` - 访问分享

## 数据库设计

### 主要数据表
- `users` - 用户表
- `file_info` - 文件信息表
- `friend_info` - 好友关系表
- `friend_request` - 好友申请表
- `family_info` - 家庭信息表
- `family_user` - 家庭成员关系表
- `share_info` - 分享信息表
- `share_files` - 分享文件关联表
- `recycle_bin` - 回收站表

## 安全特性

### 认证授权
- JWT Token认证机制
- 基于角色的权限控制
- 接口级别的权限验证

### 数据安全
- 密码BCrypt加密存储
- 文件访问权限控制
- 敏感数据脱敏处理

### 系统安全
- SQL注入防护（MyBatis参数化查询）
- XSS攻击防护
- CSRF防护
- 文件上传安全检查

## 性能优化

### 缓存策略
- Redis缓存用户会话
- 文件元数据缓存
- 热点数据缓存

### 数据库优化
- 索引优化
- 分页查询
- 连接池配置

### 文件处理
- 异步文件处理
- 文件分片上传
- 断点续传支持

## 部署配置

### 环境要求
- Java 17+
- MySQL 8.0+
- Redis 6.0+
- MinIO Server
- RabbitMQ 3.8+

### 配置文件
```yaml
# application.yml 主要配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/homic
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    
minio:
  endpoint: ${MINIO_ENDPOINT}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket-name: ${MINIO_BUCKET_NAME}
```
## 更新日志

### v1.0.2 (2026)
- 优化文件上传性能
- 增强安全性检查
- 修复已知问题

### v1.0.1
- 完善家庭系统功能
- 优化缓存策略

---

**最后更新：** 2026年

# 模块详细说明

## 📚 模块架构

本项目采用 **DDD领域驱动设计** 的分层架构，共17个模块，职责清晰，易于维护和扩展。

---

## 🔧 核心模块详解

### 1. xiaoming-common (公共模块)
**文件数**: 7个
**职责**: 提供所有模块共享的基础组件

#### 1.1 constant (常量定义)
- `ResultCode.java` - 统一返回状态码
  - 200: 成功
  - 400: 参数错误
  - 401: 未认证
  - 403: 权限不足
  - 404: 资源不存在
  - 500: 服务器错误

#### 1.2 exception (异常处理)
- `BusinessException.java` - 业务异常基类
- `GlobalExceptionEnum.java` - 全局异常枚举
- `GlobalExceptionHandler.java` - 全局异常处理器

#### 1.3 result (统一返回)
- `Result.java` - 统一响应格式封装
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {}
  }
  ```

#### 1.4 page (分页)
- `PageRequest.java` - 分页请求参数
- `PageResult.java` - 分页响应结果

---

### 2. xiaoming-security (安全模块)
**文件数**: 4个
**职责**: JWT认证与权限控制

#### 核心功能
- **JWT双Token机制**
  - AccessToken: 7天有效期
  - RefreshToken: 30天有效期
- **Redis分布式Session**
  - Token存储: `token:{userId}`
  - TTL: 7天
- **认证流程**
  1. 用户登录 → 验证账号密码
  2. 生成 AccessToken + RefreshToken
  3. 存储到 Redis
  4. 返回给客户端
- **续期流程**
  1. 接口返回 401
  2. 检查 RefreshToken
  3. 验证 Redis 中的 RefreshToken
  4. 生成新 AccessToken
  5. 返回给客户端 (无感知)

#### 技术亮点
- BCrypt 密码加密 (强度 10)
- JWT Token 签名验证
- Token 过期机制
- RefreshToken 轮换
- 踢用户下线 (主动删除 Redis Token)

---

### 3. xiaoming-module-user (用户模块)
**文件数**: 10个
**职责**: 用户注册、登录、信息管理

#### 核心功能
- 用户注册 (手机号/邮箱 + 密码)
- 用户登录 (账号密码验证)
- 用户信息查询与修改
- 头像上传 (MinIO)
- 用户列表分页查询

#### 数据库表
- `user` - 用户表

#### 技术亮点
- BCrypt 密码加密
- 参数校验 (@Valid)
- 头像压缩和格式转换

---

### 4. xiaoming-module-note (笔记模块)
**文件数**: 19个
**职责**: 笔记的发布、编辑、删除、查询

#### 核心功能
- 发布图文/视频笔记
- 编辑笔记
- 删除笔记 (软删除)
- 查看笔记详情
- 图片上传 (多图支持)
- 视频上传
- 笔记列表分页查询
- 热门笔记推荐
- 笔记状态管理 (草稿/已发布/已下架)

#### 数据库表
- `note` - 笔记表
- `note_image` - 笔记图片表
- `note_video` - 笔记视频表
- `hot_note` - 热度统计表

#### 技术亮点
- 图片压缩和缩略图生成
- 视频封面自动提取
- 热度算法设计
- 笔记状态流转管理

---

### 5. xiaoming-module-interaction (互动模块)
**文件数**: 11个
**职责**: 点赞、收藏功能

#### 核心功能
- 点赞 / 取消点赞
- 点赞状态查询
- 点赞数实时统计
- 收藏 / 取消收藏
- 收藏列表查询

#### 数据库表
- `likes` - 点赞表
- `collect` - 收藏表

#### 技术亮点
- 唯一索引防止重复点赞/收藏
- Redis 计数器 (点赞数/收藏数)
- 异步更新统计数据

---

### 6. xiaoming-module-comment (评论模块)
**文件数**: (数量待统计)
**职责**: 评论发布、回复、查询

#### 核心功能
- 发布一级评论
- 回复评论 (二级评论)
- 评论分页查询
- 评论删除 (软删除)
- 评论点赞

#### 数据库表
- `comment` - 评论表

#### 技术亮点
- 树形评论结构 (parent_id)
- 评论数缓存
- 递归查询子评论

---

### 7. xiaoming-module-social (社交模块)
**文件数**: 5个
**职责**: 关注系统

#### 核心功能
- 关注用户
- 取消关注
- 查询关注列表
- 查询粉丝列表

#### 数据库表
- `follow` - 关注表

#### 技术亮点
- 唯一索引防止重复关注
- Redis 缓存关注列表
- 双向关注检测

---

### 8. xiaoming-module-search (搜索模块)
**文件数**: 5个
**职责**: Elasticsearch全文检索

#### 核心功能
- 标题搜索
- 内容搜索
- 标签搜索
- IK中文分词
- 全文检索 + 高亮显示

#### 技术亮点
- Elasticsearch 8.x
- IK分词器 (中文分词)
- MySQL异步同步ES (RabbitMQ)
- 高亮显示搜索结果

---

### 9. xiaoming-module-mq (消息队列模块)
**文件数**: (数量待统计)
**职责**: RabbitMQ异步消息处理

#### 消息队列使用场景

| 交换机 | 路由键 | 消费者 | 业务场景 |
|--------|--------|--------|---------|
| note.exchange | note.publish | ES索引同步 | 笔记发布后同步ES |
| note.exchange | note.publish | 粉丝通知 | 推送关注者动态 |
| note.exchange | note.publish | 热度统计 | 更新笔记热度 |
| note.exchange | note.delete | ES索引删除 | 笔记删除后清理ES |

#### 技术亮点
- 持久化消息
- Publisher Confirm
- 消费者手动ACK
- 异常重试机制

---

### 10. xiaoming-module-minio (对象存储模块)
**文件数**: (数量待统计)
**职责**: MinIO对象存储管理

#### 存储结构
```
community-platform/
├── avatar/          # 用户头像
├── note/            # 笔记图片
│   ├── cover/       # 封面图
│   └── image/       # 详情图片
└── video/           # 视频文件
```

#### 文件限制
- 图片: 最大10MB，支持 jpg/png/gif/webp
- 视频: 最大100MB，支持 mp4/avi/mov

---

### 11. xiaoming-module-home (首页推荐模块)
**文件数**: 3个
**职责**: 首页推荐流

#### 推荐策略
1. **热门推荐**: 基于热度分数的排序算法
2. **关注流**: 展示关注用户的最新笔记
3. **个性化推荐**: 基于用户标签和浏览历史 (可扩展)

#### 缓存策略
- Redis String 存储热门笔记列表
- TTL: 300秒 (5分钟)
- 主动失效策略 (笔记发布/删除时清缓存)
- 防止缓存雪崩 (随机TTL)

---

### 12. xiaoming-module-message (消息模块)
**文件数**: 5个
**职责**: 系统消息管理

#### 核心功能
- 系统消息推送
- 点赞通知
- 评论通知
- 关注通知
- 收藏通知

#### 数据库表
- `message` - 消息表

---

### 13. xiaoming-module-level (用户等级体系模块)
**文件数**: 23个
**职责**: 积分、等级、徽章系统

#### 核心功能
- 积分获取与消费
- 等级配置管理
- 自动升级检测
- 用户徽章系统
- 积分明细记录

#### 数据库表
- `level_config` - 等级配置表
- `user_point` - 用户积分表
- `point_record` - 积分明细表
- `user_badge` - 用户徽章表

#### 等级配置
| 等级 | 名称 | 经验值范围 |
|------|------|-----------|
| 1 | 新手 | 0-99 |
| 2 | 入门 | 100-299 |
| 3 | 进阶 | 300-599 |
| 4 | 熟练 | 600-999 |
| 5 | 精通 | 1000-1999 |
| 6 | 专家 | 2000-4999 |
| 7 | 大师 | 5000-9999 |
| 8 | 宗师 | 10000-19999 |
| 9 | 王者 | 20000-49999 |
| 10 | 传奇 | 50000+ |

#### 积分规则
- 发布笔记: +10积分
- 发布评论: +2积分
- 点赞: +1积分
- 收藏: +2积分
- 关注: +5积分
- 被关注: +10积分

---

### 14. xiaoming-module-category (笔记分类管理模块)
**文件数**: 8个
**职责**: 笔记分类管理

#### 核心功能
- 分类CRUD管理
- 分类启用/禁用
- 笔记与分类关联
- 分类排序

#### 数据库表
- `category` - 笔记分类表

#### 初始化分类
- 美食、旅行、穿搭、美妆、健身、读书、摄影、生活

---

### 15. xiaoming-module-websocket (WebSocket实时推送模块)
**文件数**: 3个
**职责**: WebSocket实时消息推送

#### 核心功能
- WebSocket连接管理
- 在线用户管理
- 实时消息推送
- 消息确认机制

#### 技术亮点
- Spring WebSocket
- STOMP协议
- 心跳检测
- 连接池管理

---

### 16. xiaoming-web (Web启动模块)
**职责**: 项目入口，整合所有模块

#### 核心组件
- `CommunityApplication.java` - 启动类
- `config/` - 配置类 (Redis、RabbitMQ、ES、MinIO等)
- `interceptor/` - 拦截器 (JWT认证、权限校验)
- `filter/` - 过滤器 (跨域、编码等)

---

## 🔄 技术架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端层                                  │
│    移动端 App / H5 / 小程序 / Swagger文档                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP/HTTPS + JWT认证
┌───────────────────────────▼─────────────────────────────────────┐
│                      Spring Boot 2.7.18                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Controller  →  Service  →  Mapper (MyBatis-Plus)        │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────┬─────────────────────────────────────────────────┬─────────┘
      │                                                  │
┌─────▼─────────┐   ┌─────────────┐   ┌────────────────▼───────┐
│    Redis      │   │  RabbitMQ   │   │   MySQL 8.0 (主数据库)   │
│   (缓存)      │   │  (消息队列)  │   │   16张业务表              │
└──────────────┘   └─────────────┘   └─────────────────────────┘
                                    ┌───────────────────────────┐
                                    │  Elasticsearch 8.11        │
                                    │   全文检索+IK中文分词       │
                                    └───────────────────────────┘
                                    ┌───────────────────────────┐
                                    │   MinIO (对象存储)          │
                                    │   图片/视频存储             │
                                    └───────────────────────────┘
```

---

## 📊 数据流转

### 笔记发布流程
```
用户发布笔记
    ↓
Controller 接收请求
    ↓
Service 业务处理
    ↓
图片/视频上传 → MinIO
    ↓
保存笔记到 MySQL
    ↓
发送MQ消息 (note.publish)
    ↓
├─ 消费者1: 同步ES索引
├─ 消费者2: 推送粉丝动态
└─ 消费者3: 更新热度统计
```

### 点赞流程
```
用户点赞
    ↓
Controller 接收请求
    ↓
Service 处理
    ↓
写入点赞表 (唯一索引)
    ↓
更新 Redis 计数器
    ↓
更新 MySQL 点赞数
    ↓
发送MQ消息 (通知作者)
```

---

## 🎯 代码规范

### 分层架构
```
Controller (接口层)
    ↓ 参数校验、权限校验
Service (业务层)
    ↓ 业务逻辑处理
Mapper (数据层)
    ↓ 数据库操作
```

### 命名规范
- Controller: `*Controller.java`
- Service接口: `*Service.java`
- Service实现: `*ServiceImpl.java`
- Mapper接口: `*Mapper.java`
- 实体类: `*.java` (entity包)

### 统一返回
```java
// 成功
return Result.success(data);

// 失败
return Result.fail(ResultCode.PARAM_ERROR, "参数错误");

// 异常
throw new BusinessException(ResultCode.USER_NOT_FOUND);
```

---

## 🔍 调试技巧

### 1. 查看Redis缓存
```bash
redis-cli
> KEYS *
> GET user:1
> TTL hot:notes
```

### 2. 查看RabbitMQ消息
访问 RabbitMQ Management: http://localhost:15672
- 用户名: guest
- 密码: guest

### 3. 查看ES索引
```bash
curl http://localhost:9200/_cat/indices
```

### 4. 查看MinIO文件
访问 MinIO Console: http://localhost:9001

### 5. 查看接口文档
访问 Knife4j: http://localhost:8080/doc.html

---

**文档更新日期**: 2026-08-07
**文档版本**: v1.0

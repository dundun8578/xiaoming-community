# 小明社区平台 (Xiaoming Community)

> 一个类似小红书的内容社区分享平台，Java后端秋招简历项目

![Java](https://img.shields.io/badge/Java-8+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.18-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Redis](https://img.shields.io/badge/Redis-6.2-red.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.9-yellow.svg)
![Elasticsearch](https://img.shields.io/badge/ES-8.11-purple.svg)
![MinIO](https://img.shields.io/badge/MinIO-latest-cyan.svg)

## 📖 项目简介

本项目是一个完整的企业级内容社区分享平台，支持用户注册登录、发布图文/视频笔记、点赞收藏评论、关注系统、全文搜索、推荐流等核心功能。

**项目定位**: Java后端开发秋招简历项目
**目标**: 展示企业级项目开发能力和架构设计能力

## 🏗️ 技术栈

### 核心技术
- **Java 8+** - 开发语言
- **Spring Boot 2.7.18** - 应用框架
- **MyBatis-Plus 3.5.3** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Redis 6.2** - 缓存+分布式锁
- **RabbitMQ 3.9** - 消息队列
- **Elasticsearch 8.11** - 全文搜索引擎
- **MinIO** - 对象存储
- **JWT 0.12.3** - Token认证

### 辅助技术
- **Lombok** - 简化代码
- **Hutool 5.8.22** - 工具类库
- **Knife4j 3.0.3** - 接口文档
- **Docker** - 容器化部署

## 🎯 核心功能

### 1. 用户认证模块
- ✅ 用户注册/登录
- ✅ JWT双Token机制 (AccessToken 7天 + RefreshToken 30天)
- ✅ Redis分布式Session
- ✅ 主动退出登录 / 踢用户下线

### 2. 笔记发布模块
- ✅ 发布图文/视频笔记
- ✅ 图片压缩和缩略图生成
- ✅ 笔记列表分页查询
- ✅ 热门笔记推荐

### 3. 互动模块
- ✅ 点赞 / 取消点赞
- ✅ 收藏 / 取消收藏
- ✅ 发布评论 / 回复评论
- ✅ 评论分页查询

### 4. 关注系统
- ✅ 关注 / 取消关注
- ✅ 关注列表 / 粉丝列表
- ✅ RabbitMQ异步推送关注动态

### 5. 首页推荐
- ✅ 热门笔记推荐
- ✅ Redis缓存热门笔记列表 (TTL 300s)
- ✅ 缓存主动失效策略

### 6. 全文搜索
- ✅ Elasticsearch全文检索
- ✅ IK中文分词器
- ✅ 标题/内容/标签搜索

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         客户端层                                  │
│    移动端 App / H5 / 小程序 / Swagger文档                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP/HTTPS + JWT认证
┌───────────────────────────▼─────────────────────────────────────┐
│                      Spring Boot 3.x                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Controller  →  Service  →  Mapper (MyBatis-Plus)        │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────┬─────────────────────────────────────────────────┬─────────┘
      │                                                  │
┌─────▼─────────┐   ┌─────────────┐   ┌────────────────▼───────┐
│    Redis      │   │  RabbitMQ   │   │   MySQL 8.0 (主数据库)   │
│   (缓存)      │   │  (消息队列)  │   │   12张业务表              │
└──────────────┘   └─────────────┘   └─────────────────────────┘
                                    ┌───────────────────────────┐
                                    │  Elasticsearch (ES)        │
                                    │   全文检索+中文分词         │
                                    └───────────────────────────┘
                                    ┌───────────────────────────┐
                                    │   MinIO (对象存储)          │
                                    │   图片/视频存储             │
                                    └───────────────────────────┘
```

## 📁 项目结构

```
xiaoming-community/
├── xiaoming-common/              # 公共模块
│   ├── constant/                 # 常量定义
│   ├── exception/                # 全局异常
│   ├── result/                   # 统一返回封装
│   ├── page/                     # 分页封装
│   └── utils/                    # 工具类
├── xiaoming-security/            # 安全模块-JWT认证
├── xiaoming-module-user/         # 用户模块
├── xiaoming-module-note/         # 笔记模块
├── xiaoming-module-interaction/  # 互动模块-点赞收藏评论
├── xiaoming-module-social/       # 社交模块-关注
├── xiaoming-module-search/       # 搜索模块-ES全文检索
├── xiaoming-module-mq/           # 消息队列模块-RabbitMQ
├── xiaoming-module-minio/        # 对象存储模块-MinIO
├── xiaoming-module-home/         # 首页推荐模块
├── xiaoming-module-message/      # 消息模块
├── xiaoming-web/                 # Web启动模块
│   ├── config/                   # 配置类
│   ├── interceptor/              # 拦截器
│   ├── filter/                   # 过滤器
│   └── CommunityApplication.java # 启动类
├── sql/                          # 数据库脚本
├── docker-compose.yml            # Docker Compose配置
└── Dockerfile                    # Docker镜像配置
```

## 🚀 快速开始

### 环境要求
- JDK 8+
- Maven 3.8+
- MySQL 8.0
- Redis 6.2
- RabbitMQ 3.9
- Elasticsearch 8.11
- MinIO

### 本地开发

1. **克隆项目**
```bash
git clone https://github.com/你的用户名/xiaoming-community.git
cd xiaoming-community
```

2. **创建数据库**
```bash
mysql -u root -p < sql/community_platform.sql
```

3. **修改配置文件**
```yaml
# xiaoming-web/src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql:<SECRET_77d78546>password: root
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
  elasticsearch:
    rest:
      uris: http://localhost:9200
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
```

4. **启动项目**
```bash
mvn clean install
cd xiaoming-web
mvn spring-boot:run
```

5. **访问接口文档**
```
http://localhost:8080/doc.html
```

### Docker部署

```bash
# 使用Docker Compose一键启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

## 📊 数据库设计

### 数据表清单

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| note | 笔记表 |
| note_image | 笔记图片表 |
| note_video | 笔记视频表 |
| comment | 评论表 |
| likes | 点赞表 |
| collect | 收藏表 |
| follow | 关注表 |
| message | 消息表 |
| hot_note | 热度统计表 |
| tag | 标签表 |
| note_tag | 笔记标签关联表 |

详细SQL见 `sql/community_platform.sql`

## 🔥 核心技术实现

### Redis缓存优化
- 用户信息缓存 (Redis Hash)
- 热门笔记缓存 (Redis String, TTL 300s)
- 主动失效策略
- 性能提升: 200ms → 40ms

### JWT认证
- 双Token机制
- Redis分布式Session
- 无感刷新
- 踢用户下线

### RabbitMQ异步消息
- 异步更新ES索引
- 异步推送粉丝动态
- 异步统计笔记热度
- 消息确认机制

### Elasticsearch搜索
- IK中文分词
- 全文检索
- 高亮显示
- MySQL异步同步ES

## 📝 API文档

启动项目后访问:
```
http://localhost:8080/doc.html
```

## 🧪 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| user1 | 123456 | 普通用户 |
| user2 | 123456 | 普通用户 |

## 📈 性能指标

| 指标 | 目标值 |
|------|--------|
| 接口响应时间 | < 50ms |
| 缓存命中率 | > 90% |
| 并发支持 | 1000 QPS |
| 数据库连接池 | 20-50 |

## 🎓 面试亮点

1. **完整技术栈**: Spring Boot + MySQL + Redis + MQ + ES + MinIO
2. **缓存优化**: 实测性能从200ms提升到40ms (5倍提升)
3. **分布式方案**: Redis分布式Session、JWT认证、MQ异步解耦
4. **搜索引擎**: ES全文检索 + IK分词器
5. **微服务预备**: 模块化设计，易于拆分为微服务
6. **容器化部署**: Docker Compose一键部署
7. **代码规范**: 分层架构、统一异常、接口文档完善

## 📄 开源协议

MIT License

## 👨‍💻 作者

小明

## 🙏 致谢

- Spring Boot团队
- MyBatis-Plus团队
- Elastic团队
- 所有开源项目贡献者

---

**💡 本项目用于Java后端秋招面试，展示企业级项目开发能力。**

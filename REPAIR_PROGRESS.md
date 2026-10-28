# 修复进度与本地运行

更新日期：2026-10-28。

本轮按项目经历中的图文社区主链路修复：Vue3 / Pinia、Spring Boot、Redis、Elasticsearch、RabbitMQ、MinIO，以及可配置的内容审核接口。

## 当前状态

- 五个基础服务已启动：MySQL、Redis、RabbitMQ、Elasticsearch（含 IK 8.11.0）、MinIO。
- 后端完整构建并启动，前端生产构建通过；本地开发前端也已启动。
- 前端地址：http://localhost:5173
- 接口文档：http://localhost:8080/api/doc.html
- 可直接在前端注册账号后发布笔记；本轮没有修改已有演示账号密码。

## 一键运行

在项目根目录的 PowerShell 中执行：

    .\scripts\start-local.ps1

如当前终端限制脚本执行，可仅对本次启动使用：

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\start-local.ps1

脚本会查找已有 JDK 17，检查并启动基础服务、执行非破坏性新增表迁移、停止本脚本管理的旧后端、完整构建后启动前后端。可使用 -JavaHome 指定其他已安装的 JDK 17，或在仅重启时使用 -SkipBuild。-SkipInfrastructure 用于基础服务已就绪时跳过容器检查和迁移。

本机使用已有 JDK：C:\Users\25489\.jdks\jbr-17.0.14。没有重新下载 JDK，也没有修改系统 Java 配置。
Maven 缓存原本已在 D:\Dev\Maven\repository。
Docker 数据目录已确认在 D:\ACLOS\DockerDesktopWSL。
项目数据位于 docker/，日志位于 logs/，临时文件和脚本使用的 npm 缓存位于 .runtime/，均在 D 盘本项目内。

本机原有 MySQL 占用 3306，本项目容器改用 127.0.0.1:13306；没有停止原有 MySQL。容器内部仍使用 3306。
初始化 SQL 只由全新的 MySQL 数据目录自动执行；不要手动重复执行 community_platform.sql，其中包含旧版的 DROP TABLE。scripts/start-local.ps1 重复执行的是 sql/migrations/ 下的非破坏性迁移。

## 已完成的步骤

### 1. 构建与启动

- 补齐前端 package.json、依赖锁文件、HTML 入口、Vite Vue 插件及 /api 代理。
- 后端生成包含依赖和启动器的可执行 JAR。
- 启动脚本改用 clean package，避免增量打包沿用旧的业务模块 JAR。
- Docker 构建与运行统一为 Java 17，修正构建上下文及遗漏模块的问题。
- 修正 Spring Boot 2.7 的 Redis 配置前缀、数据库凭证和容器环境 JWT 配置。
- 补齐全局数据库字段填充器，解决注册等写入操作把 create_time 显式写成 NULL 的问题。
- 新增 /api/health 存活检查。

### 2. 发布、评论、互动与关注

- 修正上传接口返回 URL 字符串而页面读取 originalUrl 的错误。
- MinIO 自动创建桶；上传返回可通过后端读取的图片 URL，图片读取无需将桶设为公开。
- 补齐文件为空、大小、类型及文件路径校验；单独删除文件暂不开放，避免破坏其他笔记引用。
- 发布标题、正文和类型增加参数校验。
- 评论模块纳入构建，新增列表、发布、删除接口，返回评论人信息。
- 评论数量在事务中更新；删除校验作者，修改计数后同步搜索。
- 详情返回真实点赞、收藏状态，不再固定为 false。
- 使用数据库行锁与原子更新，避免并发点赞、收藏或浏览造成计数丢失。
- 详情页接上关注作者入口。
- 补齐通知分页、已读操作及收件人归属校验。

### 3. 登录状态与刷新

- 前端使用 Pinia 保存并同步登录状态，退出时调用后端并清理本地会话。
- 区分 accessToken 和 refreshToken，刷新 Token 无法直接用于业务接口认证。
- 刷新接口允许在 accessToken 过期时调用，并校验 Redis 登录态和用户状态。
- Axios 对并发 401 合并刷新请求，每个失败请求最多重试一次。
- accessToken 为 30 分钟，退出后 refreshToken 立即失效；已签发的 accessToken 采用无状态校验，到期前仍可能有效。

### 4. 搜索与消息同步

- 使用 ES 8 原生 Java 客户端，统一索引创建、写入、删除与搜索。
- Elasticsearch 镜像安装官方 IK 8.11.0 插件，启动前校验插件 SHA-256。
- 支持标题、正文、标签检索及多词组合查询，过滤非公开状态。
- 搜索服务异常会明确返回错误，不再伪装成“没有结果”。
- 发布、修改、删除以及互动数据变更后更新索引。
- 新增 note_outbox 事务外发表，业务写入和待发送事件在同一数据库事务中提交。
- 后台发送等待 RabbitMQ 确认；失败保留并延后重试，避免代理短暂中断时永久漏同步。
- 消费成功后自动确认，失败有限重试并进入 note.dead.queue，方便排查。
- 消费者读取并锁定最新笔记状态，避免乱序旧消息恢复已删除内容；关注动态避免重复生成。
- 为已有演示笔记补齐索引任务，重复启动不会重复初始化数据库。
- 消息系统采用至少一次投递；最终失败的死信仍需排查和人工重放，本轮未制作死信管理后台。

### 5. 缓存与热度推荐

- 热门列表恢复真实分页；第一页使用 Redis String 缓存，TTL 为 300 秒并加入抖动。
- 缓存只保存公共字段，详情中的用户互动状态单独查询。
- 事务提交后主动失效热榜缓存，不再使用 KEYS 全库扫描。
- 定时按浏览、点赞、收藏、评论加权，并叠加时间衰减，写入数据库热度分和 Redis ZSet。
- 首页从 ZSet 取排序结果；Redis 不可用时回退到数据库。
- 当前属于聚合热度推荐，没有实现个人兴趣画像或向量推荐。
- 用户资料 Hash 缓存仍沿用已有实现；关注人数等扩展统计不是本轮重点，未作完整联调。

### 6. 可配置内容审核

按本次选择，没有配置真实大模型账号，也没有调用付费模型。

- 默认关闭，审核记录标记 SKIPPED，页面明确显示未启用。
- 可配置提供 chat/completions JSON 协议的审核服务。
- 结构化提示要求返回 APPROVE / REJECT / REVIEW 与理由，严格解析响应。
- 启用后：通过为公开状态；拒绝和待复核内容不向其他用户开放；服务失败、响应损坏或未知决策进入待复核。
- 新增 note_moderation 审核记录，作者能看到自己的审核状态和理由。
- 暂未提供管理员人工复核后台；待复核记录可保留，真实审核质量需接入模型后另行评估。

如需启用，将 .env.example 复制为 .env.local 并设置：

    AI_MODERATION_ENABLED=true
    AI_MODERATION_URL=https://your-service.example/v1/chat/completions
    AI_MODERATION_API_KEY=本地配置的密钥
    AI_MODERATION_MODEL=服务提供的模型名

密钥不要提交到 Git。.env.local 已被忽略。当前没有真实模型配置。

## 验证结果

所有测试均针对完整重建后的可执行 JAR，不只针对源码编译结果。

- 后端：18 个 Maven 工程执行 clean package 成功。
- 前端：npm run build 成功，依赖锁文件可安装；主包体积仍有优化空间。
- scripts/smoke-test.mjs：17 项真实接口检查通过，覆盖注册登录、Token 用途隔离、发布校验、MinIO 上传读取、评论分页与权限、点赞收藏用户隔离、并发浏览与点赞、笔记归属、IK 搜索和发布/修改/删除同步、退出后的刷新失效。
- scripts/moderation-test.mjs：6 种审核响应契约通过；使用本地模拟服务，覆盖通过、拒绝、待复核、损坏响应、503 和未知决策。
- scripts/mq-recovery-test.mjs：实际停止并恢复本项目 RabbitMQ 后，发布及外发表保存、自动补发和搜索通知、重复投递不重复通知均通过；同时验证通知已读权限。
- scripts/RankingContract.java：通过最终 JAR 验证收藏与点赞权重、时间衰减和未来时间边界。
- 一键脚本已验证基础服务检查、迁移、完整重建和重启。
- 测试生成自己的联调账号和笔记，不修改原有演示内容；联调笔记在测试后逻辑删除，测试账号、审核记录和少量上传图片保留用于排查。
- 结果文件在 logs/smoke-result.json、logs/moderation-result.json、logs/mq-recovery-result.json 和 logs/ranking-test.log。
- 已验证 HTTP 接口和前端构建，尚未逐项进行真实浏览器点击测试，也未覆盖等级、积分和统计等外围模块。

## 与项目经历的对应边界

现在已有可运行代码支撑 Vue3 + Pinia、Redis 缓存、IK 检索、MQ 异步同步、可配置审核与热度推荐。
截图中的首页 1.1s、接口 40ms、搜索 <100ms、P99 降低 60% 等指标尚未做可复现的基准压测，不能把本次功能回归等同于这些性能结论。

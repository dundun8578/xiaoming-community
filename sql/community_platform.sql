-- ============================================================================
--  小明社区平台 —— 数据库建表脚本
--  数据库：community_platform      字符集：utf8mb4      引擎：InnoDB
--
--  执行方式一（本地 MySQL）：
--      mysql -u root -p < sql/community_platform.sql
--  执行方式二（Docker）：
--      docker-compose up -d        # 本文件已挂载到 /docker-entrypoint-initdb.d/，首次启动自动执行
--
--  ⚠️ 注意：脚本会对下列表执行 DROP TABLE IF EXISTS，重复执行会清空已有数据，请勿在生产库直接运行。
--
--  表清单（与 com.xiaoming.**.entity 中的实体类一一对应，共 15 张）：
--     1. user          用户表
--     2. note          笔记表
--     3. note_image    笔记图片表
--     4. note_video    笔记视频表
--     5. comment       评论表
--     6. likes         点赞表
--     7. collect       收藏表
--     8. follow         关注表
--     9. message       消息通知表
--    10. category      笔记分类表
--    11. level_config  等级配置表
--    12. user_point    用户积分表
--    13. point_record  积分明细表
--    14. user_badge    用户徽章表
--    15. statistics    统计数据表
--
--  说明：旧文档里提到的 tag / note_tag / hot_note 三张表并未实现——
--        标签目前以逗号分隔字符串存在 note.tags，热度分数存在 note.hot_score，故不再单独建表。
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `community_platform`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `community_platform`;


-- ---------------------------------------------------------------------------
-- 1. user  用户表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名（登录账号）',
    `password`    VARCHAR(100) NOT NULL                COMMENT '密码（BCrypt 加密后的密文）',
    `nickname`    VARCHAR(50)      DEFAULT NULL        COMMENT '昵称',
    `avatar`      VARCHAR(500)     DEFAULT NULL        COMMENT '头像URL',
    `email`       VARCHAR(100)     DEFAULT NULL        COMMENT '邮箱',
    `phone`       VARCHAR(20)      DEFAULT NULL        COMMENT '手机号',
    `description` VARCHAR(500)     DEFAULT NULL        COMMENT '个人简介',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1正常 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';


-- ---------------------------------------------------------------------------
-- 2. note  笔记表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '作者ID',
    `title`         VARCHAR(200) NOT NULL                COMMENT '标题',
    `content`       LONGTEXT         DEFAULT NULL        COMMENT '正文内容',
    `cover_image`   VARCHAR(500)     DEFAULT NULL        COMMENT '封面图URL',
    `type`          TINYINT      NOT NULL DEFAULT 1      COMMENT '类型：1图文 2视频',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1已发布 2已下架',
    `like_count`    INT          NOT NULL DEFAULT 0      COMMENT '点赞数',
    `collect_count` INT          NOT NULL DEFAULT 0      COMMENT '收藏数',
    `comment_count` INT          NOT NULL DEFAULT 0      COMMENT '评论数',
    `view_count`    INT          NOT NULL DEFAULT 0      COMMENT '浏览数',
    `hot_score`     BIGINT       NOT NULL DEFAULT 0      COMMENT '热度分（浏览*1+点赞*5+收藏*10+评论*3）',
    `tags`          VARCHAR(500)     DEFAULT NULL        COMMENT '标签（逗号分隔）',
    `publish_time`  DATETIME         DEFAULT NULL        COMMENT '发布时间',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_publish` (`user_id`, `publish_time`),
    KEY `idx_status_hot` (`status`, `hot_score` DESC),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记表';


-- ---------------------------------------------------------------------------
-- 3. note_image  笔记图片表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `note_image`;
CREATE TABLE `note_image` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `note_id`     BIGINT       NOT NULL                COMMENT '笔记ID',
    `image_url`   VARCHAR(500) NOT NULL                COMMENT '图片URL',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序（从0递增）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记图片表';


-- ---------------------------------------------------------------------------
-- 4. note_video  笔记视频表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `note_video`;
CREATE TABLE `note_video` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `note_id`     BIGINT       NOT NULL                COMMENT '笔记ID',
    `video_url`   VARCHAR(500) NOT NULL                COMMENT '视频URL',
    `cover_url`   VARCHAR(500)     DEFAULT NULL        COMMENT '视频封面URL',
    `duration`    INT              DEFAULT NULL        COMMENT '视频时长（秒）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记视频表';


-- ---------------------------------------------------------------------------
-- 5. comment  评论表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `note_id`       BIGINT       NOT NULL                COMMENT '所属笔记ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '评论人ID',
    `reply_user_id` BIGINT           DEFAULT NULL        COMMENT '被回复人ID（二级回复用）',
    `parent_id`     BIGINT           DEFAULT NULL        COMMENT '父评论ID（0或NULL表示一级评论）',
    `content`       VARCHAR(1000) NOT NULL               COMMENT '评论内容',
    `like_count`    INT          NOT NULL DEFAULT 0      COMMENT '点赞数',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_note_id` (`note_id`, `create_time`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表';


-- ---------------------------------------------------------------------------
-- 6. likes  点赞表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '点赞用户ID',
    `note_id`     BIGINT   NOT NULL                COMMENT '被点赞笔记ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_note` (`user_id`, `note_id`),
    KEY `idx_note_id` (`note_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞表';


-- ---------------------------------------------------------------------------
-- 7. collect  收藏表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '收藏用户ID',
    `note_id`     BIGINT   NOT NULL                COMMENT '被收藏笔记ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_note` (`user_id`, `note_id`),
    KEY `idx_note_id` (`note_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表';


-- ---------------------------------------------------------------------------
-- 8. follow  关注表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT   NOT NULL                COMMENT '关注人ID（粉丝）',
    `follow_user_id` BIGINT   NOT NULL                COMMENT '被关注人ID（博主）',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
    KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注表';


-- ---------------------------------------------------------------------------
-- 9. message  消息通知表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `from_user_id` BIGINT           DEFAULT NULL        COMMENT '发送者ID（系统消息为空）',
    `to_user_id`   BIGINT       NOT NULL                COMMENT '接收者ID',
    `type`         TINYINT      NOT NULL DEFAULT 1      COMMENT '类型：1点赞 2评论 3关注 4关注动态 5系统',
    `content`      VARCHAR(500)     DEFAULT NULL        COMMENT '消息内容',
    `target_id`    BIGINT           DEFAULT NULL        COMMENT '关联目标ID（笔记/评论ID）',
    `is_read`      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已读：0未读 1已读',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_to_user` (`to_user_id`, `is_read`, `create_time`),
    KEY `idx_from_user` (`from_user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息通知表';


-- ---------------------------------------------------------------------------
-- 10. category  笔记分类表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL                COMMENT '分类名称',
    `icon`        VARCHAR(200)     DEFAULT NULL        COMMENT '分类图标',
    `description` VARCHAR(500)     DEFAULT NULL        COMMENT '分类描述',
    `note_count`  INT          NOT NULL DEFAULT 0      COMMENT '该分类下笔记数',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序值（越小越靠前）',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记分类表';


-- ---------------------------------------------------------------------------
-- 11. level_config  等级配置表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `level_config`;
CREATE TABLE `level_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level`       INT          NOT NULL                COMMENT '等级（1 起）',
    `name`        VARCHAR(50)  NOT NULL                COMMENT '等级名称',
    `min_exp`     INT          NOT NULL DEFAULT 0      COMMENT '本级最低经验值',
    `max_exp`     INT          NOT NULL DEFAULT 0      COMMENT '本级最高经验值',
    `icon`        VARCHAR(200)     DEFAULT NULL        COMMENT '等级图标',
    `badge`       VARCHAR(200)     DEFAULT NULL        COMMENT '徽章名称',
    `privilege`   VARCHAR(1000)    DEFAULT NULL        COMMENT '特权信息（JSON 字符串）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level` (`level`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '等级配置表';


-- ---------------------------------------------------------------------------
-- 12. user_point  用户积分表（一个用户一行）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `user_point`;
CREATE TABLE `user_point` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT   NOT NULL                COMMENT '用户ID',
    `total_exp`       INT      NOT NULL DEFAULT 0      COMMENT '累计经验值',
    `current_level`   INT      NOT NULL DEFAULT 1      COMMENT '当前等级',
    `total_point`     INT      NOT NULL DEFAULT 0      COMMENT '累计获得积分',
    `available_point` INT      NOT NULL DEFAULT 0      COMMENT '可用积分',
    `used_point`      INT      NOT NULL DEFAULT 0      COMMENT '已消费积分',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户积分表';


-- ---------------------------------------------------------------------------
-- 13. point_record  积分明细表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `point_record`;
CREATE TABLE `point_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `type`        TINYINT      NOT NULL DEFAULT 1      COMMENT '类型：1获取 2消费',
    `point`       INT          NOT NULL                COMMENT '积分变动值（正数）',
    `action`      VARCHAR(50)      DEFAULT NULL        COMMENT '行为标识，如 publish_note / like_received',
    `action_id`   BIGINT           DEFAULT NULL        COMMENT '关联业务ID（笔记/评论ID）',
    `description` VARCHAR(200)     DEFAULT NULL        COMMENT '描述',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分明细表';


-- ---------------------------------------------------------------------------
-- 14. user_badge  用户徽章表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `user_badge`;
CREATE TABLE `user_badge` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `badge_id`    BIGINT           DEFAULT NULL        COMMENT '徽章ID',
    `badge_name`  VARCHAR(50)      DEFAULT NULL        COMMENT '徽章名称',
    `badge_icon`  VARCHAR(200)     DEFAULT NULL        COMMENT '徽章图标',
    `obtain_time` DATETIME         DEFAULT NULL        COMMENT '获得时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户徽章表';


-- ---------------------------------------------------------------------------
-- 15. statistics  统计数据表
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `statistics`;
CREATE TABLE `statistics` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `stat_date`    VARCHAR(20) NOT NULL                COMMENT '统计日期，格式 yyyy-MM-dd',
    `stat_type`    INT         NOT NULL                COMMENT '统计类型：1平台概览 2用户趋势 3笔记 4互动',
    `dimension_id` BIGINT          DEFAULT NULL        COMMENT '统计维度ID（如某个笔记/用户）',
    `items`        TEXT            DEFAULT NULL        COMMENT '统计明细（JSON 字符串）',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_date_type` (`stat_date`, `stat_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '统计数据表';

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================================
--  初始化数据
--  测试账号密码统一为 123456（BCrypt 密文，对应 Spring Security BCryptPasswordEncoder）
--      admin / 123456    管理员
--      user1 / 123456    普通用户
--      user2 / 123456    普通用户
-- ============================================================================

INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `description`, `status`) VALUES
(1, 'admin', '$2a$10$.yh1L0tNYIK8sugpx3sfZ.8nqy.khgHERLEjVGZZACVtWUr2/vfFq', '管理员小明', NULL, 'admin@xiaoming.com', '平台管理员', 1),
(2, 'user1', '$2a$10$.yh1L0tNYIK8sugpx3sfZ.8nqy.khgHERLEjVGZZACVtWUr2/vfFq', '爱分享的小明', NULL, 'user1@xiaoming.com', '热爱记录生活', 1),
(3, 'user2', '$2a$10$.yh1L0tNYIK8sugpx3sfZ.8nqy.khgHERLEjVGZZACVtWUr2/vfFq', '旅行的小红', NULL, 'user2@xiaoming.com', '一个爱旅行的人', 1);

-- 等级配置：1~6 级（经验值区间可按需调整）
INSERT INTO `level_config` (`level`, `name`, `min_exp`, `max_exp`, `icon`, `badge`, `privilege`) VALUES
(1, '新手上路', 0,     199,    'lv1.png', '萌新',   '{"publishLimit":5,"commentLimit":20}'),
(2, '初露锋芒', 200,   999,    'lv2.png', '进阶',   '{"publishLimit":10,"commentLimit":50}'),
(3, '小有名气', 1000,  2999,   'lv3.png', '达人',   '{"publishLimit":20,"commentLimit":100}'),
(4, '名扬四海', 3000,  6999,   'lv4.png', '大咖',   '{"publishLimit":50,"commentLimit":200}'),
(5, '声名远扬', 7000,  14999,  'lv5.png', '宗师',   '{"publishLimit":100,"commentLimit":500}'),
(6, '一代宗师', 15000, 999999, 'lv6.png', '传说',   '{"publishLimit":999,"commentLimit":999}');

-- 笔记分类
INSERT INTO `category` (`id`, `name`, `icon`, `description`, `sort`, `status`) VALUES
(1, '美食', 'food.png',      '美食探店与家常菜谱',   1, 1),
(2, '旅行', 'travel.png',    '旅行攻略与风景记录',   2, 1),
(3, '穿搭', 'outfit.png',    '日常穿搭与好物推荐',   3, 1),
(4, '数码', 'digital.png',   '数码产品测评与使用心得', 4, 1),
(5, '健身', 'fitness.png',   '健身计划与饮食搭配',   5, 1),
(6, '读书', 'reading.png',   '好书推荐与读书笔记',   6, 1);

-- 用户积分（与上面的用户对应）
INSERT INTO `user_point` (`user_id`, `total_exp`, `current_level`, `total_point`, `available_point`, `used_point`) VALUES
(1, 1200, 3, 1200, 1200, 0),
(2, 350,  2, 350,  350,  0),
(3, 80,   1, 80,   80,   0);

-- 示例笔记（让热门/推荐接口一启动就有数据）
INSERT INTO `note` (`id`, `user_id`, `title`, `content`, `cover_image`, `type`, `status`,
                    `like_count`, `collect_count`, `comment_count`, `view_count`, `hot_score`,
                    `tags`, `publish_time`)
VALUES
(1, 2, '周末在家做了一顿超满足的早餐',
 '今天试了牛油果吐司配水波蛋，做法比想象中简单。牛油果压成泥加一点黑胡椒和柠檬汁，水波蛋水开后转小火煮 3 分钟就好，最后撒点海苔碎，颜值和味道都在线。',
 NULL, 1, 1, 12, 5, 3, 156, 12*5 + 5*10 + 3*3 + 156, '早餐,美食,家常菜', NOW()),
(2, 3, '云南六天五晚自由行攻略',
 '昆明 → 大理 → 丽江的经典路线。建议大理住古城附近，租电动车环洱海性价比最高；丽江古城晚上人多，想拍照建议早上七八点去。全程人均 3000 左右。',
 NULL, 1, 1, 28, 15, 7, 420, 28*5 + 15*10 + 7*3 + 420, '旅行,攻略,云南', NOW()),
(3, 1, '开发笔记：Redis 缓存热门列表的几个坑',
 '用 String 存热门笔记列表时踩了两个坑：一是没设 TTL 导致数据长期不更新，二是发布新笔记后忘记主动删缓存。最后的方案是写操作后 delete key，读操作回源并设置 300 秒过期。',
 NULL, 1, 1, 35, 20, 9, 512, 35*5 + 20*10 + 9*3 + 512, '技术,Redis,缓存', NOW());

INSERT INTO `note_image` (`note_id`, `image_url`, `sort`) VALUES
(1, '/images/demo/breakfast-1.jpg', 0),
(1, '/images/demo/breakfast-2.jpg', 1),
(2, '/images/demo/yunnan-1.jpg',    0),
(2, '/images/demo/yunnan-2.jpg',    1),
(2, '/images/demo/yunnan-3.jpg',    2),
(3, '/images/demo/redis-1.jpg',     0);

-- 关注关系：user1 和 user2 互相关注，并都关注 admin
INSERT INTO `follow` (`user_id`, `follow_user_id`) VALUES
(2, 3), (3, 2), (2, 1), (3, 1);

-- 点赞 / 收藏示例
INSERT INTO `likes` (`user_id`, `note_id`) VALUES
(1, 1), (3, 1), (1, 2), (2, 2), (2, 3), (3, 3);

INSERT INTO `collect` (`user_id`, `note_id`) VALUES
(1, 2), (2, 3);

-- 示例评论
INSERT INTO `comment` (`note_id`, `user_id`, `parent_id`, `content`) VALUES
(1, 3, NULL, '看起来好好吃，周末照着做一次！'),
(1, 1, 1,    '记得水波蛋要小火，不然容易散～'),
(2, 1, NULL, '攻略很实用，收藏了，下个月就去。');

-- 示例消息
INSERT INTO `message` (`from_user_id`, `to_user_id`, `type`, `content`, `target_id`, `is_read`) VALUES
(2, 1, 4, '你关注的用户发布了新笔记: 周末在家做了一顿超满足的早餐', 1, 0),
(3, 1, 4, '你关注的用户发布了新笔记: 云南六天五晚自由行攻略', 2, 0);


-- 自动审核记录：仅新增，不影响已有表

CREATE TABLE IF NOT EXISTS note_moderation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    decision VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_note_moderation (note_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS note_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    sent TINYINT NOT NULL DEFAULT 0,
    attempts INT NOT NULL DEFAULT 0,
    available_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_outbox_pending (sent, available_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 首次启动时补齐演示笔记的搜索索引

INSERT INTO note_outbox(note_id,event_type,sent,attempts,available_at,create_time)
SELECT n.id,'UPDATE',0,0,NOW(),NOW()
FROM note n WHERE n.is_deleted=0 AND n.status=1
AND NOT EXISTS (SELECT 1 FROM note_outbox o WHERE o.note_id=n.id);

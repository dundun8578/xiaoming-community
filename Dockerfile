# Maven构建阶段
FROM maven:3.8.6-openjdk-8 AS builder

WORKDIR /app

# 复制pom.xml并预下载依赖
COPY pom.xml .
COPY xiaoming-common/pom.xml xiaoming-common/
COPY xiaoming-security/pom.xml xiaoming-security/
COPY xiaoming-module-user/pom.xml xiaoming-module-user/
COPY xiaoming-module-note/pom.xml xiaoming-module-note/
COPY xiaoming-module-interaction/pom.xml xiaoming-module-interaction/
COPY xiaoming-module-social/pom.xml xiaoming-module-social/
COPY xiaoming-module-search/pom.xml xiaoming-module-search/
COPY xiaoming-module-mq/pom.xml xiaoming-module-mq/
COPY xiaoming-module-minio/pom.xml xiaoming-module-minio/
COPY xiaoming-module-home/pom.xml xiaoming-module-home/
COPY xiaoming-module-message/pom.xml xiaoming-module-message/
COPY xiaoming-web/pom.xml xiaoming-web/

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源码
COPY xiaoming-common/src xiaoming-common/src
COPY xiaoming-security/src xiaoming-security/src
COPY xiaoming-module-user/src xiaoming-module-user/src
COPY xiaoming-module-note/src xiaoming-module-note/src
COPY xiaoming-module-interaction/src xiaoming-module-interaction/src
COPY xiaoming-module-social/src xiaoming-module-social/src
COPY xiaoming-module-search/src xiaoming-module-search/src
COPY xiaoming-module-mq/src xiaoming-module-mq/src
COPY xiaoming-module-minio/src xiaoming-module-minio/src
COPY xiaoming-module-home/src xiaoming-module-home/src
COPY xiaoming-module-message/src xiaoming-module-message/src
COPY xiaoming-web/src xiaoming-web/src

# 构建项目（跳过测试）
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM openjdk:8-jre-alpine

WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache tzdata
ENV TZ=Asia/Shanghai

# 创建非root用户运行应用
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# 复制构建产物
COPY --from=builder /app/xiaoming-web/target/xiaoming-web-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

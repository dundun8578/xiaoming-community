FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn -B package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Shanghai
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
COPY --from=builder --chown=appuser:appgroup /app/xiaoming-web/target/xiaoming-web-1.0.0.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

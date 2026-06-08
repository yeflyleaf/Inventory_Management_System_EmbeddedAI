# ============================================================================
# 进销存管理系统 - 多阶段构建 Dockerfile (企业级完善版)
# ============================================================================
#
# 优化说明:
#   1. 兼容性: 自动处理 Windows (CRLF) 换行符，防止脚本执行错误
#   2. 安全性: 使用非 root 用户运行业务进程
#   3. 稳定性: 支持优雅停机 (Graceful Shutdown)，防止数据丢失
#   4. 性能: 极致利用 Docker 缓存层
#
# ============================================================================

# ============================================================================
# 阶段 1: backend-builder - 后端构建阶段
# ============================================================================
FROM eclipse-temurin:17-jdk-alpine AS backend-builder

WORKDIR /app

# 安装基础工具 (增加 dos2unix 用于处理 Windows 换行符)
RUN apk add --no-cache maven git dos2unix

# 1. 优先复制 Maven 包装器和配置 (利用缓存)
COPY Backend/pom.xml ./pom.xml
COPY Backend/.mvn ./.mvn
COPY Backend/mvnw ./mvnw

# 2. 修复 Windows 换行符问题 (关键步骤)
# Windows 上提交的文件可能有 CRLF，导致 Linux 无法执行 mvnw
RUN dos2unix mvnw && chmod +x mvnw

# 3. 预下载依赖 (利用缓存)
RUN ./mvnw dependency:go-offline -B

# 4. 复制源码并打包
COPY Backend/src ./src
RUN ./mvnw clean package -DskipTests -B


# ============================================================================
# 阶段 2: backend - 后端运行阶段
# ============================================================================
FROM eclipse-temurin:17-jre-alpine AS backend

# 安装运行时依赖
# fontconfig, ttf-dejavu: 修复验证码/条形码/报表可能的乱码问题
# dos2unix: 用于修复启动脚本
RUN apk add --no-cache curl tzdata fontconfig ttf-dejavu dos2unix

ENV TZ=Asia/Shanghai

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 创建目录并设置权限
RUN mkdir -p /app/uploads /app/logs && chown -R spring:spring /app

# 复制构建产物
COPY --from=backend-builder --chown=spring:spring /app/target/*.jar app.jar

# 复制并处理启动脚本
COPY --chown=spring:spring Backend/entrypoint.sh ./entrypoint.sh
# 修复脚本换行符并赋权
RUN dos2unix ./entrypoint.sh && chmod +x ./entrypoint.sh

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD curl -fs http://localhost:8080/actuator/health || exit 1

# 启动命令 (使用 exec 模式)
# exec: 替换当前 shell 进程，让 Java 成为 PID 1 进程
# 这样 Java 才能接收到 docker stop 发出的 SIGTERM 信号，实现优雅关闭
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"]


# ============================================================================
# 阶段 3: frontend-builder - 前端构建阶段
# ============================================================================
FROM node:20-alpine AS frontend-builder

WORKDIR /app

COPY Frontend/package*.json ./

# npm ci: 严格按照 lock 文件安装，确保版本一致性
RUN npm ci

COPY Frontend/ ./

RUN npm run build


# ============================================================================
# 阶段 4: frontend - Nginx 托管静态资源
# ============================================================================
FROM nginx:stable-alpine AS frontend

RUN apk add --no-cache curl tzdata

ENV TZ=Asia/Shanghai

# 清理默认文件
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/*

# 复制配置和静态资源
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=frontend-builder /app/dist/ /usr/share/nginx/html/

# 权限加固
RUN chmod -R 755 /usr/share/nginx/html && \
    find /usr/share/nginx/html -type f -exec chmod 644 {} \;

# 修复 Nginx 在某些环境下 (如 WSL2) 自动生成过多 worker 导致退出的问题
RUN rm -f /docker-entrypoint.d/30-tune-worker-processes.sh && \
    sed -i 's/worker_processes .*/worker_processes 1;/g' /etc/nginx/nginx.conf

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:80/health || exit 1

CMD ["nginx", "-g", "daemon off;"]


# ============================================================================
# 阶段 5: ai-service - Python AI 微服务
# ============================================================================
FROM python:3.11-slim AS ai-service

WORKDIR /app

# 安装必要的系统依赖 (如 curl 用于 healthcheck，构建工具用于依赖编译)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential curl \
    && rm -rf /var/lib/apt/lists/*

# 复制并安装依赖
COPY AiService/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# 复制微服务源码
COPY AiService/ ./

EXPOSE 8000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -fs http://localhost:8000/openapi.json || exit 1

# 启动服务
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]

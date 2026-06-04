# Role

你是一位精通 Java 生态、前端现代架构以及大模型（LLM）微服务集成的资深 AI 架构师。

# Task

请基于现有的仓储管理系统项目的技术堆栈，制定并执行一份将项目改造为“AI嵌入模式”的完整重构与开发计划。

# Current Project Stack Analysis

原有项目采用以下结构，所有改造必须与这些技术无缝兼容：

- Backend: Java, Spring Boot, MyBatis/MyBatis-Plus 架构，包含经典的 Controller、Service、DAO/Mapper、DTO、VO 和 Entity 划分。
- Middleware: MySQL 关系型数据库，Redis 用于缓存与工具类（RedisUtils）。
- Security: 基于自定义注解（@LoginRequired, @AdminRequired）与 AuthInterceptor 拦截器实现的 JWT 认证机制。
- Frontend: Vue 3 (Vite) + Tailwind CSS 构筑的单页面应用，包含 Electron 与 Capacitor 包装层。
- Deployment: 基于 Dockerfile、docker-compose.yml 和 Nginx 进行容器化集群部署。

# Target AI Technology Stack

为了确保技术足够流行、社区活跃且与原项目完美兼容，我们选定以下 AI 技术栈：

1. AI Orchestration (Backend): 使用 LangChain4j (langchain4j-spring-boot-starter)。这是目前 Java 生态中最受欢迎、集成度最高的 LLM 框架。
2. Vector Database: 直接复用现有的 Redis 开启 Redis Search 模块作为向量数据库，不引入新的数据库中间件。
3. Protocol: 采用原生 HTTP SSE (Server-Sent Events) 协议，实现低延迟的打字机流式 Token 传输。
4. Frontend UX: 基于 Vue 3 的 Composition API 结合 fetch 的 ReadableStream 异步渲染聊天上下文。

# Upgrade & Refactoring Specifications

请按照以下模块，逐步生成或修改增量代码，禁止破坏原有业务逻辑：

## Module 1: 后端依赖与基础设施配置 (pom.xml & properties)

1. 在 `Backend/pom.xml` 中引入 `langchain4j-spring-boot-starter` 及其对应的声明式大模型连接器（支持配置 DeepSeek 或 OpenAI 兼容的 API）。
2. 在 `application.properties` 中添加 AI 模型参数、API Base URL、API Key、Temperature 以及 Redis 向量索引名称等配置。

## Module 2: 向量数据库初始化 (Redis Vector Store)

1. 编写一个配置类 `AiVectorConfig.java`，利用现有的 `RedisConfig` 基础，初始化 LangChain4j 的 `RedisEmbeddingStore`。
2. 定义商品/库存实体的 Embedding 转换服务，当 `ProductService` 发生商品上新或修改时，自动异步触发 Embedding 计算并同步至 Redis 向量库。

## Module 3: 核心 AI 服务与工具调用 (Function Calling)

1. 编写声明式接口 `WarehouseAiAssistant.java`，使用 LangChain4j 的 `@SystemMessage` 定义系统 Prompt（扮演聪明的仓储分析师）。
2. 利用 Function Calling 机制，将原有的 `ProductService` 和 `StockService` 关键查询方法（如查询低库存、统计品类库存）通过 `@Tool` 注解暴露给 LLM，使其具备检索实时 MySQL 数据的能力。

## Module 4: 流式响应控制器 (AiController.java)

1. 新增 `com.example.backend.controller.AiController.java`。
2. 建立一个支持 JWT 认证的 SSE 接口（复用原有 `@LoginRequired` 机制），接收用户的自然语言指令（如“帮我查一下哪些商品快断货了，并生成一份补货建议”）。
3. 使用 LangChain4j 的 `TokenStream` 捕获大模型输出，并通过 `SseEmitter` 实时推送至前端。

## Module 5: 容器化组件升级 (Docker)

1. 修改根目录下的 `docker-compose.yml`，将标准的 `redis:latest` 镜像替换为支持向量检索的 `redis/redis-stack-server:latest`，确保原有缓存逻辑与新向量搜索功能同时平稳运行。

## Module 6: 前端 AI 助手组件开发 (Vue 3)

1. 在 `Frontend/src/views/` 或 `components/` 下新建一个 `AiAssistant.vue` 对话框组件。
2. 使用 Vue 3 Composition API 管理对话状态（messages 数组、loading 状态）。
3. 使用 `fetch` 请求后端的 SSE 接口，通过 `reader.read()` 异步循环解析数据流，实时更新打字机效果。

# Output Constraints

- 所有的代码变更必须是增量的，严禁破坏项目原有的拦截器、全局异常处理器和统一返回体格式（Result）。
- 保持原有的 Java 17 语法兼容性及 Vue 3 规范。
- 每次输出代码时，请先给出清晰的文件路径说明，再输出完整的代码块。

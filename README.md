<div align="center">

# 📦 Inventory Management System (进销存管理系统)

**基于 Spring Boot 3 与 Vue 3 构建的现代化、高性能进销存解决方案**

[![Vue](https://img.shields.io/badge/Vue-3-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

</div>

---

## 📖 项目简介

**Inventory Management System** 是一套专为中小微企业定制的现代化智能进销存管理系统。该系统聚焦于核心业务流：采购入库、销售出库、库存流转，以及后台系统数据管控，通过数字化手段协助企业告别传统手工台账，提升供应链协同效率并降低运营成本。系统不仅具备完善的 RBAC 角色权限体系，提供全面的高价值数据可视化分析看板，还支持 Docker 容器化的快速私有部署。

---

## 📸 系统主页

<div align="center">

<img src="./Frontend/public/client.png" alt="前端展示 / 客户端" width="100%">
<br><br>
<img src="./Frontend/public/admin.png" alt="后端 / 管理系统" width="100%">

</div>

---

## ✨ 核心业务特性

本项目涵盖了企业日常进销存业务所需要的四大业务核心版块：

### 📊 1. 业务全景仪表盘 (Dashboard)

- **实时 KPI 监控：** 包含但不限于当月销售总额、本周新增采购、预警商品占比、流失客户等多维关键指标追踪。
- **库存健康度与分类结构分析：** 通过 ECharts 提供丰富的饼图、玫瑰图对商品各类目的仓储占比做出详细多维剖析。
- **出入库趋势折线图：** 实时监控过去 30 天/6 个月的整体业务波峰波谷，协助预判市场动向与采购节点。
- **智能异常预警墙：** 把需要紧急干预的操作集中展示。

### 📦 2. 核心库存系统 (WMS)

- **多库位精细化管理：** 灵活定义和层级划分企业的各大物理或逻辑仓储、货架、库位节点。
- **实时库存溯源追踪：** 按 SKU 或条形码精准提供每一个商品的库存储备快照，支持溯源所有历史流水台账。
- **动态库存调整预警：** 当商品库存触及安全下限或过高堆积时，系统主动生成预警任务推送。
- **手工盘点调整与核算：** 支持损溢报盘工作及其对应审批记账流转。

### 🏷️ 3. 商品主数据中心

- **SKU 高效 CRUD 管理：** 所见即所得的商品上新和商品图片多图并发上传及结构化处理。
- **条形码全生命周期追溯：** 深度集成条形码生成体系（ZXing），无缝桥接实物扫码出入库场景。
- **分类搜索与字典管理：** 树形维度的多级分类展示，与全局实时响应的高性能分页搜查列表。

### 🛒 4. 采购与销售闭环

- **供应链单据生命周期：** 覆盖采购询价、到货验收、销售开单、出库发货全过程管理。
- **上游源头渠道管理：** 维护供应商、客户的资质与账期，追踪信用与历史单据交互情况。
- **事件驱动库存自动更新：** 订单状态从“挂起”到“完结”，底层基于严格事务自动计算并落实扣库/加库。

### 🔐 5. 管理员与系统配置 (RBAC)

- **细粒度权限控制 (RBAC)：** 从按钮级到路由级的精细化权限切割，轻松控制“超级管理员”、“库管员”、“出纳”等多身份的权限。
- **全局日志审计中心：** 追踪“何人、何时、在什么模块、执行了什么关键修改”，防患于未然并支持定期滚动清理。
- **自定义系统级参数：** 支持前端页面的应用名配置，全局上传物理绑定挂载点，单据流水号生成前缀等多维度业务参数自定制。

---

## 🛠️ 系统架构与技术栈

采用前后端分离架构，前端注重交互体验与数据可视化展现，后端致力于高并发稳定性和强事务数据一致性控制。

| 分类            | 核心技术             | 版本  | 用途与说明                                  |
| :-------------- | :------------------- | :---: | :------------------------------------------ |
| **🎨 Web 前端** | **Vue.js**           |  3.x  | 核心逻辑与声明式渲染 (Composition API 风格) |
|                 | **Vite**             |  7.x  | 极速冷启动的现代前端构建与热重载工具        |
|                 | **Pinia**            |  3.x  | 直观、类型安全的新一代全局状态管控机制      |
|                 | **Vue Router**       |  4.x  | SPA 应用路由引擎与动态守卫控制              |
|                 | **Apache ECharts**   |  6.x  | 业务数据深度可视化呈现引擎                  |
|                 | **Axios**            |  1.x  | 封装统一拦截器与错误处理的 HTTP Client      |
| **⚙️ 核心后端** | **Spring Boot**      | 3.3.2 | 项目基础脚手架及核心控制容器                |
|                 | **Java**             |  17   | 拥抱新特性及 G1 垃圾回收优势的底层 JVM 语言 |
|                 | **MyBatis**          | 3.0.5 | 高效关系型对象映射框架，聚焦复杂 SQL 调优   |
|                 | **MySQL Server**     | 8.0+  | 持久化业务主存储关系数据库引擎              |
|                 | **Redis**            |  7.x  | Session 缓存、字典热点参数、全局 ID 分配    |
| **🚀 运维部署** | **Docker** / Compose |  24+  | 双线分离的容器化集成服务引擎                |
|                 | **Nginx**            | 1.25+ | 高并发 HTTP 服务器，代理转发及静态资源托管  |

---

## 📂 项目目录结构

该项目采用了标准的微服务与前后台解耦的单体开发目录结构，层级划分清晰：

```text
Inventory_Management_System/
├── Backend/                 # Java Spring Boot 后端源码工程
│   ├── src/main/java        # 后端核心业务代码逻辑区
│   │   └── com/.../         # Controller/Service/Mapper/Entity/Config 架构分层
│   ├── src/main/resources   # 配置文件与数据库静态文件 (application.yml, mapper/*.xml)
│   ├── pom.xml              # Maven 构建依赖配置
│   └── mvnw / mvnw.cmd      # 内置 Maven 跨平台执行脚本
├── Frontend/                # Vue 3 前端源码工程
│   ├── src/                 # 前端核心业务代码逻辑区
│   │   ├── api/             # 统一封装的 Axios 异步请求汇聚点
│   │   ├── assets/          # 全局静态样式与内置媒体资源
│   │   ├── components/      # 抽象封装的可复用 Vue 组件
│   │   ├── layout/          # 总体页面 UI 框架骨架
│   │   ├── router/          # 前端路由挂载表与登录状态拦截守卫
│   │   ├── store/           # 基于 Pinia 全局响应式状态库
│   │   ├── utils/           # 时间处理、数字精算、正则校验等基础公共类库
│   │   └── views/           # 产品核心页面视图结构
│   ├── package.json         # NPM Node 依赖地图
│   └── vite.config.js       # Vite 核心打包及反向代理规则配置
├── docker-compose.yml       # 项目完整一键容器化编排配置文件
├── Dockerfile               # 跨多阶段镜像打包构造文件 (含前后端统一描述)
├── nginx.conf               # Nginx 高级负载、伪静态及跨域转发规则模板
└── .env.example             # 部署环境隔离变量模板 (需拷贝为 .env)
```

---

## 🚀 快速安装开始

### 📋 环境前置要求 (本地原生开发与运行)

若不采用 Docker 部署方式而选择传统模式开发，开发宿主机需要配备以下环境：

- **Node.js**: `^20.19.0` 或 `≥22.12.0` (推荐使用稳定的 LTS 版本)
- **Java Development Kit (JDK)**: `17` 或更高版本
- **MySQL**: `8.0` 及以上版本
- **Redis**: 运行状态下的 `6.0+`/`7.x` 实例
- **Maven**: `3.6+` (项目内已附赠 mvnw wrapper 以省去系统环境配置)

---

### 🖥️ 本地环境 (前端启动)

1. 进入前端根工程目录：
   ```bash
   cd Frontend
   ```
2. 安装 NPM 依赖树：
   ```bash
   npm install
   ```
3. 唤醒本地热渲染开发服务器：
   ```bash
   npm run dev
   ```
   🎉 浏览器自动跳转或访问: **http://localhost:5173**

---

### ⚙️ 本地环境 (后端启动)

1. **配置数据库环境：** 极力推荐先行启动开发本地（或公网）的 MySQL `8.0` 以及 Redis 环境，并且利用你习惯的工具 (Navicat/DataGrip) 初始化并创建用于挂载的特定数据库（例如命名为 `demo1`）。若有随源码附带的 SQL，请执行导入。
2. 进入后端基础目录工程：
   ```bash
   cd Backend
   ```
3. 检视或更新 `application.properties/yml` 中的数据库配置及密码。
4. 调用 Maven Wrapper 编译启动：
   ```bash
   mvnw.cmd spring-boot:run
   # Mac/Linux 系列环境下使用: ./mvnw spring-boot:run
   ```
   🔌 服务网关激活成功监听地址: **http://localhost:8080**

---

## 🐳 Docker 容器化部署 (推荐生产与演示)

如需实现一端开发、处处无缝运行。我们原生提供了极致精简环境依赖的 `docker-compose` 自动化服务集成栈，可一步将 Nginx前端、Spring Boot后端、以及独立的内部 Redis 装载拉起。

### 第一步：准备环境变量

在项目工程根目录处拷贝示例环境变量文件，以创建针对你宿主机或生产主机的本地私有环境：

```bash
cp .env.example .env
```

用编辑器打开 `.env` 文件，完善你的 MySQL （需预先建表）相关连接参数、Redis 的访问密匙及各类端口宿主机映射需求。其中 `MYSQL_HOST` 默认设为 `host.docker.internal` 即挂载本机的 MySQL 进程。

### 第二步：一键集成编译及构建

在此之前，确认当前宿主机或服务器已经完整安装并启动了 Docker Desktop 或者原生 Docker Engine服务。

```bash
docker-compose up -d --build
```

该命令执行后系统将：

1. 自动依据 `Dockerfile` 构建基于 `alpine` / `jdk17` 的轻量运行后容器镜像。
2. 自动安装 Node 依赖包并构建前端至生产环境 Dist 静态包。
3. 把前端 Dist 包推入基于最新版 Nginx 官方容器构建的 `inventory_nginx` 前端代理节点。
4. 自主检测上游容器健康情况，拉起高可用 Redis 和 后置 Spring Boot 业务服务器。

⏳ 构建完成后，在浏览器中访问 `.env` 文件里指定的 `DOMAIN_NAME` (默认为 `http://localhost`) 即刻进行生产级体验。

---

## 👨‍💻 贡献指南

1. **Fork** 此项目并在你的代码库建立对应的副本。
2. 创建以特性功能为核心的分支并开展研发 (`git checkout -b feature/AmazingFeature`)。
3. 请严格依照 ESLint 和阿里 Java 开发规范。
4. 提交经过深思熟虑且干净利落的代码变动 (`git commit -m 'feat: Add some AmazingFeature'`)。
5. 推送到远端代码池分支 (`git push origin feature/AmazingFeature`)。
6. 并发起向我们回溯融合的 Pull Request。

---

## 📄 许可证 (License)

本项目采用 [AGPL-3.0](LICENSE) 许可证。

Copyright © 2026-Present [yeflyleaf](https://github.com/yeflyleaf). All Rights Reserved.

---

<div align="center">

_驱动中小微企业数字化生态重构，若本项目对你的学习、二次开发、业务开展有帮助，欢迎给我们一颗小小的 Star 等作为对开源作者的支持和鼓励！_

</div>

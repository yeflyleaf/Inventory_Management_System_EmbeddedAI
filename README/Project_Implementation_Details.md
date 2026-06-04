<div align="center">

# 📦 Inventory Management System

</div>

## 📁 项目结构

### 🖥️ 前端项目结构 (Frontend)

```
📦 Frontend/
├── 📄 index.html                           # 入口 HTML 文件，定义根挂载点 #app
├── 📄 package.json                         # NPM 依赖配置，定义 Vue/Vite/ECharts 等包版本
├── 📄 vite.config.js                       # Vite 构建配置，配置代理、别名、插件
├── 📄 Dockerfile                           # Docker 镜像构建文件，用于容器化部署
├── 📄 nginx.conf                           # Nginx 反向代理配置，处理前端路由和 API 转发
│
└── 📂 src/                                 # 源代码根目录
    │
    ├── 📄 App.vue                          # Vue 根组件，包含 <RouterView> 和全局 Toast
    ├── 📄 main.js                          # 应用入口，初始化 Vue/Pinia/Router 并挂载到 DOM
    │
    ├── 📂 api/                             # 🌐 API 接口层
    │   └── 📄 request.js                   # Axios 封装：baseURL=/api，请求/响应拦截器，Token 注入
    │
    ├── 📂 assets/                          # 🎨 静态资源目录
    │   ├── 📄 base.css                     # CSS 变量定义：颜色、字体、间距等设计令牌
    │   ├── 📄 main.css                     # 全局样式：重置样式、通用类、布局样式
    │   └── 📄 logo.svg                     # 系统 Logo 矢量图标
    │
    ├── 📂 components/                      # 🧩 公共可复用组件
    │   ├── 📄 ToastContainer.vue           # 全局 Toast 通知容器，支持 success/error/warning/info 类型
    │   ├── 📄 ConfirmModal.vue             # 通用确认模态框
    │   ├── 📄 RefreshButton.vue            # 防抖刷新按钮
    │   ├── 📄 HelloWorld.vue               # Vue 脚手架示例组件（可删除）
    │   ├── 📄 TheWelcome.vue               # 欢迎页组件（可删除）
    │   └── 📄 WelcomeItem.vue              # 欢迎项子组件（可删除）
    │
    ├── 📂 directives/                      # 🔧 自定义指令
    │   └── 📄 throttle.js                  # 节流指令 v-throttle
    │
    ├── 📂 router/                          # 🚦 路由配置
    │   └── 📄 index.js                     # Vue Router 配置：定义所有页面路由、嵌套路由、导航守卫
    │
    ├── 📂 stores/                          # 📦 状态管理 (Pinia)
    │   ├── 📄 counter.js                   # 计数器 Store 示例（可删除）
    │   └── 📄 user.js                      # 用户状态 Store：存储登录用户信息、Token、登录状态
    │
    ├── 📂 utils/                           # 🔧 工具函数库
    │   └── 📄 toast.js                     # Toast 工具：showSuccess/showError/showWarning/showInfo 方法
    │
    └── 📂 views/                           # 📄 页面视图组件
        │
        ├── 📂 Dashboard/                   # ━━━━━ 📊 仪表盘模块 ━━━━━
        │   └── 📄 DashboardView.vue        # 数据仪表盘主页面
        │                                   #   - 6个 KPI 统计卡片 (SKU/库存/低库存/缺货/待入库/待出库)
        │                                   #   - 异常告警面板 (缺货/低库存/积压提醒)
        │                                   #   - ECharts 饼图：库存健康度分析
        │                                   #   - ECharts 柱状图：商品分类 Top5 (可切换库存/销量/销售额)
        │                                   #   - ECharts 折线图：30天出入库趋势 (支持时间/仓库/分类/商品筛选)
        │                                   #   → 调用: GET /dashboard/stats, GET /stock
        │
        ├── 📂 Login/                       # ━━━━━ 🔐 登录模块 ━━━━━
        │   └── 📄 LoginView.vue            # 用户登录页面
        │                                   #   - 用户名/密码表单
        │                                   #   - 登录验证与 Token 存储
        │                                   #   → 调用: POST /auth/login
        │
        ├── 📂 Layout/                      # ━━━━━ 📐 布局模块 ━━━━━
        │   └── 📄 MainLayout.vue           # 主布局框架
        │                                   #   - 左侧侧边栏导航菜单
        │                                   #   - 顶部标题栏
        │                                   #   - 中间 <RouterView> 内容区
        │
        ├── 📂 Item/                        # ━━━━━ 🏷️ 商品与库存模块 ━━━━━
        │   │
        │   ├── 📄 ItemListView.vue         # 商品列表页面
        │   │                               #   - 商品搜索 (名称/分类/条码)
        │   │                               #   - 分页表格展示商品信息
        │   │                               #   - 新增/编辑/删除按钮
        │   │                               #   → 调用: GET /item, DELETE /item/{id}
        │   │
        │   ├── 📄 ItemEditView.vue         # 商品新增/编辑页面
        │   │                               #   - 商品表单 (名称/分类/规格/价格/条码/图片)
        │   │                               #   - 图片上传预览
        │   │                               #   - 条码扫描/生成
        │   │                               #   → 调用: POST /item, PUT /item/{id}, POST /upload/image
        │   │
        │   ├── 📄 StockListView.vue        # 库存列表页面
        │   │                               #   - 实时库存快照展示
        │   │                               #   - 库存状态筛选 (全部/正常/低库存/缺货)
        │   │                               #   - 低库存高亮显示
        │   │                               #   - 库存统计 (总数/低库存数/缺货数)
        │   │                               #   → 调用: GET /stock
        │   │
        │   ├── 📄 StockFlowView.vue        # 出入库流水记录页面
        │   │                               #   - 出入库历史记录列表
        │   │                               #   - 按类型筛选 (入库/出库)
        │   │                               #   - 显示操作时间、数量、关联单据
        │   │                               #   → 调用: GET /stock/flow
        │   │
        │   └── 📄 StockAdjustView.vue      # 库存调整页面
        │                                   #   - 手动入库/出库操作
        │                                   #   - 选择商品、仓库、数量
        │                                   #   - 填写调整原因
        │                                   #   → 调用: POST /stock/adjust
        │
        ├── 📂 Purchase/                    # ━━━━━ 📥 采购模块 ━━━━━
        │   │
        │   ├── 📄 PurchaseListView.vue     # 采购订单列表页面
        │   │                               #   - 订单搜索与状态筛选
        │   │                               #   - 分页表格展示订单
        │   │                               #   - 新建/查看/入库/删除操作
        │   │                               #   → 调用: GET /purchase, DELETE /purchase/{id}
        │   │
        │   ├── 📄 PurchaseEditView.vue     # 采购订单新增/编辑页面
        │   │                               #   - 选择供应商
        │   │                               #   - 添加采购商品明细 (商品/数量/单价)
        │   │                               #   - 计算订单总金额
        │   │                               #   → 调用: POST /purchase, PUT /purchase/{id}
        │   │
        │   └── 📄 PurchaseInboundView.vue  # 采购入库确认页面
        │                                   #   - 确认收货入库
        │                                   #   - 更新订单状态为已完成
        │                                   #   - 触发库存增加
        │                                   #   → 调用: PUT /purchase/{id}/inbound
        │
        ├── 📂 Sales/                       # ━━━━━ 📤 销售模块 ━━━━━
        │   │
        │   ├── 📄 SalesListView.vue        # 销售订单列表页面
        │   │                               #   - 订单搜索与状态筛选
        │   │                               #   - 分页表格展示订单
        │   │                               #   - 新建/查看/出库/删除操作
        │   │                               #   → 调用: GET /sale, DELETE /sale/{id}
        │   │
        │   ├── 📄 SalesEditView.vue        # 销售订单新增/编辑页面
        │   │                               #   - 选择客户
        │   │                               #   - 添加销售商品明细 (商品/数量/单价)
        │   │                               #   - 计算订单总金额
        │   │                               #   - 库存不足校验
        │   │                               #   → 调用: POST /sale, PUT /sale/{id}
        │   │
        │   └── 📄 SalesOutboundView.vue    # 销售出库确认页面
        │                                   #   - 确认发货出库
        │                                   #   - 更新订单状态为已完成
        │                                   #   - 触发库存减少
        │                                   #   → 调用: PUT /sale/{id}/outbound
        │
        ├── 📂 Basic/                       # ━━━━━ 📋 基础数据模块 ━━━━━
        │   │
        │   ├── 📄 CustomerListView.vue     # 客户管理列表页面
        │   │                               #   - 客户搜索与分页
        │   │                               #   - 客户信息展示 (名称/联系人/电话/地址)
        │   │                               #   - 新增/编辑/删除客户
        │   │                               #   → 调用: GET/POST/PUT/DELETE /customer
        │   │
        │   └── 📄 SupplierListView.vue     # 供应商管理列表页面
        │                                   #   - 供应商搜索与分页
        │                                   #   - 供应商信息展示 (名称/联系人/电话/地址)
        │                                   #   - 新增/编辑/删除供应商
        │                                   #   → 调用: GET/POST/PUT/DELETE /supplier
        │
        ├── 📂 Admin/                       # ━━━━━ 🛡️ 管理员模块 ━━━━━
        │   │
        │   ├── 📄 AdminDashboardView.vue   # 管理员仪表盘
        │   ├── 📄 AdminLayout.vue          # 管理员后台布局
        │   ├── 📄 OperationLogsView.vue    # 操作日志查看
        │   ├── 📄 SystemSettingsView.vue   # 系统设置
        │   └── 📄 UserManageView.vue       # 用户管理
        │
        ├── 📂 Profile/                     # ━━━━━ 👤 个人中心模块 ━━━━━
        │   └── 📄 UserProfileView.vue      # 用户个人资料页
        │                                   #   - 修改头像、密码
        │
        └── 📄 NotFoundView.vue             # 404 页面 - 路由不匹配时显示
```

---

### ⚙️ 后端项目结构 (Backend)

```
📦 Backend/
├── 📄 pom.xml                              # Maven 项目配置：依赖管理 (Spring Boot/MyBatis/MySQL/AOP/Actuator)
├── 📄 Dockerfile                           # Docker 镜像构建：基于 OpenJDK 17，打包 JAR 运行
├── 📄 entrypoint.sh                        # Docker 容器启动脚本，执行 java -jar
│
└── 📂 src/main/
    │
    ├── 📂 java/com/example/backend/        # ━━━━━ Java 源代码 ━━━━━
    │   │
    │   ├── 📄 BackendApplication.java      # Spring Boot 启动类，@SpringBootApplication 注解入口
    │   │
    │   ├── 📂 aspect/                      # ━━━━━ 🎭 AOP 切面 ━━━━━
    │   │   └── 📄 OperationLogAspect.java  # 操作日志切面，记录用户操作行为
    │   │
    │   ├── 📂 controller/                  # ━━━━━ 🎯 REST API 控制器层 ━━━━━
    │   │   │                               # 接收 HTTP 请求，调用 Service，返回 Result<VO>
    │   │   │
    │   │   ├── 📄 AuthController.java      # 用户认证控制器
    │   │   │                               #   POST /auth/login    → 用户登录，返回 JWT Token
    │   │   │                               #   POST /auth/register → 用户注册
    │   │   │
    │   │   ├── 📄 AdminController.java     # 管理员控制器
    │   │   │                               #   GET /admin/stats    → 管理员仪表盘统计
    │   │   │
    │   │   ├── 📄 SystemController.java    # 系统设置控制器
    │   │   │                               #   GET/PUT /system/settings → 获取/更新系统设置
    │   │   │
    │   │   ├── 📄 UserProfileController.java # 用户个人中心控制器
    │   │   │                               #   GET/PUT /user/profile → 获取/更新个人信息
    │   │   │
    │   │   ├── 📄 DashboardController.java # 仪表盘数据控制器
    │   │   │                               #   GET /dashboard/stats         → 获取 KPI 统计 + 趋势数据
    │   │   │                               #   GET /dashboard/category-stats → 获取分类统计 (Top5)
    │   │   │
    │   │   ├── 📄 ProductController.java   # 商品管理控制器
    │   │   │                               #   GET    /item      → 商品列表
    │   │   │                               #   GET    /item/{id} → 商品详情
    │   │   │                               #   POST   /item      → 新增商品
    │   │   │                               #   PUT    /item/{id} → 更新商品
    │   │   │                               #   DELETE /item/{id} → 删除商品
    │   │   │
    │   │   ├── 📄 StockController.java     # 库存管理控制器
    │   │   │                               #   GET  /stock        → 库存快照列表
    │   │   │                               #   GET  /stock/flow   → 出入库流水记录
    │   │   │                               #   POST /stock/adjust → 手动库存调整
    │   │   │
    │   │   ├── 📄 PurchaseOrderController.java  # 采购订单控制器
    │   │   │                               #   GET    /purchase           → 订单列表
    │   │   │                               #   GET    /purchase/{id}      → 订单详情
    │   │   │                               #   POST   /purchase           → 新增订单
    │   │   │                               #   PUT    /purchase/{id}      → 更新订单
    │   │   │                               #   DELETE /purchase/{id}      → 删除订单
    │   │   │                               #   PUT    /purchase/{id}/inbound → 确认入库
    │   │   │
    │   │   ├── 📄 SaleOrderController.java # 销售订单控制器
    │   │   │                               #   GET    /sale              → 订单列表
    │   │   │                               #   GET    /sale/{id}         → 订单详情
    │   │   │                               #   POST   /sale              → 新增订单
    │   │   │                               #   PUT    /sale/{id}         → 更新订单
    │   │   │                               #   DELETE /sale/{id}         → 删除订单
    │   │   │                               #   PUT    /sale/{id}/outbound → 确认出库
    │   │   │
    │   │   ├── 📄 CustomerController.java  # 客户管理控制器 → GET/POST/PUT/DELETE /customer
    │   │   ├── 📄 SupplierController.java  # 供应商管理控制器 → GET/POST/PUT/DELETE /supplier
    │   │   ├── 📄 WarehouseController.java # 仓库管理控制器 → GET/POST/PUT/DELETE /warehouse
    │   │   │
    │   │   ├── 📄 BarcodeController.java   # 条码管理控制器
    │   │   │                               #   GET  /barcode/generate → 生成条形码图片
    │   │   │                               #   POST /barcode/decode   → 解析条形码内容
    │   │   │
    │   │   └── 📄 FileUploadController.java # 文件上传控制器
    │   │                                   #   POST   /upload/image → 上传商品图片，返回 URL
    │   │                                   #   DELETE /upload/image → 删除已上传图片
    │   │
    │   ├── 📂 service/                     # ━━━━━ 💼 业务逻辑层 (接口) ━━━━━
    │   │   │                               # 定义业务方法签名，供 Controller 调用
    │   │   │
    │   │   ├── 📄 UserService.java         # 用户服务接口：login(), register(), getUserInfo()
    │   │   ├── 📄 DashboardService.java    # 仪表盘服务接口：getStats(), getCategoryStats()
    │   │   ├── 📄 ProductService.java      # 商品服务接口：list(), add(), update(), delete()
    │   │   ├── 📄 StockService.java        # 库存服务接口：getStockSnapshot(), adjust(), getFlows()
    │   │   ├── 📄 PurchaseOrderService.java # 采购订单服务接口：list(), create(), inbound()
    │   │   ├── 📄 SaleOrderService.java    # 销售订单服务接口：list(), create(), outbound()
    │   │   ├── 📄 CustomerService.java     # 客户服务接口：list(), add(), update(), delete()
    │   │   ├── 📄 SupplierService.java     # 供应商服务接口：list(), add(), update(), delete()
    │   │   ├── 📄 WarehouseService.java    # 仓库服务接口：list(), add(), update(), delete()
    │   │   ├── 📄 BarcodeService.java      # 条码服务接口：generate(), decode()
    │   │   │
    │   │   └── 📂 Impl/                    # ━━━━━ 💼 业务逻辑层 (实现) ━━━━━
    │   │       │                           # @Service 注解，实现具体业务逻辑
    │   │       │
    │   │       ├── 📄 UserServiceImpl.java         # 用户服务实现：密码校验、JWT 生成
    │   │       ├── 📄 DashboardServiceImpl.java    # 仪表盘服务实现：聚合统计计算、趋势数据查询
    │   │       ├── 📄 ProductServiceImpl.java      # 商品服务实现：商品 CRUD、图片关联
    │   │       ├── 📄 StockServiceImpl.java        # 库存服务实现：库存计算、调整记录写入 StockFlow
    │   │       ├── 📄 PurchaseOrderServiceImpl.java # 采购订单实现：订单创建、入库触发库存增加
    │   │       ├── 📄 SaleOrderServiceImpl.java    # 销售订单实现：订单创建、出库触发库存减少
    │   │       ├── 📄 CustomerServiceImpl.java     # 客户服务实现：客户 CRUD
    │   │       ├── 📄 SupplierServiceImpl.java     # 供应商服务实现：供应商 CRUD
    │   │       ├── 📄 WarehouseServiceImpl.java    # 仓库服务实现：仓库 CRUD
    │   │       └── 📄 BarcodeServiceImpl.java      # 条码服务实现：使用 ZXing 生成/解析条形码
    │   │
    │   ├── 📂 dao/                         # ━━━━━ 🗄️ 数据访问层 (MyBatis Mapper 接口) ━━━━━
    │   │   │                               # @Mapper 注解，定义 SQL 方法，对应 XML 映射
    │   │   │
    │   │   ├── 📄 UserMapper.java          # 用户表操作：selectByUsername(), insert()
    │   │   ├── 📄 OperationLogMapper.java  # 操作日志表操作
    │   │   ├── 📄 SystemSettingMapper.java # 系统设置表操作
    │   │   ├── 📄 ProductMapper.java       # 商品表操作：selectAll(), insert(), update(), delete()
    │   │   ├── 📄 StockFlowMapper.java     # 库存流水表：insert(), selectRecent(), selectDailyStats(), sumStock()
    │   │   ├── 📄 PurchaseOrderMapper.java # 采购订单表：selectAll(), insert(), updateStatus()
    │   │   ├── 📄 PurchaseOrderItemMapper.java # 采购明细表：insertBatch(), selectByOrderId()
    │   │   ├── 📄 SaleOrderMapper.java     # 销售订单表：selectAll(), insert(), updateStatus(), sumTodaySales()
    │   │   ├── 📄 SaleOrderItemMapper.java # 销售明细表：insertBatch(), selectSalesQtyByCategory()
    │   │   ├── 📄 CustomerMapper.java      # 客户表：selectAll(), insert(), update(), delete()
    │   │   ├── 📄 SupplierMapper.java      # 供应商表：selectAll(), insert(), update(), delete()
    │   │   └── 📄 WarehouseMapper.java     # 仓库表：selectAll(), insert(), update(), delete()
    │   │
    │   ├── 📂 entity/                      # ━━━━━ 🏛️ 数据库实体类 ━━━━━
    │   │   │                               # @Data (Lombok) 注解，映射数据库表字段
    │   │   │
    │   │   ├── 📄 User.java                # 用户实体：id, username, password, role, createdAt
    │   │   ├── 📄 Product.java             # 商品实体：id, name, category, spec, price, barcode, imageUrl
    │   │   ├── 📄 StockFlow.java           # 库存流水：id, itemId, warehouseId, changeType, changeAmount, createdAt
    │   │   ├── 📄 PurchaseOrder.java       # 采购订单：id, orderNo, supplierId, totalAmount, status, createdAt
    │   │   ├── 📄 PurchaseOrderItem.java   # 采购明细：id, orderId, itemId, quantity, unitPrice
    │   │   ├── 📄 SaleOrder.java           # 销售订单：id, orderNo, customerId, totalAmount, status, createdAt
    │   │   ├── 📄 SaleOrderItem.java       # 销售明细：id, orderId, itemId, quantity, unitPrice
    │   │   ├── 📄 Customer.java            # 客户实体：id, name, contact, phone, address
    │   │   ├── 📄 Supplier.java            # 供应商实体：id, name, contact, phone, address
    │   │   └── 📄 Warehouse.java           # 仓库实体：id, name, address
    │   │
    │   ├── 📂 vo/                          # ━━━━━ 📤 视图对象 (返回给前端的数据结构) ━━━━━
    │   │   │
    │   │   ├── 📄 LoginVO.java             # 登录响应：token, userId, username, role
    │   │   ├── 📄 DashboardVO.java         # 仪表盘：todaySales, totalStock, lowStockCount, stockTrend, pendingOrders
    │   │   ├── 📄 DashboardActivityVO.java # 最近活动：time, description
    │   │   ├── 📄 CategoryStatsVO.java     # 分类统计：category, value
    │   │   ├── 📄 StockFlowDailyStatsVO.java # 每日趋势：date, inQuantity, outQuantity
    │   │   ├── 📄 ProductVO.java           # 商品详情：包含所有字段 + 图片 URL
    │   │   ├── 📄 StockVO.java             # 库存快照：itemId, itemName, category, currentStock
    │   │   ├── 📄 StockFlowVO.java         # 流水详情：包含商品名、仓库名等关联信息
    │   │   ├── 📄 PurchaseOrderVO.java     # 采购订单详情：包含供应商名、明细列表
    │   │   ├── 📄 PurchaseOrderItemVO.java # 采购明细：包含商品名
    │   │   ├── 📄 SaleOrderVO.java         # 销售订单详情：包含客户名、明细列表
    │   │   ├── 📄 SaleOrderItemVO.java     # 销售明细：包含商品名
    │   │   ├── 📄 CustomerVO.java          # 客户详情：所有字段
    │   │   ├── 📄 SupplierVO.java          # 供应商详情：所有字段
    │   │   └── 📄 WarehouseVO.java         # 仓库详情：所有字段
    │   │
    │   ├── 📂 dto/                         # ━━━━━ 📥 数据传输对象 (接收前端请求参数) ━━━━━
    │   │   │
    │   │   ├── 📄 LoginDTO.java            # 登录请求：username, password
    │   │   ├── 📄 UserAddDTO.java          # 注册请求：username, password, role
    │   │   ├── 📄 ProductDTO.java          # 商品请求：name, category, spec, price, barcode, imageUrl
    │   │   ├── 📄 StockAdjustDTO.java      # 库存调整：itemId, warehouseId, changeType, changeAmount, reason
    │   │   ├── 📄 PurchaseOrderDTO.java    # 采购订单请求：supplierId, items[]
    │   │   ├── 📄 PurchaseOrderItemDTO.java # 采购明细：itemId, quantity, unitPrice
    │   │   ├── 📄 SaleOrderDTO.java        # 销售订单请求：customerId, items[]
    │   │   ├── 📄 SaleOrderItemDTO.java    # 销售明细：itemId, quantity, unitPrice
    │   │   ├── 📄 CustomerDTO.java         # 客户请求：name, contact, phone, address
    │   │   ├── 📄 SupplierDTO.java         # 供应商请求：name, contact, phone, address
    │   │   └── 📄 WarehouseDTO.java        # 仓库请求：name, address
    │   │
    │   ├── 📂 config/                      # ━━━━━ ⚙️ Spring 配置类 ━━━━━
    │   │   │
    │   │   ├── 📄 WebConfig.java           # Web 配置：静态资源映射 (/uploads/**)、拦截器注册
    │   │   ├── 📄 CorsConfig.java          # 跨域配置：允许前端 localhost:5173 访问
    │   │   └── 📄 RedisConfig.java         # Redis 配置：RedisTemplate 序列化配置
    │   │
    │   ├── 📂 common/                      # ━━━━━ 🔧 公共类 ━━━━━
    │   │   │
    │   │   └── 📄 Result.java              # 统一响应封装：code, message, data，提供 success()/fail() 静态方法
    │   │
    │   ├── 📂 utils/                       # ━━━━━ 🛠️ 工具类 ━━━━━
    │   │   │
    │   │   ├── 📄 JwtUtils.java            # JWT 工具：generateToken(), parseToken(), validateToken()
    │   │   ├── 📄 RedisUtils.java          # Redis 工具：set(), get(), delete(), expire() 等封装
    │   │   ├── 📄 DateUtils.java           # 日期工具：格式化、计算日期差、获取当天起止时间
    │   │   ├── 📄 FileUtils.java           # 文件工具：保存上传文件、删除文件、生成唯一文件名
    │   │   └── 📄 IdGenerator.java         # ID 生成器：生成唯一订单号 (如 PO20251207001)
    │   │
    │   ├── 📂 annotation/                  # ━━━━━ 🏷️ 自定义注解 ━━━━━
    │   │   │
    │   │   ├── 📄 LoginRequired.java       # 登录校验注解：标记需要登录才能访问的接口
    │   │   └── 📄 Permission.java          # 权限校验注解：标记需要特定权限的接口
    │   │
    │   ├── 📂 interceptor/                 # ━━━━━ 🚧 拦截器 ━━━━━
    │   │   │
    │   │   └── 📄 AuthInterceptor.java     # 认证拦截器：校验 JWT Token、注入用户信息到请求上下文
    │   │
    │   ├── 📂 handler/                     # ━━━━━ 🔧 类型处理器 ━━━━━
    │   │   └── 📄 StringListTypeHandler.java # MyBatis List<String> ↔ JSON 转换
    │   │
    │   └── 📂 task/                        # ━━━━━ ⏰ 定时任务 ━━━━━
    │       └── 📄 FileCleanupTask.java     # 文件清理任务
    │
    └── 📂 resources/                       # ━━━━━ 📂 资源文件 ━━━━━
        │
        ├── 📄 application.properties       # Spring Boot 配置：数据库连接、Redis、端口、日志级别
        ├── 📄 data.sql                     # 数据库初始化数据
        ├── 📄 schema.sql                   # 数据库初始化结构
        │
        └── 📂 mapper/                      # ━━━━━ MyBatis XML 映射文件 ━━━━━
            │                               # 定义 SQL 语句，与 Mapper 接口方法对应
            │
            ├── 📄 UserMapper.xml           # 用户 SQL：selectByUsername, insert
            ├── 📄 OperationLogMapper.xml   # 操作日志 SQL
            ├── 📄 SystemSettingMapper.xml  # 系统设置 SQL
            ├── 📄 ProductMapper.xml        # 商品 SQL：selectAll, insert, update, delete (含 image_url)
            ├── 📄 StockFlowMapper.xml      # 库存流水 SQL：insert, selectRecent, selectDailyStats (动态筛选)
            ├── 📄 PurchaseOrderMapper.xml  # 采购订单 SQL：selectAll, insert, updateStatus, countByStatus
            ├── 📄 PurchaseOrderItemMapper.xml # 采购明细 SQL：insertBatch, selectByOrderId
            ├── 📄 SaleOrderMapper.xml      # 销售订单 SQL：selectAll, insert, updateStatus, sumTodaySales
            ├── 📄 SaleOrderItemMapper.xml  # 销售明细 SQL：insertBatch, selectSalesQtyByCategory
            ├── 📄 CustomerMapper.xml       # 客户 SQL：selectAll, insert, update, delete
            ├── 📄 SupplierMapper.xml       # 供应商 SQL：selectAll, insert, update, delete
            └── 📄 WarehouseMapper.xml      # 仓库 SQL：selectAll, insert, update, delete
```

---

## 🔄 功能实现流程

### 📊 仪表盘数据流

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 (DashboardView.vue)                  │
├─────────────────────────────────────────────────────────────────┤
│  onMounted() → refreshData()                                    │
│       ↓                                                         │
│  Promise.all([                                                  │
│    axios.get('/dashboard/stats'),                               │
│    axios.get('/stock')                                          │
│  ])                                                             │
│       ↓                                                         │
│  ECharts 渲染: pieChart | barChart | lineChart                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTP
┌─────────────────────────────────────────────────────────────────┐
│                     后端 (DashboardController)                   │
├─────────────────────────────────────────────────────────────────┤
│  @GetMapping("/stats")                                          │
│       ↓                                                         │
│  DashboardService.getStats()                                    │
│       ├── SaleOrderMapper.sumTodaySales()                       │
│       ├── StockService.getStockSnapshot()                       │
│       ├── StockFlowMapper.selectDailyStats()                    │
│       └── PurchaseOrderMapper.countByStatus()                   │
│       ↓                                                         │
│  返回 DashboardVO                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔗 功能点线性实现详解 (Frontend → Backend 完整链路)

> 本模块详细列出每个功能点从前端 Vue3 到后端 Java 的完整实现路径，包含所有涉及的文件、类、接口、方法，以及数据的读取、判断、传输流程。

---

### ① 用户登录 (User Login)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Login/LoginView.vue                                               │
│  ├── 用户输入: username, password                                            │
│  ├── 表单验证: 非空校验                                                       │
│  ├── 调用方法: handleLogin()                                                 │
│  │       ↓                                                                  │
│  📄 api/request.js                                                          │
│  ├── axios.post('/auth/login', { username, password })                      │
│  ├── 请求拦截器: 自动添加 Content-Type: application/json                       │
│  │       ↓ HTTP POST                                                        │
│  📄 stores/user.js (Pinia)                                                  │
│  ├── 登录成功后: setToken(token), setUser(userInfo)                          │
│  └── 存储到 localStorage                                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/AuthController.java                                          │
│  ├── @PostMapping("/auth/login")                                            │
│  ├── 接收参数: @RequestBody LoginDTO dto                                     │
│  │       ↓                                                                  │
│  📄 dto/LoginDTO.java                                                       │
│  ├── 字段: username, password                                               │
│  │       ↓                                                                  │
│  📄 service/UserService.java (接口)                                          │
│  ├── LoginVO login(LoginDTO dto)                                            │
│  │       ↓                                                                  │
│  📄 service/Impl/UserServiceImpl.java (实现)                                 │
│  ├── 1. 调用 UserMapper.selectByUsername(username)                          │
│  ├── 2. 判断用户是否存在                                                      │
│  ├── 3. 校验密码是否匹配                                                      │
│  ├── 4. 调用 JwtUtils.generateToken(userId, username)                       │
│  ├── 5. 可选: RedisUtils.set() 存储 Token                                   │
│  │       ↓                                                                  │
│  📄 dao/UserMapper.java (接口)                                               │
│  ├── @Mapper                                                                │
│  ├── User selectByUsername(String username)                                 │
│  │       ↓                                                                  │
│  📄 resources/mapper/UserMapper.xml                                         │
│  ├── <select id="selectByUsername">                                         │
│  │     SELECT * FROM user WHERE username = #{username}                      │
│  │   </select>                                                              │
│  │       ↓                                                                  │
│  📄 entity/User.java                                                        │
│  ├── 字段: id, username, password, role, createdAt                          │
│  │       ↓                                                                  │
│  📄 utils/JwtUtils.java                                                     │
│  ├── generateToken(): 生成 JWT Token                                        │
│  │       ↓                                                                  │
│  📄 vo/LoginVO.java                                                         │
│  ├── 字段: token, userId, username, role                                    │
│  │       ↓                                                                  │
│  📄 common/Result.java                                                      │
│  └── return Result.success(loginVO, "登录成功")                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**数据流向**: `LoginView.vue` → `request.js` → `AuthController` → `UserServiceImpl` → `UserMapper` → `UserMapper.xml` → `MySQL user表` → `LoginVO` → `前端存储Token`

---

### ② 仪表盘数据加载 (Dashboard)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Dashboard/DashboardView.vue                                       │
│  ├── onMounted() 生命周期钩子                                                 │
│  │       ↓                                                                  │
│  ├── 调用 refreshData() 方法                                                 │
│  │       ↓                                                                  │
│  ├── Promise.all([                                                          │
│  │     axios.get('/stock'),               // 获取库存数据                     │
│  │     axios.get('/dashboard/stats')      // 获取统计数据                     │
│  │   ])                                                                     │
│  │       ↓                                                                  │
│  ├── 数据处理:                                                               │
│  │   ├── stats.totalSku = stockList.length                                  │
│  │   ├── stats.lowStock = stockList.filter(s => s.currentStock < 50).length │
│  │   ├── stats.zeroStock = stockList.filter(s => s.currentStock === 0).length│
│  │   └── stockTrendData = statsRes.stockTrend                               │
│  │       ↓                                                                  │
│  ├── ECharts 渲染:                                                           │
│  │   ├── pieChart.setOption() → 库存健康度饼图                                │
│  │   ├── barChart.setOption() → 分类分布柱状图                                │
│  │   └── lineChart.setOption() → 出入库趋势折线图                             │
│  │       ↓                                                                  │
│  📄 api/request.js                                                          │
│  └── 响应拦截器: 自动解析 result.data                                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend) - GET /dashboard/stats                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/DashboardController.java                                     │
│  ├── @GetMapping("/dashboard/stats")                                        │
│  ├── 接收参数: @RequestParam days, warehouseId, category, itemName          │
│  │       ↓                                                                  │
│  📄 service/DashboardService.java (接口)                                     │
│  ├── DashboardVO getStats(Long itemId, Integer days, ...)                   │
│  │       ↓                                                                  │
│  📄 service/Impl/DashboardServiceImpl.java (实现)                            │
│  ├── 1. 调用 SaleOrderMapper.sumTodaySales() → 今日销售额                     │
│  ├── 2. 调用 StockService.getStockSnapshot() → 库存快照                       │
│  ├── 3. 遍历计算 lowStockCount (库存 < 10)                                   │
│  ├── 4. 调用 StockFlowMapper.selectRecent(5) → 最近活动                       │
│  ├── 5. 调用 StockFlowMapper.selectDailyStats(days, ...) → 趋势数据          │
│  ├── 6. 调用 PurchaseOrderMapper.countByStatus("CREATED") → 待入库           │
│  ├── 7. 调用 SaleOrderMapper.countByStatus("CREATED") → 待出库               │
│  │       ↓                                                                  │
│  📄 dao/StockFlowMapper.java                                                │
│  ├── List<StockFlowDailyStatsVO> selectDailyStats(days, warehouseId, ...)   │
│  │       ↓                                                                  │
│  📄 resources/mapper/StockFlowMapper.xml                                    │
│  ├── <select id="selectDailyStats">                                         │
│  │     SELECT DATE(created_at) as date,                                     │
│  │            SUM(CASE WHEN change_amount > 0 THEN change_amount END) as in,│
│  │            SUM(CASE WHEN change_amount < 0 THEN ABS(...) END) as out     │
│  │     FROM stock_flow                                                      │
│  │     WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY)            │
│  │     <if test="warehouseId != null">AND warehouse_id = #{warehouseId}</if>│
│  │     <if test="category != null">AND item_id IN (...)</if>                │
│  │     GROUP BY DATE(created_at)                                            │
│  │   </select>                                                              │
│  │       ↓                                                                  │
│  📄 vo/DashboardVO.java                                                     │
│  ├── 字段: todaySales, totalStock, lowStockCount, zeroStockCount,           │
│  │         pendingPurchaseOrders, pendingSalesOrders, stockTrend,           │
│  │         recentActivity, overstockCount                                   │
│  │       ↓                                                                  │
│  📄 vo/StockFlowDailyStatsVO.java                                           │
│  ├── 字段: date, inQuantity, outQuantity                                    │
│  │       ↓                                                                  │
│  📄 common/Result.java                                                      │
│  └── return Result.success(dashboardVO)                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**涉及文件汇总**:
| 层级 | 文件 | 作用 |
|:---:|:---|:---|
| 前端视图 | `DashboardView.vue` | 页面渲染、ECharts 图表 |
| 前端 API | `request.js` | HTTP 请求封装 |
| 控制器 | `DashboardController.java` | 接收请求、参数校验 |
| 服务接口 | `DashboardService.java` | 定义业务方法 |
| 服务实现 | `DashboardServiceImpl.java` | 业务逻辑、数据聚合 |
| 数据访问 | `StockFlowMapper.java` | Mapper 接口 |
| SQL 映射 | `StockFlowMapper.xml` | 动态 SQL 查询 |
| 视图对象 | `DashboardVO.java` | 返回数据结构 |

---

### ③ 商品列表查询 (Product List)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Item/ItemListView.vue                                             │
│  ├── onMounted() → fetchProducts()                                          │
│  ├── 搜索功能: searchKeyword (v-model)                                       │
│  ├── 分页功能: currentPage, pageSize                                         │
│  │       ↓                                                                  │
│  ├── axios.get('/item', { params: { keyword, page, size } })                │
│  │       ↓                                                                  │
│  ├── 数据绑定: products.value = response                                     │
│  ├── 计算属性: filteredProducts (前端二次过滤)                                 │
│  └── 表格渲染: v-for="item in paginatedProducts"                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/ProductController.java                                       │
│  ├── @GetMapping("/item")                                                   │
│  ├── public Result<List<ProductVO>> list()                                  │
│  │       ↓                                                                  │
│  📄 service/ProductService.java (接口)                                       │
│  ├── List<ProductVO> list()                                                 │
│  │       ↓                                                                  │
│  📄 service/Impl/ProductServiceImpl.java (实现)                              │
│  ├── 1. 调用 ProductMapper.selectAll()                                      │
│  ├── 2. 遍历 List<Product> 转换为 List<ProductVO>                            │
│  ├── 3. 拼接图片完整 URL (如有)                                               │
│  │       ↓                                                                  │
│  📄 dao/ProductMapper.java                                                  │
│  ├── @Select("SELECT * FROM item ORDER BY id DESC")                         │
│  ├── List<Product> selectAll()                                              │
│  │       ↓                                                                  │
│  📄 resources/mapper/ProductMapper.xml                                      │
│  ├── <select id="selectAll" resultType="Product">                           │
│  │     SELECT id, name, category, spec, price, barcode, image_url           │
│  │     FROM item ORDER BY id DESC                                           │
│  │   </select>                                                              │
│  │       ↓                                                                  │
│  📄 entity/Product.java                                                     │
│  ├── @Data                                                                  │
│  ├── 字段: id, name, category, spec, price, barcode, imageUrl               │
│  │       ↓                                                                  │
│  📄 vo/ProductVO.java                                                       │
│  ├── 字段: id, name, category, spec, price, barcode, imageUrl               │
│  │       ↓                                                                  │
│  📄 common/Result.java                                                      │
│  └── return Result.success(productVOList)                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ④ 商品新增 (Product Add)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Item/ItemEditView.vue                                             │
│  ├── 表单数据: form = { name, category, spec, price, barcode, imageUrl }    │
│  ├── 图片上传: handleImageUpload()                                           │
│  │   └── axios.post('/upload/image', formData)                              │
│  ├── 条码生成: generateBarcode()                                             │
│  │       ↓                                                                  │
│  ├── 提交表单: handleSubmit()                                                │
│  │   ├── 判断: isEdit ? PUT : POST                                          │
│  │   └── axios.post('/item', form)                                          │
│  │       ↓                                                                  │
│  📄 utils/toast.js                                                          │
│  └── showSuccess('商品添加成功')                                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/ProductController.java                                       │
│  ├── @PostMapping("/item")                                                  │
│  ├── public Result<Void> add(@RequestBody ProductDTO dto)                   │
│  │       ↓                                                                  │
│  📄 dto/ProductDTO.java                                                     │
│  ├── 字段: name, category, spec, price, barcode, imageUrl                   │
│  │       ↓                                                                  │
│  📄 service/Impl/ProductServiceImpl.java                                    │
│  ├── 1. 创建 Product 实体                                                    │
│  ├── 2. BeanUtils.copyProperties(dto, product)                              │
│  ├── 3. 调用 ProductMapper.insert(product)                                  │
│  │       ↓                                                                  │
│  📄 dao/ProductMapper.java                                                  │
│  ├── int insert(Product product)                                            │
│  │       ↓                                                                  │
│  📄 resources/mapper/ProductMapper.xml                                      │
│  ├── <insert id="insert" useGeneratedKeys="true" keyProperty="id">          │
│  │     INSERT INTO item (name, category, spec, price, barcode, image_url)   │
│  │     VALUES (#{name}, #{category}, #{spec}, #{price}, #{barcode}, #{imageUrl})│
│  │   </insert>                                                              │
│  │       ↓                                                                  │
│  └── 返回 Result.success(null, "添加成功")                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ⑤ 库存快照查询 (Stock Snapshot)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Item/StockListView.vue                                            │
│  ├── onMounted() → fetchStock()                                             │
│  ├── axios.get('/stock')                                                    │
│  │       ↓                                                                  │
│  ├── 数据处理:                                                               │
│  │   ├── stockList.value = response                                         │
│  │   ├── 计算统计: totalItems, lowStockCount, zeroStockCount                 │
│  │   └── 状态筛选: filterStatus (全部/正常/低库存/缺货)                        │
│  │       ↓                                                                  │
│  ├── 低库存判断: item.currentStock < LOW_STOCK_THRESHOLD (50)               │
│  └── 行样式: :class="{ 'low-stock-row': item.currentStock < 50 }"           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/StockController.java                                         │
│  ├── @GetMapping("/stock")                                                  │
│  ├── public Result<List<StockVO>> getStockSnapshot()                        │
│  │       ↓                                                                  │
│  📄 service/StockService.java (接口)                                         │
│  ├── List<StockVO> getStockSnapshot(Long warehouseId)                       │
│  │       ↓                                                                  │
│  📄 service/Impl/StockServiceImpl.java (实现)                                │
│  ├── 1. 获取所有商品: ProductMapper.selectAll()                              │
│  ├── 2. 遍历每个商品:                                                        │
│  │   └── 调用 StockFlowMapper.sumStockByItemId(itemId) 计算当前库存          │
│  │       (SUM of change_amount WHERE item_id = ?)                           │
│  ├── 3. 组装 StockVO 列表                                                    │
│  │       ↓                                                                  │
│  📄 dao/StockFlowMapper.java                                                │
│  ├── Integer sumStockByItemIdAndWarehouseId(Long itemId, Long warehouseId)  │
│  │       ↓                                                                  │
│  📄 resources/mapper/StockFlowMapper.xml                                    │
│  ├── <select id="sumStockByItemIdAndWarehouseId" resultType="Integer">      │
│  │     SELECT COALESCE(SUM(change_amount), 0)                               │
│  │     FROM stock_flow                                                      │
│  │     WHERE item_id = #{itemId}                                            │
│  │     <if test="warehouseId != null">AND warehouse_id = #{warehouseId}</if>│
│  │   </select>                                                              │
│  │       ↓                                                                  │
│  📄 vo/StockVO.java                                                         │
│  ├── 字段: itemId, itemName, category, currentStock                         │
│  │       ↓                                                                  │
│  └── return Result.success(stockVOList)                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**库存计算逻辑**:

- 库存 = `SUM(stock_flow.change_amount)` WHERE `item_id = ?`
- 入库: `change_amount > 0`
- 出库: `change_amount < 0`

---

### ⑥ 采购订单创建 (Purchase Order Create)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Purchase/PurchaseEditView.vue                                     │
│  ├── 表单数据: form = { supplierId, items: [] }                              │
│  ├── 选择供应商: axios.get('/supplier') → supplierList                       │
│  ├── 添加商品明细: items.push({ itemId, quantity, unitPrice })               │
│  ├── 计算总金额: totalAmount = items.reduce((sum, i) => sum + i.quantity * i.unitPrice, 0)│
│  │       ↓                                                                  │
│  ├── 提交订单: axios.post('/purchase', form)                                 │
│  │       ↓                                                                  │
│  📄 utils/toast.js                                                          │
│  └── showSuccess('采购订单创建成功')                                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/PurchaseOrderController.java                                 │
│  ├── @PostMapping("/purchase")                                              │
│  ├── public Result<Void> create(@RequestBody PurchaseOrderDTO dto)          │
│  │       ↓                                                                  │
│  📄 dto/PurchaseOrderDTO.java                                               │
│  ├── 字段: supplierId, List<PurchaseOrderItemDTO> items                     │
│  │       ↓                                                                  │
│  📄 dto/PurchaseOrderItemDTO.java                                           │
│  ├── 字段: itemId, quantity, unitPrice                                      │
│  │       ↓                                                                  │
│  📄 service/Impl/PurchaseOrderServiceImpl.java                              │
│  ├── @Transactional                                                         │
│  ├── 1. 生成订单号: IdGenerator.generateOrderNo("PO")                        │
│  ├── 2. 计算总金额                                                           │
│  ├── 3. 创建 PurchaseOrder 实体                                              │
│  ├── 4. PurchaseOrderMapper.insert(order) → 获取 orderId                    │
│  ├── 5. 遍历 items:                                                          │
│  │   └── PurchaseOrderItemMapper.insert(item)                               │
│  │       ↓                                                                  │
│  📄 dao/PurchaseOrderMapper.java                                            │
│  ├── int insert(PurchaseOrder order)                                        │
│  │       ↓                                                                  │
│  📄 dao/PurchaseOrderItemMapper.java                                        │
│  ├── int insert(PurchaseOrderItem item)                                     │
│  │       ↓                                                                  │
│  📄 entity/PurchaseOrder.java                                               │
│  ├── 字段: id, orderNo, supplierId, totalAmount, status("CREATED"), createdAt│
│  │       ↓                                                                  │
│  📄 entity/PurchaseOrderItem.java                                           │
│  ├── 字段: id, orderId, itemId, quantity, unitPrice                         │
│  │       ↓                                                                  │
│  └── return Result.success(null, "创建成功")                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ⑦ 采购入库确认 (Purchase Inbound)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Purchase/PurchaseListView.vue                                     │
│  ├── 点击 "入库" 按钮                                                        │
│  ├── axios.put(`/purchase/${orderId}/inbound`)                              │
│  │       ↓                                                                  │
│  └── 刷新列表, 状态变为 "COMPLETED"                                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend) - 关键: 触发库存增加                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/PurchaseOrderController.java                                 │
│  ├── @PutMapping("/purchase/{id}/inbound")                                  │
│  │       ↓                                                                  │
│  📄 service/Impl/PurchaseOrderServiceImpl.java                              │
│  ├── @Transactional                                                         │
│  ├── 1. 查询订单: PurchaseOrderMapper.selectById(id)                         │
│  ├── 2. 判断状态: if (order.status != "CREATED") throw Exception            │
│  ├── 3. 查询订单明细: PurchaseOrderItemMapper.selectByOrderId(id)            │
│  ├── 4. 遍历明细, 调用 StockService.adjust():                                │
│  │   └── 为每个商品创建入库记录 (change_amount = +quantity)                   │
│  ├── 5. 更新订单状态: order.status = "COMPLETED"                             │
│  │   └── PurchaseOrderMapper.updateStatus(id, "COMPLETED")                  │
│  │       ↓                                                                  │
│  📄 service/Impl/StockServiceImpl.java                                      │
│  ├── adjust(StockAdjustDTO dto):                                            │
│  │   ├── 创建 StockFlow 实体                                                 │
│  │   ├── stockFlow.changeType = "PURCHASE_IN"                               │
│  │   ├── stockFlow.changeAmount = +quantity (正数表示入库)                    │
│  │   └── StockFlowMapper.insert(stockFlow)                                  │
│  │       ↓                                                                  │
│  📄 dao/StockFlowMapper.java                                                │
│  ├── int insert(StockFlow flow)                                             │
│  │       ↓                                                                  │
│  📄 entity/StockFlow.java                                                   │
│  ├── 字段: id, itemId, warehouseId, changeType, changeAmount, createdAt     │
│  │       ↓                                                                  │
│  📄 resources/mapper/StockFlowMapper.xml                                    │
│  ├── <insert id="insert">                                                   │
│  │     INSERT INTO stock_flow (item_id, warehouse_id, change_type, change_amount, created_at)│
│  │     VALUES (#{itemId}, #{warehouseId}, #{changeType}, #{changeAmount}, NOW())│
│  │   </insert>                                                              │
│  │       ↓                                                                  │
│  └── 库存增加完成                                                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**关键逻辑**: 入库时 `change_amount = +数量` (正数)，出库时 `change_amount = -数量` (负数)

---

### ⑧ 销售订单出库 (Sales Outbound)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Sales/SalesListView.vue                                           │
│  ├── 点击 "出库" 按钮                                                        │
│  ├── axios.put(`/sale/${orderId}/outbound`)                                 │
│  │       ↓                                                                  │
│  └── 刷新列表, 状态变为 "COMPLETED"                                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend) - 关键: 触发库存减少                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/SaleOrderController.java                                     │
│  ├── @PutMapping("/sale/{id}/outbound")                                     │
│  │       ↓                                                                  │
│  📄 service/Impl/SaleOrderServiceImpl.java                                  │
│  ├── @Transactional                                                         │
│  ├── 1. 查询订单: SaleOrderMapper.selectById(id)                             │
│  ├── 2. 判断状态: if (order.status != "CREATED") throw Exception            │
│  ├── 3. 查询订单明细: SaleOrderItemMapper.selectByOrderId(id)                │
│  ├── 4. 库存校验: 检查每个商品库存是否充足                                     │
│  │   └── if (currentStock < item.quantity) throw "库存不足"                  │
│  ├── 5. 遍历明细, 调用 StockService.adjust():                                │
│  │   └── 为每个商品创建出库记录 (change_amount = -quantity)                   │
│  ├── 6. 更新订单状态: order.status = "COMPLETED"                             │
│  │       ↓                                                                  │
│  📄 service/Impl/StockServiceImpl.java                                      │
│  ├── adjust(StockAdjustDTO dto):                                            │
│  │   ├── stockFlow.changeType = "SALE_OUT"                                  │
│  │   ├── stockFlow.changeAmount = -quantity (负数表示出库)                    │
│  │   └── StockFlowMapper.insert(stockFlow)                                  │
│  │       ↓                                                                  │
│  └── 库存减少完成                                                            │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ⑨ 客户/供应商管理 (Customer/Supplier CRUD)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 🔄 通用 CRUD 流程 (以客户为例)                                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  【查询列表】                                                                │
│  前端: CustomerListView.vue → axios.get('/customer')                        │
│  后端: CustomerController.list() → CustomerService.list()                   │
│        → CustomerMapper.selectAll() → List<CustomerVO>                      │
│                                                                             │
│  【新增】                                                                    │
│  前端: 弹窗表单 → axios.post('/customer', { name, contact, phone, address }) │
│  后端: CustomerController.add(@RequestBody CustomerDTO)                     │
│        → CustomerService.add() → CustomerMapper.insert()                    │
│                                                                             │
│  【编辑】                                                                    │
│  前端: 弹窗表单 → axios.put('/customer/' + id, form)                         │
│  后端: CustomerController.update(@PathVariable id, @RequestBody CustomerDTO)│
│        → CustomerService.update() → CustomerMapper.update()                 │
│                                                                             │
│  【删除】                                                                    │
│  前端: 确认弹窗 → axios.delete('/customer/' + id)                            │
│  后端: CustomerController.delete(@PathVariable id)                          │
│        → CustomerService.delete() → CustomerMapper.deleteById()             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**涉及文件**:

- 前端: `CustomerListView.vue`, `SupplierListView.vue`
- 后端: `CustomerController`, `CustomerService`, `CustomerServiceImpl`, `CustomerMapper`, `CustomerMapper.xml`
- 数据对象: `Customer.java`, `CustomerDTO.java`, `CustomerVO.java`

---

### ⑩ 文件上传 (Image Upload)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Item/ItemEditView.vue                                             │
│  ├── <input type="file" @change="handleImageUpload">                        │
│  ├── handleImageUpload(event):                                              │
│  │   ├── const file = event.target.files[0]                                 │
│  │   ├── const formData = new FormData()                                    │
│  │   ├── formData.append('file', file)                                      │
│  │   └── axios.post('/upload/image', formData, {                            │
│  │         headers: { 'Content-Type': 'multipart/form-data' }               │
│  │       })                                                                 │
│  │       ↓                                                                  │
│  ├── 响应: { url: '/uploads/xxx.jpg' }                                      │
│  └── form.imageUrl = response.url                                           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/FileUploadController.java                                    │
│  ├── @PostMapping("/upload/image")                                          │
│  ├── public Result<String> upload(@RequestParam("file") MultipartFile file) │
│  │       ↓                                                                  │
│  📄 utils/FileUtils.java                                                    │
│  ├── 1. 生成唯一文件名: UUID + 原始扩展名                                     │
│  ├── 2. 保存到本地目录: /uploads/xxx.jpg                                     │
│  │   └── file.transferTo(new File(uploadDir + fileName))                    │
│  ├── 3. 返回访问 URL: "/uploads/" + fileName                                 │
│  │       ↓                                                                  │
│  📄 config/WebConfig.java                                                   │
│  ├── 配置静态资源映射:                                                        │
│  │   registry.addResourceHandler("/uploads/**")                             │
│  │           .addResourceLocations("file:" + uploadDir)                     │
│  │       ↓                                                                  │
│  └── return Result.success(imageUrl)                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ⑪ 文件删除 (Image Delete)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Item/ItemEditView.vue                                             │
│  ├── 删除按钮: <button @click="handleRemoveImage">删除图片</button>          │
│  │       ↓                                                                  │
│  ├── handleRemoveImage():                                                   │
│  │   ├── 判断: if (!form.imageUrl) return                                   │
│  │   ├── 提取文件名: const fileName = form.imageUrl.split('/').pop()        │
│  │   │       ↓                                                              │
│  │   ├── axios.delete('/upload/image', {                                    │
│  │   │     params: { fileName: fileName }                                   │
│  │   │   })                                                                 │
│  │   │       ↓                                                              │
│  │   ├── 清空表单: form.imageUrl = ''                                        │
│  │   └── 清空预览: imagePreview.value = null                                 │
│  │       ↓                                                                  │
│  📄 utils/toast.js                                                          │
│  └── showSuccess('图片删除成功')                                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/FileUploadController.java                                    │
│  ├── @DeleteMapping("/upload/image")                                        │
│  ├── public Result<Void> delete(@RequestParam("fileName") String fileName)  │
│  │       ↓                                                                  │
│  ├── 参数校验:                                                               │
│  │   ├── if (fileName == null || fileName.isEmpty()) → 返回错误              │
│  │   └── 防止路径遍历攻击: fileName.contains("..") → 返回错误                 │
│  │       ↓                                                                  │
│  📄 utils/FileUtils.java                                                    │
│  ├── deleteFile(String fileName):                                           │
│  │   ├── 1. 构建完整路径: File file = new File(uploadDir + fileName)         │
│  │   ├── 2. 判断文件是否存在: if (!file.exists()) → 返回文件不存在            │
│  │   ├── 3. 执行删除: boolean deleted = file.delete()                        │
│  │   └── 4. 判断删除结果:                                                    │
│  │       ├── if (deleted) → 删除成功                                         │
│  │       └── else → 删除失败, 可能权限问题                                    │
│  │       ↓                                                                  │
│  📄 common/Result.java                                                      │
│  └── return Result.success(null, "删除成功")                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**数据流向**: `ItemEditView.vue (删除按钮)` → `axios.delete('/upload/image')` → `FileUploadController.delete()` → `FileUtils.deleteFile()` → `磁盘文件系统删除` → `Result.success()`

**涉及文件**:

|   层级   | 文件                        | 作用                   |
| :------: | :-------------------------- | :--------------------- |
| 前端视图 | `ItemEditView.vue`          | 删除按钮、清空预览     |
| 前端工具 | `toast.js`                  | 显示成功/失败提示      |
|  控制器  | `FileUploadController.java` | 接收删除请求、参数校验 |
|  工具类  | `FileUtils.java`            | 执行物理文件删除       |
| 响应封装 | `Result.java`               | 统一返回格式           |

**安全考虑**:

- ⚠️ **路径遍历防护**: 校验 `fileName` 不包含 `..`，防止删除系统文件
- ⚠️ **文件存在性校验**: 删除前检查文件是否存在
- ⚠️ **删除结果校验**: 检查 `file.delete()` 返回值，处理权限问题

---

---

### ⑫ 用户资料更新 (User Profile Update)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Profile/UserProfileView.vue                                       │
│  ├── 表单数据: profileForm = { email, phone }                               │
│  ├── 头像上传: handleFileChange() → selectedFile                            │
│  ├── 提交修改: updateProfile()                                              │
│  │   ├── 1. 如果有新头像:                                                    │
│  │   │   └── axios.post('/upload/avatar', formData) → 获取 avatarUrl        │
│  │   ├── 2. 构造更新数据: { email, phone, avatar: avatarUrl }                │
│  │   └── 3. axios.put('/users/profile', updateData)                         │
│  │       ↓                                                                  │
│  ├── 更新 Store: userStore.user = { ...userStore.user, ...updateData }      │
│  └── 提示成功: showSuccess('个人信息更新成功')                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/UserProfileController.java                                   │
│  ├── @PutMapping("/profile")                                                │
│  ├── public Result<User> updateProfile(@RequestBody Map<String, String> body)│
│  │       ↓                                                                  │
│  ├── 1. 获取当前用户: userId = request.getAttribute("userId")                │
│  ├── 2. 查询数据库: User user = userMapper.selectById(userId)                │
│  ├── 3. 更新字段:                                                            │
│  │   ├── if (body.email) user.setEmail(body.email)                          │
│  │   ├── if (body.phone) user.setPhone(body.phone)                          │
│  │   └── if (body.avatar)                                                   │
│  │       ├── deleteOldAvatar(user.getAvatar()) // 删除旧头像文件              │
│  │       └── user.setAvatar(body.avatar)                                    │
│  ├── 4. 保存更新: userMapper.update(user)                                    │
│  │       ↓                                                                  │
│  📄 dao/UserMapper.java                                                     │
│  ├── int update(User user)                                                  │
│  │       ↓                                                                  │
│  └── return Result.success(user, "资料更新成功")                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ⑬ 管理员仪表盘 (Admin Dashboard)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📱 前端层 (Frontend)                                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 views/Admin/AdminDashboardView.vue                                      │
│  ├── onMounted() → fetchDashboardData()                                     │
│  ├── axios.get('/admin/dashboard')                                          │
│  │       ↓                                                                  │
│  ├── 数据渲染:                                                               │
│  │   ├── 统计卡片: 用户总数、活跃用户、今日操作、商品总数                      │
│  │   ├── 异常提醒: 低库存商品数、缺货商品数                                   │
│  │   └── 系统信息: 版本号                                                    │
│  │                                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ⚙️ 后端层 (Backend)                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  📄 controller/AdminController.java                                         │
│  ├── @GetMapping("/dashboard")                                              │
│  ├── @AdminRequired // 权限校验                                              │
│  ├── public Result<AdminDashboardVO> getDashboard()                         │
│  │       ↓                                                                  │
│  ├── 1. 用户统计: userService.getAllUsers()                                  │
│  ├── 2. 操作统计: operationLogService.countToday()                           │
│  ├── 3. 商品统计: productService.getAllProducts()                            │
│  ├── 4. 库存分析:                                                            │
│  │   ├── 获取阈值: systemSettingService.getIntValue("low_stock_threshold")   │
│  │   ├── 获取库存: stockService.getStockSnapshot(null)                       │
│  │   └── 计算 lowStockCount, outOfStockCount                                 │
│  │       ↓                                                                  │
│  📄 vo/AdminDashboardVO.java                                                │
│  ├── 字段: totalUsers, activeUsers, totalOperationsToday, totalProducts,    │
│  │         lowStockProducts, outOfStockProducts, systemVersion              │
│  │       ↓                                                                  │
│  └── return Result.success(dashboardVO)                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📄 许可证 (License)

本项目采用 [AGPL-3.0](LICENSE) 许可证。

Copyright © 2026-Present [yeflyleaf](https://github.com/yeflyleaf). All Rights Reserved.

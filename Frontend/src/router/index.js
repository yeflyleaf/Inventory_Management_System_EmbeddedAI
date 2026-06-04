import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import MainLayout from '../views/Layout/MainLayout.vue'
import LoginView from '../views/Login/LoginView.vue'

const router = createRouter({
  // 路由模式选择：
  // 1. Electron/Android 模式下使用 Hash 模式 (createWebHashHistory)，避免 file:// 协议下的路由问题
  // 2. Web 模式下使用 History 模式 (createWebHistory)，URL 更美观，SEO 友好
  history: (import.meta.env.MODE === 'electron' || import.meta.env.MODE === 'android') ? createWebHashHistory() : createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: '登录',
      component: LoginView,
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard',
        },
        {
          path: 'dashboard',
          name: '仪表盘',
          component: () => import('../views/Dashboard/DashboardView.vue'),
        },
        // 商品
        {
          path: 'items',
          name: '商品列表',
          component: () => import('../views/Item/ItemListView.vue'),
        },
        {
          path: 'items/add',
          name: '新增商品',
          component: () => import('../views/Item/ItemEditView.vue'),
        },
        {
          path: 'items/edit/:id',
          name: '编辑商品',
          component: () => import('../views/Item/ItemEditView.vue'),
        },
        {
          path: 'stock',
          name: '库存列表',
          component: () => import('../views/Item/StockListView.vue'),
        },
        {
          path: 'stock/flow',
          name: '库存流水',
          component: () => import('../views/Item/StockFlowView.vue'),
        },
        {
          path: 'stock/adjust',
          name: '库存调整',
          component: () => import('../views/Item/StockAdjustView.vue'),
        },
        // 采购
        {
          path: 'purchase',
          name: '采购订单',
          component: () => import('../views/Purchase/PurchaseListView.vue'),
        },
        {
          path: 'purchase/add',
          name: '新建采购',
          component: () => import('../views/Purchase/PurchaseEditView.vue'),
        },
        {
          path: 'purchase/inbound',
          name: '采购入库',
          component: () => import('../views/Purchase/PurchaseInboundView.vue'),
        },
        // 销售
        {
          path: 'sales',
          name: '销售订单',
          component: () => import('../views/Sales/SalesListView.vue'),
        },
        {
          path: 'sales/add',
          name: '新建销售',
          component: () => import('../views/Sales/SalesEditView.vue'),
        },
        {
          path: 'sales/outbound',
          name: '销售出库',
          component: () => import('../views/Sales/SalesOutboundView.vue'),
        },
        // 基础信息
        {
          path: 'customers',
          name: '客户管理',
          component: () => import('../views/Basic/CustomerListView.vue'),
        },
        {
          path: 'suppliers',
          name: '供应商管理',
          component: () => import('../views/Basic/SupplierListView.vue'),
        },
        // 个人中心
        {
          path: 'profile',
          name: '个人信息',
          component: () => import('../views/Profile/UserProfileView.vue'),
        },
      ],
    },
    // ==================== 管理员路由 ====================
    {
      path: '/admin',
      component: () => import('../views/Admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard',
        },
        {
          path: 'dashboard',
          name: '管理控制台',
          component: () => import('../views/Admin/AdminDashboardView.vue'),
        },
        {
          path: 'users',
          name: '用户管理',
          component: () => import('../views/Admin/UserManageView.vue'),
        },
        {
          path: 'logs',
          name: '操作日志',
          component: () => import('../views/Admin/OperationLogsView.vue'),
        },
        {
          path: 'settings',
          name: '系统设置',
          component: () => import('../views/Admin/SystemSettingsView.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: '页面未找到',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  // 已登录用户访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    if (user.role === 'ADMIN') {
      next('/admin/dashboard')
    } else {
      next('/dashboard')
    }
    return
  }

  // 检查登录状态
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 检查管理员权限 - 非管理员不能访问 /admin 路由
  if (to.meta.requiresAdmin) {
    if (!user.role || user.role !== 'ADMIN') {
      // 非管理员尝试访问管理页面，重定向到普通仪表盘
      console.warn('权限不足：非管理员尝试访问管理页面')
      next('/dashboard')
      return
    }
  }

  next()
})

export default router

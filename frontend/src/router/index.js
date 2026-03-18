import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/auth'
    },
    {
      path: '/auth',
      name: 'Auth',
      component: () => import('../views/Auth.vue'),
      meta: { title: 'E-ship 船易达 - 智慧航运平台' }
    },
    {
      path: '/login',
      redirect: '/auth'
    },
    {
      path: '/register',
      redirect: '/auth'
    },
    {
      path: '/owner',
      name: 'Owner',
      component: () => import('../views/owner/Index.vue'),
      meta: {
        requiresAuth: true,
        role: 'SHIP_OWNER',
        title: '船东中心 - E-ship'
      },
      children: [
        {
          path: '',
          redirect: '/owner/dashboard'
        },
        {
          path: 'dashboard',
          name: 'OwnerDashboard',
          component: () => import('../views/owner/Dashboard.vue'),
          meta: { title: '数据统计 - E-ship' }
        },
        {
          path: 'ships',
          name: 'OwnerShips',
          component: () => import('../views/owner/Ships.vue'),
          meta: { title: '我的船舶 - E-ship' }
        },
        {
          path: 'contracts',
          name: 'OwnerContracts',
          component: () => import('../views/owner/Contracts.vue'),
          meta: { title: '合约管理 - E-ship' }
        },
        {
          path: 'inbox',
          name: 'ContractInbox',
          component: () => import('../views/owner/Inbox.vue'),
          meta: { title: '合约信箱 - E-ship' }
        }
      ]
    },
    {
      path: '/renter',
      name: 'Renter',
      component: () => import('../views/renter/Index.vue'),
      meta: {
        requiresAuth: true,
        role: 'RENTER',
        title: '租家中心 - E-ship'
      },
      children: [
        {
          path: '',
          redirect: '/renter/dashboard'
        },
        {
          path: 'dashboard',
          name: 'RenterDashboard',
          component: () => import('../views/renter/Dashboard.vue'),
          meta: { title: '数据统计 - E-ship' }
        },
        {
          path: 'search',
          name: 'SearchShips',
          component: () => import('../views/renter/Search.vue'),
          meta: { title: '搜索船舶 - E-ship' }
        },
        {
          path: 'contracts',
          name: 'RenterContracts',
          component: () => import('../views/renter/Contracts.vue'),
          meta: { title: '我的合约 - E-ship' }
        },
        {
          path: 'favorites',
          name: 'Favorites',
          component: () => import('../views/renter/Favorites.vue'),
          meta: { title: '我的收藏 - E-ship' }
        }
      ]
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('../views/admin/Index.vue'),
      meta: {
        requiresAuth: true,
        role: 'ADMIN',
        title: '管理员中心 - E-ship'
      }
    },
    {
      path: '/ship/:id',
      name: 'ShipDetail',
      component: () => import('../views/ShipDetail.vue'),
      meta: {
        requiresAuth: true,
        title: '船舶详情 - E-ship'
      }
    },
    {
      path: '/contract/:id',
      name: 'ContractDetail',
      component: () => import('../views/ContractDetail.vue'),
      meta: {
        requiresAuth: true,
        title: '合约详情 - E-ship'
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/NotFound.vue'),
      meta: { title: '页面不存在 - E-ship' }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || 'E-ship 船舶租赁平台'

  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('role')

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      next('/auth')
      return
    }

    // 检查角色权限
    if (to.meta.role && to.meta.role !== userRole) {
      next('/auth')
      return
    }
  }

  // 已登录用户访问登录/注册页，重定向到对应首页
  if (to.path === '/auth' && token) {
    if (userRole === 'SHIP_OWNER') {
      next('/owner/dashboard')
    } else if (userRole === 'RENTER') {
      next('/renter/dashboard')
    } else if (userRole === 'ADMIN') {
      next('/admin')
    } else {
      next()
    }
    return
  }

  next()
})

export default router

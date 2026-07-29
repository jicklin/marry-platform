import { createRouter, createWebHashHistory } from 'vue-router'

import Layout from '@/layout/index.vue'

const Login = () => import('@/views/login/index.vue')
const NotFound = () => import('@/views/error/404.vue')
const Dashboard = () => import('@/views/dashboard/index.vue')

const staticRoutes = [
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  { path: '/404', name: 'NotFound', component: NotFound, meta: { title: '404' } },
  // Register the root Layout statically so `/` always resolves, even
  // before dynamic permission routes have been loaded by the guard.
  // `/dashboard` is a child so it renders inside the Layout's <router-view>
  // (sidebar/header stay visible on the home page).
  {
    path: '/',
    name: 'Root',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '首页' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: staticRoutes
})

router.beforeEach(async (to, _from, next) => {
  document.title = (to.meta?.title as string) || 'marry-platform'
  next()
})

export default router
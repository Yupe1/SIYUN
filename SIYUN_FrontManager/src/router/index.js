import { createRouter, createWebHistory } from 'vue-router'
import { firstAllowedPath } from '@/menu'
import { useAuthStore } from '@/stores/auth'

const ResourcePage = () => import('@/views/ResourcePage.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '系统首页' } },
        { path: 'courses/list', component: ResourcePage, meta: { resource: 'courses', title: '课程列表' } },
        { path: 'courses/create', component: () => import('@/views/CourseCreateView.vue'), meta: { title: '添加课程' } },
        { path: 'courses/audit', component: ResourcePage, meta: { resource: 'courseAudit', title: '课程审核' } },
        { path: 'courses/comments', component: ResourcePage, meta: { resource: 'courseComments', title: '课程评论' } },
        { path: 'courses/categories', component: ResourcePage, meta: { resource: 'courseCategories', title: '课程分类' } },
        { path: 'moments/list', component: ResourcePage, meta: { resource: 'moments', title: '微圈管理' } },
        { path: 'moments/create', component: ResourcePage, meta: { resource: 'momentCreate', title: '添加微圈' } },
        { path: 'moments/audit', component: ResourcePage, meta: { resource: 'momentAudit', title: '微圈审核' } },
        { path: 'goods/list', component: ResourcePage, meta: { resource: 'goods', title: '商品列表' } },
        { path: 'goods/create', component: ResourcePage, meta: { resource: 'goodsCreate', title: '添加商品' } },
        { path: 'goods/audit', component: ResourcePage, meta: { resource: 'goodsAudit', title: '商品审核' } },
        { path: 'goods/categories', component: ResourcePage, meta: { resource: 'goodsCategories', title: '商品分类' } },
        { path: 'orders/goods', component: ResourcePage, meta: { resource: 'goodsOrders', title: '商品订单' } },
        { path: 'orders/courses', component: ResourcePage, meta: { resource: 'courseOrders', title: '课程订单' } },
        { path: 'orders/returns', component: ResourcePage, meta: { resource: 'returnOrders', title: '退货列表' } },
        { path: 'marketing/coupons', component: ResourcePage, meta: { resource: 'coupons', title: '优惠券列表' } },
        { path: 'marketing/coupon-users', component: ResourcePage, meta: { resource: 'couponUsers', title: '优惠券查询' } },
        { path: 'marketing/ads', component: ResourcePage, meta: { resource: 'ads', title: '轮播图列表' } },
        { path: 'audit-stat/audit-logs', component: ResourcePage, meta: { resource: 'auditLogs', title: '审核日志' } },
        { path: 'audit-stat/comments', component: ResourcePage, meta: { resource: 'comments', title: '评论管理' } },
        { path: 'audit-stat/statistics', component: () => import('@/views/StatisticsView.vue'), meta: { title: '运营统计' } },
        { path: 'system/roles', component: () => import('@/views/RolePermissionView.vue'), meta: { title: '角色管理' } },
        { path: 'system/permissions', component: ResourcePage, meta: { resource: 'permissions', title: '权限菜单' } },
        { path: 'hr/front-users', component: ResourcePage, meta: { resource: 'frontUsers', title: '前台用户' } },
        { path: 'hr/staff-users', component: ResourcePage, meta: { resource: 'staffUsers', title: '教师人员' } },
        { path: 'hr/depts', component: ResourcePage, meta: { resource: 'depts', title: '部门管理' } },
        { path: 'hr/position-applies', component: ResourcePage, meta: { resource: 'positionApplies', title: '作品申请' } },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return true
  if (!auth.isLogin) return '/login'
  const validSession = await auth.ensureSession()
  if (!validSession) return '/login'
  if (to.path === '/') return firstAllowedPath(auth)
  return true
})

export default router

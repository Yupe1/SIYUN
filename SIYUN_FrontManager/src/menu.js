export const menuGroups = [
  {
    key: 'dashboard',
    title: '首页',
    icon: 'HomeFilled',
    base: '/dashboard',
    children: [{ title: '系统首页', path: '/dashboard', perms: ['admin:dashboard:view'] }],
  },
  {
    key: 'course',
    title: '课程',
    icon: 'Reading',
    base: '/courses',
    children: [
      { title: '课程列表', path: '/courses/list', perms: ['admin:course:list'] },
      { title: '添加课程', path: '/courses/create', perms: ['admin:course:add'] },
      { title: '课程审核', path: '/courses/audit', perms: ['admin:course:audit'] },
      { title: '课程评论', path: '/courses/comments', perms: ['admin:course:comment'] },
      { title: '课程分类', path: '/courses/categories', perms: ['admin:course:category'] },
    ],
  },
  {
    key: 'moment',
    title: '微圈',
    icon: 'ChatDotRound',
    base: '/moments',
    children: [
      { title: '微圈管理', path: '/moments/list', perms: ['admin:moment:list'] },
      { title: '添加微圈', path: '/moments/create', perms: ['admin:moment:add'] },
      { title: '微圈审核', path: '/moments/audit', perms: ['admin:moment:audit'] },
    ],
  },
  {
    key: 'goods',
    title: '商品',
    icon: 'Goods',
    base: '/goods',
    children: [
      { title: '商品列表', path: '/goods/list', perms: ['admin:goods:list'] },
      { title: '添加商品', path: '/goods/create', perms: ['admin:goods:add'] },
      { title: '商品审核', path: '/goods/audit', perms: ['admin:goods:audit'] },
      { title: '商品分类', path: '/goods/categories', perms: ['admin:goods:category'] },
    ],
  },
  {
    key: 'order',
    title: '订单',
    icon: 'Tickets',
    base: '/orders',
    children: [
      { title: '商品订单', path: '/orders/goods', perms: ['admin:order:list'] },
      { title: '课程订单', path: '/orders/courses', perms: ['admin:order:list'] },
      { title: '退货列表', path: '/orders/returns', perms: ['admin:order:return'] },
    ],
  },
  {
    key: 'marketing',
    title: '运营',
    icon: 'TrendCharts',
    base: '/marketing',
    children: [
      { title: '优惠券列表', path: '/marketing/coupons', perms: ['admin:coupon:list'] },
      { title: '优惠券查询', path: '/marketing/coupon-users', perms: ['admin:coupon:user'] },
      { title: '轮播图列表', path: '/marketing/ads', perms: ['admin:ad:list'] },
    ],
  },
  {
    key: 'audit',
    title: '审核',
    icon: 'Checked',
    base: '/audit-stat',
    children: [
      { title: '审核日志', path: '/audit-stat/audit-logs', perms: ['admin:audit:log'] },
      { title: '评论管理', path: '/audit-stat/comments', perms: ['admin:comment:list'] },
      { title: '运营统计', path: '/audit-stat/statistics', perms: ['admin:stats:view'] },
    ],
  },
  {
    key: 'system',
    title: '权限',
    icon: 'Lock',
    base: '/system',
    children: [
      { title: '角色管理', path: '/system/roles', perms: ['admin:role:list'] },
      { title: '权限菜单', path: '/system/permissions', perms: ['admin:permission:list'] },
    ],
  },
  {
    key: 'hr',
    title: '人事',
    icon: 'User',
    base: '/hr',
    children: [
      { title: '前台用户', path: '/hr/front-users', perms: ['admin:user:list'] },
      { title: '教师人员', path: '/hr/staff-users', perms: ['admin:staff:list'] },
      { title: '部门管理', path: '/hr/depts', perms: ['admin:dept:list'] },
      { title: '作品申请', path: '/hr/position-applies', perms: ['admin:apply:list'] },
    ],
  },
]

export function firstAllowedPath(auth) {
  for (const group of menuGroups) {
    const child = group.children.find((item) => auth.hasAny(item.perms))
    if (child) return child.path
  }
  return '/dashboard'
}

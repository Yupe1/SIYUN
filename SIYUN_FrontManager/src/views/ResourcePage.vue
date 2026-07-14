<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Present, Search } from '@element-plus/icons-vue'
import http from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
const filters = reactive({})
const dialogVisible = ref(false)
const auditVisible = ref(false)
const grantVisible = ref(false)
const statusVisible = ref(false)
const dialogMode = ref('create')
const form = ref({})
const activeRow = ref(null)
const auditForm = reactive({ auditResult: 1, statusShelf: 1, feedbackDetail: '', remark: '' })
const grantForm = reactive({ couponId: null, userId: null })
const statusForm = reactive({ status: null, deliverySn: '', unlockReason: '' })

const shelfOptions = [
  { label: '下架/下线', value: 0 },
  { label: '上架/上线', value: 1 },
]
const goodsStatusOptions = [
  { label: '已删除', value: 0 },
  { label: '下架', value: 1 },
  { label: '上架', value: 2 },
]
const auditStatusOptions = [
  { label: '未审核', value: 0 },
  { label: '审核中', value: 1 },
  { label: '审核失败', value: 2 },
  { label: '通过', value: 3 },
]
const momentStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '未提交', value: 1 },
  { label: '已提交', value: 2 },
  { label: '已通过', value: 3 },
  { label: '退回', value: 4 },
]
const orderStatusOptions = [
  { label: '待付款', value: 0 },
  { label: '已付款', value: 1 },
  { label: '待发货', value: 2 },
  { label: '已发货', value: 3 },
  { label: '已签收', value: 4 },
  { label: '退货申请', value: 5 },
  { label: '退货中', value: 6 },
  { label: '已退货', value: 7 },
  { label: '取消交易', value: 8 },
]
const userStatusOptions = [
  { label: '正常', value: 0 },
  { label: '禁言', value: 1 },
  { label: '封停', value: 2 },
  { label: '注销', value: 3 },
]
const applyStatusOptions = [
  { label: '申请中', value: 0 },
  { label: '审核中', value: 1 },
  { label: '已通过', value: 2 },
  { label: '已驳回', value: 3 },
]

const commonCourseFields = [
  { prop: 'teacherId', label: '讲课教师ID', type: 'text' },
  { prop: 'cateId', label: '分类ID', type: 'text' },
  { prop: 'title', label: '课程名称' },
  { prop: 'intro', label: '介绍' },
  { prop: 'priceOriginal', label: '售价', type: 'text', append: '元' },
  { prop: 'keywords', label: '搜索关键字' },
  { prop: 'coverUrl', label: '课程图片' },
  { prop: 'videoUrl', label: '视频目录/源文件' },
  { prop: 'detailDesc', label: '内容', type: 'textarea' },
  { prop: 'duration', label: '时长', type: 'text', append: 'min' },
  { prop: 'statusShelf', label: '上下架', type: 'select', options: shelfOptions },
]

const configs = {
  courses: {
    endpoint: '/api/admin/courses',
    columns: [
      ['id', '编号', 80],
      ['title', '名称', 180],
      ['teacherId', '教师ID', 90],
      ['cateId', '分类', 80],
      ['priceOriginal', '价格', 100],
      ['statusShelf', '状态', 90, shelfOptions],
      ['statusAudit', '审核状态', 110, auditStatusOptions],
      ['episodeNum', '集数', 80],
      ['createTime', '创建时间', 170],
    ],
    filters: [
      { prop: 'keyword', label: '名称/关键字' },
      { prop: 'statusAudit', label: '审核状态', type: 'select', options: auditStatusOptions },
      { prop: 'statusShelf', label: '上下架', type: 'select', options: shelfOptions },
    ],
    fields: commonCourseFields,
    addPerm: 'admin:course:add',
    createPath: '/courses/create',
    updatePerm: 'admin:course:update',
    deletePerm: 'admin:course:delete',
    detailPerm: 'admin:course:detail',
  },
  courseCreate: null,
  courseAudit: null,
  courseComments: {
    endpoint: '/api/admin/course-comments',
    columns: [
      ['id', '编号', 80],
      ['entityId', '课程ID', 100],
      ['userId', '用户ID', 100],
      ['content', '评价内容', 280],
      ['countLike', '点赞', 80],
      ['countReply', '回复', 80],
      ['createTime', '评价时间', 170],
    ],
    filters: [
      { prop: 'courseId', label: '课程ID', type: 'number' },
      { prop: 'keyword', label: '评价内容' },
    ],
    deleteEndpoint: (row) => `/api/admin/comments/${row.id}`,
    deletePerm: 'admin:comment:delete',
    noAdd: true,
  },
  courseCategories: {
    endpoint: '/api/admin/course-categories',
    dataKey: 'categories',
    noPage: true,
    columns: [
      ['id', '编号', 80],
      ['cateName', '分类名称', 180],
      ['parentId', '父级ID', 100],
      ['sortNum', '排序', 90],
      ['createTime', '创建时间', 170],
    ],
    fields: [
      { prop: 'cateName', label: '分类名称' },
      { prop: 'parentId', label: '父级ID', type: 'number' },
      { prop: 'sortNum', label: '排序', type: 'number' },
    ],
    addPerm: 'admin:course:category:add',
    updatePerm: 'admin:course:category:update',
    deletePerm: 'admin:course:category:delete',
  },
  moments: {
    endpoint: '/api/admin/moments',
    columns: [
      ['id', '编号', 90],
      ['title', '标题', 220],
      ['authorId', '用户ID', 90],
      ['countLike', '点赞量', 90],
      ['countCollect', '收藏量', 90],
      ['countView', '查看量', 90],
      ['sortNum', '排序', 80],
      ['status', '审核状态', 100, momentStatusOptions],
      ['createTime', '创建时间', 170],
    ],
    filters: [
      { prop: 'keyword', label: '标题/关键字' },
      { prop: 'status', label: '状态', type: 'select', options: momentStatusOptions },
    ],
    fields: [
      { prop: 'authorId', label: '用户ID', type: 'number' },
      { prop: 'title', label: '标题' },
      { prop: 'keywords', label: '关键字' },
      { prop: 'coverUrl', label: '封面' },
      { prop: 'content', label: '内容', type: 'textarea' },
      { prop: 'sortNum', label: '排序', type: 'number' },
      { prop: 'statusShow', label: '显示状态', type: 'select', options: shelfOptions },
      { prop: 'status', label: '审核状态', type: 'select', options: momentStatusOptions },
    ],
    addPerm: 'admin:moment:add',
    updatePerm: 'admin:moment:update',
    deletePerm: 'admin:moment:delete',
  },
  momentCreate: null,
  momentAudit: null,
  goods: {
    endpoint: '/api/admin/goods',
    columns: [
      ['id', '编号', 80],
      ['goodsName', '商品名称', 200],
      ['cateId', '分类', 80],
      ['priceOriginal', '价格', 100],
      ['status', '状态', 90, goodsStatusOptions],
      ['recommendStatus', '推荐', 80],
      ['createTime', '创建时间', 170],
    ],
    filters: [
      { prop: 'keyword', label: '名称/关键字' },
      { prop: 'status', label: '状态', type: 'select', options: goodsStatusOptions },
    ],
    fields: [
      { prop: 'cateId', label: '分类ID', type: 'number' },
      { prop: 'goodsName', label: '商品名称' },
      { prop: 'keywords', label: '关键字' },
      { prop: 'mainPicUrl', label: '商品主图' },
      { prop: 'priceOriginal', label: '价格', type: 'number' },
      { prop: 'intro', label: '简介' },
      { prop: 'serviceTags', label: '服务标签' },
      { prop: 'recommendStatus', label: '推荐状态', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: goodsStatusOptions },
    ],
    addPerm: 'admin:goods:add',
    updatePerm: 'admin:goods:update',
    deletePerm: 'admin:goods:delete',
  },
  goodsCreate: null,
  goodsAudit: null,
  goodsCategories: {
    endpoint: '/api/admin/goods-categories',
    dataKey: 'categories',
    noPage: true,
    columns: [
      ['id', '编号', 80],
      ['cateName', '分类名称', 180],
      ['parentId', '父级ID', 100],
      ['sortNum', '排序', 90],
      ['status', '状态', 80],
      ['createTime', '创建时间', 170],
    ],
    fields: [
      { prop: 'cateName', label: '分类名称' },
      { prop: 'parentId', label: '父级ID', type: 'number' },
      { prop: 'sortNum', label: '排序', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: shelfOptions },
    ],
    addPerm: 'admin:goods:category:add',
    updatePerm: 'admin:goods:category:update',
    deletePerm: 'admin:goods:category:delete',
  },
  goodsOrders: null,
  courseOrders: null,
  returnOrders: null,
  coupons: {
    endpoint: '/api/admin/coupons',
    columns: [
      ['id', '编号', 80],
      ['couponSn', '优惠券编号', 150],
      ['couponName', '名称', 160],
      ['amount', '金额', 90],
      ['applyType', '适用类型', 90],
      ['statusShelf', '上下线', 90, shelfOptions],
      ['startTime', '开始时间', 170],
      ['endTime', '结束时间', 170],
    ],
    filters: [
      { prop: 'keyword', label: '名称/编号' },
      { prop: 'statusShelf', label: '上下线', type: 'select', options: shelfOptions },
    ],
    fields: [
      { prop: 'couponSn', label: '优惠券编号' },
      { prop: 'couponName', label: '优惠券名称' },
      { prop: 'amount', label: '面值金额', type: 'number' },
      { prop: 'imgUrl', label: '图片路径' },
      { prop: 'startTime', label: '开始时间', type: 'datetime' },
      { prop: 'endTime', label: '结束时间', type: 'datetime' },
      { prop: 'statusShelf', label: '上下线', type: 'select', options: shelfOptions },
      { prop: 'issueType', label: '发放类型', type: 'number' },
      { prop: 'applyType', label: '适用类型', type: 'number' },
    ],
    addPerm: 'admin:coupon:add',
    updatePerm: 'admin:coupon:update',
    deletePerm: 'admin:coupon:delete',
  },
  couponUsers: {
    endpoint: '/api/admin/coupon-users',
    columns: [
      ['id', '券ID', 80],
      ['couponSn', '编号', 150],
      ['couponName', '名称', 160],
      ['amount', '金额', 90],
      ['userId', '用户ID', 90],
      ['goodsId', '商品/课程ID', 120],
      ['startTime', '开始时间', 170],
      ['endTime', '结束时间', 170],
    ],
    filters: [{ prop: 'userId', label: '用户ID', type: 'number' }],
    noAdd: true,
    grant: true,
  },
  ads: {
    endpoint: '/api/admin/ads',
    columns: [
      ['id', '编号', 80],
      ['title', '标题', 180],
      ['positionType', '位置', 80],
      ['statusShow', '显示', 80, shelfOptions],
      ['sortNum', '排序', 80],
      ['startTime', '开始时间', 170],
      ['endTime', '结束时间', 170],
    ],
    fields: [
      { prop: 'title', label: '标题' },
      { prop: 'picUrl', label: '图片路径' },
      { prop: 'positionType', label: '位置类型', type: 'number' },
      { prop: 'startTime', label: '开始时间', type: 'datetime' },
      { prop: 'endTime', label: '结束时间', type: 'datetime' },
      { prop: 'statusShow', label: '显示状态', type: 'select', options: shelfOptions },
      { prop: 'sortNum', label: '排序', type: 'number' },
      { prop: 'intro', label: '描述' },
    ],
    addPerm: 'admin:ad:add',
    updatePerm: 'admin:ad:update',
    deletePerm: 'admin:ad:delete',
    topPerm: 'admin:ad:top',
  },
  auditLogs: {
    endpoint: '/api/admin/audit-logs',
    columns: [
      ['id', '编号', 80],
      ['entityId', '实体ID', 100],
      ['entityType', '类型', 80],
      ['applicantId', '申请人', 90],
      ['auditorId', '审核人', 90],
      ['auditResult', '结果', 80],
      ['feedbackDetail', '反馈', 240],
      ['auditTime', '审核时间', 170],
    ],
    noAdd: true,
  },
  comments: null,
  permissions: {
    endpoint: '/api/admin/permissions',
    dataKey: 'permissions',
    noPage: true,
    columns: [
      ['id', '编号', 80],
      ['name', '权限名称', 160],
      ['parentId', '父级ID', 90],
      ['path', '前端路径', 200],
      ['perms', '权限标识', 220],
      ['status', '状态', 80],
    ],
    fields: [
      { prop: 'name', label: '权限名称' },
      { prop: 'permDesc', label: '权限说明' },
      { prop: 'parentId', label: '父级ID', type: 'number' },
      { prop: 'path', label: '前端路径' },
      { prop: 'perms', label: '权限标识' },
      { prop: 'status', label: '状态', type: 'select', options: shelfOptions },
    ],
    addPerm: 'admin:permission:add',
    updatePerm: 'admin:permission:update',
    deletePerm: 'admin:permission:delete',
  },
  frontUsers: {
    endpoint: '/api/admin/front-users',
    columns: [
      ['id', '编号', 80],
      ['nickName', '昵称', 140],
      ['stuTel', '账号', 130],
      ['studyDuration', '学习时长', 110],
      ['status', '状态', 90, userStatusOptions],
      ['chinaId', '身份证', 180],
      ['email', '邮箱', 170],
    ],
    filters: [
      { prop: 'keyword', label: '昵称/账号' },
      { prop: 'status', label: '状态', type: 'select', options: userStatusOptions },
    ],
    fields: [
      { prop: 'nickName', label: '昵称' },
      { prop: 'stuTel', label: '账号' },
      { prop: 'chinaId', label: '身份证' },
      { prop: 'status', label: '状态', type: 'select', options: userStatusOptions },
      { prop: 'gender', label: '性别', type: 'number' },
      { prop: 'email', label: '邮箱' },
      { prop: 'studyDuration', label: '学习时长', type: 'number' },
      { prop: 'jobOrient', label: '职业方向' },
      { prop: 'remark', label: '备注' },
    ],
    updatePerm: 'admin:user:update',
  },
  staffUsers: {
    endpoint: '/api/admin/staff-users',
    columns: [
      ['id', '编号', 80],
      ['name', '姓名', 120],
      ['tel', '手机号', 130],
      ['deptId', '部门', 80],
      ['status', '状态', 90, userStatusOptions],
      ['level', '级别', 80],
      ['salary', '薪资', 100],
    ],
    filters: [
      { prop: 'keyword', label: '姓名/手机号' },
      { prop: 'status', label: '状态', type: 'select', options: userStatusOptions },
    ],
    fields: [
      { prop: 'name', label: '姓名' },
      { prop: 'tel', label: '手机号' },
      { prop: 'password', label: '密码' },
      { prop: 'email', label: '邮箱' },
      { prop: 'gender', label: '性别', type: 'number' },
      { prop: 'chinaId', label: '身份证' },
      { prop: 'birth', label: '生日', type: 'date' },
      { prop: 'deptId', label: '部门ID', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: userStatusOptions },
      { prop: 'level', label: '级别', type: 'number' },
      { prop: 'salary', label: '薪资', type: 'number' },
      { prop: 'roleIds', label: '角色ID(逗号)' },
      { prop: 'remark', label: '备注' },
    ],
    addPerm: 'admin:staff:add',
    updatePerm: 'admin:staff:update',
    deletePerm: 'admin:staff:delete',
    transformSubmit: (data) => ({ ...data, roleIds: splitIds(data.roleIds) }),
  },
  depts: {
    endpoint: '/api/admin/depts',
    dataKey: 'depts',
    noPage: true,
    columns: [
      ['id', '编号', 80],
      ['deptName', '部门名称', 160],
      ['parentId', '父级ID', 90],
      ['status', '状态', 80],
      ['deptDesc', '描述', 260],
    ],
    fields: [
      { prop: 'deptName', label: '部门名称' },
      { prop: 'deptDesc', label: '部门描述' },
      { prop: 'parentId', label: '父级ID', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: shelfOptions },
    ],
    addPerm: 'admin:dept:add',
    updatePerm: 'admin:dept:update',
    deletePerm: 'admin:dept:delete',
  },
  positionApplies: {
    endpoint: '/api/admin/position-applies',
    columns: [
      ['id', '编号', 80],
      ['userId', '用户ID', 90],
      ['targetPosition', '申请职务', 120],
      ['tel', '手机号', 130],
      ['email', '邮箱', 170],
      ['status', '状态', 100, applyStatusOptions],
      ['applyTime', '申请时间', 170],
      ['handleRemark', '处理备注', 180],
    ],
    filters: [{ prop: 'status', label: '状态', type: 'select', options: applyStatusOptions }],
    noAdd: true,
    auditEndpoint: (row) => `/api/admin/position-applies/${row.id}/audit`,
    auditPerm: 'admin:apply:audit',
    auditFields: 'apply',
  },
}

configs.courseCreate = { ...configs.courses, autoCreate: true, noDelete: true }
configs.courseAudit = { ...configs.courses, defaultParams: { statusAudit: 1 }, noAdd: true, auditEndpoint: (row) => `/api/admin/courses/${row.id}/audit`, auditPerm: 'admin:course:audit' }
configs.momentCreate = { ...configs.moments, autoCreate: true, noDelete: true }
configs.momentAudit = { ...configs.moments, defaultParams: { status: 2 }, noAdd: true, auditEndpoint: (row) => `/api/admin/moments/${row.id}/audit`, auditPerm: 'admin:moment:audit' }
configs.goodsCreate = { ...configs.goods, autoCreate: true, noDelete: true }
configs.goodsAudit = { ...configs.goods, defaultParams: { status: 1 }, noAdd: true, auditEndpoint: (row) => `/api/admin/goods/${row.id}/audit`, auditPerm: 'admin:goods:audit' }
configs.goodsOrders = orderConfig('/api/admin/orders', { entityType: 2 })
configs.courseOrders = orderConfig('/api/admin/orders', { entityType: 1 })
configs.returnOrders = { ...orderConfig('/api/admin/orders/returns', {}), noAdd: true, returnAudit: true }
configs.comments = { ...configs.courseComments, endpoint: '/api/admin/comments' }

const config = computed(() => configs[route.meta.resource] || configs.courses)

watch(
  () => route.meta.resource,
  async () => {
    resetFilters()
    await load()
    if (config.value.autoCreate) {
      nextTick(() => openCreate())
    }
  },
  { immediate: true },
)

function orderConfig(endpoint, defaultParams) {
  return {
    endpoint,
    defaultParams,
    columns: [
      ['id', '编号', 80],
      ['orderSn', '订单编号', 160],
      ['userId', '用户ID', 90],
      ['entityId', '商品/课程ID', 120],
      ['entityType', '类型', 80],
      ['status', '状态', 110, orderStatusOptions],
      ['totalQuantity', '数量', 80],
      ['pricePay', '实付', 100],
      ['deliverySn', '物流', 140],
      ['createTime', '下单时间', 170],
    ],
    filters: [{ prop: 'status', label: '状态', type: 'select', options: orderStatusOptions }],
    noAdd: true,
    noEdit: true,
    statusAction: true,
  }
}

async function load() {
  loading.value = true
  try {
    const params = { ...config.value.defaultParams, ...cleanParams(filters) }
    if (!config.value.noPage) {
      params.page = page.current
      params.size = page.size
    }
    const data = await http.get(config.value.endpoint, { params })
    const result = data.result || {}
    if (result.page) {
      rows.value = result.page.records || []
      total.value = result.page.total || 0
    } else {
      const key = config.value.dataKey || 'items'
      rows.value = result[key] || []
      total.value = rows.value.length
    }
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  Object.keys(filters).forEach((key) => delete filters[key])
  page.current = 1
}

function cleanParams(source) {
  return Object.fromEntries(Object.entries(source).filter(([, value]) => value !== '' && value !== null && typeof value !== 'undefined'))
}

function openCreate() {
  if (config.value.createPath) {
    router.push(config.value.createPath)
    return
  }
  dialogMode.value = 'create'
  form.value = {}
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

async function submitForm() {
  const payload = normalizePayload(form.value)
  if (dialogMode.value === 'create') {
    await http.post(config.value.endpoint, payload)
    ElMessage.success('新增成功')
  } else {
    await http.put(`${config.value.endpoint}/${form.value.id}`, payload)
    ElMessage.success('更新成功')
  }
  dialogVisible.value = false
  await load()
}

async function deleteRow(row) {
  await ElMessageBox.confirm('确认删除这条数据？', '删除确认', { type: 'warning' })
  await http.delete(config.value.deleteEndpoint ? config.value.deleteEndpoint(row) : `${config.value.endpoint}/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}

function openAudit(row) {
  activeRow.value = row
  auditForm.auditResult = 1
  auditForm.statusShelf = 1
  auditForm.feedbackDetail = ''
  auditForm.remark = ''
  auditVisible.value = true
}

async function submitAudit() {
  if (config.value.auditFields === 'apply') {
    await http.post(config.value.auditEndpoint(activeRow.value), {
      status: auditForm.auditResult === 1 ? 2 : 3,
      handleRemark: auditForm.feedbackDetail,
    })
  } else {
    await http.post(config.value.auditEndpoint(activeRow.value), auditForm)
  }
  ElMessage.success('审核完成')
  auditVisible.value = false
  await load()
}

function openStatus(row) {
  activeRow.value = row
  statusForm.status = row.status
  statusForm.deliverySn = row.deliverySn || ''
  statusVisible.value = true
}

async function submitStatus() {
  await http.put(`/api/admin/orders/${activeRow.value.id}/status`, {
    status: statusForm.status,
    deliverySn: statusForm.deliverySn,
  })
  ElMessage.success('状态已更新')
  statusVisible.value = false
  await load()
}

async function returnAudit(row, auditResult) {
  await http.post(`/api/admin/orders/${row.id}/return-audit`, { auditResult })
  ElMessage.success('退货审核完成')
  await load()
}

async function topAd(row) {
  await http.put(`/api/admin/ads/${row.id}/top`)
  ElMessage.success('已置顶')
  await load()
}

function openGrant() {
  grantForm.couponId = null
  grantForm.userId = null
  grantVisible.value = true
}

async function submitGrant() {
  await http.post('/api/admin/coupon-users/grant', grantForm)
  ElMessage.success('赠送成功')
  grantVisible.value = false
  await load()
}

function normalizePayload(raw) {
  const data = { ...raw }
  for (const key of Object.keys(data)) {
    if (data[key] === '') data[key] = null
  }
  return config.value.transformSubmit ? config.value.transformSubmit(data) : data
}

function splitIds(value) {
  if (Array.isArray(value)) return value
  if (!value) return []
  return value
    .toString()
    .split(',')
    .map((item) => Number(item.trim()))
    .filter(Boolean)
}

function formatValue(row, col) {
  const value = row[col[0]]
  if (col[3]) return col[3].find((item) => item.value === value)?.label ?? value
  return value
}

function can(perm) {
  return !perm || auth.hasPerm(perm)
}
</script>

<template>
  <section class="table-page">
    <div class="toolbar">
      <div class="filters">
        <template v-for="field in config.filters || []" :key="field.prop">
          <el-input
            v-if="!field.type"
            v-model="filters[field.prop]"
            :placeholder="field.label"
            clearable
            class="filter-item"
            @keyup.enter="load"
          />
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="filters[field.prop]"
            :placeholder="field.label"
            class="filter-item"
            controls-position="right"
          />
          <el-select v-else v-model="filters[field.prop]" :placeholder="field.label" clearable class="filter-item">
            <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </template>
        <el-button :icon="Search" type="primary" @click="load">查询</el-button>
        <el-button @click="resetFilters(); load()">重置</el-button>
      </div>
      <div class="toolbar-actions">
        <el-button v-if="config.grant && can('admin:coupon:grant')" type="primary" :icon="Present" @click="openGrant">赠送优惠券</el-button>
        <el-button v-if="!config.noAdd && can(config.addPerm)" type="primary" :icon="Plus" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe height="calc(100vh - 245px)">
      <el-table-column
        v-for="col in config.columns"
        :key="col[0]"
        :prop="col[0]"
        :label="col[1]"
        :width="col[2]"
        show-overflow-tooltip
      >
        <template #default="{ row }">{{ formatValue(row, col) }}</template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="260">
        <template #default="{ row }">
          <el-button v-if="config.auditEndpoint && can(config.auditPerm)" link type="primary" @click="openAudit(row)">审核</el-button>
          <el-button v-if="config.statusAction" link type="primary" @click="openStatus(row)">状态</el-button>
          <el-button v-if="config.returnAudit" link type="primary" @click="returnAudit(row, 1)">通过退货</el-button>
          <el-button v-if="config.returnAudit" link type="danger" @click="returnAudit(row, 0)">拒绝</el-button>
          <el-button v-if="config.topPerm && can(config.topPerm)" link type="primary" @click="topAd(row)">置顶</el-button>
          <el-button v-if="!config.noEdit && can(config.updatePerm)" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="!config.noDelete && can(config.deletePerm)" link type="danger" @click="deleteRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!config.noPage" class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增' : '编辑'" width="720px">
      <el-form label-width="120px" class="dialog-form">
        <el-form-item v-for="field in config.fields || []" :key="field.prop" :label="field.label">
          <el-input v-if="!field.type" v-model="form[field.prop]" clearable />
          <el-input v-else-if="field.type === 'text'" v-model="form[field.prop]" clearable>
            <template v-if="field.append" #append>{{ field.append }}</template>
          </el-input>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="4" />
          <el-input-number v-else-if="field.type === 'number'" v-model="form[field.prop]" controls-position="right" class="full-control" />
          <el-date-picker v-else-if="field.type === 'date'" v-model="form[field.prop]" type="date" value-format="YYYY-MM-DD" class="full-control" />
          <el-date-picker
            v-else-if="field.type === 'datetime'"
            v-model="form[field.prop]"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="full-control"
          />
          <el-select v-else v-model="form[field.prop]" clearable class="full-control">
            <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditVisible" title="审核" width="560px">
      <el-form label-width="110px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio-button :label="1">通过</el-radio-button>
            <el-radio-button :label="0">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="route.meta.resource === 'courseAudit'" label="上架状态">
          <el-select v-model="auditForm.statusShelf" class="full-control">
            <el-option v-for="option in shelfOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="详情反馈">
          <el-input v-model="auditForm.feedbackDetail" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="订单状态" width="520px">
      <el-form label-width="100px">
        <el-form-item label="订单状态">
          <el-select v-model="statusForm.status" class="full-control">
            <el-option v-for="option in orderStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="statusForm.deliverySn" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantVisible" title="赠送优惠券" width="480px">
      <el-form label-width="100px">
        <el-form-item label="优惠券ID">
          <el-input-number v-model="grantForm.couponId" class="full-control" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input-number v-model="grantForm.userId" class="full-control" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrant">赠送</el-button>
      </template>
    </el-dialog>
  </section>
</template>

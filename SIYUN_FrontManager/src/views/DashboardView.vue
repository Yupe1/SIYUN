<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const summary = ref({})
const todo = ref({})
const hourlyOrders = ref([])

const statCards = computed(() => [
  { label: '今日订单总数', value: summary.value.todayOrderCount ?? 0, icon: 'Document', color: '#18bfa5' },
  { label: '今日销售总额', value: money(summary.value.todaySalesAmount), icon: 'Money', color: '#14b8a6' },
  { label: '昨日销售总额', value: money(summary.value.yesterdaySalesAmount), icon: 'Coin', color: '#15b8a6' },
  { label: '近7天销售总额', value: money(summary.value.last7DaysSalesAmount), icon: 'TrendCharts', color: '#15b8a6' },
])

const quickLinks = [
  { title: '添加商品', path: '/goods/create', icon: 'Plus' },
  { title: '添加课程', path: '/courses/create', icon: 'Reading' },
  { title: '订单列表', path: '/orders/goods', icon: 'Tickets' },
  { title: '人事部门', path: '/hr/depts', icon: 'User' },
  { title: '微圈管理', path: '/moments/list', icon: 'ChatDotRound' },
  { title: '商品审核管理', path: '/goods/audit', icon: 'Goods' },
  { title: '课程审核管理', path: '/courses/audit', icon: 'Checked' },
]

async function load() {
  loading.value = true
  try {
    const data = await http.get('/api/admin/home/index-data')
    summary.value = data.result?.summary || {}
    todo.value = data.result?.todo || {}
    hourlyOrders.value = data.result?.hourlyOrders || []
  } finally {
    loading.value = false
  }
}

function money(value) {
  const num = Number(value || 0)
  return `¥${num.toFixed(2)}`
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="dashboard-page">
    <div class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <component :is="card.icon" class="stat-icon" :style="{ color: card.color }" />
        <div>
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </div>
      </div>
    </div>

    <section class="panel">
      <header>待处理事务</header>
      <div class="todo-grid">
        <div v-for="(value, key) in todo" :key="key" class="todo-item">
          <span>{{ key }}</span>
          <strong>({{ value }})</strong>
        </div>
      </div>
    </section>

    <section class="panel">
      <header>运营快捷入口</header>
      <div class="quick-grid">
        <button v-for="item in quickLinks" :key="item.path" class="quick-item" @click="router.push(item.path)">
          <component :is="item.icon" />
          <span>{{ item.title }}</span>
        </button>
      </div>
    </section>

    <div class="overview-grid">
      <section class="panel">
        <header>商品总览</header>
        <div class="numbers">
          <div><strong>{{ summary.courseCount || 0 }}</strong><span>课程数量</span></div>
          <div><strong>{{ summary.goodsCount || 0 }}</strong><span>商品数量</span></div>
        </div>
      </section>
      <section class="panel">
        <header>用户总览</header>
        <div class="numbers">
          <div><strong>{{ summary.userCount || 0 }}</strong><span>全部用户</span></div>
          <div><strong>{{ summary.pendingCourseAudit || 0 }}</strong><span>待审课程</span></div>
          <div><strong>{{ summary.pendingReturns || 0 }}</strong><span>退货处理</span></div>
        </div>
      </section>
    </div>

    <section class="panel">
      <header>今日分时订单</header>
      <div class="hour-row">
        <span v-for="item in hourlyOrders" :key="item.hour">{{ item.hour }}时 {{ item.count }}单</span>
      </div>
    </section>
  </div>
</template>

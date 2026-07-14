<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/http'

const loading = ref(false)
const data = ref({})

async function load() {
  loading.value = true
  try {
    const res = await http.get('/api/admin/statistics')
    data.value = res.result?.statistics || {}
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="statistics-page">
    <section class="panel">
      <header>课程购买量</header>
      <el-table :data="data.courseSales || []" border>
        <el-table-column prop="entityId" label="课程ID" />
        <el-table-column prop="total" label="购买量" />
      </el-table>
    </section>
    <section class="panel">
      <header>商品购买量</header>
      <el-table :data="data.goodsSales || []" border>
        <el-table-column prop="entityId" label="商品ID" />
        <el-table-column prop="total" label="购买量" />
      </el-table>
    </section>
    <section class="panel">
      <header>点赞收藏</header>
      <div class="overview-grid">
        <el-table :data="data.courseLikes || []" border>
          <el-table-column prop="courseId" label="课程ID" />
          <el-table-column prop="total" label="点赞量" />
        </el-table>
        <el-table :data="data.courseCollects || []" border>
          <el-table-column prop="courseId" label="课程ID" />
          <el-table-column prop="total" label="收藏量" />
        </el-table>
      </div>
    </section>
  </div>
</template>

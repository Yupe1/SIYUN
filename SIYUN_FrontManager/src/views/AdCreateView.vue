<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const courses = ref([])
const usedCourseIds = ref(new Set())
const currentCount = ref(0)
const maxCount = ref(9)
const courseId = ref(null)
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '')

const selectedCourse = computed(() => courses.value.find((item) => item.id === courseId.value))
const isFull = computed(() => currentCount.value >= maxCount.value)

function publicImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return `${apiBaseUrl}${url.startsWith('/') ? '' : '/'}${url}`
}

function auditLabel(status) {
  return ({ 0: '未审核', 1: '审核中', 2: '审核失败', 3: '已通过' })[status] || '未知状态'
}

async function loadOptions() {
  loading.value = true
  try {
    const data = await http.get('/api/admin/ad-course-options')
    const result = data.result || {}
    courses.value = result.courses || []
    usedCourseIds.value = new Set(result.usedCourseIds || [])
    currentCount.value = Number(result.currentCount || 0)
    maxCount.value = Number(result.maxCount || 9)
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (isFull.value) {
    ElMessage.warning('轮播图最多只能添加9个内容')
    return
  }
  if (!courseId.value) {
    ElMessage.warning('请选择要加入轮播图的课程')
    return
  }
  submitting.value = true
  try {
    await http.post('/api/admin/ads', { courseId: courseId.value })
    ElMessage.success('课程已加入轮播图')
    router.push('/marketing/ads')
  } finally {
    submitting.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <section v-loading="loading" class="course-create-page">
    <el-form label-width="110px" class="course-form">
      <section class="panel form-panel">
        <header>
          <span>添加轮播图</span>
          <el-tag :type="isFull ? 'danger' : 'success'">{{ currentCount }} / {{ maxCount }}</el-tag>
        </header>
        <div class="ad-create-form">
          <el-alert
            v-if="isFull"
            title="轮播图已达到9个，请先删除一个内容后再添加"
            type="warning"
            :closable="false"
            show-icon
          />

          <el-form-item label="选择课程" required>
            <el-select
              v-model="courseId"
              filterable
              clearable
              :disabled="isFull"
              placeholder="输入课程名称搜索"
              class="full-control"
            >
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.title"
                :value="course.id"
                :disabled="usedCourseIds.has(course.id)"
              >
                <span>{{ course.title }}</span>
                <span class="course-option-meta">
                  {{ usedCourseIds.has(course.id) ? '已在轮播图' : auditLabel(course.statusAudit) }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>

          <div v-if="selectedCourse" class="ad-course-preview">
            <el-image
              :src="publicImageUrl(selectedCourse.coverUrl)"
              fit="cover"
              class="ad-course-cover"
            >
              <template #error><div class="ad-cover-empty">课程暂无封面</div></template>
            </el-image>
            <div class="ad-course-info">
              <strong>{{ selectedCourse.title }}</strong>
              <span>课程编号：{{ selectedCourse.id }}</span>
              <span>审核状态：{{ auditLabel(selectedCourse.statusAudit) }}</span>
              <p>{{ selectedCourse.intro || '暂无课程简介' }}</p>
            </div>
          </div>
        </div>
      </section>

      <div class="form-actions">
        <el-button @click="router.push('/marketing/ads')">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="isFull" @click="submit">确认添加</el-button>
      </div>
    </el-form>
  </section>
</template>

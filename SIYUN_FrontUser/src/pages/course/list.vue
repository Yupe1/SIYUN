<template>
  <view class="page">
    <view class="nav-bar">
      <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
      <text class="nav-title">课程</text>
    </view>

    <view class="content">
      <view class="search-box">
        <text class="search-mark">搜</text>
        <input
          v-model.trim="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索课程"
          placeholder-class="placeholder"
          @confirm="loadCourses(keyword)"
        />
      </view>

      <view class="result-head">
        <text class="section-title">课程列表</text>
        <button class="section-action" @tap="loadCourses(keyword)">刷新</button>
      </view>

      <CourseCard
        v-for="course in courses"
        :key="course.id"
        :course="course"
        @select="openCourse"
      />
      <EmptyState v-if="!courses.length" title="暂无匹配课程" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import CourseCard from '@/components/CourseCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { searchCourses } from '@/api/course'
import { pickResult } from '@/utils/request'

const CURRENT_COURSE_KEY = 'SIYUN_CURRENT_COURSE'
const keyword = ref('')
const courses = ref([])

onLoad((query) => {
  keyword.value = query?.keyword ? decodeURIComponent(query.keyword) : ''
  loadCourses(keyword.value)
})

onPullDownRefresh(async () => {
  await loadCourses(keyword.value)
  uni.stopPullDownRefresh()
})

async function loadCourses(value = '') {
  try {
    const response = await searchCourses(value)
    courses.value = pickResult(response, 'courses', [])
  } catch (error) {
    courses.value = []
    uni.showToast({ title: error.message || '课程加载失败', icon: 'none' })
  }
}

function openCourse(course) {
  uni.setStorageSync(CURRENT_COURSE_KEY, course)
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`,
  })
}

function goBack() {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/index/index' }),
  })
}
</script>

<style scoped>
.search-mark {
  margin-right: 12rpx;
  color: #7d8a90;
  font-size: 22rpx;
  font-weight: 800;
}

.placeholder {
  color: #aab5ba;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 28rpx 0 18rpx;
}
</style>

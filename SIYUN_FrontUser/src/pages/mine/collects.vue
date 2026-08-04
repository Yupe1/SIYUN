<template>
  <view class="collect-page">
    <SubPageNav title="我的收藏" />
    <view class="content">
      <CourseCard v-for="course in courses" :key="course.id" :course="course" @select="openCourse" />
      <EmptyState v-if="!courses.length" title="暂无收藏课程" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CourseCard from '@/components/CourseCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getMyCollect } from '@/api/course'
import { pickResult } from '@/utils/request'

const CURRENT_COURSE_KEY = 'SIYUN_CURRENT_COURSE'
const courses = ref([])

onShow(async () => {
  try {
    const response = await getMyCollect()
    courses.value = pickResult(response, 'myCollect', [])
  } catch (error) {
    courses.value = []
    uni.showToast({ title: error.message || '收藏加载失败', icon: 'none' })
  }
})

function openCourse(course) {
  uni.setStorageSync(CURRENT_COURSE_KEY, course)
  uni.navigateTo({ url: `/pages/course/detail?id=${course.id}` })
}
</script>

<style scoped>
.collect-page { min-height: 100vh; background: #f4f8f8; }
</style>

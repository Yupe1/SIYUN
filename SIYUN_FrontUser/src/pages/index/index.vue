<template>
  <view class="page">
    <view class="top-bar home-top">
      <view class="top-title">首页</view>
    </view>

    <view class="search-wrap">
      <view class="search-box home-search">
        <text class="search-mark"></text>
        <input
          v-model.trim="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索"
          placeholder-class="search-placeholder"
          @confirm="doSearch"
        />
      </view>
    </view>

    <view v-if="bannerCourses.length" class="banner">
      <swiper class="banner-swiper" indicator-dots circular autoplay>
        <swiper-item v-for="item in bannerCourses" :key="item.id">
          <view class="banner-item" @click="openCourse(item)">
            <view class="banner-mark">?</view>
            <text class="banner-title">{{ item.title }}</text>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <view class="category-grid">
      <view
        v-for="item in courseCategories"
        :key="item.key"
        class="category"
        @click="selectCategory(item)"
      >
        <view class="category-icon" :style="{ color: item.tone }">{{ item.name.slice(0, 2) }}</view>
        <text>{{ item.name }}</text>
      </view>
    </view>

    <view class="content home-content">
      <view class="section-head">
        <text class="section-title">热门课程</text>
        <text class="section-action" @click="goCourseList">进入></text>
      </view>
      <CourseCard
        v-for="course in courses"
        :key="course.id"
        :course="course"
        @select="openCourse"
      />
      <EmptyState v-if="!courses.length" title="暂无课程" />
    </view>

    <BottomTab active="home" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import BottomTab from '@/components/BottomTab.vue'
import CourseCard from '@/components/CourseCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { searchCourses } from '@/api/course'
import { pickResult } from '@/utils/request'
import { courseCategories } from '@/utils/mock'
import { useUserStore } from '@/stores/user'

const CURRENT_COURSE_KEY = 'SIYUN_CURRENT_COURSE'
const userStore = useUserStore()
const keyword = ref('')
const courses = ref([])
const bannerCourses = computed(() => courses.value.slice(0, 3))

onLoad(() => {
  userStore.hydrate()
  loadCourses()
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

function doSearch() {
  uni.navigateTo({
    url: `/pages/course/list?keyword=${encodeURIComponent(keyword.value)}`,
  })
}

function goCourseList() {
  uni.navigateTo({ url: '/pages/course/list' })
}

function selectCategory(item) {
  uni.navigateTo({
    url: `/pages/course/list?keyword=${encodeURIComponent(item.key)}`,
  })
}

function openCourse(course) {
  uni.setStorageSync(CURRENT_COURSE_KEY, course)
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`,
  })
}
</script>

<style scoped>
.home-search {
  margin: 0;
  background: #d9e7e7;
}

.home-top {
  padding-bottom: 10rpx;
}

.search-wrap {
  padding: 18rpx 24rpx 10rpx;
  background: #f4f8f8;
}

.search-mark {
  margin-right: 12rpx;
  color: #7f9095;
  font-size: 22rpx;
  font-weight: 800;
}

.search-placeholder {
  color: #93a1a6;
}

.home-search .search-input {
  color: #263238;
}

.banner {
  padding: 20rpx 24rpx 24rpx;
  background: #f4f8f8;
}

.banner-swiper {
  height: 316rpx;
}

.banner-item {
  width: 100%;
  height: 316rpx;
  border-radius: 16rpx;
  background: #cfe4e9;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.banner-mark {
  width: 112rpx;
  height: 112rpx;
  border-radius: 18rpx;
  border: 10rpx solid rgba(255, 255, 255, 0.46);
  color: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  font-weight: 900;
}

.banner-title {
  max-width: 86%;
  margin-top: 28rpx;
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-grid {
  padding: 6rpx 24rpx 18rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 26rpx;
}

.category {
  min-width: 0;
  height: 148rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  color: #1f2d33;
  font-size: 24rpx;
  font-weight: 700;
  background: transparent;
  border: 0;
  box-shadow: none;
  outline: none;
}

.category-icon {
  width: 92rpx;
  height: 92rpx;
  border-radius: 46rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
  font-size: 22rpx;
  font-weight: 900;
}

.category text {
  max-width: 100%;
  font-size: 24rpx;
  line-height: 30rpx;
  text-align: center;
}

.home-content {
  padding-top: 4rpx;
}
</style>

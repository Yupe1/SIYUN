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

    <view v-if="homeAds.length" class="banner">
      <swiper
        class="banner-swiper"
        indicator-dots
        circular
        :autoplay="homeAds.length > 1"
      >
        <swiper-item v-for="item in homeAds" :key="item.id">
          <view class="banner-item" @click="openAd(item)">
            <image
              class="banner-image"
              :src="assetUrl(item.picUrl)"
              mode="aspectFill"
            />
            <view class="banner-overlay"></view>
            <view class="banner-copy">
              <text class="banner-title">{{ item.title }}</text>
              <text v-if="item.intro" class="banner-intro">{{ item.intro }}</text>
            </view>
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
      <view v-if="hotCourseItems.length" class="hot-course-grid">
        <view
          v-for="(column, columnIndex) in hotCourseColumns"
          :key="columnIndex"
          class="hot-course-column"
        >
          <view
            v-for="item in column"
            :key="item.key"
            class="hot-course-item"
            :data-hot-key="item.key"
          >
            <CourseCard
              :course="item.course"
              layout="grid"
              @select="openCourse"
            />
          </view>
        </view>
      </view>
      <EmptyState v-else title="暂无热门课程" />
    </view>

    <BottomTab active="home" />
  </view>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import BottomTab from '@/components/BottomTab.vue'
import CourseCard from '@/components/CourseCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { searchCourses } from '@/api/course'
import { getHomeAds } from '@/api/marketing'
import { pickResult } from '@/utils/request'
import { assetUrl } from '@/utils/format'
import { courseCategories } from '@/utils/mock'
import { useUserStore } from '@/stores/user'

const CURRENT_COURSE_KEY = 'SIYUN_CURRENT_COURSE'
const userStore = useUserStore()
const keyword = ref('')
const courses = ref([])
const homeAds = ref([])
const adCourses = ref([])
const hotCourseColumns = ref([[], []])
let hotCourseLayoutVersion = 0

const hotCourseItems = computed(() => {
  const courseMap = new Map(
    [...adCourses.value, ...courses.value]
      .map((course) => [Number(course.id), course]),
  )

  return homeAds.value
    .map((ad, index) => {
      const courseId = Number(ad.courseId || 0)
      if (!courseId) {
        return null
      }
      return {
        key: ad.id || `${courseId}-${index}`,
        course: courseMap.get(courseId) || {
          id: courseId,
          title: ad.title,
          intro: ad.intro,
          coverUrl: ad.picUrl,
        },
      }
    })
    .filter(Boolean)
})

watch(hotCourseItems, (items) => {
  distributeHotCourses(items)
}, { immediate: true })

onLoad(() => {
  userStore.hydrate()
  loadHomeData()
})

onPullDownRefresh(async () => {
  await loadHomeData(keyword.value)
  uni.stopPullDownRefresh()
})

async function loadHomeData(value = '') {
  await Promise.all([loadCourses(value), loadAds()])
}

function distributeHotCourses(items) {
  const layoutVersion = ++hotCourseLayoutVersion
  hotCourseColumns.value = fallbackHotCourseColumns(items)

  rebalanceHotCourses(items, layoutVersion)
}

async function rebalanceHotCourses(items, layoutVersion) {
  await Promise.all(items.map((item) => preloadCover(item.course.coverUrl)))
  if (layoutVersion !== hotCourseLayoutVersion) {
    return
  }

  await nextTick()
  const renderedKeys = hotCourseColumns.value
    .flat()
    .map((item) => String(item.key))
  const itemHeights = await getHotCourseItemHeights(renderedKeys)
  if (layoutVersion !== hotCourseLayoutVersion || !itemHeights.size) {
    return
  }

  const columns = [[], []]
  const columnHeights = [0, 0]
  const cardGap = typeof uni.upx2px === 'function' ? uni.upx2px(18) : 9
  items.forEach((item) => {
    const targetIndex = columnHeights[0] <= columnHeights[1] ? 0 : 1
    columns[targetIndex].push(item)
    columnHeights[targetIndex] += (itemHeights.get(String(item.key)) || 0) + cardGap
  })
  hotCourseColumns.value = columns
}

function fallbackHotCourseColumns(items) {
  return [
    items.filter((_, index) => index % 2 === 0),
    items.filter((_, index) => index % 2 === 1),
  ]
}

function finishWithin(resolve, timeout = 1500, fallback) {
  let finished = false
  const timer = setTimeout(() => done(fallback), timeout)

  function done(value) {
    if (finished) {
      return
    }
    finished = true
    clearTimeout(timer)
    resolve(value)
  }

  return done
}

function preloadCover(coverUrl) {
  const src = assetUrl(coverUrl)
  if (!src) {
    return Promise.resolve()
  }

  return new Promise((resolve) => {
    const done = finishWithin(resolve)
    uni.getImageInfo({
      src,
      success: done,
      fail: done,
    })
  })
}

function getHotCourseItemHeights(renderedKeys) {
  return new Promise((resolve) => {
    const done = finishWithin(resolve, 500, new Map())
    uni.createSelectorQuery()
      .selectAll('.hot-course-item')
      .boundingClientRect((rects) => {
        const heights = new Map()
        ;(rects || []).forEach((rect, index) => {
          const key = rect?.dataset?.hotKey ?? renderedKeys[index]
          if (key !== undefined && key !== null) {
            heights.set(String(key), Number(rect.height || 0))
          }
        })
        done(heights)
      })
      .exec()
  })
}

async function loadCourses(value = '') {
  try {
    const response = await searchCourses(value)
    courses.value = pickResult(response, 'courses', [])
  } catch (error) {
    courses.value = []
    uni.showToast({ title: error.message || '课程加载失败', icon: 'none' })
  }
}

async function loadAds() {
  try {
    const response = await getHomeAds()
    homeAds.value = pickResult(response, 'ads', [])
    adCourses.value = pickResult(response, 'courses', [])
  } catch (error) {
    homeAds.value = []
    adCourses.value = []
    uni.showToast({ title: error.message || '轮播图加载失败', icon: 'none' })
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

function openAd(ad) {
  const courseId = Number(ad.courseId || 0)
  if (!courseId) {
    return
  }

  const course = courses.value.find((item) => Number(item.id) === courseId) || {
    id: courseId,
    title: ad.title,
    intro: ad.intro,
    coverUrl: ad.picUrl,
  }
  openCourse(course)
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
  position: relative;
  width: 100%;
  height: 316rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #dce9eb;
}

.banner-image {
  width: 100%;
  height: 100%;
  display: block;
}

.banner-overlay {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background: linear-gradient(180deg, rgba(18, 35, 42, 0.02) 36%, rgba(18, 35, 42, 0.78) 100%);
}

.banner-copy {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 30rpx;
  display: flex;
  flex-direction: column;
}

.banner-title {
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.banner-intro {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.82);
  font-size: 23rpx;
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

.hot-course-grid {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
}

.hot-course-column {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.hot-course-item {
  width: 100%;
  min-width: 0;
}
</style>

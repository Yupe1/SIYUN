<template>
  <view class="detail-page">
    <view class="nav-bar transparent">
      <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
      <text class="nav-title">课程详情</text>
    </view>

    <view class="hero">
      <video
        v-if="playing && currentVideoSrc"
        :key="currentVideoKey"
        class="hero-video"
        :src="currentVideoSrc"
        autoplay
        controls
        show-center-play-btn
        object-fit="cover"
        @ended="handleVideoEnded"
        @error="handleVideoError"
      />
      <image v-else-if="cover" class="hero-image" :src="cover" mode="aspectFill" />
      <view v-else class="hero-fallback">
        <view class="hero-mark">?</view>
      </view>
      <button
        v-if="userStore.isLoggedIn && purchased"
        class="play-button"
        @tap="toggleLearning"
      >
        {{ playing ? '结束学习' : '开始学习' }}
      </button>
    </view>

    <!-- #ifdef H5 -->
    <div class="tabs">
      <div
        class="tab"
        data-detail-tab="0"
        :class="{ active: activeTab === 0 }"
        @mousedown.stop.prevent="setActiveTab(0)"
        @click.stop.prevent="setActiveTab(0)"
      >
        课程介绍
      </div>
      <div
        class="tab"
        data-detail-tab="1"
        :class="{ active: activeTab === 1 }"
        @mousedown.stop.prevent="setActiveTab(1)"
        @click.stop.prevent="setActiveTab(1)"
      >
        课程目录
      </div>
      <div
        class="tab"
        data-detail-tab="2"
        :class="{ active: activeTab === 2 }"
        @mousedown.stop.prevent="setActiveTab(2)"
        @click.stop.prevent="setActiveTab(2)"
      >
        评论区
      </div>
    </div>
    <!-- #endif -->
    <!-- #ifndef H5 -->
    <view class="tabs">
      <button
        class="tab"
        data-detail-tab="0"
        :class="{ active: activeTab === 0 }"
        hover-class="none"
        @tap="setActiveTab(0)"
      >
        课程介绍
      </button>
      <button
        class="tab"
        data-detail-tab="1"
        :class="{ active: activeTab === 1 }"
        hover-class="none"
        @tap="setActiveTab(1)"
      >
        课程目录
      </button>
      <button
        class="tab"
        data-detail-tab="2"
        :class="{ active: activeTab === 2 }"
        hover-class="none"
        @tap="setActiveTab(2)"
      >
        评论区
      </button>
    </view>
    <!-- #endif -->

    <view class="detail-content">
      <view class="course-main">
        <text class="course-title">{{ course.title }}</text>
        <view class="price-row">
          <text v-if="salePrice !== null" class="price">￥{{ money(salePrice) }}</text>
          <text v-else class="price">价格待定</text>
          <text v-if="hasPromotion" class="origin">￥{{ money(course.priceOriginal) }}</text>
          <text class="stat">点赞数 {{ compactNumber(course.countLike) }}</text>
          <text class="stat">收藏 {{ compactNumber(course.countCollect) }}</text>
        </view>
        <view v-if="userStore.isLoggedIn" class="course-coupon-row">
          <text>优惠券</text>
          <picker
            v-if="coupons.length"
            mode="selector"
            :range="couponLabels"
            :value="selectedCouponIndex"
            @change="selectCoupon"
          >
            <view class="course-coupon-picker">{{ selectedCouponLabel }} ›</view>
          </picker>
          <text v-else class="course-coupon-empty">暂无可用券，按原价购买</text>
        </view>

        <div v-show="activeTab === 0" class="tab-panel">
          <view class="teacher-block">
            <view class="teacher-avatar">图</view>
            <view class="teacher-info">
              <text class="teacher-name">进思教育 - {{ course.teacherName || '老师' }}</text>
              <text class="teacher-desc">全职学习服务辅导老师，责任心强，有耐心。</text>
            </view>
          </view>
          <view class="divider"></view>
          <text class="panel-title">课程详情</text>
          <text class="detail-text">{{ course.detailDesc || course.intro || '暂无详情' }}</text>
        </div>

        <div v-show="activeTab === 1" class="tab-panel">
          <!-- #ifdef H5 -->
          <div
            v-for="episode in episodes"
            :key="episode.key"
            class="episode-row"
            :data-episode-no="episode.no"
            :class="{
              'episode-selected': episode.selected,
              'episode-playing': episode.playing,
            }"
            @mousedown.stop.prevent="selectEpisode(episode.no)"
            @click.stop.prevent="selectEpisode(episode.no)"
          >
            <div class="episode-main">
              <span
                class="episode-title"
                :class="{
                  'episode-title-selected': episode.selected,
                  'episode-title-playing': episode.playing,
                }"
              >
                {{ episode.displayTitle }}
              </span>
              <span
                class="episode-file"
                :class="{ 'episode-file-selected': episode.selected }"
              >
                {{ episode.fileName }}
              </span>
            </div>
            <span
              class="episode-status"
              :class="{
                'episode-status-selected': episode.selected,
                'episode-status-playing': episode.playing,
              }"
            >
              {{ episode.statusText }}
            </span>
          </div>
          <!-- #endif -->
          <!-- #ifndef H5 -->
          <button
            v-for="episode in episodes"
            :key="episode.key"
            class="episode-row"
            :data-episode-no="episode.no"
            hover-class="none"
            :class="{
              'episode-selected': episode.selected,
              'episode-playing': episode.playing,
            }"
            @mousedown.stop.prevent="selectEpisode(episode.no)"
            @tap="selectEpisode(episode.no)"
          >
            <view class="episode-main">
              <text
                class="episode-title"
                :class="{
                  'episode-title-selected': episode.selected,
                  'episode-title-playing': episode.playing,
                }"
              >
                {{ episode.displayTitle }}
              </text>
              <text
                class="episode-file"
                :class="{ 'episode-file-selected': episode.selected }"
              >
                {{ episode.fileName }}
              </text>
            </view>
            <text
              class="episode-status"
              :class="{
                'episode-status-selected': episode.selected,
                'episode-status-playing': episode.playing,
              }"
            >
              {{ episode.statusText }}
            </text>
          </button>
          <!-- #endif -->
          <EmptyState
            v-if="!episodes.length"
            :title="purchased ? '暂无课程目录' : '购买后查看课程目录'"
          />
        </div>

        <div v-show="activeTab === 2" class="tab-panel comments">
          <CommentThread
            v-if="course.id"
            :entity-id="Number(course.id)"
            :entity-type="0"
          />
        </div>
      </view>
    </view>

    <view v-if="userStore.isLoggedIn" class="action-bar">
      <button class="tool" :class="{ 'like-active': liked }" @tap="likeCourse">
        <text class="tool-icon">{{ liked ? '♥' : '♡' }}</text>
        <text class="tool-label">点赞</text>
      </button>
      <button class="tool" :class="{ 'collect-active': collected }" @tap="collectCourse">
        <text class="tool-icon">{{ collected ? '★' : '☆' }}</text>
        <text class="tool-label">收藏</text>
      </button>
      <button class="tool share-tool" @tap="shareCurrent">
        <text class="tool-icon share-icon">↗</text>
        <text class="tool-label">分享</text>
      </button>
      <button
        class="order"
        :class="{ purchased: purchased }"
        :disabled="!purchased && salePrice === null"
        @tap="purchase"
      >
        {{ orderButtonText }}
      </button>
    </view>
    <view v-else class="action-bar single">
      <button class="login-action" @tap="goLogin">登录</button>
    </view>
  </view>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { onHide, onLoad, onUnload } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import CommentThread from '@/components/CommentThread.vue'
import { getAvailableCoupons } from '@/api/commerce'
import {
  getCollectStatus,
  getCourseDetail,
  getCourseContents,
  getLikeStatus,
  getOrderStatus,
  purchaseCourse,
  shareCourse,
  startPlay,
  stopPlay,
  toggleCollect,
  toggleLike,
} from '@/api/course'
import { pickResult } from '@/utils/request'
import {
  assetUrl,
  compactNumber,
  money,
} from '@/utils/format'
import { redirectForRecharge } from '@/utils/payment'
import { showShareDialog } from '@/utils/share'
import { useUserStore } from '@/stores/user'

const CURRENT_COURSE_KEY = 'SIYUN_CURRENT_COURSE'
const userStore = useUserStore()

const course = ref({
  id: 0,
  title: '课程详情',
  intro: '',
  detailDesc: '',
  priceOriginal: null,
  pricePromotion: 0,
  countLike: 0,
  countShare: 0,
  countCollect: 0,
  countView: 0,
  episodeNum: 0,
  duration: 0,
})

const activeTab = ref(0)
const selectedEpisodeNo = ref(1)
const playingEpisodeNo = ref(0)
const playing = ref(false)
const purchased = ref(false)
const liked = ref(false)
const collected = ref(false)
const routeCourseId = ref(0)
const playLog = ref(null)
const stopping = ref(false)
const currentVideoPath = ref('')
const pendingAutoPlay = ref(false)
const courseContents = ref([])
const coupons = ref([])
const selectedCouponUserId = ref(null)
let lastUiActionKey = ''
let lastUiActionAt = 0

const cover = computed(() => assetUrl(course.value.coverUrl))
const originalPrice = computed(() => {
  const value = course.value.priceOriginal
  if (value === null || value === undefined || value === '') {
    return null
  }
  const number = Number(value)
  return Number.isFinite(number) && number >= 0 ? number : null
})
const selectedCoupon = computed(() => coupons.value.find(
  (item) => Number(item.couponUserId) === Number(selectedCouponUserId.value),
) || null)
const couponDiscount = computed(() => Math.min(
  Number(selectedCoupon.value?.amount || 0),
  Number(originalPrice.value || 0),
))
const salePrice = computed(() => originalPrice.value === null
  ? null
  : Math.max(0, originalPrice.value - couponDiscount.value))
const hasPromotion = computed(() => couponDiscount.value > 0)
const couponOptions = computed(() => [
  { couponUserId: null, label: '不使用优惠券' },
  ...coupons.value.map((item, index) => ({
    ...item,
    label: `${index === 0 ? '最优 · ' : ''}${item.couponName}（减￥${money(item.amount)}）`,
  })),
])
const couponLabels = computed(() => couponOptions.value.map((item) => item.label))
const selectedCouponIndex = computed(() => {
  const index = couponOptions.value.findIndex(
    (item) => Number(item.couponUserId || 0) === Number(selectedCouponUserId.value || 0),
  )
  return index < 0 ? 0 : index
})
const selectedCouponLabel = computed(() => couponOptions.value[selectedCouponIndex.value]?.label || '不使用优惠券')
const orderButtonText = computed(() => {
  if (purchased.value) {
    return '已购买，去学习'
  }
  return salePrice.value === null
    ? '价格加载中'
    : `下订单：￥${money(salePrice.value)}`
})
const currentVideoSrc = computed(() => assetUrl(currentVideoPath.value))
const currentVideoKey = computed(() => `${course.value.id}-${playingEpisodeNo.value}-${currentVideoPath.value}`)
const activeEpisodeNo = computed(() => playingEpisodeNo.value || selectedEpisodeNo.value)
const orderedCourseContents = computed(() => {
  return courseContents.value
    .map((item, index) => normalizeCourseContent(item, index))
    .sort((left, right) => {
      if (left.no !== right.no) {
        return left.no - right.no
      }
      return left.sortIndex - right.sortIndex
    })
})
const episodes = computed(() => {
  return orderedCourseContents.value.map((episode) => {
    const selected = normalizeEpisodeNo(activeEpisodeNo.value) === episode.no
    const playingNow = playing.value && normalizeEpisodeNo(playingEpisodeNo.value) === episode.no
    const title = episode.title || `第${episode.no}讲 ${course.value.title}`
    const duration = formatDuration(episode.duration)
    return {
      ...episode,
      selected,
      playing: playingNow,
      title,
      displayTitle: playingNow ? `正在播放 · ${title}` : title,
      fileName: buildEpisodeFileName(episode),
      statusText: playingNow ? `播放中 · ${duration}` : duration,
    }
  })
})

onLoad((query = {}) => {
  userStore.hydrate()
  const cached = uni.getStorageSync(CURRENT_COURSE_KEY)
  const requestedId = Number(query.id || cached?.id || 0)
  if (cached?.id && Number(cached.id) === requestedId) {
    course.value = cached
  }
  routeCourseId.value = requestedId
  activeTab.value = normalizeTabIndex(query.tab)
  selectedEpisodeNo.value = normalizeEpisodeNo(query.episode || 1)
  pendingAutoPlay.value = query.play === '1'
  initializeCourse()
})

onHide(() => {
  stopLearning(true)
})

onUnload(() => {
  stopLearning(true)
})

// #ifdef H5
onMounted(() => {
  document.addEventListener('mousedown', handleNativeDetailClick, true)
  document.addEventListener('click', handleNativeDetailClick, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleNativeDetailClick, true)
  document.removeEventListener('click', handleNativeDetailClick, true)
})
// #endif

async function goBack() {
  await stopLearning(true)
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack({
      delta: 1,
      fail: () => uni.reLaunch({ url: '/pages/index/index' }),
    })
    return
  }
  uni.reLaunch({ url: '/pages/index/index' })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function ensureLogin() {
  if (userStore.isLoggedIn) {
    return true
  }
  goLogin()
  return false
}

function isDuplicateUiAction(key) {
  const now = Date.now()
  if (lastUiActionKey === key && now - lastUiActionAt < 120) {
    return true
  }
  lastUiActionKey = key
  lastUiActionAt = now
  return false
}

function handleNativeDetailClick(event) {
  const target = event.target?.closest?.('[data-detail-tab], [data-episode-no]')
  if (!target?.closest?.('.detail-page')) {
    return
  }

  const tab = target.getAttribute('data-detail-tab')
  const episodeNo = target.getAttribute('data-episode-no')
  if (tab === null && episodeNo === null) {
    return
  }

  event.preventDefault()
  event.stopPropagation()
  if (tab !== null) {
    setActiveTab(tab)
    return
  }
  selectEpisode(episodeNo)
}

function setActiveTab(tab) {
  const nextTab = normalizeTabIndex(tab)
  if (isDuplicateUiAction(`tab-${nextTab}`)) {
    return
  }
  activeTab.value = nextTab
}

async function selectEpisode(no) {
  const episodeNo = normalizeEpisodeNo(no)
  if (isDuplicateUiAction(`episode-${episodeNo}`)) {
    return
  }
  activeTab.value = 1
  selectedEpisodeNo.value = episodeNo

  if (purchased.value) {
    await playEpisode(episodeNo)
  }
}

async function loadInitialState() {
  await Promise.all([
    loadOrderStatus(),
    loadLikeStatus(),
    loadCollectStatus(),
    loadAvailableCourseCoupons(),
  ])
  if (purchased.value) {
    await loadCourseContents()
  }
  if (pendingAutoPlay.value) {
    pendingAutoPlay.value = false
    if (purchased.value) {
      await playEpisode(selectedEpisodeNo.value)
    } else {
      uni.showToast({ title: '请先购买该课程', icon: 'none' })
    }
  }
}

async function initializeCourse() {
  const courseId = routeCourseId.value || Number(course.value.id || 0)
  if (!courseId) return
  try {
    const response = await getCourseDetail(courseId)
    const detail = pickResult(response, 'course', null)
    if (detail) {
      course.value = detail
      syncCourseCache()
    }
    if (userStore.isLoggedIn) {
      await loadInitialState()
    }
  } catch (error) {
    uni.showToast({ title: error.message || '课程加载失败', icon: 'none' })
  }
}

async function toggleLearning() {
  if (playing.value) {
    await stopLearning()
    return
  }
  await playEpisode(selectedEpisodeNo.value)
}

async function playEpisode(no) {
  if (!ensureLogin()) {
    return
  }
  if (!purchased.value) {
    uni.showToast({ title: '请先购买该课程', icon: 'none' })
    return
  }

  const episodeNo = normalizeEpisodeNo(no)
  if (!orderedCourseContents.value.length) {
    await loadCourseContents()
  }
  const episode = findEpisode(episodeNo)
  if (!episode) {
    uni.showToast({ title: '暂无该分集', icon: 'none' })
    return
  }
  const videoPath = buildEpisodeVideoPath(episode)
  if (!videoPath) {
    uni.showToast({ title: '课程视频暂未上传', icon: 'none' })
    return
  }

  if (playing.value) {
    if (playingEpisodeNo.value === episodeNo) {
      return
    }
    await stopLearning(true)
  }

  try {
    const response = await startPlay({
      ...course.value,
      videoUrl: videoPath,
    })
    playLog.value = pickResult(response, 'playLog', null)
    selectedEpisodeNo.value = episodeNo
    playingEpisodeNo.value = episodeNo
    currentVideoPath.value = videoPath
    playing.value = true
    uni.showToast({ title: `正在播放${episode.title || `第${episodeNo}讲`}`, icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function stopLearning(silent = false) {
  const lastEpisodeNo = normalizeEpisodeNo(playingEpisodeNo.value || selectedEpisodeNo.value)
  selectedEpisodeNo.value = lastEpisodeNo

  if (!playLog.value?.id || stopping.value) {
    playing.value = false
    playingEpisodeNo.value = 0
    currentVideoPath.value = ''
    return
  }

  stopping.value = true
  const log = playLog.value
  playLog.value = null
  playing.value = false
  playingEpisodeNo.value = 0
  currentVideoPath.value = ''

  try {
    await stopPlay(log)
    if (!silent) {
      uni.showToast({ title: '已结束学习', icon: 'success' })
    }
  } catch (error) {
  } finally {
    stopping.value = false
  }
}

function handleVideoEnded() {
  stopLearning()
}

function handleVideoError() {
  uni.showToast({ title: '视频加载失败，请检查文件路径', icon: 'none' })
}

function normalizeEpisodeNo(no) {
  const number = Number(no || 1)
  return Number.isFinite(number) && number > 0 ? number : 1
}

function normalizeTabIndex(index) {
  const number = Number(index || 0)
  if (![0, 1, 2].includes(number)) {
    return 0
  }
  return number
}

function normalizeCourseContent(item = {}, index = 0) {
  const no = normalizeEpisodeNo(item.epNo || index + 1)
  const title = String(item.epName || '').trim()
  const id = item.id ?? ''
  return {
    key: id ? `content-${id}` : `content-${no}-${index}`,
    id,
    sortIndex: index,
    courseId: item.courseId,
    no,
    title,
    videoUrl: String(item.videoUrl || '').trim(),
    duration: Number(item.duration || 0),
  }
}

function findEpisode(no) {
  const episodeNo = normalizeEpisodeNo(no)
  return orderedCourseContents.value.find((episode) => episode.no === episodeNo) || null
}

function syncSelectedEpisodeNo() {
  const firstEpisode = orderedCourseContents.value[0]
  if (!firstEpisode) {
    return
  }
  if (!findEpisode(selectedEpisodeNo.value)) {
    selectedEpisodeNo.value = firstEpisode.no
  }
}

function buildEpisodeVideoPath(episode) {
  return String(episode?.videoUrl || '').trim()
}

function buildEpisodeFileName(episode) {
  const videoPath = String(episode?.videoUrl || '').trim()
  if (!videoPath) {
    return '未配置视频'
  }
  const cleanPath = videoPath.split('?')[0]
  const segments = cleanPath.split('/')
  return segments[segments.length - 1] || videoPath
}

function formatDuration(value) {
  const minutes = Number(value || 0)
  if (!Number.isFinite(minutes) || minutes <= 0) {
    return '时长待定'
  }
  return `${minutes}分钟`
}

function syncCourseCache() {
  if (!course.value?.id) {
    return
  }
  uni.setStorageSync(CURRENT_COURSE_KEY, {
    ...course.value,
  })
}

async function likeCourse() {
  if (!ensureLogin()) {
    return
  }
  try {
    await toggleLike(course.value)
    liked.value = !liked.value
    course.value.countLike = Math.max(0, Number(course.value.countLike || 0) + (liked.value ? 1 : -1))
    syncCourseCache()
    uni.showToast({ title: '已操作', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function collectCourse() {
  if (!ensureLogin()) {
    return
  }
  try {
    const response = await toggleCollect(course.value)
    collected.value = Boolean(pickResult(response, 'collected', !collected.value))
    const nextCount = pickResult(response, 'countCollect', null)
    if (nextCount !== null) {
      course.value.countCollect = Math.max(0, Number(nextCount || 0))
    }
    syncCourseCache()
    uni.showToast({ title: collected.value ? '已收藏' : '已取消收藏', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function shareCurrent() {
  if (!ensureLogin()) {
    return
  }
  try {
    await shareCourse(course.value)
    course.value.countShare = Number(course.value.countShare || 0) + 1
    syncCourseCache()
    showShareDialog(`/pages/course/detail?id=${course.value.id}`, '分享课程')
  } catch (error) {
    uni.showToast({ title: error.message || '转发失败', icon: 'none' })
  }
}

async function purchase() {
  if (!ensureLogin()) {
    return
  }
  if (purchased.value) {
    await playEpisode(selectedEpisodeNo.value)
    return
  }
  if (salePrice.value === null) {
    uni.showToast({ title: '课程价格尚未加载，请稍后重试', icon: 'none' })
    return
  }
  try {
    await purchaseCourse(course.value.id, selectedCouponUserId.value)
    purchased.value = true
    await loadCourseContents()
    syncCourseCache()
    uni.showToast({ title: '下单成功', icon: 'success' })
  } catch (error) {
    if (redirectForRecharge(error, `/pages/course/detail?id=${course.value.id}`)) {
      return
    }
    uni.showToast({ title: error.message || '下单失败', icon: 'none' })
  }
}

async function loadAvailableCourseCoupons() {
  const courseId = course.value.id || routeCourseId.value
  if (!courseId) {
    return
  }
  try {
    const response = await getAvailableCoupons(2, courseId)
    coupons.value = pickResult(response, 'coupons', [])
    selectedCouponUserId.value = coupons.value[0]?.couponUserId || null
  } catch (error) {
    coupons.value = []
    selectedCouponUserId.value = null
  }
}

function selectCoupon(event) {
  const option = couponOptions.value[Number(event.detail.value || 0)]
  selectedCouponUserId.value = option?.couponUserId || null
}

async function loadOrderStatus() {
  if (!course.value.id) {
    purchased.value = false
    courseContents.value = []
    return
  }
  try {
    const response = await getOrderStatus(course.value.id)
    purchased.value = Boolean(pickResult(response, 'purchased', false))
    if (!purchased.value) {
      courseContents.value = []
    }
  } catch (error) {
    purchased.value = false
    courseContents.value = []
  }
}

async function loadCourseContents() {
  const courseId = course.value.id || routeCourseId.value
  if (!courseId) {
    courseContents.value = []
    return
  }
  try {
    const response = await getCourseContents(courseId)
    const contents = pickResult(response, 'ep', [])
    courseContents.value = Array.isArray(contents) ? contents : []
    syncSelectedEpisodeNo()
  } catch (error) {
    courseContents.value = []
  }
}

async function loadLikeStatus() {
  if (!course.value.id) {
    liked.value = false
    return
  }
  try {
    const response = await getLikeStatus(course.value.id)
    liked.value = Boolean(pickResult(response, 'liked', false))
  } catch (error) {
    liked.value = false
  }
}

async function loadCollectStatus() {
  if (!course.value.id) {
    collected.value = false
    return
  }
  try {
    const response = await getCollectStatus(course.value.id)
    collected.value = Boolean(pickResult(response, 'collected', false))
  } catch (error) {
    collected.value = false
  }
}

</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #eef8fa;
  padding-bottom: calc(116rpx + env(safe-area-inset-bottom));
}

.transparent {
  background: #ffffff;
}

.hero {
  height: 360rpx;
  background: #d3e8ed;
  position: relative;
  overflow: hidden;
}

.hero-image,
.hero-video,
.hero-fallback {
  width: 100%;
  height: 100%;
}

.hero-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-mark {
  width: 116rpx;
  height: 116rpx;
  border-radius: 18rpx;
  border: 10rpx solid rgba(255, 255, 255, 0.48);
  color: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  font-weight: 900;
}

.play-button {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  height: 62rpx;
  padding: 0 26rpx;
  border-radius: 31rpx;
  background: #18c6a6;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 800;
}

.tabs {
  height: 86rpx;
  background: #e9f7f8;
  display: flex;
  border-bottom: 1rpx solid #d8ebee;
  position: sticky;
  top: 0;
  z-index: 40;
}

.tab {
  flex: 1;
  height: 86rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  color: #8a969c;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 86rpx;
}

.tab.active {
  color: #18bda4;
  font-weight: 800;
}

.tab::after {
  content: '';
  position: absolute;
  left: 30%;
  right: 30%;
  bottom: 0;
  height: 4rpx;
  border: 0;
  background: transparent;
  border-radius: 2rpx;
}

.tab.active::after {
  background: #18c6a6;
}

.detail-content {
  position: relative;
  z-index: 10;
}

.course-main {
  padding: 22rpx 26rpx 40rpx;
}

.course-title {
  display: block;
  color: #6e7f86;
  font-size: 36rpx;
  line-height: 48rpx;
  font-weight: 800;
}

.price-row {
  margin-top: 18rpx;
  padding-bottom: 18rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #c9dfe4;
}

.price {
  color: #f06b4f;
  font-size: 32rpx;
  font-weight: 800;
}

.origin {
  margin-left: 12rpx;
  color: #98a5aa;
  text-decoration: line-through;
}

.stat {
  margin-left: 28rpx;
  color: #7bbfb5;
  font-size: 24rpx;
}

.course-coupon-row {
  min-height: 78rpx;
  border-bottom: 1rpx solid #c9dfe4;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #61757d;
  font-size: 24rpx;
}

.course-coupon-picker {
  max-width: 470rpx;
  color: #f06b4f;
  font-size: 22rpx;
  text-align: right;
}

.course-coupon-empty {
  color: #99a7ac;
  font-size: 21rpx;
}

.tab-panel {
  padding-top: 24rpx;
}

.teacher-block {
  display: flex;
  align-items: center;
}

.teacher-avatar {
  width: 138rpx;
  height: 138rpx;
  border-radius: 69rpx;
  border: 2rpx solid #a5bdc4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7d8b91;
  flex: none;
}

.teacher-info {
  min-width: 0;
  padding-left: 28rpx;
}

.teacher-name {
  display: block;
  color: #667a82;
  font-size: 32rpx;
  font-weight: 800;
}

.teacher-desc,
.detail-text {
  display: block;
  margin-top: 14rpx;
  color: #87969d;
  font-size: 28rpx;
  line-height: 42rpx;
}

.divider {
  height: 1rpx;
  background: #c9dfe4;
  margin: 24rpx 0;
}

.panel-title {
  display: block;
  color: #6e7f86;
  font-size: 32rpx;
  font-weight: 800;
}

.episode-row {
  width: 100%;
  min-height: 96rpx;
  padding: 12rpx 8rpx;
  margin: 0;
  border: 0;
  border-radius: 0;
  border-bottom: 1rpx solid #d8e9ec;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: transparent;
  text-align: left;
  line-height: normal;
}

.episode-row::after {
  border: 0;
}

.episode-selected {
  background: rgba(24, 198, 166, 0.08);
}

.episode-playing {
  background: rgba(24, 198, 166, 0.14);
}

.episode-main {
  min-width: 0;
  flex: 1;
  padding-right: 20rpx;
}

.episode-title,
.episode-file {
  display: block;
}

.episode-title {
  color: #5f7077;
  font-size: 28rpx;
  line-height: 40rpx;
  font-weight: 700;
}

.episode-title-selected,
.episode-title-playing {
  color: #18bda4;
  font-weight: 800;
}

.episode-file {
  margin-top: 6rpx;
  color: #98a7ad;
  font-size: 22rpx;
  line-height: 30rpx;
}

.episode-file-selected {
  color: #45c5b5;
}

.episode-status {
  flex: none;
  color: #87969d;
  font-size: 28rpx;
}

.episode-status-selected,
.episode-status-playing {
  color: #18bda4;
  font-weight: 800;
}

.comment-form {
  display: flex;
  align-items: center;
  padding-bottom: 20rpx;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  border-radius: 36rpx;
  background: #ffffff;
  padding: 0 24rpx;
  font-size: 26rpx;
}

.placeholder {
  color: #aab5ba;
}

.comment-send {
  width: 110rpx;
  height: 64rpx;
  margin-left: 14rpx;
  border-radius: 32rpx;
  background: #18c6a6;
  color: #ffffff;
  font-size: 24rpx;
}

.login-tip {
  height: 78rpx;
  border-radius: 39rpx;
  background: #ffffff;
  color: #18bda4;
  font-size: 28rpx;
}

.comment-item {
  padding: 20rpx 4rpx;
  border-bottom: 1rpx solid #d8e9ec;
}

.comment-content {
  display: block;
  color: #5f7077;
  font-size: 27rpx;
  line-height: 40rpx;
}

.comment-meta {
  display: block;
  margin-top: 8rpx;
  color: #92a0a6;
  font-size: 22rpx;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 30;
  height: calc(116rpx + env(safe-area-inset-bottom));
  padding: 14rpx 18rpx env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  gap: 10rpx;
  border-top: 1rpx solid #dfe8eb;
}

.tool {
  width: 106rpx;
  height: 76rpx;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5rpx;
  color: #89969b;
}

.tool::after {
  display: none;
  border: 0;
}

.tool-icon {
  color: #9aa7ab;
  font-size: 34rpx;
  line-height: 34rpx;
}

.tool-label {
  font-size: 20rpx;
  line-height: 22rpx;
}

.tool.like-active {
  color: #e95656;
}

.tool.like-active .tool-icon {
  color: #ed4f52;
}

.tool.collect-active {
  color: #d69b18;
}

.tool.collect-active .tool-icon {
  color: #f0b52e;
}

.share-tool {
  color: #2aa993;
}

.share-icon {
  color: #21b89e;
  font-size: 33rpx;
}

.order {
  flex: 1;
  height: 76rpx;
  border-radius: 16rpx;
  background: #f27a52;
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 800;
}

.order.purchased {
  background: #42c6b2;
}

.single {
  padding-left: 24rpx;
  padding-right: 24rpx;
}

.login-action {
  width: 100%;
  height: 82rpx;
  border-radius: 16rpx;
  background: #42c6b2;
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 800;
}
</style>

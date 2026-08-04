<template>
  <view class="creations-page">
    <SubPageNav title="我的创作">
      <button class="nav-create" @tap="createCurrent">{{ activeTab === 'moments' ? '发微圈' : '传课程' }}</button>
    </SubPageNav>

    <view class="content">
      <view class="creation-tabs">
        <button class="creation-tab" :class="{ active: activeTab === 'moments' }" @tap="switchTab('moments')">
          <text class="tab-icon">○</text>
          <text>我的微圈</text>
        </button>
        <button class="creation-tab" :class="{ active: activeTab === 'courses' }" @tap="switchTab('courses')">
          <text class="tab-icon">▷</text>
          <text>视频课程</text>
        </button>
      </view>

      <view v-if="loading" class="loading-state">正在加载创作…</view>

      <template v-else-if="activeTab === 'moments'">
        <view
          v-for="item in moments"
          :key="item.id"
          class="moment-item card"
          @tap="openMoment(item)"
        >
          <image v-if="item.coverUrl" class="item-cover" :src="fileUrl(item.coverUrl)" mode="aspectFill" />
          <view v-else class="item-cover empty-cover">○</view>
          <view class="item-main">
            <text class="item-title">{{ item.title || '未命名微圈' }}</text>
            <text class="item-desc">{{ momentSummary(item) }}</text>
            <text class="item-meta">{{ dateText(item.createTime) || '刚刚' }} · {{ Number(item.countComment || 0) }} 条评论</text>
          </view>
          <button class="item-delete" @tap.stop="removeMoment(item)">删除</button>
        </view>
        <EmptyState
          v-if="!moments.length"
          title="还没有发布微圈"
          description="点击右上角“发微圈”开始创作"
        />
      </template>

      <template v-else>
        <view v-if="!isCreator" class="creator-gate card">
          <view class="gate-icon">▷</view>
          <text class="gate-title">视频课程需要创作者认证</text>
          <button class="primary-button gate-button" @tap="goCreatorApply">去认证</button>
        </view>

        <template v-else>
          <view v-for="item in courses" :key="item.id" class="course-item card">
            <image v-if="item.coverUrl" class="course-cover" :src="fileUrl(item.coverUrl)" mode="aspectFill" />
            <view v-else class="course-cover empty-cover">▷</view>
            <view class="course-main">
              <view class="course-title-row">
                <text class="item-title course-title">{{ item.title || '未命名课程' }}</text>
                <text class="status-tag" :class="statusClass(item)">{{ statusText(item) }}</text>
              </view>
              <text class="item-desc">{{ item.cateName || '未分类' }} · {{ Number(item.episodeNum || 0) }} 集 · {{ Number(item.duration || 0) }} 分钟</text>
              <view class="course-stats">
                <text class="price">¥{{ moneyText(item.priceOriginal) }}</text>
                <text>{{ Number(item.salesCount || 0) }} 人购买</text>
                <text>收益 ¥{{ moneyText(item.incomeTotal) }}</text>
              </view>
              <view class="course-actions">
                <button class="edit-button" @tap="editCourse(item)">编辑课程</button>
                <button class="delete-button" @tap="removeCourse(item)">删除</button>
              </view>
            </view>
          </view>
          <EmptyState
            v-if="!courses.length"
            title="还没有视频课程"
            description="点击右上角“传课程”添加课程资料和分集"
          />
        </template>
      </template>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { deleteCreatorCourse, getCreatorCourses } from '@/api/creator'
import { deleteMoment, getMyMoments } from '@/api/moment'
import { assetUrl, dateText } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const CURRENT_MOMENT_KEY = 'SIYUN_CURRENT_MOMENT'
const userStore = useUserStore()
const activeTab = ref('moments')
const loading = ref(true)
const moments = ref([])
const courses = ref([])
const initialized = ref(false)

const isCreator = computed(() =>
  Boolean(userStore.user?.chinaId)
  && Number(userStore.user?.createrVerified || 0) === 1,
)

onLoad((query = {}) => {
  if (query.tab === 'courses') {
    activeTab.value = 'courses'
  }
})

onShow(async () => {
  userStore.hydrate()
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await userStore.refresh()
  } catch (error) {
  }
  initialized.value = true
  await loadCurrent()
})

async function loadCurrent() {
  if (!initialized.value) {
    return
  }
  loading.value = true
  try {
    if (activeTab.value === 'moments') {
      const response = await getMyMoments()
      moments.value = pickResult(response, 'myMoments', [])
    } else if (isCreator.value) {
      const response = await getCreatorCourses()
      courses.value = pickResult(response, 'courses', [])
    } else {
      courses.value = []
    }
  } catch (error) {
    if (activeTab.value === 'moments') {
      moments.value = []
    } else {
      courses.value = []
    }
    uni.showToast({ title: error.message || '创作加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(tab) {
  if (activeTab.value === tab) {
    return
  }
  activeTab.value = tab
  loadCurrent()
}

function createCurrent() {
  if (activeTab.value === 'moments') {
    uni.navigateTo({ url: '/pages/moment/edit' })
    return
  }
  if (!isCreator.value) {
    uni.showToast({ title: '通过创作者认证后才能上传课程', icon: 'none' })
    goCreatorApply()
    return
  }
  uni.navigateTo({ url: '/pages/mine/video-upload' })
}

function fileUrl(value) {
  return assetUrl(value)
}

function momentSummary(moment) {
  const plainText = String(moment.content || '')
    .replace(/<img[^>]*>/gi, ' [图片] ')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return plainText || '暂无文字内容'
}

function openMoment(moment) {
  uni.setStorageSync(CURRENT_MOMENT_KEY, moment)
  uni.navigateTo({ url: `/pages/moment/detail?id=${moment.id}` })
}

function removeMoment(moment) {
  uni.showModal({
    title: '删除微圈',
    content: '确认删除这条微圈吗？',
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await deleteMoment(moment)
        moments.value = moments.value.filter((item) => item.id !== moment.id)
        uni.showToast({ title: '微圈已删除', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '删除失败', icon: 'none' })
      }
    },
  })
}

function editCourse(course) {
  uni.navigateTo({ url: `/pages/mine/video-upload?id=${course.id}` })
}

function removeCourse(course) {
  uni.showModal({
    title: '删除视频课程',
    content: '未产生订单的课程可删除，封面和分集视频也会清理。确认继续吗？',
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await deleteCreatorCourse(course.id)
        courses.value = courses.value.filter((item) => item.id !== course.id)
        uni.showToast({ title: '课程已删除', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '删除失败', icon: 'none' })
      }
    },
  })
}

function goCreatorApply() {
  if (!userStore.user?.chinaId) {
    uni.navigateTo({ url: '/pages/mine/identity' })
    return
  }
  uni.navigateTo({ url: '/pages/moment/creator-apply' })
}

function statusText(course) {
  const audit = Number(course.statusAudit)
  if (audit === 1) return '审核中'
  if (audit === 2) return '审核未通过'
  if (audit === 3 && Number(course.statusShelf) === 1) return '已上架'
  if (audit === 3) return '已通过·未上架'
  return '未提交'
}

function statusClass(course) {
  const audit = Number(course.statusAudit)
  if (audit === 3 && Number(course.statusShelf) === 1) return 'success'
  if (audit === 2) return 'danger'
  return 'pending'
}

function moneyText(value) {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}
</script>

<style scoped>
.creations-page { min-height: 100vh; background: #f4f8f8; padding-bottom: 40rpx; }
.nav-create { height: 56rpx; padding: 0 18rpx; border-radius: 28rpx; background: #e4f8f4; color: #16aa92; font-size: 23rpx; font-weight: 800; }
.creation-tabs { height: 84rpx; padding: 7rpx; border-radius: 18rpx; background: #e5edef; display: flex; gap: 8rpx; }
.creation-tab { flex: 1; height: 70rpx; border-radius: 14rpx; color: #7b898e; display: flex; align-items: center; justify-content: center; gap: 10rpx; font-size: 26rpx; }
.creation-tab.active { background: #fff; color: #18ad95; font-weight: 900; box-shadow: 0 7rpx 18rpx rgba(30, 58, 64, .07); }
.tab-icon { font-size: 31rpx; font-weight: 900; }
.loading-state { min-height: 440rpx; display: flex; align-items: center; justify-content: center; color: #8d9ca0; font-size: 25rpx; }
.moment-item, .course-item { margin-top: 20rpx; margin-bottom: 18rpx; padding: 20rpx; display: flex; align-items: center; gap: 18rpx; overflow: hidden; }
.item-cover { flex: 0 0 auto; width: 150rpx; height: 118rpx; border-radius: 13rpx; background: #e8f2f1; }
.empty-cover { color: #24b59d; display: flex; align-items: center; justify-content: center; font-size: 46rpx; font-weight: 900; }
.item-main, .course-main { flex: 1; min-width: 0; }
.item-title { display: block; color: #27373d; font-size: 27rpx; line-height: 36rpx; font-weight: 900; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-desc { display: block; margin-top: 8rpx; color: #718087; font-size: 22rpx; line-height: 31rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-meta { display: block; margin-top: 10rpx; color: #9aa7aa; font-size: 20rpx; }
.item-delete { flex: 0 0 auto; padding: 14rpx 8rpx; color: #ed705a; font-size: 22rpx; }
.course-item { align-items: flex-start; }
.course-cover { flex: 0 0 auto; width: 188rpx; height: 142rpx; border-radius: 13rpx; background: #e8f2f1; }
.course-title-row { display: flex; align-items: center; gap: 10rpx; min-width: 0; }
.course-title { flex: 1; min-width: 0; }
.status-tag { flex: 0 0 auto; height: 38rpx; padding: 0 11rpx; border-radius: 19rpx; display: inline-flex; align-items: center; font-size: 18rpx; }
.status-tag.pending { color: #b47c20; background: #fff5dc; }
.status-tag.success { color: #15977f; background: #def7f0; }
.status-tag.danger { color: #d45c49; background: #fff0ed; }
.course-stats { margin-top: 11rpx; display: flex; flex-wrap: wrap; gap: 8rpx 16rpx; color: #87959a; font-size: 20rpx; }
.course-stats .price { color: #f1694e; font-size: 24rpx; font-weight: 900; }
.course-actions { margin-top: 14rpx; display: flex; gap: 12rpx; }
.edit-button, .delete-button { height: 50rpx; padding: 0 17rpx; border-radius: 25rpx; font-size: 21rpx; }
.edit-button { color: #16aa92; background: #e5f8f4; }
.delete-button { color: #e36954; background: #fff0ed; }
.creator-gate { margin-top: 22rpx; padding: 48rpx 36rpx; display: flex; flex-direction: column; align-items: center; text-align: center; }
.gate-icon { width: 96rpx; height: 96rpx; border-radius: 48rpx; background: #e2f8f3; color: #18b096; display: flex; align-items: center; justify-content: center; font-size: 48rpx; font-weight: 900; }
.gate-title { margin-top: 24rpx; color: #2b3a40; font-size: 30rpx; font-weight: 900; }
.gate-button { width: 280rpx; height: 72rpx; margin-top: 28rpx; font-size: 27rpx; }
</style>

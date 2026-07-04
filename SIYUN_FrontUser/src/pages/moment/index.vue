<template>
  <view class="page">
    <view class="top-bar moment-top">
      <view class="top-title">微圈</view>
      <button class="publish" @tap="goPublish">发布</button>
      <view class="search-box moment-search">
        <text class="search-mark"></text>
        <input
          v-model.trim="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索微圈"
          placeholder-class="search-placeholder"
          @confirm="search"
        />
        <button class="search-arrow" @tap="search">›</button>
      </view>
    </view>

    <view class="content">
      <view class="segmented">
        <button class="seg" :class="{ active: mode === 'all' }" @tap="switchMode('all')">推荐</button>
        <button class="seg" :class="{ active: mode === 'mine' }" @tap="switchMode('mine')">我的</button>
      </view>

      <MomentCard
        v-for="item in moments"
        :key="item.id"
        :moment="item"
        :current-user-id="userStore.user?.id || 0"
        compact
        :show-actions="false"
        @select="openMoment"
        @delete="removeMoment"
        @like="handleLike"
        @collect="handleCollect"
        @share="handleShare"
      />
      <EmptyState v-if="!moments.length" :title="emptyTitle" />
    </view>

    <BottomTab active="moment" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import BottomTab from '@/components/BottomTab.vue'
import EmptyState from '@/components/EmptyState.vue'
import MomentCard from '@/components/MomentCard.vue'
import {
  collectMoment,
  deleteMoment,
  getMyMoments,
  likeMoment,
  searchMoments,
  shareMoment,
} from '@/api/moment'
import { isNotFoundError, isSessionExpiredError, pickResult } from '@/utils/request'
import { h5ReplaceTo } from '@/utils/navigation'
import { useUserStore } from '@/stores/user'

const CURRENT_MOMENT_KEY = 'SIYUN_CURRENT_MOMENT'
const userStore = useUserStore()
const keyword = ref('')
const mode = ref('all')
const moments = ref([])

const isIdentified = computed(() => Boolean(userStore.user?.chinaId))
const isCreator = computed(() => Number(userStore.user?.createrVerified || 0) === 1)
const emptyTitle = computed(() => (keyword.value ? '暂无搜索结果' : '暂无微圈'))

onShow(async () => {
  userStore.hydrate()
  if (userStore.isLoggedIn) {
    try {
      await userStore.refresh()
    } catch (error) {
      if (!isSessionExpiredError(error) && !isNotFoundError(error)) {
        uni.showToast({ title: error.message || '用户状态刷新失败', icon: 'none' })
      }
    }
  }
  loadMoments()
})

onPullDownRefresh(async () => {
  await loadMoments()
  uni.stopPullDownRefresh()
})

async function loadMoments() {
  if (mode.value === 'mine') {
    await loadMine()
    return
  }
  try {
    const response = await searchMoments(keyword.value)
    moments.value = pickResult(response, 'moments', [])
  } catch (error) {
    moments.value = []
    if (!isNotFoundError(error)) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    }
  }
}

function search() {
  mode.value = 'all'
  loadMoments()
}

async function loadMine() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    moments.value = []
    return
  }
  try {
    const response = await getMyMoments()
    moments.value = pickResult(response, 'myMoments', [])
  } catch (error) {
    moments.value = []
    if (!isNotFoundError(error)) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    }
  }
}

function switchMode(nextMode) {
  mode.value = nextMode
  loadMoments()
}

function goPublish() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (!isIdentified.value) {
    uni.showToast({ title: '请先完成实名认证', icon: 'none' })
    goMine()
    return
  }
  if (!isCreator.value) {
    goCreatorApply()
    return
  }
  uni.navigateTo({ url: '/pages/moment/edit' })
}

function goMine() {
  if (h5ReplaceTo('/pages/mine/index')) {
    return
  }

  uni.redirectTo({ url: '/pages/mine/index' })
}

function goCreatorApply() {
  if (h5ReplaceTo('/pages/moment/creator-apply')) {
    return
  }

  uni.navigateTo({ url: '/pages/moment/creator-apply' })
}

function openMoment(moment) {
  uni.setStorageSync(CURRENT_MOMENT_KEY, moment)
  uni.navigateTo({
    url: `/pages/moment/detail?id=${moment.id}`,
  })
}

async function removeMoment(moment) {
  try {
    await deleteMoment(moment)
    moments.value = moments.value.filter((item) => item.id !== moment.id)
    uni.showToast({ title: '已删除', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '删除失败', icon: 'none' })
  }
}

async function handleLike(moment) {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await likeMoment(moment)
    uni.showToast({ title: '已操作', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function handleCollect(moment) {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await collectMoment(moment)
    uni.showToast({ title: '已操作', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function handleShare(moment) {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await shareMoment(moment)
    uni.showToast({ title: '已分享', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '分享失败', icon: 'none' })
  }
}
</script>

<style scoped>
.moment-top {
  position: relative;
}

.publish {
  position: absolute;
  right: 24rpx;
  top: calc(var(--status-bar-height) + 18rpx);
  height: 60rpx;
  padding: 0 22rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 800;
}

.moment-search {
  margin: 0 24rpx 20rpx;
  background: rgba(255, 255, 255, 0.34);
}

.search-mark {
  margin-right: 12rpx;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 800;
}

.search-placeholder {
  color: rgba(255, 255, 255, 0.78);
}

.moment-search .search-input {
  flex: 1;
  color: #ffffff;
}

.search-arrow {
  width: 52rpx;
  height: 52rpx;
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.26);
  color: #ffffff;
  font-size: 40rpx;
  line-height: 48rpx;
  font-weight: 900;
}

.segmented {
  height: 72rpx;
  padding: 6rpx;
  border-radius: 16rpx;
  background: #e5eef0;
  display: flex;
  margin-bottom: 20rpx;
}

.seg {
  flex: 1;
  height: 60rpx;
  border-radius: 12rpx;
  color: #7e8b91;
  font-size: 26rpx;
}

.seg.active {
  background: #ffffff;
  color: #18bda4;
  font-weight: 800;
  box-shadow: 0 6rpx 16rpx rgba(23, 46, 54, 0.06);
}
</style>

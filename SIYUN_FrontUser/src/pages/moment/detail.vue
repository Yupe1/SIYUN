<template>
  <view class="detail-page">
    <view class="nav-bar">
      <button class="nav-back" @tap="goBack">‹ 返回</button>
      <text class="nav-title">微圈详情</text>
    </view>

    <view class="content">
      <view v-if="moment" class="card detail-card">
        <text class="title">{{ moment.title || '未命名动态' }}</text>
        <view class="meta-row">
          <text>{{ dateText(moment.createTime) || '刚刚' }}</text>
          <text>{{ compactNumber(moment.countView) }} 浏览</text>
        </view>
        <image v-if="cover" class="cover" :src="cover" mode="aspectFill" />
        <text class="article">{{ moment.content || '暂无内容' }}</text>
        <view class="stats">
          <text>{{ compactNumber(moment.countLike) }} 点赞</text>
          <text>{{ compactNumber(moment.countComment) }} 评论</text>
          <text>{{ compactNumber(moment.countShare) }} 分享</text>
        </view>
        <view class="actions">
          <button class="action" @tap="handleLike">点赞</button>
          <button class="action" @tap="handleCollect">收藏</button>
          <button class="action" @tap="handleShare">分享</button>
        </view>
      </view>
      <EmptyState v-else title="微圈不存在" />
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import { collectMoment, getMoment, likeMoment, shareMoment } from '@/api/moment'
import { assetUrl, compactNumber, dateText } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const CURRENT_MOMENT_KEY = 'SIYUN_CURRENT_MOMENT'
const userStore = useUserStore()
const moment = ref(null)
const cover = computed(() => assetUrl(moment.value?.coverUrl))

onLoad((query = {}) => {
  userStore.hydrate()
  const cached = uni.getStorageSync(CURRENT_MOMENT_KEY)
  if (cached && String(cached.id) === String(query.id || '')) {
    moment.value = cached
  }
  if (query.id) {
    loadMoment(query.id)
  }
})

async function loadMoment(id) {
  try {
    const response = await getMoment(id)
    moment.value = pickResult(response, 'moment', moment.value)
  } catch (error) {
    if (!moment.value) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    }
  }
}

function goBack() {
  uni.navigateBack({
    fail: () => uni.redirectTo({ url: '/pages/moment/index' }),
  })
}

function requireLogin() {
  if (userStore.isLoggedIn) {
    return true
  }
  uni.navigateTo({ url: '/pages/auth/login' })
  return false
}

async function handleLike() {
  if (!moment.value || !requireLogin()) {
    return
  }
  try {
    await likeMoment(moment.value)
    uni.showToast({ title: '已操作', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function handleCollect() {
  if (!moment.value || !requireLogin()) {
    return
  }
  try {
    await collectMoment(moment.value)
    uni.showToast({ title: '已操作', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

async function handleShare() {
  if (!moment.value || !requireLogin()) {
    return
  }
  try {
    await shareMoment(moment.value)
    uni.showToast({ title: '已分享', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '分享失败', icon: 'none' })
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f4f8f8;
}

.detail-card {
  padding: 28rpx;
}

.title {
  display: block;
  color: #253238;
  font-size: 38rpx;
  line-height: 50rpx;
  font-weight: 900;
}

.meta-row {
  margin-top: 16rpx;
  display: flex;
  gap: 24rpx;
  color: #95a2a8;
  font-size: 23rpx;
}

.cover {
  width: 100%;
  height: 330rpx;
  margin-top: 24rpx;
  border-radius: 12rpx;
  background: #dce8eb;
}

.article {
  display: block;
  margin-top: 26rpx;
  color: #4f5f66;
  font-size: 29rpx;
  line-height: 48rpx;
  white-space: pre-wrap;
}

.stats {
  margin-top: 28rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx 28rpx;
  color: #8b989e;
  font-size: 23rpx;
}

.actions {
  margin-top: 24rpx;
  display: flex;
  gap: 16rpx;
}

.action {
  flex: 1;
  height: 64rpx;
  border-radius: 32rpx;
  background: #e8f8f4;
  color: #18bda4;
  font-size: 24rpx;
  font-weight: 800;
}
</style>

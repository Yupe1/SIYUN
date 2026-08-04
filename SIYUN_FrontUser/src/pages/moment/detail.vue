<template>
  <view class="detail-page">
    <view class="nav-bar">
      <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
      <text class="nav-title">微圈详情</text>
    </view>

    <view class="content">
      <view v-if="moment" class="card detail-card">
        <text class="title">{{ moment.title || '未命名动态' }}</text>
        <view class="meta-row">
          <text>{{ dateText(moment.createTime) || '刚刚' }}</text>
          <!-- <text>{{ compactNumber(moment.countView) }} 浏览</text> -->
        </view>
        <image v-if="cover" class="cover" :src="cover" mode="aspectFill" />
        <video v-if="video" class="cover detail-video" :src="video" controls object-fit="cover" />
        <rich-text class="article rich-article" :nodes="moment.content || '暂无内容'" />
        <view class="stats">
          <text>{{ compactNumber(moment.countLike) }} 点赞</text>
          <text>{{ compactNumber(moment.countComment) }} 评论</text>
          <text>{{ compactNumber(moment.countCollect) }} 收藏</text>
        </view>
        <view class="actions">
          <button class="action" :class="{ 'like-active': liked }" @tap="handleLike">
            <text class="action-icon">{{ liked ? '♥' : '♡' }}</text>
            <text>点赞</text>
          </button>
          <button class="action" :class="{ 'collect-active': collected }" @tap="handleCollect">
            <text class="action-icon">{{ collected ? '★' : '☆' }}</text>
            <text>收藏</text>
          </button>
          <button class="action share-action" @tap="handleShare">
            <text class="action-icon share-icon">↗</text>
            <text>分享</text>
          </button>
        </view>
        <view class="comment-section">
          <text class="comment-title">评论</text>
          <CommentThread
            :entity-id="Number(moment.id)"
            :entity-type="2"
            @change="handleCommentChange"
          />
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
import CommentThread from '@/components/CommentThread.vue'
import {
  collectMoment,
  getMoment,
  getMomentCollectStatus,
  getMomentLikeStatus,
  likeMoment,
  shareMoment,
} from '@/api/moment'
import { assetUrl, compactNumber, dateText } from '@/utils/format'
import { h5ReplaceTo } from '@/utils/navigation'
import { isSessionExpiredError, pickResult } from '@/utils/request'
import { showShareDialog } from '@/utils/share'
import { useUserStore } from '@/stores/user'

const CURRENT_MOMENT_KEY = 'SIYUN_CURRENT_MOMENT'
const userStore = useUserStore()
const moment = ref(null)
const liked = ref(false)
const collected = ref(false)
const cover = computed(() => assetUrl(moment.value?.coverUrl))
const video = computed(() => assetUrl(moment.value?.videoUrl))

onLoad((query = {}) => {
  userStore.hydrate()
  const cached = uni.getStorageSync(CURRENT_MOMENT_KEY)
  if (cached && String(cached.id) === String(query.id || '')) {
    setMoment(cached)
  }
  if (query.id) {
    loadMoment(query.id)
    loadActionStatus(query.id)
  }
})

async function loadActionStatus(id) {
  if (!userStore.isLoggedIn) {
    liked.value = false
    collected.value = false
    return
  }
  const [likeResult, collectResult] = await Promise.allSettled([
    getMomentLikeStatus(id),
    getMomentCollectStatus(id),
  ])
  liked.value = likeResult.status === 'fulfilled'
    ? Boolean(pickResult(likeResult.value, 'liked', false))
    : false
  collected.value = collectResult.status === 'fulfilled'
    ? Boolean(pickResult(collectResult.value, 'collected', false))
    : false
}

async function loadMoment(id) {
  try {
    const response = await getMoment(id)
    const nextMoment = pickResult(response, 'moment', moment.value)
    if (nextMoment) {
      setMoment(nextMoment)
    }
  } catch (error) {
    if (!moment.value) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    }
  }
}

function setMoment(nextMoment) {
  moment.value = nextMoment
  uni.setStorageSync(CURRENT_MOMENT_KEY, nextMoment)
}

function goBack() {
  if (h5ReplaceTo('/pages/moment/index')) {
    return
  }

  uni.navigateBack({
    fail: goMomentIndex,
  })
}

function goMomentIndex() {
  uni.redirectTo({ url: '/pages/moment/index' })
}

function goLogin() {
  if (h5ReplaceTo('/pages/auth/login', { reload: true })) {
    return
  }

  uni.redirectTo({ url: '/pages/auth/login' })
}

function requireLogin() {
  userStore.hydrate()
  if (userStore.isLoggedIn) {
    return true
  }
  goLogin()
  return false
}

function handleActionError(error, fallbackText) {
  if (isSessionExpiredError(error)) {
    userStore.expireSession()
    uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
    setTimeout(goLogin, 500)
    return
  }
  uni.showToast({ title: error.message || fallbackText, icon: 'none' })
}

async function handleLike() {
  if (!moment.value || !requireLogin()) {
    return
  }
  try {
    await likeMoment(moment.value)
    liked.value = !liked.value
    await loadMoment(moment.value.id)
    uni.showToast({ title: liked.value ? '已点赞' : '已取消点赞', icon: 'success' })
  } catch (error) {
    handleActionError(error, '操作失败')
  }
}

async function handleCollect() {
  if (!moment.value || !requireLogin()) {
    return
  }
  try {
    const response = await collectMoment(moment.value)
    collected.value = Boolean(pickResult(response, 'collected', !collected.value))
    const nextCount = pickResult(response, 'countCollect', null)
    if (nextCount !== null) {
      moment.value.countCollect = Math.max(0, Number(nextCount || 0))
      setMoment({ ...moment.value })
    } else {
      await loadMoment(moment.value.id)
    }
    uni.showToast({ title: collected.value ? '已收藏' : '已取消收藏', icon: 'success' })
  } catch (error) {
    handleActionError(error, '操作失败')
  }
}

async function handleShare() {
  if (!moment.value || !requireLogin()) {
    return
  }
  try {
    await shareMoment(moment.value)
    showShareDialog(`/pages/moment/detail?id=${moment.value.id}`, '分享微圈')
  } catch (error) {
    handleActionError(error, '分享失败')
  }
}

function handleCommentChange(delta) {
  if (!moment.value) return
  moment.value.countComment = Math.max(0, Number(moment.value.countComment || 0) + Number(delta || 0))
  setMoment({ ...moment.value })
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

.detail-video {
  background: #1f292e;
}

.article {
  display: block;
  margin-top: 26rpx;
  color: #4f5f66;
  font-size: 29rpx;
  line-height: 48rpx;
  white-space: pre-wrap;
}

.rich-article {
  overflow: hidden;
  word-break: break-word;
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
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  color: #18bda4;
  font-size: 24rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9rpx;
}

.action::after {
  display: none;
  border: 0;
}

.action-icon {
  color: #82969a;
  font-size: 34rpx;
  line-height: 1;
}

.action.like-active {
  color: #e95656;
}

.action.like-active .action-icon {
  color: #ed4f52;
}

.action.collect-active {
  color: #bf8611;
}

.action.collect-active .action-icon {
  color: #efb52f;
}

.share-action {
  color: #209f8a;
}

.share-action .share-icon {
  color: #20b89e;
}

.comment-section {
  margin-top: 30rpx;
  padding-top: 26rpx;
  border-top: 1rpx solid #e3ebec;
}

.comment-title {
  display: block;
  margin-bottom: 18rpx;
  color: #2d3a40;
  font-size: 30rpx;
  font-weight: 900;
}
</style>

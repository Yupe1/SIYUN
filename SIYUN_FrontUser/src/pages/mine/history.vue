<template>
  <view class="sub-page">
    <SubPageNav title="学习记录" />
    <view class="content">
      <view v-for="item in history" :key="item.courseId" class="history-card card">
        <image v-if="cover(item.coverUrl)" class="history-cover" :src="cover(item.coverUrl)" mode="aspectFill" />
        <view v-else class="history-cover placeholder-cover">课</view>
        <view class="history-main">
          <text class="history-title">{{ item.courseTitle }}</text>
          <text class="history-time">{{ dateText(item.startTime) }}</text>
          <text class="history-duration">{{ durationText(item) }}</text>
        </view>
      </view>
      <EmptyState v-if="!history.length" title="暂无学习记录" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getStudyHistory } from '@/api/mine'
import { assetUrl, dateText } from '@/utils/format'
import { pickResult } from '@/utils/request'

const history = ref([])

onShow(loadHistory)

async function loadHistory() {
  try {
    const response = await getStudyHistory()
    history.value = pickResult(response, 'history', [])
  } catch (error) {
    history.value = []
    uni.showToast({ title: error.message || '学习记录加载失败', icon: 'none' })
  }
}

function cover(url) {
  return assetUrl(url)
}

function durationText(item) {
  if (!item.startTime || !item.endTime) {
    return '学习进行中或未正常结束'
  }
  const minutes = Math.max(1, Math.round((new Date(item.endTime) - new Date(item.startTime)) / 60000))
  return `学习 ${minutes} 分钟`
}
</script>

<style scoped>
.sub-page { min-height: 100vh; background: #f4f8f8; }
.history-card { padding: 18rpx; margin-bottom: 18rpx; display: flex; }
.history-cover { width: 174rpx; height: 112rpx; border-radius: 13rpx; background: #dce8eb; }
.placeholder-cover { display: flex; align-items: center; justify-content: center; color: #18bda4; font-size: 36rpx; font-weight: 900; }
.history-main { flex: 1; min-width: 0; margin-left: 20rpx; }
.history-title { display: block; color: #2c3a40; font-size: 28rpx; font-weight: 800; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.history-time, .history-duration { display: block; margin-top: 10rpx; color: #8d999e; font-size: 22rpx; }
.history-duration { color: #20b99f; }
</style>

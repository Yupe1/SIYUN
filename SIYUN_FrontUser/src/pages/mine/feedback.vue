<template>
  <view class="feedback-page">
    <SubPageNav title="意见反馈" />
    <view class="content">
      <view class="form-card card">
        <text class="form-title">告诉我们哪里需要改进</text>
        <view class="type-row">
          <button v-for="item in types" :key="item.value" class="type-pill" :class="{ active: form.feedbackType === item.value }" @tap="form.feedbackType = item.value">
            {{ item.label }}
          </button>
        </view>
        <textarea v-model.trim="form.content" class="feedback-input" maxlength="1000" placeholder="请具体描述你遇到的问题或建议" placeholder-class="placeholder" />
        <button class="primary-button submit" @tap="submit">提交反馈</button>
      </view>

      <view class="section-head"><text class="section-title">反馈记录</text></view>
      <view v-for="item in feedback" :key="item.id" class="record-card card">
        <view class="record-head">
          <text class="record-type">{{ typeText(item.feedbackType) }}</text>
          <text class="record-status">{{ statusText(item.status) }}</text>
        </view>
        <text class="record-content">{{ item.content }}</text>
        <text class="record-time">{{ dateText(item.createTime) }}</text>
      </view>
      <EmptyState v-if="!feedback.length" title="暂无反馈记录" />
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getFeedback, submitFeedback } from '@/api/mine'
import { dateText } from '@/utils/format'
import { pickResult } from '@/utils/request'

const types = [
  { value: 1, label: '视频问题' },
  { value: 2, label: '商品问题' },
  { value: 3, label: '其他建议' },
]
const form = reactive({ feedbackType: 3, content: '' })
const feedback = ref([])

onShow(loadFeedback)

async function loadFeedback() {
  try {
    const response = await getFeedback()
    feedback.value = pickResult(response, 'feedback', [])
  } catch (error) {
    feedback.value = []
  }
}

async function submit() {
  if (!form.content) {
    uni.showToast({ title: '请填写反馈内容', icon: 'none' })
    return
  }
  try {
    await submitFeedback({ ...form })
    form.content = ''
    uni.showToast({ title: '感谢你的反馈', icon: 'success' })
    loadFeedback()
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  }
}

function typeText(type) {
  return ({ 1: '视频反馈', 2: '商品反馈', 3: '其他反馈' })[type] || '意见反馈'
}

function statusText(status) {
  return ({ 1: '已提交', 2: '处理中', 3: '已回复', 4: '已解决' })[status] || '处理中'
}
</script>

<style scoped>
.feedback-page { min-height: 100vh; background: #f4f8f8; }
.form-card { padding: 26rpx; }
.form-title { display: block; color: #2b3940; font-size: 30rpx; font-weight: 900; }
.type-row { margin-top: 22rpx; display: flex; gap: 12rpx; }
.type-pill { flex: 1; height: 58rpx; border-radius: 29rpx; background: #edf4f4; color: #7a888e; font-size: 22rpx; }
.type-pill.active { background: #e0f7f2; color: #18bda4; font-weight: 800; }
.feedback-input { width: 100%; height: 260rpx; margin-top: 22rpx; padding: 20rpx; border-radius: 14rpx; background: #f4f8f8; color: #344249; font-size: 25rpx; line-height: 40rpx; }
.submit { margin-top: 24rpx; }
.placeholder { color: #a5b0b4; }
.record-card { margin-bottom: 16rpx; padding: 22rpx; }
.record-head { display: flex; align-items: center; }
.record-type { flex: 1; color: #2d3a40; font-size: 26rpx; font-weight: 800; }
.record-status { color: #18bda4; font-size: 22rpx; }
.record-content { display: block; margin-top: 14rpx; color: #66757b; font-size: 24rpx; line-height: 38rpx; }
.record-time { display: block; margin-top: 12rpx; color: #9ba6aa; font-size: 20rpx; }
</style>

<template>
  <view class="service-page">
    <SubPageNav title="在线客服" />
    <scroll-view scroll-y class="message-list" :scroll-into-view="lastMessageId">
      <view class="service-tip">工作时间 9:00—18:00，非工作时间可留言</view>
      <view v-for="item in messages" :id="`message-${item.id}`" :key="item.id" class="message-row" :class="{ mine: isMine(item) }">
        <view class="message-avatar">{{ isMine(item) ? '我' : '服' }}</view>
        <view class="message-wrap">
          <text class="message-name">{{ isMine(item) ? '我' : (item.senderName || '在线客服') }}</text>
          <view class="message-bubble">{{ item.content }}</view>
          <text class="message-time">{{ dateText(item.sendTime) }}</text>
        </view>
      </view>
      <EmptyState v-if="!messages.length" title="发条消息开始咨询吧" />
    </scroll-view>
    <view class="composer">
      <input v-model.trim="content" class="message-input" confirm-type="send" maxlength="500" placeholder="输入你的问题" placeholder-class="placeholder" @confirm="send" />
      <button class="send-button" @tap="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getServiceMessages, sendServiceMessage } from '@/api/mine'
import { dateText } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const messages = ref([])
const content = ref('')
const lastMessageId = computed(() => messages.value.length ? `message-${messages.value[messages.value.length - 1].id}` : '')

onShow(() => {
  userStore.hydrate()
  loadMessages()
})

async function loadMessages() {
  try {
    const response = await getServiceMessages()
    messages.value = pickResult(response, 'messages', [])
  } catch (error) {
    messages.value = []
  }
}

function isMine(item) {
  return Number(item.senderId) === Number(userStore.user?.id)
}

async function send() {
  if (!content.value) {
    return
  }
  const text = content.value
  content.value = ''
  try {
    await sendServiceMessage(text)
    await loadMessages()
  } catch (error) {
    content.value = text
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  }
}
</script>

<style scoped>
.service-page { height: 100vh; background: #f4f8f8; overflow: hidden; }
.message-list { height: calc(100vh - var(--status-bar-height) - 184rpx - env(safe-area-inset-bottom)); padding: 20rpx 24rpx; box-sizing: border-box; }
.service-tip { width: fit-content; max-width: 90%; margin: 0 auto 24rpx; padding: 8rpx 16rpx; border-radius: 16rpx; background: #e8eeee; color: #869398; font-size: 20rpx; }
.message-row { margin-bottom: 22rpx; display: flex; align-items: flex-start; }
.message-row.mine { flex-direction: row-reverse; }
.message-avatar { flex: 0 0 auto; width: 62rpx; height: 62rpx; border-radius: 31rpx; background: #20bea3; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 23rpx; font-weight: 900; }
.mine .message-avatar { background: #6f8790; }
.message-wrap { max-width: 72%; margin-left: 14rpx; }
.mine .message-wrap { margin: 0 14rpx 0 0; text-align: right; }
.message-name, .message-time { display: block; color: #96a2a7; font-size: 19rpx; }
.message-bubble { margin-top: 7rpx; padding: 16rpx 20rpx; border-radius: 4rpx 18rpx 18rpx 18rpx; background: #fff; color: #344249; font-size: 25rpx; line-height: 38rpx; text-align: left; box-shadow: 0 6rpx 18rpx rgba(31,63,69,.05); }
.mine .message-bubble { border-radius: 18rpx 4rpx 18rpx 18rpx; background: #dff7f1; }
.message-time { margin-top: 6rpx; }
.composer { position: fixed; left: 0; right: 0; bottom: 0; height: calc(96rpx + env(safe-area-inset-bottom)); padding: 12rpx 20rpx env(safe-area-inset-bottom); background: #fff; border-top: 1rpx solid #e6eded; display: flex; align-items: flex-start; }
.message-input { flex: 1; height: 70rpx; padding: 0 22rpx; border-radius: 35rpx; background: #f0f5f5; color: #344249; font-size: 25rpx; }
.send-button { width: 112rpx; height: 70rpx; margin-left: 14rpx; border-radius: 35rpx; background: #20bea3; color: #fff; font-size: 24rpx; font-weight: 800; }
.placeholder { color: #9fabae; }
</style>

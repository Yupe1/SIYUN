<template>
  <view class="identity-page">
    <SubPageNav title="实名认证" />
    <view class="content">
      <view class="identity-card card">
        <view class="status-icon" :class="{ done: isIdentified }">{{ isIdentified ? '✓' : '证' }}</view>
        <text class="status-title">{{ isIdentified ? '已完成实名认证' : '完成实名认证' }}</text>
        <text class="status-desc">
          {{ isIdentified ? '你的实名信息已提交，可继续申请创作者认证。' : '实名认证用于保护账号安全，也是申请创作者的前置条件。' }}
        </text>
        <template v-if="!isIdentified">
          <input v-model.trim="chinaId" class="identity-input" placeholder="请输入身份证号码" placeholder-class="placeholder" />
          <button class="primary-button submit" @tap="submit">提交实名</button>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import SubPageNav from '@/components/SubPageNav.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const chinaId = ref('')
const isIdentified = computed(() => Boolean(userStore.user?.chinaId))

onShow(() => userStore.hydrate())

async function submit() {
  if (!/^\d{15}$|^\d{17}[\dXx]$/.test(chinaId.value)) {
    uni.showToast({ title: '身份证号码格式不正确', icon: 'none' })
    return
  }
  try {
    await userStore.identify({ chinaId: chinaId.value })
    chinaId.value = ''
    uni.showToast({ title: '实名成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  }
}
</script>

<style scoped>
.identity-page { min-height: 100vh; background: #f4f8f8; }
.identity-card { padding: 42rpx 28rpx; text-align: center; }
.status-icon { width: 104rpx; height: 104rpx; margin: 0 auto; border-radius: 52rpx; background: #eef4f4; color: #849399; display: flex; align-items: center; justify-content: center; font-size: 42rpx; font-weight: 900; }
.status-icon.done { background: #e2f8f3; color: #18bda4; }
.status-title { display: block; margin-top: 24rpx; color: #2d3a40; font-size: 32rpx; font-weight: 900; }
.status-desc { display: block; margin: 16rpx auto 0; max-width: 560rpx; color: #849197; font-size: 24rpx; line-height: 40rpx; }
.identity-input { height: 82rpx; margin-top: 32rpx; padding: 0 22rpx; border-radius: 14rpx; background: #f4f8f8; color: #2d3a40; font-size: 26rpx; text-align: left; }
.submit { margin-top: 22rpx; }
.placeholder { color: #a6b1b5; }
</style>

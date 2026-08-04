<template>
  <view class="apply-page">
    <view class="nav-bar">
      <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
      <text class="nav-title">创作者认证</text>
    </view>

    <view class="content">
      <view class="card apply-card">
        <view class="head">
          <text class="title">申请上传视频课程</text>
          <text class="status">{{ statusText }}</text>
        </view>
        <text class="desc">{{ hint }}</text>

        <template v-if="canApply">
          <textarea
            v-model.trim="form.applyReason"
            class="input reason"
            maxlength="300"
            placeholder="申请理由"
            placeholder-class="placeholder"
          />
          <input
            v-model.trim="form.fileUrl"
            class="input"
            placeholder="课程样例或作品地址（选填）"
            placeholder-class="placeholder"
          />
          <button class="primary-button submit" @tap="submit">提交申请</button>
        </template>

        <button v-else-if="!isIdentified" class="primary-button submit" @tap="goMine">去实名认证</button>
        <button v-else class="primary-button submit" @tap="goVideoUpload">去上传视频课程</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { applyCreator } from '@/api/moment'
import { h5ReplaceTo } from '@/utils/navigation'
import { isNotFoundError, isSessionExpiredError } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const form = reactive({
  applyReason: '',
  fileUrl: '',
})

const isIdentified = computed(() => Boolean(userStore.user?.chinaId))
const isCreator = computed(() => Number(userStore.user?.createrVerified || 0) === 1)
const canApply = computed(() => userStore.isLoggedIn && isIdentified.value && !isCreator.value)
const statusText = computed(() => {
  if (!userStore.isLoggedIn) {
    return '未登录'
  }
  if (!isIdentified.value) {
    return '未实名'
  }
  return isCreator.value ? '已通过' : '待提交'
})
const hint = computed(() => {
  if (!userStore.isLoggedIn) {
    return '登录后才能申请创作者认证。'
  }
  if (!isIdentified.value) {
    return '上传视频课程前需要先完成实名认证。'
  }
  if (isCreator.value) {
    return '你已经通过创作者认证，可以上传视频课程。'
  }
  return '提交创作者认证申请，后台审核通过后即可上传视频课程。'
})

onLoad(async () => {
  userStore.hydrate()
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await userStore.refresh()
  } catch (error) {
    if (!isSessionExpiredError(error) && !isNotFoundError(error)) {
      uni.showToast({ title: error.message || '用户状态刷新失败', icon: 'none' })
    }
  }
})

function goBack() {
  uni.navigateBack({
    fail: goMine,
  })
}

function goVideoUpload() {
  if (h5ReplaceTo('/pages/mine/video-upload')) {
    return
  }
  uni.redirectTo({ url: '/pages/mine/video-upload' })
}

function goMine() {
  if (h5ReplaceTo('/pages/mine/index')) {
    return
  }
  uni.redirectTo({ url: '/pages/mine/index' })
}

async function submit() {
  if (!form.applyReason) {
    uni.showToast({ title: '请输入申请理由', icon: 'none' })
    return
  }
  try {
    await applyCreator({
      applyReason: form.applyReason,
      fileUrl: form.fileUrl,
    })
    form.applyReason = ''
    form.fileUrl = ''
    uni.showToast({ title: '已提交审核', icon: 'success' })
    setTimeout(goMine, 500)
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  }
}
</script>

<style scoped>
.apply-page {
  min-height: 100vh;
  background: #f4f8f8;
}

.apply-card {
  padding: 28rpx;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  color: #253238;
  font-size: 34rpx;
  font-weight: 900;
}

.status {
  color: #18bda4;
  font-size: 24rpx;
  font-weight: 800;
}

.desc {
  display: block;
  margin-top: 16rpx;
  color: #76868d;
  font-size: 26rpx;
  line-height: 40rpx;
}

.input {
  width: 100%;
  min-height: 78rpx;
  margin-top: 20rpx;
  padding: 0 22rpx;
  border-radius: 12rpx;
  background: #f4f8f8;
  color: #263238;
  font-size: 27rpx;
}

.reason {
  height: 180rpx;
  padding-top: 20rpx;
  line-height: 40rpx;
}

.placeholder {
  color: #aab5ba;
}

.submit {
  margin-top: 28rpx;
}
</style>

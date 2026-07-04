<template>
  <view class="splash-page">
    <view class="center">
      <SiLogo size="large" />
      <text class="brand">思云课堂</text>
      <view class="dots">
        <view class="dot"></view>
        <view class="dot"></view>
        <view class="dot"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import SiLogo from '@/components/SiLogo.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
let hasTimer = false
let hasNavigated = false

onLoad(() => {
  startSplashTimer()
})

onMounted(() => {
  startSplashTimer()
})

function startSplashTimer() {
  if (hasTimer) {
    return
  }
  hasTimer = true
  setTimeout(() => {
    goNext()
  }, 1200)
}

function goNext() {
  if (hasNavigated) {
    return
  }
  hasNavigated = true

  try {
    userStore.hydrate()
  } catch (error) {
    userStore.persist(null)
  }

  if (userStore.isLoggedIn) {
    goHome()
    return
  }
  goLogin()
}

function goHome() {
  const url = '/pages/index/index'
  // #ifdef H5
  h5FullReload(url)
  return
  // #endif
  uni.reLaunch({
    url,
    fail: () => {
      uni.reLaunch({ url })
    },
  })
}

function goLogin() {
  const url = '/pages/auth/login'
  // #ifdef H5
  h5FullReload(url)
  return
  // #endif
  uni.redirectTo({
    url,
    fail: () => {
      uni.reLaunch({
        url,
      })
    },
  })
}

function h5FullReload(url) {
  // #ifdef H5
  const base = `${window.location.origin}${window.location.pathname}`
  window.location.replace(`${base}?_splash=${Date.now()}#${url}`)
  // #endif
}
</script>

<style scoped>
.splash-page {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.center {
  margin-top: -40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand {
  margin-top: 18rpx;
  color: #313a3e;
  font-size: 32rpx;
  font-weight: 900;
}

.dots {
  margin-top: 34rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 9rpx;
  background: #4b4f52;
}
</style>

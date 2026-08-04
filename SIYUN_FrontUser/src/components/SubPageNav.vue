<template>
  <view class="nav-bar">
    <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
    <text class="nav-title">{{ title }}</text>
    <view class="nav-slot"><slot /></view>
  </view>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
})

function goBack() {
  uni.navigateBack({
    fail: () => uni.reLaunch({ url: '/pages/mine/index' }),
  })
}
</script>

<style scoped>
.nav-slot {
  position: absolute;
  right: 28rpx;
  bottom: 0;
  min-width: 100rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>

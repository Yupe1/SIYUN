<template>
  <view class="si-logo" :class="size">
    <text>思</text>
  </view>
</template>

<script setup>
defineProps({
  size: {
    type: String,
    default: 'normal',
  },
})
</script>

<style scoped>
.si-logo {
  border-radius: 18rpx;
  background: #18c6a6;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  box-shadow: 0 12rpx 28rpx rgba(24, 198, 166, 0.2);
}

.normal {
  width: 86rpx;
  height: 86rpx;
  font-size: 48rpx;
}

.large {
  width: 104rpx;
  height: 104rpx;
  font-size: 58rpx;
}

.small {
  width: 64rpx;
  height: 64rpx;
  border-radius: 14rpx;
  font-size: 34rpx;
}
</style>

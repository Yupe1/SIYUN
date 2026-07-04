<template>
  <view class="empty-state">
    <view class="mark">?</view>
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-desc">{{ description }}</text>
  </view>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '暂无数据',
  },
  description: {
    type: String,
    default: '',
  },
})
</script>

<style scoped>
.empty-state {
  min-height: 360rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #97a4aa;
}

.mark {
  width: 104rpx;
  height: 104rpx;
  border-radius: 16rpx;
  border: 10rpx solid #d9e8eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c4d3d8;
  font-size: 62rpx;
  font-weight: 900;
}

.empty-title {
  margin-top: 20rpx;
  font-size: 28rpx;
  font-weight: 700;
}

.empty-desc {
  margin-top: 10rpx;
  font-size: 24rpx;
}
</style>

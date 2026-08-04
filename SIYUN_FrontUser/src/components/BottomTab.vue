<template>
  <view class="bottom-tab">
    <view
      v-for="item in tabs"
      :key="item.key"
      class="tab-shell"
      @tap="go(item)"
    >
      <view
        class="tab-button"
        :class="{ active: active === item.key }"
      >
        <text class="tab-icon">{{ item.icon }}</text>
        <text class="tab-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  active: {
    type: String,
    required: true,
  },
})

const tabs = [
  { key: 'home', text: '首页', icon: '⌂', url: '/pages/index/index' },
  { key: 'moment', text: '微圈', icon: '◎', url: '/pages/moment/index' },
  { key: 'mine', text: '我的', icon: '◉', url: '/pages/mine/index' },
]

function go(item) {
  if (item.key === props.active) {
    return
  }

  // #ifdef H5
  const { origin, pathname, search } = window.location
  window.location.replace(`${origin}${pathname}${search}#${item.url}`)
  return
  // #endif

  uni.redirectTo({
    url: item.url,
  })
}
</script>

<style scoped>
.bottom-tab {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  height: calc(112rpx + env(safe-area-inset-bottom));
  padding: 10rpx 44rpx env(safe-area-inset-bottom);
  background: #ffffff;
  border-top: 1rpx solid #e7eeee;
  box-shadow: 0 -10rpx 28rpx rgba(31, 58, 66, 0.08);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.tab-shell {
  width: 132rpx;
  height: 76rpx;
  display: block;
  color: inherit;
  text-decoration: none;
}

.tab-button {
  width: 132rpx;
  height: 76rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #98a4a9;
  border: 0;
  box-shadow: none;
  outline: none;
  background: transparent;
  text-decoration: none;
}

.tab-icon {
  height: 34rpx;
  font-size: 34rpx;
  line-height: 34rpx;
  font-family: Arial, sans-serif;
}

.tab-text {
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 24rpx;
}

.tab-button.active {
  color: #18bda4;
  font-weight: 800;
}
</style>

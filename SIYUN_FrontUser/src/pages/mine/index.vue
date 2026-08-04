<template>
  <view class="page mine-page">
    <view class="mine-header">
      <view class="safe-top"></view>
      <view class="header-title">
        <text>个人中心</text>
      </view>
    </view>

    <view class="profile-card">
      <image v-if="avatarUrl" class="avatar image-avatar" :src="avatarUrl" mode="aspectFill" />
      <view v-else class="avatar">{{ avatarText }}</view>
      <view class="profile-main">
        <text class="name">{{ userStore.displayName }}</text>
        <text class="account">
          {{ userStore.isLoggedIn ? `账号：${userStore.user?.stuTel || '--'}` : '登录后同步你的学习与创作' }}
        </text>
        <text v-if="userStore.isLoggedIn" class="signature">
          {{ userStore.user?.remark || '认真学习，保持热爱。' }}
        </text>
      </view>
      <button v-if="!userStore.isLoggedIn" class="login-button" @tap="goLogin">登录</button>
    </view>

    <view v-if="userStore.isLoggedIn" class="stat-strip">
      <view class="stat-item" @tap="goPage('/pages/mine/history')">
        <text class="stat-value">{{ overview.studyDuration || 0 }}</text>
        <text class="stat-label">学习分钟</text>
      </view>
      <view class="stat-item" @tap="goPage('/pages/mine/orders')">
        <text class="stat-value">{{ overview.orderCount || 0 }}</text>
        <text class="stat-label">全部订单</text>
      </view>
      <view class="stat-item" @tap="goPage('/pages/mine/coupons')">
        <text class="stat-value">{{ overview.couponCount || 0 }}</text>
        <text class="stat-label">优惠券</text>
      </view>
    </view>

    <view class="menu-section">
      <button v-for="item in mainMenus" :key="item.key" class="menu-row" @tap="openMenu(item)">
        <view class="menu-icon"><image class="menu-icon-image" :src="item.icon" mode="aspectFit" /></view>
        <text class="menu-title">{{ item.title }}</text>
        <text v-if="item.badge" class="menu-extra">{{ item.badge }}</text>
        <text class="menu-arrow">›</text>
      </button>
    </view>

    <view class="menu-section secondary-menu">
      <button v-for="item in serviceMenus" :key="item.key" class="menu-row" @tap="openMenu(item)">
        <view class="menu-icon pale"><image class="menu-icon-image" :src="item.icon" mode="aspectFit" /></view>
        <text class="menu-title">{{ item.title }}</text>
        <text v-if="item.badge" class="menu-extra">{{ item.badge }}</text>
        <text class="menu-arrow">›</text>
      </button>
    </view>

    <view v-if="!userStore.isLoggedIn" class="guest-actions">
      <button class="primary-button" @tap="goLogin">登录</button>
      <button class="secondary-button register" @tap="goRegister">创建账号</button>
    </view>
    <button v-else class="logout-button" @tap="logout">退出账号</button>

    <BottomTab active="mine" />
  </view>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomTab from '@/components/BottomTab.vue'
import { getMineOverview } from '@/api/mine'
import { assetUrl } from '@/utils/format'
import { isNotFoundError, isSessionExpiredError, pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const overview = reactive({
  studyDuration: 0,
  wallet: 0,
  orderCount: 0,
  couponCount: 0,
})

const avatarText = computed(() => (userStore.displayName || '思').slice(0, 1))
const avatarUrl = computed(() => assetUrl(userStore.user?.avataUrl))
const isIdentified = computed(() => Boolean(userStore.user?.chinaId))
const isCreator = computed(() => Number(userStore.user?.createrVerified || 0) === 1)

const mainMenus = computed(() => [
  { key: 'wallet', icon: '/static/icons/wallet.svg', title: '钱包', url: '/pages/mine/wallet', auth: true, badge: `余额 ${moneyText(overview.wallet)}` },
  { key: 'creation', icon: '/static/icons/creation.svg', title: '我的创作', url: '/pages/mine/creations', auth: true },
  { key: 'collects', icon: '/static/icons/favorite.svg', title: '我的收藏', url: '/pages/mine/collects', auth: true },
  { key: 'store', icon: '/static/icons/store.svg', title: '微商城', url: '/pages/store/index' },
])

const serviceMenus = computed(() => [
  {
    key: 'identity',
    icon: '/static/icons/identity.svg',
    title: '实名认证',
    url: '/pages/mine/identity',
    auth: true,
    badge: isIdentified.value ? '已实名' : '未实名',
  },
  {
    key: 'creator',
    icon: '/static/icons/creator.svg',
    title: '创作者认证中心',
    action: 'creator',
    auth: true,
    badge: isCreator.value ? '已通过' : '去认证',
  },
  { key: 'video', icon: '/static/icons/video.svg', title: '上传视频课程', action: 'video', auth: true },
  { key: 'feedback', icon: '/static/icons/feedback.svg', title: '意见反馈', url: '/pages/mine/feedback', auth: true },
  { key: 'service', icon: '/static/icons/service.svg', title: '在线客服', url: '/pages/mine/service', auth: true },
  { key: 'password', icon: '/static/icons/password.svg', title: '修改密码', url: '/pages/auth/change-password', auth: true },
])

onShow(async () => {
  userStore.hydrate()
  if (!userStore.isLoggedIn) {
    resetOverview()
    return
  }

  // 用户资料刷新与统计查询互不依赖，资料接口偶发失败时也必须刷新创作数等统计。
  try {
    await userStore.refresh()
  } catch (error) {
    if (!isSessionExpiredError(error) && !isNotFoundError(error)) {
      uni.showToast({ title: error.message || '用户状态刷新失败', icon: 'none' })
    }
  }

  if (!userStore.isLoggedIn) {
    resetOverview()
    return
  }

  try {
    const response = await getMineOverview()
    Object.assign(overview, pickResult(response, 'overview', {}))
  } catch (error) {
    if (!isSessionExpiredError(error) && !isNotFoundError(error)) {
      uni.showToast({ title: error.message || '个人数据加载失败', icon: 'none' })
    }
  }

})

function moneyText(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function resetOverview() {
  Object.assign(overview, {
    studyDuration: 0,
    wallet: 0,
    orderCount: 0,
    couponCount: 0,
  })
}

function openMenu(item) {
  if (item.auth && !userStore.isLoggedIn) {
    goLogin()
    return
  }
  if (item.action === 'creator') {
    goCreatorApply()
    return
  }
  if (item.action === 'video') {
    goVideoUpload()
    return
  }
  goPage(item.url)
}

function goPage(url) {
  uni.navigateTo({ url })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function goCreatorApply() {
  if (!isIdentified.value) {
    uni.showToast({ title: '请先完成实名认证', icon: 'none' })
    goPage('/pages/mine/identity')
    return
  }
  goPage('/pages/moment/creator-apply')
}

function goVideoUpload() {
  if (!isIdentified.value || !isCreator.value) {
    uni.showToast({ title: '通过创作者认证后才能上传视频课程', icon: 'none' })
    goCreatorApply()
    return
  }
  goPage('/pages/mine/video-upload')
}

async function logout() {
  try {
    await userStore.logout()
    resetOverview()
    uni.showToast({ title: '已退出', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '退出失败', icon: 'none' })
  }
}
</script>

<style scoped>
.mine-page {
  background: #f4f8f8;
  padding-bottom: 190rpx;
}

.mine-header {
  height: calc(var(--status-bar-height) + 116rpx);
  background: #20bea3;
  color: #ffffff;
}

.header-title {
  height: 96rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  font-size: 32rpx;
  font-weight: 800;
}

.profile-card {
  width: calc(100% - 48rpx);
  min-height: 190rpx;
  margin: 20rpx 24rpx 0;
  padding: 28rpx;
  border-radius: 22rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 34rpx rgba(34, 74, 77, 0.09);
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
}

.avatar {
  flex: 0 0 auto;
  width: 116rpx;
  height: 116rpx;
  border-radius: 24rpx;
  background: #e2f8f4;
  color: #18bda4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: 900;
}

.image-avatar {
  display: block;
}

.profile-main {
  flex: 1;
  min-width: 0;
  margin-left: 24rpx;
}

.name,
.account,
.signature {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.name {
  color: #233138;
  font-size: 32rpx;
  font-weight: 900;
}

.account {
  margin-top: 10rpx;
  color: #7f8d93;
  font-size: 23rpx;
}

.signature {
  margin-top: 8rpx;
  color: #9aa6ab;
  font-size: 22rpx;
}

.login-button {
  width: 108rpx;
  height: 60rpx;
  border-radius: 30rpx;
  background: #20bea3;
  color: #ffffff;
  font-size: 25rpx;
  font-weight: 800;
}

.stat-strip {
  width: calc(100% - 48rpx);
  margin: 20rpx 24rpx;
  padding: 22rpx 0;
  border-radius: 18rpx;
  background: #ffffff;
  display: flex;
  box-shadow: 0 8rpx 24rpx rgba(34, 74, 77, 0.05);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1rpx solid #edf2f2;
}

.stat-item:last-child {
  border-right: 0;
}

.stat-value {
  color: #20b99f;
  font-size: 32rpx;
  line-height: 40rpx;
  font-weight: 900;
}

.stat-label {
  margin-top: 7rpx;
  color: #88959a;
  font-size: 22rpx;
}

.menu-section {
  width: calc(100% - 48rpx);
  margin: 20rpx 24rpx 0;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(34, 74, 77, 0.05);
  overflow: hidden;
}

.secondary-menu {
  margin-top: 20rpx;
}

.menu-row {
  width: 100%;
  min-width: 0;
  height: 94rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #edf1f2;
  border-radius: 0;
  background: transparent;
  overflow: hidden;
  text-align: left;
}

.menu-row::after {
  border: 0;
}

.menu-row:last-child {
  border-bottom: 0;
}

.menu-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 15rpx;
  background: #e5f8f4;
  color: #18bda4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 900;
}

.menu-icon.pale {
  background: #f0f7f6;
}

.menu-icon-image {
  display: block;
  width: 30rpx;
  height: 30rpx;
}

.menu-title {
  flex: 1;
  margin-left: 20rpx;
  color: #2d3a40;
  font-size: 27rpx;
}

.menu-extra {
  max-width: 240rpx;
  overflow: hidden;
  color: #909da2;
  font-size: 22rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-arrow {
  margin-left: 14rpx;
  color: #b0bbbf;
  font-size: 38rpx;
}

.guest-actions {
  width: calc(100% - 48rpx);
  margin: 28rpx 24rpx 0;
  padding: 24rpx;
  border-radius: 18rpx;
  background: #ffffff;
}

.register {
  margin-top: 18rpx;
}

.logout-button {
  width: calc(100% - 48rpx);
  height: 78rpx;
  margin: 28rpx 24rpx 0;
  border-radius: 39rpx;
  background: #ffffff;
  color: #ed6f58;
  font-size: 27rpx;
  font-weight: 800;
  box-shadow: 0 8rpx 24rpx rgba(34, 74, 77, 0.05);
}
</style>

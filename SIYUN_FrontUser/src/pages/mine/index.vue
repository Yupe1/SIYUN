<template>
  <view class="page mine-page">
    <view class="top-area">
      <view class="safe-top"></view>
      <view class="profile">
        <view class="avatar">{{ avatarText }}</view>
        <view class="profile-main">
          <text class="name">{{ userStore.displayName }}</text>
          <text class="sub">{{ userStore.isLoggedIn ? userStore.user?.stuTel : '登录后同步学习记录' }}</text>
        </view>
        <button v-if="!userStore.isLoggedIn" class="login-small" @tap="goLogin">登录</button>
      </view>
    </view>

    <view class="content">
      <view v-if="userStore.isLoggedIn" class="panel card">
        <button class="row" @tap="loadCollect">
          <text>我的课程</text>
          <text class="row-action">刷新</text>
        </button>
        <button class="row" @tap="loadMoments">
          <text>我的微圈</text>
          <text class="row-action">刷新</text>
        </button>
        <button class="row" @tap="focusIdentity">
          <text>实名认证</text>
          <text class="row-action">{{ identityStatusText }}</text>
        </button>
        <button class="row" @tap="goCreatorApply">
          <text>创作者认证</text>
          <text class="row-action">{{ creatorStatusText }}</text>
        </button>
        <button class="row" @tap="goChangePassword">
          <text>修改密码</text>
          <text class="row-action">进入</text>
        </button>
        <button class="row" @tap="logout">
          <text>退出登录</text>
          <text class="row-action danger">退出</text>
        </button>
      </view>
      <view v-else class="panel card guest-actions">
        <button class="primary-button" @tap="goLogin">登录</button>
        <button class="secondary-button register" @tap="goRegister">创建账号</button>
      </view>

      <view v-if="userStore.isLoggedIn" class="identity-card card">
        <view class="identity-head">
          <text class="identity-title">实名认证</text>
          <text class="identity-status">{{ identityStatusText }}</text>
        </view>
        <text class="identity-desc">
          {{ isIdentified ? '实名信息已提交，可继续申请创作者认证。' : '提交身份证号后才能申请创作者认证。' }}
        </text>
        <template v-if="!isIdentified">
          <input
            v-model.trim="identityForm.chinaId"
            class="identity-input"
            placeholder="身份证号码"
            placeholder-class="placeholder"
          />
          <button class="identity-submit" @tap="submitIdentity">提交实名</button>
        </template>
      </view>

      <view class="section-head">
        <text class="section-title">我的课程</text>
      </view>
      <CourseCard
        v-for="course in collects"
        :key="course.id"
        :course="course"
        @select="openCourse"
      />
      <EmptyState v-if="userStore.isLoggedIn && !collects.length" title="暂无课程" />

      <view class="section-head moment-head">
        <text class="section-title">我的微圈</text>
        <button class="section-action" @tap="goPublish">发布</button>
      </view>
      <MomentCard
        v-for="item in myMoments"
        :key="item.id"
        :moment="item"
        :current-user-id="userStore.user?.id || 0"
        @delete="removeMoment"
      />
      <EmptyState v-if="userStore.isLoggedIn && !myMoments.length" title="暂无微圈" />
    </view>

    <BottomTab active="mine" />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomTab from '@/components/BottomTab.vue'
import CourseCard from '@/components/CourseCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import MomentCard from '@/components/MomentCard.vue'
import { getMyCollect } from '@/api/course'
import { deleteMoment, getMyMoments } from '@/api/moment'
import { isSessionExpiredError, pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const CURRENT_COURSE_KEY = 'SIYUN_CURRENT_COURSE'
const userStore = useUserStore()
const collects = ref([])
const myMoments = ref([])
const identityForm = reactive({
  chinaId: '',
})
const avatarText = computed(() => (userStore.displayName || '思').slice(0, 1))
const isIdentified = computed(() => Boolean(userStore.user?.chinaId))
const identityStatusText = computed(() => (isIdentified.value ? '已实名' : '未实名'))
const creatorStatusText = computed(() => (Number(userStore.user?.createrVerified || 0) === 1 ? '已通过' : '去申请'))

onShow(() => {
  hydrateAndLoad()
})

async function hydrateAndLoad() {
  userStore.hydrate()
  if (userStore.isLoggedIn) {
    try {
      await userStore.refresh()
    } catch (error) {
      if (!isSessionExpiredError(error)) {
        uni.showToast({ title: error.message || '用户状态刷新失败', icon: 'none' })
      }
    }
    loadCollect()
    loadMoments()
  }
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function goChangePassword() {
  uni.navigateTo({ url: '/pages/auth/change-password' })
}

function focusIdentity() {
  if (isIdentified.value) {
    uni.showToast({ title: '已完成实名认证', icon: 'none' })
    return
  }
  uni.showToast({ title: '请填写身份证号码', icon: 'none' })
}

function goCreatorApply() {
  if (!isIdentified.value) {
    uni.showToast({ title: '请先完成实名认证', icon: 'none' })
    return
  }
  // #ifdef H5
  const { origin, pathname, search } = window.location
  window.location.replace(`${origin}${pathname}${search}#/pages/moment/index`)
  return
  // #endif

  uni.redirectTo({ url: '/pages/moment/index' })
}

function goPublish() {
  if (!userStore.isLoggedIn) {
    goLogin()
    return
  }
  if (!isIdentified.value || Number(userStore.user?.createrVerified || 0) !== 1) {
    goCreatorApply()
    return
  }
  uni.navigateTo({ url: '/pages/moment/edit' })
}

async function submitIdentity() {
  if (!identityForm.chinaId) {
    uni.showToast({ title: '请输入身份证号码', icon: 'none' })
    return
  }
  if (!/^\d{15}$|^\d{17}[\dXx]$/.test(identityForm.chinaId)) {
    uni.showToast({ title: '身份证号码格式不正确', icon: 'none' })
    return
  }
  try {
    await userStore.identify({ chinaId: identityForm.chinaId })
    identityForm.chinaId = ''
    uni.showToast({ title: '实名成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  }
}

async function logout() {
  try {
    await userStore.logout()
    collects.value = []
    myMoments.value = []
    uni.showToast({ title: '已退出', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '退出失败', icon: 'none' })
  }
}

async function loadCollect() {
  if (!userStore.isLoggedIn) {
    return
  }
  try {
    const response = await getMyCollect()
    collects.value = pickResult(response, 'myCollect', [])
  } catch (error) {
    collects.value = []
    if (!isSessionExpiredError(error)) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    }
  }
}

async function loadMoments() {
  if (!userStore.isLoggedIn) {
    return
  }
  if (!isIdentified.value) {
    myMoments.value = []
    return
  }
  try {
    const response = await getMyMoments()
    myMoments.value = pickResult(response, 'myMoments', [])
  } catch (error) {
    myMoments.value = []
    if (!isSessionExpiredError(error)) {
      uni.showToast({ title: error.message || '加载失败', icon: 'none' })
    }
  }
}

function openCourse(course) {
  uni.setStorageSync(CURRENT_COURSE_KEY, course)
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`,
  })
}

async function removeMoment(moment) {
  try {
    await deleteMoment(moment)
    myMoments.value = myMoments.value.filter((item) => item.id !== moment.id)
    uni.showToast({ title: '已删除', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '删除失败', icon: 'none' })
  }
}
</script>

<style scoped>
.mine-page {
  background: #f4f8f8;
}

.top-area {
  background: #42c6b2;
  color: #ffffff;
  padding-bottom: 34rpx;
}

.profile {
  min-height: 136rpx;
  padding: 20rpx 28rpx 0;
  display: flex;
  align-items: center;
}

.avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: 52rpx;
  background: #ffffff;
  color: #18bda4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 46rpx;
  font-weight: 900;
}

.profile-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.name {
  display: block;
  max-width: 360rpx;
  font-size: 34rpx;
  line-height: 44rpx;
  font-weight: 900;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub {
  display: block;
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.78);
  font-size: 24rpx;
}

.login-small {
  width: 116rpx;
  height: 60rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.22);
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
}

.panel {
  padding: 8rpx 24rpx;
  margin-bottom: 26rpx;
}

.row {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #edf1f2;
  color: #263238;
  font-size: 28rpx;
}

.row:last-child {
  border-bottom: 0;
}

.row-action {
  color: #18bda4;
  font-size: 24rpx;
}

.danger {
  color: #f06b4f;
}

.guest-actions {
  padding: 24rpx;
}

.register {
  margin-top: 18rpx;
}

.identity-card {
  padding: 24rpx;
  margin-bottom: 26rpx;
}

.identity-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.identity-title {
  color: #263238;
  font-size: 30rpx;
  font-weight: 800;
}

.identity-status {
  color: #18bda4;
  font-size: 24rpx;
  font-weight: 800;
}

.identity-desc {
  display: block;
  margin-top: 12rpx;
  color: #7d8b91;
  font-size: 24rpx;
  line-height: 36rpx;
}

.identity-input {
  width: 100%;
  height: 76rpx;
  margin-top: 18rpx;
  padding: 0 20rpx;
  border-radius: 10rpx;
  background: #f4f8f8;
  color: #263238;
  font-size: 26rpx;
  box-sizing: border-box;
}

.identity-submit {
  height: 70rpx;
  margin-top: 18rpx;
  border-radius: 35rpx;
  background: #18c6a6;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
}

.placeholder {
  color: #aab5ba;
}

.moment-head {
  margin-top: 34rpx;
}
</style>

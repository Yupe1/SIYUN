<template>
  <view class="edit-page">
    <view class="nav-bar">
      <button class="nav-back" @tap="goBack">‹ 返回</button>
      <text class="nav-title">发布微圈</text>
    </view>

    <view class="content form">
      <view class="card form-card">
        <input
          v-model.trim="form.title"
          class="title-input"
          placeholder="标题"
          placeholder-class="placeholder"
        />
        <input
          v-model.trim="form.keywords"
          class="line-input"
          placeholder="关键词"
          placeholder-class="placeholder"
        />
        <input
          v-model.trim="form.coverUrl"
          class="line-input"
          placeholder="封面图片地址"
          placeholder-class="placeholder"
        />
        <textarea
          v-model.trim="form.content"
          class="content-input"
          maxlength="1000"
          placeholder="内容"
          placeholder-class="placeholder"
        />
      </view>
      <button class="primary-button submit" @tap="submit">确认</button>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addMoment } from '@/api/moment'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const form = reactive({
  title: '',
  keywords: '',
  coverUrl: '',
  content: '',
})

onLoad(async () => {
  userStore.hydrate()
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await userStore.refresh()
  } catch (error) {
  }
  if (!userStore.user?.chinaId) {
    uni.showToast({ title: '请先完成实名认证', icon: 'none' })
    returnToMoment()
    return
  }
  if (Number(userStore.user?.createrVerified || 0) !== 1) {
    uni.showToast({ title: '请先完成创作者认证', icon: 'none' })
    returnToMoment()
  }
})

function goBack() {
  uni.navigateBack({
    fail: returnToMoment,
  })
}

function returnToMoment() {
  // #ifdef H5
  const { origin, pathname, search } = window.location
  window.location.replace(`${origin}${pathname}${search}#/pages/moment/index`)
  return
  // #endif

  uni.redirectTo({ url: '/pages/moment/index' })
}

function validate() {
  if (!form.title) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return false
  }
  if (!form.content) {
    uni.showToast({ title: '请输入内容', icon: 'none' })
    return false
  }
  return true
}

async function submit() {
  if (!validate()) {
    return
  }
  try {
    await addMoment({
      ...form,
      authorId: userStore.user?.id,
      countView: 0,
      countLike: 0,
      countComment: 0,
      countShare: 0,
      sortNum: 0,
      statusShow: 1,
      status: 3,
    })
    uni.showToast({ title: '已发布', icon: 'success' })
    setTimeout(() => {
      returnToMoment()
    }, 500)
  } catch (error) {
    uni.showToast({ title: error.message || '发布失败', icon: 'none' })
  }
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: #f4f8f8;
}

.form-card {
  padding: 24rpx;
}

.title-input {
  height: 82rpx;
  color: #263238;
  font-size: 34rpx;
  font-weight: 800;
  border-bottom: 1rpx solid #e2e8ea;
}

.line-input {
  height: 78rpx;
  color: #263238;
  font-size: 28rpx;
  border-bottom: 1rpx solid #e2e8ea;
}

.content-input {
  width: 100%;
  min-height: 360rpx;
  padding-top: 22rpx;
  color: #263238;
  font-size: 28rpx;
  line-height: 42rpx;
}

.placeholder {
  color: #aab5ba;
}

.submit {
  margin-top: 34rpx;
}
</style>

<template>
  <view class="password-page">
    <view class="nav-bar">
      <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
      <text class="nav-title">重置密码</text>
    </view>

    <view class="form">
      <view class="field">
        <text class="field-icon">机</text>
        <input
          v-model.trim="form.stuTel"
          class="field-input"
          type="number"
          maxlength="11"
          placeholder="输入手机号码"
          placeholder-class="placeholder"
        />
      </view>
      <view class="field">
        <text class="field-icon">锁</text>
        <input
          v-model="form.password"
          class="field-input"
          :password="!showOld"
          placeholder="输入当前登录密码"
          placeholder-class="placeholder"
        />
        <button class="field-extra" @tap="showOld = !showOld">{{ showOld ? '隐藏' : '显示' }}</button>
      </view>
      <view class="field">
        <text class="field-icon">新</text>
        <input
          v-model="form.newPassword"
          class="field-input"
          :password="!showNew"
          placeholder="输入新的登录密码"
          placeholder-class="placeholder"
        />
        <button class="field-extra" @tap="showNew = !showNew">{{ showNew ? '隐藏' : '显示' }}</button>
      </view>
      <button class="primary-button confirm" @tap="submit">确认</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { h5ReplaceTo } from '@/utils/navigation'

const userStore = useUserStore()
const showOld = ref(false)
const showNew = ref(false)
const form = reactive({
  stuTel: '',
  password: '',
  newPassword: '',
})

onLoad(() => {
  userStore.hydrate()
  form.stuTel = userStore.user?.stuTel || ''
})

function goBack() {
  uni.navigateBack({
    fail: () => uni.redirectTo({ url: '/pages/auth/login' }),
  })
}

function validate() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.redirectTo({ url: '/pages/auth/login' }), 500)
    return false
  }
  if (!form.password || !form.newPassword) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return false
  }
  if (form.newPassword.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' })
    return false
  }
  if (form.password === form.newPassword) {
    uni.showToast({ title: '新旧密码不能相同', icon: 'none' })
    return false
  }
  return true
}

async function submit() {
  if (!validate()) {
    return
  }
  try {
    await changePassword({ ...form })
    uni.showToast({ title: '修改成功', icon: 'success' })
    setTimeout(() => {
      goMine()
    }, 500)
  } catch (error) {
    uni.showToast({ title: error.message || '修改失败', icon: 'none' })
  }
}

function goMine() {
  if (h5ReplaceTo('/pages/mine/index', { reload: true })) {
    return
  }

  uni.reLaunch({ url: '/pages/mine/index' })
}
</script>

<style scoped>
.password-page {
  min-height: 100vh;
  background: #ffffff;
}

.form {
  padding: 118rpx 56rpx 0;
}

.field {
  height: 82rpx;
  border-bottom: 1rpx solid #e2e7e8;
  display: flex;
  align-items: center;
}

.field + .field {
  margin-top: 8rpx;
}

.field-icon {
  width: 80rpx;
  color: #a1a9ad;
  font-size: 28rpx;
  font-weight: 800;
}

.field-input {
  flex: 1;
  height: 82rpx;
  color: #263238;
  font-size: 28rpx;
}

.placeholder {
  color: #b6bfc3;
}

.field-extra {
  width: 88rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: #e7f0f2;
  color: #8a989e;
  font-size: 22rpx;
}

.confirm {
  margin-top: 62rpx;
}
</style>

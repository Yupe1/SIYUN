<template>
  <view class="auth-page login-page">
    <view class="logo-wrap">
      <SiLogo />
    </view>

    <view class="form">
      <view class="field">
        <text class="field-icon">ID</text>
        <input
          v-model.trim="form.stuTel"
          class="field-input"
          type="number"
          maxlength="11"
          placeholder="手机号码"
          placeholder-class="placeholder"
        />
      </view>
      <view class="field">
        <text class="field-icon">锁</text>
        <input
          v-model="form.password"
          class="field-input"
          :password="!showPassword"
          placeholder="登录密码"
          placeholder-class="placeholder"
        />
        <button class="field-extra" @tap="showPassword = !showPassword">
          {{ showPassword ? '隐藏' : '显示' }}
        </button>
      </view>

      <button class="forget" @tap="goChangePassword">忘记密码</button>
      <button class="primary-button login-button" @tap="submit">登录</button>
      <button class="create" @tap="goRegister">创建账号</button>

      <view class="or-row">
        <view class="line"></view>
        <text>OR</text>
        <view class="line"></view>
      </view>

      <button class="secondary-button social" @tap="thirdPartyLogin('微信')">微信账号登录</button>
      <button class="secondary-button social" @tap="thirdPartyLogin('QQ')">QQ账号登录</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import SiLogo from '@/components/SiLogo.vue'
import { useUserStore } from '@/stores/user'
import { h5ReplaceTo } from '@/utils/navigation'

const userStore = useUserStore()
const showPassword = ref(false)
const form = reactive({
  stuTel: '',
  password: '',
})

function validate() {
  if (!/^1\d{10}$/.test(form.stuTel)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return false
  }
  if (!form.password) {
    uni.showToast({ title: '请输入登录密码', icon: 'none' })
    return false
  }
  return true
}

async function submit() {
  if (!validate()) {
    return
  }
  try {
    await userStore.login({ ...form })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      goHome()
    }, 300)
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  }
}

function goHome() {
  if (h5ReplaceTo('/pages/index/index', { reload: true })) {
    return
  }

  uni.reLaunch({ url: '/pages/index/index' })
}

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function goChangePassword() {
  uni.navigateTo({ url: '/pages/auth/change-password' })
}

function thirdPartyLogin(type) {
  uni.showToast({ title: `${type}登录待后端开放`, icon: 'none' })
}
</script>

<style scoped>
.login-page {
  padding-top: calc(var(--status-bar-height) + 152rpx);
}

.logo-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 76rpx;
}

.form {
  width: 100%;
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
  font-size: 24rpx;
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

.forget {
  margin-top: 16rpx;
  margin-left: auto;
  width: 144rpx;
  height: 52rpx;
  color: #18bda4;
  font-size: 24rpx;
}

.login-button {
  margin-top: 28rpx;
}

.create {
  width: 180rpx;
  height: 72rpx;
  margin: 34rpx auto 0;
  color: #18bda4;
  font-size: 26rpx;
}

.or-row {
  margin: 12rpx 8rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #98a4a9;
  font-size: 24rpx;
}

.line {
  flex: 1;
  height: 1rpx;
  background: #dfe6e8;
}

.or-row text {
  padding: 0 34rpx;
}

.social {
  margin-top: 18rpx;
}
</style>

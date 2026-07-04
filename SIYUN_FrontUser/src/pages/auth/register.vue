<template>
  <view class="auth-page register-page">
    <view class="logo-wrap">
      <SiLogo />
    </view>

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
      <text class="field-icon">信</text>
      <input
        v-model.trim="form.code"
        class="field-input"
        type="number"
        maxlength="6"
        placeholder="短信验证码"
        placeholder-class="placeholder"
      />
      <button class="send-code" @tap="sendCode">发送验证码</button>
    </view>
    <view class="field">
      <text class="field-icon">锁</text>
      <input
        v-model="form.password"
        class="field-input"
        :password="!showPassword"
        placeholder="设置登录密码"
        placeholder-class="placeholder"
      />
      <button class="field-extra" @tap="showPassword = !showPassword">
        {{ showPassword ? '隐藏' : '显示' }}
      </button>
    </view>
    <view class="field">
      <text class="field-icon">锁</text>
      <input
        v-model="form.confirmPassword"
        class="field-input"
        :password="!showConfirm"
        placeholder="确认登录密码"
        placeholder-class="placeholder"
      />
      <button class="field-extra" @tap="showConfirm = !showConfirm">
        {{ showConfirm ? '隐藏' : '显示' }}
      </button>
    </view>

    <button class="agreement" @tap="agreed = !agreed">
      <view class="check" :class="{ checked: agreed }"></view>
      <text>已阅读并同意《用户服务协议》</text>
    </button>

    <button class="primary-button confirm" @tap="submit">确认</button>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import SiLogo from '@/components/SiLogo.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const showPassword = ref(false)
const showConfirm = ref(false)
const agreed = ref(true)
const form = reactive({
  stuTel: '',
  code: '',
  password: '',
  confirmPassword: '',
})

function sendCode() {
  if (!/^1\d{10}$/.test(form.stuTel)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  uni.showToast({ title: '验证码待后端开放', icon: 'none' })
}

function validate() {
  if (!/^1\d{10}$/.test(form.stuTel)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return false
  }
  if (form.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return false
  }
  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return false
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意协议', icon: 'none' })
    return false
  }
  return true
}

async function submit() {
  if (!validate()) {
    return
  }
  try {
    await userStore.register({
      stuTel: form.stuTel,
      password: form.password,
      nickName: `思云同学${form.stuTel.slice(-4)}`,
      status: 0,
      createrVerified: 0,
      studyDuration: 0,
    })
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 500)
  } catch (error) {
    uni.showToast({ title: error.message || '注册失败', icon: 'none' })
  }
}
</script>

<style scoped>
.register-page {
  padding-top: calc(var(--status-bar-height) + 118rpx);
}

.logo-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 70rpx;
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

.send-code {
  width: 160rpx;
  height: 58rpx;
  color: #18bda4;
  font-size: 24rpx;
}

.field-extra {
  width: 88rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: #e7f0f2;
  color: #8a989e;
  font-size: 22rpx;
}

.agreement {
  margin-top: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  color: #8d9aa0;
  font-size: 24rpx;
}

.check {
  width: 34rpx;
  height: 34rpx;
  border-radius: 17rpx;
  border: 3rpx solid #57cbbb;
  margin-right: 12rpx;
}

.check.checked {
  background: #57cbbb;
  box-shadow: inset 0 0 0 7rpx #ffffff;
}

.agreement text {
  color: #18bda4;
}

.confirm {
  margin-top: 52rpx;
}
</style>

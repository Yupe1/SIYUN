<template>
  <view class="wallet-page">
    <SubPageNav title="钱包" />
    <view class="wallet-card">
      <text class="wallet-label">账户余额（元）</text>
      <text class="wallet-balance">{{ balanceText }}</text>
      <text class="wallet-tip">余额可用于课程和商城商品支付</text>
    </view>

    <view v-if="returnUrl" class="recharge-alert">
      <text class="alert-title">余额不足</text>
      <text class="alert-text">充值成功后可返回刚才的页面继续完成付款。</text>
    </view>

    <view class="recharge-card card">
      <text class="section-title">余额充值</text>
      <text class="section-desc">演示环境为模拟充值，点击付款后金额会直接进入余额。</text>
      <view class="preset-grid">
        <button
          v-for="item in presets"
          :key="item"
          class="preset"
          :class="{ selected: Number(amount) === item }"
          @tap="amount = String(item)"
        >
          <text class="preset-number">{{ item }}</text>
          <text class="preset-unit">元</text>
        </button>
      </view>
      <view class="custom-row">
        <text class="currency">￥</text>
        <input
          v-model.trim="amount"
          class="amount-input"
          type="digit"
          maxlength="9"
          placeholder="输入自定义金额"
          placeholder-class="placeholder"
        />
      </view>
      <button class="pay-button" :disabled="paying" @tap="pay">
        {{ paying ? '正在付款…' : payButtonText }}
      </button>
    </view>

    <view class="notice card">
      <text class="notice-title">资金说明</text>
      <text class="notice-text">本项目暂未接入第三方支付渠道，充值仅用于功能演示，不会产生真实扣款。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import SubPageNav from '@/components/SubPageNav.vue'
import { getWallet, rechargeWallet } from '@/api/mine'
import { pickResult } from '@/utils/request'

const balance = ref(0)
const amount = ref('')
const paying = ref(false)
const returnUrl = ref('')
const presets = [20, 50, 100, 200]
const balanceText = computed(() => Number(balance.value || 0).toFixed(2))
const normalizedAmount = computed(() => Number(amount.value || 0))
const payButtonText = computed(() => normalizedAmount.value > 0
  ? `模拟付款 ￥${normalizedAmount.value.toFixed(2)}`
  : '请输入充值金额')

onLoad((query = {}) => {
  returnUrl.value = String(query.returnUrl || '')
})

onShow(loadWallet)

async function loadWallet() {
  try {
    const response = await getWallet()
    balance.value = pickResult(response, 'balance', 0)
  } catch (error) {
    uni.showToast({ title: error.message || '钱包加载失败', icon: 'none' })
  }
}

async function pay() {
  const value = normalizedAmount.value
  if (!Number.isFinite(value) || value < 0.01 || value > 100000) {
    uni.showToast({ title: '请输入0.01到100000元', icon: 'none' })
    return
  }
  paying.value = true
  try {
    const response = await rechargeWallet(value.toFixed(2))
    balance.value = pickResult(response, 'balance', balance.value)
    amount.value = ''
    if (returnUrl.value) {
      uni.showModal({
        title: '充值成功',
        content: `当前余额 ￥${balanceText.value}，返回继续付款。`,
        showCancel: false,
        confirmText: '返回购买',
        success: () => uni.navigateBack(),
      })
    } else {
      uni.showToast({ title: '充值成功', icon: 'success' })
    }
  } catch (error) {
    uni.showToast({ title: error.message || '充值失败', icon: 'none' })
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.wallet-page {
  min-height: 100vh;
  padding-bottom: 36rpx;
  background: #f4f8f8;
}

.wallet-card {
  margin: 28rpx 24rpx;
  padding: 42rpx 34rpx;
  border-radius: 24rpx;
  background: linear-gradient(145deg, #19b99f, #63d5c1);
  color: #fff;
  box-shadow: 0 18rpx 38rpx rgba(25, 185, 159, 0.22);
}

.wallet-label,
.wallet-balance,
.wallet-tip,
.section-desc,
.notice-title,
.notice-text,
.alert-title,
.alert-text {
  display: block;
}

.wallet-label {
  color: rgba(255, 255, 255, 0.82);
  font-size: 24rpx;
}

.wallet-balance {
  margin-top: 18rpx;
  font-size: 68rpx;
  line-height: 78rpx;
  font-weight: 900;
}

.wallet-tip {
  margin-top: 24rpx;
  color: rgba(255, 255, 255, 0.76);
  font-size: 22rpx;
}

.recharge-alert {
  margin: 0 24rpx 20rpx;
  padding: 22rpx 24rpx;
  border-radius: 16rpx;
  background: #fff3e9;
}

.alert-title {
  color: #e47a37;
  font-size: 26rpx;
  font-weight: 900;
}

.alert-text {
  margin-top: 8rpx;
  color: #9a765d;
  font-size: 22rpx;
  line-height: 34rpx;
}

.recharge-card,
.notice {
  margin: 0 24rpx;
  padding: 28rpx;
}

.section-title,
.notice-title {
  color: #2b3940;
  font-size: 29rpx;
  font-weight: 900;
}

.section-desc,
.notice-text {
  margin-top: 12rpx;
  color: #849197;
  font-size: 23rpx;
  line-height: 38rpx;
}

.preset-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 24rpx;
}

.preset {
  height: 88rpx;
  border-radius: 14rpx;
  background: #f1f7f6;
  color: #4b5b61;
}

.preset.selected {
  background: #e1f8f3;
  color: #18a991;
  box-shadow: inset 0 0 0 2rpx #42c9b2;
}

.preset-number {
  font-size: 31rpx;
  font-weight: 900;
}

.preset-unit {
  margin-left: 5rpx;
  font-size: 21rpx;
}

.custom-row {
  display: flex;
  align-items: center;
  height: 86rpx;
  margin-top: 18rpx;
  padding: 0 22rpx;
  border-radius: 14rpx;
  background: #f3f7f7;
}

.currency {
  color: #2f3d43;
  font-size: 34rpx;
  font-weight: 900;
}

.amount-input {
  flex: 1;
  min-width: 0;
  height: 86rpx;
  margin-left: 12rpx;
  color: #253238;
  font-size: 30rpx;
}

.pay-button {
  width: 100%;
  height: 82rpx;
  margin-top: 24rpx;
  border-radius: 41rpx;
  background: #20bea3;
  color: #fff;
  font-size: 28rpx;
  font-weight: 900;
  box-shadow: 0 12rpx 26rpx rgba(32, 190, 163, 0.2);
}

.pay-button[disabled] {
  opacity: 0.62;
}

.notice {
  margin-top: 20rpx;
}

.placeholder {
  color: #a5b0b4;
}
</style>

<template>
  <view class="coupon-page">
    <SubPageNav title="优惠券" />
    <view class="content">
      <view
        v-for="item in coupons"
        :key="item.couponUserId || item.id"
        class="coupon-card"
        @tap="useCoupon(item)"
      >
        <view class="amount-area">
          <text class="currency">￥</text>
          <text class="amount">{{ money(item.amount) }}</text>
        </view>
        <view class="coupon-main">
          <text class="coupon-name">{{ item.couponName || '思云优惠券' }}</text>
          <text class="coupon-scope">{{ scopeText(item.applyType) }}</text>
          <text class="coupon-time">{{ dateText(item.startTime) }} 至 {{ dateText(item.endTime) }}</text>
          <text class="coupon-use">查看 {{ item.targets?.length || 0 }} 个可用{{ targetLabel(item.applyType) }} ›</text>
        </view>
      </view>
      <EmptyState v-if="!coupons.length" title="暂无可用优惠券" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getMyCoupons } from '@/api/mine'
import { dateText, money } from '@/utils/format'
import { pickResult } from '@/utils/request'

const coupons = ref([])

onShow(async () => {
  try {
    const response = await getMyCoupons()
    coupons.value = pickResult(response, 'couponForOccassion', [])
  } catch (error) {
    coupons.value = []
    uni.showToast({ title: error.message || '优惠券加载失败', icon: 'none' })
  }
})

function scopeText(type) {
  return ({ 0: '课程与商品通用', 1: '仅限实体商品', 2: '仅限视频课程' })[type] || '指定商品可用'
}

function targetLabel(type) {
  return Number(type) === 2 ? '课程' : Number(type) === 1 ? '商品' : '内容'
}

function useCoupon(coupon) {
  const targets = Array.isArray(coupon.targets) ? coupon.targets : []
  if (!targets.length) {
    uni.showToast({ title: '暂无可用商品或课程', icon: 'none' })
    return
  }
  if (targets.length === 1) {
    openTarget(targets[0])
    return
  }
  uni.showActionSheet({
    itemList: targets.map((item) => item.targetName || `编号 ${item.targetId}`),
    success: (result) => openTarget(targets[result.tapIndex]),
  })
}

function openTarget(target) {
  if (!target) return
  if (Number(target.targetType) === 1) {
    uni.navigateTo({ url: `/pages/store/detail?id=${target.targetId}` })
    return
  }
  uni.navigateTo({ url: `/pages/course/detail?id=${target.targetId}` })
}
</script>

<style scoped>
.coupon-page { min-height: 100vh; background: #f4f8f8; }
.coupon-card { min-height: 170rpx; margin-bottom: 18rpx; border-radius: 18rpx; background: #fff; display: flex; overflow: hidden; box-shadow: 0 9rpx 25rpx rgba(31,63,69,.06); }
.amount-area { width: 200rpx; background: linear-gradient(145deg, #20bea3, #5bd1bc); color: #fff; display: flex; align-items: center; justify-content: center; }
.currency { font-size: 24rpx; font-weight: 800; }
.amount { font-size: 46rpx; font-weight: 900; }
.coupon-main { flex: 1; min-width: 0; padding: 24rpx; }
.coupon-name, .coupon-scope, .coupon-time { display: block; }
.coupon-name { color: #2d3a40; font-size: 28rpx; font-weight: 900; }
.coupon-scope { margin-top: 12rpx; color: #20b99f; font-size: 22rpx; }
.coupon-time { margin-top: 10rpx; color: #99a4a9; font-size: 19rpx; }
.coupon-use { margin-top: 13rpx; color: #18a991; font-size: 21rpx; font-weight: 800; }
</style>

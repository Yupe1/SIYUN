<template>
  <view class="detail-page">
    <SubPageNav title="商品详情" />

    <swiper v-if="images.length" class="image-swiper" indicator-dots circular>
      <swiper-item v-for="image in images" :key="image">
        <image class="detail-image" :src="image" mode="aspectFill" />
      </swiper-item>
    </swiper>
    <view v-else class="detail-image image-placeholder">思云好物</view>

    <view v-if="goods" class="goods-main">
      <view class="price-line">
        <text class="price-symbol">￥</text>
        <text class="price">{{ money(unitPrice) }}</text>
        <text v-if="selectedCoupon" class="coupon-saving">优惠券减￥{{ money(couponDiscount) }}</text>
      </view>
      <text class="goods-name">{{ goods.goodsName }}</text>
      <text class="intro">{{ goods.intro || '精心挑选的学习好物。' }}</text>
      <view v-if="serviceTags.length" class="service-tags">
        <text v-for="tag in serviceTags" :key="tag" class="service-tag">✓ {{ tag }}</text>
      </view>
    </view>

    <view v-if="goods" class="purchase-card">
      <view class="quantity-row">
        <text>购买数量</text>
        <view class="stepper">
          <button class="step" @tap="changeQuantity(-1)">−</button>
          <text class="quantity">{{ quantity }}</text>
          <button class="step" @tap="changeQuantity(1)">＋</button>
        </view>
      </view>
      <view class="coupon-row">
        <text>优惠券</text>
        <picker
          v-if="coupons.length"
          mode="selector"
          :range="couponLabels"
          :value="selectedCouponIndex"
          @change="selectCoupon"
        >
          <view class="coupon-picker">{{ selectedCouponLabel }} ›</view>
        </picker>
        <text v-else class="coupon-empty">暂无可用券，按原价购买</text>
      </view>
      <textarea
        v-model.trim="fullAddress"
        class="address-input"
        maxlength="200"
        placeholder="请填写完整收货地址"
        placeholder-class="placeholder"
      />
      <input
        v-model.trim="remark"
        class="remark-input"
        maxlength="100"
        placeholder="订单备注（选填）"
        placeholder-class="placeholder"
      />
    </view>

    <view class="purchase-bar">
      <view class="total">
        <text class="total-label">合计</text>
        <text class="total-price">￥{{ money(totalPrice) }}</text>
      </view>
      <button class="buy-button" @tap="buy">{{ userStore.isLoggedIn ? '立即购买' : '登录后购买' }}</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import SubPageNav from '@/components/SubPageNav.vue'
import { createGoodsOrder, getAvailableCoupons, getGoodsDetail } from '@/api/commerce'
import { assetUrl, money } from '@/utils/format'
import { redirectForRecharge } from '@/utils/payment'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const goods = ref(null)
const quantity = ref(1)
const fullAddress = ref('')
const remark = ref('')
const coupons = ref([])
const selectedCouponUserId = ref(null)

const images = computed(() => String(goods.value?.mainPicUrl || '')
  .split(',')
  .map((item) => assetUrl(item.trim()))
  .filter(Boolean))
const unitPrice = computed(() => Number(goods.value?.priceOriginal || 0))
const originalTotal = computed(() => unitPrice.value * quantity.value)
const selectedCoupon = computed(() => coupons.value.find(
  (item) => Number(item.couponUserId) === Number(selectedCouponUserId.value),
) || null)
const couponDiscount = computed(() => Math.min(
  Number(selectedCoupon.value?.amount || 0),
  originalTotal.value,
))
const totalPrice = computed(() => Math.max(0, originalTotal.value - couponDiscount.value))
const couponOptions = computed(() => [
  { couponUserId: null, label: '不使用优惠券' },
  ...coupons.value.map((item, index) => ({
    ...item,
    label: `${index === 0 ? '最优 · ' : ''}${item.couponName}（减￥${money(item.amount)}）`,
  })),
])
const couponLabels = computed(() => couponOptions.value.map((item) => item.label))
const selectedCouponIndex = computed(() => {
  const index = couponOptions.value.findIndex(
    (item) => Number(item.couponUserId || 0) === Number(selectedCouponUserId.value || 0),
  )
  return index < 0 ? 0 : index
})
const selectedCouponLabel = computed(() => couponOptions.value[selectedCouponIndex.value]?.label || '不使用优惠券')
const serviceTags = computed(() => String(goods.value?.serviceTags || '')
  .split(',')
  .map((item) => item.trim())
  .filter(Boolean))

onLoad((query = {}) => {
  userStore.hydrate()
  if (query.id) {
    loadDetail(query.id)
  }
})

async function loadDetail(id) {
  try {
    const response = await getGoodsDetail(id)
    goods.value = pickResult(response, 'goods', null)
    if (userStore.isLoggedIn && goods.value?.id) {
      await loadCoupons(goods.value.id)
    }
  } catch (error) {
    uni.showToast({ title: error.message || '商品加载失败', icon: 'none' })
  }
}

async function loadCoupons(goodsId) {
  try {
    const response = await getAvailableCoupons(1, goodsId, quantity.value)
    coupons.value = pickResult(response, 'coupons', [])
    selectedCouponUserId.value = coupons.value[0]?.couponUserId || null
  } catch (error) {
    coupons.value = []
    selectedCouponUserId.value = null
  }
}

function selectCoupon(event) {
  const option = couponOptions.value[Number(event.detail.value || 0)]
  selectedCouponUserId.value = option?.couponUserId || null
}

function changeQuantity(delta) {
  quantity.value = Math.min(99, Math.max(1, quantity.value + delta))
}

async function buy() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (!goods.value) {
    return
  }
  if (!fullAddress.value) {
    uni.showToast({ title: '请填写收货地址', icon: 'none' })
    return
  }
  try {
    await createGoodsOrder({
      goodsId: goods.value.id,
      quantity: quantity.value,
      fullAddress: fullAddress.value,
      userRemark: remark.value,
      ...(selectedCouponUserId.value ? { couponUserId: selectedCouponUserId.value } : {}),
    })
    uni.showToast({ title: '下单成功', icon: 'success' })
    setTimeout(() => uni.redirectTo({ url: '/pages/mine/orders' }), 500)
  } catch (error) {
    if (redirectForRecharge(error, `/pages/store/detail?id=${goods.value.id}`)) {
      return
    }
    uni.showToast({ title: error.message || '下单失败', icon: 'none' })
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding-bottom: calc(132rpx + env(safe-area-inset-bottom));
  background: #f4f8f8;
}

.image-swiper,
.detail-image {
  width: 100%;
  height: 660rpx;
}

.detail-image {
  display: block;
  background: #e3edef;
}

.image-placeholder {
  color: #9bacb1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.goods-main,
.purchase-card {
  margin-top: 16rpx;
  padding: 28rpx;
  background: #ffffff;
}

.price-line {
  display: flex;
  align-items: baseline;
}

.price-symbol,
.price,
.total-price {
  color: #ef684d;
  font-weight: 900;
}

.price-symbol {
  font-size: 25rpx;
}

.price {
  font-size: 48rpx;
}

.origin-price {
  margin-left: 14rpx;
  color: #a8b1b5;
  font-size: 23rpx;
  text-decoration: line-through;
}

.coupon-saving {
  margin-left: 16rpx;
  padding: 5rpx 10rpx;
  border-radius: 8rpx;
  background: #fff0ec;
  color: #ef684d;
  font-size: 20rpx;
  font-weight: 800;
}

.goods-name {
  display: block;
  margin-top: 16rpx;
  color: #26343a;
  font-size: 34rpx;
  line-height: 45rpx;
  font-weight: 900;
}

.intro {
  display: block;
  margin-top: 12rpx;
  color: #78868c;
  font-size: 25rpx;
  line-height: 40rpx;
}

.service-tags {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx 22rpx;
}

.service-tag {
  color: #20b99f;
  font-size: 22rpx;
}

.quantity-row {
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #344249;
  font-size: 26rpx;
}

.coupon-row {
  min-height: 76rpx;
  border-top: 1rpx solid #edf1f2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #344249;
  font-size: 25rpx;
}

.coupon-picker {
  max-width: 470rpx;
  color: #ef684d;
  font-size: 23rpx;
  text-align: right;
}

.coupon-empty {
  color: #98a5aa;
  font-size: 22rpx;
}

.stepper {
  display: flex;
  align-items: center;
}

.step {
  width: 58rpx;
  height: 50rpx;
  border-radius: 12rpx;
  background: #edf4f4;
  color: #4e6067;
  font-size: 28rpx;
}

.quantity {
  width: 68rpx;
  text-align: center;
  font-size: 27rpx;
}

.address-input {
  width: 100%;
  height: 128rpx;
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 14rpx;
  background: #f4f8f8;
  color: #344249;
  font-size: 25rpx;
  line-height: 37rpx;
}

.remark-input {
  height: 72rpx;
  margin-top: 16rpx;
  padding: 0 20rpx;
  border-radius: 14rpx;
  background: #f4f8f8;
  color: #344249;
  font-size: 25rpx;
}

.placeholder {
  color: #a6b1b5;
}

.purchase-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  height: calc(112rpx + env(safe-area-inset-bottom));
  padding: 12rpx 24rpx env(safe-area-inset-bottom);
  background: #ffffff;
  border-top: 1rpx solid #e7eeee;
  display: flex;
  align-items: flex-start;
}

.total {
  flex: 1;
  height: 86rpx;
  display: flex;
  align-items: center;
}

.total-label {
  color: #7f8c91;
  font-size: 23rpx;
}

.total-price {
  margin-left: 10rpx;
  font-size: 34rpx;
}

.buy-button {
  width: 270rpx;
  height: 86rpx;
  border-radius: 43rpx;
  background: #20bea3;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 900;
}
</style>

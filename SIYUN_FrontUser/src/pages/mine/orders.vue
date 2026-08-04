<template>
  <view class="orders-page">
    <SubPageNav title="我的订单" />
    <view class="content">
      <view v-for="item in orders" :key="item.id" class="order-card card">
        <view class="order-head">
          <text class="order-sn">{{ item.orderSn || `订单 ${item.id}` }}</text>
          <text class="order-status">{{ statusText(item.status) }}</text>
        </view>
        <view class="order-body">
          <image v-if="image(item.imageUrl)" class="order-image" :src="image(item.imageUrl)" mode="aspectFill" />
          <view v-else class="order-image image-placeholder">{{ item.entityType === 2 ? '商' : '课' }}</view>
          <view class="order-main">
            <text class="item-name">{{ item.itemName }}</text>
            <text class="order-meta">数量 × {{ item.totalQuantity || 1 }}</text>
            <text class="order-time">{{ dateText(item.createTime) }}</text>
          </view>
          <text class="order-price">￥{{ money(item.pricePay) }}</text>
        </view>
        <view v-if="item.deliverySn" class="delivery">物流单号：{{ item.deliverySn }}</view>
      </view>
      <EmptyState v-if="!orders.length" title="暂无订单" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getMyOrders } from '@/api/commerce'
import { assetUrl, dateText, money } from '@/utils/format'
import { pickResult } from '@/utils/request'

const orders = ref([])

onShow(async () => {
  try {
    const response = await getMyOrders()
    orders.value = pickResult(response, 'orders', [])
  } catch (error) {
    orders.value = []
    uni.showToast({ title: error.message || '订单加载失败', icon: 'none' })
  }
})

function image(value) {
  return assetUrl(String(value || '').split(',').map((item) => item.trim()).find(Boolean))
}

function statusText(status) {
  return ({ 0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已签收', 5: '退货申请', 6: '退货中', 7: '已退货', 8: '已取消' })[status] || '处理中'
}
</script>

<style scoped>
.orders-page { min-height: 100vh; background: #f4f8f8; }
.order-card { margin-bottom: 18rpx; overflow: hidden; }
.order-head { height: 72rpx; padding: 0 22rpx; display: flex; align-items: center; border-bottom: 1rpx solid #edf1f2; }
.order-sn { flex: 1; color: #849197; font-size: 21rpx; }
.order-status { color: #20b99f; font-size: 23rpx; font-weight: 800; }
.order-body { padding: 20rpx 22rpx; display: flex; align-items: center; }
.order-image { width: 126rpx; height: 126rpx; border-radius: 13rpx; background: #dce8eb; }
.image-placeholder { display: flex; align-items: center; justify-content: center; color: #18bda4; font-size: 32rpx; font-weight: 900; }
.order-main { flex: 1; min-width: 0; margin-left: 18rpx; }
.item-name { display: block; color: #2c3a40; font-size: 27rpx; font-weight: 800; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.order-meta, .order-time { display: block; margin-top: 9rpx; color: #929ea3; font-size: 21rpx; }
.order-price { margin-left: 14rpx; color: #ed6c51; font-size: 26rpx; font-weight: 900; }
.delivery { padding: 0 22rpx 20rpx; color: #7e8c92; font-size: 22rpx; }
</style>

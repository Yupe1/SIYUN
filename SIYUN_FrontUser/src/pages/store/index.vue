<template>
  <view class="store-page">
    <SubPageNav title="微商城">
      <button class="orders-link" @tap="goOrders">订单</button>
    </SubPageNav>

    <view class="store-hero">
      <text class="hero-title">学习好物</text>
      <text class="hero-desc">把热爱和效率，一起带回家</text>
      <view class="search-box store-search">
        <input
          v-model.trim="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索商品"
          placeholder-class="search-placeholder"
          @confirm="loadGoods"
        />
        <button class="search-button" @tap="loadGoods">搜索</button>
      </view>
    </view>

    <scroll-view scroll-x class="category-scroll" :show-scrollbar="false">
      <view class="category-row">
        <button class="category-pill" :class="{ active: !activeCategory }" @tap="selectCategory(null)">全部</button>
        <button
          v-for="item in categories"
          :key="item.id"
          class="category-pill"
          :class="{ active: activeCategory === item.id }"
          @tap="selectCategory(item.id)"
        >
          {{ item.cateName }}
        </button>
      </view>
    </scroll-view>

    <view class="goods-grid">
      <view v-for="item in goods" :key="item.id" class="goods-card" @tap="openGoods(item)">
        <image v-if="firstImage(item.mainPicUrl)" class="goods-image" :src="firstImage(item.mainPicUrl)" mode="aspectFill" />
        <view v-else class="goods-image goods-placeholder">思云好物</view>
        <view class="goods-info">
          <text class="goods-name">{{ item.goodsName }}</text>
          <text class="goods-intro">{{ item.intro || '精选学习好物' }}</text>
          <view class="price-row">
            <text class="price-symbol">￥</text>
            <text class="price">{{ money(item.priceOriginal) }}</text>
            <text v-if="item.recommendStatus === 2" class="recommend">推荐</text>
          </view>
        </view>
      </view>
    </view>
    <EmptyState v-if="!loading && !goods.length" title="暂无上架商品" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SubPageNav from '@/components/SubPageNav.vue'
import { getGoods, getGoodsCategories } from '@/api/commerce'
import { assetUrl, money } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const keyword = ref('')
const categories = ref([])
const activeCategory = ref(null)
const goods = ref([])
const loading = ref(false)

onLoad(() => {
  userStore.hydrate()
  Promise.all([loadCategories(), loadGoods()])
})

onPullDownRefresh(async () => {
  await loadGoods()
  uni.stopPullDownRefresh()
})

async function loadCategories() {
  try {
    const response = await getGoodsCategories()
    categories.value = pickResult(response, 'categories', [])
  } catch (error) {
    categories.value = []
  }
}

async function loadGoods() {
  loading.value = true
  try {
    const response = await getGoods(keyword.value, activeCategory.value)
    goods.value = pickResult(response, 'goods', [])
  } catch (error) {
    goods.value = []
    uni.showToast({ title: error.message || '商品加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  activeCategory.value = id
  loadGoods()
}

function firstImage(value) {
  const path = String(value || '').split(',').map((item) => item.trim()).find(Boolean)
  return assetUrl(path)
}

function openGoods(item) {
  uni.navigateTo({ url: `/pages/store/detail?id=${item.id}` })
}

function goOrders() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.navigateTo({ url: '/pages/mine/orders' })
}
</script>

<style scoped>
.store-page {
  min-height: 100vh;
  padding-bottom: 48rpx;
  background: #f4f8f8;
}

.orders-link {
  color: #18bda4;
  font-size: 25rpx;
  font-weight: 800;
}

.store-hero {
  padding: 34rpx 28rpx 30rpx;
  background: linear-gradient(145deg, #21bea4, #64d5c1);
  color: #ffffff;
}

.hero-title,
.hero-desc {
  display: block;
}

.hero-title {
  font-size: 42rpx;
  line-height: 52rpx;
  font-weight: 900;
}

.hero-desc {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.84);
  font-size: 24rpx;
}

.store-search {
  margin-top: 28rpx;
  background: #ffffff;
}

.search-button {
  color: #18bda4;
  font-size: 24rpx;
  font-weight: 800;
}

.category-scroll {
  width: 100%;
  padding: 22rpx 0 14rpx;
  white-space: nowrap;
}

.category-row {
  padding: 0 24rpx;
  display: inline-flex;
  gap: 14rpx;
}

.category-pill {
  height: 58rpx;
  padding: 0 26rpx;
  border-radius: 29rpx;
  background: #ffffff;
  color: #718087;
  font-size: 23rpx;
  box-shadow: 0 5rpx 16rpx rgba(31, 63, 69, 0.05);
}

.category-pill.active {
  background: #20bea3;
  color: #ffffff;
  font-weight: 800;
}

.goods-grid {
  padding: 12rpx 24rpx 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.goods-card {
  min-width: 0;
  overflow: hidden;
  border-radius: 18rpx;
  background: #ffffff;
  box-shadow: 0 9rpx 25rpx rgba(31, 63, 69, 0.07);
}

.goods-image {
  width: 100%;
  height: 250rpx;
  display: block;
  background: #e7f0f1;
}

.goods-placeholder {
  color: #9aacb0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 25rpx;
}

.goods-info {
  padding: 18rpx;
}

.goods-name,
.goods-intro {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-name {
  color: #28363c;
  font-size: 27rpx;
  font-weight: 800;
}

.goods-intro {
  margin-top: 7rpx;
  color: #99a5aa;
  font-size: 21rpx;
}

.price-row {
  margin-top: 13rpx;
  display: flex;
  align-items: baseline;
}

.price-symbol,
.price {
  color: #ef6c50;
  font-weight: 900;
}

.price-symbol {
  font-size: 20rpx;
}

.price {
  font-size: 31rpx;
}

.recommend {
  margin-left: auto;
  padding: 3rpx 9rpx;
  border-radius: 8rpx;
  background: #fff0e9;
  color: #ef6c50;
  font-size: 18rpx;
}
</style>

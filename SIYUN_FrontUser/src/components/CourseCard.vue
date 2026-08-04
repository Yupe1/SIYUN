<template>
  <view
    class="course-card card"
    :class="{ 'grid-card': layout === 'grid' }"
    @click="$emit('select', course)"
  >
    <view class="cover">
      <image
        v-if="cover"
        class="cover-image"
        :src="cover"
        :mode="layout === 'grid' ? 'widthFix' : 'aspectFill'"
      />
      <view v-else class="cover-fallback">
        <text>{{ course.cateName || '课程' }}</text>
      </view>
    </view>
    <view class="course-body">
      <view class="course-top">
        <text class="course-title">{{ course.title || '未命名课程' }}</text>
        <text v-if="course.recommendType === 1" class="tag">新品</text>
        <text v-else-if="course.recommendType === 2" class="tag">推荐</text>
      </view>
      <text class="course-intro">{{ course.intro || course.detailDesc || '暂无课程简介' }}</text>
      <view class="meta-row">
        <text class="teacher">{{ course.teacherName || '讲师待定' }}</text>
        <text class="duration">{{ course.episodeNum || 0 }}讲 · {{ course.duration || 0 }}分钟</text>
      </view>
      <view class="bottom-row">
        <view class="price-line">
          <text v-if="salePrice !== null" class="price">￥{{ money(salePrice) }}</text>
          <text v-else class="price">价格待定</text>
        </view>
        <text class="stats">{{ compactNumber(course.countView) }}人看过</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import {
  assetUrl,
  compactNumber,
  money,
} from '@/utils/format'

const props = defineProps({
  course: {
    type: Object,
    required: true,
  },
  layout: {
    type: String,
    default: 'horizontal',
  },
})

defineEmits(['select'])

const cover = computed(() => assetUrl(props.course.coverUrl))
const salePrice = computed(() => {
  const value = props.course.priceOriginal
  if (value === null || value === undefined || value === '') {
    return null
  }
  const number = Number(value)
  return Number.isFinite(number) && number >= 0 ? number : null
})
</script>

<style scoped>
.course-card {
  width: 100%;
  min-height: 220rpx;
  padding: 18rpx;
  display: flex;
  text-align: left;
  margin-bottom: 18rpx;
}

.cover {
  width: 208rpx;
  height: 164rpx;
  flex: none;
  border-radius: 12rpx;
  overflow: hidden;
  background: #dbe8eb;
}

.cover-image,
.cover-fallback {
  width: 100%;
  height: 100%;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6e7f86;
  font-size: 28rpx;
  font-weight: 800;
  background: linear-gradient(135deg, #cce5ea, #eef6f4);
}

.course-body {
  min-width: 0;
  flex: 1;
  padding-left: 20rpx;
}

.course-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.course-title {
  flex: 1;
  min-width: 0;
  font-size: 30rpx;
  line-height: 38rpx;
  color: #243238;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-intro {
  display: -webkit-box;
  margin-top: 10rpx;
  color: #7d8b91;
  font-size: 24rpx;
  line-height: 34rpx;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.meta-row,
.bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-row {
  margin-top: 12rpx;
  color: #8d9aa0;
  font-size: 22rpx;
}

.teacher,
.duration,
.stats {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.duration {
  margin-left: 12rpx;
}

.bottom-row {
  margin-top: 14rpx;
}

.price-line {
  display: flex;
  align-items: baseline;
}

.price {
  color: #f06b4f;
  font-size: 30rpx;
  font-weight: 800;
}

.origin {
  margin-left: 10rpx;
  color: #a5b0b4;
  font-size: 22rpx;
  text-decoration: line-through;
}

.stats {
  max-width: 150rpx;
  color: #8d9aa0;
  font-size: 22rpx;
}

.course-card.grid-card {
  min-width: 0;
  min-height: 0;
  margin-bottom: 0;
  padding: 0;
  display: block;
  overflow: hidden;
}

.grid-card .cover {
  width: 100%;
  height: auto;
  border-radius: 0;
}

.grid-card .cover-image {
  height: auto;
  display: block;
}

.grid-card .cover-fallback {
  height: 196rpx;
}

.grid-card .course-body {
  padding: 18rpx;
}

.grid-card .course-top {
  align-items: flex-start;
}

.grid-card .course-title {
  display: -webkit-box;
  font-size: 27rpx;
  line-height: 34rpx;
  white-space: normal;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.grid-card .tag {
  flex: none;
  height: 34rpx;
  padding: 0 10rpx;
  font-size: 18rpx;
}

.grid-card .course-intro {
  margin-top: 8rpx;
  color: #849197;
  font-size: 21rpx;
  line-height: 30rpx;
}

.grid-card .meta-row {
  min-width: 0;
  margin-top: 12rpx;
  display: block;
  font-size: 20rpx;
}

.grid-card .teacher,
.grid-card .duration {
  display: block;
}

.grid-card .duration {
  margin: 6rpx 0 0;
}

.grid-card .bottom-row {
  margin-top: 13rpx;
}

.grid-card .price {
  font-size: 27rpx;
}

.grid-card .origin {
  display: none;
}

.grid-card .stats {
  max-width: 118rpx;
  font-size: 19rpx;
}
</style>

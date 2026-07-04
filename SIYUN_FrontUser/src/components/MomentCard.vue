<template>
  <view class="moment-card card" :class="{ compact: compact }" @tap="$emit('select', moment)">
    <view v-if="!compact" class="moment-head">
      <view class="avatar">{{ avatarText }}</view>
      <view class="head-main">
        <text class="title">{{ moment.title || '未命名动态' }}</text>
        <text class="time">{{ dateText(moment.createTime) || '刚刚' }}</text>
      </view>
      <button v-if="deletable" class="delete" @tap.stop="$emit('delete', moment)">删除</button>
    </view>
    <text v-else class="compact-title">{{ moment.title || '未命名动态' }}</text>
    <text class="content">{{ moment.content || '暂无内容' }}</text>
    <image v-if="cover && !compact" class="cover" :src="cover" mode="aspectFill" />
    <view v-if="!compact" class="moment-foot">
      <text>{{ compactNumber(moment.countView) }} 浏览</text>
      <text>{{ compactNumber(moment.countLike) }} 点赞</text>
      <text>{{ compactNumber(moment.countComment) }} 评论</text>
      <text>{{ compactNumber(moment.countShare) }} 分享</text>
    </view>
    <view v-if="showActions && !compact" class="moment-actions">
      <button class="action" @tap.stop="$emit('like', moment)">点赞</button>
      <button class="action" @tap.stop="$emit('collect', moment)">收藏</button>
      <button class="action" @tap.stop="$emit('share', moment)">分享</button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { assetUrl, compactNumber, dateText } from '@/utils/format'

const props = defineProps({
  moment: {
    type: Object,
    required: true,
  },
  currentUserId: {
    type: Number,
    default: 0,
  },
  compact: {
    type: Boolean,
    default: false,
  },
  showActions: {
    type: Boolean,
    default: true,
  },
})

defineEmits(['delete', 'like', 'collect', 'share', 'select'])

const cover = computed(() => assetUrl(props.moment.coverUrl))
const deletable = computed(() => props.currentUserId && props.currentUserId === props.moment.authorId)
const avatarText = computed(() => (props.moment.title || '思').slice(0, 1))
</script>

<style scoped>
.moment-card {
  padding: 24rpx;
  margin-bottom: 18rpx;
}

.moment-card.compact {
  min-height: 148rpx;
  padding: 22rpx 24rpx;
}

.moment-head {
  display: flex;
  align-items: center;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: #e5f8f4;
  color: #18bda4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 900;
}

.head-main {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.title {
  display: block;
  font-size: 30rpx;
  line-height: 38rpx;
  color: #253238;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  display: block;
  margin-top: 6rpx;
  color: #96a2a7;
  font-size: 22rpx;
}

.delete {
  width: 84rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: #fff1ed;
  color: #f06b4f;
  font-size: 22rpx;
}

.content {
  display: block;
  margin-top: 20rpx;
  color: #5d6b71;
  font-size: 27rpx;
  line-height: 42rpx;
}

.compact-title {
  display: block;
  color: #253238;
  font-size: 30rpx;
  line-height: 38rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.compact .content {
  margin-top: 12rpx;
  line-height: 38rpx;
  max-height: 76rpx;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 300rpx;
  margin-top: 18rpx;
  border-radius: 12rpx;
  background: #dce8eb;
}

.moment-foot {
  margin-top: 22rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx 26rpx;
  color: #8e9ba1;
  font-size: 22rpx;
}

.moment-actions {
  margin-top: 18rpx;
  display: flex;
  gap: 16rpx;
}

.action {
  flex: 1;
  height: 56rpx;
  border-radius: 28rpx;
  background: #f1f7f7;
  color: #6d7c82;
  font-size: 23rpx;
}
</style>

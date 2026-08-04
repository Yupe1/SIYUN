<template>
  <view class="comment-thread">
    <view v-if="userStore.isLoggedIn" class="comment-form">
      <input
        v-model.trim="rootText"
        class="comment-input"
        maxlength="1000"
        placeholder="说说你的想法…"
        placeholder-class="placeholder"
        confirm-type="send"
        @confirm="submitRoot"
      />
      <button class="comment-send" :disabled="submitting" @tap="submitRoot">发送</button>
    </view>
    <button v-else class="login-tip" @tap="goLogin">登录后参与评论</button>

    <view v-for="comment in comments" :key="comment.id" class="comment-item">
      <view class="comment-avatar">{{ avatarText(comment) }}</view>
      <view class="comment-main">
        <view class="comment-head">
          <text class="comment-user">用户 {{ comment.userId }}</text>
          <text class="comment-time">{{ dateText(comment.createTime) }}</text>
        </view>
        <text class="comment-content">{{ comment.content }}</text>
        <view class="comment-actions">
          <view class="comment-action" @tap.stop="openReply(comment)">回复</view>
          <view
            v-if="Number(comment.countReply || 0) > 0"
            class="comment-action reply-toggle"
            @tap.stop="toggleReplies(comment)"
          >
            {{ repliesVisible[comment.id]
              ? '收起回复'
              : `查看 ${comment.countReply} 条回复` }}
          </view>
          <view
            v-if="Number(comment.userId) === Number(userStore.user?.id)"
            class="comment-action danger"
            @tap.stop="removeComment(comment, 0)"
          >
            删除
          </view>
        </view>

        <view
          v-if="replyTarget && Number(replyTarget.rootId) === Number(comment.id)"
          class="reply-composer"
        >
          <view class="reply-composer-head">
            <text class="reply-composer-title">回复用户 {{ replyTarget.userId }}</text>
            <text class="reply-composer-close" @tap.stop="closeReply">取消</text>
          </view>
          <textarea
            v-model.trim="replyText"
            class="reply-composer-input"
            :focus="Boolean(replyTarget)"
            maxlength="1000"
            placeholder="输入回复内容"
            placeholder-class="placeholder"
          />
          <button
            class="reply-composer-send"
            :disabled="submitting"
            @tap.stop="submitReply"
          >
            {{ submitting ? '发送中…' : '发送回复' }}
          </button>
        </view>

        <view v-if="repliesVisible[comment.id]" class="reply-list">
          <view v-for="reply in replies[comment.id] || []" :key="reply.id" class="reply-item">
            <view class="reply-head">
              <text class="comment-user">用户 {{ reply.userId }}</text>
              <text class="comment-time">{{ dateText(reply.createTime) }}</text>
            </view>
            <text class="reply-content">{{ reply.content }}</text>
            <view class="comment-actions">
              <view class="comment-action" @tap.stop="openReply(comment, reply)">回复</view>
              <view
                v-if="Number(reply.userId) === Number(userStore.user?.id)"
                class="comment-action danger"
                @tap.stop="removeComment(reply, comment.id)"
              >
                删除
              </view>
            </view>
          </view>
          <text v-if="!(replies[comment.id] || []).length" class="reply-empty">暂无回复</text>
        </view>
      </view>
    </view>

    <EmptyState v-if="!loading && !comments.length" title="暂无评论，来留下第一条吧" />
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import {
  addComment,
  deleteComment,
  getComments,
  getSubComments,
} from '@/api/comment'
import { dateText } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  entityId: {
    type: Number,
    required: true,
  },
  entityType: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['change'])
const userStore = useUserStore()
const comments = ref([])
const replies = ref({})
const repliesVisible = ref({})
const rootText = ref('')
const replyText = ref('')
const replyTarget = ref(null)
const loading = ref(false)
const submitting = ref(false)

watch(
  () => [props.entityId, props.entityType],
  ([entityId]) => {
    if (entityId) loadComments()
  },
  { immediate: true },
)

function avatarText(comment) {
  return String(comment.userId || '用').slice(-1)
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function ensureLogin() {
  userStore.hydrate()
  if (userStore.isLoggedIn) return true
  goLogin()
  return false
}

async function loadComments() {
  loading.value = true
  try {
    const response = await getComments(props.entityId, props.entityType)
    comments.value = pickResult(response, 'commentList', [])
  } catch (error) {
    comments.value = []
    uni.showToast({ title: error.message || '评论加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadReplies(commentId) {
  try {
    const response = await getSubComments(commentId)
    replies.value = {
      ...replies.value,
      [commentId]: pickResult(response, 'commentList', []),
    }
  } catch (error) {
    uni.showToast({ title: error.message || '回复加载失败', icon: 'none' })
  }
}

async function submitRoot() {
  if (!ensureLogin() || submitting.value) return
  if (!rootText.value) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const response = await addComment(props.entityId, props.entityType, rootText.value)
    const created = pickResult(response, 'comment', null)
    rootText.value = ''
    if (created) {
      comments.value = [created, ...comments.value.filter((item) => item.id !== created.id)]
    }
    await loadComments()
    emit('change', 1)
    uni.showToast({ title: '评论成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '评论失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function openReply(comment, reply = null) {
  if (!ensureLogin()) return
  if (!comment?.id) return
  replyTarget.value = {
    rootId: Number(comment.id),
    userId: Number(reply?.userId || comment.userId || 0),
    prefix: reply ? `回复 @用户${reply.userId}：` : '',
  }
  replyText.value = ''
}

function closeReply() {
  if (submitting.value) return
  replyTarget.value = null
  replyText.value = ''
}

async function submitReply() {
  if (!ensureLogin() || submitting.value) return
  if (!replyText.value) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }
  const target = replyTarget.value
  const rootId = Number(target?.rootId || 0)
  if (!rootId) return
  submitting.value = true
  try {
    const content = `${target.prefix || ''}${replyText.value}`.trim()
    await addComment(
      props.entityId,
      props.entityType,
      content,
      rootId,
    )
    replyText.value = ''
    replyTarget.value = null
    repliesVisible.value = { ...repliesVisible.value, [rootId]: true }
    await Promise.all([
      loadComments(),
      loadReplies(rootId),
    ])
    emit('change', 1)
    uni.showToast({ title: '回复成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error.message || '回复失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function toggleReplies(comment) {
  const visible = !repliesVisible.value[comment.id]
  repliesVisible.value = { ...repliesVisible.value, [comment.id]: visible }
  if (visible && !replies.value[comment.id]) {
    await loadReplies(comment.id)
  }
}

function removeComment(comment, rootId) {
  uni.showModal({
    title: '删除评论',
    content: '确认删除这条评论吗？',
    confirmColor: '#ed6f58',
    success: async (result) => {
      if (!result.confirm) return
      try {
        const removedCount = rootId ? 1 : 1 + Number(comment.countReply || 0)
        await deleteComment(comment.id)
        if (rootId) {
          const root = comments.value.find((item) => item.id === rootId)
          if (root) root.countReply = Math.max(0, Number(root.countReply || 0) - 1)
          await loadReplies(rootId)
        } else {
          await loadComments()
        }
        emit('change', -removedCount)
        uni.showToast({ title: '已删除', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '删除失败', icon: 'none' })
      }
    },
  })
}
</script>

<style scoped>
.comment-thread {
  padding-bottom: 20rpx;
}

.comment-form {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.comment-input {
  flex: 1;
  min-width: 0;
  height: 72rpx;
  padding: 0 22rpx;
  border-radius: 36rpx;
  background: #f0f6f6;
  color: #344249;
  font-size: 25rpx;
}

.comment-send {
  flex: 0 0 auto;
  align-self: center;
  height: 72rpx;
  padding: 0 25rpx;
  border-radius: 34rpx;
  background: #20bea3;
  color: #fff;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-tip {
  width: 100%;
  height: 72rpx;
  margin-bottom: 20rpx;
  border-radius: 36rpx;
  background: #e6f8f4;
  color: #18a991;
  font-size: 25rpx;
}

.comment-item {
  display: flex;
  gap: 18rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #deeaec;
}

.comment-avatar {
  flex: 0 0 auto;
  width: 66rpx;
  height: 66rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #dff7f2, #edf9f7);
  color: #18a991;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 27rpx;
  font-weight: 900;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-head,
.reply-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.comment-user {
  color: #5b6c72;
  font-size: 23rpx;
  font-weight: 800;
}

.comment-time {
  color: #a2adb1;
  font-size: 19rpx;
}

.comment-content,
.reply-content {
  display: block;
  margin-top: 10rpx;
  color: #344249;
  font-size: 26rpx;
  line-height: 40rpx;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 22rpx;
  margin-top: 12rpx;
}

.comment-action {
  min-height: 40rpx;
  padding: 4rpx 2rpx;
  display: inline-flex;
  align-items: center;
  color: #829196;
  font-size: 22rpx;
  line-height: 1.5;
}

.comment-action.danger {
  color: #ed745e;
}

.reply-toggle {
  color: #18a991;
  font-weight: 700;
}

.reply-list {
  margin-top: 16rpx;
  padding: 4rpx 18rpx;
  border-radius: 14rpx;
  background: #f2f7f7;
}

.reply-item {
  padding: 18rpx 0;
  border-bottom: 1rpx solid #e2ebec;
}

.reply-item:last-child {
  border-bottom: 0;
}

.reply-empty {
  display: block;
  padding: 18rpx 0;
  color: #98a5aa;
  font-size: 22rpx;
  text-align: center;
}

.reply-composer {
  margin-top: 16rpx;
  padding: 18rpx;
  border: 1rpx solid #d9e8e7;
  border-radius: 16rpx;
  background: #f6fbfa;
}

.reply-composer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.reply-composer-title {
  color: #415359;
  font-size: 23rpx;
  font-weight: 800;
}

.reply-composer-close {
  padding: 4rpx 0 4rpx 16rpx;
  color: #8a989d;
  font-size: 22rpx;
}

.reply-composer-input {
  width: 100%;
  height: 140rpx;
  margin-top: 14rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  background: #ffffff;
  color: #344249;
  font-size: 24rpx;
  line-height: 36rpx;
}

.reply-composer-send {
  width: 180rpx;
  height: 60rpx;
  margin: 14rpx 0 0 auto;
  border-radius: 30rpx;
  background: #20bea3;
  color: #ffffff;
  font-size: 23rpx;
  font-weight: 800;
}
</style>

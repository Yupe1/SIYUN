<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ChatDotRound, Promotion, Refresh, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const conversations = ref([])
const selectedUserId = ref(null)
const activeUser = ref(null)
const messages = ref([])
const replyContent = ref('')
const listLoading = ref(false)
const messageLoading = ref(false)
const sending = ref(false)
const messageList = ref(null)

onMounted(() => loadConversations())

async function loadConversations(autoSelect = true) {
  listLoading.value = true
  try {
    const data = await http.get('/api/admin/service/conversations')
    conversations.value = data.result?.conversations || []
    if (selectedUserId.value) {
      const current = conversations.value.find((item) => Number(item.userId) === Number(selectedUserId.value))
      if (current) activeUser.value = { ...activeUser.value, ...current }
    }
    if (autoSelect && !selectedUserId.value && conversations.value.length) {
      await selectConversation(conversations.value[0])
    }
  } finally {
    listLoading.value = false
  }
}

async function selectConversation(conversation) {
  selectedUserId.value = conversation.userId
  activeUser.value = conversation
  replyContent.value = ''
  await loadMessages()
  await loadConversations(false)
}

async function loadMessages() {
  if (!selectedUserId.value) return
  messageLoading.value = true
  try {
    const data = await http.get(`/api/admin/service/conversations/${selectedUserId.value}/messages`)
    messages.value = data.result?.messages || []
    activeUser.value = {
      ...activeUser.value,
      ...(data.result?.user || {}),
      userId: selectedUserId.value,
      userNickname: data.result?.user?.nickname || activeUser.value?.userNickname,
    }
    await scrollToBottom()
  } finally {
    messageLoading.value = false
  }
}

async function sendReply() {
  const content = replyContent.value.trim()
  if (!content || !selectedUserId.value || sending.value) return
  sending.value = true
  try {
    await http.post(`/api/admin/service/conversations/${selectedUserId.value}/messages`, { content })
    replyContent.value = ''
    await loadMessages()
    await loadConversations(false)
    ElMessage.success('回复已发送')
  } finally {
    sending.value = false
  }
}

async function refreshAll() {
  await loadConversations(false)
  await loadMessages()
}

async function scrollToBottom() {
  await nextTick()
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

function isServiceMessage(message) {
  return Number(message.senderId) === 0
}

function displayTime(value) {
  if (!value) return ''
  const date = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}
</script>

<template>
  <section class="customer-service-page">
    <div class="service-workspace">
      <section class="chat-pane">
        <header class="chat-header">
          <div v-if="activeUser" class="active-user">
            <div class="user-avatar"><UserFilled /></div>
            <div>
              <strong>{{ activeUser.userNickname || activeUser.nickname }}</strong>
              <span>{{ activeUser.tel || '在线咨询用户' }}</span>
            </div>
          </div>
          <div v-else class="empty-title">
            <ChatDotRound />
            <span>请选择一个会话</span>
          </div>
          <el-button :icon="Refresh" circle text title="刷新会话" @click="refreshAll" />
        </header>

        <div ref="messageList" v-loading="messageLoading" class="message-list">
          <div v-if="!activeUser" class="center-empty">
            <ChatDotRound />
            <strong>暂无选中的对话</strong>
            <span>从右侧会话列表选择用户开始处理咨询</span>
          </div>
          <template v-else>
            <div class="conversation-date">用户咨询记录</div>
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-row"
              :class="{ service: isServiceMessage(message) }"
            >
              <div class="message-avatar">{{ isServiceMessage(message) ? '服' : '用' }}</div>
              <div class="message-body">
                <div class="message-meta">
                  <span>{{ isServiceMessage(message) ? (message.senderName || '在线客服') : (message.senderName || activeUser.userNickname) }}</span>
                  <time>{{ displayTime(message.sendTime) }}</time>
                </div>
                <div class="message-bubble">{{ message.content }}</div>
              </div>
            </div>
            <div v-if="!messages.length" class="center-empty compact">
              <span>这个会话还没有消息</span>
            </div>
          </template>
        </div>

        <footer class="composer">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            resize="none"
            :disabled="!activeUser"
            placeholder="输入回复内容，Enter 发送，Shift + Enter 换行"
            @keydown.enter.exact.prevent="sendReply"
          />
          <div class="composer-actions">
            <span>{{ activeUser ? `正在回复 ${activeUser.userNickname || activeUser.nickname}` : '请先选择会话' }}</span>
            <el-button
              type="primary"
              :icon="Promotion"
              :loading="sending"
              :disabled="!activeUser || !replyContent.trim()"
              @click="sendReply"
            >
              发送回复
            </el-button>
          </div>
        </footer>
      </section>

      <aside class="conversation-pane">
        <header class="conversation-header">
          <div>
            <strong>会话列表</strong>
            <span>{{ conversations.length }} 个咨询</span>
          </div>
          <el-button :icon="Refresh" circle text title="刷新列表" @click="loadConversations(false)" />
        </header>
        <div v-loading="listLoading" class="conversation-list">
          <button
            v-for="conversation in conversations"
            :key="conversation.userId"
            class="conversation-item"
            :class="{ active: Number(selectedUserId) === Number(conversation.userId) }"
            @click="selectConversation(conversation)"
          >
            <div class="list-avatar"><UserFilled /></div>
            <div class="conversation-info">
              <div class="conversation-name">
                <strong>{{ conversation.userNickname }}</strong>
                <time>{{ displayTime(conversation.lastTime) }}</time>
              </div>
              <div class="conversation-preview">
                <span>{{ conversation.lastMessage || '暂无消息' }}</span>
                <em v-if="conversation.unreadCount">{{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}</em>
              </div>
            </div>
          </button>
          <div v-if="!conversations.length && !listLoading" class="list-empty">
            <ChatDotRound />
            <span>暂时没有用户咨询</span>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.customer-service-page {
  height: calc(100vh - 98px);
  padding: 18px 20px 20px;
  overflow: hidden;
}

.service-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  height: 100%;
  overflow: hidden;
  background: #fff;
  border: 1px solid #dde3e8;
  border-radius: 6px;
  box-shadow: 0 5px 18px rgb(48 63 72 / 6%);
}

.chat-pane {
  display: grid;
  grid-template-rows: 66px minmax(0, 1fr) 154px;
  min-width: 0;
  background: #f5f7f8;
}

.chat-header,
.conversation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  background: #fff;
  border-bottom: 1px solid #e6eaed;
}

.active-user,
.empty-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.active-user strong,
.active-user span,
.conversation-header strong,
.conversation-header span {
  display: block;
}

.active-user strong,
.conversation-header strong {
  color: #303740;
  font-size: 15px;
}

.active-user span,
.conversation-header span {
  margin-top: 4px;
  color: #929ba3;
  font-size: 12px;
}

.user-avatar,
.list-avatar {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  color: #fff;
  background: linear-gradient(145deg, #38c5aa, #259e8b);
  border-radius: 50%;
}

.user-avatar svg,
.list-avatar svg {
  width: 18px;
}

.empty-title {
  color: #7c858d;
}

.empty-title svg {
  width: 22px;
}

.message-list {
  min-height: 0;
  padding: 24px 28px;
  overflow: auto;
}

.conversation-date {
  width: fit-content;
  margin: 0 auto 22px;
  padding: 4px 11px;
  color: #91999f;
  background: #e8ecee;
  border-radius: 12px;
  font-size: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
}

.message-row.service {
  flex-direction: row-reverse;
}

.message-avatar {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  color: #fff;
  background: #7f909b;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
}

.message-row.service .message-avatar {
  background: #34bda4;
}

.message-body {
  max-width: min(70%, 620px);
}

.message-row.service .message-body {
  text-align: right;
}

.message-meta {
  display: flex;
  gap: 12px;
  margin: 0 2px 6px;
  color: #8f989f;
  font-size: 12px;
}

.message-row.service .message-meta {
  flex-direction: row-reverse;
}

.message-meta time {
  color: #b0b7bc;
}

.message-bubble {
  padding: 11px 14px;
  color: #364047;
  background: #fff;
  border: 1px solid #e3e8eb;
  border-radius: 3px 13px 13px 13px;
  line-height: 1.65;
  text-align: left;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  box-shadow: 0 3px 10px rgb(47 64 72 / 5%);
}

.message-row.service .message-bubble {
  background: #dff5ef;
  border-color: #ccebe3;
  border-radius: 13px 3px 13px 13px;
}

.composer {
  padding: 14px 18px;
  background: #fff;
  border-top: 1px solid #e3e7ea;
}

.composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.composer-actions > span {
  max-width: 60%;
  overflow: hidden;
  color: #90989f;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-pane {
  min-width: 0;
  background: #fff;
  border-left: 1px solid #dde3e8;
}

.conversation-header {
  height: 66px;
}

.conversation-list {
  height: calc(100% - 66px);
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 11px;
  width: 100%;
  min-height: 76px;
  padding: 13px 16px;
  color: inherit;
  text-align: left;
  background: #fff;
  border: 0;
  border-bottom: 1px solid #edf0f2;
  cursor: pointer;
}

.conversation-item:hover {
  background: #f6faf9;
}

.conversation-item.active {
  background: #eaf7f4;
  box-shadow: inset 3px 0 #2eb69f;
}

.list-avatar {
  width: 40px;
  height: 40px;
  color: #75858e;
  background: #edf2f3;
}

.conversation-item.active .list-avatar {
  color: #fff;
  background: #32bca3;
}

.conversation-info {
  min-width: 0;
  flex: 1;
}

.conversation-name,
.conversation-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.conversation-name strong {
  min-width: 0;
  overflow: hidden;
  color: #354047;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-name time {
  flex: 0 0 auto;
  color: #a2aab0;
  font-size: 11px;
}

.conversation-preview {
  margin-top: 7px;
}

.conversation-preview span {
  min-width: 0;
  overflow: hidden;
  color: #89939a;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-preview em {
  display: grid;
  place-items: center;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  color: #fff;
  background: #f56c6c;
  border-radius: 10px;
  font-size: 11px;
  font-style: normal;
}

.center-empty,
.list-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #a1a9af;
  text-align: center;
}

.center-empty svg,
.list-empty svg {
  width: 42px;
  margin-bottom: 12px;
  color: #c5ccd0;
}

.center-empty strong {
  margin-bottom: 8px;
  color: #7d878d;
}

.center-empty.compact {
  height: 140px;
}

.list-empty {
  height: 240px;
  font-size: 13px;
}
</style>

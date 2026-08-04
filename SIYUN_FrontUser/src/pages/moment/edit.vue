<template>
  <view class="edit-page">
    <view class="nav-bar">
      <button class="nav-back" hover-class="none" @tap="goBack">返回</button>
      <text class="nav-title">发布微圈</text>
    </view>

    <view class="content form">
      <view class="card form-card">
        <input
          v-model.trim="form.title"
          class="title-input"
          maxlength="80"
          placeholder="标题"
          placeholder-class="placeholder"
        />
        <input
          v-model.trim="form.keywords"
          class="line-input"
          maxlength="100"
          placeholder="关键词（选填）"
          placeholder-class="placeholder"
        />

        <view class="field-label">封面图片</view>
        <view v-if="coverPreview" class="cover-preview-wrap">
          <image class="cover-preview" :src="coverPreview" mode="aspectFill" />
          <view class="cover-actions">
            <button class="cover-action" :disabled="uploading" @tap="chooseCover">更换封面</button>
            <button class="cover-action danger" :disabled="uploading" @tap="removeCover">移除</button>
          </view>
        </view>
        <button v-else class="cover-uploader" :disabled="uploading" @tap="chooseCover">
          <text class="upload-mark">＋</text>
          <text>{{ uploading ? '正在上传…' : '选择封面图片' }}</text>
        </button>

        <view class="field-label editor-label">正文内容</view>
        <view class="editor-toolbar">
          <button class="toolbar-button strong" @tap="formatText('bold')">B</button>
          <button class="toolbar-button italic" @tap="formatText('italic')">I</button>
          <button class="toolbar-button underline" @tap="formatText('underline')">U</button>
          <button class="toolbar-button" @tap="formatText('header', 'H2')">标题</button>
          <button class="toolbar-button" @tap="formatText('list', 'ordered')">编号</button>
          <button class="toolbar-button image-button" :disabled="uploading" @tap="insertImage">
            插入图片
          </button>
          <button class="toolbar-button clear-button" @tap="clearFormat">清除格式</button>
        </view>
        <editor
          id="momentEditor"
          class="rich-editor"
          placeholder="写下内容，可在文字之间插入图片…"
          show-img-size
          show-img-toolbar
          show-img-resize
          @ready="onEditorReady"
          @input="onEditorInput"
        />
      </view>
      <button class="primary-button submit" :disabled="submitting || uploading" @tap="submit">
        {{ submitting ? '正在发布…' : '确认发布' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addMoment, uploadMomentImage } from '@/api/moment'
import { assetUrl } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const uploading = ref(false)
const submitting = ref(false)
const form = reactive({
  title: '',
  keywords: '',
  coverUrl: '',
  content: '',
})
const coverPreview = computed(() => assetUrl(form.coverUrl))
let editorContext = null

onLoad(() => {
  userStore.hydrate()
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/auth/login' })
  }
})

function onEditorReady() {
  uni.createSelectorQuery()
    .select('#momentEditor')
    .context((result) => {
      editorContext = result?.context || null
    })
    .exec()
}

function onEditorInput(event) {
  form.content = event.detail?.html || ''
}

function formatText(name, value) {
  editorContext?.format(name, value)
}

function clearFormat() {
  editorContext?.removeFormat()
}

function chooseImageFile(callback) {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: (result) => {
      const filePath = result.tempFilePaths?.[0]
      if (filePath) callback(filePath)
    },
  })
}

function chooseCover() {
  chooseImageFile(async (filePath) => {
    const imageUrl = await uploadImage(filePath)
    if (imageUrl) form.coverUrl = imageUrl
  })
}

function insertImage() {
  if (!editorContext) {
    uni.showToast({ title: '编辑器尚未准备好', icon: 'none' })
    return
  }
  chooseImageFile(async (filePath) => {
    const imageUrl = await uploadImage(filePath)
    if (!imageUrl) return
    editorContext.insertImage({
      src: imageUrl,
      alt: '微圈正文图片',
      width: '100%',
      success: () => uni.showToast({ title: '图片已插入正文', icon: 'success' }),
    })
  })
}

async function uploadImage(filePath) {
  if (uploading.value) return ''
  uploading.value = true
  try {
    const response = await uploadMomentImage(filePath)
    const imageUrl = pickResult(response, 'imageUrl', '')
    if (!imageUrl) throw new Error('图片地址返回为空')
    return imageUrl
  } catch (error) {
    uni.showToast({ title: error.message || '图片上传失败', icon: 'none' })
    return ''
  } finally {
    uploading.value = false
  }
}

function removeCover() {
  form.coverUrl = ''
}

function goBack() {
  uni.navigateBack({
    fail: returnToMoment,
  })
}

function returnToMoment() {
  // #ifdef H5
  const { origin, pathname, search } = window.location
  window.location.replace(`${origin}${pathname}${search}#/pages/moment/index`)
  return
  // #endif

  uni.redirectTo({ url: '/pages/moment/index' })
}

function plainContent() {
  return String(form.content || '')
    .replace(/<img\b[^>]*>/gi, '[图片]')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim()
}

function validate() {
  if (!form.title) return '请输入标题'
  if (!form.coverUrl) return '请上传封面图片'
  if (!plainContent()) return '请输入正文内容'
  return ''
}

async function submit() {
  const message = validate()
  if (message) {
    uni.showToast({ title: message, icon: 'none' })
    return
  }
  if (submitting.value || uploading.value) return
  submitting.value = true
  try {
    await addMoment({
      ...form,
      countView: 0,
      countLike: 0,
      countComment: 0,
      countCollect: 0,
      sortNum: 0,
      statusShow: 1,
      status: 3,
    })
    uni.showToast({ title: '已发布', icon: 'success' })
    setTimeout(returnToMoment, 500)
  } catch (error) {
    uni.showToast({ title: error.message || '发布失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: #f4f8f8;
}

.form-card {
  padding: 26rpx;
}

.title-input,
.line-input {
  width: 100%;
  border-bottom: 1rpx solid #e2e8ea;
  color: #263238;
}

.title-input {
  height: 82rpx;
  font-size: 34rpx;
  font-weight: 800;
}

.line-input {
  height: 78rpx;
  font-size: 27rpx;
}

.field-label {
  display: block;
  margin: 26rpx 0 14rpx;
  color: #435158;
  font-size: 25rpx;
  font-weight: 800;
}

.cover-preview-wrap {
  overflow: hidden;
  border-radius: 16rpx;
  background: #eef5f5;
}

.cover-preview {
  display: block;
  width: 100%;
  height: 320rpx;
}

.cover-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12rpx;
  padding: 14rpx;
}

.cover-action {
  height: 54rpx;
  padding: 0 20rpx;
  border-radius: 27rpx;
  background: #e4f7f3;
  color: #18a991;
  font-size: 22rpx;
}

.cover-action.danger {
  background: #fff0ec;
  color: #ed6f58;
}

.cover-uploader {
  width: 100%;
  height: 210rpx;
  border: 2rpx dashed #9edacd;
  border-radius: 16rpx;
  background: #f2faf8;
  color: #18a991;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  font-size: 24rpx;
}

.upload-mark {
  font-size: 52rpx;
  font-weight: 300;
}

.editor-label {
  margin-bottom: 12rpx;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  padding: 14rpx;
  border: 1rpx solid #dfe9ea;
  border-bottom: 0;
  border-radius: 14rpx 14rpx 0 0;
  background: #f5f9f9;
}

.toolbar-button {
  min-width: 54rpx;
  height: 50rpx;
  padding: 0 14rpx;
  border-radius: 9rpx;
  background: #fff;
  color: #526168;
  font-size: 21rpx;
  box-shadow: inset 0 0 0 1rpx #e1e9ea;
}

.toolbar-button.strong {
  font-weight: 900;
}

.toolbar-button.italic {
  font-style: italic;
}

.toolbar-button.underline {
  text-decoration: underline;
}

.toolbar-button.image-button {
  color: #18a991;
  font-weight: 800;
}

.toolbar-button.clear-button {
  color: #87959a;
}

.rich-editor {
  width: 100%;
  min-height: 460rpx;
  padding: 22rpx;
  border: 1rpx solid #dfe9ea;
  border-radius: 0 0 14rpx 14rpx;
  background: #fff;
  color: #35434a;
  font-size: 28rpx;
  line-height: 46rpx;
}

.placeholder {
  color: #aab5ba;
}

.submit {
  margin-top: 34rpx;
}

.submit[disabled],
.toolbar-button[disabled],
.cover-action[disabled],
.cover-uploader[disabled] {
  opacity: 0.62;
}
</style>

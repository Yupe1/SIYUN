<template>
  <view class="editor-page">
    <SubPageNav :title="isEdit ? '编辑视频课程' : '上传视频课程'" />

    <view v-if="loading" class="loading-state">正在加载课程信息…</view>
    <template v-else>
      <view class="step-bar">
        <view class="step-item" :class="{ active: currentStep === 1, done: currentStep > 1 }">
          <text class="step-no">{{ currentStep > 1 ? '✓' : '1' }}</text>
          <text>课程资料</text>
        </view>
        <view class="step-line" :class="{ active: currentStep > 1 }"></view>
        <view class="step-item" :class="{ active: currentStep === 2 }">
          <text class="step-no">2</text>
          <text>上传分集</text>
        </view>
      </view>

      <view v-if="currentStep === 1" class="content step-content">
        <view class="page-intro">
          <text class="intro-title">先填写课程资料</text>
          <text class="intro-desc">下一步再逐集上传视频，课程和微圈互不混用。</text>
        </view>

        <view class="form-card card">
          <button
            class="cover-picker"
            data-testid="course-cover-picker"
            :disabled="coverUploading || submitting"
            @tap="chooseCover"
          >
            <image v-if="coverPreview" class="cover-preview" :src="coverPreview" mode="aspectFill" />
            <view v-else class="cover-empty">
              <text class="cover-add">＋</text>
              <view>
                <text class="cover-title">上传课程封面</text>
                <text class="cover-help">建议 16:9，最大 5MB</text>
              </view>
            </view>
            <view v-if="coverUploading" class="upload-mask">上传中 {{ coverProgress }}%</view>
            <text v-else-if="form.coverUrl" class="replace-cover">更换</text>
          </button>

          <view class="form-row">
            <text class="row-label">课程名称</text>
            <input
              v-model.trim="form.title"
              class="row-input"
              maxlength="80"
              placeholder="例如：Java 快速入门"
            />
          </view>

          <view class="form-row">
            <text class="row-label">课程分类</text>
            <picker class="row-control" :range="categories" range-key="cateName" @change="selectCategory">
              <view class="picker-value" :class="{ placeholder: !selectedCategoryName }">
                <text>{{ selectedCategoryName || '请选择' }}</text>
                <text class="arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="form-row">
            <text class="row-label">课程价格</text>
            <view class="price-control">
              <text class="currency">¥</text>
              <input v-model="form.priceOriginal" type="digit" maxlength="10" placeholder="0.00" />
              <text class="unit">元</text>
            </view>
          </view>

          <view class="textarea-row">
            <view class="textarea-head">
              <text class="row-label">课程简介</text>
              <text class="optional">必填</text>
            </view>
            <textarea
              v-model.trim="form.intro"
              class="compact-textarea"
              maxlength="300"
              placeholder="用一两句话说明这门课能学到什么"
            />
          </view>

          <view class="textarea-row last-row">
            <view class="textarea-head">
              <text class="row-label">详细介绍</text>
              <text class="optional">选填</text>
            </view>
            <textarea
              v-model.trim="form.detailDesc"
              class="detail-textarea"
              maxlength="3000"
              placeholder="适合人群、课程目标、学习建议等"
            />
          </view>
        </view>

        <view class="income-note">
          <text class="note-icon">¥</text>
          <text>课程成交后，按照上面填写的课程原价计入创作者钱包。</text>
        </view>

        <button class="primary-button next-button" data-testid="course-next" @tap="goEpisodes">
          下一步：上传分集
        </button>
      </view>

      <view v-else class="content episode-content">
        <view class="episode-header">
          <view>
            <text class="intro-title">上传课程分集</text>
            <text class="intro-desc">每一集都需要名称、视频和时长。</text>
          </view>
          <view class="episode-total">
            <text>{{ episodes.length }}</text>
            <text class="total-unit">集</text>
          </view>
        </view>

        <view
          v-for="(episode, index) in episodes"
          :key="episode.localId"
          class="episode-card card"
          :data-testid="`episode-card-${index + 1}`"
        >
          <view class="episode-card-head">
            <view class="episode-index">
              <text class="episode-dot"></text>
              <text>第 {{ index + 1 }} 集</text>
            </view>
            <button
              v-if="episodes.length > 1"
              class="remove-button"
              :disabled="submitting"
              @tap="removeEpisode(index)"
            >
              删除
            </button>
          </view>

          <view class="episode-field">
            <text class="episode-label">本集名称</text>
            <input
              v-model.trim="episode.epName"
              class="episode-input"
              maxlength="80"
              :placeholder="`例如：${index + 1}. 课程介绍`"
            />
          </view>

          <button
            class="video-upload"
            :class="{ uploaded: episode.videoUrl }"
            :data-testid="`episode-upload-${index + 1}`"
            :disabled="episode.uploading || submitting"
            @tap="chooseEpisodeVideo(index)"
          >
            <view class="video-status-icon">{{ episode.videoUrl ? '✓' : '↑' }}</view>
            <view class="video-copy">
              <text class="video-title">
                {{ episode.uploading ? `正在上传 ${episode.progress}%` : episode.videoUrl ? '视频已上传' : '选择视频文件' }}
              </text>
              <text class="video-file">
                {{ episode.fileName || 'MP4、MOV、MKV 等格式，最大 500MB' }}
              </text>
            </view>
            <text class="video-action">{{ episode.videoUrl ? '更换' : '选择' }}</text>
            <view v-if="episode.uploading" class="progress-track">
              <view class="progress-bar" :style="{ width: `${episode.progress}%` }"></view>
            </view>
          </button>

          <view class="duration-field">
            <text class="episode-label">本集时长</text>
            <view class="duration-control">
              <input v-model="episode.duration" type="number" maxlength="5" placeholder="0" />
              <text>分钟</text>
            </view>
          </view>
        </view>

        <button
          class="add-episode-button"
          data-testid="add-episode"
          :disabled="busy"
          @tap="addEpisode"
        >
          <text class="add-icon">＋</text>
          <view>
            <text class="add-title">继续添加一集</text>
            <text class="add-desc">新分集会追加到课程末尾</text>
          </view>
        </button>

        <view class="summary-card">
          <view>
            <text class="summary-label">课程合计</text>
            <text class="summary-value">{{ episodes.length }} 集 · {{ totalDuration }} 分钟</text>
          </view>
          <text class="summary-state">{{ uploadedEpisodeCount }}/{{ episodes.length }} 已上传</text>
        </view>

        <view class="bottom-actions">
          <button class="back-button" :disabled="submitting" @tap="currentStep = 1">上一步</button>
          <button class="primary-button submit-button" :disabled="busy" @tap="submit">
            {{ submitting ? '正在提交…' : isEdit ? '保存并重新审核' : '提交课程审核' }}
          </button>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import SubPageNav from '@/components/SubPageNav.vue'
import {
  createCreatorCourse,
  getCreatorCourse,
  getCreatorCourseCategories,
  updateCreatorCourse,
  uploadCreatorCourseCover,
  uploadCreatorCourseVideo,
} from '@/api/creator'
import { assetUrl } from '@/utils/format'
import { pickResult } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(true)
const submitting = ref(false)
const coverUploading = ref(false)
const coverProgress = ref(0)
const currentStep = ref(1)
const courseId = ref(0)
const categories = ref([])
let episodeSeed = 1
const episodes = ref([newEpisode()])
const form = reactive({
  cateId: null,
  title: '',
  intro: '',
  priceOriginal: '',
  coverUrl: '',
  detailDesc: '',
})

const isEdit = computed(() => courseId.value > 0)
const busy = computed(() =>
  submitting.value
  || coverUploading.value
  || episodes.value.some((item) => item.uploading),
)
const coverPreview = computed(() => assetUrl(form.coverUrl))
const selectedCategoryName = computed(() =>
  categories.value.find((item) => Number(item.id) === Number(form.cateId))?.cateName || '',
)
const totalDuration = computed(() =>
  episodes.value.reduce((total, item) => total + (Number(item.duration) || 0), 0),
)
const uploadedEpisodeCount = computed(() =>
  episodes.value.filter((item) => Boolean(item.videoUrl)).length,
)

onLoad(async (query = {}) => {
  courseId.value = Number(query.id || 0)
  userStore.hydrate()
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/auth/login' })
    return
  }
  try {
    await userStore.refresh()
    if (!isVerifiedCreator()) {
      uni.showToast({ title: '请先完成创作者认证', icon: 'none' })
      setTimeout(() => uni.redirectTo({ url: '/pages/moment/creator-apply' }), 500)
      return
    }
    const [categoryResponse, detailResponse] = await Promise.all([
      getCreatorCourseCategories(),
      isEdit.value ? getCreatorCourse(courseId.value) : Promise.resolve(null),
    ])
    categories.value = pickResult(categoryResponse, 'categories', [])
    if (categories.value.length === 1 && !form.cateId) {
      form.cateId = categories.value[0].id
    }
    if (detailResponse) {
      fillCourse(detailResponse)
    }
  } catch (error) {
    uni.showToast({ title: error.message || '课程信息加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
})

function isVerifiedCreator() {
  return Boolean(userStore.user?.chinaId)
    && Number(userStore.user?.createrVerified || 0) === 1
}

function newEpisode(data = {}) {
  episodeSeed += 1
  return {
    localId: `episode-${episodeSeed}`,
    epName: data.epName || '',
    videoUrl: data.videoUrl || '',
    fileName: fileName(data.videoUrl),
    duration: data.duration ?? '',
    uploading: false,
    progress: 0,
  }
}

function fillCourse(response) {
  const course = pickResult(response, 'course', {})
  Object.assign(form, {
    cateId: course.cateId ?? null,
    title: course.title || '',
    intro: course.intro || '',
    priceOriginal: course.priceOriginal ?? '',
    coverUrl: course.coverUrl || '',
    detailDesc: course.detailDesc || '',
  })
  const contents = pickResult(response, 'contents', [])
  episodes.value = contents.length
    ? contents.map((item) => newEpisode(item))
    : [newEpisode({ videoUrl: course.videoUrl, duration: course.duration })]
}

function fileName(url) {
  const value = String(url || '').split('?')[0]
  const name = value.split('/').pop() || ''
  try {
    return decodeURIComponent(name)
  } catch (error) {
    return name
  }
}

function selectCategory(event) {
  const index = Number(event.detail.value)
  form.cateId = categories.value[index]?.id ?? null
}

function chooseCover() {
  uni.chooseImage({
    count: 1,
    sizeType: ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: async (result) => {
      const file = result.tempFiles?.[0]
      if (Number(file?.size || 0) > 5 * 1024 * 1024) {
        uni.showToast({ title: '封面不能超过5MB', icon: 'none' })
        return
      }
      coverUploading.value = true
      coverProgress.value = 0
      try {
        const response = await uploadCreatorCourseCover(
          result.tempFilePaths[0],
          (value) => { coverProgress.value = value },
        )
        form.coverUrl = pickResult(response, 'coverUrl', '')
        if (!form.coverUrl) {
          throw new Error('封面地址返回为空')
        }
      } catch (error) {
        uni.showToast({ title: error.message || '封面上传失败', icon: 'none' })
      } finally {
        coverUploading.value = false
      }
    },
  })
}

function validateBasic() {
  if (!form.coverUrl) return '请上传课程封面'
  if (!form.title) return '请输入课程名称'
  if (!form.cateId) return '请选择课程分类'
  if (form.priceOriginal === '' || !Number.isFinite(Number(form.priceOriginal)) || Number(form.priceOriginal) < 0) {
    return '请输入正确的课程价格'
  }
  if (!form.intro) return '请输入课程简介'
  return ''
}

function goEpisodes() {
  const message = validateBasic()
  if (message) {
    uni.showToast({ title: message, icon: 'none' })
    return
  }
  currentStep.value = 2
}

function addEpisode() {
  episodes.value = [...episodes.value, newEpisode()]
}

function removeEpisode(index) {
  if (episodes.value.length === 1) {
    uni.showToast({ title: '至少保留一集', icon: 'none' })
    return
  }
  uni.showModal({
    title: '删除分集',
    content: `确认删除第 ${index + 1} 集吗？保存后原视频文件会一并清理。`,
    success: ({ confirm }) => {
      if (confirm) {
        episodes.value = episodes.value.filter((_, itemIndex) => itemIndex !== index)
      }
    },
  })
}

function chooseEpisodeVideo(index) {
  uni.chooseVideo({
    sourceType: ['album', 'camera'],
    compressed: false,
    maxDuration: 3600,
    success: async (result) => {
      const episode = episodes.value[index]
      if (!episode) return
      if (Number(result.size || 0) > 500 * 1024 * 1024) {
        uni.showToast({ title: '视频不能超过500MB', icon: 'none' })
        return
      }
      episode.uploading = true
      episode.progress = 0
      try {
        const response = await uploadCreatorCourseVideo(
          result.tempFilePath,
          (value) => { episode.progress = value },
        )
        episode.videoUrl = pickResult(response, 'videoUrl', '')
        if (!episode.videoUrl) {
          throw new Error('视频地址返回为空')
        }
        episode.fileName = fileName(result.name || result.tempFilePath)
        if (!episode.epName) {
          episode.epName = episode.fileName.replace(/\.[^.]+$/, '') || `第 ${index + 1} 集`
        }
        if (!Number(episode.duration) && Number(result.duration) > 0) {
          episode.duration = Math.max(1, Math.ceil(Number(result.duration) / 60))
        }
      } catch (error) {
        uni.showToast({ title: error.message || '视频上传失败', icon: 'none' })
      } finally {
        episode.uploading = false
      }
    },
  })
}

function validateEpisodes() {
  for (let index = 0; index < episodes.value.length; index++) {
    const episode = episodes.value[index]
    if (!episode.epName) return `请输入第 ${index + 1} 集名称`
    if (!episode.videoUrl) return `请上传第 ${index + 1} 集视频`
    if (episode.duration === '' || Number(episode.duration) < 0) return `请输入第 ${index + 1} 集时长`
  }
  return ''
}

async function submit() {
  if (busy.value) return
  const basicMessage = validateBasic()
  const episodeMessage = validateEpisodes()
  if (basicMessage || episodeMessage) {
    uni.showToast({ title: basicMessage || episodeMessage, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const normalizedEpisodes = episodes.value.map((item) => ({
      epName: item.epName.trim(),
      videoUrl: item.videoUrl.trim(),
      duration: Number(item.duration) || 0,
    }))
    const payload = {
      course: {
        cateId: form.cateId,
        title: form.title.trim(),
        intro: form.intro.trim(),
        priceOriginal: Number(form.priceOriginal),
        keywords: form.title.trim(),
        coverUrl: form.coverUrl.trim(),
        videoUrl: normalizedEpisodes[0].videoUrl,
        detailDesc: form.detailDesc.trim(),
        episodeNum: normalizedEpisodes.length,
        duration: totalDuration.value,
      },
      contents: normalizedEpisodes,
    }
    if (isEdit.value) {
      await updateCreatorCourse(courseId.value, payload)
    } else {
      await createCreatorCourse(payload)
    }
    uni.showToast({
      title: isEdit.value ? '已重新提交审核' : '课程已提交审核',
      icon: 'success',
    })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/mine/creations?tab=courses' })
    }, 600)
  } catch (error) {
    uni.showToast({ title: error.message || '课程提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.editor-page { min-height: 100vh; padding-bottom: 46rpx; background: #f4f8f8; }
.loading-state { min-height: 600rpx; display: flex; align-items: center; justify-content: center; color: #8b999e; font-size: 25rpx; }
.step-bar { height: 98rpx; padding: 0 88rpx; background: #fff; display: flex; align-items: center; justify-content: center; }
.step-item { flex: 0 0 auto; display: flex; align-items: center; gap: 10rpx; color: #9aa6a9; font-size: 23rpx; }
.step-item.active, .step-item.done { color: #16aa92; font-weight: 900; }
.step-no { width: 42rpx; height: 42rpx; border-radius: 21rpx; background: #e7edef; display: flex; align-items: center; justify-content: center; font-size: 21rpx; }
.step-item.active .step-no, .step-item.done .step-no { background: #20bea3; color: #fff; }
.step-line { width: 72rpx; height: 3rpx; margin: 0 18rpx; background: #dfe7e8; }
.step-line.active { background: #64cdbb; }
.step-content, .episode-content { padding-top: 22rpx; }
.page-intro { padding: 2rpx 4rpx 20rpx; }
.intro-title, .intro-desc { display: block; }
.intro-title { color: #26363c; font-size: 32rpx; line-height: 42rpx; font-weight: 900; }
.intro-desc { margin-top: 7rpx; color: #829095; font-size: 22rpx; line-height: 34rpx; }
.form-card { padding: 22rpx 24rpx 4rpx; overflow: hidden; }
.cover-picker { width: 100%; height: 216rpx; margin-bottom: 8rpx; border-radius: 16rpx; overflow: hidden; background: #eaf5f3; position: relative; }
.cover-preview { width: 100%; height: 100%; display: block; }
.cover-empty { height: 100%; padding: 0 38rpx; display: flex; align-items: center; justify-content: center; gap: 22rpx; color: #18aa93; text-align: left; }
.cover-add { width: 68rpx; height: 68rpx; border-radius: 34rpx; background: #20bea3; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 42rpx; }
.cover-title, .cover-help { display: block; }
.cover-title { font-size: 27rpx; font-weight: 900; }
.cover-help { margin-top: 8rpx; color: #8a9b9e; font-size: 21rpx; }
.replace-cover { position: absolute; right: 14rpx; bottom: 14rpx; height: 48rpx; padding: 0 18rpx; border-radius: 24rpx; background: rgba(30, 43, 48, .75); color: #fff; display: flex; align-items: center; font-size: 21rpx; }
.upload-mask { position: absolute; inset: 0; background: rgba(29, 43, 48, .62); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 800; }
.form-row { min-height: 92rpx; border-bottom: 1rpx solid #edf1f2; display: flex; align-items: center; gap: 20rpx; }
.row-label { flex: 0 0 auto; width: 130rpx; color: #425258; font-size: 24rpx; font-weight: 800; }
.row-input, .row-control, .price-control { flex: 1; min-width: 0; }
.row-input { height: 82rpx; color: #27373d; font-size: 26rpx; text-align: right; }
.picker-value { height: 82rpx; display: flex; align-items: center; justify-content: flex-end; gap: 12rpx; color: #27373d; font-size: 26rpx; }
.picker-value.placeholder { color: #a0abad; }
.arrow { color: #18aa93; font-size: 38rpx; }
.price-control { height: 82rpx; display: flex; align-items: center; justify-content: flex-end; }
.price-control input { width: 160rpx; height: 70rpx; color: #ed6a50; font-size: 28rpx; text-align: right; font-weight: 900; }
.currency { color: #ed6a50; font-size: 25rpx; font-weight: 900; }
.unit { margin-left: 7rpx; color: #859398; font-size: 22rpx; }
.textarea-row { padding: 23rpx 0; border-bottom: 1rpx solid #edf1f2; }
.textarea-row.last-row { border-bottom: 0; }
.textarea-head { display: flex; align-items: center; justify-content: space-between; }
.optional { color: #9aa6aa; font-size: 20rpx; }
.compact-textarea, .detail-textarea { width: 100%; margin-top: 15rpx; padding: 16rpx; border-radius: 12rpx; background: #f7fafa; color: #34444a; font-size: 25rpx; line-height: 38rpx; }
.compact-textarea { height: 126rpx; }
.detail-textarea { height: 168rpx; }
.income-note { margin-top: 18rpx; padding: 20rpx 22rpx; border-radius: 14rpx; background: #fff7e7; color: #8c7040; display: flex; align-items: flex-start; gap: 13rpx; font-size: 21rpx; line-height: 33rpx; }
.note-icon { flex: 0 0 auto; width: 34rpx; height: 34rpx; border-radius: 17rpx; background: #f4b954; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 19rpx; font-weight: 900; }
.next-button { margin-top: 24rpx; }
.episode-header { padding: 2rpx 4rpx 20rpx; display: flex; align-items: center; justify-content: space-between; }
.episode-total { width: 72rpx; height: 72rpx; border-radius: 36rpx; background: #ddf7f1; color: #18aa92; display: flex; align-items: baseline; justify-content: center; padding-top: 13rpx; font-size: 31rpx; font-weight: 900; }
.total-unit { margin-left: 3rpx; font-size: 18rpx; }
.episode-card { margin-bottom: 18rpx; padding: 22rpx 24rpx; overflow: hidden; }
.episode-card-head { display: flex; align-items: center; justify-content: space-between; }
.episode-index { display: flex; align-items: center; gap: 10rpx; color: #25363b; font-size: 27rpx; font-weight: 900; }
.episode-dot { width: 15rpx; height: 15rpx; border-radius: 8rpx; background: #20bea3; }
.remove-button { height: 46rpx; padding: 0 12rpx; color: #e66a55; font-size: 21rpx; }
.episode-field { margin-top: 18rpx; }
.episode-label { color: #65747a; font-size: 22rpx; }
.episode-input { width: 100%; height: 72rpx; margin-top: 7rpx; border-bottom: 1rpx solid #e7eded; color: #29393f; font-size: 26rpx; }
.video-upload { width: 100%; min-height: 112rpx; margin-top: 18rpx; padding: 18rpx; border-radius: 14rpx; border: 2rpx dashed #a7dacf; background: #f2faf8; display: flex; align-items: center; gap: 14rpx; text-align: left; position: relative; overflow: hidden; }
.video-upload.uploaded { border-style: solid; border-color: #cbe9e3; background: #edf9f6; }
.video-status-icon { flex: 0 0 auto; width: 54rpx; height: 54rpx; border-radius: 27rpx; background: #20bea3; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 26rpx; font-weight: 900; }
.video-copy { flex: 1; min-width: 0; }
.video-title, .video-file { display: block; }
.video-title { color: #34454a; font-size: 24rpx; font-weight: 900; }
.video-file { margin-top: 7rpx; color: #88979b; font-size: 20rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.video-action { flex: 0 0 auto; color: #16a991; font-size: 22rpx; font-weight: 800; }
.progress-track { position: absolute; left: 0; right: 0; bottom: 0; height: 8rpx; background: #dceae7; }
.progress-bar { height: 100%; background: #20bea3; }
.duration-field { min-height: 76rpx; margin-top: 10rpx; display: flex; align-items: center; justify-content: space-between; }
.duration-control { display: flex; align-items: center; gap: 8rpx; color: #7b8a8f; font-size: 21rpx; }
.duration-control input { width: 112rpx; height: 56rpx; border-radius: 10rpx; background: #f3f7f7; color: #29393e; text-align: center; font-size: 24rpx; }
.add-episode-button { width: 100%; min-height: 106rpx; padding: 18rpx 26rpx; border: 2rpx dashed #85d2c4; border-radius: 16rpx; color: #18aa92; background: rgba(230, 248, 244, .55); display: flex; align-items: center; justify-content: center; gap: 16rpx; text-align: left; }
.add-icon { font-size: 40rpx; font-weight: 500; }
.add-title, .add-desc { display: block; }
.add-title { font-size: 25rpx; font-weight: 900; }
.add-desc { margin-top: 6rpx; color: #8a999d; font-size: 19rpx; font-weight: 400; }
.summary-card { margin-top: 18rpx; padding: 20rpx 24rpx; border-radius: 14rpx; background: #eaf3f2; display: flex; align-items: center; justify-content: space-between; }
.summary-label, .summary-value { display: block; }
.summary-label { color: #849297; font-size: 20rpx; }
.summary-value { margin-top: 6rpx; color: #35454a; font-size: 25rpx; font-weight: 900; }
.summary-state { color: #16a990; font-size: 22rpx; font-weight: 800; }
.bottom-actions { margin-top: 22rpx; display: flex; gap: 16rpx; }
.back-button { flex: 0 0 176rpx; height: 82rpx; border-radius: 41rpx; background: #fff; color: #64747a; font-size: 26rpx; box-shadow: 0 8rpx 22rpx rgba(26, 52, 58, .05); }
.submit-button { flex: 1; height: 82rpx; font-size: 28rpx; }
.submit-button[disabled], .add-episode-button[disabled] { opacity: .56; }
</style>

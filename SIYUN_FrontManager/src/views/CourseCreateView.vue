<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, UploadFilled } from '@element-plus/icons-vue'
import http from '@/api/http'

const router = useRouter()
const route = useRoute()
const courseId = computed(() => Number(route.params.id || 0))
const isEdit = computed(() => courseId.value > 0)
const loading = ref(false)
const submitting = ref(false)
const teachers = ref([])
const categories = ref([])
const imageAccept = '.jpg,.jpeg,.png,.gif,.webp'
const videoAccept = '.mp4,.mov,.mkv,.avi,.flv,.wmv,.webm'
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '')

const course = reactive({
  teacherId: null,
  frontCreatorId: null,
  cateId: null,
  title: '',
  intro: '',
  priceOriginal: '',
  keywords: '',
  coverUrl: '',
  coverFileName: '',
  detailDesc: '',
  duration: '',
})

const episodes = ref([newEpisode()])

const totalEpisodeDuration = computed(() =>
  episodes.value.reduce((total, item) => total + (Number(item.duration) || 0), 0),
)

const coverPreviewUrl = computed(() => publicFileUrl(course.coverUrl))
const isFrontCreatorCourse = computed(() => Boolean(course.frontCreatorId))

async function loadOptions() {
  loading.value = true
  try {
    const [teacherRes, cateRes, detailRes] = await Promise.all([
      http.get('/api/admin/course-teachers'),
      http.get('/api/admin/course-categories'),
      isEdit.value ? http.get(`/api/admin/courses/${courseId.value}`) : Promise.resolve(null),
    ])
    teachers.value = teacherRes.result?.teachers || []
    categories.value = cateRes.result?.categories || []
    if (!course.teacherId && teachers.value.length === 1) {
      course.teacherId = teachers.value[0].id
    }
    if (!course.cateId && categories.value.length === 1) {
      course.cateId = categories.value[0].id
    }
    if (detailRes) {
      fillEditData(detailRes.result || {})
    }
  } finally {
    loading.value = false
  }
}

function newEpisode(data = {}) {
  return {
    id: data.id || null,
    epName: data.epName || '',
    videoUrl: data.videoUrl || '',
    videoFileName: data.videoFileName || fileName(data.videoUrl),
    uploading: false,
    duration: data.duration ?? '',
  }
}

function fillEditData(result) {
  const source = result.course || {}
  Object.assign(course, {
    teacherId: source.teacherId ?? null,
    frontCreatorId: source.frontCreatorId ?? null,
    cateId: source.cateId ?? null,
    title: source.title || '',
    intro: source.intro || '',
    priceOriginal: source.priceOriginal ?? '',
    keywords: source.keywords || '',
    coverUrl: source.coverUrl || '',
    coverFileName: fileName(source.coverUrl),
    detailDesc: source.detailDesc || '',
    duration: source.duration ?? '',
  })
  const sourceEpisodes = result.contents || []
  episodes.value = sourceEpisodes.length
    ? sourceEpisodes.map((item) => newEpisode(item))
    : [newEpisode({ videoUrl: source.videoUrl, duration: source.duration })]
}

function fileName(url) {
  const cleanUrl = String(url || '').split('?')[0]
  const name = cleanUrl.split('/').pop() || ''
  try {
    return decodeURIComponent(name)
  } catch {
    return name
  }
}

function publicFileUrl(url) {
  const value = String(url || '').trim()
  if (!value || /^(?:https?:)?\/\//i.test(value) || /^(?:data|blob):/i.test(value)) return value
  return `${apiBaseUrl}${value.startsWith('/') ? '' : '/'}${value}`
}

function addEpisode() {
  episodes.value.push(newEpisode())
}

async function removeEpisode(index) {
  if (episodes.value.length === 1) {
    ElMessage.warning('至少保留一集')
    return
  }
  const episode = episodes.value[index]
  if (episode?.id) {
    await ElMessageBox.confirm(
      `确认删除第 ${index + 1} 集？保存课程后会同时删除原视频文件。`,
      '删除分集',
      { type: 'warning' },
    )
  }
  episodes.value.splice(index, 1)
}

function validate() {
  if (!course.teacherId && !isFrontCreatorCourse.value) return '请选择讲课教师'
  if (!course.cateId) return '请选择课程分类'
  if (!course.title.trim()) return '请填写课程名称'
  if (course.priceOriginal === '' || Number(course.priceOriginal) < 0) return '请填写正确的售价'
  if (!isEdit.value && !course.coverUrl.trim()) return '请上传课程图片'
  for (let i = 0; i < episodes.value.length; i++) {
    const item = episodes.value[i]
    if (!item.epName.trim()) return `请填写第 ${i + 1} 集名称`
    if (!item.videoUrl.trim()) return `请上传第 ${i + 1} 集视频文件`
    if (item.duration === '' || Number(item.duration) < 0) return `请填写第 ${i + 1} 集时长`
  }
  return ''
}

async function removeCourseCover() {
  await ElMessageBox.confirm(
    '确认移除当前课程图片？保存后旧图片文件会被删除，关联轮播图会自动停用。',
    '移除课程图片',
    { type: 'warning' },
  )
  course.coverUrl = ''
  course.coverFileName = ''
}

async function removeEpisodeVideo(index) {
  const episode = episodes.value[index]
  if (!episode?.videoUrl) return
  await ElMessageBox.confirm(
    `确认移除第 ${index + 1} 集的视频？提交前需要重新上传视频，或直接删除该分集。`,
    '移除视频文件',
    { type: 'warning' },
  )
  episode.videoUrl = ''
  episode.videoFileName = ''
}

async function uploadFile(options, url, resultKey, assign, successMessage, timeout = 60000) {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await http.post(url, formData, {
      timeout,
    })
    const uploadedUrl = data.result?.[resultKey]
    if (!uploadedUrl) {
      throw new Error('上传接口未返回文件地址')
    }
    assign(uploadedUrl, file.name)
    ElMessage.success(successMessage)
    onSuccess?.(data)
  } catch (error) {
    if (error?.message === '上传接口未返回文件地址') {
      ElMessage.error(error.message)
    }
    onError?.(error)
  }
}

function uploadCourseCover(options) {
  return uploadFile(
    options,
    '/api/admin/upload/course-cover',
    'coverUrl',
    (url, fileName) => {
      course.coverUrl = url
      course.coverFileName = fileName
    },
    '课程图片上传成功',
  )
}

function uploadEpisodeVideo(index) {
  return async (options) => {
    const episode = episodes.value[index]
    if (!episode) return
    episode.uploading = true
    try {
      await uploadFile(
        options,
        '/api/admin/upload/course-video',
        'videoUrl',
        (url, fileName) => {
          episode.videoUrl = url
          episode.videoFileName = fileName
        },
        `第 ${index + 1} 集视频上传成功`,
        0,
      )
    } finally {
      episode.uploading = false
    }
  }
}

async function submit() {
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }
  submitting.value = true
  try {
    const normalizedEpisodes = episodes.value.map((item) => ({
      ...(item.id ? { id: item.id } : {}),
      epName: item.epName.trim(),
      videoUrl: item.videoUrl.trim(),
      duration: Number(item.duration),
    }))
    const payload = {
      course: {
        teacherId: course.teacherId,
        frontCreatorId: course.frontCreatorId,
        cateId: course.cateId,
        title: course.title.trim(),
        intro: course.intro.trim(),
        priceOriginal: Number(course.priceOriginal),
        keywords: course.keywords.trim() || course.title.trim(),
        coverUrl: course.coverUrl.trim(),
        videoUrl: normalizedEpisodes[0].videoUrl,
        detailDesc: course.detailDesc.trim(),
        duration: course.duration === '' ? totalEpisodeDuration.value : Number(course.duration),
        episodeNum: normalizedEpisodes.length,
        statusShelf: 0,
        statusAudit: 1,
      },
      contents: normalizedEpisodes,
    }
    if (isEdit.value) {
      await http.put(`/api/admin/courses/${courseId.value}/with-contents`, payload)
      ElMessage.success('课程已更新并重新提交审核')
    } else {
      await http.post('/api/admin/courses/with-contents', payload)
      ElMessage.success('课程已提交待审核')
    }
    router.push('/courses/list')
  } finally {
    submitting.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <section v-loading="loading" class="course-create-page">
    <el-form label-width="120px" class="course-form">
      <section class="panel form-panel">
        <header>{{ isEdit ? '编辑课程基础信息' : '课程基础信息' }}</header>
        <div class="form-grid">
          <el-form-item label="讲课教师">
            <el-input
              v-if="isFrontCreatorCourse"
              :model-value="`前台创作者 #${course.frontCreatorId}`"
              disabled
              class="full-control"
            />
            <el-select
              v-else
              v-model="course.teacherId"
              filterable
              :disabled="teachers.length === 1"
              placeholder="请选择讲课教师"
              class="full-control"
            >
              <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="`${teacher.name}（${teacher.tel || teacher.id}）`"
                :value="teacher.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="课程分类">
            <el-select v-model="course.cateId" filterable placeholder="请选择课程分类" class="full-control">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.cateName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="课程名称">
            <el-input v-model="course.title" placeholder="请输入课程名称" />
          </el-form-item>

          <el-form-item label="售价">
            <el-input v-model="course.priceOriginal" inputmode="decimal" placeholder="请输入售价">
              <template #append>元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="搜索关键字">
            <el-input v-model="course.keywords" placeholder="默认使用课程名称" />
          </el-form-item>

          <el-form-item label="课程总时长">
            <el-input v-model="course.duration" inputmode="numeric" :placeholder="`默认按分集合计 ${totalEpisodeDuration} min`">
              <template #append>min</template>
            </el-input>
          </el-form-item>

          <el-form-item label="课程图片">
            <div class="course-file-editor">
              <div class="upload-field">
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadCourseCover"
                  :accept="imageAccept"
                >
                  <el-button :icon="UploadFilled">{{ course.coverUrl ? '替换图片' : '选择图片' }}</el-button>
                </el-upload>
                <el-button v-if="course.coverUrl" type="danger" plain :icon="Delete" @click="removeCourseCover">
                  移除图片
                </el-button>
                <div v-if="course.coverUrl" class="upload-result">
                  当前文件：{{ course.coverFileName || '课程图片' }}
                </div>
                <div v-else class="upload-tip">{{ isEdit ? '可留空保存；原图片将在保存后删除' : '上传后由后端生成访问路径' }}</div>
              </div>
              <el-image
                v-if="coverPreviewUrl"
                class="course-cover-preview"
                :src="coverPreviewUrl"
                fit="cover"
                :preview-src-list="[coverPreviewUrl]"
              >
                <template #error>
                  <div class="course-cover-error">封面加载失败</div>
                </template>
              </el-image>
            </div>
          </el-form-item>

          <el-form-item label="课程简介">
            <el-input v-model="course.intro" placeholder="请输入课程简介" />
          </el-form-item>

          <el-form-item label="课程内容" class="wide-field">
            <el-input v-model="course.detailDesc" type="textarea" :rows="4" placeholder="请输入课程详情、主讲内容等" />
          </el-form-item>
        </div>
      </section>

      <section class="panel form-panel">
        <header>
          <span>课程分集（共 {{ episodes.length }} 集）</span>
          <el-button type="primary" :icon="Plus" @click="addEpisode">添加一集</el-button>
        </header>

        <div class="episode-list">
          <div v-for="(episode, index) in episodes" :key="index" class="episode-row">
            <div class="episode-no">第 {{ index + 1 }} 集</div>
            <el-form-item label="本集名称">
              <el-input v-model="episode.epName" placeholder="请输入这一集名称" />
            </el-form-item>
            <el-form-item label="视频文件">
              <div class="upload-field">
                <el-upload
                  :show-file-list="false"
                  :http-request="uploadEpisodeVideo(index)"
                  :accept="videoAccept"
                >
                  <el-button :icon="UploadFilled" :loading="episode.uploading">
                    {{ episode.videoUrl ? '替换视频' : '选择视频' }}
                  </el-button>
                </el-upload>
                <el-button
                  v-if="episode.videoUrl"
                  type="danger"
                  link
                  :icon="Delete"
                  @click="removeEpisodeVideo(index)"
                >
                  移除文件
                </el-button>
                <el-tooltip
                  v-if="episode.videoUrl"
                  :content="episode.videoFileName || `第 ${index + 1} 集视频`"
                  placement="top"
                >
                  <div class="upload-result">
                    当前文件：{{ episode.videoFileName || `第 ${index + 1} 集视频` }}
                  </div>
                </el-tooltip>
                <div v-else class="upload-tip">{{ isEdit ? '请重新上传，或删除整个分集' : '选择文件后立即上传' }}</div>
              </div>
            </el-form-item>
            <el-form-item label="本集时长">
              <el-input v-model="episode.duration" inputmode="numeric" placeholder="请输入时长">
                <template #append>min</template>
              </el-input>
            </el-form-item>
            <el-button class="episode-delete" :icon="Delete" @click="removeEpisode(index)">删除</el-button>
          </div>
        </div>
      </section>

      <div class="form-actions">
        <el-button @click="router.push('/courses/list')">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          {{ isEdit ? '保存并重新提交审核' : '提交待审核' }}
        </el-button>
      </div>
    </el-form>
  </section>
</template>

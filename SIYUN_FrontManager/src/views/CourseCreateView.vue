<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, Plus, UploadFilled } from '@element-plus/icons-vue'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const teachers = ref([])
const categories = ref([])
const imageAccept = '.jpg,.jpeg,.png,.gif,.webp'
const videoAccept = '.mp4,.mov,.mkv,.avi,.flv,.wmv,.webm'

const course = reactive({
  teacherId: null,
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

async function loadOptions() {
  loading.value = true
  try {
    const [teacherRes, cateRes] = await Promise.all([
      http.get('/api/admin/course-teachers'),
      http.get('/api/admin/course-categories'),
    ])
    teachers.value = teacherRes.result?.teachers || []
    categories.value = cateRes.result?.categories || []
    if (!course.teacherId && teachers.value.length === 1) {
      course.teacherId = teachers.value[0].id
    }
    if (!course.cateId && categories.value.length === 1) {
      course.cateId = categories.value[0].id
    }
  } finally {
    loading.value = false
  }
}

function newEpisode() {
  return {
    epName: '',
    videoUrl: '',
    videoFileName: '',
    uploading: false,
    duration: '',
  }
}

function addEpisode() {
  episodes.value.push(newEpisode())
}

function removeEpisode(index) {
  if (episodes.value.length === 1) {
    ElMessage.warning('至少保留一集')
    return
  }
  episodes.value.splice(index, 1)
}

function validate() {
  if (!course.teacherId) return '请选择讲课教师'
  if (!course.cateId) return '请选择课程分类'
  if (!course.title.trim()) return '请填写课程名称'
  if (course.priceOriginal === '' || Number(course.priceOriginal) < 0) return '请填写正确的售价'
  if (!course.coverUrl.trim()) return '请上传课程图片'
  for (let i = 0; i < episodes.value.length; i++) {
    const item = episodes.value[i]
    if (!item.epName.trim()) return `请填写第 ${i + 1} 集名称`
    if (!item.videoUrl.trim()) return `请上传第 ${i + 1} 集视频文件`
    if (item.duration === '' || Number(item.duration) < 0) return `请填写第 ${i + 1} 集时长`
  }
  return ''
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
      epName: item.epName.trim(),
      videoUrl: item.videoUrl.trim(),
      duration: Number(item.duration),
    }))
    const payload = {
      course: {
        teacherId: course.teacherId,
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
    await http.post('/api/admin/courses/with-contents', payload)
    ElMessage.success('课程已提交待审核')
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
        <header>课程基础信息</header>
        <div class="form-grid">
          <el-form-item label="讲课教师">
            <el-select v-model="course.teacherId" filterable placeholder="请选择讲课教师" class="full-control">
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
            <div class="upload-field">
              <el-upload
                :show-file-list="false"
                :http-request="uploadCourseCover"
                :accept="imageAccept"
              >
                <el-button :icon="UploadFilled">选择图片</el-button>
              </el-upload>
              <div v-if="course.coverUrl" class="upload-result">
                已上传：{{ course.coverFileName || '课程图片' }}
              </div>
              <div v-else class="upload-tip">上传后由后端生成访问路径</div>
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
                  <el-button :icon="UploadFilled" :loading="episode.uploading">选择视频</el-button>
                </el-upload>
                <div v-if="episode.videoUrl" class="upload-result">
                  已上传：{{ episode.videoFileName || `第 ${index + 1} 集视频` }}
                </div>
                <div v-else class="upload-tip">选择文件后立即上传</div>
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
        <el-button type="primary" :loading="submitting" @click="submit">提交待审核</el-button>
      </div>
    </el-form>
  </section>
</template>

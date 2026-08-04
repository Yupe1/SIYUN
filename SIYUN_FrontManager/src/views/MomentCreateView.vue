<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import http from '@/api/http'

const router = useRouter()
const submitting = ref(false)
const uploading = ref(false)
const imageAccept = '.jpg,.jpeg,.png,.gif,.webp'
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '')

const moment = reactive({
  title: '',
  keywords: '',
  coverUrl: '',
  coverFileName: '',
  content: '',
})

const coverPreviewUrl = computed(() => {
  if (!moment.coverUrl) return ''
  if (/^https?:\/\//i.test(moment.coverUrl)) return moment.coverUrl
  return `${apiBaseUrl}${moment.coverUrl.startsWith('/') ? '' : '/'}${moment.coverUrl}`
})

function beforeCoverUpload(file) {
  const validType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!validType) {
    ElMessage.warning('封面仅支持 jpg、png、gif、webp 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('封面图片不能超过 5MB')
    return false
  }
  return true
}

async function uploadCover(options) {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  uploading.value = true
  try {
    const data = await http.post('/api/admin/upload/moment-cover', formData, { timeout: 60000 })
    const coverUrl = data.result?.coverUrl
    if (!coverUrl) throw new Error('上传接口未返回封面地址')
    moment.coverUrl = coverUrl
    moment.coverFileName = file.name
    ElMessage.success('微圈封面上传成功')
    onSuccess?.(data)
  } catch (error) {
    if (error?.message === '上传接口未返回封面地址') ElMessage.error(error.message)
    onError?.(error)
  } finally {
    uploading.value = false
  }
}

function validate() {
  if (!moment.title.trim()) return '请填写微圈标题'
  if (!moment.coverUrl) return '请上传微圈封面图片'
  if (!moment.content.trim()) return '请填写微圈内容'
  return ''
}

async function submit() {
  const message = validate()
  if (message) {
    ElMessage.warning(message)
    return
  }
  submitting.value = true
  try {
    await http.post('/api/admin/moments', {
      title: moment.title.trim(),
      keywords: moment.keywords.trim(),
      coverUrl: moment.coverUrl,
      content: moment.content.trim(),
    })
    ElMessage.success('微圈已提交审核')
    router.push('/moments/list')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="course-create-page">
    <el-form label-width="100px" class="course-form">
      <section class="panel form-panel">
        <header>微圈内容</header>
        <div class="form-grid">
          <el-form-item label="标题">
            <el-input v-model="moment.title" maxlength="150" show-word-limit placeholder="请输入微圈标题" />
          </el-form-item>

          <el-form-item label="关键词">
            <el-input v-model="moment.keywords" maxlength="255" placeholder="选填，未填写时使用标题" />
          </el-form-item>

          <el-form-item label="封面图片" class="wide-field">
            <div class="moment-cover-block">
              <div class="moment-cover-field">
                <el-upload
                  :accept="imageAccept"
                  :before-upload="beforeCoverUpload"
                  :http-request="uploadCover"
                  :show-file-list="false"
                  :disabled="uploading"
                >
                  <el-button :icon="UploadFilled" :loading="uploading">
                    {{ moment.coverUrl ? '重新上传封面' : '选择封面图片' }}
                  </el-button>
                </el-upload>
                <span v-if="moment.coverFileName" class="upload-result">{{ moment.coverFileName }}</span>
                <span v-else class="upload-tip">支持 jpg、png、gif、webp，最大 5MB</span>
              </div>
              <el-image
                v-if="coverPreviewUrl"
                :src="coverPreviewUrl"
                :preview-src-list="[coverPreviewUrl]"
                fit="cover"
                class="moment-cover-preview"
              />
            </div>
          </el-form-item>

          <el-form-item label="内容" class="wide-field">
            <el-input
              v-model="moment.content"
              type="textarea"
              :rows="14"
              maxlength="20000"
              show-word-limit
              placeholder="请输入微圈内容"
            />
          </el-form-item>
        </div>
      </section>

      <div class="form-actions">
        <el-button @click="router.push('/moments/list')">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="uploading" @click="submit">提交审核</el-button>
      </div>
    </el-form>
  </section>
</template>

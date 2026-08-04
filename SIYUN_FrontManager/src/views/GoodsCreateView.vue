<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Delete, UploadFilled } from '@element-plus/icons-vue'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const uploadingCount = ref(0)
const categories = ref([])
const images = ref([])
const imageAccept = '.jpg,.jpeg,.png,.webp'
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '')
const pricePattern = /^(0|[1-9]\d*)\.\d{2}$/
const serviceTagOptions = ['包邮', '退换无忧', '官方']

const goods = reactive({
  cateId: null,
  goodsName: '',
  keywords: '',
  priceOriginal: '',
  intro: '',
  serviceTags: [],
})

const previewUrls = computed(() => images.value.filter((item) => item.url).map((item) => publicImageUrl(item.url)))
const priceError = computed(() => {
  const value = goods.priceOriginal.trim()
  if (!value) return ''
  if (!pricePattern.test(value) || Number(value) > 99999999.99) return '原价必须填写为两位小数，例如 99.00'
  return ''
})

async function loadCategories() {
  loading.value = true
  try {
    const data = await http.get('/api/admin/goods-categories')
    categories.value = (data.result?.categories || []).filter((item) => item.status === 1)
    if (categories.value.length === 1) goods.cateId = categories.value[0].id
  } finally {
    loading.value = false
  }
}

function beforeImageUpload(file) {
  if (images.value.length >= 9) {
    ElMessage.warning('商品图片最多上传9张')
    return false
  }
  const validType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  if (!validType) {
    ElMessage.warning('仅支持 jpg、png、webp 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('单张图片不能超过5MB')
    return false
  }
  uploadingCount.value += 1
  images.value.push({ uid: file.uid, url: '', fileName: file.name, uploading: true })
  return true
}

async function uploadImage(options) {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await http.post('/api/admin/upload/goods-image', formData, { timeout: 60000 })
    const imageUrl = data.result?.imageUrl
    if (!imageUrl) throw new Error('上传接口未返回图片地址')
    const imageIndex = images.value.findIndex((item) => item.uid === file.uid)
    if (imageIndex >= 0) {
      images.value[imageIndex] = { uid: file.uid, url: imageUrl, fileName: file.name, uploading: false }
    }
    ElMessage.success(imageIndex === 0 ? '商品主图上传成功' : '商品图片上传成功')
    onSuccess?.(data)
  } catch (error) {
    const imageIndex = images.value.findIndex((item) => item.uid === file.uid)
    if (imageIndex >= 0) images.value.splice(imageIndex, 1)
    if (error?.message === '上传接口未返回图片地址') ElMessage.error(error.message)
    onError?.(error)
  } finally {
    uploadingCount.value = Math.max(0, uploadingCount.value - 1)
  }
}

function publicImageUrl(url) {
  if (/^https?:\/\//i.test(url)) return url
  return `${apiBaseUrl}${url.startsWith('/') ? '' : '/'}${url}`
}

function moveImage(index, offset) {
  const target = index + offset
  if (target < 0 || target >= images.value.length) return
  const next = [...images.value]
  const current = next[index]
  next[index] = next[target]
  next[target] = current
  images.value = next
}

function removeImage(index) {
  images.value.splice(index, 1)
}

function validate() {
  if (!goods.cateId) return '请选择商品分类'
  if (!goods.goodsName.trim()) return '请填写商品名称'
  if (!goods.priceOriginal.trim() || priceError.value) return '原价必须填写为两位小数，例如 99.00'
  if (!images.value.length) return '请至少上传一张商品图片'
  if (uploadingCount.value > 0) return '请等待商品图片上传完成'
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
    await http.post('/api/admin/goods', {
      cateId: goods.cateId,
      goodsName: goods.goodsName.trim(),
      keywords: goods.keywords.trim(),
      imageUrls: images.value.map((item) => item.url),
      priceOriginal: goods.priceOriginal.trim(),
      intro: goods.intro.trim(),
      serviceTags: goods.serviceTags,
    })
    ElMessage.success('商品已添加并等待审核')
    router.push('/goods/list')
  } finally {
    submitting.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <section v-loading="loading" class="course-create-page">
    <el-form label-width="110px" class="course-form">
      <section class="panel form-panel">
        <header>商品基础信息</header>
        <div class="form-grid">
          <el-form-item label="商品分类">
            <el-select v-model="goods.cateId" filterable placeholder="请选择商品分类" class="full-control">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.cateName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="商品名称">
            <el-input v-model="goods.goodsName" maxlength="150" show-word-limit placeholder="请输入商品名称" />
          </el-form-item>

          <el-form-item label="原价" :error="priceError">
            <el-input v-model="goods.priceOriginal" inputmode="decimal" placeholder="例如 99.00">
              <template #append>元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="搜索关键词">
            <el-input v-model="goods.keywords" maxlength="255" placeholder="选填，未填写时使用商品名称" />
          </el-form-item>

          <el-form-item label="服务标签" class="wide-field">
            <el-checkbox-group v-model="goods.serviceTags">
              <el-checkbox v-for="tag in serviceTagOptions" :key="tag" :value="tag">{{ tag }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="商品简介" class="wide-field">
            <el-input
              v-model="goods.intro"
              type="textarea"
              :rows="4"
              maxlength="255"
              show-word-limit
              placeholder="请输入商品简介"
            />
          </el-form-item>

          <el-form-item label="商品图片" class="wide-field">
            <div class="goods-images-field">
              <div v-if="images.length" class="goods-image-list">
                <div v-for="(item, index) in images" :key="item.uid" class="goods-image-card">
                  <el-image
                    v-if="item.url"
                    :src="publicImageUrl(item.url)"
                    :preview-src-list="previewUrls"
                    :initial-index="index"
                    fit="cover"
                  />
                  <div v-else v-loading="item.uploading" class="goods-image-uploading">上传中</div>
                  <span v-if="index === 0" class="main-image-badge">主图</span>
                  <div class="goods-image-actions">
                    <el-button
                      :icon="ArrowLeft"
                      circle
                      text
                      title="左移"
                      :disabled="item.uploading || index === 0"
                      @click="moveImage(index, -1)"
                    />
                    <el-button
                      :icon="ArrowRight"
                      circle
                      text
                      title="右移"
                      :disabled="item.uploading || index === images.length - 1"
                      @click="moveImage(index, 1)"
                    />
                    <el-button
                      :icon="Delete"
                      circle
                      text
                      type="danger"
                      title="删除"
                      :disabled="item.uploading"
                      @click="removeImage(index)"
                    />
                  </div>
                  <div class="goods-image-name">{{ item.fileName }}</div>
                </div>
              </div>

              <el-upload
                v-if="images.length < 9"
                :accept="imageAccept"
                :before-upload="beforeImageUpload"
                :http-request="uploadImage"
                :show-file-list="false"
                multiple
              >
                <el-button :icon="UploadFilled" :loading="uploadingCount > 0">添加图片</el-button>
              </el-upload>
              <span class="upload-tip">最多9张，可左右调整顺序，第一张作为主图</span>
            </div>
          </el-form-item>
        </div>
      </section>

      <div class="form-actions">
        <el-button @click="router.push('/goods/list')">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="uploadingCount > 0" @click="submit">提交审核</el-button>
      </div>
    </el-form>
  </section>
</template>

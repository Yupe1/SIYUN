<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import http from '@/api/http'

const router = useRouter()
const submitting = ref(false)
const uploading = ref(false)
const period = ref([])
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '')
const amountPattern = /^(0|[1-9]\d*)(\.\d{1,2})?$/

const coupon = reactive({
  couponSn: '',
  couponName: '',
  amount: '',
  imgUrl: '',
  statusShelf: 0,
  issueType: 1,
  applyType: 0,
})

const shelfOptions = [
  { label: '下线', value: 0 },
  { label: '上线', value: 1 },
]
const issueTypeOptions = [
  { label: '用户自行领取', value: 1 },
  { label: '活动自动发放', value: 2 },
  { label: '后台定向赠送', value: 3 },
]
const applyTypeOptions = [
  { label: '通用', value: 0 },
  { label: '仅限实体商品', value: 1 },
  { label: '仅限视频课程', value: 2 },
]

const amountError = computed(() => {
  const value = coupon.amount.trim()
  if (!value) return ''
  if (!amountPattern.test(value) || Number(value) <= 0 || Number(value) > 99999999.99) {
    return '请输入大于0且最多两位小数的金额'
  }
  return ''
})

function publicImageUrl(url) {
  if (/^https?:\/\//i.test(url)) return url
  return `${apiBaseUrl}${url.startsWith('/') ? '' : '/'}${url}`
}

function beforeImageUpload(file) {
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.warning('仅支持 jpg、png、webp 图片')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('优惠券图片不能超过2MB')
    return false
  }
  uploading.value = true
  return true
}

async function uploadImage(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const data = await http.post('/api/admin/upload/coupon-image', formData, { timeout: 60000 })
    coupon.imgUrl = data.result?.imageUrl || ''
    if (!coupon.imgUrl) throw new Error('上传接口未返回图片地址')
    ElMessage.success('优惠券图片上传成功')
    options.onSuccess?.(data)
  } catch (error) {
    if (error?.message === '上传接口未返回图片地址') ElMessage.error(error.message)
    options.onError?.(error)
  } finally {
    uploading.value = false
  }
}

function validate() {
  if (!coupon.couponSn.trim()) return '请填写优惠券编号'
  if (!coupon.couponName.trim()) return '请填写优惠券名称'
  if (!coupon.amount.trim() || amountError.value) return '请输入正确的优惠券金额'
  if (!period.value || period.value.length !== 2) return '请选择优惠券有效期'
  if (uploading.value) return '请等待优惠券图片上传完成'
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
    await http.post('/api/admin/coupons', {
      couponSn: coupon.couponSn.trim(),
      couponName: coupon.couponName.trim(),
      amount: Number(coupon.amount),
      imgUrl: coupon.imgUrl || null,
      startTime: period.value[0],
      endTime: period.value[1],
      statusShelf: coupon.statusShelf,
      issueType: coupon.issueType,
      applyType: coupon.applyType,
    })
    ElMessage.success('优惠券添加成功')
    router.push('/marketing/coupons')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="course-create-page">
    <el-form label-width="110px" class="course-form">
      <section class="panel form-panel">
        <header>新增优惠券</header>
        <div class="form-grid">
          <el-form-item label="优惠券编号" required>
            <el-input v-model="coupon.couponSn" maxlength="64" placeholder="请输入唯一编号" />
          </el-form-item>

          <el-form-item label="优惠券名称" required>
            <el-input v-model="coupon.couponName" maxlength="100" placeholder="请输入优惠券名称" />
          </el-form-item>

          <el-form-item label="面值金额" required :error="amountError">
            <el-input v-model="coupon.amount" inputmode="decimal" placeholder="例如 20.00">
              <template #append>元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="上下线状态" required>
            <el-select v-model="coupon.statusShelf" class="full-control">
              <el-option v-for="option in shelfOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="发放类型" required>
            <el-select v-model="coupon.issueType" class="full-control">
              <el-option v-for="option in issueTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="适用类型" required>
            <el-select v-model="coupon.applyType" class="full-control">
              <el-option v-for="option in applyTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="有效期" required class="wide-field">
            <el-date-picker
              v-model="period"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              class="full-control"
            />
          </el-form-item>

          <el-form-item label="优惠券图片" class="wide-field">
            <div class="coupon-image-field">
              <el-upload
                :show-file-list="false"
                accept=".jpg,.jpeg,.png,.webp"
                :before-upload="beforeImageUpload"
                :http-request="uploadImage"
              >
                <el-button :icon="UploadFilled" :loading="uploading">选择图片</el-button>
              </el-upload>
              <span class="upload-tip">选填，支持 jpg、png、webp，最大2MB</span>
              <el-image v-if="coupon.imgUrl" :src="publicImageUrl(coupon.imgUrl)" fit="cover" />
            </div>
          </el-form-item>
        </div>
      </section>

      <div class="form-actions">
        <el-button @click="router.push('/marketing/coupons')">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存优惠券</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const coupons = ref([])
const goods = ref([])
const courses = ref([])
const bindings = ref(new Set())
const form = reactive({ targetType: 1, couponId: null, goodsId: null, quota: '' })
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').replace(/\/$/, '')

const availableCoupons = computed(() => coupons.value.filter((item) => item.applyType === 0 || item.applyType === form.targetType))
const availableTargets = computed(() => form.targetType === 1 ? goods.value : courses.value)
const selectedCoupon = computed(() => coupons.value.find((item) => item.id === form.couponId))
const selectedTarget = computed(() => availableTargets.value.find((item) => item.id === form.goodsId))
const quotaError = computed(() => {
  if (!form.quota) return ''
  if (!/^[1-9]\d*$/.test(form.quota) || Number(form.quota) > 2147483647) return '数量必须为正整数'
  return ''
})

function targetName(item) {
  return form.targetType === 1 ? item.goodsName : item.title
}

function targetImage(item) {
  const url = form.targetType === 1 ? item.mainPicUrl?.split(',')[0] : item.coverUrl
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return `${apiBaseUrl}${url.startsWith('/') ? '' : '/'}${url}`
}

function isBound(itemId) {
  return form.couponId && bindings.value.has(`${form.couponId}:${itemId}`)
}

async function loadOptions() {
  loading.value = true
  try {
    const data = await http.get('/api/admin/coupon-bind-options')
    const result = data.result || {}
    coupons.value = result.coupons || []
    goods.value = result.goods || []
    courses.value = result.courses || []
    bindings.value = new Set((result.bindings || []).map((item) => `${item.couponId}:${item.goodsId}`))
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.couponId) {
    ElMessage.warning('请选择优惠券')
    return
  }
  if (!form.goodsId) {
    ElMessage.warning(`请选择${form.targetType === 1 ? '商品' : '课程'}`)
    return
  }
  if (!form.quota || quotaError.value) {
    ElMessage.warning('优惠券数量必须为正整数')
    return
  }
  submitting.value = true
  try {
    await http.post('/api/admin/coupon-goods', {
      targetType: form.targetType,
      couponId: form.couponId,
      goodsId: form.goodsId,
      quota: Number(form.quota),
    })
    ElMessage.success('优惠券绑定成功')
    router.push('/marketing/coupon-goods')
  } finally {
    submitting.value = false
  }
}

watch(
  () => form.targetType,
  () => {
    form.couponId = null
    form.goodsId = null
  },
)

watch(
  () => form.couponId,
  () => {
    form.goodsId = null
  },
)

onMounted(loadOptions)
</script>

<template>
  <section v-loading="loading" class="course-create-page">
    <el-form label-width="120px" class="course-form">
      <section class="panel form-panel">
        <header>商品/课程优惠券</header>
        <div class="coupon-operation-form">
          <el-form-item label="适用对象" required>
            <el-radio-group v-model="form.targetType">
              <el-radio-button :value="1">实体商品</el-radio-button>
              <el-radio-button :value="2">视频课程</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="优惠券" required>
            <el-select v-model="form.couponId" filterable clearable placeholder="请选择匹配类型的优惠券" class="full-control">
              <el-option
                v-for="coupon in availableCoupons"
                :key="coupon.id"
                :label="`${coupon.couponName}（${coupon.amount}元${coupon.applyType === 0 ? '，通用券' : ''}）`"
                :value="coupon.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item :label="form.targetType === 1 ? '选择商品' : '选择课程'" required>
            <el-select
              v-model="form.goodsId"
              filterable
              clearable
              :disabled="!form.couponId"
              :placeholder="`请选择${form.targetType === 1 ? '商品' : '课程'}`"
              class="full-control"
            >
              <el-option
                v-for="item in availableTargets"
                :key="item.id"
                :label="targetName(item)"
                :value="item.id"
                :disabled="isBound(item.id)"
              >
                <span>{{ targetName(item) }}</span>
                <span v-if="isBound(item.id)" class="course-option-meta">已绑定</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="优惠券数量" required :error="quotaError">
            <el-input v-model="form.quota" inputmode="numeric" maxlength="10" placeholder="请输入正整数">
              <template #append>张</template>
            </el-input>
          </el-form-item>

          <div v-if="selectedCoupon && selectedTarget" class="coupon-target-preview">
            <el-image :src="targetImage(selectedTarget)" fit="cover">
              <template #error><div class="coupon-image-empty">暂无图片</div></template>
            </el-image>
            <div>
              <strong>{{ targetName(selectedTarget) }}</strong>
              <span>优惠券：{{ selectedCoupon.couponName }}</span>
              <span>面值：{{ selectedCoupon.amount }} 元</span>
            </div>
          </div>
        </div>
      </section>

      <div class="form-actions">
        <el-button @click="router.push('/marketing/coupon-goods')">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认绑定</el-button>
      </div>
    </el-form>
  </section>
</template>
